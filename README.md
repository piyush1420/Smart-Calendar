# Smart Calendar

> A solo MS CS portfolio project. I'm independently extending a Java calendar app into an AI-powered, multi-user scheduling **service** — the AI layer, the evaluation harness, and the roadmap below are my individual work. *(The original desktop-calendar core began as a CS5010 group project; see [Contributions](#contributions).)*

## Roadmap — what I'm building next

A solo learning roadmap, framed as agile epics → user stories with acceptance criteria (AC). I use the agile structure to keep the work scoped and honest about what's shipped versus planned — it's a one-person project, not a team effort.

**At a glance**

| Epic | Stage |
|------|-------|
| AI semantic search & LLM summaries (Spring AI) | Done |
| Retrieval-quality evaluation harness (precision@k, MRR, nDCG) | Done |
| 1 — Calendar → multi-user service | **Now** |
| 2 — Concurrent booking & conflict detection at scale | Next |
| 3 — Horizontal scale & infrastructure | Later |
| 4 — Real-time multi-user sync | Stretch |

### Epic 1 — From desktop app to a multi-user service · *Now*

**Goal:** turn the in-memory desktop app into a persistent, multi-user backend service.

- *As a user,* I want my events stored durably so they survive restarts. — **AC:** events persisted in PostgreSQL; in-memory lists removed.
- *As an API client,* I want a REST API so I can manage events programmatically. — **AC:** CRUD endpoints for events and calendars, documented with OpenAPI.
- *As a user,* I want an account and multiple calendars so my data is isolated and organized. — **AC:** user model + authentication; every event scoped to a user and calendar.

### Epic 2 — Concurrent booking & conflict detection at scale · *Next*

**Goal:** handle concurrent bookings correctly and detect scheduling conflicts efficiently as data grows (the same problem family as seat/ticket reservation).

- *As two users booking the same slot at once,* I want the overlap rejected so double-booking is impossible. — **AC:** concurrent overlapping bookings cannot both succeed, enforced via optimistic locking (a version column) and a PostgreSQL `tstzrange` + GiST **exclusion constraint** at the database level.
- *As the system,* I want conflict detection to stay fast as the event count grows. — **AC:** `O(log n)` conflict checks via a GiST range index; verified at 10k / 100k / 1M events.
- *As the developer,* I want performance measured, not assumed. — **AC:** Gatling load tests report p95/p99 latency and booking throughput under concurrent contention, published in this README.

### Epic 3 — Horizontal scale & infrastructure · *Later*

**Goal:** show the service scales horizontally and handles read-heavy load — all runnable locally for free.

