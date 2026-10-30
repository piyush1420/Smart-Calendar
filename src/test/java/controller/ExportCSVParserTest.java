package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import model.EventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the ExportCSVParser class to ensure it correctly parses and executes
 * commands for exporting calendar events to a CSV file. It also verifies error
 * handling for invalid commands and file paths.
 */
public class ExportCSVParserTest {

  private ExportCSVParser exportCSVParser;
  private ByteArrayOutputStream outContent;

  @Before
  public void setUp() {
    EventStorage eventStorage;
    eventStorage = new EventStorage();
    exportCSVParser = new ExportCSVParser(eventStorage);
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
  }

  @Test
  public void testExecuteCommand_ValidCommand() {
    String command = "export cal test.csv";
    boolean result = exportCSVParser.executeCommand(command);
    assertTrue(result);
  }

  @Test
  public void testExecuteCommand_InvalidCommand() {
    String command = "export invalid.csv";
    boolean result = exportCSVParser.executeCommand(command);
    assertFalse(result);
  }

  @Test
  public void testExecuteCommand_IOException() {
    String command = "export cal /invalid/path/test.csv";
    boolean result = exportCSVParser.executeCommand(command);
    assertFalse(result);
  }

  @Test
  public void testExecuteCommand_InvalidCommand1() {

    boolean result = exportCSVParser.executeCommand("invalid command");

    assertFalse(result);
    assertTrue(outContent.toString().contains("Invalid command. Expected 'export cal " +
            "<fileName.csv>'."));
  }

  @Test
  public void testExecuteCommand_IOException1() {
    String invalidPath = "/invalid_path/test_calendar.csv"; // Invalid path to force an IOException

    boolean result = exportCSVParser.executeCommand("export cal " + invalidPath);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error exporting calendar to CSV"));
  }
}