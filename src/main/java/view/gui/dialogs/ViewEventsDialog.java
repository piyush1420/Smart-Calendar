package view.gui.dialogs;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import controller.CommandParser;

/**
 * Dialog for viewing events on a specific day or within a date range.
 * Provides a user interface to execute two commands:
 * - "print events on {date}" - Shows events for a specific day
 * - "print events from {datetime} to {datetime}" - Shows events in a range
 */
public class ViewEventsDialog extends JDialog {
  private final CommandParser parser;
  private final JPanel mainPanel;
  private final JCheckBox dateRangeCheckBox;
  private JSpinner datePicker;
  private JSpinner startTimePicker;
  private JSpinner endDatePicker;
  private JSpinner endTimePicker;
  private final JTextArea resultArea;

  private static final DateTimeFormatter DATE_FORMATTER =
          DateTimeFormatter.ISO_DATE;
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Creates a new dialog for viewing events by date or date range.
   *
   * @param parent The parent frame
   * @param parser The command parser to execute event queries
   */
  public ViewEventsDialog(JFrame parent, CommandParser parser) {
    super(parent, "View Events", true);
    this.parser = parser;

    setSize(600, 500);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout());

    // Main panel with selection controls
    mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    // Create the date picker panel
    JPanel datePanel = createDatePickerPanel();
    mainPanel.add(datePanel);

    // Create the checkbox for date range
    dateRangeCheckBox = new JCheckBox("View events in date range");
    dateRangeCheckBox.addActionListener(e -> toggleDateRangeControls());
    mainPanel.add(dateRangeCheckBox);

    // Create date range panels
    JPanel startTimePanel = createStartTimePanel();
    JPanel endDatePanel = createEndDatePanel();
    JPanel endTimePanel = createEndTimePanel();

    mainPanel.add(startTimePanel);
    mainPanel.add(endDatePanel);
    mainPanel.add(endTimePanel);

    // Add View Events button
    JPanel buttonPanel = createButtonPanel();
    mainPanel.add(buttonPanel);

    // Create results area
    resultArea = new JTextArea();
    resultArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(resultArea);
    scrollPane.setPreferredSize(new Dimension(550, 300));

    // Add components to dialog
    add(mainPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);