- Run multiple stateless instances behind an **Nginx** load balancer on a shared Postgres, with concurrent requests handled on Java 21 virtual threads.
- Add a **Redis** cache on hot read paths (a user's upcoming events).
- Back semantic search with **pgvector**, replacing the in-memory index.
- *(Stretch)* shard events by `user_id`. — **AC:** horizontal-scaling benchmarks (p95/p99 across instances) published.

### Epic 4 — Real-time multi-user sync · *Stretch*

**Goal:** live collaboration on shared calendars.

- *As a collaborator,* I want to see others' changes immediately on a shared calendar. — **AC:** WebSocket push on change; eventual consistency across connected clients.

**My definition of done (per epic)**

- Automated tests pass in CI.
- For performance epics, benchmark numbers are published in this README.
- The full stack runs locally via a single `docker compose up` (Postgres + pgvector, Redis, Nginx, app instances) — no paid cloud required.

> **A note on the numbers:** targets such as `O(log n)` conflict detection and p95/p99 latency are **acceptance criteria for planned work** — I'll back them with published benchmarks as each epic lands, not claim them before they're measured.

---

# AI Features (Spring AI + OpenAI)

The calendar includes an AI layer built on **Spring AI** and the **OpenAI API**:

- **Event summaries** — natural-language one-line summaries of an event via an LLM.
- **Semantic event search** — a "find similar events" feature that ranks events by meaning rather than keywords, using OpenAI embeddings.

## How it works

`startapp.ai.AiIntegrationService` is the entry point:

- `summarizeEvent(text)` uses a Spring AI `ChatClient`.
- `indexEvents(events)` embeds each event (subject + description + location) via the `EmbeddingModel`.
- `findSimilar(query, k)` embeds the query and returns the top-k events ranked by **cosine similarity**.

Details:

- The vector index is **in-memory** and rebuilt on each run.
- Indexing embeds events **concurrently using Java 21 virtual threads** (`Executors.newVirtualThreadPerTaskExecutor()`). Each embedding is a blocking OpenAI HTTP call, so virtual threads let them run in parallel without tying up a large thread pool.

## Requirements & configuration

- **JDK 21+** (required for virtual threads). The pom sets `<java.version>21</java.version>`.
- Set your OpenAI key:
  ```bash
  export OPENAI_API_KEY=sk-...
  ```
- `src/main/resources/application.yml` configures Spring AI (API key, connect/read timeouts).

## Running the AI features

The AI runners are gated behind program arguments so they don't interfere with the normal GUI / interactive / headless modes.

**Semantic-search demo** — indexes a sample set of events, runs a few queries, and prints the ranked matches:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=ai-demo
```

**Evaluation** — measures retrieval quality (see below):
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=ai-eval
```

> Note: `CalendarApp` also routes command-line modes, so passing `ai-demo` / `ai-eval` prints a harmless `Usage: ...` line before the AI output. The AI output follows it.

## Retrieval evaluation harness

Package `startapp.ai.eval` measures how well semantic search ranks results, against a hand-labeled query set.

- **Metrics** (binary relevance): precision@k, recall@k, MRR, nDCG.
- **`RetrievalMetrics`** — the metric implementations.
- **`RetrievalMetricsTest`** — JUnit unit tests for the metric logic. These run **for free**, no OpenAI calls:
  ```bash
  mvn test
  ```
- **`EventSearchEvaluator`** — runs a labeled query set through `findSimilar` and aggregates the metrics.
- **Ground truth**: `src/main/resources/event-search-ground-truth.json` maps each query to its relevant event IDs. Event IDs are of the form `subject@startDateTime`.

The `ai-eval` run prints a summary plus a per-query breakdown:
```
Retrieval evaluation @k=3 over 25 queries
  Precision@3 : 0.84
  Recall@3    : 0.86
  MRR         : 0.88
  nDCG@3      : 0.91
```
(Run it to populate the real values — they aren't hardcoded.)

> Design note: the metric unit tests are CI-safe because they make no API calls. The `ai-eval` run hits OpenAI, so it's an **integration check, not a unit test** — paid API calls shouldn't fire on every CI push.

## Limitations / next steps

- The demo indexes a **hardcoded** event set and the index is **in-memory** (not persisted). For persistence, swap in Spring AI's `SimpleVectorStore` (file-backed) or a vector database such as **pgvector**.
- When indexing large event sets, bound the virtual-thread executor with a `Semaphore` to cap concurrent OpenAI calls and stay under rate limits.

### Run Procedure

1) Navigate to the source directory

```
cd /Users/atultiwary/Documents/PDP/group/Assignment_4_Part_I/src/main/java
```

2) Compile the Java files

```
javac -d . startapp/CalendarApp.java controller/CommandParser.java
```

3) Run the Application

GUI Mode
```
java startapp.CalendarApp
```

Interactive Mode
```
java startapp.CalendarApp --mode interactive
```

## Headless Mode

For valid commands:

```
java startapp.CalendarApp --mode headless <full path of the valid_commands.txt file>
```

For invalid commands:

```
java startapp.CalendarApp --mode headless <full path of the invalid_commands.txt file>
```

### To run the jar

**Point to your res directory :**
```
command : cd <your absolute path to the res directory>
Example :
cd </Users/atultiwary/Documents/PDP/group/Assignment_5/Group-Project-CS5010/Assignment_5_Part_II/res>
```
**To run in GUI mode**
```
command : java -jar cs5010-group-project.jar 
Example :
java -jar cs5010-group-project.jar 
```

**To run in interactive mode**
```
command : java -jar cs5010-group-project.jar --mode interactive
Example :
java -jar cs5010-group-project.jar --mode interactive
```

**To run in headless mode :**
```
command : java -jar cs5010-group-project.jar --mode headless <your absolute path>
Example :
java -jar cs5010-group-project.jar --mode headless </Users/atultiwary/Documents/PDP/group/Assignment_5/Group-Project-CS5010/Assignment_5_Part_II/res/valid_command_assignment5.txt>
```

All commands are provided in the respective .txt files.


