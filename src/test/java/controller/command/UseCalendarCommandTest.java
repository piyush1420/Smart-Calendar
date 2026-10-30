package controller.command;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import controller.InvalidCommandException;
import model.CalendarManager;
import model.EventStorage;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the UseCalendarCommand class. Verifies the correct behavior
 * of calendar switching operations, including successful switches, error handling
 * for non-existent calendars, and proper validation of command formats.
 */
public class UseCalendarCommandTest {
  private CalendarManager manager;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;
  private String validCommand;
  private String nonExistentCommand;
  private String invalidCommand;

  @Before
  public void setUp() {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    manager = new CalendarManager(storage);
    storage.putCalendarStorage("Work", new EventStorage());

    // Set up test commands
    validCommand = "use calendar --name Work";
    nonExistentCommand = "use calendar --name Personal";
    invalidCommand = "use calendar invalid-format";

    // Redirect System.out
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void testExecute_SwitchToExistingCalendarReturnsTrue()
          throws InvalidCommandException {
    UseCalendarCommand command = new UseCalendarCommand(manager, validCommand);
    assertTrue("Should return true when switching to existing calendar",
            command.execute());
  }

  @Test
  public void testExecute_NonExistentCalendarPrintsErrorAndReturnsTrue()
          throws InvalidCommandException {
    UseCalendarCommand command = new UseCalendarCommand(manager, nonExistentCommand);
    assertTrue("Should return true after handling missing calendar",
            command.execute());
    assertTrue("Should print error message for missing calendar",
            outContent.toString().contains("Error: "));
  }

  @Test
  public void testExecute_InvalidCommandFormatReturnsFalse()
          throws InvalidCommandException {
    UseCalendarCommand command = new UseCalendarCommand(manager, invalidCommand);
    assertFalse("Should return false for invalid command format",
            command.execute());
    assertTrue("Should print error message for invalid format",
            outContent.toString().contains("Error: "));
  }

  @Test
  public void testExecute_MalformedCommandPrintsErrorAndReturnsFalse()
          throws InvalidCommandException {
    UseCalendarCommand command = new UseCalendarCommand(manager,
            "use calendar --missing-name");
    assertFalse("Should return false for malformed command",
            command.execute());
    assertTrue("Should print error message for malformed command",
            outContent.toString().contains("Error: "));
  }

  @Test
  public void testExecute_EmptyCalendarNamePrintsErrorAndReturnsFalse()
          throws InvalidCommandException {
    UseCalendarCommand command = new UseCalendarCommand(manager,
            "use calendar --name ");
    assertFalse("Should return false for empty calendar name",
            command.execute());
    assertTrue("Should print error message for empty name",
            outContent.toString().contains("Error: "));
  }
}