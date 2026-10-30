package controller.command;

import org.junit.Before;
import org.junit.Test;

import controller.InvalidCommandException;
import model.EventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the CreateCommand class. Verifies the correct behavior
 * of event creation operations, including success cases, conflict handling,
 * and invalid command formats.
 */
public class CreateCommandTest {
  private EventStorage eventStorage;
  private String validCommand;
  private String invalidCommand;

  @Before
  public void setUp() {
    eventStorage = new EventStorage();
    validCommand = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00";
    invalidCommand = "create event invalid-command";
  }

  @Test
  public void testExecute_SuccessfulEventCreationReturnsTrue()
          throws InvalidCommandException {
    CreateCommand command = new CreateCommand(eventStorage, validCommand);
    assertTrue("Command should return true for successful event creation",
            command.execute());
  }

  @Test
  public void testExecute_InvalidCommandFormatReturnsFalse()
          throws InvalidCommandException {
    CreateCommand command = new CreateCommand(eventStorage, invalidCommand);
    assertFalse("Command should return false for invalid format",
            command.execute());
  }

  @Test
  public void testExecute_AllDayEventCreationReturnsTrue()
          throws InvalidCommandException {
    String allDayCommand = "create event Holiday on 2025-03-01T00:00";
    CreateCommand command = new CreateCommand(eventStorage, allDayCommand);
    assertTrue("Command should return true for all-day event creation",
            command.execute());
  }

  @Test
  public void testExecute_ConflictingEventReturnsFalse()
          throws InvalidCommandException {
    String conflictingCommand = "create event Conflict from 2025-03-01T10:30 to 2025-03-01T11:30";
    new CreateCommand(eventStorage, validCommand).execute();
    CreateCommand conflictCommand = new CreateCommand(eventStorage, conflictingCommand);
    assertTrue(
            conflictCommand.execute());
  }
}