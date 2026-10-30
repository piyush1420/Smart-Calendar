package view;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

import model.CalendarEvent;
import model.EventStorage;
import view.text.CalendarPrinter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the functionality of the CalendarPrinter class, ensuring
 * that events are correctly printed and displayed to the user.
 */
public class CalendarPrinterTest {

  private EventStorage eventStorage;
  private CalendarPrinter calendarPrinter;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @Before
  public void setUp() {
    eventStorage = new EventStorage();
    calendarPrinter = new CalendarPrinter(eventStorage);
    System.setOut(new PrintStream(outContent));
  }

  @Test
  public void testPrintEventsOnDate_NoEvents() {
    LocalDate date = LocalDate.of(2023, 10, 1);

    calendarPrinter.printEventsOnDate(date);

    String expectedOutput = "No events found on 2023-10-01";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testPrintEventsOnDate_WithEvents() {
    LocalDateTime startDateTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime endDateTime = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", startDateTime, endDateTime,
            "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false);

    LocalDate date = LocalDate.of(2023, 10, 1);

    outContent.reset();

    calendarPrinter.printEventsOnDate(date);

    String actualOutput = outContent.toString().replaceAll("\r\n", "\n");

    String expectedOutput = "Events on 2023-10-01:\n" +
            "- Meeting | 2023-10-01 10:00 - 2023-10-01 11:00 | Location: Office";
    assertTrue(actualOutput.contains(expectedOutput.replaceAll("\r\n", "\n")));
  }

  @Test
  public void testPrintEventsInRange_NoEvents() {
    LocalDateTime startDateTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime endDateTime = LocalDateTime.of(2023, 10, 1, 11, 0);

    calendarPrinter.printEventsInRange(startDateTime, endDateTime);

    String expectedOutput = "No events found in range.";
    assertTrue(outContent.toString().contains(expectedOutput));
  }

  @Test
  public void testPrintEventsInRange_WithEvents() {
    LocalDateTime startDateTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime endDateTime = LocalDateTime.of(2023, 10, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", startDateTime, endDateTime,
            "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false);

    outContent.reset();

    calendarPrinter.printEventsInRange(startDateTime, endDateTime);

    String actualOutput = outContent.toString().replaceAll("\r\n", "\n");

    String expectedOutput = "Events on 2023-10-01:\n" +
            "- Meeting | 2023-10-01 10:00 - 2023-10-01 11:00 | Location: Office";
    assertTrue(actualOutput.contains(expectedOutput.replaceAll("\r\n", "\n")));
  }

  @Test
  public void testPrintEventsOnFullDate() {
    CalendarEvent event1 = new CalendarEvent("Event 1", LocalDateTime.of(2025, 3, 7, 0, 0),
            LocalDateTime.of(2025, 3, 7, 8, 0));
    CalendarEvent event2 = new CalendarEvent("Event 2", LocalDateTime.of(2025, 3, 7, 8, 0),
            LocalDateTime.of(2025, 3, 7, 16, 0));
    CalendarEvent event3 = new CalendarEvent("Event 3", LocalDateTime.of(2025, 3, 7, 16, 0),
            LocalDateTime.of(2025, 3, 7, 23, 59));

    eventStorage.addEvent(event1, true);
    eventStorage.addEvent(event2, true);
    eventStorage.addEvent(event3, true);

    outContent.reset();

    calendarPrinter.printEventsOnDate(LocalDate.of(2025, 3, 7));

    String output = outContent.toString().replaceAll("\r\n", "\n");

    String expectedOutput = "Events on 2025-03-07:\n" +
            "- Event 1 | 2025-03-07 00:00 - 2025-03-07 08:00\n" +
            "- Event 2 | 2025-03-07 08:00 - 2025-03-07 16:00\n" +
            "- Event 3 | 2025-03-07 16:00 - 2025-03-07 23:59\n";
    expectedOutput = expectedOutput.replaceAll("\r\n", "\n");

    assertEquals(expectedOutput, output);
  }

  @Test
  public void testPrintEventsOnFullDateLocation() {
    CalendarEvent event1 = new CalendarEvent("Event 1", LocalDateTime.of(2025, 3, 7, 0, 0),
            LocalDateTime.of(2025, 3, 7, 8, 0), null, null, null);
    CalendarEvent event2 = new CalendarEvent("Event 2", LocalDateTime.of(2025, 3, 7, 8, 0),
            LocalDateTime.of(2025, 3, 7, 16, 0), null, "Room B", null);
    CalendarEvent event3 = new CalendarEvent("Event 3", LocalDateTime.of(2025, 3, 7, 16, 0),
            LocalDateTime.of(2025, 3, 7, 23, 59), null, "Room C", null);

    eventStorage.addEvent(event1, true);
    eventStorage.addEvent(event2, true);
    eventStorage.addEvent(event3, true);

    outContent.reset();

    calendarPrinter.printEventsOnDate(LocalDate.of(2025, 3, 7));

    String output = outContent.toString().replaceAll("\r\n", "\n");

    String expectedOutput = "Events on 2025-03-07:\n" +
            "- Event 1 | 2025-03-07 00:00 - 2025-03-07 08:00\n" +
            "- Event 2 | 2025-03-07 08:00 - 2025-03-07 16:00 | Location: Room B\n" +
            "- Event 3 | 2025-03-07 16:00 - 2025-03-07 23:59 | Location: Room C\n";
    expectedOutput = expectedOutput.replaceAll("\r\n", "\n");

    assertEquals(expectedOutput, output);
  }

  @Test
  public void testPrintEventsInRange_withEventsInRange() {
    CalendarEvent event1 = new CalendarEvent("Event 1", LocalDateTime.of(2025, 3, 7, 8, 0),
            LocalDateTime.of(2025, 3, 7, 10, 0), null, "Room A", null);
    CalendarEvent event2 = new CalendarEvent("Event 2", LocalDateTime.of(2025, 3, 7, 12, 0),
            LocalDateTime.of(2025, 3, 7, 14, 0), null, "Room B", null);
    CalendarEvent event3 = new CalendarEvent("Event 3", LocalDateTime.of(2025, 3, 7, 16, 0),
            LocalDateTime.of(2025, 3, 7, 18, 0), null, "Room C", null);

    eventStorage.addEvent(event1, true);
    eventStorage.addEvent(event2, true);
    eventStorage.addEvent(event3, true);

    outContent.reset();

    calendarPrinter.printEventsInRange(LocalDateTime.of(2025, 3, 7, 8, 0),
            LocalDateTime.of(2025, 3, 7, 18, 0));

    String output = outContent.toString().replaceAll("\r\n", "\n");

    String expectedOutput = "Events on 2025-03-07:\n" +
            "- Event 1 | 2025-03-07 08:00 - 2025-03-07 10:00 | Location: Room A\n" +
            "- Event 2 | 2025-03-07 12:00 - 2025-03-07 14:00 | Location: Room B\n" +
            "- Event 3 | 2025-03-07 16:00 - 2025-03-07 18:00 | Location: Room C\n";
    expectedOutput = expectedOutput.replaceAll("\r\n", "\n");

    assertEquals(expectedOutput, output);
  }

  @Test
  public void testPrintEventsInRange_TwoEventsNotInRange() {
    CalendarEvent event1 = new CalendarEvent("Event 1", LocalDateTime.of(2025, 3, 7, 8, 0),
            LocalDateTime.of(2025, 3, 7, 10, 0), null, "Room A", null);
    CalendarEvent event2 = new CalendarEvent("Event 2", LocalDateTime.of(2025, 3, 7, 12, 0),
            LocalDateTime.of(2025, 3, 7, 14, 0), null, "Room B", null);
    CalendarEvent event3 = new CalendarEvent("Event 3", LocalDateTime.of(2025, 3, 7, 16, 0),
            LocalDateTime.of(2025, 3, 7, 18, 0), null, "Room C", null);

    eventStorage.addEvent(event1, true);
    eventStorage.addEvent(event2, true);
    eventStorage.addEvent(event3, true);

    outContent.reset();

    calendarPrinter.printEventsInRange(LocalDateTime.of(2025, 3, 7, 9, 0),
            LocalDateTime.of(2025, 3, 7, 17, 0));

    String output = outContent.toString().replaceAll("\r\n", "\n");

    String expectedOutput = "Events on 2025-03-07:\n" +
            "- Event 2 | 2025-03-07 12:00 - 2025-03-07 14:00 | Location: Room B\n";
    expectedOutput = expectedOutput.replaceAll("\r\n", "\n");

    assertEquals(expectedOutput, output);
  }

  @Test
  public void testPrintEventsInRange_noEventsInRange() {
    calendarPrinter.printEventsInRange(LocalDateTime.of(2025, 3, 7, 20, 0),
            LocalDateTime.of(2025, 3, 7, 22, 0));

    String output = outContent.toString().replaceAll("\r\n", "\n");

    String expectedOutput = "No events found in range.\n";
    expectedOutput = expectedOutput.replaceAll("\r\n", "\n");

    assertEquals(expectedOutput, output);
  }

  @Test
  public void testShowStatusOnDateTime_UserIsBusy() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", dateTime, dateTime.plusHours(1),
            "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false);

    outContent.reset();

    calendarPrinter.showStatusOnDateTime(dateTime);

    String expectedOutput = "User is busy on " + dateTime + System.lineSeparator();
    assertEquals(expectedOutput, outContent.toString());
  }

  @Test
  public void testShowStatusOnDateTime_UserIsAvailable() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 10, 0);

