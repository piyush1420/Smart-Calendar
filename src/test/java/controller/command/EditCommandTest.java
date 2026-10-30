package controller.command;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import controller.InvalidCommandException;
import model.CalendarEvent;
import model.EventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the EditCommand class. Verifies the correct behavior
 * of event editing operations, including successful edits, conflict handling,
 * and proper error handling for invalid inputs.
 */
public class EditCommandTest {
  private EventStorage eventStorage;
  private String validEditCommand;
  private String invalidCommand;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private PrintStream originalOut;

  @Before
  public void setUp() {
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    eventStorage = new EventStorage();
    // Initialize test event locally since it's only used here
    CalendarEvent testEvent = new CalendarEvent("Meeting",
            LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"));
    eventStorage.addEvent(testEvent, false);

    validEditCommand = "edit events subject Meeting from 2025-03-01T10:00 with NewMeeting";
    invalidCommand = "edit event invalid-command-format";
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void testExecute_SuccessfulEventEditReturnsTrue()
          throws InvalidCommandException {
    EditCommand command = new EditCommand(eventStorage, validEditCommand);
    assertTrue("Should return true for successful edit", command.execute());
    assertNotNull("Event should exist with new name",
            eventStorage.findEvent("NewMeeting",
                    LocalDateTime.parse("2025-03-01T10:00")));
  }

  @Test
  public void testExecute_ConflictEditHandlingReturnsTrue()
          throws InvalidCommandException {
    // Add conflicting event
    eventStorage.addEvent(new CalendarEvent("Conflict",
                    LocalDateTime.parse("2025-03-01T11:00"),
                    LocalDateTime.parse("2025-03-01T12:00")),
            true);

    String extendCommand = "edit event subject Meeting from 2025-03-01T10:00 "
            + "to 2025-03-01T11:30 with New Meeting";

    EditCommand command = new EditCommand(eventStorage, extendCommand);
    assertTrue("Should return true when handling conflict", command.execute());
    assertTrue("Should indicate edit was processed",
            outContent.toString().contains("Edited event:"));
  }

  @Test
  public void testExecute_InvalidCommandFormatReturnsFalse()
          throws InvalidCommandException {
    EditCommand command = new EditCommand(eventStorage, invalidCommand);
    assertFalse("Should return false for invalid format", command.execute());
    assertTrue("Should print error message",
            outContent.toString().contains("Invalid command format."));
  }

  @Test
  public void testExecute_NonexistentEventHandlingReturnsTrue()
          throws InvalidCommandException {
    String nonExistentCommand = "edit events subject Missing from 2025-03-01T12:00 "
            + "with NewName";
    EditCommand command = new EditCommand(eventStorage, nonExistentCommand);
    assertFalse("Should return false when handling missing event", command.execute());
    assertTrue("Should print error message for missing event",
            outContent.toString().contains("No events found matching: Missing starting at or " +
                    "after 2025-03-01T12:00"));
  }

  @Test
  public void testExecute_HandlesIllegalArgumentException() throws InvalidCommandException {
    // Mock or create a scenario that will cause IllegalArgumentException
    String invalidPropertyCommand = "edit event subject Meeting from 2025-03-01T10:00 with ";
    EditCommand command = new EditCommand(eventStorage, invalidPropertyCommand);
    assertFalse("Should return true when handling IllegalArgumentException", command.execute());
    assertTrue("Should print error message for invalid argument",
            outContent.toString().contains("Invalid format for editing a single event."));
  }

  @Test
  public void testExecute_HandlesInvalidCommandException() throws InvalidCommandException {
    // Mock or create a scenario that will cause InvalidCommandException
    String invalidFormatCommand = "edit invalid format";
    EditCommand command = new EditCommand(eventStorage, invalidFormatCommand);
    assertFalse("Should return false when handling InvalidCommandException", command.execute());
    assertTrue("Should print error message for invalid command",
            outContent.toString().contains("Invalid command format."));
  }

  @Test
  public void testExecute_ReturnsFalseOnIllegalArgumentException() throws InvalidCommandException {
    // Command missing required "with" keyword
    String invalidCommand = "edit event subject Meeting from 2025-03-01T10:00 to 2025-03-01T11:00";
    EditCommand command = new EditCommand(eventStorage, invalidCommand);
    assertFalse(command.execute());
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));
  }

  @Test
  public void testExecute_HandlesMissingProperty() throws InvalidCommandException {
    // Command with empty property
    String invalidCommand = "edit event \"\" Meeting from 2025-03-01T10:00 with NewName";
    EditCommand command = new EditCommand(eventStorage, invalidCommand);
    assertFalse(command.execute());
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));
  }

  @Test
  public void testExecute_HandlesInvalidBackticks() throws InvalidCommandException {
    // Command with unclosed backtick
    String invalidCommand = "edit events subject Meeting `NewName";
    EditCommand command = new EditCommand(eventStorage, invalidCommand);
    assertTrue(command.execute());
    assertTrue(outContent.toString().contains("Error : Missing closing backtick (`) for" +
            " new value."));
  }
}