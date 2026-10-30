package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages event storage for multiple calendars.
 */
public class MultiCalendarEventStorage {
  private final Map<String, EventStorage> calendarEventStorages;

  /**
   * Creates new multi-calendar event storage.
   */
  public MultiCalendarEventStorage() {
    this.calendarEventStorages = new HashMap<>();
  }

  /**
   * Adds event to specified calendar.
   */
  public void addEvent(String calendarName, CalendarEvent event, boolean autoDecline) {
    EventStorage eventStorage = calendarEventStorages.computeIfAbsent(
            calendarName, k -> new EventStorage());
    eventStorage.addEvent(event, autoDecline);
  }

  /**
   * Checks for event conflicts in specified calendar.
   */
  public boolean hasConflict(String calendarName, CalendarEvent event) {
    EventStorage eventStorage = calendarEventStorages.get(calendarName);
    if (eventStorage == null) {
      return false;
    }
    return eventStorage.hasConflict(event);
  }

  /**
   * Finds event by name and start time in calendar.
   */
  public CalendarEvent findEvent(String calendarName, String eventName,
                                 LocalDateTime startDateTime) {
    EventStorage eventStorage = calendarEventStorages.get(calendarName);
    if (eventStorage == null) {
      return null;
    }
    return eventStorage.findEvent(eventName, startDateTime);
  }

  /**
   * Gets all events on specific date for calendar.
   */
  public List<CalendarEvent> getEventsOnDate(String calendarName, LocalDateTime date) {
    EventStorage eventStorage = calendarEventStorages.get(calendarName);
    if (eventStorage == null) {
      return new ArrayList<>();
    }
    return eventStorage.getEventsOnDate(date);
  }

  /**
   * Gets events within date range for calendar.
   */
  public List<CalendarEvent> getEventsInRange(String calendarName, LocalDateTime start,
                                              LocalDateTime end) {
    EventStorage eventStorage = calendarEventStorages.get(calendarName);
    if (eventStorage == null) {
      return new ArrayList<>();
    }
    return eventStorage.getEventsInRange(start, end);
  }

  /**
   * Gets all events for specified calendar.
   */
  public Map<LocalDateTime, List<CalendarEvent>> getEventsForCalendar(String calendarName) {
    EventStorage eventStorage = calendarEventStorages.get(calendarName);
    if (eventStorage == null) {
      return new HashMap<>();
    }
    return eventStorage.getAllEvents();
  }

  /**
   * Gets event storage for specified calendar.
   */
  public EventStorage getEventStorage(String calendarName) {
    return calendarEventStorages.get(calendarName);
  }

  /**
   * Removes event from specified calendar.
   */
  public void removeEvent(String calendarName, CalendarEvent event) {
    EventStorage storage = getEventStorage(calendarName);
    if (storage != null) {
      storage.removeEvent(event);
    }
  }

  /**
   * Gets event storage for calendar by name.
   */
  public EventStorage getEventStorageForCalendar(String calendarName) {
    return calendarEventStorages.get(calendarName);
  }

  /**
   * Removes calendar storage by name.
   */
  public void removeCalendarStorage(String calendarName) {
    calendarEventStorages.remove(calendarName);
  }

  /**
   * Adds calendar storage with name.
   */
  public void putCalendarStorage(String calendarName, EventStorage storage) {
    calendarEventStorages.put(calendarName, storage);
  }
}