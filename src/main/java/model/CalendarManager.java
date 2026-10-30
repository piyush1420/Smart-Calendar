package model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Manages multiple calendars within an application.
 * This class provides comprehensive functionality for creating, manipulating, and using
 * calendars. It supports operations such as creating new calendars, switching between
 * calendars, editing calendar properties, and managing events across multiple calendars.
 * The CalendarManager serves as the central coordinator for all calendar-related operations,
 * maintaining a collection of calendars and tracking which calendar is currently active.
 */
public class CalendarManager implements ICalendarManager {
  private final Map<String, Calendar> calendars;
  private Calendar currentCalendar;
  private final MultiCalendarEventStorage eventStorage;


  /**
   * Constructs a new CalendarManager with the specified event storage.
   * The provided event storage will be used to store and retrieve events across all calendars
   * managed by this CalendarManager instance.
   */
  public CalendarManager(MultiCalendarEventStorage eventStorage) {
    this.calendars = new HashMap<>();
    this.eventStorage = eventStorage;
  }

  /**
   * Retrieves the event storage used by this CalendarManager.
   * The event storage contains all events for all calendars managed by this CalendarManager.
   */
  public void createCalendar(String name, ZoneId timezone) {
    if (calendars.containsKey(name) || name.isEmpty() || Objects.isNull(name)) {
      throw new IllegalArgumentException("A calendar with the name '" + name + "' already exists.");
    }
    calendars.put(name, new Calendar(name, timezone, eventStorage));
  }

  /**
   * Retrieves the event storage used by this CalendarManager.
   * Event storage contains all events for the managed calendars.
   *
   * @return MultiCalendarEventStorage instance managing event data.
   */
  public MultiCalendarEventStorage getEventStorage() {
    return eventStorage;
  }

  /**
   * Retrieves the currently active calendar.
   *
   * @return The active Calendar instance or null if none is selected.
   */
  public Calendar getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Edits a specified property of a calendar.
   * Supports renaming or updating the calendar's timezone.
   *
   * @param name     Name of the calendar to edit.
   * @param property Property to modify ("name" or "timezone").
   * @param newValue New value for the specified property.
   * @throws IllegalArgumentException If the property is invalid or the calendar is not found.
   */
  public void editCalendar(String name, String property, String newValue) {
    Calendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar with name " + name + " does not exist.");
    }

