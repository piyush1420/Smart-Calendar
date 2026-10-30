/*
package view.gui;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JLabel;

import controller.CommandParser;
import model.CalendarManager;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

*/
/**
 * Tests for the CalendarFrame GUI component.
 * Verifies proper updating of calendar information and month navigation.
 * Uses reflection to access private components and a fake CommandParser
 *     to isolate GUI testing from backend logic.
 *//*

public class CalendarFrameTest {

  @BeforeClass
  public static void setUpHeadless() {
    // Ensure headless mode is enabled to avoid GUI pop-ups.
    System.setProperty("java.awt.headless", "false");
  }

  // FakeCommandParser that always returns true.
  private class FakeCommandParser extends CommandParser {
    public FakeCommandParser() {
      super(null); // Dummy CalendarManager; not used in our tests.
    }

    @Override
    public boolean executeCommand(String command) {
      return true;
    }
  }

  private CalendarFrame frame;

  @Before
  public void setUp() {
    FakeCommandParser fakeParser = new FakeCommandParser();
    CalendarManager calendarManager = new CalendarManager(new MultiCalendarEventStorage());
    // Create a CalendarFrame with initial timezone "UTC".
    frame = new CalendarFrame(fakeParser, calendarManager, "UTC");
  }

  @Test
  public void testUpdateCalendarInfo() throws Exception {
    frame.updateCalendarInfo("TestCal", "TestTZ");
    Field infoField = CalendarFrame.class.getDeclaredField("calendarInfoLabel");
    infoField.setAccessible(true);
    JLabel infoLabel = (JLabel) infoField.get(frame);
    assertEquals("Calendar: TestCal | Timezone: TestTZ", infoLabel.getText());
  }

  @Test
  public void testShiftMonthUpdatesNavigationBar() throws Exception {
    // Obtain the NavigationBar's month label via reflection.
    Field navBarField = CalendarFrame.class.getDeclaredField("navBar");
    navBarField.setAccessible(true);
    Object navBarObj = navBarField.get(frame);
    Field monthLabelField = navBarObj.getClass().getDeclaredField("monthYearLabel");
    monthLabelField.setAccessible(true);
    JLabel monthLabel = (JLabel) monthLabelField.get(navBarObj);

    String before = monthLabel.getText();
    frame.shiftMonth(1);
    String after = monthLabel.getText();

    // Verify that the label text has changed.
    assertNotEquals(before, after);

    // Also check that it equals the current month/year from the MonthView.
    Field monthViewField = CalendarFrame.class.getDeclaredField("monthView");
    monthViewField.setAccessible(true);
    Object monthViewObj = monthViewField.get(frame);
    Method getCurrentMonthYearMethod = monthViewObj.getClass().getMethod("getCurrentMonthYear");
    String expected = (String) getCurrentMonthYearMethod.invoke(monthViewObj);
    assertEquals(expected, after);
  }
}*/
