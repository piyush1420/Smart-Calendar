package controller.command;

import controller.InvalidCommandException;

/**
 * The Command interface declares an `execute` method for all concrete commands.
 */
public interface Command {
  /**
   * Executes the command.
   *
   * @return true if the command was executed successfully, false otherwise
   * @throws InvalidCommandException if the command format is invalid
   */
  boolean execute() throws InvalidCommandException;
}