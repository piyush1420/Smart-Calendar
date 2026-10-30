# Smart Calendar

A Java calendar application with multiple calendars, time zones, recurring events, CSV import/export, a Swing desktop GUI and two text-based modes. It also includes an AI layer built on Spring AI and OpenAI for event summaries and semantic event search, plus an evaluation harness that measures search quality.

## Features

**Calendar**
- Multiple calendars, each with its own time zone. Switch between them or rename them at any time.
- Changing a calendar's time zone shifts its events so each one keeps the same moment in time.
- Single, all-day and recurring events. Recurring events repeat on chosen weekdays, either a set number of times or until a date.
- Optional location, description and public/private type on each event.
- Conflict detection: a new event that overlaps an existing one is declined.
- Edit one event, every event with a given name from a start time onward, or every event with a given name.
- Copy one event, one day, or a date range into another calendar. Day and range copies convert times between the two calendars' time zones.
- Export to CSV in Google Calendar's import format, and import events from CSV.

**Interfaces**
- **GUI (Swing):** month view, calendar picker, and dialogs to create, edit and view events.
- **Interactive mode:** type commands one at a time.
- **Headless mode:** run a file of commands and exit.

**AI (Spring AI + OpenAI)**
- One-line event summaries from an LLM.
- Semantic search: find events by meaning instead of keywords, using OpenAI embeddings and cosine similarity.
- Embeddings are computed concurrently on Java 21 virtual threads.
- An evaluation harness scores search quality with precision@k, recall@k, MRR and nDCG.

## Tech stack

Java 21 · Maven · Spring Boot 3.4 · Spring AI 1.0 (OpenAI) · Swing · JUnit 4

## Getting started

### Requirements

- JDK 21 or newer (needed for virtual threads)
- Maven 3
- An OpenAI API key (only for the AI features)

### Build and test

```bash
mvn test                   # run the unit tests
mvn package -DskipTests    # build the runnable jar in target/
```

### Run

Spring Boot starts in headless mode by default, so the GUI needs `java.awt.headless=false`.

```bash
# GUI
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false"

# Interactive mode
mvn spring-boot:run -Dspring-boot.run.arguments="--mode interactive"

# Headless mode, using the sample command file
mvn spring-boot:run -Dspring-boot.run.arguments="--mode headless res_A5/valid_command_assignment5.txt"
```

Or run the built jar:

```bash
java -Djava.awt.headless=false -jar target/cs5010-group-project-1.0-SNAPSHOT.jar
java -jar target/cs5010-group-project-1.0-SNAPSHOT.jar --mode interactive
java -jar target/cs5010-group-project-1.0-SNAPSHOT.jar --mode headless <path-to-commands.txt>
```

The app also starts Spring Boot's embedded web server on port 8080 (no REST endpoints yet). Because that server keeps the process running, stop the app with `Ctrl+C` after typing `exit` or after a headless run finishes.

`res/Assignment_6.jar` is a prebuilt desktop version without the AI layer. It supports the same three modes, for example `java -jar res/Assignment_6.jar --mode interactive`.

## Using the GUI

- **New Calendar** and **Edit Calendar** create a calendar or change its name or time zone.
- The drop-down at the top left switches calendars. The current calendar and time zone are shown at the bottom.
- Click a date to see its events. Right-click a date to create or edit an event.
- For recurring events, the edit dialog has an **Edit all occurrences** checkbox.
- **Export** and **Import** write and read CSV files.

See [USEME.md](USEME.md) for a step-by-step walkthrough.

## Command reference

Commands work in interactive and headless mode. Select a calendar with `use calendar` before working with events.

Date-times use `yyyy-MM-ddTHH:mm` (for example `2025-04-29T10:00`). Dates use `yyyy-MM-dd`.

**Calendars**

```
create calendar --name <name> --timezone <Area/City>
edit calendar --name <name> --property name <newName>
edit calendar --name <name> --property timezone <Area/City>
use calendar --name <name>
```

**Create events**

```
create event <subject> from <dateTime> to <dateTime>
create event <subject> from <dateTime> to <dateTime> repeats <days> for <N> times
create event <subject> from <dateTime> to <dateTime> repeats <days> until <dateTime>
create event <subject> on <dateTime>                              # all-day
create event <subject> on <date> repeats <days> for <N> times     # recurring all-day
create event <subject> on <date> repeats <days> until <date>      # recurring all-day
```

Any create command can end with `location <text>`, `description <text>` and `type public|private`.

`<days>` is a string of weekday letters, such as `MWF`:

