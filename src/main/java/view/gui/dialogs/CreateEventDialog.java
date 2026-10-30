package view.gui.dialogs;

import java.awt.GridLayout;
import java.io.PrintStream;
import java.time.LocalDate;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import controller.CommandParser;


/**
 * A dialog window for creating calendar events. Supports both one-time and
 * recurring events, with customizable fields for name, date, time, location,
 * description, and type.
 */
public class CreateEventDialog extends JDialog {
  private final JTextField eventNameField;
  private final JTextField startDateField;
  private final JTextField startTimeField;
  private final JTextField endDateField;
  private final JTextField endTimeField;
  private final JCheckBox allDayCheckBox;
  private final JCheckBox recurringCheckBox;
  private final JTextField repeatCountField;
  private final JTextField untilDateField;
  private final JTextField untilTimeField;
  private final JCheckBox[] weekdayCheckBoxes;
  private final JButton createBtn;
  private final CommandParser parser;
  private final JTextField locationField;
  private final JTextField descriptionField;
  private final JComboBox<String> typeComboBox;
  private final JLabel untilTimeLabel;

  /**
   * A dialog window for creating calendar events. Supports both one-time and
   *     recurring events, with customizable fields for name, date, time, location,
   *     description, and type.
   */
  public CreateEventDialog(CommandParser parser) {
    this.parser = parser;
    setTitle("Create Event");
    setSize(1000, 400);
    setLocationRelativeTo(null);
    setModal(true);
    setLayout(new GridLayout(0, 2, 5, 5));

    // Final field initialization
    locationField = new JTextField();
    descriptionField = new JTextField();
    typeComboBox = new JComboBox<>(new String[]{"", "public", "private"});

    eventNameField = new JTextField();
    startDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
    startTimeField = new JTextField("09:00");
    endDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
    endTimeField = new JTextField("10:00");

    allDayCheckBox = new JCheckBox("All Day Event");
    recurringCheckBox = new JCheckBox("Recurring Event");
    repeatCountField = new JTextField();
    untilDateField = new JTextField(LocalDate.now().plusWeeks(1).
            format(DateTimeFormatter.ISO_DATE));
    untilTimeField = new JTextField("10:00");
    untilTimeLabel = new JLabel("Repeat Until Time (HH:mm):");
    createBtn = new JButton("Create Event");
    weekdayCheckBoxes = new JCheckBox[7];

    setUpLayout();
    toggleAllDayFields();
    toggleRecurringFields();
  }

  private void setUpLayout() {
    add(new JLabel("Event Name:"));
    add(eventNameField);
    add(new JLabel("Start Date (yyyy-MM-dd):"));
    add(startDateField);
    add(new JLabel("Start Time (HH:mm):"));
    add(startTimeField);
    add(new JLabel("End Date (yyyy-MM-dd):"));
    add(endDateField);
    add(new JLabel("End Time (HH:mm):"));
    add(endTimeField);

    allDayCheckBox.addActionListener(e -> toggleAllDayFields());
    add(new JLabel("All Day:"));
    add(allDayCheckBox);

    recurringCheckBox.addActionListener(e -> toggleRecurringFields());
    add(new JLabel("Recurring:"));
    add(recurringCheckBox);

    JPanel weekdayPanel = new JPanel(new GridLayout(1, 7));
    String[] weekdays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (int i = 0; i < 7; i++) {
      weekdayCheckBoxes[i] = new JCheckBox(weekdays[i]);
      weekdayPanel.add(weekdayCheckBoxes[i]);
    }
    add(new JLabel("Repeat on:"));
    add(weekdayPanel);

    add(new JLabel("Repeat Count:"));
    add(repeatCountField);
    add(new JLabel("Repeat Until (yyyy-MM-dd):"));
    add(untilDateField);
    add(untilTimeLabel);
    add(untilTimeField);

    add(new JLabel("Location (optional):"));
    add(locationField);
    add(new JLabel("Description (optional):"));
    add(descriptionField);
    add(new JLabel("Type (optional):"));
    add(typeComboBox);

    createBtn.addActionListener(e -> createEvent());
    add(new JLabel());
    add(createBtn);
  }

  private void toggleAllDayFields() {
    boolean allDay = allDayCheckBox.isSelected();
    startTimeField.setEnabled(!allDay);
    endTimeField.setEnabled(!allDay);

    boolean recurring = recurringCheckBox.isSelected();
    untilTimeField.setEnabled(!allDay && recurring);
    untilTimeLabel.setEnabled(!allDay && recurring);

    if (allDay) {
      startTimeField.setText("");
      endTimeField.setText("");
    } else {
      startTimeField.setText("09:00");
      endTimeField.setText("10:00");
      if (recurring) {
        untilTimeField.setText("10:00");
      }
    }
  }

  private void toggleRecurringFields() {
    boolean recurring = recurringCheckBox.isSelected();
    boolean allDay = allDayCheckBox.isSelected();

    repeatCountField.setEnabled(recurring);
    untilDateField.setEnabled(recurring);
    untilTimeField.setEnabled(recurring && !allDay);
    untilTimeLabel.setEnabled(recurring && !allDay);

    for (JCheckBox box : weekdayCheckBoxes) {
      box.setEnabled(recurring);
    }
  }

