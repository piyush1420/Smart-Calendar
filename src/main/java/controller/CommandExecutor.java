package controller;

/**
 * Interface for executing commands. Implementations of this interface
 * are responsible for processing and executing specific commands.
 */
public interface CommandExecutor {

  /**
   * Executes the given command.
   *
   * @param command the command to be executed as a String
   * @return true if the command was executed successfully, false otherwise
   */
  boolean executeCommand(String command) throws InvalidCommandException;

}