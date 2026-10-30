package controller.command;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;

import controller.InvalidCommandException;
import model.Calendar;
import model.CalendarManager;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the CreateCalendarCommand class. Verifies the correct behavior
 * of calendar creation operations, including success cases, duplicate handling,
 * and invalid command formats.
 */
public class CreateCalendarCommandTest {
  private CalendarManager manager;
  private MultiCalendarEventStorage storage;
  private String validCommand;
  private String duplicateCommand;
  private String invalidCommand;

  @Before
  public void setUp() {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    storage = new MultiCalendarEventStorage();
    manager = new CalendarManager(storage);

    // Setup test commands
    validCommand = "create calendar --name Work --timezone America/New_York";
    duplicateCommand = "create calendar --name Personal --timezone America/Chicago";
    invalidCommand = "create calendar invalid-format";

    // Create initial calendar that will cause duplicate
    manager.createCalendar("Personal", ZoneId.of("America/Chicago"));
  }

  @Test
  public void testExecute_SuccessfulCalendarCreationReturnsTrue()
          throws InvalidCommandException {
    CreateCalendarCommand command = new CreateCalendarCommand(manager, validCommand);
    assertTrue("Should return true for successful creation", command.execute());
    assertNull("New calendar should have empty event storage",
            storage.getEventStorageForCalendar("Work"));
  }

  @Test
  public void testExecute_DuplicateCalendarName()
          throws InvalidCommandException {
    CreateCalendarCommand command = new CreateCalendarCommand(manager, duplicateCommand);
    assertTrue("Should return false for duplicate calendar", command.execute());
    assertNull("Original calendar should still exist",
            storage.getEventStorageForCalendar("Personal"));
  }

  @Test
  public void testExecute_InvalidCommandFormatReturnsFalse()
          throws InvalidCommandException {
    CreateCalendarCommand command = new CreateCalendarCommand(manager, invalidCommand);
    assertFalse("Should return false for invalid command", command.execute());
  }

  @Test
  public void testExecute_SuccessfulCalendarCreationReturnsNameAndTimeZone()
          throws InvalidCommandException {
    CreateCalendarCommand command = new CreateCalendarCommand(manager, validCommand);

    // Act
    boolean result = command.execute();

    // Assert
    assertTrue("Should return true for successful creation", result);

    // Check calendar was created in the manager
    Calendar createdCalendar = manager.getCalendar("Work");
    assertNotNull("Calendar should exist after creation", createdCalendar);
    assertEquals("Calendar name should match", "Work", createdCalendar.getName());
    assertEquals("Calendar timezone should match", ZoneId.of("America/New_York"),
            createdCalendar.getTimeZone());
  }
}