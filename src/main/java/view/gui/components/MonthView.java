package view.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import view.gui.IGUIView;

/**
 * MonthView component responsible for displaying a month of days in a grid.
 * Follows Single Responsibility Principle by focusing only on view presentation.
 * Depends on abstraction (IGUIView) rather than concrete implementations.
 */
public class MonthView extends JPanel {
  private final IGUIView controller;
  private YearMonth currentMonth;

  /**
   * Constructs a MonthView with the specified controller.
   *
   * @param controller The controller for handling view interactions
   */
  public MonthView(IGUIView controller) {
    this.controller = controller;
    this.currentMonth = YearMonth.now();
    setLayout(new GridLayout(0, 7));
    initializeDayHeaders();
    refresh();
  }

  /**
   * Initialize the day name headers (Sun, Mon, etc.).
   */
  private void initializeDayHeaders() {
    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : dayNames) {
      add(new JLabel(day, SwingConstants.CENTER));
    }
  }

  /**
   * Refresh the view to display the current month.
   */
  private void refresh() {
    removeAll();
    initializeDayHeaders();

    LocalDate firstOfMonth = currentMonth.atDay(1);
    int skipDays = firstOfMonth.getDayOfWeek().getValue() % 7;

    for (int i = 0; i < skipDays; i++) {
      add(new JLabel(""));
    }

    for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
      JButton dayButton = createDayButton(day);
      add(dayButton);
    }

    revalidate();
    repaint();
  }

  /**
   * Shift the displayed month by the specified offset.
   *
   * @param offset Number of months to shift (positive for future, negative for past)
   */
  public void shiftMonth(int offset) {
    currentMonth = currentMonth.plusMonths(offset);
    refresh();
  }

  /**
   * Get the current month and year as a formatted string.
   *
   * @return The current month and year (e.g. "APRIL 2023")
   */
  public String getCurrentMonthYear() {
    return currentMonth.getMonth().toString() + " " + currentMonth.getYear();
  }

  /**
   * Create a button representing a day in the month.
   *
   * @param day The day number
   * @return A button configured with appropriate event handlers
   */
  private JButton createDayButton(int day) {
    JButton dayButton = new JButton(String.valueOf(day));
    LocalDate date = currentMonth.atDay(day);

    // Left-click shows events
    dayButton.addActionListener(e -> {
      controller.handleDayClicked(date);
      controller.showVisualFeedback(dayButton);
    });

    // Right-click context menu
    dayButton.setComponentPopupMenu(controller.createContextMenuFor(date));

    // Visual indication for days with events
    updateDayButtonAppearance(dayButton, date);

    return dayButton;
  }

  /**
   * Update the appearance of a day button based on whether it has events.
   *
   * @param button The button to update
   * @param date   The date associated with the button
   */
  private void updateDayButtonAppearance(JButton button, LocalDate date) {
    if (controller.hasEventsOn(date)) {
      button.setForeground(Color.BLUE);
      button.setFont(button.getFont().deriveFont(Font.BOLD));
    }
  }
}