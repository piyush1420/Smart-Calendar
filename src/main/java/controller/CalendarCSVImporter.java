package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import model.CalendarEvent;
import model.EventStorage;

/**
 * Imports calendar events from a CSV file into the application's event storage.
 * <p>
 * This class reads a CSV file and creates {@link CalendarEvent} objects from each
 * valid CSV line. Imported events are added to the provided {@link EventStorage}.
 * </p>
 */
public class CalendarCSVImporter {
  private final EventStorage eventStorage;

  /**
   * Constructs a new CalendarCSVImporter using the specified event storage.
   *
   * @param eventStorage the event storage to which events will be added.
   */
  public CalendarCSVImporter(EventStorage eventStorage) {
    this.eventStorage = eventStorage;
  }

  /**
   * Imports events from the given CSV file path.
   *
   * @param filePath the path of the CSV file.
   * @throws IOException if an I/O error occurs during reading the file.
   */
  public void importFromCSV(String filePath) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      boolean isHeader = true;

      while ((line = br.readLine()) != null) {
        if (isHeader) {
          isHeader = false;
          continue;
        }

        CalendarEvent event = parseCSVLine(line);
        if (event != null) {
          eventStorage.addEvent(event, true);
        }
      }
    }
  }

  /**
   * Parses a CSV line into a {@link CalendarEvent}.
   *
   * @param line the CSV line to parse.
   * @return a CalendarEvent object if parsing is successful, or {@code null} if invalid.
   */
  private CalendarEvent parseCSVLine(String line) {
    String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    try {
      String subject = unescape(values[0]);
      LocalDateTime start = parseDateTime(values[1], values[2]);
      LocalDateTime end = parseDateTime(values[3], values[4]);

      CalendarEvent event = new CalendarEvent(subject, start, end);
      if (values.length > 6) {
        event.setDescription(unescape(values[6]));
      }
      if (values.length > 7) {
        event.setLocation(unescape(values[7]));
      }
      return event;
    } catch (Exception e) {
      System.err.println("Skipping invalid line: " + e.getMessage());
      return null;
    }
  }

  /**
   * Removes surrounding quotes from a CSV field and unescapes double quotes.
   *
   * @param value the CSV field value.
   * @return the unescaped string.
   */
  private String unescape(String value) {
    return
            value.replaceAll("^\"|\"$", "").replace("\"\"", "\"");
  }

  /**
   * Parses the provided date and time strings into a {@link LocalDateTime}.
   *
   * @param dateStr the date string in "MM/dd/yyyy" format.
   * @param timeStr the time string in "hh:mm a" format.
   * @return a LocalDateTime representing the parsed date and time.
   */
  private LocalDateTime parseDateTime(String dateStr, String timeStr) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    LocalDate date = LocalDate.parse(dateStr, dateFormatter);
    LocalTime time = LocalTime.parse(timeStr, timeFormatter);
    return LocalDateTime.of(date, time);
  }
}