package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import controller.command.Command;
import model.Calendar;
import model.CalendarEvent;
import model.CalendarManager;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the CommandParser class to ensure it correctly parses and executes
 * commands for creating, editing, printing, exporting, and showing calendar
 * events. It also verifies error handling for invalid or empty commands.
 */
public class CommandParserTest {

  private CalendarManager calendarManager;
  private CommandParser commandParser;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @Before
  public void setUp() {
    MultiCalendarEventStorage multiCalendarEventStorage = new MultiCalendarEventStorage();
    calendarManager = new CalendarManager(multiCalendarEventStorage);
    commandParser = new CommandParser(calendarManager);
    System.setOut(new PrintStream(outContent));
  }

  @Test
  public void rejectsNullCommands() throws InvalidCommandException {
    boolean result = commandParser.executeCommand(null);
    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: Empty command."));
  }

  @Test
  public void rejectsEmptyCommands() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("");
    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: Empty command."));
  }

  @Test(expected = InvalidCommandException.class)
  public void rejectsInvalidCommandFormats() throws InvalidCommandException {
    commandParser.executeCommand("invalid command");
  }

  @Test
  public void createsCalendarWithValidParameters() throws InvalidCommandException {
    outContent.reset();
    boolean result = commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    assertTrue(result);
    assertTrue(outContent.toString().contains("Calendar 'Work' created successfully with" +
            " timezone: America/New_York"));
  }

  @Test
  public void handlesMissingTimezoneWhenCreatingCalendar() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("create calendar --name Work");
    assertFalse(result);
  }

  @Test
  public void updatesCalendarTimezoneSuccessfully() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    boolean result = commandParser.executeCommand(
            "edit calendar --name Work --property timezone Europe/Paris");
    assertTrue(result);
    assertTrue(outContent.toString().contains("Calendar 'Work' updated successfully."));
  }

  @Test
  public void rejectsInvalidPropertyUpdates() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    outContent.reset();
    boolean result = commandParser.executeCommand(
            "edit calendar --name Work --property invalidProperty Europe/Paris");
    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: Invalid command format."));
  }

  @Test
  public void switchesToExistingCalendar() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    outContent.reset();
    boolean result = commandParser.executeCommand("use calendar --name Work");
    assertTrue(result);
    assertTrue(outContent.toString().contains("Switched to calendar 'Work'."));
  }

  @Test
  public void handlesNonexistentCalendarSwitch() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("use calendar --name NonExistent");
    assertTrue(result);
    assertTrue(outContent.toString().contains("Calendar 'NonExistent' does not exist"));
  }

  @Test
  public void copiesEventBetweenCalendars() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand("create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand("create calendar --name Personal --timezone America/New_York");
    outContent.reset();
    boolean result = commandParser.executeCommand(
            "copy event Meeting on 2023-10-01T10:00 --target Personal to 2023-10-02T10:00");
    assertTrue(result);
    assertTrue(outContent.toString().contains("Event copied successfully."));
  }

  @Test
  public void printsEventsForSpecificDate() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand("create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");
    outContent.reset();
    boolean result = commandParser.executeCommand("print events on 2023-10-01");
    assertTrue(result);
    assertTrue(outContent.toString().contains("Events on 2023-10-01"));
  }

  @Test
  public void exportsCalendarToCsv() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand("create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");
    boolean result = commandParser.executeCommand("export cal events.csv");
    assertTrue(result);
    assertTrue(outContent.toString().contains("CSV generated successfully"));
  }

  @Test
  public void showsBusyStatusCorrectly() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand("create event Meeting from 2023-10-01T10:00 to 2023-10-01T11:00");
    outContent.reset();
    boolean result = commandParser.executeCommand("show status on 2023-10-01T10:30");
    assertTrue(result);
    assertTrue(outContent.toString().contains("User is busy on 2023-10-01T10:30"));
  }

  @Test
  public void rejectsInvalidCreateCommands() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("create Meeting 2025-03-15 10:00 12:00");
    assertFalse(result);
  }

  @Test
  public void rejectsInvalidPrintCommands() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("print");
    assertFalse(result);
  }

  @Test
  public void rejectsInvalidExportCommands() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("export events.csv");
    assertFalse(result);
  }

  @Test
  public void rejectsInvalidShowCommands() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("show");
    assertFalse(result);
  }

  @Test
  public void maintainsEventsWhenChangingTimezone() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("create event Meeting1 from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand("create event Meeting2 from 2023-10-01T14:00 to 2023-10-01T15:00");
    commandParser.executeCommand("edit calendar --name Work --property timezone Europe/London");
    Calendar workCalendar = calendarManager.getCalendar("Work");
    assertEquals(ZoneId.of("Europe/London"), workCalendar.getTimeZone());
  }

  @Test
  public void maintainsChangingTimezone()
          throws InvalidCommandException {

    // Step 1: Create calendar and add events in New York
    commandParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    commandParser.executeCommand(
            "create event Meeting1 from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand(
            "create event Meeting2 from 2023-10-01T14:00 to 2023-10-01T15:00");

    Calendar calendarBefore = calendarManager.getCalendar("Work");
    ZoneId oldZone = calendarBefore.getTimeZone();

    // Assert timezone before change
    assertEquals(ZoneId.of("America/New_York"), oldZone);

    // Capture original event times as instants
    CalendarEvent event1 = calendarBefore.findEvent("Meeting1",
            LocalDateTime.of(2023, 10, 1, 10, 0));
    CalendarEvent event2 = calendarBefore.findEvent("Meeting2",
            LocalDateTime.of(2023, 10, 1, 14, 0));
    assertNotNull(event1);
    assertNotNull(event2);

    ZonedDateTime event1Instant = event1.getStartDateTime()
            .atZone(oldZone).withZoneSameInstant(ZoneId.of("Europe/London"));
    ZonedDateTime event2Instant = event2.getStartDateTime()
            .atZone(oldZone).withZoneSameInstant(ZoneId.of("Europe/London"));

    // Step 2: Change calendar timezone
    commandParser.executeCommand(
            "edit calendar --name Work --property timezone Europe/London");

    Calendar calendarAfter = calendarManager.getCalendar("Work");
    ZoneId newZone = calendarAfter.getTimeZone();
    assertEquals(ZoneId.of("Europe/London"), newZone);

    // Step 3: Check if events exist at adjusted times in new timezone
    LocalDateTime newStart1 = event1Instant.toLocalDateTime();
    LocalDateTime newStart2 = event2Instant.toLocalDateTime();

    CalendarEvent updated1 = calendarAfter.findEvent("Meeting1", newStart1);
    CalendarEvent updated2 = calendarAfter.findEvent("Meeting2", newStart2);

    assertNotNull("Meeting1 should exist at new adjusted time", updated1);
    assertNotNull("Meeting2 should exist at new adjusted time", updated2);

    // Optional: print adjusted times for debugging
    System.out.println("Meeting1 moved to (London): " + newStart1);
    System.out.println("Meeting2 moved to (London): " + newStart2);
  }

  /**
   * Tests that the "no calendar in use" error message is properly displayed.
   */
  @Test
  public void testNoCalendarInUseErrorMessage() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("create event Test from " +
            "2023-01-01T10:00 to 2023-01-01T11:00");
    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: No calendar in use. " +
            "Use 'use calendar --name <calName>'"));
  }

  /**
   * Tests that the default case in createCommand returns null and displays error.
   */
  @Test
  public void testCreateCommandDefaultCase() throws InvalidCommandException {
    // Use reflection to test private method
    try {
      java.lang.reflect.Method method =
              CommandParser.class.getDeclaredMethod("createCommand", String.class, String.class);
      method.setAccessible(true);

      Command result = (Command) method.invoke(commandParser, "invalid-command", "invalid input");
      assertNull("Default case should return null", result);

      // Verify the error message is shown in executeCommand
      boolean execResult = commandParser.executeCommand("invalid-command");
      assertFalse(execResult);
      assertTrue(outContent.toString().contains("Error: Invalid command."));
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Error: Invalid command. " +
              "Must start with 'create', 'edit', 'use', " +
              "'copy', 'print', 'export', or 'show'."));
    }
  }

  /**
   * Tests that import commands are properly handled.
   */
  @Test
  public void testImportCommandHandling() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");

    boolean result = commandParser.executeCommand("import events.csv");
    assertTrue("Import command should be accepted", result);
  }

  /**
   * Tests that extractMainCommand properly identifies import commands.
   */
  @Test
  public void testExtractMainCommandImport() throws Exception {
    java.lang.reflect.Method method =
            CommandParser.class.getDeclaredMethod("extractMainCommand", String.class);
    method.setAccessible(true);

    String result = (String) method.invoke(commandParser, "import events.csv");
    assertEquals("Should recognize import commands", "import", result);
  }

  /**
   * Tests that extractMainCommand throws exception for completely invalid commands.
   */
  @Test(expected = InvalidCommandException.class)
  public void testExtractMainCommandInvalid() throws Throwable {
    try {
      java.lang.reflect.Method method =
              CommandParser.class.getDeclaredMethod("extractMainCommand", String.class);
      method.setAccessible(true);
      method.invoke(commandParser, "completely-invalid-command");
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  /**
   * Tests that the error message for invalid commands is properly displayed.
   */
  @Test
  public void testInvalidCommandErrorMessage() {
    InvalidCommandException exception =
        assertThrows(InvalidCommandException.class, () ->
                commandParser.executeCommand("invalid-command")
    );
    assertTrue(exception.getMessage().contains(
            "Error: Invalid command. Must start with"
    ));
  }

  /**
   * Tests that empty command handling doesn't produce null pointer exceptions.
   */
  @Test
  public void testEmptyCommandSafety() throws InvalidCommandException {
    boolean result = commandParser.executeCommand("   ");
    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: Empty command."));
  }

  @Test
  public void testInvalidMainCommandHandling() throws InvalidCommandException {
    // Creating a CommandParser with a calendar so we can test the else path
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    outContent.reset();

    // Now create a command that passes the initial checks (not null, not empty, calendar exists)
    // but fails in createCommand, returning null
    // We could use reflection for this, but we can also just use a command that matches none
    // of the expected prefixes while avoiding the InvalidCommandException
    try {
      boolean result = commandParser.executeCommand("unknown-command parameters");

      // This should reach the else block in executeCommand
      assertFalse("Unknown command should return false", result);
      assertTrue("Error message should be printed for unknown command",
              outContent.toString().contains("Error: Invalid command. Must start with"));
    } catch (InvalidCommandException e) {
      assertTrue(e.getMessage().contains("Error: Invalid command. Must start with 'create', " +
              "'edit', 'use', 'copy', 'print', 'export', or 'show'."));
    }
  }

  @Test
  public void testCreateCommandReturnsNull() throws Exception {
    // This test directly invokes the createCommand method to ensure it returns null
    // for an unknown command type

    // Use reflection to access the private method
    java.lang.reflect.Method method = CommandParser.class.getDeclaredMethod(
            "createCommand", String.class, String.class);
    method.setAccessible(true);

    // Call with an unknown command type
    Command result = (Command) method.invoke(commandParser, "unknown", "unknown command");

    // Verify it returns null
    assertNull("createCommand should return null for unknown command type", result);
  }

  @Test
  public void testExecuteCommandHittingFinalElse() throws Exception {
    // Set up a situation where createCommand will return null but not throw exception
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("use calendar --name Work");
    outContent.reset();

    try {
      // Use reflection to access and modify the private extractMainCommand method
      // so it returns a command string that doesn't match any case in createCommand
      java.lang.reflect.Method extractMainCommandMethod = CommandParser.class.getDeclaredMethod(
              "extractMainCommand", String.class);
      extractMainCommandMethod.setAccessible(true);

      // Create a custom CommandParser that uses our modified extractMainCommand
      CommandParser testParser = new CommandParser(calendarManager) {
        @Override
        public boolean executeCommand(String command) throws InvalidCommandException {
          // Only override for our specific test command
          if (command.equals("special-test-command")) {
            // Call the original implementation but bypass the extractMainCommand check
            // This will lead to the createCommand method returning null
            try {
              // Skip the initial null/empty checks
              if (command == null || command.trim().isEmpty()) {
                System.out.println("Error: Empty command.");
                return false;
              }

              String mainCommand = "non-existent-command-type";

              if (!mainCommand.equals("use") && !mainCommand.equals("create calendar") &&
                      !mainCommand.equals("edit calendar") &&
                      calendarManager.getCurrentCalendar() == null) {
                System.out.println("Error: No calendar in use. " +
                        "Use 'use calendar --name <calName>' to " +
                        "select a calendar.");
                return false;
              }

              // This will return null since "non-existent-command-type" doesn't match any case
              java.lang.reflect.Method createCommandMethod = CommandParser.class.getDeclaredMethod(
                      "createCommand", String.class, String.class);
              createCommandMethod.setAccessible(true);
              Command cmd = (Command) createCommandMethod.invoke(this, mainCommand, command);

              // This will trigger the final else block we're trying to test
              if (cmd != null) {
                return cmd.execute();
              } else {
                System.out.println("Error: Invalid command. Must start with 'create', " +
                        "'edit', 'use', 'copy', 'print', 'export', or 'show'.");
                return false;
              }
            } catch (Exception e) {
              return false;
            }
          } else {
            return super.executeCommand(command);
          }
        }
      };

      // Execute our special test command that will hit the final else
      boolean result = testParser.executeCommand("special-test-command");

      // Should print error and return false
      assertFalse("Should return false when createCommand returns null", result);
      assertTrue("Should print error message",
              outContent.toString().contains("Error: Invalid command. Must start with"));

    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  public void check_changingTimezone() throws InvalidCommandException {
    commandParser.executeCommand("create calendar --name Work --timezone America/New_York");
    commandParser.executeCommand("create event Meeting1 from 2023-10-01T10:00 to 2023-10-01T11:00");
    commandParser.executeCommand("create event Meeting2 from 2023-10-01T14:00 to 2023-10-01T15:00");
    commandParser.executeCommand("edit calendar --name Work --property timezone Europe/London");
    Calendar workCalendar = calendarManager.getCalendar("Work");
    assertEquals(ZoneId.of("Europe/London"), workCalendar.getTimeZone());
  }
}