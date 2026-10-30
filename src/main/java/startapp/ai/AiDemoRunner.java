package startapp.ai;

import model.CalendarEvent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Tiny end-to-end demo of the AI features, gated behind the "ai-demo" program
 * argument so it doesn't fire alongside CalendarApp's GUI/interactive runner.
 *
 * Run with:  mvn spring-boot:run -Dspring-boot.run.arguments=ai-demo
 *
 *   - builds a hardcoded set of CalendarEvents
 *   - indexes them concurrently (one virtual thread per OpenAI embedding call)
 *   - runs a few free-text queries and prints the top matches
 */
@Component
public class AiDemoRunner implements CommandLineRunner {

    private final AiIntegrationService ai;

    public AiDemoRunner(AiIntegrationService ai) {
        this.ai = ai;
    }

    @Override
    public void run(String... args) {
        // Only run when explicitly requested. CalendarApp is also a CommandLineRunner,
        // so without this guard both would fire and the Swing GUI would launch too.
        if (!Arrays.asList(args).contains("ai-demo")) {
            return;
        }

        List<CalendarEvent> events = List.of(
                event("Weekly engineering standup",
                        "2025-04-07T09:30", "2025-04-07T10:00",
                        "Team sync on sprint progress and blockers", "Zoom"),
                event("1:1 with Sarah",
                        "2025-04-08T14:00", "2025-04-08T14:30",
                        "Career check-in with manager", "Office"),
                event("Q2 planning workshop",
                        "2025-04-10T10:00", "2025-04-10T12:00",
                        "Quarterly roadmap and OKRs", "Conference Room A"),
                event("Dentist appointment",
                        "2025-04-11T08:00", "2025-04-11T08:45",
                        "Routine cleaning", "Downtown Dental"),
                event("Team happy hour",
                        "2025-04-11T17:30", "2025-04-11T19:00",
                        "Drinks and food with the team", "The Tap House"),
                event("Yoga class",
                        "2025-04-12T07:00", "2025-04-12T08:00",
                        "Vinyasa flow", "Studio Loft")
        );

        long t0 = System.nanoTime();
        ai.indexEvents(events);
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("%nIndexed %d events in %d ms (concurrent embedding via virtual threads).%n",
                events.size(), ms);

        for (String q : List.of(
                "morning team meeting",
                "social with coworkers",
                "doctor visit",
                "fitness")) {
            System.out.printf("%nQuery: %s%n", q);
            int rank = 1;
            for (CalendarEvent ev : ai.findSimilar(q, 3)) {
                System.out.printf("  %d. %s%n", rank++, ev.getSubject());
            }
        }
    }

    private static CalendarEvent event(String subject, String start, String end,
                                       String description, String location) {
        return new CalendarEvent(
                subject,
                LocalDateTime.parse(start),
                LocalDateTime.parse(end),
                description,
                location,
                "public");
    }
}