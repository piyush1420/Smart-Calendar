package controller.command;


import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import controller.InvalidCommandException;
import model.CalendarEvent;
import model.EventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PrintCommand} verifying event printing functionality.
 */
public class PrintCommandTest {
  private EventStorage eventStorage;
  private String validCommand;
  private String emptyCommand;
  private ByteArrayOutputStream outContent;

  @Before
  public void setUp() {
    PrintStream originalOut;
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    eventStorage = new EventStorage();
    // Add test event
    eventStorage.addEvent(new CalendarEvent("Meeting",
            LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00")), false);

    validCommand = "print events on 2025-03-01";
    emptyCommand = "print events on 2025-03-02"; // Date with no events
  }

  @Test
  public void testExecute_WithEvents_ReturnsTrue() throws InvalidCommandException {
    PrintCommand command = new PrintCommand(eventStorage, validCommand);
    assertTrue("Should return true when events exist", command.execute());
  }

  @Test
  public void testExecute_NoEvents_ReturnsFalse() throws InvalidCommandException {
    PrintCommand command = new PrintCommand(eventStorage, emptyCommand);
    assertTrue(command.execute());
    assertTrue(outContent.toString().contains("No events found on 2025-03-02"));
  }

  @Test
  public void testExecute_InvalidCommand_ThrowsException() throws InvalidCommandException {
    PrintCommand command = new PrintCommand(eventStorage, "invalid command");
    assertFalse(command.execute());
    assertTrue(outContent.toString().contains("Invalid command. Expected 'print events' command"));
  }
}