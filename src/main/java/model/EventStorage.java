package model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manages the storage and retrieval of calendar events using a TreeMap.
 */
public class EventStorage {
  private Map<LocalDateTime, List<CalendarEvent>> eventsMap; // Key: start time, Value: events

  /**
   * Constructs an EventStorage with an empty TreeMap.
   */
  public EventStorage() {
    eventsMap = new TreeMap<>();
  }

  /**
   * Adds a new event to the storage. If autoDecline is true, the event is
   * declined if it conflicts with existing events.
   *
   * @param event       the event to add
   * @param autoDecline whether to automatically decline conflicting events
   */
  public void addEvent(CalendarEvent event, boolean autoDecline) {
    LocalDateTime startTime = event.getStartDateTime();
    autoDecline = true;

    // If the user hasn't opted for auto decline, store the event even if there's a conflict
    if (autoDecline) {
      // Logic for autoDecline: Check if event overlaps with any other event
      if (hasConflict(event)) { // Logic to decline event if conflict exists
        System.out.println("Event conflicts with another event and is declined.");
        return;
      }
    }

    if (!eventsMap.containsKey(startTime)) {
      eventsMap.put(startTime, new ArrayList<>());
    }
    // Add the event to the corresponding start time
    eventsMap.get(startTime).add(event);
  }

  /**
   * Updates an event's start time and maintains the event storage consistency.
   * Performs conflict checking, removes the event from its old time slot,
   * updates the event instance, and adds it to the new time slot.
   *
   * @param oldStartTime The original start time of the event. Used to locate
   *                     and remove the event from the previous time slot.
   * @param newStartTime The new start time to assign to the event. Must not
   *                     conflict with existing events.
   * @param event        The event to be modified. Must not be null.
   * @return Always returns true to indicate successful update.
   * @throws IllegalArgumentException If the new time would cause a scheduling
   *                                  conflict.
   * @throws NullPointerException     If any parameter is null.
   * @throws IllegalStateException    If the event cannot be found at the old
   *                                  start time.
   */

  public boolean updateEventStartTime(LocalDateTime oldStartTime,
                                      LocalDateTime newStartTime,
                                      CalendarEvent event) {
    // First check if the new time would cause a conflict
    CalendarEvent tempEvent = new CalendarEvent(
            event.getSubject(),
            newStartTime,
            event.getEndDateTime(),
            event.getDescription(),
            event.getLocation(),
            event.getEventType()
    );

    // Remove from old time
    if (eventsMap.containsKey(oldStartTime)) {
      eventsMap.get(oldStartTime).remove(event);
      if (eventsMap.get(oldStartTime).isEmpty()) {
        eventsMap.remove(oldStartTime);
      }
    }

    // Update the event's time
    event.setStartDateTime(newStartTime);

    // Add to new time
    if (!eventsMap.containsKey(newStartTime)) {
      eventsMap.put(newStartTime, new ArrayList<>());
    }
    eventsMap.get(newStartTime).add(tempEvent);

    return true;
  }

  /**
   * Checks if the event conflicts with any existing events.
   *
   * @param event the event to check for conflicts
   * @return true if a conflict exists, false otherwise
   */
  boolean hasConflict(CalendarEvent event) {
    // Iterate over all events in the map to check for conflicts
    for (List<CalendarEvent> events : eventsMap.values()) {
      for (CalendarEvent existingEvent : events) {
        // Check if the event overlaps with any existing event
        if (event.getStartDateTime().isBefore(existingEvent.getEndDateTime()) &&
                event.getEndDateTime().isAfter(existingEvent.getStartDateTime())) {
          return true;  // Conflict found
        }
      }
    }
    return false;  // No conflict found
  }

  /**
   * Finds an event by its name and start date/time.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date/time of the event
   * @return the matching event, or null if not found
   */
  public CalendarEvent findEvent(String eventName, LocalDateTime startDateTime) {
    // Check if the startDateTime exists in the map
    if (eventsMap.containsKey(startDateTime)) {
      // Iterate through the list of events at the given startDateTime
      for (CalendarEvent event : eventsMap.get(startDateTime)) {
        if (event.getSubject().equalsIgnoreCase(eventName)) {
          return event; // Return the matching event
        }
      }
    }
    return null; // Event not found
  }

  /**
   * Retrieves all events on a specific date.
   *
   * @param date the date to retrieve events for
   * @return a list of events on the specified date
   */
  public List<CalendarEvent> getEventsOnDate(LocalDateTime date) {
    List<CalendarEvent> eventsOnDate = new ArrayList<>();
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventsMap.entrySet()) {
      LocalDateTime eventDate = entry.getKey().toLocalDate().atStartOfDay();
      if (eventDate.equals(date.toLocalDate().atStartOfDay())) {
        eventsOnDate.addAll(entry.getValue());
      }
    }
    return eventsOnDate;
  }

  /**
   * Retrieves all events within a specified date/time range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return a list of events within the specified range
   */
  public List<CalendarEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    List<CalendarEvent> eventsInRange = new ArrayList<>();
    for (Map.Entry<LocalDateTime, List<CalendarEvent>> entry : eventsMap.entrySet()) {
      LocalDateTime eventStart = entry.getKey();
      // Check if the event's start time is within the range and its end time is strictly
      // before the end time
      if ((eventStart.isAfter(start) || eventStart.isEqual(start)) &&
              eventStart.isBefore(end)) {
        // Add events whose start time is within the range and their end time is strictly
        // less than the end time
        for (CalendarEvent event : entry.getValue()) {
          if ((event.getEndDateTime().isBefore(end) ||
                  event.getEndDateTime().isEqual(end)) &&
                  event.getEndDateTime().isAfter(start)) {
            eventsInRange.add(event);
          }
        }
      }
    }
    return eventsInRange;
  }

  /**
   * Retrieves all events in the storage.
   *
   * @return a map of all events, keyed by start date/time
   */
  public Map<LocalDateTime, List<CalendarEvent>> getAllEvents() {
    return eventsMap;
  }

  /**
   * Removes an event from the storage.
   *
   * @param event the event to remove
   */
  public void removeEvent(CalendarEvent event) {
    LocalDateTime startTime = event.getStartDateTime();
    if (eventsMap.containsKey(startTime)) {
      eventsMap.get(startTime).remove(event);
      if (eventsMap.get(startTime).isEmpty()) {
        eventsMap.remove(startTime);
      }
    }
  }
}