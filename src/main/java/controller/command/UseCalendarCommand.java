package controller.command;

import controller.InvalidCommandException;
import controller.UseCalendarParser;
import model.CalendarManager;

/**
 * Encapsulates the execution logic for the "use calendar" command.
 */
public class UseCalendarCommand implements Command {
  private final UseCalendarParser parser;
  private final String command;

  /**
   * Constructs a UseCalendarCommand with calendar manager and command string.
   *
   * @param calendarManager the calendar manager model
   * @param command         the command string to parse
   */
  public UseCalendarCommand(CalendarManager calendarManager, String command) {
    this.parser = new UseCalendarParser(calendarManager);
    this.command = command;
  }

  /**
   * Executes the use calendar command.
   *
   * @return true if execution succeeded
   * @throws InvalidCommandException if command is invalid
   */
  @Override
  public boolean execute() throws InvalidCommandException {
    try {
      return parser.executeCommand(command);
    } catch (IllegalArgumentException ex) {
      System.out.println("Error: " + ex.getMessage());
      return true;
    }
  }
}