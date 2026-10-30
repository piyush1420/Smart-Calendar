package model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for {@link EventCopier} copying calendar events across calendar.
 */
public class EventCopierTest {
  private EventCopier eventCopier;
  private CalendarManager calendarManager;
  private MultiCalendarEventStorage eventStorage;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Before
  public void setUp() {
    System.setOut(new PrintStream(outContent));
    eventStorage = new MultiCalendarEventStorage();
    calendarManager = new CalendarManager(eventStorage);

    // Set up source calendar
    calendarManager.createCalendar("SourceCalendar",
            ZoneId.of("America/New_York"));
    calendarManager.useCalendar("SourceCalendar");

    // Set up target calendar
    calendarManager.createCalendar(
            "TargetCalendar", ZoneId.of("Europe/London"));

    // Create the EventCopier
    eventCopier = new EventCopier(calendarManager);
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void testCopyEventNoCurrentCalendar() {
    // Reset calendar manager
    calendarManager = new CalendarManager(eventStorage);
    eventCopier = new EventCopier(calendarManager);

    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime targetTime = startTime.plusHours(1);

    boolean result = eventCopier.copyEvent("TestEvent",
            startTime, "TargetCalendar", targetTime);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: No calendar selected."));
  }

  @Test
  public void testCopyEventEventNotFound() {
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime targetTime = startTime.plusHours(1);

    boolean result = eventCopier.copyEvent("NonExistentEvent", startTime,
            "TargetCalendar", targetTime);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: Event not found."));
  }

  @Test
  public void testCopyEventTargetCalendarNotFound() {
    // Add an event to source calendar
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("TestEvent", startTime, endTime);
    calendarManager.getCurrentCalendar().addEvent(event, false);

    LocalDateTime targetTime = startTime.plusHours(2);

    boolean result = eventCopier.copyEvent("TestEvent", startTime,
            "NonExistentCalendar", targetTime);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: Target calendar not found."));
  }

  @Test
  public void testCopyEventAlreadyExists() {
    // Add event to source calendar
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("TestEvent", startTime, endTime);
    calendarManager.getCurrentCalendar().addEvent(event, false);

    // Add same event to target calendar
    calendarManager.useCalendar("TargetCalendar");
    LocalDateTime targetTime = startTime.plusHours(2);
    CalendarEvent targetEvent = new CalendarEvent("TestEvent", targetTime,
            targetTime.plusHours(1));
    calendarManager.getCurrentCalendar().addEvent(targetEvent, false);

    // Switch back to source calendar
    calendarManager.useCalendar("SourceCalendar");

    boolean result = eventCopier.copyEvent("TestEvent", startTime,
            "TargetCalendar", targetTime);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: Event exists in target."));
  }

  @Test
  public void testCopyEventSuccess() {
    // Add event to source calendar
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("TestEvent", startTime, endTime);
    calendarManager.getCurrentCalendar().addEvent(event, false);

    LocalDateTime targetTime = startTime.plusHours(2);

    boolean result = eventCopier.copyEvent("TestEvent", startTime,
            "TargetCalendar", targetTime);

    assertTrue(result);
    assertTrue(outContent.toString().contains("Event copied successfully."));
  }

  @Test
  public void testCopyEventsOnDateNoCurrentCalendar() {
    // Reset calendar manager
    calendarManager = new CalendarManager(eventStorage);
    eventCopier = new EventCopier(calendarManager);

    LocalDate today = LocalDate.now();
    LocalDate tomorrow = today.plusDays(1);

    boolean result = eventCopier.copyEventsOnDate(today,
            "TargetCalendar", tomorrow);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: No calendar selected."));
  }

  @Test
  public void testCopyEventsOnDateTargetCalendarNotFound() {
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = today.plusDays(1);

    boolean result = eventCopier.copyEventsOnDate(today,
            "NonExistentCalendar", tomorrow);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: Target calendar not found."));
  }

  @Test
  public void testCopyEventsOnDateNoEvents() {
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = today.plusDays(1);

    boolean result = eventCopier.copyEventsOnDate(today,
            "TargetCalendar", tomorrow);

    assertFalse(result);
    assertTrue(outContent.toString().contains("No events found on " + today + "."));
  }

  @Test
  public void testCopyEventsOnDateSuccess() {
    // Add events to source calendar for today
    LocalDate today = LocalDate.now();
    LocalDateTime startTime = today.atTime(9, 0);
    LocalDateTime endTime = today.atTime(10, 0);
    CalendarEvent event = new CalendarEvent("DailyMeeting", startTime, endTime);
    calendarManager.getCurrentCalendar().addEvent(event, false);

    LocalDate tomorrow = today.plusDays(1);

    boolean result = eventCopier.copyEventsOnDate(today,
            "TargetCalendar", tomorrow);

    assertTrue(result);
    assertTrue(outContent.toString().contains("Events copied successfully."));
  }

  @Test
  public void testCopyEventsBetweenNoCurrentCalendar() {
    // Reset calendar manager
    calendarManager = new CalendarManager(eventStorage);
    eventCopier = new EventCopier(calendarManager);

    LocalDate start = LocalDate.now();
    LocalDate end = start.plusDays(7);
    LocalDate targetStart = start.plusDays(14);

    boolean result = eventCopier.copyEventsBetween(start, end,
            "TargetCalendar", targetStart);

    assertFalse(result);
  }

  @Test
  public void testCopyEventsBetweenNoTargetCalendar() {
    LocalDate start = LocalDate.now();
    LocalDate end = start.plusDays(7);
    LocalDate targetStart = start.plusDays(14);

    boolean result = eventCopier.copyEventsBetween(start, end,
            "NonExistentCalendar", targetStart);

    assertFalse(result);
  }

  @Test
  public void testCopyEventsBetweenNoEvents() {
    LocalDate start = LocalDate.now();
    LocalDate end = start.plusDays(7);
    LocalDate targetStart = start.plusDays(14);

    boolean result = eventCopier.copyEventsBetween(start,
            end, "TargetCalendar", targetStart);

    assertFalse(result);
    assertTrue(outContent.toString().contains("No events found in range."));
  }

  @Test
  public void testCopyEventsBetweenSuccess() {
    // Add events to source calendar within date range
    LocalDate today = LocalDate.now();
    LocalDateTime startTime = today.atTime(9, 0);
    LocalDateTime endTime = today.atTime(10, 0);
    CalendarEvent event = new CalendarEvent("WeeklyMeeting", startTime, endTime);
    calendarManager.getCurrentCalendar().addEvent(event, false);

    LocalDateTime startTime2 = today.plusDays(2).atTime(14, 0);
    LocalDateTime endTime2 = today.plusDays(2).atTime(15, 0);
    CalendarEvent event2 = new CalendarEvent("ProjectReview", startTime2, endTime2);
    calendarManager.getCurrentCalendar().addEvent(event2, false);

    LocalDate start = today;
    LocalDate end = today.plusDays(7);
    LocalDate targetStart = today.plusDays(14);

    boolean result = eventCopier.copyEventsBetween(start,
            end, "TargetCalendar", targetStart);

    assertTrue(result);
    assertTrue(outContent.toString().contains("Events copied successfully."));
  }
}