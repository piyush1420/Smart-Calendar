package startapp.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.CalendarEvent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import startapp.ai.eval.EventSearchEvaluator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Runs the retrieval evaluation against the live findSimilar() and prints
 * precision@k / recall@k / MRR / nDCG. Gated behind the "ai-eval" argument.
 *
 *   mvn spring-boot:run -Dspring-boot.run.arguments=ai-eval
 *
 * NOTE: this hits OpenAI (it embeds the events and the queries), so it costs
 * API calls and is an INTEGRATION check, not a free unit test. The pure metric
 * unit tests live in RetrievalMetricsTest and run under `mvn test` for free.
 * That separation is deliberate — you don't want paid API calls firing in CI.
 */
@Component
public class AiEvalRunner implements CommandLineRunner {

    private static final int K = 3;

    private final AiIntegrationService ai;

    public AiEvalRunner(AiIntegrationService ai) {
        this.ai = ai;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!Arrays.asList(args).contains("ai-eval")) {
            return;
        }

        // Must be the SAME events the ground-truth file labels (idOf = subject@start).
        List<CalendarEvent> events = List.of(
                event("Weekly engineering standup", "2025-04-07T09:30", "2025-04-07T10:00",
                        "Team sync on sprint progress and blockers", "Zoom"),
                event("1:1 with Sarah", "2025-04-08T14:00", "2025-04-08T14:30",
                        "Career check-in with manager", "Office"),
                event("Q2 planning workshop", "2025-04-10T10:00", "2025-04-10T12:00",
                        "Quarterly roadmap and OKRs", "Conference Room A"),
                event("Dentist appointment", "2025-04-11T08:00", "2025-04-11T08:45",
                        "Routine cleaning", "Downtown Dental"),
                event("Team happy hour", "2025-04-11T17:30", "2025-04-11T19:00",
                        "Drinks and food with the team", "The Tap House"),
                event("Yoga class", "2025-04-12T07:00", "2025-04-12T08:00",
                        "Vinyasa flow", "Studio Loft")
        );
        ai.indexEvents(events);

        List<EventSearchEvaluator.QueryCase> cases = new ObjectMapper().readValue(
                getClass().getResourceAsStream("/event-search-ground-truth.json"),
                new TypeReference<>() {});

        // Adapt the live search to the evaluator's interface (event -> stable id).
        EventSearchEvaluator.EventSearcher searcher =
                (query, k) -> ai.findSimilar(query, k).stream()
                        .map(AiIntegrationService::idOf)
                        .toList();

        var report = new EventSearchEvaluator().evaluate(cases, searcher, K);
        System.out.println();
        System.out.println(report.format());
        report.perQuery().forEach(pq ->
                System.out.printf("  %-18s P=%.2f  R=%.2f  RR=%.2f  nDCG=%.2f%n",
                        pq.queryId(), pq.precision(), pq.recall(),
                        pq.reciprocalRank(), pq.ndcg()));
    }

    private static CalendarEvent event(String subject, String start, String end,
                                       String description, String location) {
        return new CalendarEvent(subject, LocalDateTime.parse(start),
                LocalDateTime.parse(end), description, location, "public");
    }
}