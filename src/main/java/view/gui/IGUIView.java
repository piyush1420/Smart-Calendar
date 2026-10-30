package view.gui;

import java.time.LocalDate;

import javax.swing.JPopupMenu;
import javax.swing.JButton;

import controller.CommandParser;

/**
 * Interface defining controller operations for the calendar view.
 * Follows Interface Segregation Principle by providing a focused interface.
 * Enables Dependency Inversion by allowing views to depend on abstraction.
 */
public interface IGUIView {
  /**
   * Handle when a user clicks on a day in the calendar.
   *
   * @param date The date that was clicked
   */
  void handleDayClicked(LocalDate date);

  /**
   * Create a context menu for the specified date.
   *
   * @param date The date to create context menu for
   * @return The popup menu with appropriate options
   */
  JPopupMenu createContextMenuFor(LocalDate date);

  /**
   * Check if the specified date has events.
   *
   * @param date The date to check
   * @return true if events exist for the date, false otherwise
   */
  boolean hasEventsOn(LocalDate date);

  /**
   * Show the create event dialog for the specified date.
   *
   * @param date The date to create an event on
   */
  void showCreateEventDialog(LocalDate date);

  /**
   * Show events for the specified day in a dialog.
   *
   * @param date The date to show events for
   */
  void showEventsForDay(LocalDate date);

  /**
   * Show the edit events dialog for the specified date.
   *
   * @param date The date to edit events for
   */
  void showEditEventsDialog(LocalDate date);

  /**
   * Show visual feedback for a button (e.g., highlight effect).
   *
   * @param button The button to show feedback for
   */
  void showVisualFeedback(JButton button);

  /**
   * Get the command parser instance.
   *
   * @return The command parser
   */
  CommandParser getParser();

  /**
   * Show the view events dialog for browsing events by date or date range.
   * This dialog allows users to select a date or date range and view
   * events for that period.
   */
  void showViewEventsDialog();
}