| M | T | W | R | F | S | U |
|---|---|---|---|---|---|---|
| Mon | Tue | Wed | Thu | Fri | Sat | Sun |

**Edit events**

`<property>` is one of `subject`, `start`, `end`, `location` or `description`.

```
edit event <property> <subject> from <dateTime> to <dateTime> with <newValue>   # one event
edit events <property> <subject> from <dateTime> with <newValue>                # events starting at or after <dateTime>
edit events <property> <subject> `<newValue>`                                   # all events with this name
```

In the last form, wrap the new value in backticks so it isn't confused with the event name:

```
edit events subject Team Sync `Weekly Sync`
```

**Copy events**

```
copy event <subject> on <dateTime> --target <calendar> to <dateTime>
copy events on <date> --target <calendar> to <date>
copy events between <date> and <date> --target <calendar> to <date>
```

**View, export and import**

```
print events on <date>
print events from <dateTime> to <dateTime>
show status on <dateTime>       # checks whether an event is scheduled at that time
export cal <file.csv>
import <file.csv>
exit
```

Full example sessions are in `res_A5/valid_command_assignment5.txt` and `res_A5/valid_commands_assignment4.txt`. The `invalid_*` files in the same folder show how errors are handled.

## AI features

`startapp.ai.AiIntegrationService` holds the AI logic:

- `summarizeEvent(text)` asks the chat model for a one-sentence summary.
- `indexEvents(events)` embeds each event's subject, description and location. Each embedding is a blocking HTTP call to OpenAI, so the calls run in parallel on virtual threads (`Executors.newVirtualThreadPerTaskExecutor()`).
- `findSimilar(query, k)` embeds the query and returns the top k events ranked by cosine similarity.

The vector index is kept in memory and rebuilt on each run.

### Setup

```bash
export OPENAI_API_KEY=sk-...
```

`src/main/resources/application.yml` reads the key and sets the connect and read timeouts.

### Semantic search demo

Indexes a sample set of events, runs a few queries and prints the ranked matches:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=ai-demo
```

### Retrieval evaluation

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=ai-eval
```

This scores `findSimilar` against the hand-labeled queries in `src/main/resources/event-search-ground-truth.json` and prints precision@3, recall@3, MRR and nDCG@3, overall and per query. Event IDs in that file have the form `subject@startDateTime`.

The metric logic lives in `startapp.ai.eval.RetrievalMetrics` and is covered by `RetrievalMetricsTest`, which runs under `mvn test` with no API calls. The demo and evaluation runners do call OpenAI, so they are kept out of the unit tests and never make paid calls in CI.

`CalendarApp` also reads the program arguments, so `ai-demo` and `ai-eval` print a harmless `Usage: ...` line before the AI output.

## Project structure

```
src/main/java
├── startapp/        entry point (CalendarApp) and mode routing
│   └── ai/          Spring AI service, demo and evaluation runners
│       └── eval/    retrieval metrics and evaluator
├── model/           calendars, events, recurring events, storage, copying
├── controller/      command parsers and CSV import
│   ├── command/     one class per command (Command pattern)
│   └── gui/         bridge between the GUI and the controller
└── view/
    ├── gui/         Swing frame, components and dialogs
    └── text/        console printer and CSV exporter
```

`res/` and `res_A5/` hold sample command files, CSV files, screenshots and prebuilt jars.

## Design

- **MVC:** the model holds calendars and events, the controller parses and runs commands, and the views (Swing GUI or console) show the results. The same controller serves all three modes.
- **Command pattern:** each command (`create`, `edit`, `copy`, `print`, ...) is its own class in `controller.command`, so adding a command doesn't change existing ones.
- **Time zones:** events are converted with `ZonedDateTime.withZoneSameInstant`, so they keep the same instant in time and daylight saving time is handled automatically.
- **Generic model interface:** `CalendarOperations<E extends Event>` keeps the calendar type-safe and open to new event types.

## Testing

```bash
mvn test
```

JUnit tests cover the model, every command parser and command, CSV import and export, the GUI controller bridge, GUI components and dialogs, and the retrieval metrics. The GUI tests check controller logic and the commands the GUI produces, not the visual rendering. Some GUI tests may open small confirmation dialogs; click **OK** to continue.

## Limitations and next steps

- Events are kept in memory. Export to CSV to keep them between runs.
- The AI demo and evaluation use a fixed sample set of events, and the vector index isn't saved. A persistent store (Spring AI's `SimpleVectorStore` or pgvector) is a natural next step.
- The GUI receives the AI service but doesn't offer AI actions yet.
- For large event sets, cap concurrent embedding calls (for example with a `Semaphore`) to stay under OpenAI rate limits.
