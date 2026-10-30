package startapp.ai;

import model.CalendarEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * AI features for the calendar app:
 *   - summarizeEvent: chat-based one-line summary
 *   - indexEvents: embed events in parallel (virtual threads) into an in-memory store
 *   - findSimilar: rank indexed events by cosine similarity to a free-text query
 */
@Service
public class AiIntegrationService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    // In-memory vector store. Replace with Spring AI's SimpleVectorStore or pgvector later.
    private final List<IndexedEvent> index = new ArrayList<>();

    public AiIntegrationService(ChatModel chatModel, EmbeddingModel embeddingModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.embeddingModel = embeddingModel;
    }

    /** Chat-based summary */
    public String summarizeEvent(String eventDescription) {
        return chatClient
                .prompt()
                .system("Summarize this event in one short sentence.")
                .user(eventDescription)
                .call()
                .content();
    }

    /**
     * Embed every event concurrently using virtual threads.
     * Each embed() call blocks on an OpenAI HTTP round-trip, so on platform threads
     * this would either serialize (slow) or burn a fat pool thread per call.
     * Virtual threads let all N calls wait in parallel essentially for free — the bottleneck
     * becomes OpenAI's rate limit, not our thread count.
     */
    public void indexEvents(List<CalendarEvent> events) {
        index.clear();
        try (ExecutorService vt = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<IndexedEvent>> futures = new ArrayList<>(events.size());
            for (CalendarEvent ev : events) {
                futures.add(vt.submit(() -> {
                    float[] vec = embeddingModel.embed(textFor(ev));
                    return new IndexedEvent(ev, vec);
                }));
            }
            for (Future<IndexedEvent> f : futures) {
                index.add(f.get()); // propagate any embedding failure
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to index events", e);
        }
    }

    /** Top-k events most semantically similar to the query, ranked best-first. */
    public List<CalendarEvent> findSimilar(String query, int k) {
        if (index.isEmpty()) return List.of();
        float[] q = embeddingModel.embed(query);
        return index.stream()
                .map(ie -> new Scored(ie.event(), cosine(q, ie.vector())))
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(k)
                .map(Scored::event)
                .toList();
    }

    /**
     * Stable string identifier for an event.
     * CalendarEvent has no id field, so I derive one from subject + startTime.
     * The eval harness uses this to match results against ground-truth labels.
     */
    public static String idOf(CalendarEvent ev) {
        return ev.getSubject() + "@" + ev.getStartDateTime();
    }

    /** Text fed into the embedding model. */
    private static String textFor(CalendarEvent ev) {
        return (ev.getSubject() == null ? "" : ev.getSubject())
                + ". " + (ev.getDescription() == null ? "" : ev.getDescription())
                + " Location: " + (ev.getLocation() == null ? "" : ev.getLocation());
    }

    private static double cosine(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector length mismatch: " + a.length + " vs " + b.length);
        }
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na  += a[i] * a[i];
            nb  += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private record IndexedEvent(CalendarEvent event, float[] vector) {}
    private record Scored(CalendarEvent event, double score) {}
}