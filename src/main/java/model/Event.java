package model;

import java.time.LocalDateTime;

/**
 * Represents an event with a subject and a start date/time.
 */
public interface Event {

  /**
   * Gets the subject of the event.
   *
   * @return the subject of the event
   */
  String getSubject();

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date and time of the event
   */
  LocalDateTime getStartDateTime();
}