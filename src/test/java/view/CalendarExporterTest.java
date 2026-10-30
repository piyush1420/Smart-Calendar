package view;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import model.CalendarEvent;
import model.EventStorage;
import view.text.CSVCalendarExporter;
import view.text.ICalendarExporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the functionality of the CalendarExporter class,
 * ensuring that events are exported to CSV files correctly.
 */
public class CalendarExporterTest {

  private EventStorage eventStorage;
  private ICalendarExporter calendarExporter;
  private String resourcesDir;

  @Before
  public void setUp() {
    eventStorage = new EventStorage();
    calendarExporter = new CSVCalendarExporter(eventStorage);
    resourcesDir = Paths.get("src", "test", "resources").toString();
  }

  @Test
  public void testExportToCSV_Success() throws IOException {
    LocalDateTime now = LocalDateTime.now();
    CalendarEvent event = new CalendarEvent("Meeting", now, now.plusHours(1),
            "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    calendarExporter.exportToFormat(filePath);

    File file = new File(filePath);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);
    file.delete();
  }

  @Test(expected = IOException.class)
  public void testExportToCSV_InvalidFilePath() throws IOException {
    String invalidFilePath = "/invalid/path/test.csv";

    calendarExporter.exportToFormat(invalidFilePath);
  }

  @Test
  public void testExportToCSV_FormatEventAsCSV_AllFields() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, "Team Meeting",
            "Office", "Work");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    calendarExporter.exportToFormat(filePath);

    File file = new File(filePath);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    String expectedLine = "\"Meeting\",10/01/2023,10:00 AM,10/01/2023,11:00 AM,False," +
            "\"Team Meeting\",\"Office\",False";
    assertTrue(fileContainsLine(file, expectedLine));

    file.delete();
  }

  @Test
  public void testExportToCSV_FormatEventAsCSV_NullDescriptionAndLocation() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null,
            null, "Work");
    eventStorage.addEvent(event, false);

    String filePath = Paths.get(resourcesDir, "test.csv").toString();

    calendarExporter.exportToFormat(filePath);

    File file = new File(filePath);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    String expectedLine = "\"Meeting\",10/01/2023,10:00 AM,10/01/2023,11:00 AM,False,,,False";
    assertTrue(fileContainsLine(file, expectedLine));

    file.delete();
  }

  @Test
  public void testExportToCSV_IsAllDayEvent_True() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 23, 59);
    CalendarEvent event = new CalendarEvent("All Day Event", start, end,
            "All Day", "Home", "Personal");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    calendarExporter.exportToFormat(filePath);

    File file = new File(filePath);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    String expectedLine = "\"All Day Event\",10/01/2023,12:00 AM,10/01/2023,11:59 PM," +
            "True,\"All Day\",\"Home\",False";
    assertTrue(fileContainsLine(file, expectedLine));

    file.delete();
  }

  @Test
  public void testExportToCSV_IsAllDayEvent_False() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
            "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    calendarExporter.exportToFormat(filePath);

    File file = new File(filePath);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    String expectedLine = "\"Meeting\",10/01/2023,10:00 AM,10/01/2023,11:00 AM,False," +
            "\"Team Meeting\",\"Office\",False";
    assertTrue(fileContainsLine(file, expectedLine));

    file.delete();
  }

  @Test
  public void testExportToCSV_EscapeCSV_WithSpecialCharacters() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting, \"Special\"", start, end,
            "Team Meeting\nNew Line", "Office\tTab", "Work");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    calendarExporter.exportToFormat(filePath);

    File file = new File(filePath);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    String expectedLine = "\"Meeting, \"\"Special\"\"\",10/01/2023,10:00 AM,10/01/2023,11:00 AM," +
            "False,\"Team Meeting New Line\",\"Office Tab\",False";
    assertTrue(fileContainsLine(file, expectedLine));

    file.delete();
  }

  @Test
  public void testExportToCSV_EscapeCSV_NullValue() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null,
            null, "Work");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    calendarExporter.exportToFormat(filePath);

    File file = new File(filePath);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    String expectedLine = "\"Meeting\",10/01/2023,10:00 AM,10/01/2023,11:00 AM,False,,,False";
    assertTrue(fileContainsLine(file, expectedLine));

    file.delete();
  }

  private boolean fileContainsLine(File file, String expectedLine) throws IOException {
    try (java.util.Scanner scanner = new java.util.Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.equals(expectedLine)) {
          return true;
        }
      }
    }
    return false;
  }

  @Test
  public void testExportToCSV_VerifyFileContent() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null,
            null, "Work");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    calendarExporter.exportToFormat(filePath);

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    assertEquals(2, lines.size());
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
            "Description,Location,Private", lines.get(0));
    assertEquals("\"Meeting\",10/01/2023,10:00 AM,10/01/2023,11:00 AM,False,,,False", lines.get(1));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testExportToCSV_VerifyConsoleOutput() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null,
            null, "Work");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    calendarExporter.exportToFormat(filePath);

    String expectedOutput = "CSV generated successfully at: " + filePath + System.lineSeparator();
    assertEquals(expectedOutput, outContent.toString());

    Files.deleteIfExists(Paths.get(filePath));
    System.setOut(System.out);
  }

  @Test
  public void testExportToCSV_VerifyNewlines() throws IOException {
    LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null,
            null, "Work");
    eventStorage.addEvent(event, false);

    String filePath = "test.csv";

    calendarExporter.exportToFormat(filePath);

    String fileContent = Files.readString(Paths.get(filePath));
    String[] lines = fileContent.split(System.lineSeparator());

    assertEquals(2, lines.length);
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
            "Description,Location,Private", lines[0]);
    assertEquals("\"Meeting\",10/01/2023,10:00 AM,10/01/2023,11:00 AM,False,,,False",
            lines[1]);

    Files.deleteIfExists(Paths.get(filePath));
  }
}