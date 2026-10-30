package model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests MultiCalendarEventStorage functionality.
 */
public class MultiCalendarEventStorageTest {

  private MultiCalendarEventStorage eventStorage;
  private LocalDateTime specificTime;
  private CalendarManager calendarManager;

  @Before
  public void setUp() {
    eventStorage = new MultiCalendarEventStorage();
    specificTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    calendarManager = new CalendarManager(eventStorage);
    calendarManager.createCalendar("Spring", ZoneId.of("America/New_York"));
    calendarManager.createCalendar("Fall", ZoneId.of("America/Los_Angeles"));
  }

  @Test
  public void addsEventSuccessfully() {
    CalendarEvent event = new CalendarEvent("Meeting", specificTime,
            specificTime.plusHours(1), "Team Meeting", "Office", "Work",
            false);
    eventStorage.addEvent("Work", event, false);
    CalendarEvent foundEvent = eventStorage.findEvent("Work", "Meeting",
            specificTime);
    assertNotNull(foundEvent);
    assertEquals("Meeting", foundEvent.getSubject());
  }

  @Test
  public void preventsConflictingEvents() {
    CalendarEvent event1 = new CalendarEvent("Meeting", specificTime,
            specificTime.plusHours(1), "Team Meeting", "Office", "Work",
            false);
    CalendarEvent event2 = new CalendarEvent("Workshop", specificTime,
            specificTime.plusHours(1), "Team Workshop", "Office", "Work",
            false);
    eventStorage.addEvent("Work", event1, true);
    eventStorage.addEvent("Work", event2, true);
    CalendarEvent foundEvent = eventStorage.findEvent("Work", "Workshop",
            specificTime);
    assertNull(foundEvent);
  }

  @Test
  public void findsEventsInDifferentCalendars() {
    CalendarEvent event1 = new CalendarEvent("Meeting", specificTime,
            specificTime.plusHours(1), "Team Meeting", "Office", "Work",
            false);
    CalendarEvent event2 = new CalendarEvent("Workshop", specificTime,
            specificTime.plusHours(1), "Team Workshop", "Office",
            "Personal", false);
    eventStorage.addEvent("Work", event1, false);
    eventStorage.addEvent("Personal", event2, false);
    CalendarEvent foundEvent1 = eventStorage.findEvent("Work", "Meeting",
            specificTime);
    CalendarEvent foundEvent2 = eventStorage.findEvent(
            "Personal", "Workshop",
            specificTime);
    assertNotNull(foundEvent1);
    assertNotNull(foundEvent2);
    assertEquals("Meeting", foundEvent1.getSubject());
    assertEquals("Workshop", foundEvent2.getSubject());
  }

  @Test
  public void addsMultipleEventsToSpringCalendar() {
    String spring = "Spring";
    String fall = "Fall";
    calendarManager.useCalendar(spring);

    Calendar springCal = new Calendar(spring,
            ZoneId.of("America/New_York"), eventStorage);
    Calendar fallCal = new Calendar(fall,
            ZoneId.of("America/New_York"), eventStorage);

    calendarManager.useCalendar(fallCal.getName());

    CalendarEvent event1 = new CalendarEvent("Spring Meeting",
            LocalDateTime.of(2023, 4, 10, 10, 0),
            LocalDateTime.of(2023, 4, 10, 11, 0));
    CalendarEvent event2 = new CalendarEvent("Spring Conference",
            LocalDateTime.of(2023, 4, 10, 14, 0),
            LocalDateTime.of(2023, 4, 10, 16, 0));

    springCal.addEvent(event1, true);
    springCal.addEvent(event2, true);

    Map<LocalDateTime, List<CalendarEvent>> events =
            eventStorage.getEventsForCalendar(spring);
    assertEquals(2, events.size());
  }

