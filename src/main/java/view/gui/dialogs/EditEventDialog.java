package view.gui.dialogs;

import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import controller.CommandParser;
import model.CalendarEvent;

/**
 * Dialog window used for editing calendar events. Supports editing single
 * or multiple events, and provides various properties that can be modified.
 */
public class EditEventDialog extends JDialog {
  private final CommandParser parser;
  private final CalendarEvent event;
  private final List<CalendarEvent> eventsToEdit;
  private final boolean isSingleEvent;

  private final JComboBox<String> propertyComboBox;
  private final JTextField newValueField;
  private final JButton submitButton;

  private final JTextField eventNameField;
  private final JTextField startDateTimeField;
  private final JTextField endDateTimeField;
  private final JCheckBox editAllEventsCheckbox;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


  /**
   * Constructs an EditEventDialog used for modifying one or more calendar events.
   * Initializes the dialog layout, populates fields based on whether a single
   *     or multiple events are being edited, and sets up UI components and listeners
   *     to capture user input for event property changes.
   *
   * @param parser the CommandParser used to execute edit commands
   * @param event  the CalendarEvent to edit; if null, the dialog assumes
   *               multiple events will be edited
   */
  public EditEventDialog(CommandParser parser, CalendarEvent event) {
    this(parser, event, null);
  }

  private EditEventDialog(CommandParser parser, CalendarEvent event,
                          List<CalendarEvent> events) {
    this.parser = parser;
    this.event = event;
    this.eventsToEdit = events;
    this.isSingleEvent = event != null;

    setTitle(isSingleEvent ? "Edit Event" : "Edit Multiple Events");
    setSize(500, 300);
    setLocationRelativeTo(null);
    setModal(true);
    setLayout(new GridLayout(0, 2, 5, 5));

    String[] properties = {"subject", "description", "location",
      "event_type", "start", "end"};
    propertyComboBox = new JComboBox<>(properties);
    newValueField = new JTextField();
    submitButton = new JButton("Submit");

    eventNameField = new JTextField();
    startDateTimeField = new JTextField();
    endDateTimeField = new JTextField();
    editAllEventsCheckbox = new JCheckBox("Edit all occurrences");

    if (isSingleEvent) {
      setupForSingleEvent();
    } else {
      setupForMultipleEvents();
    }

    propertyComboBox.addActionListener(e -> updateFieldConstraints());
    submitButton.addActionListener(e -> executeEditCommand());
    updateFieldConstraints();
  }

  private void setupForSingleEvent() {
    eventNameField.setText(event.getSubject());
    eventNameField.setEditable(false);
    startDateTimeField.setText(
            event.getStartDateTime().format(DATE_TIME_FORMATTER));
    startDateTimeField.setEditable(false);
    endDateTimeField.setText(
            event.getEndDateTime().format(DATE_TIME_FORMATTER));
    endDateTimeField.setEditable(false);

    add(new JLabel("Event Name:"));
    add(eventNameField);
    add(new JLabel("Start Date/Time: (yyyy-MM-ddThh:mm)"));
    add(startDateTimeField);
    add(new JLabel("End Date/Time: (yyyy-MM-ddThh:mm)"));
    add(endDateTimeField);
    add(new JLabel("Property to Edit:"));
    add(propertyComboBox);
    add(new JLabel("New Value:"));
    add(newValueField);
    add(new JLabel());
    add(submitButton);

    if (event.isRecurring()) {
      add(new JLabel());
      add(editAllEventsCheckbox);
    }
  }

  private void setupForMultipleEvents() {
    add(new JLabel("Editing " + eventsToEdit.size() + " Events"));
    add(new JLabel());
    add(new JLabel("Property to Edit:"));
    add(propertyComboBox);
    add(new JLabel("New Value:"));
    add(newValueField);
    add(new JLabel());
    add(submitButton);
  }

  private void updateFieldConstraints() {
    String selectedProperty = (String) propertyComboBox.getSelectedItem();
    if ("start".equals(selectedProperty) || "end".equals(selectedProperty)) {
      newValueField.setText(
              DATE_TIME_FORMATTER.format(LocalDateTime.now()));
    } else {
      newValueField.setText("");
    }
  }

  private void executeEditCommand() {
    try {
      String property = (String) propertyComboBox.getSelectedItem();
      String newValue = newValueField.getText().trim();
      if (newValue.isEmpty()) {
        throw new IllegalArgumentException("New value cannot be empty");
      }

      String command = buildEditCommand(property, newValue);
      System.out.println("Executing command: " + command);

      PrintStream originalOut = System.out;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      System.setOut(ps);

      try {
        boolean success = parser.executeCommand(command);
        System.out.flush();
        System.setOut(originalOut);
        handleCommandOutput(success, baos.toString().trim());
      } finally {
        System.setOut(originalOut);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
              "Edit Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private String buildEditCommand(String property, String newValue) {
    boolean needsEscaping =
            property.equals("subject") ||
                    property.equals("location") ||
                    property.equals("description");

    if (isSingleEvent) {
      if (event.isRecurring()) {
        if (editAllEventsCheckbox.isSelected()) {
          return String.format("edit events %s %s %s",
                  property,
                  event.getSubject(),
                  escapeIfNeeded(newValue, needsEscaping));
        } else {
          return String.format("edit event %s %s from %s to %s with %s",
                  property,
                  event.getSubject(),
                  event.getStartDateTime().format(DATE_TIME_FORMATTER),
                  event.getEndDateTime().format(DATE_TIME_FORMATTER),
                  newValue);
        }
      } else {
        return String.format("edit event %s %s from %s to %s with %s",
                property,
                event.getSubject(),
                event.getStartDateTime().format(DATE_TIME_FORMATTER),
                event.getEndDateTime().format(DATE_TIME_FORMATTER),
                newValue);
      }
    } else {
      return String.format("edit events %s %s %s",
              property,
              eventsToEdit.get(0).getSubject(),
              escapeIfNeeded(newValue, needsEscaping));
    }
  }

  private void handleCommandOutput(boolean success, String output) {
    if (!success || output.contains("Error:")) {
      String errorMsg = output;
      if (output.contains("Error:")) {
        StringBuilder formattedError = new StringBuilder();
        for (String line : output.split("\n")) {
          if (line.startsWith("Error:")) {
            formattedError.append(line).append("\n");
          }
        }
        errorMsg = formattedError.toString().trim();
        if (errorMsg.isEmpty()) {
          errorMsg = "Failed to update event(s)";
        }
      }
      JOptionPane.showMessageDialog(this, errorMsg,
              "Edit Error", JOptionPane.ERROR_MESSAGE);
    } else {
      String message = output.isEmpty() ?
              "Event(s) updated successfully!" : output;
      JOptionPane.showMessageDialog(this, message,
              "Success", JOptionPane.INFORMATION_MESSAGE);
      dispose();
    }
  }

  private String escapeIfNeeded(String value, boolean allowEscape) {
    if (allowEscape && value.contains(" ")) {
      return "`" + value + "`";
    }
    return value;
  }
}
