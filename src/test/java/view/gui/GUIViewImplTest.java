package view.gui;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertTrue;

import controller.CommandParser;

import controller.gui.GUIControllerBridge;

import java.time.LocalDate;

import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;

import javax.swing.JPopupMenu;

import javax.swing.JMenuItem;

import org.junit.Before;

import org.junit.BeforeClass;

import org.junit.Test;

import model.CalendarEvent;

import model.CalendarManager;

import model.MultiCalendarEventStorage;

/**

 * Tests for the GUIViewImpl class which implements the IGUIView interface.

 * Verifies event detection, context menu creation, and interaction with the controller.

 * Uses fake implementations of CommandParser and GUIControllerBridge for isolated testing.

 * Test runs in headless mode to avoid GUI popups during test execution.

 */

public class GUIViewImplTest {

  /**

   * Sets up the environment for all tests to run in headless mode.

   * This prevents GUI components from trying to display on screen during testing,

   * allowing tests to run in environments without display capabilities.

   */

  @BeforeClass

  public static void setUpHeadless() {

    System.setProperty("java.awt.headless", "true");

  }

  // FakeCommandParser that always returns true.

  private class FakeCommandParser

          extends CommandParser {

    public FakeCommandParser() {

      super(null);

    }

    @Override

    public boolean executeCommand(String command) {

      return true;

    }

  }

  // FakeGUIControllerBridge returns preset values for event queries.

  private class FakeGUIControllerBridge

          extends GUIControllerBridge {

    private List<CalendarEvent> eventsForDay =

            new ArrayList<>();

    private String outputForDay = "Fake output";

    public FakeGUIControllerBridge(CommandParser parser) {

      super(parser, new CalendarManager(

              new MultiCalendarEventStorage()));

    }

    @Override

    public List<CalendarEvent> getEventsForDay(LocalDate date) {

      return eventsForDay;

    }

    @Override

    public String getEventsOutputForDay(LocalDate date) {

      return outputForDay;

    }

    public void setEventsForDay(List<CalendarEvent> events) {

      eventsForDay = events;

    }

    public void setOutputForDay(String output) {

      outputForDay = output;

    }

  }

  private FakeCommandParser fakeParser;

  private FakeGUIControllerBridge fakeBridge;

  // We reference the controller via its interface.

  private IGUIView controller;

  @Before

  public void setUp() {

    fakeParser = new FakeCommandParser();

    fakeBridge = new FakeGUIControllerBridge(fakeParser);

    controller = new GUIViewImpl(

            fakeBridge, fakeParser);

  }

  @Test

  public void testGetParser() {

    assertEquals("getParser() should return the fake parser",

            fakeParser, controller.getParser());

  }

  @Test

  public void testHasEventsOn_NoEvents() {

    fakeBridge.setEventsForDay(new ArrayList<>());

    assertFalse("hasEventsOn() should return false when there " +

            "are no events", controller.hasEventsOn(LocalDate.now()));

  }

  @Test

  public void testHasEventsOn_WithEvents() {

    LocalDateTime now = LocalDate.now().atStartOfDay();

    CalendarEvent event = new CalendarEvent("Test Event", now,

            now.plusHours(1), "", "", "");

    fakeBridge.setEventsForDay(new ArrayList<CalendarEvent>() {{

        add(event);

      }

    });

    assertTrue("hasEventsOn() should return true when events " +

            "exist", controller.hasEventsOn(LocalDate.now()));

  }

  @Test

  public void testCreateContextMenuFor() {

    JPopupMenu menu = controller.createContextMenuFor(

            LocalDate.now());

    assertNotNull("createContextMenuFor() should not return " +

            "null", menu);

    boolean foundCreate = false;

    boolean foundEdit = false;

    for (int i = 0; i < menu.getComponentCount(); i++) {

      if (menu.getComponent(i) instanceof JMenuItem) {

        String text = ((JMenuItem)

                menu.getComponent(i)).getText();

        if ("Create Event".equals(text)) {

          foundCreate = true;

        }

        if ("Edit Events".equals(text)) {

          foundEdit = true;

        }

      }

    }

    assertTrue("Menu should contain 'Create Event'",

            foundCreate);

    assertTrue("Menu should contain 'Edit Events'",

            foundEdit);

  }

}
