package controller.command;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;

import controller.InvalidCommandException;
import model.CalendarManager;
import model.EventStorage;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the EditCalendarCommand class. Verifies the correct behavior
 * of calendar editing operations, including successful edits, error handling
 * for invalid inputs, and proper management of calendar properties.
 */
public class EditCalendarCommandTest {
  private CalendarManager manager;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;
  private String validCommand;
  private String invalidCommand;

  @Before
  public void setUp() {
    // Initialize storage locally since it's only used here
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    manager = new CalendarManager(storage);

    // Create initial calendar
    storage.putCalendarStorage("Work", new EventStorage());
    manager.createCalendar("Work", ZoneId.of("America/New_York"));

    // Set up commands
    validCommand = "edit calendar --name Work --property timezone America/New_York";
    invalidCommand = "edit calendar invalid-command";

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
  public void testExecute_SuccessfulTimezoneEditReturnsTrue()
          throws InvalidCommandException {
    EditCalendarCommand command = new EditCalendarCommand(manager, validCommand);
    assertTrue("Should return true for successful timezone edit",
            command.execute());
  }

  @Test
  public void testExecute_InvalidTimezonePrintsErrorMessage()
          throws InvalidCommandException {
    String invalidTzCommand = "edit calendar --name Work --property timezone Invalid/Timezone";
    EditCalendarCommand command = new EditCalendarCommand(manager, invalidTzCommand);

    assertTrue("Should return true after handling error", command.execute());
    assertTrue("Should print error message for invalid timezone",
            outContent.toString().contains("Error: "));
  }

  @Test
  public void testExecute_NonexistentCalendarPrintsErrorMessage()
          throws InvalidCommandException {
    String nonExistentCommand = "edit calendar --name Missing --property timezone America/Chicago";
    EditCalendarCommand command = new EditCalendarCommand(manager, nonExistentCommand);

    assertTrue("Should return true after handling error", command.execute());
    assertTrue("Should print error message for missing calendar",
            outContent.toString().contains("Error: "));
  }

  @Test
  public void testExecute_InvalidCommandFormatPrintsErrorMessage()
          throws InvalidCommandException {
    EditCalendarCommand command = new EditCalendarCommand(manager, invalidCommand);

    assertFalse("Should return false for invalid command format",
            command.execute());
    assertTrue("Should print error message for invalid format",
            outContent.toString().contains("Error:"));
  }

  @Test
  public void testExecute_InvalidTimezoneHandlingReturnsTrue()
          throws InvalidCommandException {
    String command = "edit calendar --name Work --property timezone Invalid/Zone";
    EditCalendarCommand cmd = new EditCalendarCommand(manager, command);
    assertTrue("Should return true when handling invalid timezone",
            cmd.execute());
  }
}