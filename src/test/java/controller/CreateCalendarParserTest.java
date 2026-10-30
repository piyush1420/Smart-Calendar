package controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import model.CalendarManager;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for CreateCalendarParser functionality.
 */
public class CreateCalendarParserTest {
  private CreateCalendarParser createCalendarParser;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Before
  public void setUp() throws InvalidCommandException {
    MultiCalendarEventStorage eventStorage = new MultiCalendarEventStorage();
    CalendarManager calendarManager = new CalendarManager(eventStorage);
    createCalendarParser = new CreateCalendarParser(calendarManager);
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void restoreStreams() throws InvalidCommandException {
    System.setOut(originalOut);
  }

  @Test
  public void shouldCreateCalendarWithValidNameAndTimezone() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work --timezone America/New_York"));
    String output = outContent.toString();
    assertTrue(output.contains("Calendar 'Work' created successfully"));
    assertTrue(output.contains("America/New_York"));
  }

  @Test
  public void shouldRejectDuplicateCalendarName() throws InvalidCommandException {
    createCalendarParser.executeCommand(
            "create calendar --name Work --timezone America/New_York");
    outContent.reset();

    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work --timezone Europe/Paris"));
    String output = outContent.toString();
    assertTrue(output.contains("Error: A calendar with the name 'Work' already exists"));
  }

  @Test
  public void shouldRejectInvalidTimezoneFormat() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Personal --timezone Invalid/Timezone"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid timezone"));
  }

  @Test
  public void shouldRejectMissingNameFlag() throws InvalidCommandException {
    assertFalse(createCalendarParser.executeCommand(
            "create calendar --timezone America/New_York"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void shouldRejectMissingTimezoneFlag() throws InvalidCommandException {
    assertFalse(createCalendarParser.executeCommand(
            "create calendar --name Work"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void shouldRejectEmptyCalendarName() throws InvalidCommandException {
    assertFalse(createCalendarParser.executeCommand(
            "create calendar --name  --timezone America/New_York"));
    String output = outContent.toString();
    assertTrue(output.contains("Error: Invalid command format"));
  }

  @Test
  public void shouldRejectEmptyTimezone() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work --timezone \"\""));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid timezone"));
  }

  @Test
  public void shouldRejectMalformedTimezone() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work --timezone New_York"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid timezone"));
  }

  @Test
  public void shouldRejectUnsupportedTimezone() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work --timezone Moon/Base"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid timezone"));
  }

  @Test
  public void shouldRejectCaseInsensitiveTimezone() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work --timezone america/new_york"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid timezone"));
  }

  @Test
  public void shouldAcceptCalendarNameWithSpaces() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work Calendar --timezone America/New_York"));
    String output = outContent.toString();
    assertTrue(output.contains("Calendar 'Work Calendar' created successfully"));
  }

  @Test
  public void shouldAcceptCalendarNameWithSpecialChars() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work-Calendar! --timezone America/New_York"));
    String output = outContent.toString();
    assertTrue(output.contains("Calendar 'Work-Calendar!' created successfully"));
  }

  @Test
  public void shouldCreateMultipleValidCalendars() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Work --timezone America/New_York"));
    outContent.reset();

    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name Personal --timezone Europe/Paris"));
    String output = outContent.toString();
    assertTrue(output.contains("Calendar 'Personal' created successfully"));
    assertTrue(output.contains("Europe/Paris"));
  }

  @Test
  public void shouldHandleCommandWithExtraSpaces() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(
            "create calendar --name  Work  --timezone  America/New_York"));
    String output = outContent.toString();
    assertTrue(output.contains("Calendar 'Work' created successfully"));
  }

  @Test
  public void shouldRejectCommandWithMissingSpaces() throws InvalidCommandException {
    assertFalse(createCalendarParser.executeCommand(
            "create calendar--name Work--timezone America/New_York"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void shouldRejectCommandWithExtraParameters() throws InvalidCommandException {
    assertFalse(createCalendarParser.executeCommand(
            "create calendar --name Work --timezone America/New_York --extra param"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void shouldRejectCommandWithInvalidOrder() throws InvalidCommandException {
    assertFalse(createCalendarParser.executeCommand(
            "create calendar --timezone America/New_York --name Work"));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void shouldRejectNullCommand() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(null));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }

  @Test
  public void shouldRejectEmptyCommand() throws InvalidCommandException {
    assertTrue(createCalendarParser.executeCommand(""));
    String output = outContent.toString();
    assertTrue(output.contains("Invalid command format"));
  }
}