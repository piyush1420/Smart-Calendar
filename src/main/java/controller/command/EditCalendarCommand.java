package controller.command;

import controller.EditCalendarParser;
import controller.InvalidCommandException;
import model.CalendarManager;

/**
 * Encapsulates the execution logic for the "edit calendar" command.
 */
public class EditCalendarCommand implements Command {
  private final EditCalendarParser parser;
  private final String command;

  /**
   * Constructs an EditCalendarCommand with calendar manager and command string.
   *
   * @param calendarManager the calendar manager model
   * @param command         the command string to parse
   */
  public EditCalendarCommand(CalendarManager calendarManager, String command) {
    this.parser = new EditCalendarParser(calendarManager);
    this.command = command;
  }

  /**
   * Executes the edit calendar command.
   *
   * @return true if execution succeeded
   * @throws InvalidCommandException if command is invalid
   */
  @Override
  public boolean execute() throws InvalidCommandException {
    return parser.executeCommand(command);
  }
}