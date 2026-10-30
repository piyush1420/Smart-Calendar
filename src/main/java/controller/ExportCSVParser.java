package controller;

import java.io.IOException;

import model.EventStorage;
import view.text.CSVCalendarExporter;
import view.text.ICalendarExporter;

/**
 * Parses and executes commands for exporting calendar events to a CSV file.
 */
public class ExportCSVParser implements CommandExecutor {
  private final ICalendarExporter calendarExporter;

  /**
   * Constructs an ExportCSVParser with the given EventStorage.
   *
   * @param eventStorage the EventStorage to be used for exporting events
   */
  public ExportCSVParser(EventStorage eventStorage) {
    this.calendarExporter = new CSVCalendarExporter(eventStorage);
  }

  /**
   * Executes the given command by exporting the calendar to a CSV file.
   *
   * @param command the command string to be executed
   * @return true if the export was successful, false otherwise
   */
  @Override
  public boolean executeCommand(String command) {
    if (command.startsWith("export cal ")) {
      String filePath = command.substring("export cal ".length()).trim();

      try {
        calendarExporter.exportToFormat(filePath);
        return true;
      } catch (IOException e) {
        System.out.println("Error exporting calendar to CSV: " + e.getMessage());
      }
    } else {
      System.out.println("Invalid command. Expected 'export cal <fileName.csv>'.");
    }
    return false;
  }
}