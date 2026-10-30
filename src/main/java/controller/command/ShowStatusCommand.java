package controller.command;

import controller.InvalidCommandException;
import controller.ShowStatusParser;
import model.EventStorage;

/**
 * Encapsulates the execution logic for the "show status" calendar command.
 */
public class ShowStatusCommand implements Command {
  private final ShowStatusParser parser;
  private final String command;

  /**
   * Constructs a ShowStatusCommand with event storage and command string.
   *
   * @param eventStorage the event storage model
   * @param command      the command string to parse
   */
  public ShowStatusCommand(EventStorage eventStorage, String command) {
    this.parser = new ShowStatusParser(eventStorage);
    this.command = command;
  }

  /**
   * Executes the show status command.
   *
   * @return true if execution succeeded
   * @throws InvalidCommandException if command is invalid
   */
  @Override
  public boolean execute() throws InvalidCommandException {
    return parser.executeCommand(command);
  }
}