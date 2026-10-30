package controller;

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

import model.CalendarEvent;
import model.EventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for CalendarCSVImporter.
 * <p>
 * This class creates a temporary CSV file without extra quotes on date and
 * time fields so that they can be parsed using patterns "MM/dd/yyyy" and
 * "hh:mm a". For example, an event line looks like:
 * Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM,,Team meeting,Office
 * </p>
 */
public class CalendarCSVImporterTest {

  private DummyEventStorage storage;
  private CalendarCSVImporter importer;
  private Path tempFile;

  /**
   * Dummy EventStorage that collects events.
   */
  private class DummyEventStorage extends EventStorage {
    private List<CalendarEvent> events = new ArrayList<>();

    @Override
    public void addEvent(CalendarEvent event, boolean recurring) {
      events.add(event);
    }

    @Override
    public Map<LocalDateTime, List<CalendarEvent>> getAllEvents() {
      Map<LocalDateTime, List<CalendarEvent>> map = new HashMap<>();
      for (CalendarEvent ev : events) {
        LocalDateTime key = ev.getStartDateTime();
        if (!map.containsKey(key)) {
          map.put(key, new ArrayList<>());
        }
        map.get(key).add(ev);
      }
      return map;
    }
  }

  @Before
  public void setUp() {
    storage = new DummyEventStorage();
    importer = new CalendarCSVImporter(storage);
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
  public void testImportFromCSV_Valid() throws IOException {
    tempFile = Files.createTempFile("testValid", ".csv");
    String header = "Subject,StartDate,StartTime,EndDate,EndTime,Unused,Description,Location";
    String eventLine = "Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM,,Team meeting,Office";
    List<String> lines = new ArrayList<>();
    lines.add(header);
    lines.add(eventLine);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    }

    importer.importFromCSV(tempFile.toString());
    Map<LocalDateTime, List<CalendarEvent>> allEvents = storage.getAllEvents();
    List<CalendarEvent> events = new ArrayList<>();
    for (List<CalendarEvent> lst : allEvents.values()) {
      events.addAll(lst);
    }
    assertEquals("One event should be imported", 1, events.size());

    CalendarEvent event = events.get(0);
    assertEquals("Meeting", event.getSubject());

    LocalDateTime expectedStart = LocalDateTime.of(2023, 4, 1, 9, 0);
    LocalDateTime expectedEnd = LocalDateTime.of(2023, 4, 1, 10, 0);
    assertEquals(expectedStart, event.getStartDateTime());
    assertEquals(expectedEnd, event.getEndDateTime());
    assertEquals("Team meeting", event.getDescription());
    assertEquals("Office", event.getLocation());
  }

