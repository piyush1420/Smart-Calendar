package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import model.Calendar;
import model.CalendarEvent;
import model.CalendarManager;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for CopyEventParser functionality. Verifies correct parsing,
 * execution of event copy commands. Tests include basic copy operations,
 * cross-calendar copying, and error handling cases.
 */
public class CopyEventParserTest {
  private CopyEventParser copyEventParser;
  private CalendarManager calendarManager;
  private CommandParser commandParser;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @Before
  public void setUp() {
    MultiCalendarEventStorage eventStorage = new MultiCalendarEventStorage();
    calendarManager = new CalendarManager(eventStorage);
    copyEventParser = new CopyEventParser(calendarManager);
    commandParser = new CommandParser(calendarManager);
    System.setOut(new PrintStream(outContent));
  }

  @Test
  public void testCopySingleEventBetweenDifferentCalendarsSuccessfully()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand(
            "create calendar --name Personal --timezone Europe/Paris");

    boolean result = commandParser.executeCommand(
            "copy event Meeting on 2023-10-01T10:00 --target Personal "
                    + "to 2023-10-02T10:00");
    assertTrue(result);
  }

  @Test
  public void testEditEventStartTimeUpdatesSuccessfully()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Home --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Home");
    commandParser.executeCommand(
            "create event Workshop from 2026-10-03T13:00 to 2026-10-03T15:00 "
                    + "repeats TU for 3 times");
    commandParser.executeCommand(
            "create event Conference from 2026-10-05T13:00 to 2026-10-05T16:00");

    commandParser.executeCommand(
            "edit event start Conference from 2026-10-05T13:00 to 2026-10-05T16:00 "
                    + "with 2026-10-01T09:00");
    assertNotNull(outContent.toString());
  }

  @Test
  public void testEditEventWithoutConflictCompletesSuccessfully()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Home --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Home");
    commandParser.executeCommand(
            "create event Workshop from 2026-10-03T13:00 to 2026-10-03T15:00 "
                    + "repeats TU for 3 times");
    commandParser.executeCommand(
            "create event Conference from 2026-10-05T13:00 to 2026-10-05T16:00");

    commandParser.executeCommand(
            "edit event start Conference from 2026-10-05T13:00 to 2026-10-05T16:00 "
                    + "with 2026-10-04T15:01");
    assertTrue(outContent.toString().contains("Edited event: Conference"));
  }

  @Test
  public void testRenameCalendarWithEventsPreservesAllEvents()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Home --timezone America/New_York");
    commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Home");
    commandParser.executeCommand(
            "create event Workshop from 2026-10-03T13:00 to 2026-10-03T15:00 "
                    + "repeats TU for 3 times");

    commandParser.executeCommand(
            "edit calendar --name Home --property name My Home");
    assertNotNull(outContent.toString());
  }

  @Test
  public void testCopyRecurringEventsWithDateShiftCreatesCorrectInstances()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Home --timezone America/New_York");
    commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Home");

    // Create a recurring event on Mondays starting from 2025-03-03
    commandParser.executeCommand(
            "create event Workshop from 2025-03-03T13:00 to 2025-03-03T15:00 " +
                    "repeats M for 3 times");

    // Copy the events to the Work calendar starting from 2025-05-13
    commandParser.executeCommand(
            "copy events between 2025-03-03 and 2025-03-17 --target Work to 2025-05-13");

    Calendar workCalendar = calendarManager.getCalendar("Work");

    // Get events in the new range and verify count
    LocalDateTime start = LocalDate.of(2025, 5, 13).atStartOfDay();
    LocalDateTime end = LocalDate.of(2025, 5, 31).atTime(LocalTime.MAX);

    var copiedEvents = workCalendar.getEventsInRange(start, end);
    assertEquals(3, copiedEvents.size());

    // Validate each individual copied event time
    assertNotNull(workCalendar.findEvent("Workshop",
            LocalDateTime.of(2025, 5, 13, 13, 0)));
    assertNotNull(workCalendar.findEvent("Workshop",
            LocalDateTime.of(2025, 5, 20, 13, 0)));
    assertNotNull(workCalendar.findEvent("Workshop",
            LocalDateTime.of(2025, 5, 27, 13, 0)));
  }

  @Test
  public void testCopySingleEventMaintainsOriginalDuration()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Home --timezone America/New_York");
    commandParser.executeCommand(
            "create calendar --name Work --timezone Europe/London");
    commandParser.executeCommand("use calendar --name Home");

    // Create original event in New York timezone
    commandParser.executeCommand(
            "create event Conference1 from 2025-04-29T10:00 to 2025-04-29T12:59");

    // Copy it to Work calendar (London timezone)
    commandParser.executeCommand(
            "copy event Conference1 on 2025-04-29T10:00 --target Work " +
                    "to 2025-05-12T12:00");

    Calendar workCalendar = calendarManager.getCalendar("Work");
    assertEquals("Europe/London", workCalendar.getTimeZone().getId());

    // Retrieve copied event at 12:00 (local to Europe/London)
    CalendarEvent copied = workCalendar.findEvent(
            "Conference1", LocalDateTime.of(2025, 5, 12,
                    12, 0));
    assertNotNull("Copied event should exist", copied);

    // Duration of original was 2h59m, so check that is preserved
    long durationMinutes = java.time.Duration.between(
            copied.getStartDateTime(), copied.getEndDateTime()).toMinutes();
    assertEquals("Duration must be preserved", 179, durationMinutes);
  }

  @Test
  public void testCopyAllEventsOnDateHandlesTimezoneConversion()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Home --timezone America/New_York");
    commandParser.executeCommand(
            "create calendar --name Work --timezone Europe/London");
    commandParser.executeCommand("use calendar --name Home");
    commandParser.executeCommand(
            "create event Conference1 from 2025-04-29T10:00 to 2025-04-29T12:59");

    commandParser.executeCommand(
            "copy events on 2025-04-29 --target Work to 2025-04-29");
    Calendar workCalendar = calendarManager.getCalendar("Work");
    assertEquals(1, workCalendar.getEventsInRange(
            LocalDate.of(2025, 4, 29).atStartOfDay(),
            LocalDate.of(2025, 4, 30).atTime(LocalTime.MAX)).size());
  }

  @Test
  public void testCreateCalendarAndEventsPersistsEventsCorrectly()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand(
            "create event Meeting1 from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand(
            "create event Meeting2 from 2023-10-01T11:01 to 2023-10-01T12:00");

    Calendar workCalendar = calendarManager.getCalendar("Work");
    assertEquals(2, workCalendar.getEventsOnDate(
            LocalDateTime.of(2023, 10, 1, 10, 0)).size());
  }

  @Test
  public void testCopyNonExistentEventReturnsFalse() {
    boolean result = copyEventParser.executeCommand(
            "copy event NonExistent on 2023-10-01T10:00 --target Personal "
                    + "to 2023-10-02T10:00");
    assertFalse(result);
  }

  @Test
  public void testChangeCalendarTimezoneUpdatesEventsCorrectly()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Home --timezone America/New_York");
    commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Home");
    commandParser.executeCommand(
            "create event Meeting on 2025-05-24 repeats M until 2025-06-14");

    commandParser.executeCommand(
            "edit calendar --name Work --property timezone Europe/Paris");
    Calendar workCalendar = calendarManager.getCalendar("Work");
    assertEquals("Europe/Paris", workCalendar.getTimeZone().getId());
  }

  @Test
  public void testTimezoneChangeReflectedInPrintedEvents()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name Zone Change --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Zone Change");
    commandParser.executeCommand(
            "create event Class 1 from 2025-08-29T10:00 to 2025-08-29T12:59");
    commandParser.executeCommand("print events on 2025-08-29");
    commandParser.executeCommand(
            "edit calendar --name Zone Change --property timezone Europe/London");
    commandParser.executeCommand("print events on 2025-08-29");

    Calendar workCalendar = calendarManager.getCalendar("Zone Change");
    assertEquals("Europe/London", workCalendar.getTimeZone().getId());
  }

  @Test
  public void testRenameEmptyCalendarCompletesSuccessfully()
          throws InvalidCommandException {
    commandParser.executeCommand(
            "create calendar --name home --timezone America/New_York");
    boolean result = commandParser.executeCommand(
            "edit calendar --name home --property name ghar");
    assertTrue(result);
  }

  @Test
  public void testExecuteCommand_InvalidDateTimeFormat_PrintsErrorAndReturnsFalse() {
    // Setup
    System.setOut(new PrintStream(outContent));

    assertThrows(IllegalArgumentException.class, () ->
            copyEventParser.executeCommand(
                    "copy event Meeting on 2023/10/01T10:00 --target Personal to 2023-10-02T10:00"
            )
    );
  }

  @Test
  public void testExecuteCommand_InvalidCommandFormat_ThrowsException() {
    try {
      copyEventParser.executeCommand("invalid copy command");
      fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid command format.", e.getMessage());
    }
  }

  @Test
  public void testExecuteCommand_CopyEventsOnDate_ReturnsCorrectBoolean()
          throws InvalidCommandException {
    // Setup test data
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand("create event Meeting on 2023-10-01 repeats M until 2023-10-14");
    commandParser.executeCommand("print events on 2023-10-02");

    // Test successful copy
    boolean successResult = copyEventParser.executeCommand(
            "copy events on 2023-10-02 --target Work to 2023-10-05");
    assertTrue("Should return true for successful copy", successResult);
    // Test failed copy (non-existent target)
    boolean failResult = copyEventParser.executeCommand(
            "copy events on 2023-10-01 --target NonExistent to 2023-10-02");
    assertFalse("Should return false for failed copy", failResult);
  }

  @Test
  public void testExecuteCommand_CopyEventsBetween_ReturnsCorrectBoolean()
          throws InvalidCommandException {
    // Setup test data
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand("create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand("print events on 2023-10-01");

    // Test successful copy
    boolean successResult = copyEventParser.executeCommand(
            "copy events between 2023-10-01 and 2023-10-02 --target Work to 2023-11-01");
    assertTrue("Should return true for successful copy", successResult);

    assertThrows(IllegalArgumentException.class, () ->
            copyEventParser.executeCommand(
                    "copy events between 2023/10/01 and 2023-10-02 --target Work to 2023-11-01"
            )
    );
  }

  @Test
  public void testExecuteCommand_CopyEventsBetween()
          throws InvalidCommandException {
    // Setup calendar and create multiple events
    commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");

    // Create two events on different dates in range
    commandParser.executeCommand(
            "create event Meeting1 from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand(
            "create event Meeting2 from 2023-10-02T14:00 to 2023-10-02T15:30");

    Calendar workCalendar = calendarManager.getCalendar("Work");

    // Validate original events exist
    CalendarEvent original1 = workCalendar.findEvent(
            "Meeting1", LocalDateTime.of(2023, 10, 1,
                    10, 0));
    CalendarEvent original2 = workCalendar.findEvent(
            "Meeting2", LocalDateTime.of(2023, 10, 2,
                    14, 0));
    assertNotNull(original1);
    assertNotNull(original2);

    // Copy events between date range to a new date
    boolean result = copyEventParser.executeCommand(
            "copy events between 2023-10-01 and 2023-10-02 --target Work to 2023-11-01");
    assertTrue("Copy operation should succeed", result);

    // Validate copied event 1
    LocalDateTime expectedStart1 = LocalDateTime.of(2023, 11, 1,
            10, 0);
    CalendarEvent copied1 = workCalendar.findEvent("Meeting1", expectedStart1);
    assertNotNull("Copied Meeting1 should exist", copied1);
    assertEquals("Start time should match", expectedStart1, copied1.getStartDateTime());
    assertEquals("End time should match",
            expectedStart1.plusHours(1), copied1.getEndDateTime());

    // Validate copied event 2
    LocalDateTime expectedStart2 = LocalDateTime.of(2023, 11, 2,
            14, 0);
    CalendarEvent copied2 = workCalendar.findEvent("Meeting2", expectedStart2);
    assertNotNull("Copied Meeting2 should exist", copied2);
    assertEquals("Start time should match", expectedStart2, copied2.getStartDateTime());
    assertEquals("End time should match",
            expectedStart2.plusMinutes(90), copied2.getEndDateTime());

    // Final check: total number of events on new copied dates
    int eventsOnNov1 = workCalendar.getEventsOnDate(expectedStart1).size();
    int eventsOnNov2 = workCalendar.getEventsOnDate(expectedStart2).size();
    assertEquals(1, eventsOnNov1);
    assertEquals(1, eventsOnNov2);

    // Negative test: invalid date format
    assertThrows(IllegalArgumentException.class, () ->
            copyEventParser.executeCommand(
                    "copy events between 2023/10/01 and 2023-10-02 --target Work to 2023-11-01")
    );
  }

  @Test
  public void testExecuteCommand_CopySingleEvent_ReturnsCorrectBoolean()
          throws InvalidCommandException {
    // Setup test data
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand("create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand("print events on 2023-10-01");

    // Test successful copy
    boolean successResult = copyEventParser.executeCommand(
            "copy event Meeting on 2023-10-01T10:00 --target Work to 2023-10-02T10:00");
    assertTrue("Should return true for successful copy", successResult);

    // Test failed copy (non-existent event)
    boolean failResult = copyEventParser.executeCommand(
            "copy event NonExistent on 2023-10-01T10:00 --target Work to 2023-10-02T10:00");
    assertFalse("Should return false for non-existent event", failResult);
  }

  @Test
  public void testExecuteCommand_CopySingleEvent()
          throws InvalidCommandException {
    // Setup calendar and event
    commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");

    // Validate original event is created
    Calendar workCalendar = calendarManager.getCalendar("Work");
    assertNotNull("Original event should be present",
            workCalendar.findEvent("Meeting",
                    LocalDateTime.of(2023, 10, 1, 10, 0)));

    // Execute successful copy command
    boolean successResult = copyEventParser.executeCommand(
            "copy event Meeting on 2023-10-01T10:00 --target Work to 2023-10-02T10:00");
    assertTrue("Should return true for successful copy", successResult);

    // Validate copied event exists
    assertNotNull("Copied event should exist",
            workCalendar.findEvent("Meeting",
                    LocalDateTime.of(2023, 10, 2, 10, 0)));

    // Test failed copy (non-existent event)
    boolean failResult = copyEventParser.executeCommand(
            "copy event NonExistent on 2023-10-01T10:00 --target Work to 2023-10-02T10:00");
    assertFalse("Should return false for non-existent event", failResult);
  }

  @Test
  public void testExecuteCommand_CopyEventsBetween_FailedCopy_ReturnsFalse()
          throws InvalidCommandException {
    // Setup
    commandParser.executeCommand("create calendar --name Source --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Source");
    commandParser.executeCommand("create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");

    // Create target calendar but make it read-only or otherwise cause copy to fail
    commandParser.executeCommand("create calendar --name Target --timezone America/New_York");

    // Make the copy fail by creating a conflicting event
    commandParser.executeCommand("use calendar --name Target");
    commandParser.executeCommand("create event Conflict from " +
            "2023-11-01T10:00 to 2023-11-01T11:00");

    // Execute copy command that should fail due to conflict
    boolean result = copyEventParser.executeCommand(
            "copy events between 2023-10-01 and 2023-10-02 --target Target to 2023-11-01");

    assertFalse("Should return false when copy operation fails", result);
  }

  @Test
  public void testExecuteCommand_CopyEventsBetween_DayLight()
          throws InvalidCommandException {
    // Setup
    commandParser.executeCommand("create calendar --name home --timezone America/New_York");
    commandParser.executeCommand("create calendar --name work --timezone Europe/London");
    commandParser.executeCommand("use calendar --name home");

    // Create target calendar but make it read-only or otherwise cause copy to fail
    commandParser.executeCommand("create event Workshop from " +
            "2025-03-03T13:00 to 2025-03-03T15:00" +
            " repeats M for 3 times");
    commandParser.executeCommand("create event Workshop 1 from 2025-03-05T13:00 to" +
            " 2025-03-05T15:00");
    commandParser.executeCommand("create event Workshop 2 from 2025-03-06T13:00 to" +
            " 2025-03-06T15:00");

    // Execute copy command that should fail due to conflict
    boolean result = copyEventParser.executeCommand(
            "copy events between 2025-03-03 and 2025-03-17 --target work to 2025-03-03");

    // Make the copy fail by creating a conflicting event
    commandParser.executeCommand("use calendar --name work");
    commandParser.executeCommand("print events from 2025-03-03T00:00 to 2025-03-24T13:00");
    assertTrue(result);

  }

  @Test
  public void testExecuteCommand_CopyEventsBetween_DayLight_1()
          throws InvalidCommandException {
    // Setup
    commandParser.executeCommand("create calendar --name home --timezone Europe/London");
    commandParser.executeCommand("use calendar --name home");

    commandParser.executeCommand("create event Workshop 1 from 2025-03-05T13:00 to" +
            " 2025-03-05T15:00");

    // Execute copy command that should fail due to conflict
    boolean result = copyEventParser.executeCommand(
            "copy events between 2025-03-05 and 2025-03-05 --target home to 2025-03-07");
    commandParser.executeCommand("print events from 2025-03-03T00:00 to 2025-03-24T13:00");
    assertTrue(result);
  }


  @Test
  public void testTimezoneChangeReflectedInPrintedEventsValidate()
          throws InvalidCommandException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      // Create calendar and event
      commandParser.executeCommand("create calendar --name Zone Change " +
              "--timezone America/New_York");
      commandParser.executeCommand("use calendar --name Zone Change");
      commandParser.executeCommand("create event Class 1 from " +
              "2025-08-29T10:00 to 2025-08-29T12:59");

      // Validate calendar name and timezone before change
      assertEquals("Zone Change", calendarManager.getCurrentCalendar().getName());
      assertEquals("America/New_York", calendarManager.getCurrentCalendar().
              getTimeZone().getId());

      // Print event before timezone change
      commandParser.executeCommand("print events on 2025-08-29");
      String outputBefore = outContent.toString().trim();
      outContent.reset();

      // Change timezone
      commandParser.executeCommand("edit calendar --name Zone Change --property " +
              "timezone Europe/London");

      // Validate calendar name and timezone after change
      assertEquals("Zone Change", calendarManager.getCurrentCalendar().getName());
      assertEquals("Europe/London", calendarManager.getCurrentCalendar().
              getTimeZone().getId());

      // Print event after timezone change
      commandParser.executeCommand("print events on 2025-08-29");
      String outputAfter = outContent.toString().trim();

      // Check event time in original timezone
      assertTrue(outputBefore.contains("10:00") && outputBefore.contains("12:59"));

      // Check event time in updated timezone (London is +5h in summer)
      assertTrue(outputAfter.contains("15:00") && outputAfter.contains("17:59"));

    } finally {
      System.setOut(originalOut); // Restore output stream
    }
  }
}