    switch (property) {
      case "name":
        renameCalendar(name, newValue);
        break;
      case "timezone":
        updateCalendarTimeZone(calendar, ZoneId.of(newValue));
        break;
      default:
        throw new IllegalArgumentException("Invalid property: " + property);
    }
  }

  private void renameCalendar(String oldName, String newName) {
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("Calendar with name " + newName + " already exists.");
    }

    Calendar oldCalendar = calendars.get(oldName);
    if (oldCalendar == null) {
      throw new IllegalArgumentException("Calendar with name " + oldName + " does not exist.");
    }

    // Get existing storage (may be null if no events)
    EventStorage existingStorage = eventStorage.getEventStorageForCalendar(oldName);

    // Remove old calendar mapping
    calendars.remove(oldName);
    eventStorage.removeCalendarStorage(oldName);

    // Create new calendar with new name (same timezone)
    Calendar newCalendar = new Calendar(newName, oldCalendar.getTimeZone(), eventStorage);
    calendars.put(newName, newCalendar);

    // Restore events if they existed
    if (existingStorage != null) {
      eventStorage.putCalendarStorage(newName, existingStorage);
    }

    // Update current calendar reference if needed
    if (currentCalendar != null && currentCalendar.getName().equals(oldName)) {
      currentCalendar = newCalendar;
    }
  }

  /**
   * Selects the current calendar by its name.
   *
   * @param name the name of the calendar to use
   * @throws IllegalArgumentException if the name is null, empty, or not found
   */
  public void useCalendar(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist.");
    }
    currentCalendar = calendars.get(name);
  }

  /**
   * Retrieves a calendar by name.
   *
   * @param calendarName The name of the calendar.
   * @return The Calendar instance or null if not found.
   */
  public Calendar getCalendar(String calendarName) {
    if (calendarName == null) {
      return null;
    }
    return calendars.get(calendarName);
  }

  /**
   * Retrieves event storage for the currently active calendar.
   *
   * @return The EventStorage instance or null if no calendar is active.
   */
  public EventStorage getCurrentCalendarEventStorage() {
    if (currentCalendar == null) {
      return null;
    }
    EventStorage storage = eventStorage.getEventStorage(currentCalendar.getName());
    if (storage == null) {
      storage = new EventStorage();
      eventStorage.putCalendarStorage(currentCalendar.getName(), storage);
    }
    return storage;
  }

  /**
   * Transfers events from an external storage into the currently active calendar.
   * Converts time zones if necessary.
   *
   * @param sourceStorage The source storage from which events are transferred.
   * @return True if events were successfully transferred, false otherwise.
   */
  public boolean transferEventsFromStorage(EventStorage sourceStorage) {
    if (sourceStorage == null || currentCalendar == null) {
      return false;
    }

    Map<LocalDateTime, List<CalendarEvent>> events = sourceStorage.getAllEvents();
    boolean needsTimeZoneConversion = !currentCalendar
            .getTimeZone().equals(ZoneId.of("America/New_York"));

    for (List<CalendarEvent> eventList : events.values()) {
      for (CalendarEvent event : eventList) {
        CalendarEvent eventToAdd = needsTimeZoneConversion
                ? convertEventToTimeZone(event,
                ZoneId.of("America/New_York"), currentCalendar.getTimeZone())
                : event;
        currentCalendar.addEvent(eventToAdd, true);
      }
    }
    return true;
  }

  private CalendarEvent convertEventToTimeZone(CalendarEvent event,
                                               ZoneId fromZone, ZoneId toZone) {
    LocalDateTime newStartTime = event.getStartDateTime()
            .atZone(fromZone)
            .withZoneSameInstant(toZone)
            .toLocalDateTime();

    LocalDateTime newEndTime = event.getEndDateTime()
            .atZone(fromZone)
            .withZoneSameInstant(toZone)
            .toLocalDateTime();

    return new CalendarEvent(
            event.getSubject(),
            newStartTime,
            newEndTime,
            event.getDescription(),
            event.getLocation(),
            event.getEventType()
    );
  }

  private void updateCalendarTimeZone(Calendar calendar, ZoneId newTimeZone) {
    EventStorage calendarStorage = eventStorage.getEventStorage(calendar.getName());

    if (calendarStorage == null) {
      calendar.setTimeZone(newTimeZone);
      System.err.println("No events found in calendar '"
              + calendar.getName() + "'. Timezone updated.");
      return;
    }

    List<CalendarEvent> eventsToProcess = new ArrayList<>();
    for (List<CalendarEvent> eventList : calendarStorage.getAllEvents().values()) {
      eventsToProcess.addAll(eventList);
    }

    removeAllEvents(calendar, eventsToProcess);
    reAddEventsWithNewTimeZone(calendar, eventsToProcess, newTimeZone);
    calendar.setTimeZone(newTimeZone);
  }


  private void removeAllEvents(Calendar calendar, List<CalendarEvent> events) {
    for (CalendarEvent event : events) {
      calendar.removeEvent(event);
    }
  }

  private void reAddEventsWithNewTimeZone(Calendar calendar,
                                          List<CalendarEvent> events, ZoneId newTimeZone) {
    ZoneId originalTimeZone = calendar.getTimeZone();

    for (CalendarEvent event : events) {
      CalendarEvent adjustedEvent = convertEventToTimeZone(event, originalTimeZone, newTimeZone);
      calendar.addEvent(adjustedEvent, true);
    }
  }

  /**
   * Returns the list of all available calendar names.
   *
   * @return List of calendar names.
   */
  public List<String> getAllCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }
}