  private void createEvent() {
    try {
      String command = buildEventCommand();

      PrintStream originalOut = System.out;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      System.setOut(ps);

      try {
        boolean success = parser.executeCommand(command);

        System.out.flush();
        System.setOut(originalOut);

        String consoleOutput = baos.toString().trim();

        if (!success || consoleOutput.contains("Error:")) {
          String errorMsg = consoleOutput;
          if (consoleOutput.contains("Error:")) {
            StringBuilder formattedError = new StringBuilder();
            for (String line : consoleOutput.split("\n")) {
              if (line.startsWith("Error:")) {
                formattedError.append(line).append("\n");
              }
            }
            errorMsg = formattedError.toString().trim();
          }
          JOptionPane.showMessageDialog(this,
                  errorMsg.isEmpty() ? "Command execution failed" : errorMsg,
                  "Event Creation Error",
                  JOptionPane.ERROR_MESSAGE);
        } else {
          if (!consoleOutput.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    consoleOutput,
                    "Event Created",
                    JOptionPane.INFORMATION_MESSAGE);
          } else {
            JOptionPane.showMessageDialog(this,
                    "Event created successfully!",
                    "Event Created",
                    JOptionPane.INFORMATION_MESSAGE);
          }
          dispose();
        }
      } finally {
        System.setOut(originalOut);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
              "Error: " + ex.getMessage(),
              "Event Creation Error",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  private String buildEventCommand() {
    String name = eventNameField.getText().trim();
    String startDate = getDate(startDateField.getText().trim());
    String endDate = getDate(endDateField.getText().trim());
    boolean allDay = allDayCheckBox.isSelected();
    boolean recurring = recurringCheckBox.isSelected();

    String extraFields = buildExtraFields();

    LocalDate date = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    LocalDateTime dateTime = date.atStartOfDay();
    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

    if (allDay) {
      if (recurring) {
        return buildRecurringAllDayCommand(name, startDate) + extraFields;
      }
      return String.format("create event %s on %s%s", name, formattedDate, extraFields);
    } else {
      String startTime = getTime(startTimeField.getText().trim());
      String endTime = getTime(endTimeField.getText().trim());
      String startDateTime = startDate + "T" + startTime;
      String endDateTime = endDate + "T" + endTime;

      if (recurring) {
        return buildRecurringTimedCommand(name, startDateTime, endDateTime) + extraFields;
      }
      return String.format("create event %s from %s to %s%s",
              name, startDateTime, endDateTime, extraFields);
    }
  }

  private String buildExtraFields() {
    StringBuilder extraFields = new StringBuilder();
    String location = locationField.getText().trim();
    String description = descriptionField.getText().trim();
    String type = (String) typeComboBox.getSelectedItem();

    if (!location.isEmpty()) {
      extraFields.append(" location ").append(location);
    }
    if (!description.isEmpty()) {
      extraFields.append(" description ").append(description);
    }
    if (type != null && !type.isEmpty()) {
      extraFields.append(" type ").append(type);
    }
    return extraFields.toString();
  }

  private String buildRecurringAllDayCommand(String name, String startDate) {
    String weekdays = getSelectedWeekdays();
    if (!repeatCountField.getText().trim().isEmpty()) {
      int count = getRepeatCount();
      return String.format("create event %s on %s repeats %s for %d times",
              name, startDate, weekdays, count);
    } else if (!untilDateField.getText().trim().isEmpty()) {
      String untilDate = getDate(untilDateField.getText().trim());
      return String.format("create event %s on %s repeats %s until %s",
              name, startDate, weekdays, untilDate);
    }
    throw new IllegalArgumentException("Must specify either repeat count or end date");
  }

  private String buildRecurringTimedCommand(String name, String startDateTime,
                                            String endDateTime) {
    String weekdays = getSelectedWeekdays();
    if (!repeatCountField.getText().trim().isEmpty()) {
      int count = getRepeatCount();
      return String.format("create event %s from %s to %s repeats %s for %d times",
              name, startDateTime, endDateTime, weekdays, count);
    } else if (!untilDateField.getText().trim().isEmpty()) {
      String untilDate = getDate(untilDateField.getText().trim());
      String untilTime = allDayCheckBox.isSelected() ? "00:00" :
              getTime(untilTimeField.getText().trim());
      String formattedUntilDate = untilDate + "T" + untilTime;
      return String.format("create event %s from %s to %s repeats %s until %s",
              name, startDateTime, endDateTime, weekdays, formattedUntilDate);
    }
    throw new IllegalArgumentException("Must specify either repeat count or end date");
  }

  private String getSelectedWeekdays() {
    ArrayList<String> selectedDays = new ArrayList<>();
    String[] dayValues = {"M", "T", "W", "R", "F", "S", "U"};
    for (int i = 0; i < 7; i++) {
      if (weekdayCheckBoxes[i].isSelected()) {
        selectedDays.add(dayValues[i]);
      }
    }
    if (selectedDays.isEmpty()) {
      throw new IllegalArgumentException("Select at least one weekday for recurring event");
    }
    return String.join("", selectedDays);
  }

  private int getRepeatCount() {
    return Integer.parseInt(repeatCountField.getText().trim());
  }

  private String getDate(String date) {
    return LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            .format(DateTimeFormatter.ISO_DATE);
  }

  private String getTime(String time) {
    if (!time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
      throw new IllegalArgumentException("Invalid time format (use HH:mm)");
    }
    return time;
  }

  /**
   * Gets the JTextField containing the start date of the event.
   *
   * @return the JTextField for the start date
   */
  public JTextField getStartDateField() {
    return startDateField;
  }

  /**
   * Gets the JTextField containing the end date of the event.
   *
   * @return the JTextField for the end date
   */
  public JTextField getEndDateField() {
    return endDateField;
  }
}
