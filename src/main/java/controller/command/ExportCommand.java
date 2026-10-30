package controller.command;

import controller.ExportCSVParser;
import controller.InvalidCommandException;
import model.EventStorage;

/**
 * Encapsulates the execution logic for the "export calendar" command.
 */
public class ExportCommand implements Command {
  private final ExportCSVParser parser;
  private final String command;

  /**
   * Constructs an ExportCommand with event storage and command string.
   *
   * @param eventStorage the event storage model
   * @param command      the command string to parse
   */
  public ExportCommand(EventStorage eventStorage, String command) {
    this.parser = new ExportCSVParser(eventStorage);
    this.command = command;
  }

  /**
   * Executes the export calendar command.
   *
   * @return true if execution succeeded
   * @throws InvalidCommandException if command is invalid
   */
  @Override
  public boolean execute() throws InvalidCommandException {
    return parser.executeCommand(command);
  }
}