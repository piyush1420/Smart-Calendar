package view.gui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import controller.CommandParser;
import controller.gui.GUIControllerBridge;
import view.gui.CalendarFrame;
import view.gui.IGUIView;
import view.gui.utils.DialogUtils;

/**
 * A navigation bar component for the calendar application GUI.
 * This panel provides navigation controls for month selection and action buttons for event
 *     management. It includes:
 * - Month navigation buttons to move forward and backward through months.
 * - Event creation, viewing, and editing buttons.
 * - Import and export functionality for calendar events.
 * The component is designed to work with the CalendarFrame and interfaces with the controller
 *     for handling user actions.
 */
public class NavigationBar extends JPanel {
  private JLabel monthYearLabel;
  private final IGUIView controller;

  /**
   * Navigation bar component for the calendar application.
   * Provides a user interface for navigating through calendar months and accessing various
   *     calendar management functions including creating, viewing, and editing events, as well as
   *     importing and exporting calendar data.
   */
  public NavigationBar(CommandParser parser, CalendarFrame frame) {
    this.controller = frame.getViewController();
    setLayout(new BorderLayout());
    initMonthNavigationSection(frame);
    initActionButtonsSection(parser, frame);
  }

  private void initMonthNavigationSection(CalendarFrame frame) {
    JPanel monthPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER));
    JButton prevMonthBtn = new JButton("<");
    prevMonthBtn.addActionListener(e -> frame.shiftMonth(-1));
    JButton nextMonthBtn = new JButton(">");
    nextMonthBtn.addActionListener(e -> frame.shiftMonth(1));
    monthYearLabel = new JLabel();
    monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    monthPanel.add(prevMonthBtn);
    monthPanel.add(monthYearLabel);
    monthPanel.add(nextMonthBtn);
    add(monthPanel, BorderLayout.CENTER);
  }

  private void initActionButtonsSection(CommandParser parser,
                                        CalendarFrame frame) {
    JPanel actionPanel = new JPanel(
            new FlowLayout(FlowLayout.RIGHT));

    JButton createEventBtn = new JButton("New Event");
    createEventBtn.addActionListener(e ->
            controller.showCreateEventDialog(LocalDate.now()));

    JButton viewEventsBtn = new JButton("View Events");
    viewEventsBtn.addActionListener(e -> controller.showViewEventsDialog());

    JButton editBtn = new JButton("Edit");
    editBtn.addActionListener(e -> promptAndEditEvents());

    JButton importBtn = new JButton("Import");
    importBtn.addActionListener(e -> handleImport(parser));

    JButton exportBtn = new JButton("Export");
    exportBtn.addActionListener(e -> executeExport(frame));

    actionPanel.add(createEventBtn);
    actionPanel.add(viewEventsBtn);
    actionPanel.add(editBtn);
    actionPanel.add(importBtn);
    actionPanel.add(exportBtn);
    add(actionPanel, BorderLayout.EAST);
  }

  private void promptAndEditEvents() {
    String dateStr = JOptionPane.showInputDialog(this,
            "Enter date (YYYY-MM-DD) for events to edit:",
            LocalDate.now().toString());
    if (dateStr != null && !dateStr.trim().isEmpty()) {
      try {
        LocalDate date = LocalDate.parse(dateStr.trim());
        controller.showEditEventsDialog(date);
      } catch (Exception ex) {
        DialogUtils.showOutputDialog("Error",
                "Invalid date format. Please use YYYY-MM-DD");
      }
    }
  }

  private void executeExport(CalendarFrame frame) {
    GUIControllerBridge bridge = frame.getBridge();
    try {
      String result = bridge.executeCommandWithConsoleCapture(
              "export cal events.csv");
      DialogUtils.showOutputDialog("Export Result", result);
    } catch (Exception ex) {
      DialogUtils.showOutputDialog("Export Error",
              ex.getMessage());
    }
  }

  public void updateMonthYear(String text) {
    monthYearLabel.setText(text);
  }

  private void handleImport(CommandParser parser) {
    JFileChooser fileChooser = new JFileChooser();
    if (fileChooser.showOpenDialog(this) ==
            JFileChooser.APPROVE_OPTION) {
      try {
        String filePath = fileChooser.getSelectedFile().getPath();
        boolean success = parser.executeCommand(
                "import " + filePath);
        if (success) {
          JOptionPane.showMessageDialog(this,
                  "Events imported successfully!",
                  "Import Complete",
                  JOptionPane.INFORMATION_MESSAGE);
        } else {
          throw new Exception("Import command failed");
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Import failed: " + ex.getMessage(),
                "Import Error",
                JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
      }
    }
  }
}