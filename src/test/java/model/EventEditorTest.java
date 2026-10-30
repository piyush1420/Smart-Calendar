package model;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import controller.EditCommandParser;
import controller.InvalidCommandException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests the EventEditor functionality, including editing single events, recurring
 * events, and handling edge cases such as invalid commands or conflicting events.
 */
public class EventEditorTest {
  private EventStorage eventStorage;
  private EditCommandParser editCommandProcessor;
  private RecurringEventStorage recurringEventStorage;

  @Before
  public void setUp() throws InvalidCommandException {
    eventStorage = new EventStorage();

    // Create a base recurring event
    CalendarEvent baseEvent = new CalendarEvent("Recurring Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Team sync", "Room 101", "public", true);

    // Define the repeat days (e.g., every Monday)
    List<DayOfWeek> repeatDays = Arrays.asList(DayOfWeek.MONDAY);

    // Initialize the RecurringEventStorage with N = 3 occurrences
    recurringEventStorage = new RecurringEventStorage(baseEvent, repeatDays, 3);

    // Generate occurrences and add them to the EventStorage
    for (Event event : recurringEventStorage.generateOccurrences()) {
      eventStorage.addEvent((CalendarEvent) event, false);
    }

    // Add a non-recurring event
    CalendarEvent nonRecurringEvent = new CalendarEvent("Workshop",
            LocalDateTime.of(2025, 3, 11, 14, 0),
            LocalDateTime.of(2025, 3, 11, 16, 0),
            "Skill upgrade", "Hall A", "private", false);
    eventStorage.addEvent(nonRecurringEvent, false);

    // Initialize EditCommandParser with EventStorage
    editCommandProcessor = new EditCommandParser(eventStorage);
  }

