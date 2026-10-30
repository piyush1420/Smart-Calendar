package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles editing of calendar events, ensuring property updates and conflict checks.
 */
public class EditEvent {

  private static final DateTimeFormatter EVENT_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private final CalendarEvent event;
  private final EventStorage eventStorage;
  private final List<String> editedProperties = new ArrayList<>();

  /**
   * Constructs an EditEvent instance with a given event and event storage.
   *
   * @param event        The event to be edited.
   * @param eventStorage The event storage system to check for conflicts.
   */
  public EditEvent(CalendarEvent event, EventStorage eventStorage) {
    this.event = event;
    this.eventStorage = eventStorage;
  }

  /**
   * Executes an edit operation on a single property of the event.
   *
   * @param propertyName The name of the property to edit.
   * @param newValue     The new value for the property.
   * @return true if the edit was successful, false otherwise.
   */
  public boolean executeEdit(String propertyName, String newValue) {
    boolean result = updateProperty(propertyName, newValue);
    if (result) {
      System.out.println("Edited event: " + event.getSubject() +
              " - Changed: " + String.join(", ", editedProperties));
    }
    return result;
  }

  /**
   * Executes a batch edit operation on multiple events, ensuring conflicts are avoided.
   *
   * @param events       List of events to be edited.
   * @param property     The property to modify.
   * @param newValue     The new value to assign.
   * @param eventStorage The event storage system to validate conflicts.
   * @return true if at least one event was successfully edited.
   */
  public static boolean executeMultipleEdits(List<CalendarEvent> events, String property,
                                             String newValue, EventStorage eventStorage) {
    boolean anyUpdated = false;
    boolean onlyNonRecurring = true;
    int totalEdited = 0;

    // First check for potential conflicts in time changes
    if (property.equals("start") || property.equals("end")) {
      for (CalendarEvent event : events) {
        if (event.isRecurring()) {
          EditEvent editEvent = new EditEvent(event, eventStorage);
          CalendarEvent tempEvent = createTempEventWithNewTime(event, property, newValue);
          if (editEvent.hasConflictAfterUpdate(tempEvent)) {
            System.out.println("Edit would cause scheduling conflict for event: " +
                    event.getSubject() + " at " + tempEvent.getStartDateTime());
            return false;
          }
        }
      }
    }

    // Perform the actual edits
    for (CalendarEvent event : events) {
      onlyNonRecurring = false;
      EditEvent editEvent = new EditEvent(event, eventStorage);
      if (editEvent.updateProperty(property, newValue)) {
        anyUpdated = true;
        totalEdited++;
        System.out.println("Edited event: " + event.getSubject() +
                " - Changed: " + String.join(", ", editEvent.getEditedProperties()));
      }
    }

    if (!anyUpdated) {
      if (onlyNonRecurring) {
        throw new IllegalArgumentException("Found only non-recurring events, which " +
                "cannot be updated.");
      }
      throw new IllegalArgumentException("Failed to update any events with " +
              "the property: " + property);
    }

    System.out.println("Successfully edited " + totalEdited + " events.");
    return true;
  }

  /**
   * Updates a specified property of an event while checking for conflicts.
   *
   * @param propertyName The name of the property to update.
   * @param newValue     The new value for the property.
   * @return true if the update was successful, false otherwise.
   */
  private boolean updateProperty(String propertyName, String newValue) {
    validateUpdate(newValue);

    try {
      EventProperty property = getEventPropertyFromString(propertyName);
      CalendarEvent tempEvent = createTempEvent(event);

      applyPropertyChange(tempEvent, property, newValue);

      if (hasConflictAfterUpdate(tempEvent)) {
        throw new IllegalArgumentException("Edit would cause scheduling conflict");
      }

      applyPropertyChange(event, property, newValue);
      editedProperties.add(property.name().toLowerCase() + " to " + newValue);
      return true;

    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
      return false;
    }
  }

