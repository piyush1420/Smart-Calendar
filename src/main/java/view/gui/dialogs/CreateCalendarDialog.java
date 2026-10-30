package view.gui.dialogs;

import java.awt.GridLayout;
import java.time.ZoneId;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import controller.CommandParser;
import controller.gui.GUIControllerBridge;

/**
 * A dialog window allowing users to create new calendar instances.
 * Collects inputs such as calendar name and timezone.
 */
public class CreateCalendarDialog extends JDialog {
  private JTextField nameField;
  private JComboBox<String> timezoneCombo;
  private final Runnable onCalendarCreated;
  private final GUIControllerBridge bridge;

  /**
   * Constructs the dialog with necessary components and logic handlers.
   *
   * @param parser            the command parser handling user commands
   * @param bridge            GUIControllerBridge for calendar management
   * @param onCalendarCreated a callback executed upon calendar creation
   */
  public CreateCalendarDialog(CommandParser parser,
                              GUIControllerBridge bridge,
                              Runnable onCalendarCreated) {
    this.bridge = bridge;
    this.onCalendarCreated = onCalendarCreated;
    initDialog();
    JPanel panel = buildMainPanel();
    add(panel);
  }

  private void initDialog() {
    setTitle("Create New Calendar");
    setModal(true);
    setSize(400, 200);
    setLocationRelativeTo(null);
  }

  private JPanel buildMainPanel() {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
    panel.add(new JLabel("Calendar Name:"));
    nameField = new JTextField();
    panel.add(nameField);
    panel.add(new JLabel("Timezone:"));
    timezoneCombo = new JComboBox<>(ZoneId.getAvailableZoneIds()
            .toArray(new String[0]));
    panel.add(timezoneCombo);
    JButton createBtn = new JButton("Create");
    createBtn.addActionListener(e -> processCreateAction());
    panel.add(new JLabel());
    panel.add(createBtn);
    return panel;
  }

  private void processCreateAction() {
    try {
      String calendarName = getCalendarName();
      String timezone = getTimezone();
      if (!validateCalendarName(calendarName)) {
        return;
      }
      if (!validateTimezone(timezone)) {
        return;
      }
      bridge.createCalendar(calendarName, timezone);
      dispose();
      runOnCalendarCreated();
    } catch (Exception ex) {
      ex.printStackTrace();
      showError("Error creating calendar: " + ex.getMessage());
    }
  }

  private String getCalendarName() {
    return nameField.getText().trim();
  }

  private String getTimezone() {
    return (String) timezoneCombo.getSelectedItem();
  }

  private boolean validateCalendarName(String name) {
    if (name.isEmpty()) {
      showError("Calendar name cannot be empty");
      return false;
    }
    return true;
  }

  private boolean validateTimezone(String timezone) {
    if (timezone == null || timezone.trim().isEmpty()) {
      showError("Please select a valid timezone");
      return false;
    }
    return true;
  }

  private void runOnCalendarCreated() {
    if (onCalendarCreated != null) {
      onCalendarCreated.run();
    }
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message);
  }
}