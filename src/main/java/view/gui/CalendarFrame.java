package view.gui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

import controller.CommandParser;
import controller.gui.GUIControllerBridge;
import model.CalendarManager;
import view.gui.components.CalendarSelector;
import view.gui.components.MonthView;
import view.gui.components.NavigationBar;
import view.gui.utils.DialogUtils;
import startapp.ai.AiIntegrationService;

/**
 * The CalendarFrame class is the main JFrame window of the Calendar GUI
 * application and now integrates an AI service for advanced features.
 */
public class CalendarFrame extends JFrame {
  private final CommandParser parser;
  private final GUIControllerBridge bridge;
  private final IGUIView viewController;
  private final AiIntegrationService aiService;
  private MonthView monthView;
  private JLabel calendarInfoLabel;
  private NavigationBar navBar;

  /**
   * Constructs the main application window, sets up GUI components,
   * and wires in the AI service.
   *
   * @param parser          the CommandParser for parsing user commands
   * @param calendarManager the CalendarManager for calendar data operations
   * @param initialTimezone the initial timezone for the default calendar
   * @param aiService       the AI integration service for summarization, suggestions, etc.
   */
  public CalendarFrame(CommandParser parser,
                       CalendarManager calendarManager,
                       String initialTimezone,
                       AiIntegrationService aiService) {
    this.parser = parser;
    this.bridge = new GUIControllerBridge(parser, calendarManager);
    this.viewController = new GUIViewImpl(bridge, parser);
    this.aiService = aiService;

    initializeUI();

    // Initialize the default calendar and handle any errors
    try {
      bridge.createDefaultCalendar(initialTimezone);
    } catch (Exception e) {
      DialogUtils.showOutputDialog("Error",
              "Failed to create default calendar: " + e.getMessage());
    }

    updateCalendarInfo("Default", initialTimezone);
    updateNavigationBarMonth();
  }

  // Expose AI service if needed elsewhere
  public AiIntegrationService getAiService() {
    return aiService;
  }

  private void initializeUI() {
    setTitle("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setLocationRelativeTo(null);

    JPanel mainPanel = new JPanel(new BorderLayout());

    navBar = new NavigationBar(parser, this);
    mainPanel.add(navBar, BorderLayout.NORTH);

    mainPanel.add(new CalendarSelector(parser, this, bridge), BorderLayout.WEST);

    monthView = new MonthView(viewController);
    mainPanel.add(monthView, BorderLayout.CENTER);

    calendarInfoLabel = new JLabel();
    mainPanel.add(calendarInfoLabel, BorderLayout.SOUTH);

    add(mainPanel);
  }

  public GUIControllerBridge getBridge() {
    return bridge;
  }

  public IGUIView getViewController() {
    return viewController;
  }

  public void updateCalendarInfo(String calendarName, String timezone) {
    calendarInfoLabel.setText("Calendar: " + calendarName + " | Timezone: " + timezone);
  }

  public void shiftMonth(int offset) {
    monthView.shiftMonth(offset);
    updateNavigationBarMonth();
  }

  private void updateNavigationBarMonth() {
    String monthYear = monthView.getCurrentMonthYear();
    navBar.updateMonthYear(monthYear);
  }
}
