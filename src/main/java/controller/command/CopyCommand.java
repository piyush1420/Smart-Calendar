package controller.command;

import controller.CopyEventParser;
import controller.InvalidCommandException;
import model.CalendarManager;

/**
 * Encapsulates the execution logic for the "copy event" calendar command.
 */
public class CopyCommand implements Command {
  private final CopyEventParser parser;
  private final String command;

  /**
   * Constructs a CopyCommand with calendar manager and command string.
   *
   * @param calendarManager the calendar manager model
   * @param command         the command string to parse
   */
  public CopyCommand(CalendarManager calendarManager, String command) {
    this.parser = new CopyEventParser(calendarManager);
    this.command = command;
  }

  /**
   * Executes the copy event command.
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