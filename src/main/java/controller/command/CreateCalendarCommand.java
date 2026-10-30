package controller.command;

import controller.CreateCalendarParser;
import controller.InvalidCommandException;
import model.CalendarManager;

/**
 * Encapsulates the execution logic for the "create calendar" command.
 */
public class CreateCalendarCommand implements Command {
  private final CreateCalendarParser parser;
  private final String command;

  /**
   * Constructs a CreateCalendarCommand with calendar manager and command string.
   *
   * @param calendarManager the calendar manager model
   * @param command         the command string to parse
   */
  public CreateCalendarCommand(CalendarManager calendarManager, String command) {
    this.parser = new CreateCalendarParser(calendarManager);
    this.command = command;
  }

  /**
   * Executes the create calendar command.
   *
   * @return true if execution succeeded
   * @throws InvalidCommandException if command is invalid
   */
  @Override
  public boolean execute() throws InvalidCommandException {
    return parser.executeCommand(command);
  }
}