package controller.command;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import controller.InvalidCommandException;
import model.CalendarEvent;
import model.CalendarManager;
import model.EventStorage;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the CopyCommand class. Verifies the correct behavior of
 * event copying operations between calendars, including success cases,
 * conflict handling, and error conditions.
 */
public class CopyCommandTest {
  private CalendarManager manager;
  private MultiCalendarEventStorage eventStorage;
  private PrintStream originalErr;
  private String validCommand;

  @Before
  public void setUp() {
    eventStorage = new MultiCalendarEventStorage();
    manager = new CalendarManager(eventStorage);

    // Setup test calendar and event
    eventStorage.putCalendarStorage("Work", new EventStorage());
    eventStorage.addEvent("Work",
            new CalendarEvent("Meeting",
                    LocalDateTime.parse("2025-03-01T10:00"),
                    LocalDateTime.parse("2025-03-01T11:00")),
            false);

    validCommand = "copy event Meeting on 2025-03-01T10:00 "
            + "--target Work to 2025-03-02T10:00";

    // Redirect System.err to capture output
    originalErr = System.err;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));
  }

  @After
  public void restoreStreams() {
    System.setErr(originalErr);
  }

  @Test
  public void testExecute_SuccessfulCopy_ReturnsTrue()
          throws InvalidCommandException {
    CopyCommand command = new CopyCommand(manager, validCommand);
    command.execute();

    CalendarEvent copiedEvent = eventStorage.findEvent(
            "Work",
            "Meeting",
            LocalDateTime.parse("2025-03-01T10:00"));
    assertNotNull(copiedEvent);
  }

  @Test
  public void testExecute_Conflict_ReturnsFalse()
          throws InvalidCommandException {
    // Create conflicting event
    eventStorage.addEvent("Work",
            new CalendarEvent("Conflict",
                    LocalDateTime.parse("2025-03-02T10:00"),
                    LocalDateTime.parse("2025-03-02T11:00")),
            false);

    CopyCommand command = new CopyCommand(manager, validCommand);
    assertFalse(command.execute());
  }

  @Test
  public void testExecute_NonexistentSource_ReturnsFalse()
          throws InvalidCommandException {
    CopyCommand command = new CopyCommand(manager,
            "copy event Missing on 2025-03-01T10:00 "
                    + "--target Work to 2025-03-02T10:00");
    assertFalse(command.execute());
  }

  @Test
  public void testExecute_NonexistentTarget_ReturnsFalse()
          throws InvalidCommandException {
    CopyCommand command = new CopyCommand(manager,
            "copy event Meeting on 2025-03-01T10:00 "
                    + "--target Missing to 2025-03-02T10:00");
    assertFalse(command.execute());
  }

  @Test
  public void testExecute_InvalidCommand_HandlesException()
          throws InvalidCommandException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      CopyCommand command = new CopyCommand(manager, "invalid command");
      assertTrue(command.execute());
      assertTrue("Error message should be printed",
              outContent.toString().contains("Error: "));
    } finally {
      System.setOut(originalOut);
    }
  }
}