  @Test
  public void testHasConflictWithNullCalendar() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();

    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);

    boolean result = storage.hasConflict("NonExistentCalendar", event);

    assertFalse(result);
  }

  @Test
  public void testHasConflictWithNoEvents() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    storage.putCalendarStorage(calendarName, new EventStorage());

    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);

    boolean result = storage.hasConflict(calendarName, event);

    assertFalse(result);
  }

  @Test
  public void testHasConflictWithConflictingEvent() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    LocalDateTime existingStart = LocalDateTime.now();
    LocalDateTime existingEnd = existingStart.plusHours(1);
    CalendarEvent existingEvent = new CalendarEvent(
            "Existing Event", existingStart, existingEnd);

    storage.addEvent(calendarName, existingEvent, false);

    LocalDateTime newStart = existingStart.plusMinutes(30);
    LocalDateTime newEnd = newStart.plusHours(1);
    CalendarEvent newEvent = new CalendarEvent("New Event", newStart, newEnd);

    boolean result = storage.hasConflict(calendarName, newEvent);

    assertTrue(result);
  }

  @Test
  public void testGetEventsOnDateWithExistingEvents() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    LocalDateTime today = LocalDateTime.now();
    LocalDateTime startTime1 = today.withHour(9).withMinute(0);
    LocalDateTime endTime1 = today.withHour(10).withMinute(0);

    CalendarEvent event1 = new CalendarEvent("Meeting", startTime1, endTime1);
    storage.addEvent(calendarName, event1, false);

    LocalDateTime startTime2 = today.withHour(14).withMinute(0);
    LocalDateTime endTime2 = today.withHour(15).withMinute(0);
    CalendarEvent event2 = new CalendarEvent("Presentation", startTime2, endTime2);
    storage.addEvent(calendarName, event2, false);

    List<CalendarEvent> eventsToday = storage.getEventsOnDate(calendarName, today);

    assertEquals(2, eventsToday.size());
    assertTrue(eventsToday.contains(event1));
    assertTrue(eventsToday.contains(event2));

    LocalDateTime tomorrow = today.plusDays(1);
    List<CalendarEvent> eventsTomorrow = storage.getEventsOnDate(calendarName, tomorrow);

    assertTrue(eventsTomorrow.isEmpty());

    List<CalendarEvent> nonExistentCalendarEvents =
            storage.getEventsOnDate("NonExistentCalendar", today);
    assertTrue(nonExistentCalendarEvents.isEmpty());
  }

  @Test
  public void testGetEventsInRangeWithExistingEvents() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusDays(7);

    LocalDateTime eventStart1 = start.plusDays(1);
    LocalDateTime eventEnd1 = eventStart1.plusHours(2);
    CalendarEvent event1 = new CalendarEvent("Event 1", eventStart1, eventEnd1);
    storage.addEvent(calendarName, event1, false);

    LocalDateTime eventStart2 = start.plusDays(3);
    LocalDateTime eventEnd2 = eventStart2.plusHours(1);
    CalendarEvent event2 = new CalendarEvent("Event 2", eventStart2, eventEnd2);
    storage.addEvent(calendarName, event2, false);

    LocalDateTime eventStart3 = end.plusDays(1);
    LocalDateTime eventEnd3 = eventStart3.plusHours(2);
    CalendarEvent event3 = new CalendarEvent("Event 3", eventStart3, eventEnd3);
    storage.addEvent(calendarName, event3, false);

    List<CalendarEvent> eventsInRange = storage.getEventsInRange(calendarName, start, end);

    assertEquals(2, eventsInRange.size());
    assertTrue(eventsInRange.contains(event1));
    assertTrue(eventsInRange.contains(event2));
    assertFalse(eventsInRange.contains(event3));

    List<CalendarEvent> nonExistentCalendarEvents =
            storage.getEventsInRange("NonExistentCalendar", start, end);
    assertTrue(nonExistentCalendarEvents.isEmpty());
  }

  @Test
  public void testGetEventsForCalendarWithExistingCalendar() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);
    storage.addEvent(calendarName, event, false);

    Map<LocalDateTime, List<CalendarEvent>> calendarEvents =
            storage.getEventsForCalendar(calendarName);

    assertFalse(calendarEvents.isEmpty());
    assertTrue(calendarEvents.containsKey(startTime));
    assertEquals(1, calendarEvents.get(startTime).size());
    assertEquals(event, calendarEvents.get(startTime).get(0));
  }

  @Test
  public void testGetEventsForCalendarWithNonExistentCalendar() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();

    Map<LocalDateTime, List<CalendarEvent>> calendarEvents =
            storage.getEventsForCalendar("NonExistentCalendar");
    assertTrue(calendarEvents.isEmpty());
    assertEquals(new HashMap<>(), calendarEvents);
  }

  @Test
  public void testGetEventStorageForCalendarWithExistingCalendar() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    EventStorage eventStorage = new EventStorage();
    storage.putCalendarStorage(calendarName, eventStorage);

    EventStorage retrievedStorage = storage.getEventStorageForCalendar(calendarName);

    assertNotNull(retrievedStorage);
    assertEquals(eventStorage, retrievedStorage);
  }

  @Test
  public void testGetEventStorageForCalendarWithNonExistentCalendar() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();

    EventStorage retrievedStorage = storage.
            getEventStorageForCalendar("NonExistentCalendar");

    assertNull(retrievedStorage);
  }

  @Test
  public void testRemoveCalendarStorageAndPutCalendarStorage() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    EventStorage eventStorage = new EventStorage();
    storage.putCalendarStorage(calendarName, eventStorage);

    assertNotNull(storage.getEventStorageForCalendar(calendarName));

    storage.removeCalendarStorage(calendarName);

    assertNull(storage.getEventStorageForCalendar(calendarName));
  }

  @Test
  public void testAddEventAndGetEventStorage() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);
    storage.addEvent(calendarName, event, false);

    EventStorage eventStorage = storage.getEventStorage(calendarName);
    assertNotNull(eventStorage);

    CalendarEvent retrievedEvent = eventStorage.findEvent("Test Event", startTime);
    assertNotNull(retrievedEvent);
    assertEquals(event, retrievedEvent);
  }

  @Test
  public void testFindEventAndRemoveEvent() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "TestCalendar";

    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event", startTime, endTime);
    storage.addEvent(calendarName, event, false);

    CalendarEvent foundEvent = storage.findEvent(calendarName, "Test Event", startTime);
    assertNotNull(foundEvent);
    assertEquals(event, foundEvent);

    CalendarEvent nonExistentEvent =
            storage.findEvent(calendarName, "Non-Existent Event", startTime);
    assertNull(nonExistentEvent);

    CalendarEvent eventInNonExistentCalendar =
            storage.findEvent(
                    "NonExistentCalendar", "Test Event", startTime);
    assertNull(eventInNonExistentCalendar);

    storage.removeEvent(calendarName, event);

    CalendarEvent removedEvent = storage.findEvent(
            calendarName, "Test Event", startTime);
    assertNull(removedEvent);
  }

  @Test
  public void testGetEventsOnDateMK() {
    // Create storage and calendar
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "MutationKillerCalendar";
    // Add events for today
    LocalDateTime today = LocalDateTime.now();
    LocalDateTime startTime = today.withHour(9).withMinute(0);
    LocalDateTime endTime = today.withHour(10).withMinute(0);
    CalendarEvent event = new CalendarEvent("MutationTest", startTime, endTime);
    storage.addEvent(calendarName, event, false);
    // Get events for today - should not be empty or a default empty list
    List<CalendarEvent> events = storage.getEventsOnDate(calendarName, today);
    // Basic assertions
    assertEquals(1, events.size());
    assertEquals("MutationTest", events.get(0).getSubject());
    // Critical assertion for mutation: if we modify the returned list, it should affect
    // future calls if it's not a default empty list
    events.clear();
    List<CalendarEvent> eventsAfterClear = storage.getEventsOnDate(calendarName, today);
    assertTrue("Events should still exist after clearing the returned list",
            !eventsAfterClear.isEmpty());
    assertEquals(1, eventsAfterClear.size());
  }

  @Test
  public void testGetEventsInRangeMK() {
    // Create storage and calendar
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "MutationKillerCalendar";
    // Add event within a date range
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusDays(7);
    LocalDateTime eventStart = start.plusDays(1);
    LocalDateTime eventEnd = eventStart.plusHours(2);
    CalendarEvent event = new CalendarEvent("RangeEvent", eventStart, eventEnd);
    storage.addEvent(calendarName, event, false);
    // Get events in range - should not be empty or a default empty list
    List<CalendarEvent> events = storage.getEventsInRange(calendarName, start, end);
    // Basic assertions
    assertEquals(1, events.size());
    assertEquals("RangeEvent", events.get(0).getSubject());
    // Try to add a second event to the returned list
    LocalDateTime now = LocalDateTime.now();
    CalendarEvent newEvent = new CalendarEvent("NewEvent", now, now.plusHours(1));
    events.add(newEvent);
    // Verify the original storage is unaffected
    List<CalendarEvent> eventsAfterAdd = storage.getEventsInRange(calendarName, start, end);
    assertEquals("Original storage should be unaffected", 1, eventsAfterAdd.size());
    assertEquals("RangeEvent", eventsAfterAdd.get(0).getSubject());
    // Verify returned list is not a reference to a static empty list
    assertNotSame("Empty list should not be a static reference",
            storage.getEventsInRange("NonExistentCalendar", start, end),
            storage.getEventsInRange("AnotherNonExistent", start, end));
  }

  @Test
  public void testGetEventsForCalendarMutationKiller() {
    // Create storage and calendar
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    String calendarName = "MutationKillerCalendar";

    // Add an event
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("CalendarEvent", startTime, endTime);
    storage.addEvent(calendarName, event, false);

    // Get events for calendar - should not be empty or a default empty map
    Map<LocalDateTime, List<CalendarEvent>> events = storage.getEventsForCalendar(calendarName);

    // Basic assertions
    assertEquals(1, events.size());
    assertTrue(events.containsKey(startTime));

    // Add a new key to the returned map
    LocalDateTime newKey = LocalDateTime.now().plusDays(1);
    events.put(newKey, new java.util.ArrayList<>());

    // Verify the original storage is unaffected
    Map<LocalDateTime, List<CalendarEvent>> eventsAfterAdd =
            storage.getEventsForCalendar(calendarName);
    assertEquals("Original storage should be unaffected",
            2, eventsAfterAdd.size());
  }
}