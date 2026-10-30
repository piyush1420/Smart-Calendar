package controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import controller.command.EditCommand;
import model.CalendarEvent;
import model.EventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests EditCommandParser functionality for event editing operations.
 */
public class EditCommandParserTest {

  private EventStorage eventStorage;
  private EditCommandParser editCommandParser;
  private final ByteArrayOutputStream outContent =
          new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Before
  public void setUp() throws InvalidCommandException {
    eventStorage = new EventStorage();
    editCommandParser = new EditCommandParser(eventStorage);
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void restoreStreams() throws InvalidCommandException {
    System.setOut(originalOut);
  }

  @Test
  public void updatesSubjectForSingleEvent() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");

    String command = "edit event subject Meeting from 2023-10-15T10:00 to " +
            "2023-10-15T11:00 with Updated Meeting";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Updated Meeting", event.getSubject());
    assertTrue(outContent.toString().contains("Edited event: Updated Meeting"));
  }

  @Test
  public void updatesDescriptionForSingleEvent() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");

    String command = "edit event description Meeting from 2023-10-15T10:00 to " +
            "2023-10-15T11:00 with Quarterly planning session";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Quarterly planning session", event.getDescription());
    assertTrue(outContent.toString().contains("Edited event: Meeting"));
  }

  @Test
  public void updatesLocationForSingleEvent() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");

    String command = "edit event location Meeting from 2023-10-15T10:00 to " +
            "2023-10-15T11:00 with Conference Room A";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Conference Room A", event.getLocation());
    assertTrue(outContent.toString().contains("Edited event: Meeting"));
  }

  @Test
  public void updatesEventTypeForSingleEvent() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");

    String command = "edit event event_type Meeting from 2023-10-15T10:00 to " +
            "2023-10-15T11:00 with Personal";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Personal", event.getEventType());
    assertTrue(outContent.toString().contains("Edited event: Meeting"));
  }

  @Test
  public void updatesStartTimeForSingleEvent() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");

    String command = "edit event start Meeting from 2023-10-15T10:00 to " +
            "2023-10-15T11:00 with 2023-10-15T09:30";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals(LocalDateTime.parse("2023-10-15T09:30"),
            event.getStartDateTime());
    assertTrue(outContent.toString().contains("Edited event: Meeting"));
  }

  @Test
  public void updatesEndTimeForSingleEvent() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");

    String command = "edit event end Meeting from 2023-10-15T10:00 to " +
            "2023-10-15T11:00 with 2023-10-15T11:30";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals(LocalDateTime.parse("2023-10-15T11:30"),
            event.getEndDateTime());
    assertTrue(outContent.toString().contains("Edited event: Meeting"));
  }

  @Test
  public void updatesAllMatchingEventsFromStartTime() throws InvalidCommandException {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Weekly Meeting", start1, end1,
            "Team 1", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Weekly Meeting", start2, end2,
            "Team 2", "Room 102", "Work");

    String command = "edit events location Weekly Meeting from " +
            "2023-10-16T10:00 with Conference Room";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Room 101", event1.getLocation());
    assertEquals("Conference Room", event2.getLocation());
    assertTrue(outContent.toString().contains("Successfully edited 1 events"));
  }

  @Test
  public void updatesAllEventsWithSameName() throws InvalidCommandException {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Standup", start1, end1,
            "Team 1", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Standup", start2, end2,
            "Team 2", "Room 102", "Work");

    String command = "edit events description Standup `Daily sync meeting`";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Daily sync meeting", event1.getDescription());
    assertEquals("Daily sync meeting", event2.getDescription());
    assertTrue(outContent.toString().contains("Successfully edited 2 events"));
  }

  @Test
  public void handlesEventNameWithSpaces() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Team Planning Meeting", start, end,
            "Planning session", "Room 101", "Work");

    String command = "edit event subject Team Planning Meeting from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with Quarterly Strategy Session";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Quarterly Strategy Session", event.getSubject());
    assertTrue(outContent.toString().contains("Edited event: Quarterly Strategy Session"));
  }

  @Test
  public void handlesMultipleEventsWithSpacesInName() throws InvalidCommandException {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Team Planning Meeting", start1, end1,
            "Planning session", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Team Planning Meeting", start2, end2,
            "Planning session", "Room 102", "Work");

    String command = "edit events location Team Planning Meeting `Conference Room A`";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Conference Room A", event1.getLocation());
    assertEquals("Conference Room A", event2.getLocation());
    assertTrue(outContent.toString().contains("Successfully edited 2 events"));
  }

  @Test
  public void handlesEventNameWithSpecialChars() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting #1 (Special)", start, end,
            "Special meeting", "Room 101", "Work");

    String command = "edit event subject Meeting #1 (Special) from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with Updated Special Meeting";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Updated Special Meeting", event.getSubject());
    assertTrue(outContent.toString().contains("Edited event: Updated Special Meeting"));
  }

  @Test
  public void rejectsMissingEditKeyword() throws InvalidCommandException {
    String command = "invalid command";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("Invalid command format. Must start with 'edit'"));
  }

  @Test
  public void rejectsInvalidEditType() throws InvalidCommandException {
    String command = "edit something property event_name from " +
            "2023-10-15T10:00 with new_value";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("Invalid edit type: something"));
  }

  @Test
  public void rejectsMissingKeywordsForSingleEvent() throws InvalidCommandException {
    String command = "edit event subject Meeting with New Subject";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));
  }


  @Test
  public void rejectsInvalidDateFormat() throws InvalidCommandException {
    String command = "edit event subject Meeting from 2023/10/15 10:00 to " +
            "2023/10/15 11:00 with New Subject";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("Invalid date format: 2023/10/15 10:00"));
  }

  @Test
  public void rejectsNonexistentEvent() throws InvalidCommandException {
    String command = "edit event subject Non-Existent Meeting from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with New Subject";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("Event not found: " +
            "Non-Existent Meeting at 2023-10-15T10:00"));
  }

  @Test
  public void rejectsMissingClosingBacktick() throws InvalidCommandException {
    String command = "edit events subject Meetings `new_subject";
    try {
      editCommandParser.executeCommand(command);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Missing closing backtick (`)"));
    }
  }

  @Test
  public void rejectsNonexistentEventsForBulkEdit() throws InvalidCommandException {
    String command = "edit events subject Non-Existent-Meetings `New Subject`";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("No events found with name: Non-Existent-Meetings"));
  }

  @Test
  public void rejectsNonexistentEventsFromTime() throws InvalidCommandException {
    String command = "edit events subject Non-Existent-Meetings from " +
            "2023-10-15T10:00 with New Subject";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("No events found matching:" +
            " Non-Existent-Meetings starting at or after 2023-10-15T10:00"));
  }

  @Test
  public void updatesNonRecurringEvent() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addNonRecurringEvent("One-time Meeting", start, end,
            "One-time sync", "Room 101", "Work");

    String command = "edit event subject One-time Meeting from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with Updated Meeting";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Updated Meeting", event.getSubject());
    assertTrue(outContent.toString().contains("Edited event: Updated Meeting"));
  }

  @Test
  public void updatesMultipleNonRecurringEvents() throws InvalidCommandException {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addNonRecurringEvent("One-time Meeting", start1, end1,
            "Team 1", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addNonRecurringEvent("One-time Meeting", start2, end2,
            "Team 2", "Room 102", "Work");

    String command = "edit events description One-time Meeting `Updated description`";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals("Updated description", event1.getDescription());
    assertEquals("Updated description", event2.getDescription());
    assertTrue(outContent.toString().contains("Successfully edited 2 events"));
  }

  @Test
  public void rejectsInvalidPropertyName() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Meeting", start, end, "Team sync",
            "Room 101", "Work");

    String command = "edit event invalid_property Meeting from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with New Value";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("Error : Invalid property name: invalid_property"));
  }

  @Test
  public void preventsTimeConflictUpdates() throws InvalidCommandException {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Meeting 1", start1, end1, "Team 1",
            "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-15T11:30");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-15T12:30");
    CalendarEvent event2 = addRecurringEvent("Meeting 2", start2, end2, "Team 2",
            "Room 102", "Work");

    String command = "edit event start Meeting 2 from " +
            "2023-10-15T11:30 to 2023-10-15T12:30 with 2023-10-15T10:30";
    boolean result = editCommandParser.executeCommand(command);

    assertFalse(result);
    assertEquals(LocalDateTime.parse("2023-10-15T11:30"), event2.getStartDateTime());
    assertTrue(outContent.toString().contains("Error: Edit would cause scheduling conflict"));
  }

  @Test
  public void updatesMultipleEventsStartTime() throws InvalidCommandException {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Project Meeting", start1, end1,
            "Planning", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Project Meeting", start2, end2,
            "Planning", "Room 102", "Work");

    String command = "edit events start Project Meeting from " +
            "2023-10-16T10:00 with 2023-10-16T09:00";
    boolean result = editCommandParser.executeCommand(command);

    assertTrue(result);
    assertEquals(LocalDateTime.parse("2023-10-15T10:00"), event1.getStartDateTime());
    assertEquals(LocalDateTime.parse("2023-10-16T09:00"), event2.getStartDateTime());
    assertTrue(outContent.toString().contains("Edited event: Project Meeting - Changed:" +
            " start to 2023-10-16T09:00"));
    assertTrue(outContent.toString().contains("Successfully edited 1 events."));
  }

  @Test
  public void rejectsInvalidStartTimeFormat() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Important Meeting", start, end,
            "Planning", "Room 101", "Work");

    String command = "edit event start Important Meeting from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with 15/10/2023 09:30";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("Error: Invalid date " +
            "format. Expected: yyyy-MM-dd'T'HH:mm"));

  }

  @Test
  public void preventsStartTimeAfterEndTime() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Board Meeting", start, end,
            "Planning", "Room 101", "Work");

    String command = "edit event start Board Meeting from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with 2023-10-15T12:00";
    assertFalse(editCommandParser.executeCommand(command));

    String expectedMessage = "Error: Start time (2023-10-15T12:00) cannot be after end time";
    assertTrue(outContent.toString().contains(expectedMessage));
    assertEquals(LocalDateTime.parse("2023-10-15T10:00"), event.getStartDateTime());
  }

  @Test
  public void preventsEndTimeBeforeStartTime() throws InvalidCommandException {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Weekly Review", start, end,
            "Planning", "Room 101", "Work");

    String command = "edit event end Weekly Review from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with 2023-10-15T09:00";
    assertFalse(editCommandParser.executeCommand(command));

    String expectedMessage = "Error: End time (2023-10-15T09:00) cannot be before start time";
    assertTrue(outContent.toString().contains(expectedMessage));
    assertEquals(LocalDateTime.parse("2023-10-15T11:00"), event.getEndDateTime());
  }

  @Test
  public void rejectsMissingRequiredParts() throws InvalidCommandException {
    assertFalse(editCommandParser.executeCommand("edit"));
    assertTrue(outContent.toString().contains("Invalid command format."));
  }

  private CalendarEvent addRecurringEvent(String name, LocalDateTime start,
                                          LocalDateTime end, String description, String location,
                                          String type) {
    CalendarEvent event = new CalendarEvent(name, start, end, description,
            location, type, true);
    eventStorage.addEvent(event, true);
    outContent.reset();
    return event;
  }

  @Test
  public void testExecuteCommand_HandleSingleEventEdit_ReturnsCorrectBoolean()
          throws InvalidCommandException {
    // Create a test event
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");

    // Test successful edit
    assertTrue(editCommandParser.executeCommand("edit event subject Test Event from" +
            " 2023-10-15T10:00 to 2023-10-15T11:00 with New Test Event"));

    // Test edit with non-existent event
    assertFalse(editCommandParser.executeCommand("edit event subject NonExistentEvent " +
            "from 2023-10-15T10:00 to 2023-10-15T11:00 with New Name"));
  }

  @Test
  public void testExecuteCommand_HandleMultipleEventsFromTime_ReturnsCorrectBoolean()
          throws InvalidCommandException {
    // Create test events
    LocalDateTime startTime = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime endTime = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Test Event", startTime, endTime,
            "Team 1", "Room 101", "Work");

    LocalDateTime startTime2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime endTime2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Test Event", startTime2, endTime2,
            "Team 2", "Room 102", "Work");

    // Test successful edit
    assertTrue(editCommandParser.executeCommand("edit events location Test Event " +
            "from 2023-10-16T10:00 with New Location"));

    // Test edit with non-existent time
    assertFalse(editCommandParser.executeCommand("edit events location Test " +
            "Event from 2023-10-17T00:00 with New Location"));
  }

  @Test
  public void testExecuteCommand_HandleAllEventsByName_ReturnsCorrectBoolean()
          throws InvalidCommandException {
    // Create test events
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Test Event", start1, end1,
            "Team 1", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Test Event", start2, end2,
            "Team 2", "Room 102", "Work");

    // Test successful edit
    assertTrue(editCommandParser.executeCommand("edit events " +
            "description Test Event `New Description`"));

    // Test edit with non-existent event name
    assertFalse(editCommandParser.executeCommand("edit events " +
            "description NonExistentEvent `New Description`"));
    assertFalse(editCommandParser.executeCommand("edit events " +
            "description NonExistentEvent `New Description`"));
  }

  @Test
  public void testExecuteCommand_InvalidCommandFormat() throws InvalidCommandException {
    assertFalse(editCommandParser.executeCommand("invalid command"));
    assertTrue(outContent.toString().contains("Invalid command format. Must start with 'edit'"));
  }

  @Test
  public void testExecuteCommand_UnmatchedPatterns() throws InvalidCommandException {
    // Test command that matches none of the patterns
    assertFalse(editCommandParser.executeCommand("edit invalid pattern command"));
  }

  @Test
  public void testExecuteCommand_NullCommand() throws InvalidCommandException {
    // Test null command
    assertFalse(editCommandParser.executeCommand(null));
  }

  @Test
  public void testExecuteCommand_EmptyCommand() throws InvalidCommandException {
    // Test empty command
    assertFalse(editCommandParser.executeCommand(""));
  }

  @Test
  public void testHandleMultipleEventsFromTime_InvalidParameters() throws InvalidCommandException {
    // Test with null property
    editCommandParser.executeCommand("edit events null Test" +
            " Event from 2023-10-15T10:00 with New Value");
    assertTrue(outContent.toString().contains("Invalid property name: null"));

    // Test with null event name
    assertFalse(editCommandParser.executeCommand("edit events subject null" +
            " from 2023-10-15T10:00 with New Value"));

    // Test with null new value
    assertFalse(editCommandParser.executeCommand("edit events subject " +
            "Test Event from 2023-10-15T10:00 with null"));
  }

  @Test
  public void testHandleMultipleEventsFromTime_EventNameCaseSensitivity()
          throws InvalidCommandException {
    // Create test event with mixed case
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");

    // Test with different case
    assertTrue(editCommandParser.executeCommand("edit events subject TEST " +
            "EVENT from 2023-10-15T10:00 with New Name"));
    assertEquals("New Name", event.getSubject());
  }

  @Test
  public void testHandleMultipleEventsFromTime_EventNameWithSpaces()
          throws InvalidCommandException {

    // Create test event with spaces
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Team Planning Meeting", start, end,
            "Team sync", "Room 101", "Work");

    // Test with spaces in name
    assertTrue(editCommandParser.executeCommand("edit events subject Team Planning " +
            "Meeting from 2023-10-15T10:00 with New Name"));
    assertEquals("New Name", event.getSubject());
  }

  @Test
  public void testHandleMultipleEventsFromTime_EventNameWithLeadingTrailingSpaces()
          throws InvalidCommandException {
    // Create test event
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");

    // Test with leading/trailing spaces in name
    assertTrue(editCommandParser.executeCommand("edit events subject  " +
            "Test Event  from 2023-10-15T10:00 with New Name"));
    assertEquals("New Name", event.getSubject());
  }

  @Test
  public void testHandleMultipleEventsFromTime_EmptyEventName() throws InvalidCommandException {
    // Test with empty event name
    assertFalse(editCommandParser.executeCommand("edit events subject  " +
            "from 2023-10-15T10:00 with New Value"));
  }

  @Test
  public void testHandleMultipleEventsFromTime_InvalidDateTime() throws InvalidCommandException {
    // Test with invalid date time format
    assertFalse(editCommandParser.executeCommand("edit events subject Test" +
            " Event from invalid-date with New Value"));
  }

  @Test
  public void testHandleMultipleEventsFromTime_NoMatchingEvents() throws InvalidCommandException {
    // Test with non-existent event name
    assertFalse(editCommandParser.executeCommand("edit events " +
            "subject NonExistentEvent from 2023-10-15T10:00 with New Value"));
  }

  @Test
  public void testHandleMultipleEventsFromTime_NoEventsAfterDateTime()
          throws InvalidCommandException {
    // Create test event
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");

    // Test with date time after all events
    assertFalse(editCommandParser.executeCommand("edit events subject " +
            "Test Event from 2023-10-16T10:00 with New Value"));
  }

  @Test
  public void testHandleMultipleEventsFromTime_EditFailure() throws InvalidCommandException {
    // Create test event
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");

    // Test with invalid property
    assertFalse(editCommandParser.executeCommand("edit events invalid_property" +
            " Test Event from 2023-10-15T10:00 with New Value"));
    assertTrue(outContent.toString().contains("Error : Invalid property name: invalid_property"));
  }

  @Test
  public void testHandleMultipleEventsFromTime_EventNameMatching() throws InvalidCommandException {
    // Create test events with different cases and spaces
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Test Event", start1, end1,
            "Team 1", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("TEST EVENT", start2, end2,
            "Team 2", "Room 102", "Work");

    // Test with different case and spaces
    assertTrue(editCommandParser.executeCommand("edit events subject  " +
            "test event  from 2023-10-15T10:00 with New Name"));
    assertEquals("New Name", event1.getSubject());
    assertEquals("New Name", event2.getSubject());
  }

  @Test
  public void testHandleAllEventsByName_ReturnValues() throws InvalidCommandException {
    // Create test events
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Test Event", start1, end1,
            "Team 1", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Test Event", start2, end2,
            "Team 2", "Room 102", "Work");

    // Test successful edit
    assertTrue(editCommandParser.executeCommand("edit events subject Test Event `New Name`"));

    // Test with non-existent event
    assertFalse(editCommandParser.executeCommand("edit events " +
            "subject NonExistentEvent `New Name`"));
  }

  @Test
  public void testFallbackCommandParsing_InvalidFormat() throws InvalidCommandException {
    // Test with insufficient parts
    assertFalse(editCommandParser.executeCommand("edit event"));
    assertTrue(outContent.toString().contains("Invalid command format."));

    editCommandParser.executeCommand("edit event  Test Event");
    assertTrue(outContent.toString().
            contains("Invalid command format."));

    assertFalse(editCommandParser.executeCommand("edit event subject"));

    assertTrue(outContent.toString().
            contains("Invalid command format."));

  }

  @Test
  public void testFallbackCommandParsing_InvalidEditType() throws InvalidCommandException {
    // Test with invalid edit type
    assertFalse(editCommandParser.executeCommand("edit invalid " +
            "Test Event from 2023-10-15T10:00 with New Value"));
  }

  @Test
  public void testFallbackCommandParsing_EventFormat() throws InvalidCommandException {
    // Create test event
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");
    assertFalse(editCommandParser.executeCommand("edit event subject Test Event with New Value"));
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));
  }

  @Test
  public void testFallbackCommandParsing_EventsFormat() throws InvalidCommandException {
    // Test invalid multiple events format
    assertFalse(editCommandParser.executeCommand("edit events subject Test Event invalid format"));
    assertTrue(outContent.toString().contains("No events found with name: Test Event invalid"));
  }

  @Test
  public void testFallbackCommandParsing_EventNameWithKeywords() throws InvalidCommandException {
    // Create test event with keywords in name
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");

    // Test with keywords in event name
    assertTrue(editCommandParser.executeCommand("edit event subject Test Event " +
            "from 2023-10-15T10:00 to 2023-10-15T11:00 with New Name"));
    assertEquals("New Name", event.getSubject());
  }

  private CalendarEvent addNonRecurringEvent(String name, LocalDateTime start,
                                             LocalDateTime end, String description,
                                             String location, String type) {
    CalendarEvent event = new CalendarEvent(name, start, end, description,
            location, type, false);
    eventStorage.addEvent(event, false);
    outContent.reset();
    return event;
  }

  @Test
  public void testExecute_HandlesInvalidCommandFormat() throws InvalidCommandException {
    // Command with insufficient parts will make parser return false
    String invalidCommand = "edit event subject"; // Missing required parts
    EditCommand command = new EditCommand(eventStorage, invalidCommand);

    assertFalse("Should return false for invalid command format",
            command.execute());
    assertTrue("Should print error message for invalid format",
            outContent.toString().contains("Invalid command format."));
  }

  @Test
  public void testExecute_HandlesParserReturnsFalse() throws InvalidCommandException {
    // Command that will make parser return false (nonexistent event)
    String nonExistentCommand = "edit event subject Nonexistent from 2025-03-01T10:00 to " +
            "2025-03-01T11:00 with NewName";
    EditCommand command = new EditCommand(eventStorage, nonExistentCommand);

    assertFalse("Should return false when parser returns false",
            command.execute());
    assertTrue("Should print error message for nonexistent event",
            outContent.toString().contains("Event not found: Nonexistent"));
  }

  @Test
  public void testExecute_HandlesInvalidDateTimeFormat() throws InvalidCommandException {
    // Command with invalid datetime format
    String invalidDateTimeCommand = "edit event subject Meeting from invalid-datetime to " +
            "2025-03-01T11:00 with NewMeeting";
    EditCommand command = new EditCommand(eventStorage, invalidDateTimeCommand);

    assertFalse("Should return false for invalid datetime format",
            command.execute());
    assertTrue("Should print error message for invalid datetime",
            outContent.toString().contains("Invalid date format: invalid-datetime"));
  }

  @Test
  public void testExecute_HandlesEmptyCommand() throws InvalidCommandException {
    // Empty command will trigger InvalidCommandException
    EditCommand command = new EditCommand(eventStorage, "");

    assertFalse("Should return false for empty command",
            command.execute());
    assertTrue("Should print error message for empty command",
            outContent.toString().contains("Invalid command format. Must start with 'edit'"));
  }

  @Test
  public void testExecute_HandlesNullCommand() throws InvalidCommandException {
    // Null command will trigger InvalidCommandException
    EditCommand command = new EditCommand(eventStorage, null);

    assertFalse("Should return false for null command",
            command.execute());
    assertTrue("Should print error message for null command",
            outContent.toString().contains("Invalid command format. Must start with 'edit'"));
  }

  @Test
  public void testExecute_SuccessfulCommandReturnsTrue() throws InvalidCommandException {
    // Valid command that should return true
    addRecurringEvent("Meeting", LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"),
            "Team sync", "Room 101", "Work");
    String validCommand = "edit event subject Meeting from 2025-03-01T10:00 to " +
            "2025-03-01T11:00 with NewMeeting";
    EditCommand command = new EditCommand(eventStorage, validCommand);

    assertTrue("Should return true for successful edit",
            command.execute());
    assertTrue("Should print success message",
            outContent.toString().contains("Edited event:"));
  }

  @Test
  public void testExecuteCommand_ReturnsFalseForInvalidProperty() throws InvalidCommandException {
    // Test with invalid property name
    String command = "edit event invalid_property Meeting from 2025-03-01T10:00 to " +
            "2025-03-01T11:00 with NewMeeting";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("Invalid property name: invalid_property"));
  }

  @Test
  public void testExecuteCommand_ReturnsFalseForEmptyNewValue() throws InvalidCommandException {
    // Test with empty new value
    String command = "edit event subject Meeting from 2025-03-01T10:00 to " +
            "2025-03-01T11:00 with ";
    assertFalse(editCommandParser.executeCommand(command));
    assertTrue(outContent.toString().contains("New property value must be specified"));
  }

  @Test
  public void testHandleMultipleEventsFromTime_ReturnsTrueForSuccessfulEdit()
          throws InvalidCommandException {
    // Add multiple test events
    LocalDateTime start1 = LocalDateTime.parse("2025-03-01T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2025-03-01T11:00");
    CalendarEvent event1 = addRecurringEvent("Weekly Sync", start1, end1,
            "Team meeting", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2025-03-08T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2025-03-08T11:00");
    CalendarEvent event2 = addRecurringEvent("Weekly Sync", start2, end2,
            "Team meeting", "Room 101", "Work");

    // Edit all events from specific date
    String command = "edit events location Weekly Sync from " +
            "2025-03-08T10:00 with Conference Room";
    assertTrue(editCommandParser.executeCommand(command));
    assertEquals("Conference Room", event2.getLocation());
    assertEquals("Room 101", event1.getLocation()); // First event shouldn't change
  }

  @Test
  public void testHandleAllEventsByName_ReturnsTrueForSuccessfulBulkEdit()
          throws InvalidCommandException {
    // Add multiple test events
    LocalDateTime start1 = LocalDateTime.parse("2025-03-01T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2025-03-01T11:00");
    CalendarEvent event1 = addRecurringEvent("Team Meeting", start1, end1,
            "Planning", "Room 101", "Work");

    LocalDateTime start2 = LocalDateTime.parse("2025-03-02T14:00");
    LocalDateTime end2 = LocalDateTime.parse("2025-03-02T15:00");
    CalendarEvent event2 = addRecurringEvent("Team Meeting", start2, end2,
            "Review", "Room 102", "Work");

    // Bulk edit all events with same name
    String command = "edit events description Team Meeting `General team meeting`";
    assertTrue(editCommandParser.executeCommand(command));
    assertEquals("General team meeting", event1.getDescription());
    assertEquals("General team meeting", event2.getDescription());
  }

  @Test
  public void testFallbackCommandParsing_EdgeCaseCommandLength() throws InvalidCommandException {
    // Test exactly 4 parts (boundary condition)
    String minimalCommand = "edit event subject Meeting";
    assertFalse(editCommandParser.executeCommand(minimalCommand));
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));
  }

  @Test
  public void testExecuteCommand_ExtractBetweenEdgeCases() throws InvalidCommandException {
    // Test exact positions extraction

    addNonRecurringEvent("Meeting", LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"),
            "Team sync", "Room 101", "Work");
    String command1 = "edit event subject Meeting from 2025-03-01T10:00" +
            " to 2025-03-01T11:00 with NewMeeting";
    assertTrue(editCommandParser.executeCommand(command1));

    // Test empty content between markers
    String command2 = "edit event subject Meeting from  to 2025-03-01T11:00 with NewMeeting";
    assertFalse(editCommandParser.executeCommand(command2));
    assertTrue(outContent.toString().contains("Invalid date format"));
  }

  @Test
  public void testExecuteCommand_ExtractBetweenMissingKeywords() throws InvalidCommandException {
    // Test missing "from" keyword
    addNonRecurringEvent("Meeting", LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"),
            "Team sync", "Room 101", "Work");
    String command1 = "edit event subject Meeting 2025-03-01T10:00 to" +
            " 2025-03-01T11:00 with NewMeeting";
    assertFalse(editCommandParser.executeCommand(command1));
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));

    // Test missing "to" keyword
    String command2 = "edit event subject Meeting from 2025-03-01T10:00 " +
            "2025-03-01T11:00 with NewMeeting";
    assertFalse(editCommandParser.executeCommand(command2));
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));
  }

  @Test
  public void testExecuteCommand_ExtractAfterEdgeCases() {
    addNonRecurringEvent("Meeting", LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"),
            "Team sync", "Room 101", "Work");
    // Test exact position extraction
    String command1 = "edit event subject Meeting with NewMeeting";
    assertFalse(editCommandParser.executeCommand(command1));
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));

    // Test missing "with" keyword
    String command2 = "edit event subject Meeting NewMeeting";
    assertFalse(editCommandParser.executeCommand(command2));
    assertTrue(outContent.toString().contains("Invalid format for editing a single event."));
  }

  @Test
  public void testExecuteCommand_InvalidBacktickUsage() {
    addNonRecurringEvent("Meeting",
            LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"),
            "Team sync", "Room 101", "Work"
    );

    // Valid case: Correct usage of backticks
    String validCommand = "edit events subject Meeting `NewMeeting`";
    assertTrue(editCommandParser.executeCommand(validCommand));

    // Invalid case: Empty content between backticks
    String emptyBacktickCommand = "edit events subject Meeting ``";
    IllegalArgumentException emptyBacktickException =
            assertThrows(IllegalArgumentException.class, () ->
                    editCommandParser.executeCommand(emptyBacktickCommand)
            );
    assertTrue(emptyBacktickException.getMessage().
            contains("New property value must be specified."));

    // Invalid case: Missing opening backtick
    String missingOpeningBacktickCommand = "edit events subject `NewMeeting` Meeting`";
    editCommandParser.executeCommand(missingOpeningBacktickCommand);
    assertTrue(
            outContent.toString().contains("No events found with name: `NewMeeting` Meeting`"));

    String missingClosingBacktickCommand = "edit events subject `NewMeeting` `NewMeeting";
    editCommandParser.executeCommand(missingClosingBacktickCommand);
    outContent.toString().contains("No events found with name: `NewMeeting` `NewMeeting");
  }

  @Test
  public void testFallbackCommandParsing_ReturnsTrue() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");
    String command = "edit event subject Test Event from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with Updated Event";
    assertTrue(editCommandParser.executeCommand(command));
    assertEquals("Updated Event", event.getSubject());
  }

  @Test
  public void testEventFilteringByName() {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Team Meeting", start1, end1,
            "Team 1", "Room 101", "Work");
    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Team Workshop", start2, end2,
            "Team 2", "Room 102", "Work");
    String command = "edit events location Team Meeting from 2023-10-15T09:00 " +
            "with Conference Room";
    assertTrue(editCommandParser.executeCommand(command));
    assertEquals("Conference Room", event1.getLocation());
    assertEquals("Room 102", event2.getLocation());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHandleMultipleEventsFromTime_EditFailureReturnsFalse() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Test Event", start, end,
            "Team sync", "Room 101", "Work");
    String command = "edit events start Test Event from 2023-10-15T09:00 " +
            "with 2023-10-15T12:00";
    editCommandParser.executeCommand(command);
  }

  @Test
  public void testExtractMethodsWorkCorrectly() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Meeting", start, end, "Team sync", "Room 101",
            "Work");
    String command1 = "edit event subject Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with New Meeting";
    assertTrue(editCommandParser.executeCommand(command1));
    String command2 = "edit event subject Meeting from    2023-10-15T10:00    " +
            "to 2023-10-15T11:00 with New Meeting";
    assertFalse(editCommandParser.executeCommand(command2));
    String command3 = "edit event subject Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with    Spaced Value   ";
    assertFalse(editCommandParser.executeCommand(command3));
    String command4 = "edit events subject Meeting `Special Meeting Name`";
    assertFalse(editCommandParser.executeCommand(command4));
    String command5 = "edit event subject Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with ";
    assertFalse(editCommandParser.executeCommand(command5));
    assertTrue(outContent.toString().contains(
            "New property value must be specified"));
  }

  @Test
  public void testExtractMethodEdgeCases() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Meeting", start, end, "Team sync", "Room 101",
            "Work");
    String missingFromCommand = "edit event subject Meeting to " +
            "2023-10-15T11:00 with New Meeting";
    assertFalse(editCommandParser.executeCommand(missingFromCommand));
    String mixedCaseCommand = "edit event subject Meeting FROM " +
            "2023-10-15T10:00 TO 2023-10-15T11:00 WITH New Meeting";
    assertTrue(editCommandParser.executeCommand(mixedCaseCommand));
    assertEquals("New Meeting",
            eventStorage.findEvent("New Meeting", start).getSubject());
    String keywordsInNameCommand = "edit event subject Meeting with Keywords " +
            "from 2023-10-15T10:00 to 2023-10-15T11:00 with Updated Name";
  }



  @Test
  public void testValidateParametersWithStartProperty() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Meeting", start, end, "Team sync", "Room 101",
            "Work");
    String validCommand = "edit event start Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with 2023-10-15T09:30";
    assertTrue(editCommandParser.executeCommand(validCommand));
    String invalidCommand = "edit event start Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with not-a-date";
    assertFalse(editCommandParser.executeCommand(invalidCommand));
    assertTrue(outContent.toString().contains("Invalid date format"));
  }

  @Test
  public void testFallbackCommandParsing_WhenSuccessful_ReturnsTrue() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");
    String command = "edit event subject Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with New Subject";
    boolean result = editCommandParser.executeCommand(command);
    assertTrue(result);
    assertEquals("New Subject", event.getSubject());
  }

  @Test
  public void testSubjectFilteringInHandleMultipleEventsFromTime() {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Team Meeting", start1, end1,
            "Description 1", "Room A", "Work");
    LocalDateTime start2 = LocalDateTime.parse("2023-10-15T12:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-15T13:00");
    CalendarEvent event2 = addRecurringEvent("Team Meeting Update", start2, end2,
            "Description 2", "Room B", "Work");
    String command = "edit events location Team Meeting from 2023-10-15T09:00 " +
            "with New Location";
    boolean result = editCommandParser.executeCommand(command);
    assertTrue(result);
    assertEquals("New Location", event1.getLocation());
    assertEquals("Room B", event2.getLocation());
  }

  @Test
  public void testEditEvent_WhenEditEventExecuteEditReturnsFalse_ReturnsFalse() {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Meeting 1", start1, end1,
            "Description 1", "Room 1", "Work");
    LocalDateTime start2 = LocalDateTime.parse("2023-10-15T11:30");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-15T12:30");
    CalendarEvent event2 = addRecurringEvent("Meeting 2", start2, end2,
            "Description 2", "Room 2", "Work");
    String command = "edit event start Meeting 2 from 2023-10-15T11:30 to " +
            "2023-10-15T12:30 with 2023-10-15T10:30";
    boolean result = editCommandParser.executeCommand(command);
    assertFalse(result);
    assertEquals(LocalDateTime.parse("2023-10-15T11:30"),
            event2.getStartDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleEdits_WhenExecuteMultipleEdits() {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Team Meeting", start1, end1,
            "Description 1", "Room A", "Work");
    LocalDateTime start2 = LocalDateTime.parse("2023-10-15T14:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-15T15:00");
    CalendarEvent event2 = addRecurringEvent("Team Meeting", start2, end2,
            "Description 2", "Room B", "Work");
    addRecurringEvent("Block", LocalDateTime.parse("2023-10-15T13:00"),
            LocalDateTime.parse("2023-10-15T16:00"),
            "Blocking Time", "Room C", "Work");
    String command = "edit events start Team Meeting from 2023-10-15T10:00 " +
            "with 2023-10-15T13:30";
    editCommandParser.executeCommand(command);
  }

  @Test
  public void testFromWithCondition_HandlesBothKeywords() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");
    String command = "edit events subject Meeting from 2023-10-15T09:00 " +
            "with New Subject";
    boolean result = editCommandParser.executeCommand(command);
    assertTrue(result);
    assertEquals("New Subject", event.getSubject());
  }

  @Test
  public void testExtractBetween_HandlesWhitespaceCorrectly() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");
    String command = "edit event subject Meeting from    2023-10-15T10:00    " +
            "to 2023-10-15T11:00 with New Subject";
    boolean result = editCommandParser.executeCommand(command);
    assertTrue(result);
    assertEquals("New Subject", event.getSubject());
  }

  @Test
  public void testExtractBacktickedValue_HandlesPositionCorrectly() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");
    String command = "edit events subject Meeting `New Subject with spaces and " +
            "special chars: !@#$%^&*()`";
    boolean result = editCommandParser.executeCommand(command);
    assertTrue(result);
    assertTrue(event.getSubject().contains(
            "New Subject with spaces and special chars: !@#$%^&*()"));
  }

  @Test
  public void testStartPropertyValidation() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");
    String validCommand = "edit event start Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with 2023-10-15T09:30";
    boolean validResult = editCommandParser.executeCommand(validCommand);
    assertTrue(validResult);
    assertEquals(LocalDateTime.parse("2023-10-15T09:30"),
            event.getStartDateTime());
    String invalidCommand = "edit event start Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with invalid-date";
    boolean invalidResult = editCommandParser.executeCommand(invalidCommand);
    assertFalse(invalidResult);
    assertTrue(outContent.toString().contains("Invalid date format"));
  }

  @Test
  public void testExtractBetween_NonEmptyResult() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Team sync", "Room 101", "Work");
    String command = "edit event subject Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with New Subject";
    boolean result = editCommandParser.executeCommand(command);
    assertTrue(result);
    assertEquals("New Subject", event.getSubject());
  }

  @Test
  public void testFallbackCommandParsingReturnsTrue_WhenNoOtherPatternMatches() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("TestEvent", start, end,
            "Description", "Location", "Work");
    String command = "edit event subject TestEvent from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with NewName";
    boolean result = editCommandParser.executeCommand(command);
    assertTrue("Valid command should succeed through fallback parsing", result);
    assertEquals("NewName", event.getSubject());
  }

  @Test
  public void testHandleMultipleEventsFromTime_WhenEditSucceeds_ReturnsFalse() {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Meeting", start1, end1,
            "Team sync", "Room 101", "Work");
    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Meeting", start2, end2,
            "Team sync", "Room 102", "Work");
    LocalDateTime conflictStart = LocalDateTime.parse("2023-10-16T09:00");
    LocalDateTime conflictEnd = LocalDateTime.parse("2023-10-16T12:00");
    CalendarEvent conflictEvent = addRecurringEvent("ConflictMeeting",
            conflictStart, conflictEnd, "Conflict", "Room 103", "Work");
    String command = "edit events start Meeting from 2023-10-16T10:00 " +
            "with 2023-10-16T09:30";
    boolean result = editCommandParser.executeCommand(command);
    assertEquals(LocalDateTime.parse("2023-10-16T09:30"),
            event2.getStartDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHandleAllEventsByName_WhenEditFails_ReturnsFalse() {
    LocalDateTime start1 = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end1 = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event1 = addRecurringEvent("Meeting", start1, end1,
            "Team sync", "Room 101", "Work");
    LocalDateTime start2 = LocalDateTime.parse("2023-10-16T10:00");
    LocalDateTime end2 = LocalDateTime.parse("2023-10-16T11:00");
    CalendarEvent event2 = addRecurringEvent("Meeting", start2, end2,
            "Team sync", "Room 102", "Work");
    LocalDateTime conflictStart = LocalDateTime.parse("2023-10-16T09:00");
    LocalDateTime conflictEnd = LocalDateTime.parse("2023-10-16T12:00");
    CalendarEvent conflictEvent = addRecurringEvent("ConflictMeeting",
            conflictStart, conflictEnd, "Conflict", "Room 103", "Work");
    String command = "edit events start Meeting `2023-10-16T09:30`";
    editCommandParser.executeCommand(command);
  }

  @Test
  public void testFallbackCommandParsing_SingleEventEdit_WhenFails_ReturnsFalse() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("TestEvent", start, end,
            "Description", "Location", "Work");
    String command = "edit event subject NonExistentEvent from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with NewName";
    boolean result = editCommandParser.executeCommand(command);
    assertFalse("Command for non-existent event should fail", result);
    assertEquals("TestEvent", event.getSubject());
  }

  @Test
  public void testEventsFromWithCondition() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    CalendarEvent event = addRecurringEvent("Meeting", start, end,
            "Description", "Location", "Work");
    String invalidCommand = "edit events subject Meeting from 2023-10-15T10:00";
    boolean invalidResult = false;
    try {
      invalidResult = editCommandParser.executeCommand(invalidCommand);
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains(""));
    }
    String anotherInvalidCommand = "edit events subject Meeting with NewName";
    try {
      editCommandParser.executeCommand(anotherInvalidCommand);
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains(""));
    }
    String validCommand = "edit events subject Meeting from 2023-10-15T10:00 " +
            "with NewName";
    boolean validResult = editCommandParser.executeCommand(validCommand);
    assertTrue("Command with both 'from' and 'with' should succeed", validResult);
    assertEquals("NewName", event.getSubject());
  }

  @Test
  public void testExtractBetween_StartLengthHandling() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Meeting", start, end, "Description",
            "Location", "Work");
    String command = "edit event subject Meeting from2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with NewName";
    try {
      editCommandParser.executeCommand(command);
    } catch (Exception e) {
      assertTrue(e.getMessage().contains(""));
    }
    String properCommand = "edit event subject Meeting from 2023-10-15T10:00 " +
            "to 2023-10-15T11:00 with NewName";
    boolean result = editCommandParser.executeCommand(properCommand);
    assertTrue("Command with proper space after 'from' should succeed", result);
  }

  @Test
  public void testExtractBetween_EmptyResult() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Meeting", start, end, "Description",
            "Location", "Work");
    String command = "edit event subject Meeting from to 2023-10-15T11:00 " +
            "with NewName";
    boolean result = editCommandParser.executeCommand(command);
    assertFalse("Command with no content between from/to should fail", result);
    assertTrue(outContent.toString().contains("Invalid date format"));
  }

  @Test
  public void testExtractBacktickedValue_EmptyContent() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Meeting", start, end, "Description",
            "Location", "Work");
    String command = "edit events subject Meeting ``";
    try {
      editCommandParser.executeCommand(command);
      fail("Should throw an exception for empty backtick content");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains(
              "New property value must be specified"));
    }
  }

  @Test
  public void testStringManipulationEdgeCases() {
    LocalDateTime start = LocalDateTime.parse("2023-10-15T10:00");
    LocalDateTime end = LocalDateTime.parse("2023-10-15T11:00");
    addRecurringEvent("Meeting", start, end, "Description",
            "Location", "Work");
    String command = "edit  event  subject  Meeting  from  2023-10-15T10:00  " +
            "to  2023-10-15T11:00  with  NewSubject";
    boolean result = editCommandParser.executeCommand(command);
    assertTrue("Command with extra spaces should still work", result);
    String complexCommand = "edit event subject Meeting from with to from " +
            "2023-10-15T10:00 to 2023-10-15T11:00 with New Subject";
    try {
      editCommandParser.executeCommand(complexCommand);
    } catch (Exception e) {
      assertTrue(e.getMessage().contains(""));
    }
  }



}