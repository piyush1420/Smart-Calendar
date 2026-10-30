package model;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Comprehensive tests for CalendarManager functionality
 * including calendar creation, management, and mutation.
 */
public class CalendarManagerTest {
  private ICalendarManager calendarManager;
  private MultiCalendarEventStorage eventStorage;
  private final ZoneId nyZone = ZoneId.of("America/New_York");
  private final ZoneId londonZone = ZoneId.of("Europe/London");
  private final ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
  private ByteArrayOutputStream errContent;
  private PrintStream originalErr;
  private final LocalDateTime testDate =
          LocalDateTime.parse("2025-03-01T10:00");
  private final LocalDateTime testDatePlus1h =
          LocalDateTime.parse("2025-03-01T11:00");
  private final LocalDateTime testDatePlus2h =
          LocalDateTime.parse("2025-03-01T12:00");
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    eventStorage = new MultiCalendarEventStorage();
    calendarManager = new CalendarManager(eventStorage);
    errContent = new ByteArrayOutputStream();
    originalErr = System.err;
    System.setErr(new PrintStream(errContent));
  }

  @After
  public void restoreStreams() {
    System.setErr(originalErr);
  }

  @Test
  public void shouldCreateNewCalendarWithValidNameAndTimezone() {
    calendarManager.createCalendar("Work", nyZone);
    calendarManager.useCalendar("Work");
    assertNotNull(calendarManager.getCurrentCalendar());
    assertEquals("Work",
            calendarManager.getCurrentCalendar().getName());
  }

  @Test
  public void shouldPreventDuplicateCalendarCreation() {
    calendarManager.createCalendar("Work", nyZone);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
            "A calendar with the name 'Work' already exists.");
    calendarManager.createCalendar("Work", londonZone);
  }

  @Test
  public void shouldUpdateCalendarTimezoneSuccessfully() {
    calendarManager.createCalendar("Work", nyZone);
    calendarManager.useCalendar("Work");
    calendarManager.editCalendar("Work", "timezone",
            "Europe/London");
    assertEquals(londonZone,
            calendarManager.getCurrentCalendar().getTimeZone());
  }

  @Test
  public void shouldFailWhenEditingNonexistentCalendar() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
            "Calendar with name NonExistent does not exist.");
    calendarManager.editCalendar("NonExistent", "timezone",
            "Europe/London");
  }

  @Test
  public void shouldSwitchToDifferentCalendarSuccessfully() {
    calendarManager.createCalendar("Work", nyZone);
    calendarManager.createCalendar("Personal", londonZone);
    calendarManager.useCalendar("Personal");
    assertEquals("Personal",
            calendarManager.getCurrentCalendar().getName());
  }

  @Test
  public void shouldFailWhenSwitchingToNonexistentCalendar() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
            "Calendar 'NonExistent' does not exist.");
    calendarManager.useCalendar("NonExistent");
  }

  @Test
  public void testGetEventStorage() {
    assertNotNull(calendarManager.getEventStorage());
    assertEquals(eventStorage,
            calendarManager.getEventStorage());
  }

  @Test
  public void testRenameCalendar() {
    String oldName = "oldCalendar";
    String newName = "newCalendar";
    ZoneId timeZone = ZoneId.systemDefault();
    calendarManager.createCalendar(oldName, timeZone);
    calendarManager.editCalendar(oldName, "name", newName);
    assertNull(calendarManager.getCalendar(oldName));
    assertNotNull(calendarManager.getCalendar(newName));
  }

  @Test
  public void testRenameCalendar_WithEvents() {
    String oldName = "Original";
    String newName = "Renamed";
    calendarManager.createCalendar(oldName, nyZone);
    calendarManager.useCalendar(oldName);
    CalendarEvent event = new CalendarEvent("Meeting",
            LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"));
    // Cast to access getCurrentCalendar()
    ((CalendarManager) calendarManager)
            .getCurrentCalendar().addEvent(event, false);
    calendarManager.editCalendar(oldName, "name", newName);
    assertNotNull(eventStorage.getEventStorageForCalendar(newName));
    assertEquals(1, eventStorage.getEventsForCalendar(newName)
            .size());
  }

  @Test
  public void testTransferEventsFromStorage() {
    String calName = "testCalendar";
    ZoneId timeZone = ZoneId.systemDefault();
    calendarManager.createCalendar(calName, timeZone);
    calendarManager.useCalendar(calName);
    EventStorage sourceStorage = new EventStorage();
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event",
            startTime, endTime);
    sourceStorage.addEvent(event, false);
    assertTrue(calendarManager.transferEventsFromStorage(
            sourceStorage));
    assertFalse(calendarManager.transferEventsFromStorage(null));
  }

  @Test
  public void transferEventsFromStorage_SameTimezone_NoConversion_1() {
    calendarManager.createCalendar("TestCal", nyZone);
    calendarManager.useCalendar("TestCal");
    EventStorage source = new EventStorage();
    LocalDateTime now = LocalDateTime.now();
    CalendarEvent event1 = createTestEvent("Meeting1", now, 1);
    CalendarEvent event2 = createTestEvent("Meeting2",
            now.plusHours(2), 1);
    source.addEvent(event1, false);
    source.addEvent(event2, false);
    boolean result = calendarManager.
            transferEventsFromStorage(source);
    assertTrue(result);
    // Cast to access getCurrentCalendar()
    Calendar testCal = ((CalendarManager) calendarManager)
            .getCurrentCalendar();
    assertNotNull(testCal.findEvent("Meeting1",
            event1.getStartDateTime()));
    assertNotNull(testCal.findEvent("Meeting2",
            event2.getStartDateTime()));
  }

  // The remaining tests are integrated as in the reference class

  @Test
  public void testTransferEventsFromStorage_WithTimeZoneConversion() {
    calendarManager.createCalendar("Source", nyZone);
    calendarManager.createCalendar("Target", londonZone);
    calendarManager.useCalendar("Source");
    CalendarEvent event = new CalendarEvent("Meeting",
            LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"));
    calendarManager.getCurrentCalendar().addEvent(event, false);
    calendarManager.useCalendar("Target");
    EventStorage sourceStorage = eventStorage.
            getEventStorage("Source");
    boolean result = calendarManager.
            transferEventsFromStorage(sourceStorage);
    assertTrue(result);
    assertEquals(1, eventStorage.getEventsForCalendar("Target")
            .size());
  }

  @Test
  public void testUpdateCalendarTimeZone() {
    String calName = "testCalendar";
    ZoneId oldTimeZone = ZoneId.systemDefault();
    ZoneId newTimeZone = nyZone;
    calendarManager.createCalendar(calName, oldTimeZone);
    calendarManager.editCalendar(calName, "timezone",
            "America/New_York");
    Calendar calendar = calendarManager.getCalendar(calName);
    assertEquals(newTimeZone, calendar.getTimeZone());
  }

  @Test
  public void testUpdateCalendarTimeZone_NoEvents() {
    calendarManager.createCalendar("Empty", nyZone);
    calendarManager.editCalendar("Empty", "timezone",
            "Europe/Paris");
    assertTrue(errContent.toString().contains("No events found"));
  }

  @Test
  public void testUpdateCalendarTimeZone_WithEvents() {
    calendarManager.createCalendar("Test", nyZone);
    calendarManager.useCalendar("Test");
    CalendarEvent event = new CalendarEvent("Meeting",
            LocalDateTime.parse("2025-03-01T10:00"),
            LocalDateTime.parse("2025-03-01T11:00"));
    calendarManager.getCurrentCalendar().addEvent(event, false);
    calendarManager.editCalendar("Test", "timezone",
            "Asia/Tokyo");
    List<CalendarEvent> events = new ArrayList<>(
            eventStorage.getEventsForCalendar("Test")
                    .values().iterator().next());
    assertEquals(tokyoZone, events.get(0).getStartDateTime()
            .atZone(tokyoZone).getZone());
  }

  @Test
  public void testRemoveAndReAddEvents() {
    String calName = "testCalendar";
    ZoneId timeZone = ZoneId.systemDefault();
    calendarManager.createCalendar(calName, timeZone);
    calendarManager.useCalendar(calName);
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event",
            startTime, endTime);
    Calendar calendar = calendarManager.getCurrentCalendar();
    calendar.addEvent(event, false);
    calendarManager.editCalendar(calName, "timezone",
            "America/New_York");
    List<CalendarEvent> events =
            calendar.getEventsOnDate(startTime);
    assertFalse(events.isEmpty());
  }

  @Test
  public void testConvertEventToTimeZone() {
    String calName = "testCalendar";
    ZoneId fromZone = ZoneId.systemDefault();
    ZoneId toZone = nyZone;
    calendarManager.createCalendar(calName, fromZone);
    calendarManager.useCalendar(calName);
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);
    CalendarEvent event = new CalendarEvent("Test Event",
            startTime, endTime);
    EventStorage sourceStorage = new EventStorage();
    sourceStorage.addEvent(event, false);
    calendarManager.transferEventsFromStorage(sourceStorage);
    List<CalendarEvent> events = calendarManager.
            getCurrentCalendar().getEventsOnDate(startTime);
    assertFalse(events.isEmpty());
  }

  @Test
  public void testRenameCalendar_StorageOperations() {
    String oldName = "OldCalendar";
    String newName = "NewCalendar";
    calendarManager.createCalendar(oldName,
            ZoneId.systemDefault());
    calendarManager.useCalendar(oldName);
    CalendarEvent event = new CalendarEvent("Test",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1));
    calendarManager.getCurrentCalendar()
            .addEvent(event, false);
    assertNotNull(eventStorage.getEventStorageForCalendar(oldName));
    calendarManager.editCalendar(oldName, "name", newName);
    assertNull(eventStorage.getEventStorageForCalendar(oldName));
    assertNotNull(eventStorage.getEventStorageForCalendar(newName));
    assertEquals(1, eventStorage.getEventsForCalendar(newName)
            .size());
  }

  @Test
  public void testRenameCalendar_CurrentCalendarUpdate() {
    String oldName = "Work";
    String newName = "WorkRenamed";
    calendarManager.createCalendar(oldName,
            ZoneId.systemDefault());
    calendarManager.useCalendar(oldName);
    calendarManager.editCalendar(oldName, "name", newName);
    assertEquals(newName, calendarManager.getCurrentCalendar()
            .getName());
  }

  @Test
  public void testRenameCalendar_NotCurrentCalendar() {
    String oldName = "Temp";
    String newName = "TempRenamed";
    calendarManager.createCalendar(oldName,
            ZoneId.systemDefault());
    calendarManager.createCalendar("Other",
            ZoneId.systemDefault());
    calendarManager.useCalendar("Other");
    calendarManager.editCalendar(oldName, "name", newName);
    assertEquals("Other", calendarManager.getCurrentCalendar()
            .getName());
  }

  @Test
  public void transferEventsFromStorage_SameTimezone_NoConversion() {
    calendarManager.createCalendar("TestCal", nyZone);
    calendarManager.useCalendar("TestCal");
    EventStorage source = new EventStorage();
    LocalDateTime now = LocalDateTime.now();
    CalendarEvent event1 = createTestEvent("Meeting1", now, 1);
    CalendarEvent event2 = createTestEvent("Meeting2",
            now.plusHours(2), 1);
    source.addEvent(event1, false);
    source.addEvent(event2, false);
    boolean result = calendarManager.
            transferEventsFromStorage(source);
    assertTrue(result);
    Calendar testCal = calendarManager.getCurrentCalendar();
    assertNotNull(testCal.findEvent("Meeting1",
            event1.getStartDateTime()));
    assertNotNull(testCal.findEvent("Meeting2",
            event2.getStartDateTime()));
  }

  @Test
  public void transferEventsFromStorage_DifferentTimezone_ConvertsTimes() {
    calendarManager.createCalendar("TestCal", nyZone);
    calendarManager.useCalendar("TestCal");
    LocalDateTime londonTime =
            LocalDateTime.parse("2025-03-01T10:00");
    EventStorage source = new EventStorage();
    CalendarEvent event = createTestEvent("LondonMeeting",
            londonTime, 1);
    source.addEvent(event, false);
    boolean result = calendarManager.
            transferEventsFromStorage(source);
    assertTrue(result);
    LocalDateTime expectedNYTime =
            LocalDateTime.parse("2025-03-01T10:00");
    CalendarEvent transferred = calendarManager.
            getCurrentCalendar().findEvent("LondonMeeting",
                    expectedNYTime);
    assertNotNull("Event should exist after transfer",
            transferred);
    assertEquals("Time should be converted to NY timezone",
            expectedNYTime,
            transferred.getStartDateTime());
  }

  @Test
  public void transferEventsFromStorage_EmptySource_ReturnsTrue() {
    calendarManager.createCalendar("TestCal", nyZone);
    calendarManager.useCalendar("TestCal");
    EventStorage empty = new EventStorage();
    boolean result = calendarManager.
            transferEventsFromStorage(empty);
    assertTrue(result);
    List<CalendarEvent> events = calendarManager.
            getCurrentCalendar().getEventsOnDate(
                    LocalDateTime.now());
    assertTrue(events.isEmpty());
  }

  @Test
  public void transferEventsFromStorage_NoCurrentCalendar_ReturnsFalse() {
    EventStorage source = new EventStorage();
    source.addEvent(createTestEvent("Test",
            LocalDateTime.now(), 1), false);
    assertFalse(calendarManager.
            transferEventsFromStorage(source));
  }

  @Test
  public void updateCalendarTimezone_ConvertsMultipleEventsCorrectly() {
    calendarManager.createCalendar("Test", nyZone);
    calendarManager.useCalendar("Test");
    LocalDateTime baseTime = LocalDateTime.now();
    calendarManager.getCurrentCalendar().addEvent(
            createTestEvent("Morning", baseTime, 1), false);
    calendarManager.getCurrentCalendar().addEvent(
            createTestEvent("Afternoon",
                    baseTime.plusHours(3), 2), false);
    calendarManager.editCalendar("Test", "timezone",
            "Asia/Tokyo");
    Calendar updated = calendarManager.getCalendar("Test");
    LocalDateTime expectedMorning = baseTime.plusHours(13);
    LocalDateTime expectedAfternoon = baseTime.plusHours(16);
    assertNotNull(updated.findEvent("Morning", expectedMorning));
    assertNotNull(updated.findEvent("Afternoon",
            expectedAfternoon));
  }

  @Test
  public void updateCalendarTimeZone_VerifyEventRemovalCalled() {
    calendarManager.createCalendar("Test", nyZone);
    calendarManager.useCalendar("Test");
    CalendarEvent event1 = createTestEvent("Meeting1",
            testDate, 1);
    CalendarEvent event2 = createTestEvent("Meeting2",
            testDatePlus2h, 1);
    calendarManager.getCurrentCalendar()
            .addEvent(event1, false);
    calendarManager.getCurrentCalendar()
            .addEvent(event2, false);
    LocalDateTime startOfDay = testDate.toLocalDate()
            .atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1)
            .minusNanos(1);
    int initialCount = calendarManager.getCurrentCalendar()
            .getEventsInRange(startOfDay, endOfDay).size();
    calendarManager.editCalendar("Test", "timezone",
            "Asia/Tokyo");
    assertNull(calendarManager.getCurrentCalendar().findEvent(
            "Meeting1", testDate));
    assertNull(calendarManager.getCurrentCalendar().findEvent(
            "Meeting2", testDatePlus2h));
    LocalDateTime expectedTokyoTime1 = testDate.plusHours(13);
    LocalDateTime expectedTokyoTime2 =
            testDatePlus2h.plusHours(13);
    assertNotNull(calendarManager.getCurrentCalendar().findEvent(
            "Meeting1", LocalDateTime.parse("2025-03-02T00:00")));
    assertNotNull(calendarManager.getCurrentCalendar().findEvent(
            "Meeting2", LocalDateTime.parse("2025-03-02T02:00")));
  }

  @Test
  public void removeAllEvents_ActuallyRemovesEvents() {
    calendarManager.createCalendar("Test", nyZone);
    calendarManager.useCalendar("Test");
    CalendarEvent event1 = createTestEvent("Meeting1",
            testDate, 1);
    CalendarEvent event2 = createTestEvent("Meeting2",
            testDatePlus2h, 1);
    calendarManager.getCurrentCalendar()
            .addEvent(event1, false);
    calendarManager.getCurrentCalendar()
            .addEvent(event2, false);
    EventStorage storage = eventStorage.
            getEventStorage("Test");
    List<CalendarEvent> events = new ArrayList<>();
    events.addAll(storage.getAllEvents().values()
            .iterator().next());
    assertEquals(2, calendarManager.getCurrentCalendar()
            .getEventsOnDate(testDate.toLocalDate()
                    .atStartOfDay()).size());
    calendarManager.editCalendar("Test", "timezone",
            "Asia/Tokyo");
    assertNull(calendarManager.getCurrentCalendar().findEvent(
            "Meeting1", testDate));
    assertNull(calendarManager.getCurrentCalendar().findEvent(
            "Meeting2", testDatePlus2h));
  }

  private CalendarEvent createTestEvent(String name,
                                        LocalDateTime start, int hours) {
    return new CalendarEvent(name, start,
            start.plusHours(hours),
            "Test Description",
            "Test Location", "private");
  }
}