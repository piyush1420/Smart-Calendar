package view.gui.components;

import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import controller.CommandParser;
import controller.gui.GUIControllerBridge;
import view.gui.CalendarFrame;
import view.gui.dialogs.CreateCalendarDialog;
import view.gui.dialogs.EditCalendarDialog;
import view.gui.utils.DialogUtils;

/**
 * Panel for selecting and managing calendars.
 * Follows Single Responsibility Principle by focusing only on
 * calendar selection UI.
 */
public class CalendarSelector extends JPanel {
  private final JComboBox<String> calendarComboBox;
  private final GUIControllerBridge bridge;
  private final CommandParser parser;
  private final CalendarFrame frame;

  /**
   * Constructs a CalendarSelector panel component to manage calendar selection.
   * Includes functionality for selecting, creating, and editing calendars.
   *
   * @param parser the CommandParser instance for executing calendar commands
   * @param frame  the CalendarFrame that contains this selector component
   * @param bridge the GUIControllerBridge facilitating backend interactions
   */
  public CalendarSelector(CommandParser parser, CalendarFrame frame,
                          GUIControllerBridge bridge) {
    this.bridge = bridge;
    this.parser = parser;
    this.frame = frame;

    setLayout(new FlowLayout(FlowLayout.LEFT));
    add(new JLabel("Current Calendar:"));
    List<String> calendars = bridge.getAllCalendarNames();
    calendarComboBox = new JComboBox<>(calendars.toArray(new String[0]));
    calendarComboBox.addActionListener(e -> handleCalendarSelection());
    add(calendarComboBox);

    JButton createCalendarBtn = new JButton("New Calendar");
    createCalendarBtn.addActionListener(e -> showCreateCalendarDialog());
    add(createCalendarBtn);

    JButton editCalendarBtn = new JButton("Edit Calendar");
    editCalendarBtn.addActionListener(e -> showEditCalendarDialog());
    add(editCalendarBtn);
  }

  /**
   * Handle calendar selection from dropdown.
   */
  private void handleCalendarSelection() {
    String selectedCalendar = (String) calendarComboBox.getSelectedItem();
    if (selectedCalendar != null && !selectedCalendar.trim().isEmpty()) {
      try {
        String result = bridge.executeCommandWithConsoleCapture(
                "use calendar --name " + selectedCalendar);
        frame.updateCalendarInfo(selectedCalendar,
                bridge.getCalendarTimeZone(selectedCalendar));

        // Show any messages from the command.
        if (!result.isEmpty()) {
          DialogUtils.showOutputDialog("Calendar Selection", result);
        }
      } catch (Exception ex) {
        DialogUtils.showOutputDialog("Error",
                "Failed to switch calendar: " + ex.getMessage());
      }
    }
  }

  /**
   * Show dialog for creating a new calendar.
   */
  private void showCreateCalendarDialog() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(parser, bridge, () -> {
      List<String> updatedCalendars = bridge.getAllCalendarNames();
      refreshCalendars(updatedCalendars);
    }
    );
    dialog.setVisible(true);
  }

  /**
   * Show dialog for editing an existing calendar.
   */
  private void showEditCalendarDialog() {
    String currentCalendar = (String) calendarComboBox.getSelectedItem();
    EditCalendarDialog dialog = new EditCalendarDialog(parser, bridge.getAllCalendarNames(),
        currentCalendar, () -> {
      List<String> updatedCalendars = bridge.getAllCalendarNames();
      refreshCalendars(updatedCalendars);
      frame.updateCalendarInfo((String) calendarComboBox.getSelectedItem(),
                      bridge.getCalendarTimeZone((String) calendarComboBox.getSelectedItem()));
    }
    );
    dialog.setVisible(true);
  }

  /**
   * Refresh the list of calendars in the dropdown.
   *
   * @param calendarNames Updated list of calendar names.
   */
  private void refreshCalendars(List<String> calendarNames) {
    calendarComboBox.removeAllItems();
    for (String name : calendarNames) {
      calendarComboBox.addItem(name);
    }
  }
}