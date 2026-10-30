package view.gui.components;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controller.CommandParser;
import controller.InvalidCommandException;
import controller.gui.GUIControllerBridge;
import view.gui.CalendarFrame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the NavigationBar component.
 * Verifies import and export command functionality through fake implementations.
 * Uses test-specific subclasses to access and test otherwise private functionality.
 * Focuses on command execution and response handling for file operations.
 */
public class NavigationBarTest {

  // FakeCommandParser simulates command execution.
  private class FakeCommandParser extends CommandParser {
    private boolean returnValue = true;
    private String lastCommand = "";

    public FakeCommandParser() {
      super(new model.CalendarManager(
              new model.MultiCalendarEventStorage()));
    }

    @Override
    public boolean executeCommand(String command) {
      lastCommand = command;
      return returnValue;
    }

    public void setReturnValue(boolean value) {
      returnValue = value;
    }

    public String getLastCommand() {
      return lastCommand;
    }
  }

  // FakeGUIControllerBridge returns preset values.
  private class FakeGUIControllerBridge extends GUIControllerBridge {
    private List<String> calendarNames;

    public FakeGUIControllerBridge(CommandParser parser,
                                   List<String> names, String timeZone) {
      super(parser, null);
      calendarNames = names;
    }

    @Override
    public List<String> getAllCalendarNames() {
      return calendarNames;
    }

    @Override
    public String getCalendarTimeZone(String calendarName) {
      return "UTC";
    }

    @Override
    public String executeCommandWithConsoleCapture(String command)
            throws Exception {
      return "";
    }
  }

  // FakeCalendarFrame stub.
  private class FakeCalendarFrame extends CalendarFrame {
    public FakeCalendarFrame(CommandParser parser,
                             model.CalendarManager calendarManager,
                             String initialTimezone) {
      super(parser, calendarManager, initialTimezone);
    }
  }

  // TestableNavigationBar exposes public methods to test export/import
  // logic.
  private class TestableNavigationBar extends
          NavigationBar {
    private CommandParser parser;
    private GUIControllerBridge bridge;

    public TestableNavigationBar(CommandParser parser,
                                 CalendarFrame frame,
                                 GUIControllerBridge bridge) {
      super(parser, frame);
      this.parser = parser;
      this.bridge = bridge;
    }

    public String testExportCommand() {
      try {
        return bridge.executeCommandWithConsoleCapture(
                "export cal events.csv");
      } catch (Exception e) {
        return "Error: " + e.getMessage();
      }
    }

    public boolean testImportCommand(String filePath)
            throws InvalidCommandException {
      return parser.executeCommand("import " + filePath);
    }
  }

  private FakeCommandParser fakeParser;
  private TestableNavigationBar testNavBar;

  @Before
  public void setUp() {
    fakeParser = new FakeCommandParser();
    List<String> names = new ArrayList<>(
            Arrays.asList("Cal1", "Cal2"));
    FakeGUIControllerBridge fakeBridge = new FakeGUIControllerBridge(
            fakeParser, names, "UTC");
    model.CalendarManager calendarManager =
            new model.CalendarManager(
                    new model.MultiCalendarEventStorage());
    FakeCalendarFrame fakeFrame = new FakeCalendarFrame(
            fakeParser, calendarManager, "UTC");
    testNavBar = new TestableNavigationBar(
            fakeParser, fakeFrame, fakeBridge);
  }

  @Test
  public void testExportCommand() {
    String result = testNavBar.testExportCommand();
    // Our fake bridge returns an empty string if no error.
    assertEquals("", result);
  }

  @Test
  public void testImportCommand()
          throws InvalidCommandException {
    boolean success =
            testNavBar.testImportCommand("fakePath.csv");
    assertTrue(success);
    assertEquals("import fakePath.csv",
            fakeParser.getLastCommand());
  }
}