  private void applyPropertyChange(CalendarEvent targetEvent,
                                   EventProperty property,
                                   String newValue) {
    switch (property) {
      case SUBJECT:
        targetEvent.setSubject(newValue);
        break;
      case DESCRIPTION:
        targetEvent.setDescription(newValue);
        break;
      case LOCATION:
        targetEvent.setLocation(newValue);
        break;
      case EVENT_TYPE:
        targetEvent.setEventType(newValue);
        break;
      case START:
        updateStartTime(targetEvent, newValue);
        break;
      case END:
        updateEndTime(targetEvent, newValue);
        break;
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
  }

  private void updateStartTime(CalendarEvent targetEvent, String newValue) {
    LocalDateTime newStart = parseDateTime(newValue);
    LocalDateTime currentEnd = targetEvent.getEndDateTime();

    if (newStart.isAfter(currentEnd)) {
      throw new IllegalArgumentException(String.format(
              "Start time (%s) cannot be after end time (%s)",
              formatDateTime(newStart),
              formatDateTime(currentEnd)));
    }

    if (targetEvent == event && eventStorage != null) {
      eventStorage.updateEventStartTime(
              event.getStartDateTime(),
              newStart,
              event
      );
    }

    targetEvent.setStartDateTime(newStart);
  }

  private void updateEndTime(CalendarEvent targetEvent, String newValue) {
    LocalDateTime newEnd = parseDateTime(newValue);
    LocalDateTime currentStart = targetEvent.getStartDateTime();

    if (newEnd.isBefore(currentStart)) {
      throw new IllegalArgumentException(String.format(
              "End time (%s) cannot be before start time (%s)",
              formatDateTime(newEnd),
              formatDateTime(currentStart)));
    }

    targetEvent.setEndDateTime(newEnd);
  }

  private CalendarEvent createTempEvent(CalendarEvent original) {
    return new CalendarEvent(
            original.getSubject(),
            original.getStartDateTime(),
            original.getEndDateTime(),
            original.getDescription(),
            original.getLocation(),
            original.getEventType(),
            original.isRecurring()
    );
  }

  private static CalendarEvent createTempEventWithNewTime(CalendarEvent original,
                                                          String property, String newValue) {
    LocalDateTime newStart = original.getStartDateTime();
    LocalDateTime newEnd = original.getEndDateTime();

    if (property.equals("start")) {
      newStart = parseDateTime(newValue);
    } else if (property.equals("end")) {
      newEnd = parseDateTime(newValue);
    }

    return new CalendarEvent(
            original.getSubject(),
            newStart,
            newEnd,
            original.getDescription(),
            original.getLocation(),
            original.getEventType(),
            original.isRecurring()
    );
  }

  private boolean hasConflictAfterUpdate(CalendarEvent updatedEvent) {
    if (eventStorage == null) {
      return false;
    }

    eventStorage.removeEvent(event);
    boolean hasConflict = eventStorage.hasConflict(updatedEvent);
    eventStorage.addEvent(event, false);
    return hasConflict;
  }

  private void validateUpdate(String newValue) {
    if (newValue == null || newValue.isEmpty() || newValue.equals("null")) {
      throw new IllegalArgumentException("Invalid property value");
    }
  }

  private static LocalDateTime parseDateTime(String dateTimeString) {
    try {
      return LocalDateTime.parse(dateTimeString.trim(), EVENT_FORMATTER);
    } catch (Exception e) {
      throw new IllegalArgumentException(
              "Invalid date format. Expected: yyyy-MM-dd'T'HH:mm");
    }
  }

  private String formatDateTime(LocalDateTime dateTime) {
    return dateTime.format(EVENT_FORMATTER);
  }

  private List<String> getEditedProperties() {
    return new ArrayList<>(editedProperties);
  }


  private enum EventProperty {
    SUBJECT, DESCRIPTION, LOCATION, EVENT_TYPE, START, END
  }

  /**
   * Converts a string into the corresponding EventProperty enum value.
   *
   * @param propertyName The property name as a string.
   * @return The corresponding EventProperty enum.
   * @throws IllegalArgumentException If the property name is invalid.
   */
  public static EventProperty getEventPropertyFromString(String propertyName) {
    if (propertyName == null || propertyName.isEmpty()) {
      throw new IllegalArgumentException("Property name cannot be empty");
    }

    try {
      return EventProperty.valueOf(propertyName.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
              "Invalid property name: " + propertyName);
    }
  }
}