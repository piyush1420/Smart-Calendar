package controller.command;

import controller.CreateCommandParser;
import controller.InvalidCommandException;
import model.EventStorage;

/**
 * Encapsulates the execution logic for the "create event" calendar command.
 */
public class CreateCommand implements Command {
  private final CreateCommandParser parser;
  private final String command;

  /**
   * Constructs a CreateCommand with event storage and command string.
   *
   * @param eventStorage the event storage model
   * @param command      the command string to parse
   */
  public CreateCommand(EventStorage eventStorage, String command) {
    this.parser = new CreateCommandParser(eventStorage);
    this.command = command;
  }

  /**
   * Executes the create event command.
   *
   * @return true if execution succeeded
   * @throws InvalidCommandException if command is invalid
   */
  @Override
  public boolean execute() throws InvalidCommandException {
    return parser.executeCommand(command);
  }
}