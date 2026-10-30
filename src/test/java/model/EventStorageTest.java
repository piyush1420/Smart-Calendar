package model;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the functionality of the EventStorage class, ensuring
 * that events are added, retrieved, and managed correctly, including
 * handling conflicts and all-day events.
 */
public class EventStorageTest {

  private EventStorage storage;
  private CalendarEvent event1;
  private CalendarEvent event2;
  private CalendarEvent allDayEvent;
  private CalendarEvent conflictingEvent;

  @Before
  public void setUp() {
    storage = new EventStorage();
    event1 = new CalendarEvent("Meeting", LocalDateTime.of(2025, 3,
            4,
            10, 0), LocalDateTime.of(2025, 3, 4,
            11, 0));
    event2 = new CalendarEvent("Lunch", LocalDateTime.of(2025, 3, 4,
            12, 0), LocalDateTime.of(2025, 3, 4, 13,
            0));
    allDayEvent = new CalendarEvent("Holiday", LocalDate.of(2025, 3,
            5));
    conflictingEvent = new CalendarEvent("Conflicting Meeting",
            LocalDateTime.of(2025, 3, 4, 10, 30),
            LocalDateTime.of(2025, 3, 4, 11, 30));
  }

  private boolean isAllDayEvent(CalendarEvent event) {
    LocalDateTime start = event.getStartDateTime();
    LocalDateTime end = event.getEndDateTime();
    return start.toLocalTime().equals(java.time.LocalTime.MIDNIGHT) &&
            end.toLocalTime().equals(java.time.LocalTime.of(23, 59)) &&
            start.toLocalDate().equals(end.toLocalDate());
  }

