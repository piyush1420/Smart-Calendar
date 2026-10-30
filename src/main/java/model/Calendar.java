package model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Represents a single calendar with a unique name, time zone, and list of events.
 * This class provides operations for managing calendar events including adding,
 * retrieving, and removing events from the calendar.
 */
public class Calendar implements CalendarOperations<CalendarEvent> {
  private final String name; // Calendar name is final (immutable)
  private ZoneId timeZone;
  private final MultiCalendarEventStorage eventStorage;

  /**
   * Constructs a new Calendar with the specified name, time zone, and event storage.
   *
   * @param name         The unique identifier for this calendar, cannot be modified after creation.
   * @param timeZone     The time zone in which this calendar operates.
   * @param eventStorage The storage system for managing calendar events across multiple calendars.
   */
  public Calendar(String name, ZoneId timeZone, MultiCalendarEventStorage eventStorage) {
    this.name = name;
    this.timeZone = timeZone;
    this.eventStorage = eventStorage;
  }

  /**
   * Returns the name of this calendar.
   * The name serves as a unique identifier for the calendar.
   *
   * @return The immutable name of this calendar
   */
  public String getName() {

    return name;
  }

  /**
   * Returns the time zone setting for this calendar.
   * All events in this calendar are interpreted according to this time zone.
   *
   * @return The time zone of this calendar
   */
  public ZoneId getTimeZone() {

    return timeZone;
  }

  /**
   * Updates the time zone setting for this calendar.
   * This does not convert existing event times, just changes the zone for future operations.
   *
   * @param timeZone The new time zone to set for this calendar
   */
  public void setTimeZone(ZoneId timeZone) {

    this.timeZone = timeZone;
  }

  /**
   * Adds a new event to this calendar.
   *
   * @param event       The calendar event to add
   * @param autoDecline If true, automatically declines conflicting events
   */
  @Override
  public void addEvent(CalendarEvent event, boolean autoDecline) {
    eventStorage.addEvent(name, event, autoDecline);  // No cast needed
  }

  /**
   * Retrieves all events scheduled on the specified date.
   *
   * @param date The date for which to retrieve events
   * @return A list of events occurring on the specified date
   */
  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDateTime date) {
    return eventStorage.getEventsOnDate(name, date);  // No cast needed
  }

  /**
   * Retrieves all events within the specified time range.
   * Both start and end times are inclusive in the search.
   *
   * @param start The start time of the range
   * @param end   The end time of the range
   * @return A list of events within the specified range
   */
  @Override
  public List<CalendarEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return eventStorage.getEventsInRange(name, start, end);  // No cast needed
  }

  /**
   * Finds an event by its name and start date/time in this calendar.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date/time of the event
   * @return the matching event, or null if not found
   */
  @Override
  public CalendarEvent findEvent(String eventName, LocalDateTime startDateTime) {
    return eventStorage.findEvent(name, eventName, startDateTime);
  }

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   */
  @Override
  public void removeEvent(CalendarEvent event) {
    eventStorage.removeEvent(name, event);  // No cast needed
  }
}