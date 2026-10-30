package view.gui.dialogs;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import controller.CommandParser;

import java.lang.reflect.Method;

import javax.swing.JFrame;

/** ViewEventsDialogTest. */
public class ViewEventsDialogTest {

  @BeforeClass
  public static void setUpHeadless() {
    // Set headless mode before any Swing classes load.
    System.setProperty("java.awt.headless", "false");
  }

  // FakeCommandParser simulates command execution and allows us
  // to simulate output.
  private class FakeCommandParser extends CommandParser {
    private boolean returnValue = true;
    private String simulatedOutput = "";

    public FakeCommandParser() {
      super(null);
    }

    @Override
    public boolean executeCommand(String command) {
      // Instead of printing to System.out, we simulate output here.
      System.out.print(simulatedOutput);
      return returnValue;
    }

    public void setReturnValue(boolean value) {
      returnValue = value;
    }

    public void setSimulatedOutput(String output) {
      simulatedOutput = output;
    }
  }

  // TestableViewEventsDialog exposes the private method via
  // reflection.
  private class TestableViewEventsDialog extends ViewEventsDialog {
    public TestableViewEventsDialog(JFrame parent,
                                    CommandParser parser) {
      super(parent, parser);
    }

    // Expose the private method using reflection.
    public String invokeExecuteCommandWithConsoleCapture(String command) {
      try {
        Method m = ViewEventsDialog.class.getDeclaredMethod(
                "executeCommandWithConsoleCapture", String.class);
        m.setAccessible(true);
        return (String) m.invoke(this, command);
      } catch (Exception e) {
        fail("Failed to invoke executeCommandWithConsoleCapture: " +
                e.getMessage());
        return null;
      }
    }
  }

  private FakeCommandParser fakeParser;
  private TestableViewEventsDialog testDialog;

  @Before
  public void setUp() {
    fakeParser = new FakeCommandParser();
    // Create a dummy JFrame as parent.
    JFrame dummyParent = new JFrame();
    testDialog = new TestableViewEventsDialog(dummyParent, fakeParser);
  }

  @Test
  public void testExecuteCommandWithConsoleCapture_SuccessEmptyOutput() {
    fakeParser.setReturnValue(true);
    fakeParser.setSimulatedOutput("");
    String command = "print events on 2023-04-06";
    String result = testDialog.invokeExecuteCommandWithConsoleCapture(
            command);
    assertEquals("No events found.", result);
  }

  @Test
  public void testExecuteCommandWithConsoleCapture_SuccessWithOutput() {
    fakeParser.setReturnValue(true);
    fakeParser.setSimulatedOutput("Events: Meeting, Lunch");
    String command = "print events on 2023-04-06";
    String result = testDialog.invokeExecuteCommandWithConsoleCapture(
            command);
    assertEquals("Events: Meeting, Lunch", result);
  }

  @Test
  public void testExecuteCommandWithConsoleCapture_FailureWithOutput() {
    fakeParser.setReturnValue(false);
    fakeParser.setSimulatedOutput("Error: Unable to retrieve events");
    String command = "print events on 2023-04-06";
    try {
      String result = testDialog.invokeExecuteCommandWithConsoleCapture(
              command);
      // When failure and output exists, the method returns that output.
      assertEquals("Error: Unable to retrieve events", result);
    } catch (Exception e) {
      fail("Did not expect an exception: " + e.getMessage());
    }
  }
}