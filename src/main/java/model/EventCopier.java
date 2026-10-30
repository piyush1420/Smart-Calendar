package model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Handles the business logic for copying events between calendars.
 */
public class EventCopier {
  private final CalendarManager calendarManager;

  /**
   * Constructs an EventCopier with a calendar manager for accessing calendars.
   *
   * @param calendarManager Manages calendar operations and event retrieval.
   */
  public EventCopier(CalendarManager calendarManager) {
    this.calendarManager = calendarManager;
  }

  /**
   * Copies an event from the source calendar to the target calendar, adjusting for time zones.
   *
   * @param eventName          The name of the event to copy.
   * @param startTime          The start time of the event in the source calendar.
   * @param targetCalendarName The name of the target calendar.
   * @param targetTime         The desired start time in the target calendar.
   * @return True if the event was copied successfully, false otherwise.
   * @throws IllegalArgumentException If the event or target calendar is not found,
   *                                  or if an event with the same name and time already exists.
   */

  public boolean copyEvent(String eventName, LocalDateTime startTime,
                           String targetCalendarName, LocalDateTime targetTime) {
    Calendar currentCalendar = calendarManager.getCurrentCalendar();
    if (currentCalendar == null) {
      System.out.println("Error: No calendar selected.");
      return false;
    }

    CalendarEvent event = currentCalendar.findEvent(eventName, startTime);
    if (event == null) {
      System.out.println("Error: Event not found.");
      return false;
    }

    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);
    if (targetCalendar == null) {
      System.out.println("Error: Target calendar not found.");
      return false;
    }

    if (targetCalendar.findEvent(eventName, targetTime) != null) {
      System.out.println("Error: Event exists in target.");
      return false;
    }

    Duration duration = Duration.between(event.getStartDateTime(), event.getEndDateTime());

    LocalDateTime convertedStartTime = targetTime;
    LocalDateTime convertedEndTime = targetTime.plus(duration);

    CalendarEvent copiedEvent = new CalendarEvent(
            event.getSubject(),
            convertedStartTime,
            convertedEndTime,
            event.getDescription(),
            event.getLocation(),
            event.getEventType()
    );

    targetCalendar.addEvent(copiedEvent, true);
    System.out.println("Event copied successfully.");
    return true;
  }

  /**
   * Copies all events occurring on a specific date to another calendar,
   * adjusting times accordingly.
   *
   * @param date               Date of events in the source calendar.
   * @param targetCalendarName Name of the destination calendar.
   * @param targetDate         Date to which events should be copied.
   * @return True if events are copied, false if no events exist on the date.
   */

  public boolean copyEventsOnDate(LocalDate date, String targetCalendarName,
                                  LocalDate targetDate) {
    Calendar currentCalendar = calendarManager.getCurrentCalendar();
    if (currentCalendar == null) {
      System.out.println("Error: No calendar selected.");
      return false;
    }

    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);
    if (targetCalendar == null) {
      System.out.println("Error: Target calendar not found.");
      return false;
    }

    LocalDateTime startDateTime = date.atStartOfDay();
    LocalDateTime endDateTime = date.atTime(LocalTime.MAX);
    List<CalendarEvent> events = currentCalendar.getEventsInRange(startDateTime, endDateTime);

    if (events.isEmpty()) {
      System.out.println("No events found on " + date + ".");
      return false;
    }

    long daysOffset = ChronoUnit.DAYS.between(date, targetDate);
    ZoneId sourceZone = currentCalendar.getTimeZone();
    ZoneId targetZone = targetCalendar.getTimeZone();

    for (CalendarEvent event : events) {
      LocalDateTime convertedStartTime = event.getStartDateTime()
              .atZone(sourceZone)
              .withZoneSameInstant(targetZone)
              .toLocalDateTime();

      LocalDateTime convertedEndTime = event.getEndDateTime()
              .atZone(sourceZone)
              .withZoneSameInstant(targetZone)
              .toLocalDateTime();

      LocalDateTime adjustedStartTime = convertedStartTime.plusDays(daysOffset);
      LocalDateTime adjustedEndTime = convertedEndTime.plusDays(daysOffset);

      CalendarEvent copiedEvent = new CalendarEvent(
              event.getSubject(),
              adjustedStartTime,
              adjustedEndTime,
              event.getDescription(),
              event.getLocation(),
              event.getEventType()
      );

      targetCalendar.addEvent(copiedEvent, true);
    }

    System.out.println("Events copied successfully.");
    return true;
  }

  /**
   * Copies events occurring within a date range to a target calendar,
   * maintaining event offsets.
   *
   * @param startDate          Start date of the range in the source calendar.
   * @param endDate            End date of the range in the source calendar.
   * @param targetCalendarName Name of the calendar to copy events to.
   * @param targetDate         New start date for copied events in the target calendar.
   * @return True if events are copied, false if no events exist in range.
   */

  public boolean copyEventsBetween(LocalDate startDate, LocalDate endDate,
                                   String targetCalendarName, LocalDate targetDate) {
    Calendar currentCalendar = calendarManager.getCurrentCalendar();
    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);
    if (currentCalendar == null || targetCalendar == null) {
      return false;
    }

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
    List<CalendarEvent> events = currentCalendar.getEventsInRange(startDateTime, endDateTime);

    if (events.isEmpty()) {
      System.out.println("No events found in range.");
      return false;
    }

    LocalDate firstEventDate = events.stream()
            .map(e -> e.getStartDateTime().toLocalDate())
            .min(LocalDate::compareTo)
            .orElse(startDate);

    long daysOffset = ChronoUnit.DAYS.between(firstEventDate, targetDate);
    ZoneId sourceZone = currentCalendar.getTimeZone();
    ZoneId targetZone = targetCalendar.getTimeZone();

    for (CalendarEvent event : events) {
      LocalDateTime convertedStartTime = event.getStartDateTime()
              .atZone(sourceZone)
              .withZoneSameInstant(targetZone)
              .toLocalDateTime();

      LocalDateTime convertedEndTime = event.getEndDateTime()
              .atZone(sourceZone)
              .withZoneSameInstant(targetZone)
              .toLocalDateTime();

      LocalDateTime adjustedStartTime = convertedStartTime.plusDays(daysOffset);
      LocalDateTime adjustedEndTime = convertedEndTime.plusDays(daysOffset);

      CalendarEvent copiedEvent = new CalendarEvent(
              event.getSubject(),
              adjustedStartTime,
              adjustedEndTime,
              event.getDescription(),
              event.getLocation(),
              event.getEventType()
      );

      targetCalendar.addEvent(copiedEvent, true);
    }

    System.out.println("Events copied successfully.");
    return true;
  }
}