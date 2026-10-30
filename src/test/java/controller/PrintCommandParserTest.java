package controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import model.CalendarEvent;
import model.EventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the PrintCommandParser class to ensure it correctly parses and executes
 * commands for printing calendar events on a specific date or within a date
 * range. It also verifies error handling for invalid or incomplete commands.
 */
public class PrintCommandParserTest {

  private EventStorage eventStorage;
  private PrintCommandParser printCommandParser;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private PrintStream originalOut;

  @Before
  public void setUp() {
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    eventStorage = new EventStorage();
    printCommandParser = new PrintCommandParser(eventStorage);
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void testExecuteCommand_PrintEventsOnDate_ValidCommand() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", dateTime,
            dateTime.plusHours(1), "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false); // Add event to storage

    String command = "print events on 2023-10-01";
    boolean result = printCommandParser.executeCommand(command);
    assertTrue(result);
  }

  @Test
  public void testExecuteCommand_PrintEventsOnDate_NoEvents() {
    String command = "print events on 2023-10-01";
    boolean result = printCommandParser.executeCommand(command);
    assertTrue(result);
  }

  @Test
  public void testExecuteCommand_PrintEventsInRange_ValidCommand() {
    LocalDateTime startDateTime = LocalDateTime.of(2023, 10, 1, 10,
            0);
    LocalDateTime endDateTime = LocalDateTime.of(2023, 10, 1, 11,
            0);
    CalendarEvent event = new CalendarEvent("Meeting", startDateTime,
            endDateTime, "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false); // Add event to storage

    String command = "print events from 2023-10-01T10:00 to 2023-10-01T11:00";
    boolean result = printCommandParser.executeCommand(command);
    assertTrue(result);
  }

  @Test
  public void testExecuteCommand_PrintEventsInRange_NoEvents() {
    String command = "print events from 2023-10-01T10:00 to 2023-10-01T11:00";
    boolean result = printCommandParser.executeCommand(command);
    assertTrue(result);
  }

  @Test
  public void testExecuteCommand_InvalidCommand() {
    String command = "print invalid command";
    boolean result = printCommandParser.executeCommand(command);
    assertFalse(result);
  }

  @Test
  public void testExecuteCommand_InvalidDateFormat() {
    String command = "print events on 2023/10/01";
    boolean result = printCommandParser.executeCommand(command);
    assertFalse(result);
  }

  @Test
  public void testExecuteCommand_InvalidDateTimeFormat() {
    String command = "print events from 2023/10/01 10:00 to 2023/10/01 11:00";
    boolean result = printCommandParser.executeCommand(command);
    assertFalse(result);
  }

  @Test
  public void testExecuteCommand_InvalidRangeFormat() {
    String command = "print events from 2023-10-01 10:00";
    boolean result = printCommandParser.executeCommand(command);
    assertFalse(result);
  }

  @Test
  public void testExecuteCommand_InvalidCommand_ReturnsFalseAndPrintsError() {
    String command = "print invalid command";
    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for invalid command", result);
    assertTrue("Should print error message for invalid command",
            outContent.toString().contains("Invalid command. Expected 'print events' command."));
  }

  @Test
  public void testExecuteCommand_InvalidPrintFormat_ReturnsFalseAndPrintsError() {
    String command = "print events invalid-format";
    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for invalid print format", result);
    assertTrue("Should print error message for invalid format",
            outContent.toString().contains("Invalid print command format."));
  }

  @Test
  public void testExecuteCommand_MissingDate_ReturnsFalseAndPrintsError() {
    String command = "print events on";
    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for missing date", result);
    assertTrue("Should print error message for invalid date format",
            outContent.toString().contains("Invalid date format"));
  }

  @Test
  public void testExecuteCommand_MissingRange_ReturnsFalseAndPrintsError() {
    String command = "print events from";
    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for missing range", result);
    assertTrue("Should print error message for invalid range format",
            outContent.toString().contains("Invalid range format"));
  }

  @Test
  public void testHandlePrintEventsOnDate_NullDateString_TriggersGenericException() {
    // This command will pass the initial validation but fail in date parsing
    String command = "print events on ";

    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for parsing failure", result);
    assertTrue("Should print generic error message",
            outContent.toString().contains("Invalid date format. Expected format: yyyy-MM-dd"));
  }

  @Test
  public void testHandlePrintEventsInRange_InvalidRangeFormat_ReturnsFalseAndPrintsError() {
    String command = "print events from 2023-10-01T10:00"; // Missing second date
    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for invalid range format", result);
    assertTrue("Should print error message for invalid range format",
            outContent.toString().contains("Invalid range format"));
  }

  @Test
  public void testHandlePrintEventsInRange_InvalidDateTimeFormat_ReturnsFalseAndPrintsError() {
    String command = "print events from 2023/10/01 10:00 to 2023/10/01 11:00";
    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for invalid date-time format", result);
    assertTrue("Should print error message for invalid date-time format",
            outContent.toString().contains("Invalid date-time format"));
  }

  @Test
  public void testHandlePrintEventsInRange_NullStorage_TriggersGenericException() {
    // Create a command that will pass initial validation
    String command = "print events from 2023-10-01T10:00 to 2023-10-01T11:00";

    // Create a parser with null storage which should cause an exception
    PrintCommandParser testParser = new PrintCommandParser(null);

    boolean result = testParser.executeCommand(command);

    assertFalse("Should return false for generic exception", result);
    assertTrue("Should print generic error message",
            outContent.toString().contains("Error processing command: null"));
  }

  @Test
  public void testHandlePrintEventsOnDate_GeneralException_ReturnsFalseAndPrintsError() {
    // Create a special parser that will throw a general exception
    PrintCommandParser specialParser = new PrintCommandParser(null) {
      @Override
      public boolean executeCommand(String command) {
        // Override to call handlePrintEventsOnDate directly
        if (command.startsWith("print events on")) {
          try {
            // Use reflection to access the private method
            java.lang.reflect.Method method = PrintCommandParser.class
                    .getDeclaredMethod("handlePrintEventsOnDate", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(this, command);
          } catch (Exception e) {
            return false;
          }
        }
        return super.executeCommand(command);
      }
    };
    String command = "print events on 2023-10-01";
    boolean result = specialParser.executeCommand(command);

    assertFalse("Should return false for general exception", result);
    assertTrue("Should print error message with exception details",
            outContent.toString().contains("Error processing command:"));
  }

  @Test
  public void testHandlePrintEventsOnDate_IndexOutOfBoundsException() {
    // Command with no space after "on" will cause substring to fail
    String command = "print events on '";
    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for index out of bounds", result);
    assertTrue("Should print error message",
            outContent.toString().contains("Invalid date format. Expected format: yyyy-MM-dd"));
  }

  @Test
  public void testHandlePrintEventsOnDate_EmptyDateString() {
    // Command with just a space after "on" will give an empty date string
    // which will trigger a DateTimeParseException
    String command = "print events on ";
    boolean result = printCommandParser.executeCommand(command);

    assertFalse("Should return false for empty date string", result);
    assertTrue("Should print date format error message",
            outContent.toString().contains("Invalid date format"));
  }


}