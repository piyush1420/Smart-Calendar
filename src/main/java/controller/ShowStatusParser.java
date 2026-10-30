package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import model.EventStorage;
import view.text.CalendarPrinter;

/**
 * Handles parsing and execution of status check commands.
 */
public class ShowStatusParser implements CommandExecutor {
  private final CalendarPrinter calendarPrinter;

  /**
   * Initializes with event storage for status checks.
   */
  public ShowStatusParser(EventStorage eventStorage) {
    this.calendarPrinter = new CalendarPrinter(eventStorage);
  }


  @Override
  public boolean executeCommand(String command) {
    if (!command.startsWith("show status on ")) {
      System.out.println("Invalid command format.");
      return false;
    }

    String dateTimeString = command.substring("show status on ".length()).trim();
    try {
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeString,
              DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
      calendarPrinter.showStatusOnDateTime(dateTime);
      return true;
    } catch (DateTimeParseException e) {
      System.out.println("Invalid date/time format.");
      return false;
    }
  }
}