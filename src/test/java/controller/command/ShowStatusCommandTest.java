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
 * Unit tests for {@link ShowStatusCommand} verifying calendar availability status checks.
 */
public class ShowStatusCommandTest {
  private EventStorage eventStorage;
  private String busyCommand;
  private String availableCommand;
  private String invalidCommand;
  private ByteArrayOutputStream outContent;

  @Before
  public void setUp() {
    PrintStream originalOut;
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    eventStorage = new EventStorage();
    // Add test event from 10:00-11:00
    eventStorage.addEvent(new CalendarEvent("Meeting",
            LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00")), false);

    busyCommand = "show status on 2025-03-01T10:30";
    availableCommand = "show status on 2025-03-01T09:00";
    invalidCommand = "show status invalid-format";
  }

  @Test
  public void testExecute_BusyStatus_ReturnsTrue() throws InvalidCommandException {
    ShowStatusCommand command = new ShowStatusCommand(eventStorage, busyCommand);
    assertTrue("Should return true when busy", command.execute());
  }

  @Test
  public void testExecute_AvailableStatus_ReturnsFalse() throws InvalidCommandException {
    ShowStatusCommand command = new ShowStatusCommand(eventStorage, availableCommand);
    assertTrue(command.execute());
    assertTrue(outContent.toString().contains("User is available on 2025-03-01T09:00"));
  }

  @Test
  public void testExecute_InvalidCommand_ThrowsException() throws InvalidCommandException {
    ShowStatusCommand command = new ShowStatusCommand(eventStorage, invalidCommand);
    assertFalse(command.execute());
    assertTrue(outContent.toString().contains("Invalid command format."));
  }
}