## Using Backticks for name, description `NewPropertyValue`

## Problem

Commands like `edit events subject Recurring Meeting Updated Meeting` are ambiguous.
Is `Updated Meeting` part of the event name or the new value?

## Solution

Use **backticks** to explicitly denote the `NewPropertyValue`:

Here, `Updated Meeting` is clearly the new value.

---

### Why Backticks?

1. **Eliminates Ambiguity**:
    - Clearly separates the event name from the new value.

2. **Supports Spaces**:
    - Allows `NewPropertyValue` to contain spaces or special characters.

3. **User-Friendly**:
    - Easy to read and understand.

---

### Example

#### Ambiguous Command

#### Clear Command with Backticks

---

### Implementation

The `CommandProcessor` extracts text enclosed in backticks as the `NewPropertyValue` and removes the
backticks for processing.

---

### Benefits

- **No Ambiguity**: Clear distinction between event name and new value.
- **Flexibility**: Supports complex `NewPropertyValue` strings.
- **Consistency**: Ensures reliable command parsing.

---

Using backticks ensures robust and user-friendly command parsing. 🚀

# Updating Recurring Event Occurrences (N)

## Problem

When updating the number of occurrences (N) for a recurring event, we need to ensure that:

- Only the specified event is updated.
- The last occurrence is deleted when reducing N to N-1.

### Supported Commands

#### Update a Specific Event

```
edit event <N> <eventName> from <dateStringTtimeString> to <dateStringTtimeString> with <N>
```

**Purpose:** Updates the number of occurrences (N) for a specific event.

**Behavior:**

- If N is reduced, the last occurrence is deleted.
- If N is increased, new occurrences are added.

#### Example

```
edit event 3 Recurring Meeting from 2025-03-10T10:00 to 2025-03-10T11:00 with 2
```

Updates the recurring event "Recurring Meeting" from 3 occurrences to 2 occurrences.

- The last occurrence (3rd) is deleted.

### Unsupported Commands

#### 1. Update All Events Starting at a Specific Time

```
edit events <N> <eventName> from <dateStringTtimeString> with <N>
```

**Why Unsupported?**

- This command can update multiple events with the same name and start time, leading to unintended
  changes.

#### 2. Update All Events with the Same Name

```
edit events <N> <eventName> <N>
```

**Why Unsupported?**

- This command can update all events with the same name, even if they are unrelated, causing
  undesired modifications.

## Solution

### Supported Command: Update a Specific Event

The `CommandProcessor` class processes this command as follows:

1. **Parse the Command:** Extract `<N>`, `<eventName>`, `<dateStringTtimeString>`, and the new `<N>`
   .
2. **Find the Event:** Locate the specific recurring event using the event name and start time.
3. **Update Occurrences:**
    - If the new `<N>` is less than the current number of occurrences, delete the last occurrence.
    - If the new `<N>` is greater, add new occurrences.
4. **Save Changes:** Update the event in the calendar.

### Example Implementation

```java
private boolean editEventOccurrences(String eventName,LocalDateTime startDateTime,int newOccurrences){
        // Find the recurring event
        RecurringEventStorage recurringEvent=findRecurringEvent(eventName,startDateTime);
        if(recurringEvent==null){
        return false; // Event not found
        }

        // Update the number of occurrences
        recurringEvent.setOccurrences(newOccurrences);

        // Regenerate occurrences
        List<CalendarEvent> updatedEvents=recurringEvent.generateOccurrences();
        events.removeIf(e->e.getSubject().equals(eventName)); // Remove old occurrences
        events.addAll(updatedEvents); // Add updated occurrences

        return true;
        }
```

### Resolving Encoding Issues in Java Compilation

Issue Description

The error is due to the use of a non-ASCII character (•) in the System.out.printf statement. The •
character is a Unicode bullet point, and the Java compiler is having trouble encoding it in the
default ASCII encoding.

Specify UTF-8 Encoding for the Java Compiler :

If you want to keep the • character, you need to ensure that the Java compiler uses UTF-8 encoding.
You can do this by specifying the -encoding option when compiling your Java files.

Compile with UTF-8 Encoding

Run the following command to compile your Java files with UTF-8 encoding:

```
javac -encoding UTF-8 -d . startapp/CalendarApp.java controller/CommandParser.java view/CalendarPrinter.java

```

Explanation

