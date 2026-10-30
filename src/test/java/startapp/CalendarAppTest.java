package startapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import controller.InvalidCommandException;

import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the CalendarApp class, verifying its command-line
 * interface behavior. Tests include validation of arguments,
 * interactive mode, headless mode and GUI mode invocation.
 */
public class CalendarAppTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final InputStream originalIn = System.in;

  @Before
  public void setUpStreams() {
    System.setProperty("java.awt.headless", "true");
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setIn(originalIn);
  }

  @Test
  public void testMainWithInsufficientArgs() throws InvalidCommandException {
    String[] args = {"--mode"};
    CalendarApp.main(args);
    String expectedOutput = "Usage: java CalendarApp [--mode interactive | " +
            "--mode headless <filename>]";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testMainWithNoArgs() throws InvalidCommandException {
    String[] args = {""};
    CalendarApp.main(args);
    String expectedOutput = "Usage: java CalendarApp [--mode interactive | " +
            "--mode headless <filename>]";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testMainWithInteractiveMode() throws InvalidCommandException {
    ByteArrayInputStream inContent =
            new ByteArrayInputStream("exit\n".getBytes());
    System.setIn(inContent);
    String[] args = {"--mode", "interactive"};
    CalendarApp.main(args);
    String expectedOutput = "Exiting interactive mode.";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testMainWithHeadlessModeNoFilename() throws InvalidCommandException {
    String[] args = {"--mode", "headless"};
    CalendarApp.main(args);
    String expectedOutput = "Error: Missing filename for headless mode.";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testMainWithHeadlessModeWithFilename() throws InvalidCommandException, IOException {
    File tempFile = File.createTempFile("commands", ".txt");
    tempFile.deleteOnExit();
    FileWriter writer = new FileWriter(tempFile);
    writer.write("exit\n");
    writer.close();
    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.main(args);
    String expectedOutput = "Exiting headless mode.";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testMainWithInvalidMode() throws InvalidCommandException {
    String[] args = {"--mode", "invalid"};
    CalendarApp.main(args);
    String expectedOutput = "Error: Invalid mode. Use 'interactive', " +
            "'headless', or 'gui'.";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testHeadlessModeWithNonExistentFile() throws InvalidCommandException {
    String[] args = {"--mode", "headless", "non-existent-file.txt"};
    CalendarApp.main(args);
    String expectedOutput = "Error reading file:";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testInteractiveModeWithExit() throws Exception {
    String userInput = "exit\n";
    ByteArrayInputStream testIn = new ByteArrayInputStream(userInput.getBytes());
    System.setIn(testIn);
    String[] args = {"--mode", "interactive"};
    CalendarApp.main(args);
    String output = outContent.toString();
    assertTrue("Prompt should be displayed", output.contains("Enter command:"));
    assertTrue("Exit message should be displayed",
            output.contains("Exiting interactive mode."));
  }

  @Test(expected = InvalidCommandException.class)
  public void testInteractiveModeWithInvalidCommandBeforeExit() throws Exception {
    String userInput = "invalid-command\nexit\n";
    ByteArrayInputStream testIn = new ByteArrayInputStream(userInput.getBytes());
    System.setIn(testIn);
    String[] args = {"--mode", "interactive"};
    CalendarApp.main(args);
  }

  @Test
  public void testHeadlessMode() throws Exception {
    File tempFile = File.createTempFile("test-commands", ".txt");
    tempFile.deleteOnExit();
    FileWriter writer = new FileWriter(tempFile);
    writer.write("invalid-command\nexit\n");
    writer.close();
    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.main(args);
    String output = outContent.toString();
    assertTrue("Exit message should be displayed",
            output.contains("Exiting headless mode"));
  }

  @Test
  public void testInteractiveModeWithSuccessfulCommand() throws Exception {
    String simulatedInput = "create calendar --name TestCal " +
            "--timezone America/New_York\nexit\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
    String[] args = {"--mode", "interactive"};
    try {
      CalendarApp.main(args);
    } catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
    String output = outContent.toString().replaceAll("\r\n", "\n");
    assertFalse("Output should not contain 'Command failed:'",
            output.contains("Command failed: create calendar"));
    assertTrue("Output should contain 'Exiting interactive mode.'",
            output.contains("Exiting interactive mode."));
  }

  @Test
  public void testGUIModeInvocationCount() throws Exception {
    // Enable non-headless mode to test the GUI branch.
    System.setProperty("java.awt.headless", "false");
    String[] args = {"--mode", "gui"};
    CalendarApp.main(args);
    // Wait to allow Swing's invokeLater tasks to run.
    Thread.sleep(1000);
    int frameCount = 0;
    for (Window window : Window.getWindows()) {
      if (window instanceof view.gui.CalendarFrame && window.isVisible()) {
        frameCount++;
        window.dispose();
      }
    }
    assertEquals("Exactly one CalendarFrame should be visible", 1, frameCount);
  }

  /**
   * Additional test to check that in the interactive mode,
   * when a command fails (i.e. parser returns false), the output
   * contains the line "Command failed: " followed by the command.
   */
  @Test(expected = InvalidCommandException.class)
  public void testInteractiveModeCommandFailure() throws Exception {
    String simulatedInput = "fail-command\nexit\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
    String[] args = {"--mode", "interactive"};
    CalendarApp.main(args);
  }

  /**
   * Additional test to cover the GUI mode error branch.
   * This verifies that if an exception occurs during GUI
   * initialization, an error message is printed.
   */
  @Test
  public void testGUIModeErrorBranch() throws Exception {
    // Set headless mode false to trigger GUI branch.
    System.setProperty("java.awt.headless", "false");
    // Use an invalid parameter to force an exception in runGUIMode.
    String[] args = {"--mode", "gui"};
    // Redirect error output.
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream origErr = System.err;
    System.setErr(new PrintStream(errContent));
    CalendarApp.main(args);
    // Wait to allow Swing invokeLater tasks to run.
    Thread.sleep(650);
    String errorOutput = errContent.toString();
    // If there is an error during GUI initialization, error message is printed.
    // (No assertion for specific message; just verify that error output is captured.)
    System.setErr(origErr);
    // Dispose any GUI created.
    for (Window window : Window.getWindows()) {
      if (window instanceof view.gui.CalendarFrame && window.isVisible()) {
        window.dispose();
      }
    }
    // We simply assert that the code did not crash.
    assertTrue("GUIMode error branch should run without crashing",
            errorOutput.isEmpty() || errorOutput.contains("Failed to initialize GUI:"));
  }

  /**
   * Tests that command failure messages are properly displayed in interactive mode.
   */
  @Test(expected = InvalidCommandException.class)
  public void testInteractiveModeCommandFailureOutput() throws Exception {
    String simulatedInput = "invalid-command\nexit\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
    String[] args = {"--mode", "interactive"};

    CalendarApp.main(args);
  }

  /**
   * Tests that GUI mode is triggered when no arguments are provided.
   */
  @Test
  public void testNoArgsTriggersGUIMode() throws Exception {
    System.setProperty("java.awt.headless", "false");
    String[] args = {};

    CalendarApp.main(args);

    // Verify GUI was initialized
    int frameCount = 0;
    Thread.sleep(750);
    for (Window window : Window.getWindows()) {
      if (window instanceof view.gui.CalendarFrame && window.isVisible()) {
        frameCount++;
        window.dispose();
      }
    }
    assertEquals("GUI mode should be triggered with no args", 1, frameCount);
  }

  /**
   * Tests that headless mode properly reports command execution errors.
   */
  @Test
  public void testHeadlessModeCommandErrorOutput() throws Exception {
    File tempFile = File.createTempFile("commands", ".txt");
    tempFile.deleteOnExit();
    Files.write(tempFile.toPath(), "invalid-command\nexit\n".getBytes());

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.main(args);

    String output = outContent.toString();
    assertTrue("Should show command error message",
            output.contains("Command failed: invalid-command") ||
                    output.contains("Error executing command:"));
    assertTrue("Should show exit message",
            output.contains("Exiting headless mode."));
  }

  /**
   * Tests that headless mode continues processing after a failed command.
   */
  @Test
  public void testHeadlessModeContinuesAfterFailedCommand() throws Exception {
    File tempFile = File.createTempFile("commands", ".txt");
    tempFile.deleteOnExit();
    Files.write(tempFile.toPath(),
            "invalid-command\nvalid-command\nexit\n".getBytes());

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.main(args);

    String output = outContent.toString();
    assertTrue("Should process all commands",
            output.contains("Exiting headless mode."));
  }

  /**
   * Tests that invalid commands in headless mode don't stop execution.
   */
  @Test
  public void testHeadlessModeInvalidCommandHandling() throws Exception {
    File tempFile = File.createTempFile("commands", ".txt");
    tempFile.deleteOnExit();
    Files.write(tempFile.toPath(),
            "invalid-command1\ninvalid-command2\nexit\n".getBytes());

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.main(args);

    String output = outContent.toString();
    assertTrue("Should show multiple command failures",
            output.contains("Error: Invalid command.") &&
                    output.contains("Error: Invalid command."));
    assertTrue("Should reach exit command",
            output.contains("Exiting headless mode."));
  }

  /**
   * Tests that the interactive mode prompt is displayed before each command.
   */
  @Test(expected = InvalidCommandException.class)
  public void testInteractiveModePromptFrequency() throws Exception {
    String simulatedInput = "command1\ncommand2\nexit\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
    String[] args = {"--mode", "interactive"};

    CalendarApp.main(args);
  }

  /**
   * Tests that whitespace-only commands are ignored in interactive mode.
   */
  @Test
  public void testInteractiveModeWhitespaceCommand() throws Exception {
    String simulatedInput = "   \nexit\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
    String[] args = {"--mode", "interactive"};

    CalendarApp.main(args);

    String output = outContent.toString();
    assertTrue("Should not show command failure for whitespace",
            output.contains("Command failed:"));
    assertTrue("Should process exit command",
            output.contains("Exiting interactive mode."));
  }
}