  @Test
  public void testAddSingleEventWithoutConflict() {
    storage.addEvent(event1, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    List<CalendarEvent> events = eventsMap.get(event1.getStartDateTime());

    assertNotNull(events);
    assertEquals(1, events.size());
    assertEquals(event1, events.get(0));
  }

  @Test
  public void testAddConflictingEventWithAutoDecline() {
    storage.addEvent(event1, false);
    storage.addEvent(conflictingEvent, true);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    List<CalendarEvent> events = eventsMap.get(event1.getStartDateTime());

    assertEquals(1, eventsMap.size());
    assertEquals(event1, events.get(0));
  }

  @Test
  public void testAddConflictingEventWithoutAutoDecline() {
    storage.addEvent(event1, false);
    storage.addEvent(conflictingEvent, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();

    assertEquals(1, eventsMap.size());
    assertTrue(eventsMap.containsKey(event1.getStartDateTime()));
  }

  @Test
  public void testAddAllDayEvent() {
    storage.addEvent(allDayEvent, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    List<CalendarEvent> events = eventsMap.get(allDayEvent.getStartDateTime());

    assertNotNull(events);
    assertEquals(1, events.size());
    assertEquals(allDayEvent, events.get(0));
    assertTrue(isAllDayEvent(allDayEvent));
  }

  @Test
  public void testAddNonConflictingEventsWithAutoDecline() {
    storage.addEvent(event1, false);
    storage.addEvent(event2, true);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    assertEquals(2, eventsMap.size());
    assertEquals(1, eventsMap.get(event1.getStartDateTime()).size());
    assertEquals(1, eventsMap.get(event2.getStartDateTime()).size());
  }

  @Test
  public void testAddNonConflictingEventsWithoutAutoDecline() {
    storage.addEvent(event1, false);
    storage.addEvent(event2, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();

    assertEquals(1, eventsMap.get(event1.getStartDateTime()).size());
    assertEquals(1, eventsMap.get(event2.getStartDateTime()).size());
  }

  @Test
  public void testRetrieveEventsFromEmptyStorage() {
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    assertNull(eventsMap.get(LocalDateTime.of(2025, 3, 6, 10,
            0)));
    assertEquals(0, eventsMap.size());
  }

  @Test
  public void testAddEventWithSameStartAndEndTime() {
    CalendarEvent edgeCaseEvent = new CalendarEvent("Same Time Event",
            LocalDateTime.of(2025, 3, 4, 10, 0),
            LocalDateTime.of(2025, 3, 4, 10, 0));
    storage.addEvent(edgeCaseEvent, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    List<CalendarEvent> events = eventsMap.get(edgeCaseEvent.getStartDateTime());

    assertNotNull(events);
    assertEquals(1, events.size());
    assertEquals(edgeCaseEvent, events.get(0));
  }

  @Test
  public void testAddMultipleEventsWithSameStartTime() {
    CalendarEvent eventA = new CalendarEvent("Event A",
            LocalDateTime.of(2025, 3, 4, 15, 0),
            LocalDateTime.of(2025, 3, 4, 16, 0));
    CalendarEvent eventB = new CalendarEvent("Event B",
            LocalDateTime.of(2025, 3, 4, 15, 0),
            LocalDateTime.of(2025, 3, 4, 16, 0));
    storage.addEvent(eventA, false);
    storage.addEvent(eventB, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    List<CalendarEvent> events = eventsMap.get(eventA.getStartDateTime());

    assertNotNull(events);
    assertEquals(1, events.size());
    assertTrue(events.contains(eventA));
  }

  @Test
  public void testAddConflictingAllDayEvents() {
    CalendarEvent allDayEvent1 = new CalendarEvent("Holiday 1",
            LocalDateTime.of(2025, 3, 5, 0, 0),
            LocalDateTime.of(2025, 3, 5, 23, 59));
    CalendarEvent allDayEvent2 = new CalendarEvent("Holiday 2",
            LocalDateTime.of(2025, 3, 5, 0, 0),
            LocalDateTime.of(2025, 3, 5, 23, 59));

    storage.addEvent(allDayEvent1, true);
    storage.addEvent(allDayEvent2, true);

    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    List<CalendarEvent> events = eventsMap.get(allDayEvent1.getStartDateTime());

    assertNotNull(events);
    assertEquals(1, events.size());
    assertTrue(events.contains(allDayEvent1));
    assertTrue(isAllDayEvent(allDayEvent1));
  }

  @Test
  public void testAddAllDayEventWithStartAndEndDateTime() {
    CalendarEvent allDayEventWithEnd = new CalendarEvent("All Day Event",
            LocalDateTime.of(2025, 3, 5, 0, 0),
            LocalDateTime.of(2025, 3, 5, 23, 59));

    storage.addEvent(allDayEventWithEnd, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();

    assertNotNull(eventsMap);
    assertTrue(eventsMap.containsKey(allDayEventWithEnd.getStartDateTime()));
    assertTrue(isAllDayEvent(allDayEventWithEnd));
  }

  @Test
  public void testAddAllDayEventWithStartDateTimeOnly() {
    CalendarEvent allDayEventWithStartOnly = new CalendarEvent("All Day Event Start Only",
            LocalDate.of(2025, 3, 5));

    storage.addEvent(allDayEventWithStartOnly, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();

    assertNotNull(eventsMap);
    assertTrue(eventsMap.containsKey(allDayEventWithStartOnly.getStartDateTime()));
    assertTrue(isAllDayEvent(allDayEventWithStartOnly));
  }

  @Test
  public void testAddSameDay3HourEvent() {
    CalendarEvent sameDayEvent = new CalendarEvent("3-Hour Event",
            LocalDateTime.of(2025, 3, 5, 9, 0),
            LocalDateTime.of(2025, 3, 5, 12, 0));

    storage.addEvent(sameDayEvent, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();

    assertNotNull(eventsMap);
    assertTrue(eventsMap.containsKey(sameDayEvent.getStartDateTime()));
    assertFalse(isAllDayEvent(sameDayEvent));
  }

  @Test
  public void testAddEventThatLastsFor2Days() {
    CalendarEvent twoDayEvent = new CalendarEvent("2-Day Event",
            LocalDateTime.of(2025, 3, 5, 10, 0),
            LocalDateTime.of(2025, 3, 7, 10, 0));

    storage.addEvent(twoDayEvent, false);
    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();

    assertNotNull(eventsMap);
    assertTrue(eventsMap.containsKey(twoDayEvent.getStartDateTime()));
    assertFalse(isAllDayEvent(twoDayEvent));
  }

  @Test
  public void testAddConflictingAllDayEventsWithAutoDecline() {
    CalendarEvent event1 = new CalendarEvent("All Day Event 1",
            LocalDate.of(2025, 3, 5));
    CalendarEvent event2 = new CalendarEvent("All Day Event 2",
            LocalDate.of(2025, 3, 5));

    storage.addEvent(event1, false);
    storage.addEvent(event2, true);

    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    assertNotNull(eventsMap);
    assertEquals(1, eventsMap.size());
    assertTrue(isAllDayEvent(event1));
    assertTrue(isAllDayEvent(event2));
  }

  @Test
  public void testAddConflicting3HourEventsWithAutoDecline() {
    CalendarEvent event1 = new CalendarEvent("Same Day 3-Hour Event 1",
            LocalDateTime.of(2025, 3, 5, 9, 0),
            LocalDateTime.of(2025, 3, 5, 12, 0));
    CalendarEvent event2 = new CalendarEvent("Same Day 3-Hour Event 2",
            LocalDateTime.of(2025, 3, 5, 9, 30),
            LocalDateTime.of(2025, 3, 5, 12, 30));

    storage.addEvent(event1, false);
    storage.addEvent(event2, true);

    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    assertNotNull(eventsMap);
    assertEquals(1, eventsMap.size());
    assertFalse(isAllDayEvent(event1));
    assertFalse(isAllDayEvent(event2));
  }

  @Test
  public void testAddConflicting2DayEventsWithAutoDecline() {
    CalendarEvent event1 = new CalendarEvent("2-Day Event 1",
            LocalDateTime.of(2025, 3, 5, 0, 0),
            LocalDateTime.of(2025, 3, 6, 23, 59, 59));
    CalendarEvent event2 = new CalendarEvent("2-Day Event 2",
            LocalDateTime.of(2025, 3, 5, 0, 0),
            LocalDateTime.of(2025, 3, 6, 23, 59, 59));

    storage.addEvent(event1, false);
    storage.addEvent(event2, true);

    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    assertNotNull(eventsMap);
    assertEquals(1, eventsMap.size());
    assertFalse(isAllDayEvent(event1));
    assertFalse(isAllDayEvent(event2));
  }

  @Test
  public void testMultiDayEventConflict() {
    CalendarEvent multiDayEvent = new CalendarEvent("Multi-Day Event",
            LocalDateTime.of(2025, 3, 5, 0, 0),
            LocalDateTime.of(2025, 3, 8, 23, 59));
    CalendarEvent singleDayEvent = new CalendarEvent("Single-Day Event",
            LocalDateTime.of(2025, 3, 6, 0, 0),
            LocalDateTime.of(2025, 3, 6, 23, 59));

    storage.addEvent(multiDayEvent, false);
    storage.addEvent(singleDayEvent, true);

    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    assertTrue(eventsMap.containsKey(multiDayEvent.getStartDateTime()));
    assertFalse(eventsMap.containsKey(singleDayEvent.getStartDateTime()));
  }

  @Test
  public void testMultiDayEventConflictWithAllDayEvent() {
    CalendarEvent multiDayEvent = new CalendarEvent("Multi-Day Event",
            LocalDateTime.of(2025, 3, 5, 0, 0),
            LocalDateTime.of(2025, 3, 8, 23, 59));
    CalendarEvent singleDayEvent = new CalendarEvent("Single-Day Event",
            LocalDate.of(2025, 3, 5));

    storage.addEvent(multiDayEvent, false);
    storage.addEvent(singleDayEvent, true);

    Map<LocalDateTime, List<CalendarEvent>> eventsMap = storage.getAllEvents();
    assertTrue(eventsMap.containsKey(multiDayEvent.getStartDateTime()));
    assertTrue(eventsMap.containsKey(singleDayEvent.getStartDateTime()));
  }

  @Test
  public void testAddSampleEventWithAllParameters() {
    CalendarEvent event1 = new CalendarEvent(
            "Team Meeting",
            LocalDateTime.of(2025, 3, 10, 14, 0),
            LocalDateTime.of(2025, 3, 10, 16, 0),
            "Ell Hall",
            "Project discussion",
            "public"
    );

    storage.addEvent(event1, true);
    Map<LocalDateTime, List<CalendarEvent>> allEvents = storage.getAllEvents();
    assertEquals(1, allEvents.size());

    List<CalendarEvent> eventsOnDate = storage.getEventsOnDate(
            LocalDateTime.of(2025, 3, 10, 0, 0));
    assertFalse(eventsOnDate.isEmpty());
    assertTrue(eventsOnDate.contains(event1));
  }

  @Test
  public void testAddNonConflictingEventWithAutoDecline() {
    final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    final PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      EventStorage eventStorage = new EventStorage();
      LocalDateTime startTime1 = LocalDateTime.of(2023, 10, 15,
              10, 0);
      LocalDateTime endTime1 = LocalDateTime.of(2023, 10, 15,
              11, 0);
      CalendarEvent event1 = new CalendarEvent("Meeting 1", startTime1, endTime1);

      LocalDateTime startTime3 = LocalDateTime.of(2023, 10, 15,
              12, 0);
      LocalDateTime endTime3 = LocalDateTime.of(2023, 10, 15,
              13, 0);
      CalendarEvent event3 = new CalendarEvent("Meeting 3", startTime3, endTime3);

      eventStorage.addEvent(event1, false);
      eventStorage.addEvent(event3, true);

      assertNotNull(eventStorage.findEvent("Meeting 3", startTime3));


    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testAddConflictingEventWithAutoDecline_1() {
    final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    final PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      EventStorage eventStorage = new EventStorage();
      LocalDateTime startTime1 = LocalDateTime.of(2023, 10, 15,
              10, 0);
      LocalDateTime endTime1 = LocalDateTime.of(2023, 10, 15,
              11, 0);
      CalendarEvent event1 = new CalendarEvent("Meeting 1", startTime1, endTime1);

      LocalDateTime startTime2 = LocalDateTime.of(2023, 10, 15,
              10, 0);
      LocalDateTime endTime2 = LocalDateTime.of(2023, 10, 15,
              11, 0);
      CalendarEvent event2 = new CalendarEvent("Meeting 2", startTime2, endTime2);

      eventStorage.addEvent(event1, false);
      eventStorage.addEvent(event2, true);


      String expectedOutput =
              "Event conflicts with another event and is declined.\n";

      assertEquals(expectedOutput.replaceAll("\\r\\n?", "\n"),
              outContent.toString().replaceAll("\\r\\n?", "\n"));

    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testUpdateEventStartTime_RemoveFromOldTime() {
    // Set up the test environment
    EventStorage eventStorage = new EventStorage();
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      // Create and add an event
      LocalDateTime oldStart = LocalDateTime.of(2023,
              1, 1, 10, 0);
      LocalDateTime end = LocalDateTime.of(2023, 1, 1, 11, 0);
      CalendarEvent event = new CalendarEvent(
              "Test Event", oldStart, end,
              "Description", "Location", "Work");
      eventStorage.addEvent(event, true);

      // Verify event was added correctly
      Map<LocalDateTime, List<CalendarEvent>> eventsMap = eventStorage.getAllEvents();
      assertTrue(eventsMap.containsKey(oldStart));
      assertEquals(1, eventsMap.get(oldStart).size());

      // Update the event's start time
      LocalDateTime newStart = LocalDateTime.of(2023,
              1, 1, 9, 0);
      boolean result = eventStorage.updateEventStartTime(oldStart, newStart, event);

      // Verify the result is true (kills mutation on line 98)
      assertTrue("updateEventStartTime should return true", result);

      // Verify old time slot is removed (kills mutation on line 82, 84)
      assertFalse("Old time slot should be removed", eventsMap.containsKey(oldStart));

      // Verify new time slot is created with the event
      assertTrue("New time slot should exist", eventsMap.containsKey(newStart));
      assertEquals(1, eventsMap.get(newStart).size());

      // Verify the event's start time was updated (kills mutation on line 90)
      assertEquals("Event start time should be updated",
              newStart, event.getStartDateTime());
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testUpdateEventStartTime_RemovesOldAddsNewTimeSlot() {
    // Set up the test environment
    EventStorage eventStorage = new EventStorage();
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      // Create and add an event
      LocalDateTime oldStart = LocalDateTime.of(2023, 1, 1, 10, 0);

      LocalDateTime end = LocalDateTime.of(2023, 1, 1, 11, 0);
      CalendarEvent event = new CalendarEvent(
              "Test Event", oldStart, end,
              "Description", "Location", "Work");
      eventStorage.addEvent(event, true);

      // Verify event was added correctly
      Map<LocalDateTime, List<CalendarEvent>> eventsMap = eventStorage.getAllEvents();
      assertTrue("Events map should contain the old start time",
              eventsMap.containsKey(oldStart));

      // Add a second event at a different time to test line 84
      LocalDateTime otherStart = LocalDateTime.of(2023, 1, 1, 13, 0);
      LocalDateTime otherEnd = LocalDateTime.of(2023, 1, 1, 14, 0);
      CalendarEvent otherEvent = new CalendarEvent(
              "Other Event", otherStart, otherEnd,
              "Other Desc", "Other Loc", "Work");
      eventStorage.addEvent(otherEvent, true);

      // Now add a second event at oldStart - this is a separate instance
      // but the same time slot
      CalendarEvent sameTimeEvent = new CalendarEvent(
              "Same Time Event", oldStart, end,
              "Another Desc", "Another Loc", "Work");
      eventStorage.addEvent(sameTimeEvent, false); // Try to force adding it

      // Check if we can get all events at oldStart - this should be at least one
      List<CalendarEvent> eventsAtOldStart = eventsMap.get(oldStart);
      int initialCount = eventsAtOldStart.size();
      assertTrue("Should have " +
              "at least one event at old start time", initialCount > 0);

      // If we managed to add multiple events at oldStart, test line 84
      if (initialCount > 1) {
        // Remove one of the events to test isEmpty() conditional (line 84)
        // Move one event to a new time
        CalendarEvent eventToMove = eventsAtOldStart.get(0);
        LocalDateTime newStart = LocalDateTime.
                of(2023, 1, 1, 9, 0);
        boolean result = eventStorage.updateEventStartTime(oldStart, newStart, eventToMove);

        // Verify the result is true (kills mutation on line 98)
        assertTrue("updateEventStartTime should return true", result);

        // Check if old time slot still exists with at least one event
        assertTrue("Old time slot should still exist", eventsMap.containsKey(oldStart));

        // Verify event's start time was updated (kills mutation on line 90)
        assertEquals("Event start time should be updated",
                newStart, eventToMove.getStartDateTime());

        // Verify new time slot exists
        assertTrue("New time slot should exist", eventsMap.containsKey(newStart));
      } else {
        // We couldn't add multiple events at same time due to conflict check
        // Let's test both line 82 (removing event) and 84 (removing empty slot)
        // with a single event
        LocalDateTime newStart = LocalDateTime.
                of(2023, 1, 1, 9, 0);
        boolean result = eventStorage.updateEventStartTime(oldStart, newStart, event);

        // Verify the result is true (kills mutation on line 98)
        assertTrue("updateEventStartTime should return true", result);

        // Verify old time slot is removed completely (kills mutation on line 82)
        assertFalse("Old time slot should be removed", eventsMap.containsKey(oldStart));

        // Verify event's start time was updated (kills mutation on line 90)
        assertEquals("Event start time should be updated",
                newStart, event.getStartDateTime());

        // Verify new time slot exists
        assertTrue("New time slot should exist", eventsMap.containsKey(newStart));
      }
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testUpdateEventStartTime_OldTimeRemovedWhenEmpty() {
    // Set up the test environment
    EventStorage eventStorage = new EventStorage();
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      // Create non-conflicting events at different times
      LocalDateTime time1 = LocalDateTime.of(2023, 1, 1, 10, 0);
      LocalDateTime time2 = LocalDateTime.of(2023, 1, 2, 10, 0);
      LocalDateTime end1 = LocalDateTime.of(2023, 1, 1, 11, 0);
      LocalDateTime end2 = LocalDateTime.of(2023, 1, 2, 11, 0);

      CalendarEvent event1 = new CalendarEvent(
              "Event 1", time1, end1, "Description 1", "Location 1", "Work");
      CalendarEvent event2 = new CalendarEvent(
              "Event 2", time2, end2, "Description 2", "Location 2", "Personal");

      eventStorage.addEvent(event1, true);
      eventStorage.addEvent(event2, true);

      // Verify both events were added to different time slots
      Map<LocalDateTime, List<CalendarEvent>> eventsMap = eventStorage.getAllEvents();
      assertTrue("Events map should contain time1", eventsMap.containsKey(time1));
      assertTrue("Events map should contain time2", eventsMap.containsKey(time2));

      // Now update event1's time so it moves from time1 to a new time
      LocalDateTime newTime = LocalDateTime.of(2023, 1, 1, 9, 0);
      boolean result = eventStorage.updateEventStartTime(time1, newTime, event1);

      // Verify update was successful
      assertTrue("Update should return true", result);

      // Re-fetch the events map
      eventsMap = eventStorage.getAllEvents();

      // Verify the old time slot is completely removed (tests line 82 and 84)
      assertFalse("Time1 slot should be removed", eventsMap.containsKey(time1));

      // Verify new time slot exists
      assertTrue("New time slot should exist", eventsMap.containsKey(newTime));

      // Verify event's start time was updated (kills mutation on line 90)
      assertEquals("Event start time should be updated",
              newTime, event1.getStartDateTime());

      // Verify the other event is still where it should be
      assertTrue("Time2 slot should still exist", eventsMap.containsKey(time2));
      assertEquals("Event 2", eventsMap.get(time2).get(0).getSubject());
    } finally {
      System.setOut(originalOut);
    }
  }
}