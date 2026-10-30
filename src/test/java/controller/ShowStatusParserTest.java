package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import model.CalendarEvent;
import model.EventStorage;
import view.text.CalendarPrinter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the ShowStatusParser class to ensure it correctly parses and executes
 * commands for checking the user's busy/available status at a specific date and
 * time. It also verifies error handling for invalid or incomplete commands.
 */
public class ShowStatusParserTest {

  private EventStorage eventStorage;
  private CalendarPrinter calendarPrinter;
  private ShowStatusParser showStatusParser;

  private ByteArrayOutputStream outContent;

  @Before
  public void setUp() {
    eventStorage = new EventStorage();
    calendarPrinter = new CalendarPrinter(eventStorage);
    showStatusParser = new ShowStatusParser(eventStorage);

    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent)); // Capture the output
  }

  @Test
  public void testExecuteCommand_ValidCommand_UserBusy() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", dateTime,
            dateTime.plusHours(1), "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false); // Add event to storage

    String command = "show status on 2023-10-01T10:00";
    boolean result = showStatusParser.executeCommand(command);
    assertTrue(result);
    assertTrue(outContent.toString().contains("User is busy on 2023-10-01T10:00"));
  }

  @Test
  public void testExecuteCommand_ValidCommand_UserAvailable() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    String command = "show status on 2023-10-01T10:00";
    boolean result = showStatusParser.executeCommand(command);
    assertTrue(result);
    assertTrue(outContent.toString().contains("User is available on 2023-10-01T10:00"));
  }

  @Test
  public void testExecuteCommand_InvalidDateTimeFormat() {
    String command = "show status on 2023/10/01 10:00";
    boolean result = showStatusParser.executeCommand(command);
    assertFalse(result);
    assertTrue(outContent.toString().contains(
            "Invalid date/time format."));
  }

  @Test
  public void testExecuteCommand_InvalidCommand() {
    String command = "show status 2023-10-01T10:00";
    boolean result = showStatusParser.executeCommand(command);
    assertFalse(result);
    assertTrue(outContent.toString().contains(
            "Invalid command format."));
  }

  @Test
  public void testShowStatusOnDateTime_UserAvailable() {
    CalendarEvent event = new CalendarEvent("Test Event",
            LocalDateTime.of(2025, 3, 6, 9, 0),
            LocalDateTime.of(2025, 3, 6, 10, 0));
    eventStorage.addEvent(event, false); // Add event to storage
    outContent.reset();

    calendarPrinter.showStatusOnDateTime(LocalDateTime.of(2025, 3, 6,
            10, 0));
    String expectedOutput = "User is available on 2025-03-06T10:00\n";
    assertEquals(expectedOutput.replaceAll("\\r\\n?", "\n"),
            outContent.toString().replaceAll("\\r\\n?", "\n"));
  }

  @Test
  public void testShowStatusOnDateTime_UserBusy() {
    CalendarEvent event = new CalendarEvent("Test Event",
            LocalDateTime.of(2025, 3, 6, 9, 30),
            LocalDateTime.of(2025, 3, 6, 10, 30));
    eventStorage.addEvent(event, false); // Add event to storage

    calendarPrinter.showStatusOnDateTime(LocalDateTime.of(2025, 3, 6,
            10, 0));
    String expectedOutput = "User is busy on 2025-03-06T10:00\n";
    assertTrue(outContent.toString().replaceAll("\\r\\n?", "\n")
            .contains(expectedOutput.replaceAll("\\r\\n?", "\n")));
  }

  @Test
  public void testShowStatusOnDateTime_NoEvents() {
    calendarPrinter.showStatusOnDateTime(LocalDateTime.of(2025, 3, 6,
            10, 0));
    String expectedOutput = "User is available on 2025-03-06T10:00\n";
    assertEquals(expectedOutput.replaceAll("\\r\\n?", "\n"),
            outContent.toString().replaceAll("\\r\\n?", "\n"));
  }
}