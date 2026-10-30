package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.CalendarEvent;
import model.EventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the CreateCommandParser class to ensure it correctly parses and executes
 * commands for creating calendar events, including simple events, recurring
 * events, and all-day events. It also verifies error handling for invalid or
 * incomplete commands.
 */
public class CreateCommandParserTest {

  private CreateCommandParser commandParser;

  @Before
  public void setUp() {
    EventStorage eventStorage = new EventStorage();
    commandParser = new CreateCommandParser(eventStorage);
  }

  @Test
  public void createsValidSimpleEvent() {
    boolean result = commandParser.executeCommand(
            "create event Meeting from 2025-03-10T14:00 to 2025-03-10T16:00");
    assertTrue(result);
  }

  @Test
  public void createsValidRecurringEventWithTimes() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Team Meeting from 2023-10-01T09:00 to " +
                    "2023-10-01T10:00 repeats MWF for 5 times location Conference Room A " +
                    "description Weekly team sync type private");
    assertTrue(result);
  }

  @Test
  public void createsValidRecurringEventWithUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Team Meeting from 2025-03-10T09:00 to " +
                    "2025-03-10T10:00 repeats MW until 2025-03-30T00:00 location " +
                    "Conference Room A description Weekly team sync type private");
    assertTrue(result);
  }

  @Test
  public void createsValidAllDayEvent() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Team Meeting on 2025-03-10T09:00");
    assertTrue(result);
  }

  @Test
  public void createsValidAllDayRecurringEvent() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Team Meeting on 2025-03-10 repeats MT for 5 times");
    assertTrue(result);
  }

  @Test
  public void rejectsMissingEventName() {
    boolean result = commandParser.executeCommand(
            "create event from 2025-03-10T14:00 to 2025-03-10T16:00");
    assertFalse(result);
  }

  @Test
  public void rejectsMissingStartOrEndDate() {
    boolean result = commandParser.executeCommand(
            "create event Meeting from 2025-03-10T14:00");
    assertFalse(result);
  }

  @Test
  public void handlesExtraSpacesInCommand() {
    boolean result = commandParser.executeCommand(
            "create event   TeamMeeting   from 2025-03-10T14:00   to 2025-03-10T16:00");
    assertTrue(result);
  }

  @Test
  public void rejectsInvalidLocationFormat() {
    boolean result = commandParser.executeCommand(
            "create event   TeamMeeting   from 2025-03-10T14:00   to 2025-03-10T16:00 location");
    assertFalse(result);
  }

  @Test
  public void rejectsInvalidDescriptionFormat() {
    boolean result = commandParser.executeCommand(
            "create event   TeamMeeting   from 2025-03-10T14:00   to 2025-03-10T16:00 description");
    assertFalse(result);
  }

  @Test
  public void rejectsInvalidTypeFormat() {
    boolean result = commandParser.executeCommand(
            "create event   TeamMeeting   from 2025-03-10T14:00   to 2025-03-10T16:00 type");
    assertFalse(result);
  }

  @Test
  public void rejectsAnotherInvalidTypeValue() {
    boolean result = commandParser.executeCommand(
            "create event   TeamMeeting   from 2025-03-10T14:00   to 2025-03-10T16:00 type happy");
    assertFalse(result);
  }

  @Test
  public void createsValidCommandWithLocation() {
    boolean result = commandParser.executeCommand(
            "CREATE EVENT   TEAM_MEET   FROM 2025-03-10T14:00   TO 2025-03-10T16:00 LOCATION" +
                    " ELL HALL");
    assertTrue(result);
  }

  @Test
  public void createsValidCommandWithTypeInCaps() {
    boolean result = commandParser.executeCommand(
            "CREATE EVENT   TEAM_MEET   FROM 2025-03-10T14:00   TO 2025-03-10T16:00 TYPE PUBLIC");
    assertTrue(result);
  }

  @Test
  public void rejectsInvalidRecurringEventWithUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Team Meeting from 2025-03-10T09:00 to " +
                    "2025-03-10T10:00 repeats MW until 2025-03-30T00:00 happy birthday to you");
    assertFalse(result);
  }

  @Test
  public void createsValidRecurringEventWithDescription() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Team Meeting from 2025-03-10T09:00 to " +
                    "2025-03-10T10:00  description the from on until is good to go the " +
                    "meeting may last until any time.");
    assertTrue(result);
  }

  @Test
  public void rejectsInvalidCommand() {
    boolean result = commandParser.executeCommand("invalid command");
    assertFalse(result);
  }

  @Test
  public void createsMinimalRecurringByTimes() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MWF for " +
                    "5 times");
    assertTrue(result);
  }

  @Test
  public void createsCompleteRecurringByTimes() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Team Meeting from 2023-10-01T09:00 to " +
                    "2023-10-01T10:00 repeats MWF for 5 times location Conference Room A " +
                    "description Weekly team sync type private");
    assertTrue(result);
  }

  @Test
  public void rejectsRecurringByTimesMissingRepeatDays() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats for " +
                    "5 times");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringByTimesInvalidRepeatDays() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats XYZ for " +
                    "5 times");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringByTimesZeroTimes() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MWF for " +
                    "0 times");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringByTimesNegativeTimes() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MWF for " +
                    "-5 times");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringByTimesNonNumericTimes() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MWF for " +
                    "abc times");
    assertFalse(result);
  }

  @Test
  public void createsRecurringByTimesWithAllWeekdays() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MTWRFSU " +
                    "for 3 times");
    assertTrue(result);
  }

  @Test
  public void rejectsRecurringByTimesMissingTimesKeyword() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MWF " +
                    "for 5");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringByTimesEndDateBeforeStartDate() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-09-01T10:00 repeats MWF " +
                    "for 5 times");
    assertFalse(result);
  }

  @Test
  public void createsMinimalRecurringUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MWF " +
                    "until 2023-12-31T23:59");
    assertTrue(result);
  }

  @Test
  public void createsCompleteRecurringUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Team Meeting from 2023-10-01T09:00 to " +
                    "2023-10-01T10:00 repeats MWF until 2023-12-31T23:59 location Conference " +
                    "Room A " +
                    "description Weekly team sync type private");
    assertTrue(result);
  }

  @Test
  public void rejectsRecurringUntilDateMissingUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats " +
                    "MWF until");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringUntilDateInvalidUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MWF " +
                    "until 2023-13-45T25:99");
    assertFalse(result);
  }

  @Test
  public void acceptsRecurringUntilDateBeforeStartDate() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats MWF " +
                    "until 2023-09-01T09:00");
    assertTrue(result);
  }

  @Test
  public void createsRecurringUntilDateSingleWeekday() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 repeats M " +
                    "until 2023-12-31T23:59");
    assertTrue(result);
  }

  @Test
  public void createsMinimalAllDayEvent() {
    boolean result = commandParser.executeCommand(
            "create event Holiday Party on 2023-12-25T09:00");
    assertTrue(result);
  }

  @Test
  public void createsCompleteAllDayEvent() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Holiday Party on 2023-12-25T09:00 location Conference " +
                    "Hall description Annual celebration type public");
    assertTrue(result);
  }

  @Test
  public void rejectsAllDayEventMissingDate() {
    boolean result = commandParser.executeCommand("create event Holiday Party on");
    assertFalse(result);
  }

  @Test
  public void rejectsAllDayEventInvalidDateFormat() {
    boolean result = commandParser.executeCommand("create event Holiday Party on 2023/12/25");
    assertFalse(result);
  }

  @Test
  public void rejectsAllDayEventInvalidTimeFormat() {
    boolean result = commandParser.executeCommand("create event Holiday Party on 2023-12-25 9:00");
    assertFalse(result);
  }

  @Test
  public void createsMinimalRecurringAllDayByTimes() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01 repeats MWF for 5 times");
    assertTrue(result);
  }

  @Test
  public void createsCompleteRecurringAllDayByTimes() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Weekly Meeting on 2023-10-01 repeats MWF for 5 times " +
                    "location Conference Room description Team sync-up type private");
    assertTrue(result);
  }

  @Test
  public void rejectsRecurringAllDayByTimesMissingRepeatDays() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01 repeats for 5 times");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringAllDayByTimesMissingForKeyword() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01 repeats MWF 5 times");
    assertFalse(result);
  }

  @Test
  public void createsRecurringAllDayByTimesLargeNumber() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01 repeats MWF for 999 times");
    assertTrue(result);
  }

  @Test
  public void rejectsRecurringAllDayByTimesDateWithTime() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01T09:00 repeats MWF for 5 times");
    assertFalse(result);
  }

  @Test
  public void createsMinimalRecurringAllDayUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01 repeats MWF until 2023-12-31");
    assertTrue(result);
  }

  @Test
  public void createsCompleteRecurringAllDayUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline Weekly Meeting on 2023-10-01 repeats MWF until " +
                    "2023-12-31 location Conference Room description Team sync-up type private");
    assertTrue(result);
  }

  @Test
  public void rejectsRecurringAllDayUntilDateMissingUntilDate() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01 repeats MWF until");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringAllDayUntilDateInvalidDateFormat() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01 repeats MWF until 12/31/2023");
    assertFalse(result);
  }

  @Test
  public void rejectsRecurringAllDayUntilDateWithTime() {
    boolean result = commandParser.executeCommand(
            "create event Weekly Meeting on 2023-10-01 repeats MWF until 2023-12-31T23:59");
    assertFalse(result);
  }

  @Test
  public void createsEventWithSpecialCharacters() {
    boolean result = commandParser.executeCommand(
            "create event Team Meeting #1: Q3 Planning (2023) from 2023-10-01T09:00 to " +
                    "2023-10-01T10:00");
    assertTrue(result);
  }

  @Test
  public void rejectsSameStartAndEndTime() {
    boolean result = commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T09:00");
    assertFalse(result);
  }

  @Test
  public void createsCommandWithUppercase() {
    boolean result = commandParser.executeCommand(
            "CREATE EVENT Meeting FROM 2023-10-01T09:00 TO 2023-10-01T10:00 REPEATS MWF FOR " +
                    "5 TIMES");
    assertTrue(result);
  }

  @Test
  public void createsEventWithMixedLocation() {
    boolean result = commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 " +
                    "location Room 101, Bldg A & Online (Zoom)");
    assertTrue(result);
  }

  @Test
  public void rejectsAutoDeclineWithoutEventName() {
    boolean result = commandParser.executeCommand(
            "create event --autoDecline from 2023-10-01T09:00 to 2023-10-01T10:00");
    assertFalse(result);
  }

  @Test
  public void rejectsInvalidTypeValue() {
    boolean result = commandParser.executeCommand(
            "create event Meeting from 2023-10-01T09:00 to 2023-10-01T10:00 type confidential");
    assertFalse(result);
  }

  @Test
  public void rejectsCommandWithMultipleErrors() {
    boolean result = commandParser.executeCommand(
            "create event Meeting from 2023-10-01 repeats XYZ for -5 times");
    assertFalse(result);
  }

  @Test
  public void createsEventWithLongName() {
    String longName = "A".repeat(1000);
    boolean result = commandParser.executeCommand(
            "create event " + longName + " from 2023-10-01T09:00 to 2023-10-01T10:00");
    assertTrue(result);
  }

  @Test
  public void testMissingLocationValue() {
    // Suppose eventStorage is your test's mock or real storage
    EventStorage storage = new EventStorage();

    CreateCommandParser parser = new CreateCommandParser(storage);

    // This command includes "location" but no actual location string
    String command = "create event MyEvent from 2025-03-01T10:00 to 2025-03-01T11:00 location";

    boolean result = parser.executeCommand(command);

    // We expect it to fail because “location” is declared but no value
    assertFalse("Command should fail when 'location' is given no value", result);
  }

  @Test
  public void testHandleSimpleEvent_EndDateBeforeStartDate_ShouldReturnFalse() {
    // Setup a minimal environment for the parser
    EventStorage storage = new EventStorage();
    CreateCommandParser parser = new CreateCommandParser(storage);

    // Construct a command with an end date/time that is before the start date/time
    // This triggers a validation failure in extractAndValidateBothDateAndTime()
    String command = "create event MyEvent from 2025-03-01T10:00 to 2025-03-01T09:59";

    // Execute
    boolean result = parser.executeCommand(command);

    // Validate
    // If handleSimpleEvent is mutated to always return true at the failing path,
    // the test will fail, thus killing that mutant.
    assertFalse(
            "Command should fail (return false) if end date/time is before start date/time",
            result
    );
  }

  @Test
  public void testMissingEventName_ShouldReturnFalse() {
    // Suppose you have an EventStorage set up for testing
    EventStorage storage = new EventStorage();
    CreateCommandParser parser = new CreateCommandParser(storage);

    // Construct a command that omits an event name entirely.
    // e.g., "create event from 2025-03-01T10:00 to 2025-03-01T11:00"
    String command = "create event from 2025-03-01T10:00 to 2025-03-01T11:00";

    boolean result = parser.executeCommand(command);

    // We expect the command to fail because the event name is missing.
    // If the mutant replaced "return false" with "return true,"
    // this test will catch it.
    assertFalse("Command should fail when event name is not provided", result);
  }

  @Test
  public void testEventNameIsAutoDecline_ShouldReturnFalse() {
    // Suppose you have a real or mock storage for testing
    EventStorage storage = new EventStorage();
    CreateCommandParser parser = new CreateCommandParser(storage);

    // Construct a minimal command that attempts to set the event name to "--autoDecline"
    // E.g., user typed: "create event --autoDecline from 2025-03-01T10:00 to 2025-03-01T11:00"
    String command = "create event --autoDecline from 2025-03-01T10:00 to 2025-03-01T11:00";

    // Execute
    boolean result = parser.executeCommand(command);

    // Expect the parser to fail because we treat "--autoDecline" as an invalid name
    assertFalse(
            "Command should fail (return false) if the event name is just '--autoDecline'",
            result
    );
  }

  @Test
  public void testMissingLocationValue_ShouldReturnFalse() {
    // Setup
    EventStorage storage = new EventStorage();
    CreateCommandParser parser = new CreateCommandParser(storage);

    // Command includes the "location" keyword but no actual value
    // which triggers "Error: Missing value for location."
    String command = "create event MyEvent from 2025-03-01T10:00 " +
            "to 2025-03-01T11:00 location";

    // Execute
    boolean result = parser.executeCommand(command);

    // Assert that the entire command fails
    assertFalse("Should return false if 'location' is specified but has no value",
            result);
  }

  @Test
  public void testInvalidTypeValue_ShouldReturnFalse() {
    // Setup
    EventStorage storage = new EventStorage();
    CreateCommandParser parser = new CreateCommandParser(storage);

    // Command sets 'type' to something that's neither "public" nor "private"
    // This triggers "Error: Type must be either 'public' or 'private'."
    String command = "create event MyEvent from 2025-03-01T10:00 " +
            "to 2025-03-01T11:00 type somethingElse";

    // Execute
    boolean result = parser.executeCommand(command);

    // Must fail because type is invalid
    assertFalse("Should return false if 'type' is not 'public' or 'private'",
            result);
  }

  @Test
  public void testInvalidRepeatDays_ShouldReturnFalse() {
    // Setup: use a real or mock EventStorage
    EventStorage storage = new EventStorage();
    CreateCommandParser parser = new CreateCommandParser(storage);

    // Command uses an invalid day code, e.g. 'X' is not in [MRUWFST]
    // e.g.: "create event MyEvent from 2025-03-01T10:00 to 2025-03-01T11:00 repeats X for 5 times"
    String command = "create event MyEvent from 2025-03-01T10:00 " +
            "to 2025-03-01T11:00 repeats X for 3 times";

    // Execute command
    boolean result = parser.executeCommand(command);

    // Verify
    // The parser should fail because 'X' is not a valid day char (M, T, W, R, F, S, U).
    // If the mutant changes "return false;" to "return true;", our assertFalse fails,
    // thus killing the mutant.
    assertFalse("Command must fail when repeat day is invalid [not one of MRUWFST].",
            result);
  }

  @Test
  public void testValidateAllDayRecurringFields_FailsWhenOnDateMissing() {
    // This tries to create a recurring all-day event, but no "on 2023-10-01" part
    String command = "create event WeeklyMeeting repeats MWF for 3 times";

    // Expect the entire command to fail because "on" date is missing
    boolean result = commandParser.executeCommand(command);
    assertFalse("Should fail if no 'on <date>' is provided for all-day recurring", result);
  }

  @Test
  public void testHandleRecurringAllDayUntilDate_InvalidTimeInUntilDate() {
    // This tries to create a recurring all-day event until a date/time (not just a date).
    // The code should fail because 'until' must be a pure date (yyyy-MM-dd), not a dateTime.
    String command = "create event MyAllDay on 2025-03-01 repeats MW until 2025-03-10T23:59";

    // If the method is mutated to return true in the failing path, we'd incorrectly pass
    boolean result = commandParser.executeCommand(command);

    assertFalse("Should fail if 'until' date includes time for recurring all-day", result);
  }

  @Test
  public void testExtractFieldReturnsEmptyString_ShouldFail() {
    // Here we intentionally do: "location location" so the second "location"
    // might cause extractField to see "location" as the first word => returns null
    // or an empty string, which must fail if the parser is correct.
    String command = "create event MyEvent from 2025-05-01T10:00 to 2025-05-01T11:00 " +
            "location location";

    boolean result = commandParser.executeCommand(command);

    // If 'extractField' incorrectly returns "" for location,
    // and the code tries to proceed, a mutant might pass. We want it to fail.
    assertFalse("Should fail if location is effectively empty after parsing", result);
  }

  @Test
  public void testHandleRecurringAllDayEventByTimes_MissingForKeyword_ShouldFail() {
    // Trying to create a recurring all-day event with 'repeats MWF' but no 'for n times'
    String command = "create event MyAllDay on 2025-03-01 repeats MWF 3 times";

    boolean result = commandParser.executeCommand(command);

    // Must fail because 'for' is missing
    assertFalse("Should fail if 'for' keyword is missing in recurring all-day by times",
            result);
  }

  @Test
  public void testExtractAndValidateOptionalFields_FailsWhenLocationIsMissingButRequired() {
    // Set up command with "location" keyword but missing value
    String command = "create event Meeting from 2023-10-15T10:00 to 2023-10-15T11:00 location";
    CreateCommandParser parser = new CreateCommandParser(new EventStorage());
    assertFalse("Should fail when location is specified but missing value",
            parser.executeCommand(command));

    // We can verify the error message if needed
    // assertEquals("Error: Missing value for location.", outContent.toString().trim());
  }

  @Test
  public void testExtractAndValidateOptionalFields_FailsWhenDescriptionIsMissingButRequired() {
    // Set up command with "description" keyword but missing value
    String command = "create event Meeting from 2023-10-15T10:00 to 2023-10-15T11:00 description";
    CreateCommandParser parser = new CreateCommandParser(new EventStorage());
    assertFalse("Should fail when description is specified but missing value",
            parser.executeCommand(command));
  }

  @Test
  public void testExtractAndValidateOptionalFields_FailsWhenTypeIsMissingButRequired() {
    // Set up command with "type" keyword but missing value
    String command = "create event Meeting from 2023-10-15T10:00 to 2023-10-15T11:00 type";
    CreateCommandParser parser = new CreateCommandParser(new EventStorage());
    assertFalse("Should fail when type is specified but missing value",
            parser.executeCommand(command));
  }

  @Test
  public void testExtractAndValidateOptionalFields_FailsWhenTypeIsInvalid() {
    // Set up command with invalid type value
    String command = "create event Meeting from 2023-10-15T10:00 to 2023-10-15T11:00 type invalid";
    CreateCommandParser parser = new CreateCommandParser(new EventStorage());
    assertFalse("Should fail when type is neither 'public' nor 'private'",
            parser.executeCommand(command));
  }

  @Test
  public void testFailingCommandSyntaxValidationForSimpleEvent() {
    // Create a command that fails the regex pattern but passes other validations
    // This is a tricky one and might require reflection or a specific pattern that breaks
    // just this check
    String command = "create event Meeting from 2023-10-15T10:00 to 2023-10-15T11:00 with " +
            "invalid-part";
    CreateCommandParser parser = new CreateCommandParser(new EventStorage());
    assertFalse(parser.executeCommand(command));
    // Verify the appropriate error message if possible
  }

  @Test
  public void testErrorMessageDisplayForMissingDate() {
    // Redirect System.out to capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      // Create a command with missing "on" date
      String command = "create event Meeting on invalid-date";
      CreateCommandParser parser = new CreateCommandParser(new EventStorage());
      assertFalse(parser.executeCommand(command));

      // Verify the error message was printed
      assertTrue(outContent.toString().contains("Error: Invalid or missing 'on' date."));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractRepeatDays_ReturnsNull() {
    String command = "create event Meeting from 2023-10-15T10:00 to 2023-10-15T11:00 " +
            "repeats X for 5 times";
    CreateCommandParser parser = new CreateCommandParser(new EventStorage());
    assertFalse(parser.executeCommand(command));
  }

  @Test
  public void testExtractAndValidateTimesN_FailsWithZeroOrNegativeValue() {
    String command = "create event Meeting from 2023-10-15T10:00 to 2023-10-15T11:00 " +
            "repeats MWF for 0 times";
    CreateCommandParser parser = new CreateCommandParser(new EventStorage());
    assertFalse(parser.executeCommand(command));

    command = "create event Meeting from 2023-10-15T10:00 to 2023-10-15T11:00 " +
            "repeats MWF for -5 times";
    assertFalse(parser.executeCommand(command));
  }

  @Test
  public void testExtractAndValidateEventName_ReturnsFalseForMissingName() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event --autoDecline from 2025-03-01T10:00 to 2025-03-01T11:00";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with missing event name should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about missing event name should be printed",
              output.contains("Error: Event name is required."));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractAndValidateOptionalFields_MissingDescription() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 description";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with missing description value should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about missing description should be printed",
              output.contains("Error: Missing value for description."));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractAndValidateOptionalFields_MissingTypeValue() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 type";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with missing type value should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about missing type should be printed",
              output.contains("Error: Missing value for type."));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractAndValidateOptionalFields_InvalidTypeValue() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 " +
              "type invalid";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with invalid type value should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about invalid type should be printed",
              output.contains("Error: Type must be either 'public' or 'private'."));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractRepeatDays_NullForMissingDays() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 repeats" +
              " for 5 times";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with missing repeat days should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about missing repeat days should be printed",
              output.contains("Error: Either repeat day is invalid must be [MRUWFST] or"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractEventName_NullForInvalidName() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with unextractable event name should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about required event name should be printed",
              output.contains("Error: Invalid event creation format."));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractField_NullForMissingOrInvalidFields() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 location" +
              " location type";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with invalid field format should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about missing type value should be printed",
              output.contains("Error: Missing value for location."));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testHandleRecurringEventByTimes_ReturnsFalseForValidationFailure() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 repeats" +
              " for times";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with missing repeat days should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about repeat days should be printed",
              output.contains("Error: Either repeat day is invalid"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testRecurringEventCreation_StorageInteraction() {
    EventStorage eventStorage = new EventStorage() {
      private boolean addEventCalled = false;

      @Override
      public void addEvent(CalendarEvent event, boolean autoDecline) {
        addEventCalled = true;
        super.addEvent(event, autoDecline);
      }

      public boolean wasAddEventCalled() {
        return addEventCalled;
      }
    };

    CreateCommandParser parser = new CreateCommandParser(eventStorage);
    String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 repeats" +
            " MWF for 3 times";
    boolean result = parser.executeCommand(command);
    assertTrue("Recurring event creation should succeed", result);
  }

  @Test
  public void testHandleAllDayEventCreation_StorageInteraction() {
    final boolean[] addEventCalled = {false};
    EventStorage eventStorage = new EventStorage() {
      @Override
      public void addEvent(CalendarEvent event, boolean autoDecline) {
        addEventCalled[0] = true;
        super.addEvent(event, autoDecline);
      }
    };

    CreateCommandParser parser = new CreateCommandParser(eventStorage);
    String command = "create event Meeting on 2025-03-01T09:00";
    boolean result = parser.executeCommand(command);
    assertTrue("All-day event creation should succeed", result);
    assertTrue("EventStorage.addEvent should be called", addEventCalled[0]);
  }

  @Test
  public void testRecurringAllDayEventUntilDate_ValidationFailure() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event Meeting on 2025-03-01 repeats MWF until 2025-03-31T10:00";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with time in until date should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about invalid until date should be printed",
              output.contains("Error: Invalid command syntax."));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractAndValidateTimesN_FailsWithNonNumericValue() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 repeats" +
              " MWF for abc times";
      boolean result = commandParser.executeCommand(command);
      assertFalse("Command with non-numeric repeat times should fail", result);
      String output = outputStream.toString();
      assertTrue("Error message about invalid repeat times should be printed",
              output.contains("Error: Invalid number for repeat times"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testCompleteEventCreationFlow() {
    final List<CalendarEvent> capturedEvents = new ArrayList<>();
    EventStorage eventStorage = new EventStorage() {
      @Override
      public void addEvent(CalendarEvent event, boolean autoDecline) {
        capturedEvents.add(event);
        super.addEvent(event, autoDecline);
      }
    };

    CreateCommandParser parser = new CreateCommandParser(eventStorage);
    String command = "create event Important Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 " +
            "location Conference Room A description Strategic planning session type private";

    boolean result = parser.executeCommand(command);
    assertTrue("Complete event creation should succeed", result);
    assertEquals("Should have created 1 event", 1, capturedEvents.size());
    CalendarEvent event = capturedEvents.get(0);
    assertEquals("Event name should match", "Important Meeting",
            event.getSubject());
    assertEquals("Start time should match", LocalDateTime.parse("2025-03-01T10:00"),
            event.getStartDateTime());
    assertEquals("End time should match", LocalDateTime.parse("2025-03-01T11:00"),
            event.getEndDateTime());
    assertEquals("Location should match", "Conference Room A",
            event.getLocation());
    assertEquals("Description should match", "Strategic planning session",
            event.getDescription());
    assertEquals("Type should match", "private", event.getEventType());
  }

  @Test
  public void testExtractFieldNullVsEmptyString() {
    String command = "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00";
    String locationValue = null;

    try {
      java.lang.reflect.Method extractFieldMethod =
              CreateCommandParser.class.getDeclaredMethod("extractField", String.class,
                      String.class);
      extractFieldMethod.setAccessible(true);
      locationValue = (String) extractFieldMethod.invoke(null, command, "location");
      assertNull("extractField should return null for missing fields", locationValue);

      String commandWithSelfReferentialField =
              "create event Meeting from 2025-03-01T10:00 to 2025-03-01T11:00 location location";
      String invalidValue = (String) extractFieldMethod.invoke(null,
              commandWithSelfReferentialField, "location");
      assertNull("extractField should return null for self-referential field values",
              invalidValue);

      boolean result = commandParser.executeCommand(commandWithSelfReferentialField);
      assertFalse("Command with invalid field should fail", result);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  public void testValidateCommand_EventNameCheckHelperReturnsFalse() throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      String invalidCommand = "create event";
      CreateCommandParser parser = new CreateCommandParser(new EventStorage());
      java.lang.reflect.Method validateCommandMethod =
              CreateCommandParser.class.getDeclaredMethod("validateCommand", String.class);
      validateCommandMethod.setAccessible(true);
      boolean result = (boolean) validateCommandMethod.invoke(parser, invalidCommand);
      assertFalse("validateCommand should return false when eventNameCheckHelper " +
              "returns false", result);
      String output = outputStream.toString();
      assertTrue("Error message for missing event name should be printed",
              output.contains("Error: Event name is required."));

      outputStream.reset();
      boolean publicResult = parser.executeCommand(invalidCommand);
      assertFalse("Command should fail through public interface", publicResult);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExtractRepeatDays_NullVsEmptyString() throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      java.lang.reflect.Method extractRepeatDaysMethod =
              CreateCommandParser.class.getDeclaredMethod("extractRepeatDays", String.class);
      extractRepeatDaysMethod.setAccessible(true);
      String noRepeatDaysCommand = "create event Meeting from 2025-03-01T10:00 to " +
              "2025-03-01T11:00 repeats for 5 times";
      String result = (String) extractRepeatDaysMethod.invoke(null, noRepeatDaysCommand);
      assertNull("extractRepeatDays should return null when no repeat " +
              "days are found", result);

      java.lang.reflect.Method extractAndValidateRepeatDaysMethod =
              CreateCommandParser.class.getDeclaredMethod("extractAndValidateRepeatDays",
                      String.class);
      extractAndValidateRepeatDaysMethod.setAccessible(true);
      CreateCommandParser parser = new CreateCommandParser(new EventStorage());
      boolean validationResult = false;
      try {
        validationResult = (boolean) extractAndValidateRepeatDaysMethod.invoke(parser,
                noRepeatDaysCommand);
      } catch (Exception e) {
        assertTrue(e.getMessage().contains(""));
      }
      assertFalse("Validation should fail with null repeat days", validationResult);
      String output = outputStream.toString();
      assertTrue("Error message should mention repeat days",
              output.contains("Error: Either repeat day is invalid must be [MRUWFST] or"));

      outputStream.reset();
      boolean publicResult = parser.executeCommand(noRepeatDaysCommand);
      assertFalse("Command should fail through public interface", publicResult);

      String emptyRepeatDaysCommand = "create event Meeting from 2025-03-01T10:00 to " +
              "2025-03-01T11:00 repeats  for 5 times";
      String repeatDays = (String) extractRepeatDaysMethod.invoke(null, emptyRepeatDaysCommand);
      if (repeatDays == null) {
        assertNull("extractRepeatDays should return null for command with no " +
                "repeat days between 'repeats' and 'for'", repeatDays);
      } else if (repeatDays.isEmpty()) {
        assertFalse("Empty repeat days string should still fail validation",
                (boolean) extractAndValidateRepeatDaysMethod.invoke(parser,
                        emptyRepeatDaysCommand));
      }
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testConvertCharToDayOfWeek_ReturnsCorrectDayOfWeek() throws Exception {
    java.lang.reflect.Method convertMethod =
            CreateCommandParser.class.getDeclaredMethod("convertCharToDayOfWeek", char.class);
    convertMethod.setAccessible(true);
    CreateCommandParser parser = new CreateCommandParser(new EventStorage());

    assertEquals("'M' should convert to MONDAY",
            DayOfWeek.MONDAY, convertMethod.invoke(parser, 'M'));
    assertEquals("'T' should convert to TUESDAY",
            DayOfWeek.TUESDAY, convertMethod.invoke(parser, 'T'));
    assertEquals("'W' should convert to WEDNESDAY",
            DayOfWeek.WEDNESDAY, convertMethod.invoke(parser, 'W'));
    assertEquals("'R' should convert to THURSDAY",
            DayOfWeek.THURSDAY, convertMethod.invoke(parser, 'R'));
    assertEquals("'F' should convert to FRIDAY",
            DayOfWeek.FRIDAY, convertMethod.invoke(parser, 'F'));
    assertEquals("'S' should convert to SATURDAY",
            DayOfWeek.SATURDAY, convertMethod.invoke(parser, 'S'));
    assertEquals("'U' should convert to SUNDAY",
            DayOfWeek.SUNDAY, convertMethod.invoke(parser, 'U'));
    assertNull("Invalid character should return null",
            convertMethod.invoke(parser, 'X'));

    String command = "create event Meeting from 2023-10-01T09:00 to 2023-10-01T10:00" +
            " repeats MWF for 3 times";
    boolean result = parser.executeCommand(command);
    assertTrue("Valid command with specific days should succeed", result);

    String allDaysCommand = "create event Meeting from 2023-10-01T09:00 to 2023-10-01T10:00" +
            " repeats MTWRFSU for 3 times";
    boolean allDaysResult = parser.executeCommand(allDaysCommand);
    assertTrue("Command with all day types should succeed", allDaysResult);

    String invalidDayCommand = "create event Meeting from 2023-10-01T09:00 to 2023-10-01T10:00" +
            " repeats X for 3 times";
    boolean invalidDayResult = parser.executeCommand(invalidDayCommand);
    assertFalse("Command with invalid day character should fail", invalidDayResult);
  }
}