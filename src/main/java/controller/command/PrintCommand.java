package controller.command;

import controller.InvalidCommandException;
import controller.PrintCommandParser;
import model.EventStorage;

/**
 * Encapsulates the execution logic for the "print events" calendar command.
 */
public class PrintCommand implements Command {
  private final PrintCommandParser parser;
  private final String command;

  /**
   * Constructs a PrintCommand with event storage and command string.
   *
   * @param eventStorage the event storage model
   * @param command      the command string to parse
   */
  public PrintCommand(EventStorage eventStorage, String command) {
    this.parser = new PrintCommandParser(eventStorage);
    this.command = command;
  }

  /**
   * Executes the print events command.
   *
   * @return true if execution succeeded
   * @throws InvalidCommandException if command is invalid
   */
  @Override
  public boolean execute() throws InvalidCommandException {
    return parser.executeCommand(command);
  }
}