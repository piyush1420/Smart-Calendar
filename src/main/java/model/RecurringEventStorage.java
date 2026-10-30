package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the storage and generation of recurring calendar events.
 */
public class RecurringEventStorage implements Event {
  private CalendarEvent baseEvent; // Stores the event details
  private List<DayOfWeek> repeatDays; // Days the event repeats (e.g., Tuesday, Friday)
  private Integer occurrences; // Number of times the event repeats
  private LocalDateTime untilDateTime; // Stop recurrence on this date and time
  private LocalDate untilDate; // Stop recurrence on this date (without time)
  private EventStorage eventStorage;

  public EventStorage getEventStorage() {
    return eventStorage;
  }

  public void setEventStorage(EventStorage eventStorage) {
    this.eventStorage = eventStorage;
  }

  /**
   * Gets the base event for this recurring event.
   *
   * @return the base event
   */
  public CalendarEvent getBaseEvent() {
    return baseEvent;
  }

  /**
   * Gets the number of occurrences for this recurring event.
   *
   * @return the number of occurrences
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Sets the number of occurrences for this recurring event.
   *
   * @param occurrences the number of occurrences
   */
  public void setOccurrences(Integer occurrences) {
    this.occurrences = occurrences;
  }

  /**
   * Constructs a RecurringEventStorage with a base event, repeat days, and a
   * fixed number of occurrences.
   *
   * @param baseEvent   the base event
   * @param repeatDays  the days the event repeats
   * @param occurrences the number of occurrences
   */
  public RecurringEventStorage(CalendarEvent baseEvent, List<DayOfWeek> repeatDays,
                               int occurrences) {
    this.baseEvent = baseEvent;
    this.repeatDays = repeatDays;
    this.occurrences = occurrences;
  }

  /**
   * Constructs a RecurringEventStorage with a base event, repeat days, and a
   * recurrence end date.
   *
   * @param baseEvent  the base event
   * @param repeatDays the days the event repeats
   * @param untilDate  the recurrence end date
   */
  public RecurringEventStorage(CalendarEvent baseEvent, List<DayOfWeek> repeatDays,
                               LocalDate untilDate) {
    this.baseEvent = baseEvent;
    this.repeatDays = repeatDays;
    this.untilDate = untilDate;
  }

  /**
   * Constructs a RecurringEventStorage with a base event, repeat days, and a
   * recurrence end date and time.
   *
   * @param baseEvent     the base event
   * @param repeatDays    the days the event repeats
   * @param untilDateTime the recurrence end date and time
   */
  public RecurringEventStorage(CalendarEvent baseEvent, List<DayOfWeek> repeatDays,
                               LocalDateTime untilDateTime) {
    this.baseEvent = baseEvent;
    this.repeatDays = repeatDays;
    this.untilDateTime = untilDateTime;
  }

  /**
   * Generates occurrences of this recurring event.
   *
   * @return a list of CalendarEvent instances
   */
  public List<CalendarEvent> generateOccurrences() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDate currentDate = baseEvent.getStartDateTime().toLocalDate();
    int count = 0;

    while (true) {
      // Check if we have reached the limit based on occurrences
      if (occurrences != null && count >= occurrences) {
        break;
      }

      // Check if we have reached the limit based on untilDate
      if (untilDate != null && currentDate.isAfter(untilDate)) {
        break;
      }

      // Check if we have reached the limit based on untilDateTime
      if (untilDateTime != null && !currentDate.atTime(
              baseEvent.getEndDateTime().toLocalTime()).isBefore(untilDateTime)) {
        break;
      }

      // Check if the current day is one of the repeat days
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = baseEvent.getStartDateTime().with(currentDate);
        LocalDateTime eventEnd = baseEvent.getEndDateTime().with(currentDate);

        // Add the event to the list
        events.add(new CalendarEvent(baseEvent.getSubject(),
                eventStart, eventEnd,
                baseEvent.getDescription(), baseEvent.getLocation(),
                baseEvent.getEventType(), true));
        count++;
      }

      // Move to the next day
      currentDate = currentDate.plusDays(1);
    }

    return events;
  }

  /**
   * Adds recurring events to the event storage, checking for conflicts if
   * autoDecline is enabled.
   *
   * @param autoDecline whether to automatically decline conflicting events
   */
  public void addRecurringEvents(boolean autoDecline) {
    List<CalendarEvent> events = generateOccurrences();
    if (events.isEmpty()) {
      System.out.println("No occurrences found.");
      return;
    }

    if (autoDecline) {
      // Check for conflicts in all occurrences
      for (CalendarEvent event : events) {
        if (eventStorage.hasConflict(event)) { // Assuming hasConflict checks if event conflicts
          System.out.println("Recurring event declined due to conflict: " + event);
          return; // Decline the whole recurring series if any conflict is found
        }
      }
    }

    // If no conflicts found (or autoDecline is false), add all events
    for (CalendarEvent event : events) {
      eventStorage.addEvent(event, autoDecline);
    }
  }

  /**
   * Prints all occurrences of this recurring event.
   */
  public void printOccurrences() {
    List<CalendarEvent> events = generateOccurrences();
    if (events.isEmpty()) {
      System.out.println("No occurrences found.");
    } else {
      for (CalendarEvent event : events) {
        System.out.println(event);
      }
    }
  }

  /**
   * Gets the subject of the base event.
   *
   * @return the subject of the base event
   */
  @Override
  public String getSubject() {
    return baseEvent.getSubject();
  }

  /**
   * Gets the start date and time of the base event.
   *
   * @return the start date and time of the base event
   */
  @Override
  public LocalDateTime getStartDateTime() {
    return baseEvent.getStartDateTime();
  }
}