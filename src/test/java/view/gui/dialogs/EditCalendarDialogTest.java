package view.gui.dialogs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import controller.CommandParser;
import java.awt.Component;
import java.awt.HeadlessException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for the EditCalendarDialog class, verifying dialog behavior,
 * field initialization, and command generation logic.
 */
public class EditCalendarDialogTest {
  @BeforeClass
  public static void setUpHeadless() {
    System.setProperty("java.awt.headless", "flase");
  }

  // Dummy CommandParser that records executed commands.
  private class DummyCommandParser
          extends CommandParser {
    private final List<String> commands =
            new ArrayList<>();

    public DummyCommandParser() {
      super(null);
    }

    @Override
    public boolean executeCommand(String command) {
      commands.add(command);
      return true;
    }

    public List<String> getCommands() {
      return commands;
    }
  }

  // Fake Runnable to record callback execution.
  private class FakeRunnable implements Runnable {
    public boolean runCalled = false;

    @Override
    public void run() {
      runCalled = true;
    }
  }

  // Testable subclass of EditCalendarDialog that overrides
  // dispose() and setVisible() so no GUI appears.
  private class TestableEditCalendarDialog
          extends EditCalendarDialog {
    public boolean disposed = false;

    public TestableEditCalendarDialog(CommandParser parser,
                                      List<String> calendarNames, String currentCalendar,
                                      Runnable onCalendarEdited) {
      super(parser, calendarNames, currentCalendar,
              onCalendarEdited);
    }

    @Override
    public void dispose() {
      disposed = true;
      // Do not call super.dispose() to avoid actual GUI
      // cleanup.
    }

    @Override
    public void setVisible(boolean b) {
      // Override to do nothing.
    }

    // Helper to simulate a click on the "Save Changes" button.
    public void simulateSaveButtonClick() {
      try {
        for (Component comp :
                getContentPane().getComponents()) {
          if (comp instanceof JPanel) {
            for (Component child :
                    ((JPanel) comp).getComponents()) {
              if (child instanceof JButton) {
                JButton btn = (JButton) child;
                if ("Save Changes".equals(btn.getText())) {
                  btn.doClick();
                  return;
                }
              }
            }
          }
        }
        fail("Save Changes button not found.");
      } catch (Exception e) {
        fail("Failed to simulate Save Changes button click: "
                + e.getMessage());
      }
    }
  }

  private DummyCommandParser dummyParser;
  private FakeRunnable fakeRunnable;
  private TestableEditCalendarDialog dialog;

  // Helpers to set private fields via reflection.
  private void setTextField(String fieldName, String value,
                            TestableEditCalendarDialog dialog) {
    try {
      Field field = EditCalendarDialog.class
              .getDeclaredField(fieldName);
      field.setAccessible(true);
      ((JTextField) field.get(dialog)).setText(value);
    } catch (Exception e) {
      fail("Failed to set " + fieldName + ": " +
              e.getMessage());
    }
  }

  private void setComboBox(String fieldName, String value,
                           TestableEditCalendarDialog dialog) {
    try {
      Field field = EditCalendarDialog.class
              .getDeclaredField(fieldName);
      field.setAccessible(true);
      ((JComboBox<String>) field.get(dialog))
              .setSelectedItem(value);
    } catch (Exception e) {
      fail("Failed to set " + fieldName + ": " +
              e.getMessage());
    }
  }

  @Before
  public void setUp() {
    try {
      dummyParser = new DummyCommandParser();
      fakeRunnable = new FakeRunnable();
      List<String> calendarNames = new ArrayList<>();
      calendarNames.add("Cal1");
      calendarNames.add("Cal2");
      dialog = new TestableEditCalendarDialog(dummyParser,
              calendarNames, "Cal1", fakeRunnable);
      dialog.setVisible(false);
      setTextField("nameField", "NewCal", dialog);
      setComboBox("timezoneCombo", "UTC", dialog);
    } catch (HeadlessException he) {
      assumeTrue("Headless mode not supported", false);
    }
  }

  @Test
  public void testEditCalendarDialog_UpdateNameOnly() {
    setTextField("nameField", "NewCal", dialog);
    setComboBox("timezoneCombo", "", dialog);
    dialog.simulateSaveButtonClick();
    List<String> commands = dummyParser.getCommands();
    boolean nameCommandFound = commands.stream()
            .anyMatch(cmd -> cmd.contains("edit calendar")
                    && cmd.contains("--property name"));
    assertTrue("Expected command for updating name was not " +
            "executed", nameCommandFound);
    assertTrue("Dialog should be disposed on success",
            dialog.disposed);
    assertTrue("onCalendarEdited callback should be executed",
            fakeRunnable.runCalled);
  }

  @Test
  public void testEditCalendarDialog_UpdateTimezoneOnly() {
    setTextField("nameField", "", dialog);
    setComboBox("timezoneCombo", "America/New_York", dialog);
    dialog.simulateSaveButtonClick();
    List<String> commands = dummyParser.getCommands();
    boolean timezoneCommandFound = commands.stream()
            .anyMatch(cmd -> cmd.contains("edit calendar")
                    && cmd.contains("--property timezone"));
    assertTrue("Expected command for updating timezone was " +
            "not executed", timezoneCommandFound);
    assertTrue("Dialog should be disposed on success",
            dialog.disposed);
    assertTrue("onCalendarEdited callback should be executed",
            fakeRunnable.runCalled);
  }

  @Test
  public void testEditCalendarDialog_UpdateBoth() {
    setTextField("nameField", "NewCal", dialog);
    setComboBox("timezoneCombo", "America/New_York", dialog);
    dialog.simulateSaveButtonClick();
    List<String> commands = dummyParser.getCommands();
    boolean nameCommandFound = commands.stream()
            .anyMatch(cmd -> cmd.contains("edit calendar")
                    && cmd.contains("--property name"));
    boolean timezoneCommandFound = commands.stream()
            .anyMatch(cmd -> cmd.contains("edit calendar")
                    && cmd.contains("--property timezone"));
    assertTrue("Expected command for updating name was not " +
            "executed", nameCommandFound);
    assertTrue("Expected command for updating timezone was " +
            "not executed", timezoneCommandFound);
    assertTrue("Dialog should be disposed on success",
            dialog.disposed);
    assertTrue("onCalendarEdited callback should be executed",
            fakeRunnable.runCalled);
  }

  @Test
  public void testEditCalendarDialog_EmptyCurrentCalendar() {
    try {
      Field comboField =
              EditCalendarDialog.class.getDeclaredField(
                      "calendarCombo");
      comboField.setAccessible(true);
      @SuppressWarnings("unchecked")
      JComboBox<String> combo =
              (JComboBox<String>) comboField.get(dialog);
      combo.setSelectedItem("");
    } catch (Exception e) {
      fail("Failed to set calendarCombo: "
              + e.getMessage());
    }
    dialog.simulateSaveButtonClick();
    List<String> commands = dummyParser.getCommands();
    assertFalse("No command should be executed when current " +
            "calendar is empty", commands.isEmpty());
  }
}