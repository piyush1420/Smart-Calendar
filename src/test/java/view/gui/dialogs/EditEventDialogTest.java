package view.gui.dialogs;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

import controller.CommandParser;
import java.awt.HeadlessException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import model.CalendarEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** EditEventDialogTest. */
public class EditEventDialogTest {

  @BeforeClass
  public static void setUpHeadless() {
    // Set headless mode before any Swing classes load.
    System.setProperty("java.awt.headless", "false");
  }

  // FakeCommandParser simulates command execution.
  private class FakeCommandParser extends CommandParser {
    private boolean returnValue = true;
    private String lastCommand = "";

    public FakeCommandParser() {
      super(null); // CalendarManager not needed for this test.
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

  // TestableEditEventDialog overrides dispose() so no actual GUI is shown,
  // and exposes the private executeEditCommand() method.
  private class TestableEditEventDialog extends EditEventDialog {
    public boolean disposed = false;

    public TestableEditEventDialog(CommandParser parser,
                                   CalendarEvent event) {
      // Pass a dummy list for multiple events (not used in this test)
      super(parser, event);
    }

    @Override
    public void dispose() {
      disposed = true;
      // Do not call super.dispose() to avoid any GUI cleanup.
    }

    // Public wrapper to invoke the private
    // executeEditCommand() method.
    public void invokeExecuteEditCommand() {
      try {
        Method m = EditEventDialog.class.getDeclaredMethod(
                "executeEditCommand");
        m.setAccessible(true);
        m.invoke(this);
      } catch (Exception e) {
        fail("Failed to invoke executeEditCommand: " +
                e.getMessage());
      }
    }
  }

  private FakeCommandParser fakeParser;
  private TestableEditEventDialog dialog;
  private static final DateTimeFormatter DT_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  // Helper to set the text of a private JTextField.
  private void setTextField(String fieldName, String text) {
    try {
      Field f = EditEventDialog.class.getDeclaredField(fieldName);
      f.setAccessible(true);
      ((javax.swing.JTextField) f.get(dialog)).setText(text);
    } catch (Exception e) {
      fail("Failed to set " + fieldName + ": " + e.getMessage());
    }
  }

  // Helper to set the state of a private JCheckBox.
  private void setCheckBox(String fieldName, boolean selected) {
    try {
      Field f = EditEventDialog.class.getDeclaredField(fieldName);
      f.setAccessible(true);
      ((javax.swing.JCheckBox) f.get(dialog)).setSelected(selected);
    } catch (Exception e) {
      fail("Failed to set " + fieldName + ": " + e.getMessage());
    }
  }

  // Helper to set the selected item of a private JComboBox.
  private void setComboBox(String fieldName, String item) {
    try {
      Field f = EditEventDialog.class.getDeclaredField(fieldName);
      f.setAccessible(true);
      ((javax.swing.JComboBox<String>) f.get(dialog))
              .setSelectedItem(item);
    } catch (Exception e) {
      fail("Failed to set " + fieldName + ": " + e.getMessage());
    }
  }

  @Before
  public void setUp() {
    try {
      fakeParser = new FakeCommandParser();
      // Create a dummy event for testing.
      LocalDateTime now = LocalDateTime.now();
      // For testing, create a dummy event.
      CalendarEvent testEvent = new CalendarEvent("Meeting", now,
              now.plusHours(1), "", "", "");
      dialog = new TestableEditEventDialog(fakeParser, testEvent);
    } catch (HeadlessException he) {
      // Skip tests if headless mode is not supported.
      assumeNoException(he);
    }
  }

  @Test
  public void testExecuteEditCommand_Success() {
    // Set up fields to simulate editing (e.g., change subject).
    setComboBox("propertyComboBox", "subject");
    setTextField("newValueField", "NewMeeting");
    setCheckBox("editAllEventsCheckbox", false);

    // Simulate a successful command.
    fakeParser.setReturnValue(true);
    dialog.disposed = false;

    dialog.invokeExecuteEditCommand();

    // Verify that the dialog is disposed on success.
    assertTrue("Dialog should be disposed on success",
            dialog.disposed);
  }

  @Test
  public void testExecuteEditCommand_Failure() {
    // Set up fields to simulate editing.
    setComboBox("propertyComboBox", "subject");
    setTextField("newValueField", "NewMeeting");
    setCheckBox("editAllEventsCheckbox", false);

    // Simulate failure.
    fakeParser.setReturnValue(false);
    dialog.disposed = false;

    dialog.invokeExecuteEditCommand();

    // Verify that the dialog is not disposed on failure.
    assertTrue("Dialog should not be disposed on failure",
            !dialog.disposed);
  }
}