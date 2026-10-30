package controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import model.CalendarManager;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for UseCalendarParser functionality.
 * Verifies the correctness of parsing operations.
 * Utilizes ByteArrayOutputStream to capture console output.
 */
public class UseCalendarParserTest {
  private UseCalendarParser useCalendarParser;
  private CreateCalendarParser createCalendarParser;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Before
  public void setUp() throws InvalidCommandException {
    MultiCalendarEventStorage eventStorage = new MultiCalendarEventStorage();
    CalendarManager calendarManager = new CalendarManager(eventStorage);
    useCalendarParser = new UseCalendarParser(calendarManager);
    createCalendarParser = new CreateCalendarParser(calendarManager);

    // Redirect System.out to capture output
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void testValidInput() throws InvalidCommandException {
    createCalendarParser.executeCommand("create calendar --name TestCalendar --timezone" +
            " America/New_York");
    assertTrue(useCalendarParser.executeCommand("use calendar --name TestCalendar"));
    String output = outContent.toString();
    assertTrue(output.contains("Switched to calendar 'TestCalendar'"));
  }

  @Test
  public void testNonexistentCalendar() {
    try {
      useCalendarParser.executeCommand("use calendar --name NonexistentCalendar");
      fail("Expected IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException ex) {
      assertEquals("Calendar 'NonexistentCalendar' does not exist.", ex.getMessage());
    }
  }

  @Test
  public void testMissingNameFlag() {
    assertFalse(useCalendarParser.executeCommand("use calendar"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
    assertTrue(output.contains("Expected: use calendar --name <calName>"));
  }

  @Test
  public void testEmptyCalendarName() {
    assertFalse(useCalendarParser.executeCommand("use calendar --name"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void testEmptyCalendarNameWithQuotes() {
    try {
      useCalendarParser.executeCommand("use calendar --name \"\"");
      fail("Expected IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException ex) {
      assertEquals("Calendar '\"\"' does not exist.", ex.getMessage());
    }
  }

  @Test
  public void testCalendarNameWithSpaces() throws InvalidCommandException {
    // Create a calendar with spaces in the name
    createCalendarParser.executeCommand("create calendar --name \"Work Calendar\" --timezone" +
            " America/New_York");
    outContent.reset();
    assertTrue(useCalendarParser.executeCommand("use calendar --name \"Work Calendar\""));
    String output = outContent.toString();
    assertTrue(output.contains("Switched to calendar '\"Work Calendar\"'."));
  }

  @Test
  public void testCalendarNameWithSpecialCharacters() throws InvalidCommandException {
    try {
      createCalendarParser.executeCommand("create calendar --name Work-Calendar! --timezone" +
              " America/New_York");
      assertTrue(useCalendarParser.executeCommand("use calendar --name Work-Calendar!"));
    } catch (IllegalArgumentException ex) {
      System.err.println("Exception caught: ");
    }
  }

  @Test
  public void testCommandWithExtraSpaces() throws InvalidCommandException {
    try {
      createCalendarParser.executeCommand("");
      useCalendarParser.executeCommand("use   calendar   --name   TestCalendar");
      fail("Expected IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException ex) {
      assertEquals("Calendar 'TestCalendar' does not exist.", ex.getMessage());
    }
  }

  @Test
  public void testCommandWithMissingSpaces() {
    assertFalse(useCalendarParser.executeCommand("usecalendar--name TestCalendar"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void testCommandWithExtraParameters() {
    try {
      useCalendarParser.executeCommand("use calendar --name Test Calendar --extra param");
      fail("Expected IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException ex) {
      assertEquals("Calendar 'Test Calendar --extra param' does not exist.",
              ex.getMessage());
    }
  }

  @Test
  public void testCaseSensitivity() throws InvalidCommandException {
    // Create calendars with same name but different case
    createCalendarParser.executeCommand("create calendar --name WorkCalendar --timezone " +
            "America/New_York");
    createCalendarParser.executeCommand("create calendar --name workcalendar --timezone " +
            "Europe/Paris");
    outContent.reset();

    // Test first calendar
    assertTrue(useCalendarParser.executeCommand("use calendar --name WorkCalendar"));
    String output1 = outContent.toString();
    assertTrue(output1.contains("Switched to calendar 'WorkCalendar'"));
    outContent.reset();

    // Test second calendar
    assertTrue(useCalendarParser.executeCommand("use calendar --name workcalendar"));
    String output2 = outContent.toString();
    assertTrue(output2.contains("Switched to calendar 'workcalendar'"));
  }

  @Test
  public void testSwitchingBetweenCalendars() throws InvalidCommandException {
    // Create a second calendar
    createCalendarParser.executeCommand("create calendar --name Personal --timezone " +
            "Europe/London");
    outContent.reset();

    // Switch to first calendar
    createCalendarParser.executeCommand("create calendar --name TestCalendar --timezone " +
            "America/New_York");
    assertTrue(useCalendarParser.executeCommand("use calendar --name TestCalendar"));
    String output1 = outContent.toString();
    assertTrue(output1.contains("Switched to calendar 'TestCalendar'"));
    outContent.reset();

    // Switch to second calendar
    assertTrue(useCalendarParser.executeCommand("use calendar --name Personal"));
    String output2 = outContent.toString();
    assertTrue(output2.contains("Switched to calendar 'Personal'"));
  }

  @Test(expected = NullPointerException.class)
  public void testNullCommand() {
    assertTrue(useCalendarParser.executeCommand(null));
  }

  @Test
  public void testEmptyCommand() {
    assertFalse(useCalendarParser.executeCommand(""));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void testUseCalendarWithNonexistentCalendarWithSpecialChars() {
    try {
      useCalendarParser.executeCommand("use calendar --name Special@#$Calendar");
      fail("Expected IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException ex) {
      assertEquals("Calendar 'Special@#$Calendar' does not exist.", ex.getMessage());
    }
  }

  @Test
  public void testUseCalendarWithIncorrectCommand() {
    assertFalse(useCalendarParser.executeCommand("user calendar --name TestCalendar"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void testUseCalendarWithoutFullCommand() {
    assertFalse(useCalendarParser.executeCommand("use"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }
}