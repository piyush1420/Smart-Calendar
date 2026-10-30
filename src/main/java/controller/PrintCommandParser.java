package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import model.EventStorage;
import view.text.CalendarPrinter;

/**
 * Parses and executes commands for printing calendar events on a specific date
 * or within a date range.
 */
public class PrintCommandParser implements CommandExecutor {
  private final CalendarPrinter calendarPrinter;

  /**
   * Constructs a PrintCommandParser with the given EventStorage.
   *
   * @param eventStorage the EventStorage to be used for printing events
   */
  public PrintCommandParser(EventStorage eventStorage) {
    this.calendarPrinter = new CalendarPrinter(eventStorage);
  }

  /**
   * Executes the given command by printing events on a specific date or within
   * a date range.
   *
   * @param command the command string to be executed
   * @return true if the command was executed successfully, false otherwise
   */
  @Override
  public boolean executeCommand(String command) {
    command = command.trim();

    if (!command.startsWith("print events")) {
      System.out.println("Invalid command. Expected 'print events' command.");
      return false;
    }

    if (command.startsWith("print events on")) {
      return handlePrintEventsOnDate(command);
    }

    if (command.startsWith("print events from")) {
      return handlePrintEventsInRange(command);
    }

    System.out.println("Invalid print command format.");
    return false;
  }

  /**
   * Handles the command to print events on a specific date.
   *
   * @param command the command string to process
   * @return true if the events were printed successfully, false otherwise
   */
  private boolean handlePrintEventsOnDate(String command) {
    try {
      String dateString = command.substring("print events on".length()).trim();
      LocalDate date = LocalDate.parse(dateString,
              DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      calendarPrinter.printEventsOnDate(date);
      return true;
    } catch (DateTimeParseException e) {
      System.out.println("Invalid date format. Expected format: yyyy-MM-dd");
      return false;
    } catch (Exception e) {
      System.out.println("Error processing command: " + e.getMessage());
      return false;
    }
  }

  /**
   * Handles the command to print events within a date range.
   *
   * @param command the command string to process
   * @return true if the events were printed successfully, false otherwise
   */
  private boolean handlePrintEventsInRange(String command) {
    try {
      String rangeString = command.substring("print events from".length()).trim();
      String[] parts = rangeString.split(" to ");
      if (parts.length != 2) {
        System.out.println("Invalid range format. Expected format: " +
                "yyyy-MM-ddTHH:mm to yyyy-MM-ddTHH:mm");
        return false;
      }

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
      LocalDateTime startDateTime = LocalDateTime.parse(parts[0], formatter);
      LocalDateTime endDateTime = LocalDateTime.parse(parts[1], formatter);

      calendarPrinter.printEventsInRange(startDateTime, endDateTime);
      return true;
    } catch (DateTimeParseException e) {
      System.out.println("Invalid date-time format. Expected format: " +
              "yyyy-MM-ddTHH:mm");
      return false;
    } catch (Exception e) {
      System.out.println("Error processing command: " + e.getMessage());
      return false;
    }
  }
}