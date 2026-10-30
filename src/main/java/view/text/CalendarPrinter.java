package view.text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import model.CalendarEvent;
import model.EventStorage;

/**
 * Prints calendar events in formatted output.
 */
public class CalendarPrinter {
  private final EventStorage eventStorage;

  /**
   * Creates printer with event storage.
   */
  public CalendarPrinter(EventStorage eventStorage) {
    this.eventStorage = eventStorage;
  }

  /**
   * Prints all events on specified date.
   */
  public void printEventsOnDate(LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    List<CalendarEvent> events = eventStorage.getEventsOnDate(startOfDay);
    if (events.isEmpty()) {
      System.out.println("No events found on " + date);
    } else {
      System.out.println("Events on " + date + ":");
      for (CalendarEvent event : events) {
        printEventDetails(event);
      }
    }
  }

  /**
   * Prints events within date-time range.
   */
  public void printEventsInRange(LocalDateTime startDateTime,
                                 LocalDateTime endDateTime) {
    List<CalendarEvent> events = eventStorage.getEventsInRange(startDateTime, endDateTime);
    if (events.isEmpty()) {
      System.out.println("No events found in range.");
      return;
    }
    Map<LocalDate, List<CalendarEvent>> groupedEvents = new TreeMap<>();
    for (CalendarEvent event : events) {
      groupedEvents
              .computeIfAbsent(event.
                      getStartDateTime().toLocalDate(), k -> new ArrayList<>())
              .add(event);
    }
    for (Map.Entry<LocalDate, List<CalendarEvent>> entry : groupedEvents.entrySet()) {
      System.out.println("Events on " + entry.getKey() + ":");
      for (CalendarEvent event : entry.getValue()) {
        printEventDetails(event);
      }
    }
  }

  /**
   * Shows busy/available status at date-time.
   */
  public void showStatusOnDateTime(LocalDateTime dateTime) {
    List<CalendarEvent> events = eventStorage.getEventsOnDate(dateTime);
    boolean isBusy = events.stream().anyMatch(event ->
            dateTime.isEqual(event.getStartDateTime()) ||
                    (dateTime.isAfter(event.getStartDateTime()) &&
                            dateTime.isBefore(event.getEndDateTime())));

    System.out.println("User is " + (isBusy ? "busy" : "available") +
            " on " + dateTime);
  }

  /**
   * Prints formatted event details.
   */
  private void printEventDetails(CalendarEvent event) {
    String locationString = (event.getLocation() != null && !event.getLocation().isEmpty())
            ? " | Location: " + event.getLocation()
            : "";
    System.out.printf("- %s | %s - %s%s\n", event.getSubject(),
            event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            event.getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            locationString);
  }
}