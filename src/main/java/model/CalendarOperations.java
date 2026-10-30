package model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Defines operations for managing events in a calendar.
 * Provides methods to add, find, and remove events, as well as retrieve events by date
 * or date range.
 *
 * @param <E> the type of events this calendar manages, must extend {@link Event}
 */
public interface CalendarOperations<E extends Event> {
  /**
   * Returns the name of this calendar.
   *
   * @return the calendar name
   */
  String getName();

  /**
   * Returns the timezone of this calendar.
   *
   * @return the timezone as a ZoneId
   */
  ZoneId getTimeZone();

  /**
   * Sets the timezone for this calendar.
   *
   * @param timeZone the new timezone to use
   */
  void setTimeZone(ZoneId timeZone);

  /**
   * Adds an event to this calendar.
   *
   * @param event the event to add
   * @param autoDecline whether to automatically decline conflicting events
   */
  void addEvent(E event, boolean autoDecline);

  /**
   * Retrieves all events occurring on a specific date.
   *
   * @param date the date to find events for
   * @return a list of events on the specified date
   */
  List<E> getEventsOnDate(LocalDateTime date);

  /**
   * Retrieves all events occurring within a date range.
   *
   * @param start the start of the date range
   * @param end the end of the date range
   * @return a list of events within the specified range
   */
  List<E> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Finds a specific event by name and start time.
   *
   * @param eventName the name of the event to find
   * @param startDateTime the start time of the event
   * @return the matching event or null if not found
   */
  E findEvent(String eventName, LocalDateTime startDateTime);

  /**
   * Removes an event from this calendar.
   *
   * @param event the event to remove
   */
  void removeEvent(E event);
}