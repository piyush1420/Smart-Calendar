package controller;

/**
 * Custom exception class for invalid command formats.
 * This exception is thrown when a command does not match the expected format.
 */
public class InvalidCommandException extends Exception {
  /**
   * Constructs an InvalidCommandException with a default error message.
   */
  public InvalidCommandException() {
    super("Invalid command format.");
  }

  /**
   * Constructs an InvalidCommandException with a custom error message.
   *
   * @param message the custom error message
   */
  public InvalidCommandException(String message) {
    super(message);
  }

  /**
   * Constructs an InvalidCommandException with a custom error message and a cause.
   *
   * @param message the custom error message
   * @param cause   the cause of the exception
   */
  public InvalidCommandException(String message, Throwable cause) {
    super(message, cause);
  }
}