package view.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import controller.CommandParser;
import controller.gui.GUIControllerBridge;
import model.CalendarEvent;
import view.gui.dialogs.CreateEventDialog;
import view.gui.dialogs.EditEventDialog;
import view.gui.dialogs.ViewEventsDialog;
import view.gui.utils.DialogUtils;

/**
 * Controller implementation for calendar view operations.
 * Follows the Single Responsibility Principle by handling only UI control logic.
 * Provides Open/Closed design via clear interfaces.
 * Acts as an intermediate layer between views and the bridge.
 */
public class GUIViewImpl implements IGUIView {
  private final GUIControllerBridge bridge;
  private final CommandParser parser;

  /**
   * Constructs a GUIViewImpl.
   *
   * @param bridge the GUI controller bridge
   * @param parser the command parser
   */
  public GUIViewImpl(GUIControllerBridge bridge,
                     CommandParser parser) {
    this.bridge = bridge;
    this.parser = parser;
  }

  @Override
  public void handleDayClicked(LocalDate date) {
    showEventsForDay(date);
  }

  @Override
  public JPopupMenu createContextMenuFor(LocalDate date) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem createItem = new JMenuItem("Create Event");
    createItem.addActionListener(e -> showCreateEventDialog(date));
    menu.add(createItem);
    JMenuItem editItem = new JMenuItem("Edit Events");
    editItem.addActionListener(e -> showEditEventsDialog(date));
    menu.add(editItem);
    return menu;
  }

  @Override
  public boolean hasEventsOn(LocalDate date) {
    List<CalendarEvent> events = bridge.getEventsForDay(date);
    return !events.isEmpty();
  }

  @Override
  public CommandParser getParser() {
    return parser;
  }

  @Override
  public void showCreateEventDialog(LocalDate date) {
    CreateEventDialog dialog = new CreateEventDialog(parser);
    dialog.getStartDateField().setText(
            date.format(DateTimeFormatter.ISO_DATE));
    dialog.getEndDateField().setText(
            date.format(DateTimeFormatter.ISO_DATE));
    dialog.setVisible(true);
  }

  @Override
  public void showEventsForDay(LocalDate date) {
    try {
      String output = bridge.getEventsOutputForDay(date);
      DialogUtils.showOutputDialog("Events on " + date, output);
    } catch (Exception e) {
      DialogUtils.showOutputDialog("Error",
              "Error showing events: " + e.getMessage());
    }
  }

  @Override
  public void showEditEventsDialog(LocalDate date) {
    try {
      List<CalendarEvent> events = bridge.getEventsForDay(date);
      if (events.isEmpty()) {
        DialogUtils.showOutputDialog("Edit Events",
                "No events found to edit on " + date);
        return;
      }
      if (events.size() == 1) {
        showSingleEventEditDialog(events.get(0));
      } else {
        showEventSelectionDialog(date, events);
      }
    } catch (Exception e) {
      DialogUtils.showOutputDialog("Error",
              "Error editing events: " + e.getMessage());
    }
  }

  @Override
  public void showVisualFeedback(JButton button) {
    button.setBackground(new Color(220, 220, 255));
    new Timer(300, evt -> button.setBackground(null)) {
      {
        setRepeats(false);
        start();
      }
    };
  }

  @Override
  public void showViewEventsDialog() {
    ViewEventsDialog dialog = new ViewEventsDialog(null, parser);
    dialog.setVisible(true);
  }

  // Private helper methods

  /**
   * Shows edit dialog for a single event.
   *
   * @param event the event to edit
   */
  private void showSingleEventEditDialog(CalendarEvent event) {
    EditEventDialog dialog = new EditEventDialog(parser, event);
    dialog.setVisible(true);
  }

  /**
   * Shows a selection dialog for multiple events.
   *
   * @param date   the date of events
   * @param events the list of events
   */
  private void showEventSelectionDialog(LocalDate date, List<CalendarEvent> events) {
    JDialog selectionDialog = new JDialog();
    selectionDialog.setTitle("Select Event to Edit - " + date);
    selectionDialog.setLayout(new BorderLayout());
    selectionDialog.setSize(400, 300);

    DefaultListModel<String> listModel = new DefaultListModel<>();
    for (CalendarEvent event : events) {
      listModel.addElement(formatEventForList(event));
    }
    JList<String> eventsList = new JList<>(listModel);
    selectionDialog.add(new JScrollPane(eventsList),
            BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    JButton editButton = new JButton("Edit Selected");
    JButton editAllButton = new JButton("Edit All");
    JButton cancelButton = new JButton("Cancel");

    editButton.addActionListener(e -> {
      int index = eventsList.getSelectedIndex();
      if (index >= 0) {
        selectionDialog.dispose();
        showSingleEventEditDialog(events.get(index));
      }
    });
    editAllButton.addActionListener(e -> {
      selectionDialog.dispose();
      editEventsSequentially(events, 0);
    });
    cancelButton.addActionListener(e -> selectionDialog.dispose());

    buttonPanel.add(editButton);
    buttonPanel.add(editAllButton);
    buttonPanel.add(cancelButton);
    selectionDialog.add(buttonPanel, BorderLayout.SOUTH);
    selectionDialog.setVisible(true);
  }

  /**
   * Formats an event for display in a list.
   *
   * @param event the event to format
   * @return formatted event string
   */
  private String formatEventForList(CalendarEvent event) {
    return String.format("%s (%s - %s)",
            event.getSubject(),
            event.getStartDateTime().toLocalTime(),
            event.getEndDateTime().toLocalTime());
  }

  /**
   * Recursively edits events sequentially.
   *
   * @param events the list of events
   * @param index  the current index
   */
  private void editEventsSequentially(List<CalendarEvent> events, int index) {
    if (index < events.size()) {
      EditEventDialog dialog = new EditEventDialog(parser, events.get(index));
      dialog.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          editEventsSequentially(events, index + 1);
        }
      });
      dialog.setVisible(true);
    }
  }
}