package controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.CalendarManager;

/**
 * Parses and executes the 'use calendar' command to switch between different calendars.
 * Implements the CommandExecutor interface to provide command execution capability.
 */
public class UseCalendarParser implements CommandExecutor {
  private final CalendarManager calendarManager;

  /**
   * Constructs a UseCalendarParser with the specified CalendarManager.
   *
   * @param calendarManager the calendar manager to use for calendar operations
   */
  public UseCalendarParser(CalendarManager calendarManager) {
    this.calendarManager = calendarManager;
  }

  /**
   * Executes the 'use calendar' command to switch to a specified calendar.
   *
   * @param command the command string to parse and execute
   * @return true if the command executed successfully, false otherwise
   */
  @Override
  public boolean executeCommand(String command) {
    try {
      // Define the regex pattern for the command
      String useCalendarPattern = "^use\\s+calendar\\s+--name\\s+(.+)$";
      Pattern commandPattern = Pattern.compile(useCalendarPattern, Pattern.CASE_INSENSITIVE);
      Matcher commandMatcher = commandPattern.matcher(command.trim());

      // Check if the command matches the expected format
      if (!commandMatcher.matches()) {
        throw new InvalidCommandException(
                "Error: Invalid command format. Expected: use calendar --name <calName>");
      }

      // Extract and trim the calendar name
      String calendarName = commandMatcher.group(1).trim();

      // Switch to the specified calendar
      calendarManager.useCalendar(calendarName);
      System.out.println("Switched to calendar '" + calendarName + "'.");
      return true;
    } catch (InvalidCommandException e) {
      // Print the error message and return true to continue execution
      System.out.println(e.getMessage());
      return false;
    }
  }
}