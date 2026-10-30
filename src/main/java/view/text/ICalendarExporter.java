package view.text;

import java.io.IOException;

/**
 * Interface for exporting calendar events to external formats.
 */
public interface ICalendarExporter {

  /**
   * Exports all events to a file at the specified file path.
   *
   * @param filePath the path to the export file
   * @throws IOException if an I/O error occurs while writing the file
   */
  void exportToFormat(String filePath) throws IOException;
}