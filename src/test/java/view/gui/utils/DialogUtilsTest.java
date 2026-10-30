package view.gui.utils;

import static org.junit.Assert.fail;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Unit tests for utility methods related to calendar dialog interactions,
 * such as formatting and escaping values for command construction.
 */
public class DialogUtilsTest {

  /**
   * Sets the system to headless mode before any GUI-related tests are run.
   * This ensures that tests which involve GUI components are skipped or safely
   * handled when the environment does not support display output (e.g., CI/CD pipelines).
   */
  @BeforeClass
  public static void setUpHeadless() {
    // Skip the test if running in headless mode to avoid
    // GUI pop-ups.
    Assume.assumeFalse("Skipping DialogUtils test in headless mode",
            GraphicsEnvironment.isHeadless());
  }

  @Test
  public void testShowOutputDialog_NoException() {
    try {
      // Call the method; we expect no exceptions since
      // rendering is handled by Swing.
      DialogUtils.showOutputDialog("Test Title", "Test Content");
    } catch (Exception e) {
      fail("DialogUtils.showOutputDialog threw an exception: " +
              e.getMessage());
    }
  }
}