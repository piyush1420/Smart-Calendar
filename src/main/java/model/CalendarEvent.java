package model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a calendar event with a subject, start and end date/time, description,
 * location, event type, and recurrence status.
 */
public class CalendarEvent implements Event {
  private String subject;
  private LocalDateTime startDateTime;

  public void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  public void setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
  }

  private LocalDateTime endDateTime;
  private String description;
  private String location;
  private String eventType;
  private boolean isRecurring;

  /**
   * Constructs an all-day event with the given subject and start date.
   *
   * @param subject   the subject of the event
   * @param startDate the start date of the event
   */
  public CalendarEvent(String subject, LocalDate startDate) {
    this(subject, startDate.atStartOfDay(), startDate.atTime(23, 59), "",
            "", "", false);
  }

  /**
   * Constructs an event with the given subject, start date/time, and end date/time.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   */
  public CalendarEvent(String subject, LocalDateTime startDateTime,
                       LocalDateTime endDateTime) {
    this(subject, startDateTime, endDateTime, "", "", "",
            false);
  }

  /**
   * Constructs an event with the given subject, start date/time, end date/time,
   * description, location, and event type.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param description   the description of the event
   * @param location      the location of the event
   * @param eventType     the type of the event (e.g., public or private)
   */
  public CalendarEvent(String subject, LocalDateTime startDateTime,
                       LocalDateTime endDateTime, String description, String location,
                       String eventType) {
    this(subject, startDateTime, endDateTime, description, location, eventType, false);
  }

  /**
   * Constructs an event with all fields, including recurrence status.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param description   the description of the event
   * @param location      the location of the event
   * @param eventType     the type of the event (e.g., public or private)
   * @param isRecurring   whether the event is recurring
   * @throws IllegalArgumentException if start or end date/time is null, or if
   *                                  start date/time is after end date/time
   */
  public CalendarEvent(String subject, LocalDateTime startDateTime,
                       LocalDateTime endDateTime, String description, String location,
                       String eventType, boolean isRecurring) {
    if (startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException(
              "Start and end date/time must not be null.");
    }
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException(
              "Start date/time must be before end date/time.");
    }

    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description;
    this.location = location;
    this.eventType = eventType;
    this.isRecurring = isRecurring;
  }

  /**
   * Gets the subject of the event.
   *
   * @return the subject of the event
   */
  @Override
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of the event.
   *
   * @param subject the subject of the event
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date and time of the event
   */
  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date and time of the event
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Gets the description of the event.
   *
   * @return the description of the event
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of the event.
   *
   * @param description the description of the event
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the location of the event.
   *
   * @return the location of the event
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the location of the event.
   *
   * @param location the location of the event
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Gets the type of the event.
   *
   * @return the type of the event
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * Sets the type of the event.
   *
   * @param eventType the type of the event
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  /**
   * Checks if the event is recurring.
   *
   * @return true if the event is recurring, false otherwise
   */
  public boolean isRecurring() {
    return isRecurring;
  }

  /**
   * Checks if this event conflicts with another event.
   *
   * @param other the other event to check for conflicts
   * @return true if the events conflict, false otherwise
   */
  public boolean conflictsWith(CalendarEvent other) {
    return !this.endDateTime.isBefore(other.startDateTime) &&
            !this.startDateTime.isAfter(other.endDateTime);
  }

  /**
   * Determines if an event is an all-day event. An event is considered all-day
   * if it starts at 00:00 and ends at 23:59 on the same day.
   *
   * @param event the event to check
   * @return true if the event is an all-day event, false otherwise
   */
  public boolean isAllDayEvent(CalendarEvent event) {
    LocalDateTime start = event.getStartDateTime();
    LocalDateTime end = event.getEndDateTime();

    // Check if the event starts at 00:00 and ends at 23:59 on the same day
    return start.toLocalTime().equals(java.time.LocalTime.MIDNIGHT) && // Starts at 00:00
            end.toLocalTime().equals(java.time.LocalTime.of(23, 59)) &&
            // Ends at 23:59
            start.toLocalDate().equals(end.toLocalDate()); // Same day
  }

  /**
   * Returns a string representation of the event.
   *
   * @return a string representation of the event
   */
  @Override
  public String toString() {
    return String.format("Event: %s, Start: %s, End: %s",
            subject, startDateTime, endDateTime);
  }

  public Duration getDuration() {
    return Duration.between(startDateTime, endDateTime);
  }
}