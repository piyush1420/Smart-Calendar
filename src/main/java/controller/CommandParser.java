package controller;

import controller.command.Command;
import controller.command.CopyCommand;
import controller.command.CreateCalendarCommand;
import controller.command.CreateCommand;
import controller.command.EditCalendarCommand;
import controller.command.EditCommand;
import controller.command.ExportCommand;
import controller.command.ImportCommand;
import controller.command.PrintCommand;
import controller.command.ShowStatusCommand;
import controller.command.UseCalendarCommand;
import model.CalendarManager;
import model.EventStorage;

/**
 * Parses and executes commands based on the input string. Delegates specific
 * command execution to appropriate command objects.
 */
public class CommandParser {

  private final CalendarManager calendarManager;
  private EventStorage currentEventStorage;

  /**
   * Constructs a CommandParser with the specified multi-calendar storage and manager.
   */
  public CommandParser(CalendarManager calendarManager) {
    this.calendarManager = calendarManager;
  }

  /**
   * Executes the given command string.
   */
  public boolean executeCommand(String command) throws InvalidCommandException {
    if (command == null || command.trim().isEmpty()) {
      System.out.println("Error: Empty command.");
      return false;
    }

    String mainCommand = extractMainCommand(command);

    // Only allow "create calendar", "edit calendar", and "use" without a current calendar
    if (!mainCommand.equals("use") && !mainCommand.equals("create calendar") &&
            !mainCommand.equals("edit calendar") && calendarManager.getCurrentCalendar() == null) {
      System.out.println("Error: No calendar in use. Use 'use calendar --name <calName>' to " +
              "select a calendar.");
      return false;
    }

    /*//added on next command(print events)
    if (currentEventStorage == null) {
      currentEventStorage = new EventStorage();
    } else {
      // Transfer events from currentEventStorage to current calendar
      if (calendarManager.transferEventsFromStorage(currentEventStorage)) {
        // Create a new empty storage for future use
        currentEventStorage = new EventStorage();
      }
    }*/

    // Create and execute the appropriate command object
    Command cmd = createCommand(mainCommand, command);
    return cmd.execute();
  }

  /**
   * Creates the appropriate command object based on the main command.
   */
  private Command createCommand(String mainCommand, String command) {
    switch (mainCommand) {
      case "create calendar":
        return new CreateCalendarCommand(calendarManager, command);
      case "create":
        return new CreateCommand(calendarManager.getCurrentCalendarEventStorage(), command);
      case "edit calendar":
        return new EditCalendarCommand(calendarManager, command);
      case "edit":
        return new EditCommand(calendarManager.getCurrentCalendarEventStorage(), command);
      case "use":
        return new UseCalendarCommand(calendarManager, command);
      case "copy":
        return new CopyCommand(calendarManager, command);
      case "print":
        return new PrintCommand(calendarManager.getCurrentCalendarEventStorage(), command);
      case "export":
        return new ExportCommand(calendarManager.getCurrentCalendarEventStorage(), command);
      case "import":
        return new ImportCommand(calendarManager.getCurrentCalendarEventStorage(), command);
      case "show":
        return new ShowStatusCommand(calendarManager.getCurrentCalendarEventStorage(), command);
      default:
        return null;
    }
  }

  /**
   * Extracts the main command prefix from the input command.
   */
  private String extractMainCommand(String command) throws InvalidCommandException {
    if (command.startsWith("create calendar")) {
      return "create calendar";
    } else if (command.startsWith("edit calendar")) {
      return "edit calendar";
    } else if (command.startsWith("use")) {
      return "use";
    } else if (command.startsWith("create")) {
      return "create";
    } else if (command.startsWith("edit")) {
      return "edit";
    } else if (command.startsWith("copy")) {
      return "copy";
    } else if (command.startsWith("print")) {
      return "print";
    } else if (command.startsWith("export")) {
      return "export";
    } else if (command.startsWith("show")) {
      return "show";
    } else if (command.startsWith("import")) {
      return "import";
    } else {
      throw new InvalidCommandException("Error: Invalid command. Must start with 'create'," +
              " 'edit', 'use', 'copy', 'print', 'export', or 'show'.");
    }
  }
}