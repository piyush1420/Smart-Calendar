package controller;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.CalendarManager;

/**
 * Parses and executes the `edit calendar` command.
 */
public class EditCalendarParser implements CommandExecutor {
  private final CalendarManager calendarManager;

  /**
   * Constructs an EditCalendarParser with the specified CalendarManager.
   *
   * @param calendarManager the calendar manager to use for operations
   */
  public EditCalendarParser(CalendarManager calendarManager) {
    this.calendarManager = calendarManager;
  }

  @Override
  public boolean executeCommand(String command) {
    // Trim the command to remove extra spaces or lines before and after
    command = command.trim();
    // Regex to parse the command (allows extra spaces and lines before/after)
    // Updated pattern to handle names with spaces
    String editCalendarRegex = "^\\s*edit\\s+calendar\\s+--name\\s+(.+?)" +
            "\\s+--property\\s+(name|timezone)\\s+(.+)$";
    Pattern editCalendarPattern = Pattern.compile(editCalendarRegex,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher commandMatcher = editCalendarPattern.matcher(command);
    // Validate the command format
    if (!commandMatcher.matches()) {
      System.out.println("Error: Invalid command format. Expected: edit calendar" +
              " --name <name-of-calendar> --property <property-name> <new-property-value>");
      return false;
    }
    // Extract the calendar name, property, and new value
    String calendarName = commandMatcher.group(1).trim();
    String propertyToEdit = commandMatcher.group(2).trim().toLowerCase();
    String newPropertyValue = commandMatcher.group(3).trim();
    // Handle timezone validation
    if (propertyToEdit.equals("timezone")) {
      try {
        ZoneId.of(newPropertyValue); // Validate the timezone
      } catch (DateTimeException e) {
        System.out.println("Error: Invalid timezone '" + newPropertyValue +
                "'. Please provide a valid timezone (e.g., 'America/New_York').");
        return true;
      }
    }
    // Edit the calendar
    try {
      calendarManager.editCalendar(calendarName, propertyToEdit, newPropertyValue);
      System.out.println("Calendar '" + calendarName + "' updated successfully.");
      return true;
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
      return true;
    }
  }
}