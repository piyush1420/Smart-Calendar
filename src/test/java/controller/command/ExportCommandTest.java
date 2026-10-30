package controller.command;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import controller.InvalidCommandException;
import model.EventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the ExportCommand class. Verifies the correct behavior
 * of calendar export operations, including successful exports, empty calendar
 * handling, and proper error handling for invalid paths and commands.
 */
public class ExportCommandTest {
  private EventStorage eventStorage;
  private Path tempFile;

  @Before
  public void setUp() throws IOException {
    eventStorage = new EventStorage();
    tempFile = Files.createTempFile("calendar", ".csv");
    tempFile.toFile().deleteOnExit();
  }

  @Test
  public void testExecute_SuccessfulExportReturnsTrueAndCreatesFile()
          throws InvalidCommandException, IOException {
    String command = "export cal " + tempFile.toString();
    ExportCommand exportCommand = new ExportCommand(eventStorage, command);

    assertTrue("Export should succeed with valid path", exportCommand.execute());
    assertTrue("Exported file should exist", Files.exists(tempFile));
    assertTrue("Exported file should not be empty", Files.size(tempFile) > 0);
  }

  @Test
  public void testExecute_EmptyCalendarExportReturnsTrue()
          throws InvalidCommandException, IOException {
    String command = "export cal " + tempFile.toString();
    ExportCommand exportCommand = new ExportCommand(eventStorage, command);

    assertTrue("Export should succeed with empty calendar", exportCommand.execute());
  }

  @Test
  public void testExecute_InvalidPathReturnsFalse()
          throws InvalidCommandException {
    String command = "export cal /invalid/path/calendar.csv";
    ExportCommand exportCommand = new ExportCommand(eventStorage, command);

    assertFalse("Export should fail with invalid path", exportCommand.execute());
  }

  @Test
  public void testExecute_InvalidCommandFormatReturnsFalse()
          throws InvalidCommandException {
    ExportCommand command = new ExportCommand(eventStorage, "invalid-command");

    assertFalse("Should return false for invalid command format", command.execute());
  }

  @Test
  public void testExecute_ReadOnlyFileReturnsFalse()
          throws InvalidCommandException, IOException {
    tempFile.toFile().setReadOnly();
    String command = "export cal " + tempFile.toString();
    ExportCommand exportCommand = new ExportCommand(eventStorage, command);

    assertFalse("Export should fail with read-only file", exportCommand.execute());
  }
}