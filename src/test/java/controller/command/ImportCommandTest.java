package controller.command;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.InvalidCommandException;
import model.CalendarEvent;
import model.EventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for ImportCommand.
 */
public class ImportCommandTest {

  private DummyEventStorage storage;
  private Path tempFile;
  private final String csvHeader =
          "Subject,StartDate,StartTime,EndDate,EndTime,Unused,Description,Location";
  private final String csvLine =
          "Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM,,Team meeting,Office";

  /**
   * Dummy EventStorage that gathers events in a map.
   */
  private class DummyEventStorage extends EventStorage {
    private Map<LocalDateTime, List<CalendarEvent>> eventsMap =
            new HashMap<>();

    @Override
    public void addEvent(CalendarEvent event, boolean recurring) {
      LocalDateTime key = event.getStartDateTime();
      eventsMap.computeIfAbsent(key, k -> new ArrayList<>())
              .add(event);
    }

    @Override
    public Map<LocalDateTime, List<CalendarEvent>> getAllEvents() {
      return eventsMap;
    }
  }

  /**
   * Dummy CommandParser that uses CalendarCSVImporter.
   */
  private class DummyCommandParser extends controller.CommandParser {
    public DummyCommandParser() {
      super(null);
    }

    @Override
    public boolean executeCommand(String command)
            throws InvalidCommandException {
      try {
        new controller.CalendarCSVImporter(storage)
                .importFromCSV(command);
        System.out.println("Events imported from: " + command);
        return true;
      } catch (IOException e) {
        throw new InvalidCommandException(e.getMessage());
      }
    }
  }

  @Before
  public void setUp() throws IOException {
    DummyCommandParser parser;
    storage = new DummyEventStorage();
    parser = new DummyCommandParser();
    // Create a temporary valid CSV file.
    tempFile = Files.createTempFile("testValid", ".csv");
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write(csvHeader);
      writer.newLine();
      writer.write(csvLine);
      writer.newLine();
    }
  }

  @After
  public void tearDown() throws IOException {
    if (tempFile != null && Files.exists(tempFile)) {
      Files.delete(tempFile);
    }
  }

  /**
   * Tests that a valid CSV file is imported correctly.
   */
  @Test
  public void testExecute_ValidImport() {
    String cmdStr = "import " + tempFile.toString();
    ImportCommand cmd = new ImportCommand(storage, cmdStr);
    PrintStream origOut = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    boolean result;
    try {
      result = cmd.execute();
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
      return;
    } finally {
      System.out.flush();
      System.setOut(origOut);
    }
    String outStr = baos.toString().trim();
    assertTrue("Command should succeed", result);
    assertTrue("Output must contain file path",
            outStr.contains("Events imported from: " + tempFile.toString()));
    int total = 0;
    for (List<CalendarEvent> lst : storage.getAllEvents().values()) {
      total += lst.size();
    }
    assertEquals("One event should be imported", 1, total);
    CalendarEvent event = storage.getAllEvents().values()
            .iterator().next().get(0);
    assertEquals("Meeting", event.getSubject());
    LocalDateTime expStart = LocalDateTime.of(2023, 4, 1, 9, 0);
    LocalDateTime expEnd = LocalDateTime.of(2023, 4, 1, 10, 0);
    assertEquals(expStart, event.getStartDateTime());
    assertEquals(expEnd, event.getEndDateTime());
  }

  /**
   * Tests that an invalid command throws an exception.
   */
  @Test
  public void testExecute_InvalidCommand() {
    try {
      ImportCommand cmd = new ImportCommand(storage, "badcommand");
      cmd.execute();
      fail("Expected InvalidCommandException");
    } catch (InvalidCommandException e) {
      assertTrue(e.getMessage().contains("Invalid import command"));
    } catch (Exception ex) {
      fail("Unexpected exception type: " + ex.getClass());
    }
  }

  /**
   * Tests that a non-existent file causes an exception.
   */
  @Test
  public void testExecute_FileNotFound() {
    String fakePath = "non_existent_file.csv";
    try {
      ImportCommand cmd = new ImportCommand(storage, "import " + fakePath);
      cmd.execute();
      fail("Expected InvalidCommandException due to missing file");
    } catch (InvalidCommandException e) {
      assertTrue(e.getMessage().contains("Error importing"));
    } catch (Exception ex) {
      fail("Unexpected exception type: " + ex.getClass());
    }
  }
}