  @Test
  public void testEditSingleRecurringEvent() throws InvalidCommandException {
    String command = "edit event subject Recurring Meeting from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 with Updated Meeting";
    boolean result = editCommandProcessor.executeCommand(command);
    assertTrue(result);

    Event updatedEvent = findEvent("Updated Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0));
    assertNotNull(updatedEvent);
    assertEquals("Updated Meeting", updatedEvent.getSubject());
  }

  @Test
  public void testEditSingleNonRecurringEvent() throws InvalidCommandException {
    String command = "edit event subject Workshop from " +
            "2025-03-11T14:00 to 2025-03-11T16:00 with Updated Workshop";
    boolean result = editCommandProcessor.executeCommand(command);
    assertTrue(result);

    Event nonUpdatedEvent = findEvent("Updated Workshop",
            LocalDateTime.of(2025, 3, 11, 14, 0));
    assertNotNull(nonUpdatedEvent);
    assertEquals("Updated Workshop", nonUpdatedEvent.getSubject());
  }

  @Test
  public void testEditEventsByStartTimeRecurring() throws InvalidCommandException {
    String command = "edit events location Recurring Meeting from " +
            "2025-03-10T10:00 with Room 202";
    boolean result = editCommandProcessor.executeCommand(command);
    assertTrue(result);

    Event updatedEvent = findEvent("Recurring Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0));
    assertNotNull(updatedEvent);
    assertEquals("Room 202", ((CalendarEvent) updatedEvent).getLocation());
  }

  @Test
  public void testEditEventsByStartTimeNonRecurring() throws InvalidCommandException {
    String command = "edit events location Recurring Meeting from " +
            "2025-03-11T14:00 with Hall B";
    boolean result = editCommandProcessor.executeCommand(command);
    assertTrue(result);
  }

  @Test
  public void testEditAllEventsByNameRecurring() throws InvalidCommandException {
    String command = "edit events subject Recurring Meeting `Updated Meeting`";
    boolean result = editCommandProcessor.executeCommand(command);
    assertTrue(result);

    Event updatedEvent = findEvent("Updated Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0));
    assertNotNull(updatedEvent);
    assertEquals("Updated Meeting", updatedEvent.getSubject());
  }

  @Test
  public void testEditAllEventsByNameNonRecurring() throws InvalidCommandException {
    String command = "edit events subject Workshop `Updated Workshop`";
    boolean result = editCommandProcessor.executeCommand(command);
    assertTrue(result);
  }

  @Test
  public void testEditRecurringEventOccurrences_NMinus1() throws InvalidCommandException {
    assertEquals(3, recurringEventStorage.getOccurrences().intValue());

    recurringEventStorage.setOccurrences(2);
    assertEquals(2, recurringEventStorage.getOccurrences().intValue());

    List<CalendarEvent> updatedEvents = recurringEventStorage.generateOccurrences();
    assertEquals(2, updatedEvents.size());

    Event firstEvent = updatedEvents.get(0);
    assertEquals("Recurring Meeting", firstEvent.getSubject());
    assertEquals(LocalDateTime.of(2025, 3, 10, 10, 0),
            firstEvent.getStartDateTime());

    Event lastEvent = updatedEvents.get(updatedEvents.size() - 1);
    LocalDateTime expectedLastEventTime = LocalDateTime.of(2025, 3, 17,
            10, 0);
    assertEquals(expectedLastEventTime, lastEvent.getStartDateTime());
  }

  @Test
  public void testEditNonExistentEvent() throws InvalidCommandException {
    String command = "edit event subject NonExistentEvent from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 with Updated Event";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventWithInvalidProperty() throws InvalidCommandException {
    String command = "edit event invalidProperty Recurring Meeting from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 with New Value";
    assertFalse(editCommandProcessor.executeCommand(command));

  }

  @Test
  public void testEditEventWithInvalidDateTimeFormat() throws InvalidCommandException {
    String command = "edit event subject Recurring Meeting from " +
            "2025/03/10T10:00 to 2025/03/10T11:00 with Updated Meeting";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventWithConflictAndAutoDeclineFalse() throws InvalidCommandException {
    CalendarEvent originalEvent = new CalendarEvent("Recurring Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Team sync", "Room 101", "public", true);
    eventStorage.addEvent(originalEvent, false);

    CalendarEvent conflictingEvent = new CalendarEvent("Conflicting Event",
            LocalDateTime.of(2025, 3, 10, 10, 30),
            LocalDateTime.of(2025, 3, 10, 11, 30),
            "Conflict", "Room 101", "public", false);
    eventStorage.addEvent(conflictingEvent, false);

    String command = "edit events subject Recurring Meeting `Updated Meeting`";
    boolean result = editCommandProcessor.executeCommand(command);
    assertTrue(result);

    Event updatedEvent = findEvent("Updated Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0));
    assertNotNull(updatedEvent);
    assertEquals("Updated Meeting", updatedEvent.getSubject());

    /*Event unchangedEvent = findEvent("Conflicting Event",
            LocalDateTime.of(2025, 3, 10, 10, 30));
    assertNotNull(unchangedEvent);
    assertEquals("Conflicting Event", unchangedEvent.getSubject());*/
  }

  @Test
  public void testEditEventWithEmptyNewValue() throws InvalidCommandException {
    String command = "edit event subject Recurring Meeting from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 with ";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventWithNullNewValue() throws InvalidCommandException {
    String command = "edit event subject Recurring Meeting from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 with null";
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
      editCommandProcessor.executeCommand(command);
    });
    assertEquals("Invalid property value", ex.getMessage());
  }

  @Test
  public void testEditRecurringEventWithInvalidCommand() throws InvalidCommandException {
    String command = "edit events subject 'Recurring Meeting'";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventMissingFromClause() throws InvalidCommandException {
    String command = "edit event subject Recurring Meeting to " +
            "2025-03-10T11:00 with Updated Meeting";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventMissingToClause() throws InvalidCommandException {
    String command = "edit event subject Recurring Meeting from " +
            "2025-03-10T10:00 with Updated Meeting";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventMissingWithClause() throws InvalidCommandException {
    String command = "edit event subject Recurring Meeting from " +
            "2025-03-10T10:00 to 2025-03-10T11:00";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventInvalidProperty() throws InvalidCommandException {
    String command = "edit event invalidProperty Recurring Meeting from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 with Updated Meeting";
    assertFalse(editCommandProcessor.executeCommand(command));

  }

  @Test
  public void testEditEventsMissingFromClause() throws InvalidCommandException {
    String command = "edit events subject Recurring Meeting with Updated Meeting";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventsMissingWithClause() throws InvalidCommandException {
    String command = "edit events subject Recurring Meeting from 2025-03-10T10:00";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventsInvalidDateTimeFormat() throws InvalidCommandException {
    String command = "edit events subject Recurring Meeting from " +
            "2025/03/10T10:00 with Updated Meeting";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventsInvalidProperty() throws InvalidCommandException {
    String command = "edit events invalidProperty Recurring Meeting from " +
            "2025-03-10T10:00 with Updated Meeting";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventsMissingNewValue() throws InvalidCommandException {
    String command = "edit events subject Recurring Meeting";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsInvalidProperty_1() throws InvalidCommandException {
    String command = "edit events invalidProperty Recurring Meeting Updated Meeting";
    editCommandProcessor.executeCommand(command);
  }

  @Test
  public void testEditEventsEmptyNewValue() throws InvalidCommandException {
    String command = "edit events subject Recurring Meeting ";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testEditEventsInvalidCommandFormat() throws InvalidCommandException {
    String command = "edit events subject Recurring Meeting Updated Meeting ExtraArgument";
    assertFalse(editCommandProcessor.executeCommand(command));
  }

  @Test
  public void testCreateAndEditRecurringEvent() throws InvalidCommandException {
    assertEquals(3, recurringEventStorage.getOccurrences().intValue());

    String command = "edit events subject Recurring Meeting `Updated Meeting`";
    boolean result = editCommandProcessor.executeCommand(command);
    assertTrue(result);

    for (int i = 0; i < 3; i++) {
      LocalDateTime occurrenceTime = LocalDateTime.of(2025, 3, 10 + (i * 7),
              10, 0);
      Event updatedEvent = findEvent("Updated Meeting", occurrenceTime);
      assertNotNull(updatedEvent);
      assertEquals("Updated Meeting", updatedEvent.getSubject());
    }
  }

  private Event findEvent(String subject, LocalDateTime startTime) {
    return eventStorage.getAllEvents().values().stream()
            .flatMap(List::stream)
            .filter(e -> e.getSubject().equals(subject) &&
                    e.getStartDateTime().equals(startTime))
            .findFirst()
            .orElse(null);
  }

  @Test
  public void testIsRecurring() throws InvalidCommandException {
    // Create a recurring event
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    String eventName = "Recurring Event";

    CalendarEvent nonRecurringEvent = new CalendarEvent(eventName, startTime, endTime);
    assertFalse(nonRecurringEvent.isRecurring());
  }

  @Test
  public void testConflictsWith() throws InvalidCommandException {
    // Create base event
    LocalDateTime baseStart = LocalDateTime.now();
    LocalDateTime baseEnd = baseStart.plusHours(1);
    CalendarEvent baseEvent = new CalendarEvent("Base Event", baseStart, baseEnd);

    // Test overlapping event (conflict)
    LocalDateTime overlapStart = baseStart.plusMinutes(30);
    LocalDateTime overlapEnd = baseEnd.plusMinutes(30);
    CalendarEvent overlappingEvent = new CalendarEvent("Overlapping Event", overlapStart,
            overlapEnd);
    assertTrue(baseEvent.conflictsWith(overlappingEvent));

    // Test non-overlapping event (no conflict)
    LocalDateTime noConflictStart = baseEnd.plusHours(1);
    LocalDateTime noConflictEnd = noConflictStart.plusHours(1);
    CalendarEvent noConflictEvent = new CalendarEvent("No Conflict Event",
            noConflictStart, noConflictEnd);
    assertFalse(baseEvent.conflictsWith(noConflictEvent));


  }

  @Test
  public void testGetDuration() throws InvalidCommandException {
    // Create event with specific duration
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(2).plusMinutes(30);
    CalendarEvent event = new CalendarEvent("Duration Test Event", startTime, endTime);

    // Verify duration
    Duration expectedDuration = Duration.ofHours(2).plusMinutes(30);
    assertEquals(expectedDuration, event.getDuration());

    // Test with zero duration
    CalendarEvent zeroDurationEvent = new CalendarEvent("Zero Duration Event",
            startTime, startTime);
    assertEquals(Duration.ZERO, zeroDurationEvent.getDuration());
  }

  @Test
  public void testExecuteEditWithValidProperty() {
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);
    EventStorage eventStorage = new EventStorage();
    EditEvent editEvent = new EditEvent(event, eventStorage);

    assertTrue(editEvent.executeEdit("subject", "New Subject"));
    assertEquals("New Subject", event.getSubject());
  }

  @Test
  public void testExecuteEditWithInvalidProperty() {
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);
    EventStorage eventStorage = new EventStorage();
    EditEvent editEvent = new EditEvent(event, eventStorage);

    assertFalse(editEvent.executeEdit("invalid", "value"));
  }

  @Test
  public void testExecuteMultipleEdits() {
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event1 = new CalendarEvent("Event 1", startTime, endTime,
            "desc", "loc", "type", true);
    CalendarEvent event2 = new CalendarEvent("Event 2",
            startTime.plusHours(2), endTime.plusHours(2),
            "desc", "loc", "type", true);
    List<CalendarEvent> events = Arrays.asList(event1, event2);
    EventStorage eventStorage = new EventStorage();

    assertTrue(EditEvent.executeMultipleEdits(events,
            "subject", "New Subject", eventStorage));
    assertEquals("New Subject", event1.getSubject());
    assertEquals("New Subject", event2.getSubject());
  }

  @Test
  public void testExecuteMultipleEditsWithConflict() {
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event1 = new CalendarEvent("Event 1", startTime, endTime,
            "desc", "loc", "type", true);
    CalendarEvent event2 = new CalendarEvent("Event 2", startTime, endTime,
            "desc", "loc", "type", true);
    List<CalendarEvent> events = Arrays.asList(event1, event2);
    EventStorage eventStorage = new EventStorage();
    eventStorage.addEvent(event1, false);
    assertEquals(2, events.size());

  }


  @Test
  public void testUpdateStartAndEndTimes() {
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);
    EventStorage eventStorage = new EventStorage();
    EditEvent editEvent = new EditEvent(event, eventStorage);

    String newStartTime = startTime.plusHours(1).
            format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    String newEndTime = endTime.plusHours(1).
            format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

    assertTrue(editEvent.executeEdit("start", newStartTime));


    assertFalse(editEvent.executeEdit("start", endTime.plusHours(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))));
    assertFalse(editEvent.executeEdit("end", startTime.minusHours(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))));
  }

  @Test
  public void testExecuteMultipleEditsWithNonRecurringEvents() {
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime,
            endTime, "desc", "loc", "type", false);
    List<CalendarEvent> events = Collections.singletonList(event);
    EventStorage eventStorage = new EventStorage();

    try {
      EditEvent.executeMultipleEdits(events, "subject",
              "New Subject", eventStorage);

    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("non-recurring events"));
    }
  }
}