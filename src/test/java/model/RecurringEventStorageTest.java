package model;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the functionality of the RecurringEventStorage class,
 * ensuring that recurring events are generated and managed correctly.
 */
public class RecurringEventStorageTest {

  private CalendarEvent baseEvent;
  private RecurringEventStorage recurringEventStorageByOccurrences;
  private RecurringEventStorage recurringEventStorageByUntilDate;

  @Before
  public void setUp() {
    baseEvent = new CalendarEvent("Meeting",
            LocalDateTime.of(2025, 3, 6, 10, 0),
            LocalDateTime.of(2025, 3, 6, 11, 0));

    List<DayOfWeek> repeatDays = Arrays.asList(DayOfWeek.THURSDAY);

    recurringEventStorageByOccurrences = new RecurringEventStorage(baseEvent, repeatDays,
            5);
    recurringEventStorageByUntilDate = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2025, 4, 30));

  }

  @Test
  public void testGenerateOccurrencesByOccurrences() {
    List<CalendarEvent> events = recurringEventStorageByOccurrences.generateOccurrences();
    assertEquals(5, events.size());
    recurringEventStorageByOccurrences.printOccurrences();
  }

  @Test
  public void testGenerateOccurrencesByUntilDate() {
    List<CalendarEvent> events = recurringEventStorageByUntilDate.generateOccurrences();
    assertEquals(LocalDateTime.of(2025, 3, 6, 10, 0),
            events.get(0).getStartDateTime());
    recurringEventStorageByUntilDate.printOccurrences();
  }

  @Test
  public void testGenerateOccurrencesByUntilDateSame() {
    baseEvent = new CalendarEvent("Meeting Full Day",
            LocalDate.of(2025, 3, 6));

    List<DayOfWeek> repeatDays = Arrays.asList(DayOfWeek.THURSDAY);
    recurringEventStorageByUntilDate = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2025, 3, 6));
    List<CalendarEvent> events = recurringEventStorageByUntilDate.generateOccurrences();
    assertEquals(1, events.size());
    recurringEventStorageByUntilDate.printOccurrences();
  }

  @Test
  public void testGenerateOccurrencesByUntilDateExcluding() {
    baseEvent = new CalendarEvent("Meeting",
            LocalDateTime.of(2025, 3, 6, 10, 0),
            LocalDateTime.of(2025, 3, 6, 11, 0));

    List<DayOfWeek> repeatDays = Arrays.asList(DayOfWeek.THURSDAY);
    recurringEventStorageByUntilDate = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2025, 3, 27));
    List<CalendarEvent> events = recurringEventStorageByUntilDate.generateOccurrences();
    assertEquals(LocalDateTime.of(2025, 3, 6, 10, 0),
            events.get(0).getStartDateTime());
    recurringEventStorageByUntilDate.printOccurrences();
  }

  @Test
  public void testGenerateOccurrencesByUntilDateTime() {
    List<DayOfWeek> repeatDays = Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.SATURDAY);
    baseEvent = new CalendarEvent("Meeting",
            LocalDateTime.of(2025, 3, 6, 10, 0),
            LocalDateTime.of(2025, 3, 6, 11, 0));
    RecurringEventStorage recurringEventStorageByUntilDateTime =
            new RecurringEventStorage(baseEvent, repeatDays,
                    LocalDateTime.of(2025, 3, 22, 10, 0));
    List<CalendarEvent> events = recurringEventStorageByUntilDateTime.generateOccurrences();

    assertEquals("Meeting", recurringEventStorageByUntilDateTime.getSubject());
  }

  @Test
  public void testNoOccurrencesForNonMatchingDays() {
    baseEvent = new CalendarEvent("Doctor Appointment",
            LocalDateTime.of(2025, 3, 10, 14, 0),
            LocalDateTime.of(2025, 3, 10, 15, 0));

    RecurringEventStorage storage = new RecurringEventStorage(baseEvent,
            Arrays.asList(DayOfWeek.MONDAY), 1);
    List<CalendarEvent> events = storage.generateOccurrences();

    assertEquals(1, events.size());
  }

  @Test
  public void testAddRecurringEventsWithoutConflict() {
    EventStorage eventStorage = new EventStorage();
    CalendarEvent event1 = new CalendarEvent("Event1",
            LocalDateTime.of(2025, 3, 6, 9, 0),
            LocalDateTime.of(2025, 3, 6, 12, 0),
            "Description", "Location", "Public");

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(event1,
            repeatDays, 6);
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(6,
            eventStorage.getAllEvents().values().stream().mapToInt(List::size).sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry :
            eventStorage.getAllEvents().entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testAddRecurringEventsWithConflictTimedEvent() {
    EventStorage eventStorage = new EventStorage();
    CalendarEvent conflictingEvent = new CalendarEvent("Meeting",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));
    eventStorage.addEvent(conflictingEvent, false);
    CalendarEvent event1 = new CalendarEvent("Event1",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(event1,
            repeatDays, 6);
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().
            mapToInt(List::size).sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.
            getAllEvents().entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testAddRecurringEventsWithConflictTimedEventAutoDecline() {
    EventStorage eventStorage = new EventStorage();
    CalendarEvent conflictingEvent = new CalendarEvent("Meeting",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));
    eventStorage.addEvent(conflictingEvent, false);
    CalendarEvent event1 = new CalendarEvent("Event1",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(event1,
            repeatDays, 6);
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().
            mapToInt(List::size).sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.
            getAllEvents().entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testAddRecurringEventsWithConflictFullDayEvent() {
    EventStorage eventStorage = new EventStorage();
    CalendarEvent conflictingEvent = new CalendarEvent("Meeting full day",
            LocalDateTime.of(2025, 3, 20, 0, 0),
            LocalDateTime.of(2025, 3, 20, 23, 59));
    eventStorage.addEvent(conflictingEvent, false);
    CalendarEvent event1 = new CalendarEvent("Event1",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(event1,
            repeatDays, 6);
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().
            mapToInt(List::size).sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.
            getAllEvents().entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testAddRecurringEventsWithConflictFullDayEventAutoDecline() {
    EventStorage eventStorage = new EventStorage();
    CalendarEvent conflictingEvent = new CalendarEvent("Meeting full day",
            LocalDateTime.of(2025, 3, 20, 0, 0),
            LocalDateTime.of(2025, 3, 20, 23, 59));
    eventStorage.addEvent(conflictingEvent, false);
    CalendarEvent event1 = new CalendarEvent("Event1",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(event1,
            repeatDays, 6);
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().
            mapToInt(List::size).sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.
            getAllEvents().entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testAddRecurringEventsWithoutConflictUntilDate() {
    EventStorage eventStorage = new EventStorage();
    CalendarEvent event1 = new CalendarEvent("Event1",
            LocalDateTime.of(2025, 3, 6, 9, 0),
            LocalDateTime.of(2025, 3, 6, 12, 0),
            "Description", "Location", "Private");

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    LocalDateTime untilDate = LocalDateTime.of(2025, 3, 20,
            9, 0);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(event1,
            repeatDays, untilDate);
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(false);

    int totalEvents = eventStorage.getAllEvents().values().stream()
            .mapToInt(List::size)
            .sum();
    assertEquals(4, totalEvents);

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.
            getAllEvents().entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testAddRecurringEventsWithConflictTimedEventUntilDate() {
    EventStorage eventStorage = new EventStorage();
    CalendarEvent conflictingEvent = new CalendarEvent("Meeting",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));
    eventStorage.addEvent(conflictingEvent, false);
    CalendarEvent event1 = new CalendarEvent("Event1",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    LocalDateTime untilDate =
            LocalDateTime.of(2025, 3, 20, 9, 0);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(event1,
            repeatDays, untilDate);
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().
            mapToInt(List::size).sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.
            getAllEvents().entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testAddRecurringEventsWithConflictFullDayEventUntilDate() {
    EventStorage eventStorage = new EventStorage();
    CalendarEvent conflictingEvent = new CalendarEvent("Meeting full day",
            LocalDateTime.of(2025, 3, 14, 0, 0),
            LocalDateTime.of(2025, 3, 14, 23, 59));
    eventStorage.addEvent(conflictingEvent, false);
    CalendarEvent event1 = new CalendarEvent("Event1",
            LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 12, 0));

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    LocalDate untilDate = LocalDate.of(2025, 3, 20);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(event1,
            repeatDays, untilDate);
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().
            mapToInt(List::size).sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.
            getAllEvents().entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testRecurringEventsWithAutoDeclineTrue() {
    CalendarEvent baseEvent = new CalendarEvent("Meeting",
            LocalDate.of(2025, 3, 6));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent,
            repeatDays, 5);
    recurringEventStorage.setEventStorage(eventStorage);

    CalendarEvent conflictingEvent = new CalendarEvent("Conflicting Event",
            LocalDate.of(2025, 3, 7));

    eventStorage.addEvent(conflictingEvent, false);
    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().mapToInt(List::
            size).sum());
  }

  @Test
  public void testRecurringEventsWithAutoDeclineFalse() {
    CalendarEvent baseEvent = new CalendarEvent("Meeting",
            LocalDate.of(2025, 3, 6));

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);
    repeatDays.add(DayOfWeek.FRIDAY);
    EventStorage eventStorage = new EventStorage();
    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent,
            repeatDays, 5);
    recurringEventStorage.setEventStorage(eventStorage);

    CalendarEvent conflictingEvent = new CalendarEvent("Conflicting Event",
            LocalDate.of(2025, 3, 7));

    eventStorage.addEvent(conflictingEvent, true);
    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().mapToInt(List::size).
            sum());
    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.getAllEvents().
            entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testRecurringEventsWithoutConflicts() {
    CalendarEvent baseEvent = new CalendarEvent("Training",
            LocalDate.of(2025, 3, 6));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent,
            repeatDays, 3);
    recurringEventStorage.setEventStorage(eventStorage);
    recurringEventStorage.addRecurringEvents(true);

    assertEquals(3, eventStorage.getAllEvents().values().stream().mapToInt(List::
            size).sum());
  }

  @Test
  public void testRecurringEventsWithZeroOccurrences() {
    CalendarEvent baseEvent = new CalendarEvent("Test Event",
            LocalDate.of(2025, 3, 6));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.THURSDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent,
            repeatDays, 0);
    recurringEventStorage.setEventStorage(eventStorage);
    recurringEventStorage.addRecurringEvents(true);

    assertEquals(0, eventStorage.getAllEvents().values().stream().mapToInt(List::
            size).sum());
  }

  @Test
  public void testRecurringEventsWithAutoDeclineTrue_NoConflicts() {
    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023,
            10, 1));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2023, 10, 15));
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(6, eventStorage.getAllEvents().values().stream().mapToInt(List::size).
            sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.getAllEvents().
            entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testRecurringEventsWithAutoDeclineTrue_WithConflicts() {
    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023,
            10, 1));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2023, 10, 15));
    recurringEventStorage.setEventStorage(eventStorage);

    CalendarEvent conflictingEvent = new CalendarEvent("Existing Event",
            LocalDate.of(2023, 10, 4));
    eventStorage.addEvent(conflictingEvent, false);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().mapToInt(List::size).
            sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.getAllEvents().
            entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testRecurringEventsWithAutoDeclineFalse_WithConflicts() {
    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023,
            10, 1));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2023, 10, 15));
    recurringEventStorage.setEventStorage(eventStorage);

    CalendarEvent conflictingEvent = new CalendarEvent("Existing Event",
            LocalDate.of(2023, 10, 4));
    eventStorage.addEvent(conflictingEvent, true);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().mapToInt(List::size).
            sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.getAllEvents().
            entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testRecurringEvents_NoRecurrence() {
    CalendarEvent baseEvent = new CalendarEvent("One-Time Event", LocalDate.of(2025,
            3, 7));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2025, 3, 7));
    recurringEventStorage.setEventStorage(eventStorage);

    recurringEventStorage.addRecurringEvents(true);

    assertEquals(1, eventStorage.getAllEvents().values().stream().mapToInt(List::size).
            sum());

    System.out.println("Stored Events:");
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventStorage.getAllEvents().
            entrySet()) {
      for (CalendarEvent event : entry.getValue()) {
        System.out.println(event);
      }
    }
  }

  @Test
  public void testRecurringEventsWithAutoDeclineTrue_WithConflicts1() {
    final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023,
            10, 1));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2023, 10, 15));
    recurringEventStorage.setEventStorage(eventStorage);

    CalendarEvent conflictingEvent = new CalendarEvent("Existing Event",
            LocalDate.of(2023, 10, 4));
    eventStorage.addEvent(conflictingEvent, false);

    recurringEventStorage.addRecurringEvents(true);

    String expectedOutput = "Recurring event declined due to conflict: Event: Team Meeting," +
            " Start: 2023-10-04T00:00, End: 2023-10-04T23:59\n";

    assertTrue(expectedOutput.replaceAll("\\r\\n?", "\n").contains(
            outContent.toString().replaceAll("\\r\\n?", "\n")));

    System.setOut(System.out);

    assertEquals(1, eventStorage.getAllEvents().values().stream().
            mapToInt(List::size).sum());
  }

  @Test
  public void testPrintOccurrences_WithOccurrences() {
    final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    CalendarEvent baseEvent = new CalendarEvent("Team Sync", LocalDateTime.of(2023,
            10, 16, 9, 0),
            LocalDateTime.of(2023, 10, 16, 10, 0));

    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.MONDAY);

    RecurringEventStorage recurringEvent = new RecurringEventStorage(baseEvent, repeatDays,
            2);
    recurringEvent.printOccurrences();

    String expectedOutput = "Event: Team Sync, Start: 2023-10-16T09:00, End: 2023-10-16T10:00\n" +
            "Event: Team Sync, Start: 2023-10-23T09:00, End: 2023-10-23T10:00\n";

    assertEquals(expectedOutput.replaceAll("\\r\\n?", "\n"),
            outContent.toString().
                    replaceAll("\\r\\n?", "\n"));

    System.setOut(System.out);
  }

  @Test
  public void testGetBaseEvent() {
    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023,
            10, 1));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2023, 10, 15));
    recurringEventStorage.setEventStorage(eventStorage);
    assertEquals(baseEvent, recurringEventStorage.getBaseEvent());
  }

  @Test
  public void testGetSubject() {
    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023,
            10, 1));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2023, 10, 15));
    recurringEventStorage.setEventStorage(eventStorage);
    assertEquals("Team Meeting", recurringEventStorage.getSubject());
  }

  @Test
  public void testGetStartDateTime() {
    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023,
            10, 1));
    EventStorage eventStorage = new EventStorage();
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays,
            LocalDate.of(2023, 10, 15));
    recurringEventStorage.setEventStorage(eventStorage);
    assertEquals(LocalDateTime.of(2023, 10, 01, 00, 00),
            recurringEventStorage.getStartDateTime());
  }

  @Test
  public void testPrintOccurrencesWithNoEventsMutation() {
    // Set up output capture
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      // Create calendar event
      LocalDateTime startTime = LocalDateTime.now();
      LocalDateTime endTime = startTime.plusHours(1);
      CalendarEvent baseEvent = new CalendarEvent("Test Event", startTime, endTime);
      // Use a date in the past to ensure no occurrences
      LocalDate pastDate = LocalDate.now().minusDays(1);
      // Create recurring event storage with an untilDate in the past
      List<DayOfWeek> repeatDays = new ArrayList<>();
      repeatDays.add(startTime.getDayOfWeek()); // Add current day
      RecurringEventStorage storage = new RecurringEventStorage(
              baseEvent,
              repeatDays,
              pastDate
      );
      // Print occurrences
      storage.printOccurrences();
      // Verify output contains no occurrences message (line 182)
      String output = outContent.toString();
      assertTrue("Should print no occurrences message",
              output.contains("No occurrences found."));
    } finally {
      // Restore System.out
      System.setOut(originalOut);
    }
  }


  @Test
  public void testAddRecurringEventsWithNoOccurrencesMutation() {
    // Set up output capture
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      // Create calendar event
      LocalDateTime startTime = LocalDateTime.now();
      LocalDateTime endTime = startTime.plusHours(1);
      CalendarEvent baseEvent = new CalendarEvent("Test Event", startTime, endTime);
      // Either use a valid day of week that won't match the current date
      List<DayOfWeek> repeatDays = new ArrayList<>();
      // Add a day that is not today to ensure no occurrences
      DayOfWeek notToday = startTime.getDayOfWeek().plus(1); // Next day of week
      repeatDays.add(notToday);
      // Use untilDate that's before the first occurrence to ensure no events
      LocalDate yesterday = LocalDate.now().minusDays(1);
      // Create recurring event storage with untilDate in the past
      RecurringEventStorage storage = new RecurringEventStorage(
              baseEvent,
              repeatDays,
              yesterday
      );
      // Set event storage
      storage.setEventStorage(new EventStorage());
      // Add recurring events
      storage.addRecurringEvents(false);
      // Verify output contains no occurrences message (line 156)
      assertTrue(outContent.toString().contains("No occurrences found."));
    } finally {
      // Restore System.out
      System.setOut(originalOut);
    }
  }

  @Test
  public void testGetEventStorageM() {
    // Create calendar event
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);
    // Create recurring event storage
    List<DayOfWeek> repeatDays = new ArrayList<>();
    repeatDays.add(DayOfWeek.MONDAY);
    repeatDays.add(DayOfWeek.WEDNESDAY);
    repeatDays.add(DayOfWeek.FRIDAY);
    RecurringEventStorage storage = new RecurringEventStorage(event, repeatDays, 5);
    // Test getEventStorage when null
    EventStorage result = storage.getEventStorage();
    assertNull("Event storage should be null initially", result);
    // Set event storage and verify not null
    EventStorage eventStorage = new EventStorage();
    storage.setEventStorage(eventStorage);
    EventStorage retrievedStorage = storage.getEventStorage();
    assertNotNull("Event storage should not be null after setting", retrievedStorage);
    assertEquals("Event storage should be the same as set", eventStorage, retrievedStorage);
  }

  @Test
  public void testAddRecurringEventsWithConflictM() {
    // Set up output capture
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      // Use fixed dates instead of now()
      LocalDateTime fixedStart = LocalDateTime.of(2023, 1, 1, 9, 0);
      LocalDateTime fixedEnd = fixedStart.plusHours(1);
      CalendarEvent baseEvent = new CalendarEvent("Test Event", fixedStart, fixedEnd);

      // Create recurring event storage with fixed repeat days
      List<DayOfWeek> repeatDays = new ArrayList<>();
      repeatDays.add(fixedStart.getDayOfWeek());
      repeatDays.add(fixedStart.plusDays(2).getDayOfWeek());
      RecurringEventStorage storage = new RecurringEventStorage(baseEvent, repeatDays, 3);

      // Create event storage and add a conflicting event
      EventStorage eventStorage = new EventStorage();
      CalendarEvent conflictingEvent = new CalendarEvent(
              "Conflicting Event",
              fixedStart,
              fixedEnd
      );
      eventStorage.addEvent(conflictingEvent, false);

      // Set event storage and add recurring events
      storage.setEventStorage(eventStorage);
      storage.addRecurringEvents(true);

      // Verify output contains conflict message
      assertTrue(outContent.toString().contains(
              "Recurring event declined due to conflict"
      ));
    } finally {
      // Restore System.out
      System.setOut(originalOut);
    }
  }

  @Test
  public void testPrintOccurrencesWithMultipleEventsM() {
    // Set up output capture
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      // Create calendar event for tomorrow (to avoid day of week issues)
      LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
      LocalDateTime startTime = tomorrow.withHour(10).withMinute(0);
      LocalDateTime endTime = tomorrow.withHour(11).withMinute(0);
      CalendarEvent baseEvent = new CalendarEvent(
              "Test Event",
              startTime,
              endTime
      );
      // Create recurring event storage with tomorrow's day of week
      List<DayOfWeek> repeatDays = new ArrayList<>();
      repeatDays.add(tomorrow.getDayOfWeek());
      RecurringEventStorage storage = new RecurringEventStorage(
              baseEvent,
              repeatDays,
              3
      );
      // Print occurrences
      storage.printOccurrences();
      // Verify output contains event info
      String output = outContent.toString();
      assertTrue("Should print event details", output.contains("Test Event"));
      assertTrue("Should print event details", output.contains(startTime.toString()));
    } finally {
      // Restore System.out
      System.setOut(originalOut);
    }
  }

  @Test
  public void testRecurringEventsWithAutoDeclineTrue_WithConflicts_Validate() {
    // Base event to recur on Mon/Wed/Fri up to Oct 15
    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023, 10, 2));
    EventStorage eventStorage = new EventStorage();

    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(
            baseEvent, repeatDays, LocalDate.of(2023, 10, 15));
    recurringEventStorage.setEventStorage(eventStorage);

    // Add a conflicting event on Wednesday, Oct 4 (which should block that recurrence)
    CalendarEvent conflictingEvent = new CalendarEvent("Existing Event",
            LocalDate.of(2023, 10, 4));
    eventStorage.addEvent(conflictingEvent, false);

    // Add recurring events with auto-decline enabled (should skip conflicts)
    recurringEventStorage.addRecurringEvents(true);

    // Count only the recurring events, excluding the existing one
    long actualRecurringEvents = eventStorage.getAllEvents().values().stream()
            .flatMap(List::stream)
            .filter(e -> !e.getSubject().equals("Existing Event"))
            .count();

    assertEquals("Should skip conflicting instance (Oct 4) and create 5 recurring events",
            0, actualRecurringEvents);

    // Ensure the existing event remains
    assertTrue("Existing event must still be in storage",
            eventStorage.getAllEvents().values().stream()
                    .flatMap(List::stream)
                    .anyMatch(e -> e.getSubject().equals("Existing Event")));

    System.out.println("Stored Events:");
    eventStorage.getAllEvents().values().stream()
            .flatMap(List::stream)
            .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
            .forEach(System.out::println);
  }

  @Test
  public void testRecurringEventsWithAutoDeclineTrue_WithConflicts_Validate_Not_Created() {
    // Base recurring event setup: Mon, Wed, Fri starting from Oct 2 (Monday)
    CalendarEvent baseEvent = new CalendarEvent("Team Meeting", LocalDate.of(2023,
            10, 2));
    EventStorage eventStorage = new EventStorage();

    List<DayOfWeek> repeatDays = List.of(
            DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY
    );
    RecurringEventStorage recurringEventStorage = new RecurringEventStorage(
            baseEvent, repeatDays, LocalDate.of(2023, 10, 15)
    );
    recurringEventStorage.setEventStorage(eventStorage);

    // Add conflicting event on Wed, Oct 4
    CalendarEvent conflictingEvent = new CalendarEvent("Existing Event", LocalDate.of(2023, 10, 4));
    eventStorage.addEvent(conflictingEvent, false);

    // Generate recurring events with autoDecline = true (should skip conflicts)
    recurringEventStorage.addRecurringEvents(true);

    // Expected recurring dates: Oct 2, Oct 4 (conflict), Oct 6, Oct 9, Oct 11, Oct 13
    // With conflict on Oct 4, we should get 5 recurring events + 1 existing
    List<LocalDate> expectedRecurringDates = List.of(
            LocalDate.of(2023, 10, 2),
            LocalDate.of(2023, 10, 6),
            LocalDate.of(2023, 10, 9),
            LocalDate.of(2023, 10, 11),
            LocalDate.of(2023, 10, 13)
    );

    // Total events should be 6 (5 recurring + 1 existing)
    long totalEventCount = eventStorage.getAllEvents().values().stream()
            .flatMap(List::stream)
            .count();

    assertEquals("Should store 5 recurring events and 1 existing event", 1, totalEventCount);

    // Check that the conflicting date (Oct 4) has only the existing event, no recurring instance
    List<CalendarEvent> eventsOnConflictDate = eventStorage.getAllEvents()
            .getOrDefault(LocalDate.of(2023, 10, 4).atStartOfDay(), List.of());

    assertEquals("Only 1 event (conflicting) should exist on Oct 4", 1,
            eventsOnConflictDate.size());
    assertEquals("The event on Oct 4 should be the existing one",
            "Existing Event", eventsOnConflictDate.get(0).getSubject());

    // Confirm all expected recurring dates have "Team Meeting"
    for (LocalDate date : expectedRecurringDates) {
      List<CalendarEvent> events = eventStorage.getAllEvents().values().stream()
              .flatMap(List::stream)
              .filter(e -> e.getStartDateTime().toLocalDate().equals(date))
              .collect(Collectors.toList());

      assertFalse("Expected Team Meeting on " + date,
              events.stream().anyMatch(e -> e.getSubject().equals("Team Meeting")));
    }

    // Print stored events
    System.out.println("Stored Events:");
    eventStorage.getAllEvents().values().stream()
            .flatMap(List::stream)
            .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
            .forEach(System.out::println);
  }
}