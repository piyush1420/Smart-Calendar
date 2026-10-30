package model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Calendar class functionality.
 */
public class CalendarTest {
  private Calendar calendar;

  @Before
  public void setUp() {
    MultiCalendarEventStorage eventStorage = new MultiCalendarEventStorage();
    calendar = new Calendar("Work", ZoneId.of("America/New_York"), eventStorage);
  }

  @Test
  public void shouldAddEventSuccessfullyWhenNoConflict() {
    CalendarEvent event = new CalendarEvent("Meeting",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "Team Meeting", "Office", "Work", false);
    calendar.addEvent(event, false);
    List<CalendarEvent> events = calendar.getEventsOnDate(LocalDateTime.now());
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void shouldPreventAddingConflictingEventsWhenAutoDeclineEnabled() {
    CalendarEvent event1 = new CalendarEvent("Meeting",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "Team Meeting", "Office", "Work", false);
    CalendarEvent event2 = new CalendarEvent("Workshop",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "Team Workshop", "Office", "Work", false);
    calendar.addEvent(event1, true);
    calendar.addEvent(event2, true);
    List<CalendarEvent> events = calendar.getEventsOnDate(LocalDateTime.now());
    assertEquals(1, events.size());
  }

  @Test
  public void shouldFindEventByNameAndStartTime() {
    LocalDateTime startTime = LocalDateTime.now();
    CalendarEvent event = new CalendarEvent("Meeting",
            startTime,
            startTime.plusHours(1),
            "Team Meeting", "Office", "Work", false);
    calendar.addEvent(event, false);
    CalendarEvent foundEvent = calendar.findEvent("Meeting", startTime);
    assertNotNull(foundEvent);
    assertEquals("Meeting", foundEvent.getSubject());
  }

  @Test
  public void shouldReturnNullWhenEventNotFound() {
    LocalDateTime startTime = LocalDateTime.now();
    CalendarEvent foundEvent = calendar.findEvent("NonExistentEvent", startTime);
    assertNull(foundEvent);
  }

  @Test
  public void testRemoveEvent() {
    // Create calendar and storage
    MultiCalendarEventStorage eventStorage = new MultiCalendarEventStorage();
    Calendar calendar = new Calendar("TestCalendar", ZoneId.systemDefault(), eventStorage);

    // Create test event
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    String eventName = "Test Event";
    CalendarEvent event = new CalendarEvent(eventName, startTime, endTime);

    // Add the event to the calendar
    calendar.addEvent(event, false);

    // Verify event was added
    List<CalendarEvent> events = calendar.getEventsOnDate(startTime);
    assertEquals(1, events.size());
    assertTrue(events.contains(event));

    // Remove the event
    calendar.removeEvent(event);

    // Verify event was removed
    events = calendar.getEventsOnDate(startTime);
    assertTrue(events.isEmpty());
  }

  @Test
  public void testRemoveNonExistentEvent() {
    // Create calendar and storage
    MultiCalendarEventStorage eventStorage = new MultiCalendarEventStorage();
    Calendar calendar = new Calendar("TestCalendar", ZoneId.systemDefault(), eventStorage);

    // Create test event
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    String eventName = "Test Event";
    CalendarEvent event = new CalendarEvent(eventName, startTime, endTime);

    // Try to remove an event that hasn't been added
    calendar.removeEvent(event);

    // Verify no events exist
    List<CalendarEvent> events = calendar.getEventsOnDate(startTime);
    assertTrue(events.isEmpty());
  }

  @Test
  public void testRemoveEventFromEmptyCalendar() {
    // Create calendar and storage
    MultiCalendarEventStorage eventStorage = new MultiCalendarEventStorage();
    Calendar calendar = new Calendar("TestCalendar", ZoneId.systemDefault(), eventStorage);

    // Create test event
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    String eventName = "Test Event";
    CalendarEvent event = new CalendarEvent(eventName, startTime, endTime);

    // Try to remove an event from an empty calendar
    calendar.removeEvent(event);

    // Verify calendar is still empty
    List<CalendarEvent> events = calendar.getEventsOnDate(startTime);
    assertTrue(events.isEmpty());
  }
}