  /**
   * Tests that an invalid CSV line is skipped.
   */
  @Test
  public void testImportFromCSV_InvalidLine() throws IOException {
    tempFile = Files.createTempFile("testInvalid", ".csv");
    String header = "Subject,StartDate,StartTime,EndDate,EndTime";
    String invalidLine = "BadEvent,wrongDate,09:00 AM";
    List<String> lines = new ArrayList<>();
    lines.add(header);
    lines.add(invalidLine);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    }
    importer.importFromCSV(tempFile.toString());
    Map<LocalDateTime, List<CalendarEvent>> allEvents = storage.getAllEvents();
    List<CalendarEvent> events = new ArrayList<>();
    for (List<CalendarEvent> lst : allEvents.values()) {
      events.addAll(lst);
    }
    assertEquals("No event should be imported", 0, events.size());
  }

  /**
   * Tests the private unescape() method using reflection.
   */
  @Test
  public void testUnescape() throws Exception {
    java.lang.reflect.Method method =
            CalendarCSVImporter.class.getDeclaredMethod("unescape", String.class);
    method.setAccessible(true);
    String input = "\"Hello \"\"World\"\"\"";
    String expected = "Hello \"World\"";
    String output = (String) method.invoke(importer, input);
    assertEquals(expected, output);
  }

  /**
   * Tests the private parseDateTime() method using reflection.
   */
  @Test
  public void testParseDateTime() throws Exception {
    java.lang.reflect.Method method =
            CalendarCSVImporter.class.getDeclaredMethod("parseDateTime", String.class,
                    String.class);
    method.setAccessible(true);
    String dateStr = "04/01/2023";
    String timeStr = "09:00 AM";
    LocalDateTime expected = LocalDateTime.of(2023, 4, 1, 9, 0);
    LocalDateTime result = (LocalDateTime) method.invoke(importer, dateStr, timeStr);
    assertEquals(expected, result);
  }

  /**
   * Tests that header lines are properly skipped during import.
   */
  @Test
  public void testImportFromCSV_SkipsHeader() throws IOException {
    tempFile = Files.createTempFile("testHeader", ".csv");
    String header = "Subject,StartDate,StartTime,EndDate,EndTime";
    String eventLine = "Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM";
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write(header);
      writer.newLine();
      writer.write(eventLine);
    }

    importer.importFromCSV(tempFile.toString());
    Map<LocalDateTime, List<CalendarEvent>> allEvents = storage.getAllEvents();
    assertEquals("Should skip header and import one event", 1, allEvents.size());
  }

  /**
   * Tests that optional fields (description) are properly handled when present.
   */
  @Test
  public void testImportFromCSV_WithOptionalDescription() throws IOException {
    tempFile = Files.createTempFile("testDesc", ".csv");
    String eventLine = "Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM,,Team meeting";
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write("Subject,StartDate,StartTime,EndDate,EndTime");
      writer.newLine();
      writer.write(eventLine);
    }

    importer.importFromCSV(tempFile.toString());
    CalendarEvent event = storage.getAllEvents().values().iterator().next().get(0);
    assertEquals("Description should be set", "Team meeting", event.getDescription());
  }

  /**
   * Tests that optional fields (location) are properly handled when present.
   */
  @Test
  public void testImportFromCSV_WithOptionalLocation() throws IOException {
    tempFile = Files.createTempFile("testLoc", ".csv");
    String eventLine = "Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM,,,Office";
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write("Subject,StartDate,StartTime,EndDate,EndTime");
      writer.newLine();
      writer.write(eventLine);
    }

    importer.importFromCSV(tempFile.toString());
    CalendarEvent event = storage.getAllEvents().values().iterator().next().get(0);
    assertEquals("Location should be set", "Office", event.getLocation());
  }

  /**
   * Tests that error messages are printed for invalid lines.
   */
  @Test
  public void testImportFromCSV_PrintsErrorMessageForInvalidLines() throws IOException {
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(errContent));

    try {
      tempFile = Files.createTempFile("testErrMsg", ".csv");
      String invalidLine = "BadEvent,wrongDate,09:00 AM";
      try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
        writer.write("Subject,StartDate,StartTime");
        writer.newLine();
        writer.write(invalidLine);
      }

      importer.importFromCSV(tempFile.toString());
      assertTrue("Should print error message for invalid line",
              errContent.toString().contains("Skipping invalid line:"));
    } finally {
      System.setErr(originalErr);
    }
  }

  /**
   * Tests boundary condition for optional fields (exactly 6 fields).
   */
  @Test
  public void testImportFromCSV_ExactlySixFields() throws IOException {
    tempFile = Files.createTempFile("testSixFields", ".csv");
    String eventLine = "Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM,ExtraField";
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write("Subject,StartDate,StartTime,EndDate,EndTime,Extra");
      writer.newLine();
      writer.write(eventLine);
    }

    importer.importFromCSV(tempFile.toString());
    CalendarEvent event = storage.getAllEvents().values().iterator().next().get(0);
    assertTrue("Description should be null when exactly 6 fields",
            event.getDescription().contains(""));
  }

  /**
   * Tests boundary condition for optional fields (exactly 7 fields).
   */
  @Test
  public void testImportFromCSV_ExactlySevenFields() throws IOException {
    tempFile = Files.createTempFile("testSevenFields", ".csv");
    String eventLine = "Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM,,Description";
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write("Subject,StartDate,StartTime,EndDate,EndTime,Extra,Description");
      writer.newLine();
      writer.write(eventLine);
    }

    importer.importFromCSV(tempFile.toString());
    CalendarEvent event = storage.getAllEvents().values().iterator().next().get(0);
    assertEquals("Description should be set", "Description", event.getDescription());
    assertTrue("Location should be null when exactly 7 fields", event.getLocation().contains(""));
  }

  /**
   * Tests that ONLY the first line is treated as header when isHeader flag is true.
   */
  @Test
  public void testImportFromCSV_StrictHeaderHandling() throws IOException {
    tempFile = Files.createTempFile("testStrictHeader", ".csv");
    String header = "Subject,StartDate,StartTime,EndDate,EndTime";
    String eventLine1 = "Meeting1,04/01/2023,09:00 AM,04/01/2023,10:00 AM";
    String eventLine2 = "Meeting2,04/01/2023,11:00 AM,04/01/2023,12:00 PM";

    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write(header);
      writer.newLine();
      writer.write(eventLine1);
      writer.newLine();
      writer.write(eventLine2);
    }

    importer.importFromCSV(tempFile.toString());
    Map<LocalDateTime, List<CalendarEvent>> allEvents = storage.getAllEvents();
    assertEquals("Should skip only first line as header and import two events",
            2, allEvents.size());
  }

  /**
   * Tests that empty files are handled correctly (header exists but no events).
   */
  @Test
  public void testImportFromCSV_EmptyFileWithHeader() throws IOException {
    tempFile = Files.createTempFile("testEmpty", ".csv");
    String header = "Subject,StartDate,StartTime,EndDate,EndTime";

    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write(header);
    }

    importer.importFromCSV(tempFile.toString());
    Map<LocalDateTime, List<CalendarEvent>> allEvents = storage.getAllEvents();
    assertTrue("Should handle empty file with header correctly",
            allEvents.isEmpty());
  }

  /**
   * Tests that malformed header lines are still skipped.
   */
  @Test
  public void testImportFromCSV_MalformedHeader() throws IOException {
    tempFile = Files.createTempFile("testMalformedHeader", ".csv");
    String badHeader = "Not,A,Proper,Header";
    String eventLine = "Meeting,04/01/2023,09:00 AM,04/01/2023,10:00 AM";

    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write(badHeader);
      writer.newLine();
      writer.write(eventLine);
    }

    importer.importFromCSV(tempFile.toString());
    Map<LocalDateTime, List<CalendarEvent>> allEvents = storage.getAllEvents();
    assertEquals("Should skip malformed header and import one event",
            1, allEvents.size());
  }

  /**
   * Tests that the header line is skipped and not processed as an event.
   * This test specifically verifies the isHeader flag functionality.
   */
  @Test
  public void testHeaderLineIsNotProcessedAsEvent() throws IOException {
    // Create a CSV where the header line could be mistakenly interpreted as valid data
    tempFile = Files.createTempFile("testHeaderProcessing", ".csv");

    // Create a header that would be a valid event if processed
    String header = "ValidEventName,04/01/2023,09:00 AM,04/01/2023,10:00 AM,,Meeting notes,Room A";

    // Create a normal event line
    String eventLine = "ActualEvent,04/02/2023,09:00 AM,04/02/2023,10:00 AM,,Notes,LocationB";

    // Write both to the file
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write(header);
      writer.newLine();
      writer.write(eventLine);
    }

    // Import the file
    importer.importFromCSV(tempFile.toString());

    // Get all imported events
    Map<LocalDateTime, List<CalendarEvent>> allEvents = storage.getAllEvents();
    List<CalendarEvent> events = new ArrayList<>();
    for (List<CalendarEvent> lst : allEvents.values()) {
      events.addAll(lst);
    }

    // Verify only one event was imported (the actual event, not the header)
    assertEquals("Only one event should be imported", 1, events.size());

    // Verify the imported event is the actual event, not the header
    CalendarEvent importedEvent = events.get(0);
    assertEquals("ActualEvent", importedEvent.getSubject());

    // Explicitly verify the header wasn't imported
    for (CalendarEvent event : events) {
      assertFalse("Header should not be imported as an event",
              event.getSubject().equals("ValidEventName"));
    }

    // Check the date of the imported event to further verify it's the right one
    LocalDateTime expectedDate = LocalDateTime.of(2023, 4, 2, 9, 0);
    assertEquals("Imported event should have the correct date",
            expectedDate, importedEvent.getStartDateTime());
  }
}