    // Initially hide date range controls
    toggleDateRangeControls();
  }

  /**
   * Creates the date picker panel for selecting a specific date.
   *
   * @return The configured date picker panel
   */
  private JPanel createDatePickerPanel() {
    JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    datePanel.add(new JLabel("Date:"));

    SpinnerDateModel dateModel = new SpinnerDateModel();
    dateModel.setValue(new Date());
    datePicker = new JSpinner(dateModel);
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(
            datePicker, "yyyy-MM-dd");
    datePicker.setEditor(dateEditor);
    datePanel.add(datePicker);

    return datePanel;
  }

  /**
   * Creates the panel for selecting start time in a date range.
   *
   * @return The configured start time panel
   */
  private JPanel createStartTimePanel() {
    JPanel startTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    startTimePanel.add(new JLabel("Start Time:"));

    SpinnerDateModel startTimeModel = new SpinnerDateModel();
    startTimeModel.setValue(new Date());
    startTimePicker = new JSpinner(startTimeModel);
    JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(
            startTimePicker, "HH:mm");
    startTimePicker.setEditor(startTimeEditor);
    startTimePanel.add(startTimePicker);

    return startTimePanel;
  }

  /**
   * Creates the panel for selecting end date in a date range.
   *
   * @return The configured end date panel
   */
  private JPanel createEndDatePanel() {
    JPanel endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    endDatePanel.add(new JLabel("End Date:"));

    SpinnerDateModel endDateModel = new SpinnerDateModel();
    endDateModel.setValue(new Date());
    endDatePicker = new JSpinner(endDateModel);
    JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(
            endDatePicker, "yyyy-MM-dd");
    endDatePicker.setEditor(endDateEditor);
    endDatePanel.add(endDatePicker);

    return endDatePanel;
  }

  /**
   * Creates the panel for selecting end time in a date range.
   *
   * @return The configured end time panel
   */
  private JPanel createEndTimePanel() {
    JPanel endTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    endTimePanel.add(new JLabel("End Time:"));

    SpinnerDateModel endTimeModel = new SpinnerDateModel();
    endTimeModel.setValue(new Date());
    endTimePicker = new JSpinner(endTimeModel);
    JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(
            endTimePicker, "HH:mm");
    endTimePicker.setEditor(endTimeEditor);
    endTimePanel.add(endTimePicker);

    return endTimePanel;
  }

  /**
   * Creates the button panel with the View Events button.
   *
   * @return The configured button panel
   */
  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton viewButton = new JButton("View Events");
    viewButton.addActionListener(e -> viewEvents());
    buttonPanel.add(viewButton);
    return buttonPanel;
  }

  /**
   * Toggles the visibility of date range controls based on checkbox status.
   */
  private void toggleDateRangeControls() {
    boolean showDateRange = dateRangeCheckBox.isSelected();

    // Get all components in the main panel
    Component[] components = mainPanel.getComponents();

    // Show/hide date range components based on checkbox
    for (Component component : components) {
      if (component instanceof JPanel) {
        JPanel panel = (JPanel) component;
        if (panel.getComponentCount() > 0
                && panel.getComponent(0) instanceof JLabel) {
          JLabel label = (JLabel) panel.getComponent(0);
          String labelText = label.getText();

          if (labelText.equals("Start Time:")
                  || labelText.equals("End Date:")
                  || labelText.equals("End Time:")) {
            panel.setVisible(showDateRange);
          }
        }
      }
    }

    // Adjust dialog size
    pack();
    setSize(600, 500);
  }

  /**
   * Executes the appropriate command based on user selections and displays
   * the results.
   */
  private void viewEvents() {
    try {
      String command = buildCommand();
      String result = executeCommandWithConsoleCapture(command);
      resultArea.setText(result);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
              "Error viewing events: " + ex.getMessage(),
              "Error",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Builds the command string based on user selections.
   *
   * @return The command to execute
   */
  private String buildCommand() {
    if (dateRangeCheckBox.isSelected()) {
      return buildDateRangeCommand();
    } else {
      return buildSingleDateCommand();
    }
  }

  /**
   * Builds a command for viewing events on a single date.
   *
   * @return The single date command
   */
  private String buildSingleDateCommand() {
    Date dateValue = (Date) datePicker.getValue();
    LocalDate date = convertToLocalDate(dateValue);
    return String.format("print events on %s", date.format(DATE_FORMATTER));
  }

  /**
   * Builds a command for viewing events in a date range.
   *
   * @return The date range command
   */
  private String buildDateRangeCommand() {
    // Get dates and times from spinners
    Date startDateValue = (Date) datePicker.getValue();
    Date startTimeValue = (Date) startTimePicker.getValue();
    Date endDateValue = (Date) endDatePicker.getValue();
    Date endTimeValue = (Date) endTimePicker.getValue();

    // Convert to LocalDateTime
    LocalDate startDate = convertToLocalDate(startDateValue);
    LocalTime startTime = convertToLocalTime(startTimeValue);
    LocalDate endDate = convertToLocalDate(endDateValue);
    LocalTime endTime = convertToLocalTime(endTimeValue);

    LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
    LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

    // Format as command
    return String.format("print events from %s to %s",
            startDateTime.format(DATE_TIME_FORMATTER),
            endDateTime.format(DATE_TIME_FORMATTER));
  }

  /**
   * Converts a java.util.Date to java.time.LocalDate.
   *
   * @param date The Date to convert
   * @return The converted LocalDate
   */
  private LocalDate convertToLocalDate(Date date) {
    return new java.sql.Date(date.getTime()).toLocalDate();
  }

  /**
   * Converts a java.util.Date to java.time.LocalTime.
   *
   * @param time The Date to convert (only time part is used)
   * @return The converted LocalTime
   */
  private LocalTime convertToLocalTime(Date time) {
    return new java.sql.Time(time.getTime()).toLocalTime();
  }

  /**
   * Executes a command and captures the console output.
   *
   * @param command The command to execute
   * @return The captured output as a string
   * @throws Exception If command execution fails
   */
  private String executeCommandWithConsoleCapture(String command)
          throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);

    try {
      System.setOut(ps);
      boolean success = parser.executeCommand(command);

      System.out.flush();
      String output = baos.toString().trim();

      if (!success) {
        if (output.isEmpty()) {
          throw new Exception("Command execution failed");
        } else {
          // If there's output but command failed, return the output
          // which likely contains error messages
          return output;
        }
      }

      return output.isEmpty() ? "No events found." : output;
    } finally {
      System.setOut(originalOut);
    }
  }
}