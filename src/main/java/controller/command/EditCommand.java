package controller.command;

import controller.EditCommandParser;
import controller.InvalidCommandException;
import model.EventStorage;

/**
 * Encapsulates the execution logic for the "edit event" calendar command.
 */
public class EditCommand implements Command {
  private final EditCommandParser parser;
  private final String command;

  /**
   * Constructs an EditCommand with event storage and command string.
   *
   * @param eventStorage the event storage model
   * @param command      the command string to parse
   */
  public EditCommand(EventStorage eventStorage, String command) {
    this.parser = new EditCommandParser(eventStorage);
    this.command = command;
  }

  /**
   * Executes the edit event command.
   *
   * @return true if execution succeeded
   * @throws InvalidCommandException if command is invalid
   */
  @Override
  public boolean execute() throws InvalidCommandException {
    try {
      return parser.executeCommand(command);
    } catch (IllegalArgumentException ex) {
      handleError(ex);
      return true; // or false depending on your requirements
    }
  }

  /**
   * Handles errors that occur during the execution of a command.
   *
   * @param ex The exception that occurred.
   */
  private void handleError(Exception ex) {
    System.out.println("Error : " + ex.getMessage());
  }
}