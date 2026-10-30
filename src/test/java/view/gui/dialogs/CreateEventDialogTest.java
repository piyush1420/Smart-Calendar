package view.gui.dialogs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import controller.CommandParser;
import java.awt.HeadlessException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the CreateEventDialog component.
 * Verifies event creation functionality and command execution through the parser.
 * Uses a testable subclass to override GUI display methods for non-visual testing.
 * Tests both successful and failed command execution scenarios.
 */
public class CreateEventDialogTest {

  @BeforeClass
  public static void setUpHeadless() {
    // Run tests in headless mode to avoid actual GUI display.
    System.setProperty("java.awt.headless", "false");
  }

  // Dummy CommandParser that returns a preset value and records
  // the last command.
  private class DummyCommandParser extends CommandParser {
    private boolean returnValue = true;
    private String lastCommand = "";

    public DummyCommandParser() {
      super(null); // No CalendarManager needed.
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

  // Testable subclass of CreateEventDialog that overrides dispose()
  // and setVisible() to avoid GUI pop-ups.
  private class TestableCreateEventDialog
          extends CreateEventDialog {
    public boolean disposedFlag = false;

    public TestableCreateEventDialog(CommandParser parser) {
      super(parser);
    }

    @Override
    public void dispose() {
      disposedFlag = true;
      // Do not call super.dispose() to prevent actual GUI cleanup.
    }

    @Override
    public void setVisible(boolean b) {
      // Override to do nothing.
    }

    // Expose the private createEvent() method via reflection.
    public void invokeCreateEvent() {
      try {
        Method m = CreateEventDialog.class
                .getDeclaredMethod("createEvent");
        m.setAccessible(true);
        m.invoke(this);
      } catch (Exception e) {
        fail("Failed to invoke createEvent: " +
                e.getMessage());
      }
    }
  }

  // Helper method to set a private JTextField's text.
  private void setTextField(String fieldName, String value,
                            TestableCreateEventDialog dialog) {
    try {
      Field field = CreateEventDialog.class
              .getDeclaredField(fieldName);
      field.setAccessible(true);
      ((JTextField) field.get(dialog)).setText(value);
    } catch (Exception e) {
      fail("Failed to set " + fieldName + ": " +
              e.getMessage());
    }
  }

  private DummyCommandParser dummyParser;
  private TestableCreateEventDialog dialog;

  @Before
  public void setUp() {
    try {
      dummyParser = new DummyCommandParser();
      dialog = new TestableCreateEventDialog(dummyParser);
      dialog.setVisible(false);
      // Set default valid input values.
      setTextField("eventNameField", "Meeting", dialog);
      String today = LocalDate.now()
              .format(DateTimeFormatter.ISO_DATE);
      setTextField("startDateField", today, dialog);
      setTextField("startTimeField", "14:00", dialog);
      setTextField("endDateField", today, dialog);
      setTextField("endTimeField", "15:00", dialog);
      // Set checkboxes to false (non-all-day, non-recurring)
      Field allDayField = CreateEventDialog.class
              .getDeclaredField("allDayCheckBox");
      allDayField.setAccessible(true);
      ((JCheckBox) allDayField.get(dialog)).setSelected(false);
      Field recurringField = CreateEventDialog.class
              .getDeclaredField("recurringCheckBox");
      recurringField.setAccessible(true);
      ((JCheckBox) recurringField.get(dialog)).setSelected(false);
    } catch (HeadlessException he) {
      assumeTrue("Headless mode not supported", false);
    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }
  }

  @Test
  public void testCreateEvent_Success() {
    dummyParser.setReturnValue(true);
    dialog.disposedFlag = false;
    dialog.invokeCreateEvent();
    // On success, the dialog should be disposed.
    assertTrue("Dialog should be disposed on success",
            dialog.disposedFlag);
    // Check that the command starts with "create event".
    String command = dummyParser.getLastCommand();
    assertNotNull("Command should not be null", command);
    assertTrue("Command should start with 'create event'",
            command.startsWith("create event"));
  }

  @Test
  public void testCreateEvent_Failure() {
    dummyParser.setReturnValue(false);
    dialog.disposedFlag = false;
    dialog.invokeCreateEvent();
    // On failure, the dialog should not be disposed.
    assertTrue("Command should start with 'create event'",
            dummyParser.getLastCommand().startsWith("create event"));
    assertTrue("Command should not be null",
            dummyParser.getLastCommand() != null);
    assertTrue("Command should start with 'create event'",
            dummyParser.getLastCommand().startsWith("create event"));
    // The dialog is expected to remain visible.
    assertTrue("Dialog should not be disposed on failure",
            !dialog.disposedFlag);
  }
}