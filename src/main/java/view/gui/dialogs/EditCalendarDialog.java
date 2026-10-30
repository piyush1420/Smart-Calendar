package view.gui.dialogs;

import java.awt.GridLayout;
import java.time.ZoneId;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.CommandParser;

/**
 * A dialog for editing calendar properties such as the name and timezone.
 * This dialog lets the user choose a calendar, update its properties, and
 * execute the corresponding commands using the provided CommandParser.
 * An optional callback is invoked when the calendar is successfully edited.
 */
public class EditCalendarDialog extends JDialog {
  private final JComboBox<String> calendarCombo;
  private final JTextField nameField;
  private final JComboBox<String> timezoneCombo;
  private final CommandParser parser;
  private final Runnable onCalendarEdited;

  /**
   * Constructs an EditCalendarDialog.
   *
   * @param parser           the command parser for executing calendar edits
   * @param calendarNames    the list of available calendar names
   * @param currentCalendar  the currently selected calendar
   * @param onCalendarEdited a Runnable callback that is executed after the calendar is edited
   */
  public EditCalendarDialog(CommandParser parser, List<String> calendarNames,
                            String currentCalendar, Runnable onCalendarEdited) {
    this.parser = parser;
    this.onCalendarEdited = onCalendarEdited;

    setTitle("Edit Calendar");
    setModal(true);
    setSize(400, 250);
    setLocationRelativeTo(null);

    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

    // Initialize all UI components and event handlers
    this.calendarCombo = new JComboBox<>(calendarNames.toArray(new String[0]));
    this.calendarCombo.setSelectedItem(currentCalendar);
    this.nameField = new JTextField();
    this.timezoneCombo = new JComboBox<>(ZoneId.getAvailableZoneIds().toArray(new String[0]));

    // Set up UI components and add them to the panel
    setupUIComponents(panel);

    add(panel);
  }

  /**
   * Sets up all UI components and adds them to the panel.
   *
   * @param panel The panel to add components to
   */
  private void setupUIComponents(JPanel panel) {
    panel.add(new JLabel("Select Calendar:"));
    panel.add(calendarCombo);
    panel.add(new JLabel("New Name:"));
    panel.add(nameField);
    panel.add(new JLabel("New Timezone:"));
    panel.add(timezoneCombo);
    JButton editBtn = new JButton("Save Changes");
    editBtn.addActionListener(e -> {
      try {
        String oldName = (String) calendarCombo.getSelectedItem();
        String newName = nameField.getText().trim();
        String timezone = (String) timezoneCombo.getSelectedItem();
        if (oldName == null || oldName.isEmpty()) {
          JOptionPane.showMessageDialog(this, "Please select a calendar to edit");
          return;
        }
        boolean updateName = !newName.isEmpty();
        boolean updateTimezone = timezone != null;
        if (updateName) {
          parser.executeCommand(String.format(
                  "edit calendar --name %s --property name %s",
                  oldName, newName));
        }
        if (updateTimezone) {
          String targetName = updateName ? newName : oldName;
          parser.executeCommand(String.format(
                  "edit calendar --name %s --property timezone %s",
                  targetName, timezone));
        }
        dispose();
        if (onCalendarEdited != null) {
          onCalendarEdited.run();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error editing calendar: " + ex.getMessage());
      }
    });
    panel.add(new JLabel());
    panel.add(editBtn);
  }
}