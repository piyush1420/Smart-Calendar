package controller;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.CalendarManager;

/**
 * Parses and executes commands for creating new calendars in the calendar management system.
 * Validates command syntax and calendar parameters before creating new calendar instances.
 */
public class CreateCalendarParser implements CommandExecutor {
  private final CalendarManager calendarManager;

  /**
   * Constructs a CreateCalendarParser with the specified CalendarManager.
   *
   * @param calendarManager the CalendarManager instance to use for calendar creation operations
   */
  public CreateCalendarParser(CalendarManager calendarManager) {
    this.calendarManager = calendarManager;
  }

  /**
   * Executes the calendar creation command by parsing the input string and creating a new calendar.
   *
   * @param command the command string to parse and execute
   * @return true if the calendar was created successfully, false otherwise
   * @throws InvalidCommandException if the command format is invalid
   */
  @Override
  public boolean executeCommand(String command) throws InvalidCommandException {
    if (command == null || command.trim().isEmpty()) {
      System.out.println("Error: Invalid command format. " +
              "Expected: create calendar --name <calName>" +
              " --timezone <timezone>");
      return true;
    }

    try {
      Pattern pattern = Pattern.compile("^\\s*create\\s+calendar\\s" +
              "+--name\\s+(.+?)\\s+--timezone\\s" +
              "+(\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(command);

      if (!matcher.matches()) {
        throw new InvalidCommandException("Error: Invalid command format. Expected: " +
                "create calendar --name <calName> --timezone <timezone>");
      }

      String calName = matcher.group(1).trim();
      String timezoneStr = matcher.group(2).trim();

      try {
        ZoneId timezone = ZoneId.of(timezoneStr);
        calendarManager.createCalendar(calName, timezone);
        System.out.println("Calendar '" + calName + "' created successfully " +
                "with timezone: " + timezone);
        return true;
      } catch (DateTimeException e) {
        System.out.println("Error: Invalid timezone '" + timezoneStr + "'. Please provide" +
                " a valid timezone (e.g., 'America/New_York').");
        return true;
      } catch (IllegalArgumentException e) {
        System.out.println("Error: " + e.getMessage());
        return true;
      }
    } catch (InvalidCommandException e) {
      System.out.println(e.getMessage());
      return false;
    }
  }
}