package view.gui.components;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import controller.CommandParser;

import controller.gui.GUIControllerBridge;

import view.gui.CalendarFrame;

import javax.swing.JComboBox;

import java.lang.reflect.Method;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collections;

import java.util.List;

/**

 * Tests for the CalendarSelector component of the GUI.

 * Verifies calendar selection handling and integration with CalendarFrame.

 * Uses fake implementations of CommandParser, GUIControllerBridge, and CalendarFrame.

 * Tests private methods through reflection to ensure complete coverage.

 */

public class CalendarSelectorTest {

  // FakeCommandParser simulates command execution.

  private class FakeCommandParser extends CommandParser {

    public FakeCommandParser() {

      super(new model.CalendarManager(

              new model.MultiCalendarEventStorage()));

    }

    @Override

    public boolean executeCommand(String command) {

      boolean returnValue = true;

      return returnValue;

    }



  }

  // FakeCalendarFrame captures updateCalendarInfo calls.

  private class FakeCalendarFrame extends CalendarFrame {

    private String lastCalendarName;

    private String lastTimeZone;

    public FakeCalendarFrame(CommandParser parser,

                             model.CalendarManager calendarManager,

                             String initialTimezone) {

      super(parser, calendarManager, initialTimezone);

    }

    @Override

    public void updateCalendarInfo(String calendarName,

                                   String timezone) {

      lastCalendarName = calendarName;

      lastTimeZone = timezone;

    }

    public String getLastCalendarName() {

      return lastCalendarName;

    }

    public String getLastTimeZone() {

      return lastTimeZone;

    }

  }

  // FakeGUIControllerBridge returns preset values.

  private class FakeGUIControllerBridge extends GUIControllerBridge {

    public List<String> calendarNames;

    private String simulatedOutput = "";

    private String calendarTimeZone;

    public FakeGUIControllerBridge(CommandParser parser,

                                   List<String> names, String timeZone) {

      super(parser, null);

      calendarNames = names;

      calendarTimeZone = timeZone;

    }

    @Override

    public List<String> getAllCalendarNames() {

      return calendarNames;

    }

    @Override

    public String getCalendarTimeZone(String calendarName) {

      return calendarTimeZone;

    }

    @Override

    public String executeCommandWithConsoleCapture(String command)

            throws Exception {

      return simulatedOutput;

    }

  }

  private FakeCalendarFrame fakeFrame;

  private CalendarSelector selector;

  @org.junit.Before

  public void setUp() {

    FakeCommandParser fakeParser = new FakeCommandParser();

    List<String> names = new ArrayList<>(

            Arrays.asList("Cal1", "Cal2"));

    FakeGUIControllerBridge fakeBridge = new FakeGUIControllerBridge(

            fakeParser, names, "UTC");

    model.CalendarManager calendarManager = new model.CalendarManager(

            new model.MultiCalendarEventStorage());

    fakeFrame = new FakeCalendarFrame(fakeParser, calendarManager, "UTC");

    selector = new CalendarSelector(fakeParser, fakeFrame, fakeBridge);

    // Use reflection to invoke the now-private method.

    try {

      Method refreshMethod = CalendarSelector.class.

              getDeclaredMethod("refreshCalendars", List.class);

      refreshMethod.setAccessible(true);

      refreshMethod.invoke(selector,

              Collections.singletonList("Cal1"));

    } catch (Exception e) {

      fail("Failed to invoke refreshCalendars: " + e.getMessage());

    }

    // Set the selected calendar to "Cal1".

    ((JComboBox<String>) getField(selector,

            "calendarComboBox")).setSelectedItem("Cal1");

  }

  @org.junit.Test

  public void testHandleCalendarSelection() {

    try {

      // Use reflection to invoke the private method.

      Method method = CalendarSelector.class.

              getDeclaredMethod("handleCalendarSelection");

      method.setAccessible(true);

      method.invoke(selector);

    } catch (Exception e) {

      fail("Failed to invoke handleCalendarSelection: " +

              e.getMessage());

    }

    // Verify that the frame was updated correctly.

    assertEquals("Cal1", fakeFrame.getLastCalendarName());

    assertEquals("UTC", fakeFrame.getLastTimeZone());

  }

  private Object getField(Object obj, String fieldName) {

    try {

      java.lang.reflect.Field field =

              obj.getClass().getDeclaredField(fieldName);

      field.setAccessible(true);

      return field.get(obj);

    } catch (Exception e) {

      fail("Failed to get field '" + fieldName + "': " +

              e.getMessage());

      return null;

    }

  }

}
