package controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import model.EventStorage;

import static org.junit.Assert.assertTrue;

/**
 * Tests error message outputs for CreateCommandParser.
 */
public class CreateCommandParserOutputTest {

  private final ByteArrayOutputStream outContent =
          new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private CreateCommandParser commandParser;

  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    EventStorage eventStorage = new EventStorage();
    commandParser = new CreateCommandParser(eventStorage);
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void rejectsNonCreateEventCommands() {
    commandParser.executeCommand("add event Meeting");
    assertTrue(outContent.toString().contains(
            "Error: Command must start with 'create event'."));
  }

  @Test
  public void requiresValidEventCreationFormat() {
    commandParser.executeCommand("create event");
    assertTrue(outContent.toString().contains(
            "Error: Invalid event creation format."));
  }

  @Test
  public void enforcesEventNameRequirement() {
    commandParser.executeCommand(
            "create event from 2023-10-01T09:00 to 2023-10-01T10:00");
    assertTrue(outContent.toString().contains(
            "Error: Event name is required."));
  }

  @Test
  public void handlesAutoDeclineWithoutEventName() {
    commandParser.executeCommand(
            "create event --autoDecline from 2023-10-01T09:00 to 2023-10-01T10:00");
    assertTrue(outContent.toString().contains(
            "Error: Event name is required."));
  }

  @Test
  public void validatesEndDateAfterStartDate() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T10:00 to 2023-10-01T10:00");
    assertTrue(outContent.toString().contains(
            "Error: End date must be after start date."));
  }

  @Test
  public void requiresValueForLocation() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 location");
    assertTrue(outContent.toString().contains(
            "Error: Missing value for location."));
  }

  @Test
  public void requiresValueForDescription() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 description");
    assertTrue(outContent.toString().contains(
            "Error: Missing value for description."));
  }

  @Test
  public void requiresValueForType() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 type");
    assertTrue(outContent.toString().contains(
            "Error: Missing value for type."));
  }

  @Test
  public void restrictsTypeToPublicOrPrivate() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 type confidential");
    assertTrue(outContent.toString().contains(
            "Error: Type must be either 'public' or 'private'."));
  }

  @Test
  public void requiresRepeatDaysSpecification() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 repeats for 5 times");
    assertTrue(outContent.toString().contains(
            "Error: Either repeat day is invalid must be [MRUWFST]"));
  }

  @Test
  public void validatesAtLeastOneValidRepeatDay() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 repeats X for 5 times");
    assertTrue(outContent.toString().contains(
            "Error: Either repeat day is invalid must be [MRUWFST]"));
  }

  @Test
  public void detectsInvalidWeekdayCharacters() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 repeats " +
                    "MTWXF for 5 times");
    assertTrue(outContent.toString().contains(
            "Error: Either repeat day is invalid must be [MRUWFST]"));
  }

  @Test
  public void validatesOnDatePresence() {
    commandParser.executeCommand("create event Holiday on");
    assertTrue(outContent.toString().contains(
            "Error: Missing or invalid on date."));
  }

  @Test
  public void validatesDateFormat() {
    commandParser.executeCommand("create event Holiday on 2023/12/25");
    assertTrue(outContent.toString().contains(
            "Error: Missing or invalid on date."));
  }

  @Test
  public void validatesNumberForRepeatTimes() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 repeats MWF" +
                    " for abc times");
    assertTrue(outContent.toString().contains(
            "Error: Invalid number for repeat times."));
  }

  @Test
  public void requiresPositiveRepeatTimes() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 repeats MWF" +
                    " for 0 times");
    assertTrue(outContent.toString().contains(
            "Error: Repeat times must be greater than zero."));
  }

  @Test
  public void validatesFromDatePresence() {
    commandParser.executeCommand("create event Meeting from");
    assertTrue(outContent.toString().contains(
            "Error: Missing or invalid from date."));
  }

  @Test
  public void validatesToDatePresence() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to");
    assertTrue(outContent.toString().contains(
            "Error: Missing or invalid to date."));
  }

  @Test
  public void detectsInvalidDateTimeFormat() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01-09:00 to 2023-10-01T11:00");
    assertTrue(outContent.toString().contains(
            "Error: Missing or invalid from date."));
  }

  @Test
  public void detectsInvalidCommandSyntax() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 random text");
    assertTrue(outContent.toString().contains(
            "Error: Invalid command syntax."));
  }

  @Test
  public void validatesUntilDateForDateTimeEvents() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 repeats MWF until");
    assertTrue(outContent.toString().contains(
            "Error: Invalid or missing 'until' date."));
  }

  @Test
  public void validatesUntilDateForDateEvents() {
    commandParser.executeCommand(
            "create event Meeting on 2023-10-01 repeats MWF until");
    assertTrue(outContent.toString().contains(
            "Error: Missing or invalid until date."));
  }

  @Test
  public void handlesEventConflictWithAutoDecline() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T11:00");
    outContent.reset();
    commandParser.executeCommand(
            "create event --autoDecline Conflict from 2023-10-01T09:30 to 2023-10-01T10:30");
    assertTrue(outContent.toString().contains(
            "Event conflicts with another event and is declined."));
  }

  @Test
  public void reportsMultipleErrorsInCommand() {
    commandParser.executeCommand(
            "create event from invalid to invalid repeats XYZ for -5 times");
    String output = outContent.toString();
    assertTrue(output.contains("Error: Event name is required."));
  }

  @Test
  public void successfullyAddsRecurringEvents() {
    assertTrue(commandParser.executeCommand(
            "create event Weekly Meeting from 2023-10-01T09:00 to 2023-10-01T11:00 repeats MWF" +
                    " for 3 times"));
  }

  @Test
  public void detectsInvalidMonthInDate() {
    commandParser.executeCommand(
            "create event Meeting from 2023-13-01T09:00 to 2023-10-01T11:00");
    assertTrue(outContent.toString().contains(
            "Error: Invalid date format. Use 'yyyy-MM-ddTHH:mm'."));
  }

  @Test
  public void detectsInvalidHourInTime() {
    commandParser.executeCommand(
            "create event Meeting from 2023-10-01T25:00 to 2023-10-01T11:00");
    assertTrue(outContent.toString().contains(
            "Error: Invalid date format. Use 'yyyy-MM-ddTHH:mm'."));
  }
}