package model;

import java.time.ZoneId;
import java.util.List;

/**
 * Interface defining operations for managing multiple calendars.
 * This provides a contract for calendar management functionality without
 * affecting existing implementations.
 */
public interface ICalendarManager {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     The name of the new calendar
   * @param timezone The timezone for the calendar
   * @throws IllegalArgumentException if calendar with name already exists
   */
  void createCalendar(String name, ZoneId timezone);

  /**
   * Gets the currently active calendar.
   *
   * @return The current calendar or null if none selected
   */
  Calendar getCurrentCalendar();

  /**
   * Gets a calendar by name.
   *
   * @param calendarName The name of the calendar to retrieve
   * @return The Calendar instance or null if not found
   */
  Calendar getCalendar(String calendarName);

  /**
   * Switches to using the specified calendar.
   *
   * @param name The name of the calendar to use
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  void useCalendar(String name);

  /**
   * Edits a property of a calendar.
   *
   * @param name     The name of the calendar to edit
   * @param property The property to edit ("name" or "timezone")
   * @param newValue The new value for the property
   * @throws IllegalArgumentException if calendar not found or invalid property
   */
  void editCalendar(String name, String property, String newValue);

  /**
   * Gets event storage for the current calendar.
   *
   * @return The EventStorage instance or null if no calendar is active
   */
  EventStorage getCurrentCalendarEventStorage();

  /**
   * Transfers events from source storage to current calendar.
   *
   * @param sourceStorage The source storage containing events
   * @return true if transfer was successful, false otherwise
   */
  boolean transferEventsFromStorage(EventStorage sourceStorage);

  /**
   * Gets all available calendar names.
   *
   * @return List of calendar names
   */
  List<String> getAllCalendarNames();

  /**
   * Gets the multi-calendar event storage.
   *
   * @return The MultiCalendarEventStorage instance
   */
  MultiCalendarEventStorage getEventStorage();
}