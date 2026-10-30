package controller.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import controller.CommandParser;
import model.Calendar;
import model.CalendarEvent;
import model.CalendarManager;

/**
 * Acts as a controller in the MVC architecture, bridging between GUI actions and the model.
 * Responsible for:
 * - Translating user actions into model operations
 * - Executing commands through the command parser
 * - Retrieving data from the model to display in the view
 */
public class GUIControllerBridge {
  private final CommandParser parser;
  private final CalendarManager calendarManager;

  public GUIControllerBridge(CommandParser parser, CalendarManager calendarManager) {
    this.parser = parser;
    this.calendarManager = calendarManager;
  }

  /**
   * Creates a default calendar with the specified timezone.
   */
  public void createDefaultCalendar(String timezoneId) {
    try {
      parser.executeCommand("create calendar --name Default --timezone " + timezoneId);
      parser.executeCommand("use calendar --name Default");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates a default calendar with the system's default timezone.
   */
  public void createDefaultCalendar() {
    createDefaultCalendar(java.util.TimeZone.getDefault().getID());
  }

  /**
   * Gets the current system timezone.
   */
  public String getCurrentTimeZone() {
    return java.util.TimeZone.getDefault().getID();
  }

  /**
   * Gets all events for a specific day from the model.
   */
  public List<CalendarEvent> getEventsForDay(LocalDate date) {
    try {
      Calendar currentCalendar = calendarManager.getCurrentCalendar();
      if (currentCalendar == null) {
        return Collections.emptyList();
      }
      return currentCalendar.getEventsOnDate(date.atStartOfDay());
    } catch (Exception e) {
      System.err.println("Error getting events: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Gets events output for a specific day using the command parser.
   */
  public String getEventsOutputForDay(LocalDate date) {
    try {
      String command = "print events on " + date.format(DateTimeFormatter.ISO_DATE);
      return executeCommandWithConsoleCapture(command);
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Gets all calendar names from the model.
   */
  public List<String> getAllCalendarNames() {
    return calendarManager.getAllCalendarNames();
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   */
  public void createCalendar(String name, String timezone) {
    try {
      parser.executeCommand("create calendar --name " + name + " --timezone " + timezone);
      parser.executeCommand("use calendar --name " + name);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the timezone for a specific calendar.
   */
  public String getCalendarTimeZone(String calendarName) {
    if (calendarName == null || calendarName.trim().isEmpty()) {
      return java.util.TimeZone.getDefault().getID();
    }
    try {
      Calendar calendar = calendarManager.getCalendar(calendarName);
      if (calendar != null) {
        return calendar.getTimeZone().getId();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return java.util.TimeZone.getDefault().getID();
  }

  /**
   * Gets the command parser for direct use by dialogs.
   */
  public CommandParser getParser() {
    return parser;
  }

  /**
   * Gets the calendar manager for direct use by dialogs.
   */
  public CalendarManager getCalendarManager() {
    return calendarManager;
  }

  /**
   * Executes a command and captures all console output including errors.
   *
   * @param command The command to execute
   * @return The captured console output
   * @throws Exception if the command execution fails
   */
  public String executeCommandWithConsoleCapture(String command) throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);

    try {
      System.setOut(ps);
      boolean success = parser.executeCommand(command);

      System.out.flush();
      String output = baos.toString().trim();

      if (!success) {
        if (output.isEmpty()) {
          throw new Exception("Command execution failed");
        } else {
          // If there's output but command failed, return the output
          // which likely contains error messages
          return output;
        }
      }

      return output;
    } finally {
      System.setOut(originalOut);
    }
  }
}