package view.gui.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.awt.Component;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import controller.CommandParser;
import view.gui.IGUIView;

/**
 * Tests for the MonthView GUI component.
 * Verifies month navigation, day selection, and event indicator functionality.
 * Uses a fake IGUIView implementation to test interactions with the controller.
 * Runs in headless mode to prevent actual GUI display during testing.
 */
public class MonthViewTest {

  // Fake implementation of IGUIView to simulate backend
  // behavior.
  private class FakeIGUIView implements IGUIView {
    public LocalDate lastClickedDate = null;
    public boolean visualFeedbackCalled = false;

    @Override
    public void handleDayClicked(LocalDate date) {
      lastClickedDate = date;
    }

    @Override
    public JPopupMenu createContextMenuFor(LocalDate date) {
      return new JPopupMenu(); // Minimal implementation.
    }

    @Override
    public boolean hasEventsOn(LocalDate date) {
      // For testing, assume events exist only on the 15th.
      return date.getDayOfMonth() == 15;
    }

    @Override
    public CommandParser getParser() {
      return null;
    }

    @Override
    public void showCreateEventDialog(LocalDate date) {
      // No-op for testing.
    }

    @Override
    public void showEventsForDay(LocalDate date) {
      // No-op for testing.
    }

    @Override
    public void showEditEventsDialog(LocalDate date) {
      // No-op for testing.
    }

    @Override
    public void showVisualFeedback(JButton button) {
      visualFeedbackCalled = true;
    }

    @Override
    public void showViewEventsDialog() {
      // No-op for testing.
    }
  }

  private FakeIGUIView fakeController;
  private MonthView monthView;

  @BeforeClass
  public static void setUpHeadless() {
    // Enable headless mode to avoid GUI popups.
    System.setProperty("java.awt.headless", "true");
  }

  @Before
  public void setUp() {
    fakeController = new FakeIGUIView();
    monthView = new MonthView(fakeController);
  }

  @Test
  public void testGetCurrentMonthYear() {
    YearMonth currentMonth = YearMonth.now();
    String expected = currentMonth.getMonth().toString() + " " +
            currentMonth.getYear();
    assertEquals("MonthView should return the current " +
            "month and year", expected, monthView.getCurrentMonthYear());
  }

  @Test
  public void testShiftMonth() {
    String before = monthView.getCurrentMonthYear();
    monthView.shiftMonth(1);
    String after = monthView.getCurrentMonthYear();
    assertNotEquals("MonthView should update month after " +
            "shifting", before, after);
  }

  @Test
  public void testRefreshComponentCount() {
    YearMonth ym = YearMonth.now();
    int headerCount = 7;
    int skipDays = ym.atDay(1).getDayOfWeek().getValue() % 7;
    int dayButtons = ym.lengthOfMonth();
    int expectedComponents = headerCount + skipDays + dayButtons;
    try {
      // Invoke the private refresh() method via reflection.
      java.lang.reflect.Method m = MonthView.class.
              getDeclaredMethod("refresh");
      m.setAccessible(true);
      m.invoke(monthView);
    } catch (Exception e) {
      fail("Failed to invoke refresh(): " + e.getMessage());
    }
    assertEquals("Component count should equal headers + " +
                    "skipped labels + day buttons", expectedComponents,
            monthView.getComponentCount());
  }

  @Test
  public void testDayButtonAction() {
    // Find the day button with text "15". Our fake controller
    // reports events on day 15.
    boolean found = false;
    for (Component comp : monthView.getComponents()) {
      if (comp instanceof JButton) {
        JButton button = (JButton) comp;
        if ("15".equals(button.getText())) {
          button.doClick();
          found = true;
          break;
        }
      }
    }
    assertTrue("A day button for '15' should be present", found);
    LocalDate expectedDate = YearMonth.now().atDay(15);
    assertEquals("handleDayClicked should be called with " +
            "day 15", expectedDate, fakeController.lastClickedDate);
    assertTrue("Visual feedback should be triggered on " +
            "button click", fakeController.visualFeedbackCalled);
  }
}