    calendarPrinter.showStatusOnDateTime(dateTime);

    String expectedOutput = "User is available on " + dateTime + System.lineSeparator();
    assertEquals(expectedOutput, outContent.toString());
  }

  @Test
  public void testShowStatusOnDateTime_DateTimeEqualsStartTime() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", dateTime, dateTime.plusHours(1),
            "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false);

    outContent.reset();

    calendarPrinter.showStatusOnDateTime(dateTime);

    String expectedOutput = "User is busy on " + dateTime + System.lineSeparator();
    assertEquals(expectedOutput, outContent.toString());
  }

  @Test
  public void testShowStatusOnDateTime_DateTimeBetweenStartAndEndTime() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 10, 30);
    CalendarEvent event = new CalendarEvent("Meeting", LocalDateTime.of(2023, 10, 1, 10, 0),
            LocalDateTime.of(2023, 10, 1, 11, 0),
            "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false);
    outContent.reset();

    calendarPrinter.showStatusOnDateTime(dateTime);

    String expectedOutput = "User is busy on " + dateTime + System.lineSeparator();
    assertEquals(expectedOutput, outContent.toString());
  }

  @Test
  public void testShowStatusOnDateTime_DateTimeOutsideEventTimeRange() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 12, 0);
    CalendarEvent event = new CalendarEvent("Meeting", LocalDateTime.of(2023, 10, 1, 10, 0),
            LocalDateTime.of(2023, 10, 1, 11, 0),
            "Team Meeting", "Office", "Work");
    eventStorage.addEvent(event, false);

    outContent.reset();

    calendarPrinter.showStatusOnDateTime(dateTime);

    String expectedOutput = "User is available on " + dateTime + System.lineSeparator();
    assertEquals(expectedOutput, outContent.toString());
  }
}