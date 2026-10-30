package view.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import model.CalendarEvent;
import model.EventStorage;

/**
 * Exports calendar events to a CSV file in a format compatible with Google Calendar.
 */
public class CSVCalendarExporter implements ICalendarExporter {
  private final EventStorage eventStorage;

  /**
   * Constructs a CalendarExporter with the given EventStorage.
   *
   * @param eventStorage the EventStorage containing the events to export
   */
  public CSVCalendarExporter(EventStorage eventStorage) {
    this.eventStorage = eventStorage;
  }

  /**
   * Exports all events to a CSV file at the specified file path.
   *
   * @param filePath the path to the CSV file
   * @throws IOException if an I/O error occurs while writing the file
   */
  @Override
  public void exportToFormat(String filePath) throws IOException {
    File file = new File(filePath);
    try (FileWriter fw = new FileWriter(file);
         BufferedWriter bw = new BufferedWriter(fw)) {

      // Write CSV header (Google Calendar format)
      bw.write(String.join(",", "Subject", "Start Date", "Start Time",
              "End Date", "End Time", "All Day Event", "Description",
              "Location", "Private"));
      bw.newLine();

      // Get all events from EventStorage as a Map<LocalDateTime, List<CalendarEvent>>
      Map<LocalDateTime, List<CalendarEvent>> eventsMap =
              eventStorage.getAllEvents();

      // Iterate over the map and write each event to the CSV file
      for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry :
              eventsMap.entrySet()) {
        List<CalendarEvent> events = entry.getValue();
        for (CalendarEvent event : events) {
          bw.write(formatEventAsCSV(event));
          bw.newLine();
        }
      }

      System.out.println("CSV generated successfully at: " + filePath);
    }
  }

  /**
   * Formats a CalendarEvent as a CSV-compatible string.
   *
   * @param event the event to format
   * @return the formatted CSV string
   */
  private String formatEventAsCSV(CalendarEvent event) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    String subject = escapeCSV(event.getSubject());
    String startDate = event.getStartDateTime().format(dateFormatter);
    String startTime = event.getStartDateTime().format(timeFormatter);
    String endDate = event.getEndDateTime().format(dateFormatter);
    String endTime = event.getEndDateTime().format(timeFormatter);
    String allDayEvent = event.isAllDayEvent(event) ? "True" : "False";

    String description = event.getDescription() == null || event.getDescription().trim().isEmpty()
            ? ""
            : escapeCSV(event.getDescription());

    String location = event.getLocation() == null || event.getLocation().trim().isEmpty()
            ? ""
            : escapeCSV(event.getLocation());

    String isPrivate = "False"; // Default to False

    return String.join(",",
            subject,
            startDate,
            startTime,
            endDate,
            endTime,
            allDayEvent,
            description,
            location,
            isPrivate
    );
  }

  /**
   * Escapes special characters in a CSV field.
   *
   * @param value the value to escape
   * @return the escaped value
   */
  private String escapeCSV(String value) {
    if (value == null) {
      return "";
    }
    // Replace special characters and wrap in double quotes
    value = value.replaceAll("\"", "\"\"") // Escape double quotes
            .replaceAll("\n", " ")    // Replace newlines with spaces
            .replaceAll("\t", " ")    // Replace tabs with spaces
            .replaceAll("\r", " ");   // Replace carriage returns with spaces
    return "\"" + value + "\"";
  }
}