The -encoding UTF-8 flag tells the Java compiler to use UTF-8 encoding, which supports Unicode
characters like •.


### Contributions

**Atul:** Implemented the entire MVC structure for Edit Command, Recurring Events, Export to CSV,
and the parser class for Print and Show commands. Also created this README file. For part 2 implemented the use and copy features. For part 3 implemented the GUI features like edit calendar, event, export and import the events.


**Piyush:** Implemented the Calendar Event and Event Storage, including the Command Parser for
Create Event, and the Model and View for Print. For part 2 implemented the clearer command set, create and edit calendar features. For part 3 implemented the GUI features like create calendar, event, print the events.



**Testing:** Code testing was shared mutually across both contributors.

> Note: the AI layer (Spring AI semantic search, summaries), the retrieval evaluation harness, and the roadmap above are my (Atul's) individual work, added after the original group assignment.

###All the features are working

### Testing Procedure

For testing, we created an all-day event from March 10, 2025, to March 24, 2025, recurring every
Monday and Tuesday, and successfully imported it into Google Calendar, a screenshot of which is
attached in the root folder's res directory.

### Date and Time Format :

-   yyyy-MM-ddTHH:mm


### Day of the Week Mapping

The application uses the following mapping for days of the week:

| Character | Day of the Week  |
|-----------|----------------|
| M         | Monday         |
| T         | Tuesday        |
| W         | Wednesday      |
| R         | Thursday       |
| F         | Friday         |
| S         | Saturday       |
| U         | Sunday         |

This mapping is used to represent days in a compact format, such as `MRUWFST`, where each letter
corresponds to a specific day.

### Notes

1. Due to the ambiguous nature of the question, which initially stated `dateStringTtimeString` as
   the format, we have standardized the format as follows:

- For **Create**, **Print**, and **Export** commands, the format is `yyyy-MM-dd HH:mm`.
- For **Edit** and **Show Status** commands, the format is `yyyy-MM-ddTHH:mm`.

2. The application expects the following formats:

- `<dateString>`: A string of the form `yyyy-MM-dd` (e.g., `2023-10-01`).
- `<timeString>`: A string of the form `HH:mm` (e.g., `10:00`).
- `<dateTime>`:
    - For **Create**, **Print**, and **Export** commands: `yyyy-MM-dd HH:mm` (
      e.g., `2023-10-01 10:00`).
    - For **Edit** and **Show Status** commands: `yyyy-MM-ddTHH:mm` (e.g., `2023-10-01T10:00`).

# Recent Changes and Improvements

## Calendar Application Design Patterns

## Benefits of Using the Command Design Pattern in Controller Package

### Modularity

- Each command is encapsulated in its own class, improving maintainability and extensibility.
- New commands can be added without modifying existing code (Open/Closed Principle).

### Separation of Concerns

- The command classes handle execution logic, while the parser manages parsing and validation.
- This separation ensures a clean and organized codebase.

### Reusability

- Command objects can be reused across different execution contexts (CLI, GUI, etc.).
- The same command logic can serve multiple interfaces.

### Flexibility

- Commands can be extended to support features like undo/redo functionality.
- Allows dynamic command execution and queuing.

### Improved Testing

- Each command can be independently tested, leading to better test coverage.
- Mocking and unit testing are simplified due to clear separation of responsibilities.

### Scalability

- Adding new commands is straightforward—create a new class and update the CommandParser.
- Scales well as the application grows.

### Cleaner Code

- The CommandParser becomes simpler as it only creates and executes command objects.
- Logic for each command is encapsulated in its respective class.

### Better Error Handling

- Command-specific error handling can be implemented within each command class.
- Improves debugging and exception handling.

### Decoupling

- The CommandParser (invoker) is decoupled from command logic, promoting loose coupling.
- Encourages high cohesion in code design.

### Future-Proofing

- Makes it easier to adapt to future requirements, such as new command types or integrations.

## Example Use Case

### Before:

- The CommandParser directly invoked parser classes, resulting in tightly coupled code.

### After:

- The CommandParser creates and executes command objects, which delegate to parser classes.
- The system becomes more modular and maintainable.

## Summary

By adopting the Command Design Pattern, the controller package becomes more modular, scalable, and
maintainable. This approach promotes clean code practices and prepares the application for future
enhancements.

## Future-Proof Export Functionality

To support future export formats like PDF and JSON, the design introduces an `EventExporter`
interface for extensibility. The `ExporterFactory` dynamically selects an exporter (
e.g., `CSVExporter`, `PDFExporter`).

### Example:

```java
public interface EventExporter {
  void export(String filePath) throws IOException;
}
```

```java
public class ExporterFactory {
  public static EventExporter createExporter(String format, EventStorage storage) {
    return switch (format.toLowerCase()) {
      case "csv" -> new CSVExporter(storage);
      case "pdf" -> new PDFExporter(storage);
      default -> throw new IllegalArgumentException("Unsupported format: " + format);
    };
  }
}
```

This modular approach ensures easy scalability, decoupling, and maintainability. However, I couldn't
implement this as it would require significant code changes over the previous assignment.

# Calendar Application - Edit Event Start and End Times

## Overview

The calendar application now allows users to edit the start and end times of events using the `edit`
command. This feature provides flexibility in adjusting event timings as needed.

## Commands

### 1. Edit Start Time:

To change the start time of a specific event:

**Example:**

### 2. Edit End Time:

To change the end time of a specific event:

**Example:**

### 3. Edit Multiple Events:

To update the start or end time for all events with the same name:

- **Update start time for all events with the same name:**

**Example:**

## Implementation Details

**Properties of event that can be edited :**

- subject (name of the event)
- start (start date time of event)
- end (end date time of event)
- location
- description
- type

- The `EventProperty` enum has been extended to include the `START` and `END` properties.
- The `updateProperty` method in the `EditEvent` class has been updated to handle changes to start
  and end times.
- Date/time strings are parsed and validated using the `parseDateTime` method to ensure correct
  formatting.

**Example Code:**

```java
CalendarEvent event=new CalendarEvent("Meeting",LocalDateTime.now(),LocalDateTime.now().plusHours(1),"Team Meeting","Office","Work");
        EditEvent editEvent=new EditEvent(event);

        editEvent.updateProperty("start","2023-10-01T12:00");
        editEvent.updateProperty("end","2023-10-01T13:00");

        This structure will provide users with a clear set of commands they can use along with examples and details about how the feature works.
```

### Timezone Conversion Behavior

1. **Timezone Adjustment**:
    - When updating a calendar's timezone, all existing events are adjusted to reflect the new
      timezone while preserving their **instant in time**.
    - For example, if an event is at **12:00 AM (midnight)** in `America/New_York`, it will be
      adjusted to **5:00 AM** or **6:00 AM** in `Europe/Paris`, depending on daylight saving time (
      DST).

2. **Daylight Saving Time (DST)**:
    - The application automatically accounts for DST differences between timezones.
    - During DST (March to October/November):
        - New York is **UTC-4**.
        - Paris is **UTC+2**.
        - Time difference: **6 hours** (Paris is 6 hours ahead).
    - Outside DST (November to March):
        - New York is **UTC-5**.
        - Paris is **UTC+1**.
        - Time difference: **5 hours** (Paris is 5 hours ahead).

3. **Example**:
    - If an event is at **12:00 AM (midnight)** in `America/New_York`:
        - During DST, it becomes **6:00 AM** in `Europe/Paris`.
        - Outside DST, it becomes **5:00 AM** in `Europe/Paris`.

4. **How It Works**:
    - The application uses Java's `ZonedDateTime` and `withZoneSameInstant` to ensure accurate
      timezone conversions.
    - Event times are adjusted based on the **instant in time**, not the local time, ensuring
      consistency across timezones.

## Calendar Name Constraints

The application allows calendar names to contain special characters since there is no explicit
restriction mentioned. Users can create calendars with names that include symbols such as `-`, `!`
, `_`, etc.

### Example:

```sh
create calendar --name Work-Calendar! --timezone America/New_York
```

### Editing Events in the Calendar

Since in the new assignment, it is mentioned that we are now allowed to edit any valid event,
whether recurring or non-recurring, as long as it matches the specified name and starts at or after
the given date and time, we have updated our implementation accordingly. The from keyword does not
require an exact match but includes all future occurrences. We have modified our logic and existing
test cases to support this behavior, ensuring seamless event modifications across the calendar.

## Editing Events

The edit events command allows modifying a specific property of all events with the same event name.

Command Format:

edit events <property> <eventName> <NewPropertyValue>

	•	Updates the given property (e.g., name, location, description, start, end) for all matching events.

Example Usage:

edit events name Meeting `New Meeting`
edit events location Conference `Room 202`
edit events description Planning `Project Kickoff`
edit events end Team Meeting 2023-10-15T11:30

Editing Start and End Times

When modifying start or end times, the new values must be provided in the format:

YYYY-MM-DDTHH:MM

	•	New start time should not exceed the existing end time.
	•	New end time should not precede the existing start time.

This ensures that the event duration remains valid and avoids scheduling conflicts.

## Editing Recurring Events - Important Note

Avoid editing recurring events by setting a specific start date (e.g., 2023-10-16T10:00), as this
may cause conflicts with other occurrences in the series. Instead, edit individual instances
carefully or adjust the recurrence settings.

## CSV Export Location

The generated CSV file will be stored in the class path:
src/main/java

### Test Cases Setup

Ensure the src/test/resources directory exists in your project. If missing, create it manually in
IntelliJ IDEA under src/test.

Steps:

1. Create the resources folder inside src/test.
2. Mark it as Test Resources Root in IntelliJ (Project Structure → Modules).
3. Run tests via IntelliJ, Maven (mvn test), or Gradle (./gradlew test).

This directory is required for test execution.

### Generic CalendarOperations Interface

**Benefits**

• Type-Safe – Prevents unsafe casting.

• Extensible – Supports future event types.

• Minimal Impact – Only adds generics, no major code changes.

**Future Use Cases**

• Multiple Event Types – RecurringEvent, HolidayEvent.

• Different Calendar Implementations – RemoteCalendar, SharedCalendar.

``
public interface CalendarOperations<E extends Event> { void addEvent(E event); void removeEvent(E event); }
``

``
public class Calendar implements CalendarOperations<CalendarEvent> { }
``

**Why Genrics?**

| Approach        | Pros                    | Cons                |
|----------------|-------------------------|---------------------|
| Non-generic    | Simple                   | Limited flexibility |
| Raw Event      | Works for all            | Requires casting    |
| Generic Approach | Type-safe, future-proof | Slightly complex    |

## Testing Strategy

The `main` method in the `StartApp` class serves as the entry point for the application and is
responsible for handling user interactions. Since `main` methods do not return values and primarily
deal with I/O operations, they cannot be directly tested using unit test cases.

While Behavior-Driven Development (BDD) tests using Cucumber could be written for integration
testing, this was beyond the scope of the assignment. Therefore, unit tests were not implemented for
the `main` method.


### GUI

The application can be run in either default mode (without specifying any command-line arguments) or in GUI mode by providing the `gui` flag.

#### Running the Application

You can start the application using the following commands:

```
java startapp.CalendarApp --mode gui
```

**Using the GUI**

	•	Create and Edit Events:
Right-click on a day to create or edit an event.

	•	View Events:
Click on a date to view all events scheduled for that day.

	•	Event Details:
Both the start and end date-times are displayed to avoid ambiguity. For example, if an event originally scheduled from 14:00 to 15:00 on April 4 is edited so that it ends at 13:00 on April 5, showing only the times would be unclear. Displaying full date-time information ensures the event duration is unmistakable.

**Testing**

•	JUnit Testing Focus:
We use JUnit to verify the controller logic and command generation rather than the visual rendering of GUI components. This allows us to ensure that the controller behaves correctly in response to user actions without needing to simulate actual button clicks or UI interactions.

•	GUI Component Testing Limitations:
Classes like CreateCalendarDialog depend on Swing for rendering, which is managed by the underlying UI framework. Since our primary goal is to test business logic, we isolate and test the backend functionality using dummy objects and reflection, rather than testing the GUI's visual output.

•	GUIControllerBridge Mutation Coverage:
The GUIControllerBridge.java has been thoroughly tested with mutation testing to confirm that it reacts correctly to simulated events, ensuring that commands are executed as expected.

•	**PIT Mutation Testing Note**:
When running PIT mutation tests, if any pop-up dialogs (e.g., "Event created successfully") appear, simply click "OK." These dialogs require no input and do not affect the outcome of the tests.
Rebuild project if pit test fails, maybe due to thread.sleep() in test testGUIModeInvocationCount()
Pit Configration Required :
```
<excludedTestClasses>
    <param>view.gui.*</param>
</excludedTestClasses>
```
