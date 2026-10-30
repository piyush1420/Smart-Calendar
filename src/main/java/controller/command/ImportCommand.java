package controller.command;

import controller.CalendarCSVImporter;
import controller.InvalidCommandException;
import model.EventStorage;

/**
 * Command to import events from a CSV file.
 */
public class ImportCommand implements Command {

  private final EventStorage eventStorage;
  private final String command;

  /**
   * Constructs an ImportCommand.
   *
   * @param eventStorage the event storage to use
   * @param command      the import command string
   */
  public ImportCommand(EventStorage eventStorage,
                       String command) {
    this.eventStorage = eventStorage;
    this.command = command;
  }

  /**
   * Executes the import command.
   *
   * @return true if import is successful
   * @throws InvalidCommandException if an error occurs
   */
  @Override
  public boolean execute()
          throws InvalidCommandException {
    if (command.startsWith("import ")) {
      String filePath = command.substring(
              "import ".length()).trim();
      CalendarCSVImporter importer =
              new CalendarCSVImporter(eventStorage);
      try {
        importer.importFromCSV(filePath);
        System.out.println("Events imported from: " +
                filePath);
        return true;
      } catch (Exception e) {
        throw new InvalidCommandException(
                "Error importing: " + e.getMessage());
      }
    }
    throw new InvalidCommandException(
            "Invalid import command");
  }
}