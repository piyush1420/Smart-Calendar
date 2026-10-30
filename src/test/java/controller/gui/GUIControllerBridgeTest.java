package controller.gui;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import controller.CommandParser;
import model.Calendar;
import model.CalendarEvent;
import model.CalendarManager;
import model.MultiCalendarEventStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * GUIControllerBridgeTest.
 */
public class GUIControllerBridgeTest {
  // --- Fake Implementations ---

  private class FakeCommandParser extends CommandParser {
    private boolean returnValue = true;
    private String lastCommand = "";
    private Exception exceptionToThrow = null;
    private String simulatedOutput = "";

    public FakeCommandParser() {
      super(new FakeCalendarManager());
    }

    public void setReturnValue(boolean value) {
      returnValue = value;
    }

    public void setException(Exception e) {
      exceptionToThrow = e;
    }

    public void setSimulatedOutput(String output) {
      simulatedOutput = output;
    }

    public String getLastCommand() {
      return lastCommand;
    }

    @Override
    public boolean executeCommand(String command) {
      lastCommand = command;
      if (exceptionToThrow != null) {
        throw new RuntimeException(exceptionToThrow);
      }
      if (!simulatedOutput.isEmpty()) {
        System.out.print(simulatedOutput);
      }
      return returnValue;
    }
  }

  private class FakeCalendar extends Calendar {
    private List<CalendarEvent> events =
            new ArrayList<CalendarEvent>();

    public FakeCalendar(String name, ZoneId zoneId) {
      super(name, zoneId, new MultiCalendarEventStorage());
    }

    public void setEvents(List<CalendarEvent> evs) {
      events = evs;
    }

    @Override
    public List<CalendarEvent> getEventsOnDate(
            LocalDateTime dateTime) {
      return events;
    }
  }

  private class FakeCalendarManager extends CalendarManager {
    private Calendar currentCalendar = null;
    private List<String> calendarNames =
            new ArrayList<String>();

    public FakeCalendarManager() {
      super(new MultiCalendarEventStorage());
    }

    @Override
    public Calendar getCurrentCalendar() {
      return currentCalendar;
    }

    public void setCurrentCalendar(Calendar cal) {
      currentCalendar = cal;
    }

    @Override
    public List<String> getAllCalendarNames() {
      return calendarNames;
    }

    public void addCalendarName(String name) {
      calendarNames.add(name);
    }

    @Override
    public Calendar getCalendar(String calendarName) {
      if ("TestCalendar".equals(calendarName)) {
        return new Calendar("TestCalendar",
                ZoneId.of("UTC"),
                new MultiCalendarEventStorage());
      }
      return null;
    }
  }

  private FakeCommandParser parser;
  private FakeCalendarManager calendarManager;
  private GUIControllerBridge bridge;

  @Before
  public void setUp() {
    parser = new FakeCommandParser();
    calendarManager = new FakeCalendarManager();
    bridge = new GUIControllerBridge(parser, calendarManager);
  }

  @Test
  public void testCreateDefaultCalendar_normal() {
    bridge.createDefaultCalendar("America/New_York");
    assertEquals("use calendar --name Default",
            parser.getLastCommand());
  }

  @Test
  public void testCreateDefaultCalendar_exception() {
    parser.setException(new Exception("fail"));
    bridge.createDefaultCalendar("UTC");
    assertNotNull(parser);
  }

  @Test
  public void testCreateDefaultCalendar_noArg() {
    bridge.createDefaultCalendar();
    assertEquals("use calendar --name Default",
            parser.getLastCommand());
  }

  @Test
  public void testGetCurrentTimeZone() {
    String defaultTZ =
            TimeZone.getDefault().getID();
    assertEquals(defaultTZ, bridge.getCurrentTimeZone());
  }

  @Test
  public void testGetEventsForDay_noCalendar() {
    calendarManager.setCurrentCalendar(null);
    List<CalendarEvent> events =
            bridge.getEventsForDay(LocalDate.now());
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsForDay_withCalendar() {
    FakeCalendar fakeCalendar = new FakeCalendar("Test",
            ZoneId.systemDefault());
    CalendarEvent event = new CalendarEvent("Test Event",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1));
    List<CalendarEvent> eventList =
            new ArrayList<CalendarEvent>();
    eventList.add(event);
    fakeCalendar.setEvents(eventList);
    calendarManager.setCurrentCalendar(fakeCalendar);
    List<CalendarEvent> events =
            bridge.getEventsForDay(LocalDate.now());
    assertEquals(1, events.size());
    assertEquals("Test Event",
            events.get(0).getSubject());
  }

  @Test
  public void testGetEventsForDay_exception() {
    calendarManager = new FakeCalendarManager() {
      @Override
      public Calendar getCurrentCalendar() {
        throw new RuntimeException("fail");
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    List<CalendarEvent> events =
            bridge.getEventsForDay(LocalDate.now());
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsOutputForDay_success()
          throws Exception {
    parser = new FakeCommandParser() {
      @Override
      public boolean executeCommand(String command) {
        System.out.print("Output Message");
        return true;
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    String output = bridge.getEventsOutputForDay(
            LocalDate.of(2023, 10, 1));
    assertEquals("Output Message", output);
  }

  @Test
  public void testGetEventsOutputForDay_failureEmptyOutput() {
    parser.setReturnValue(false);
    String output = bridge.getEventsOutputForDay(LocalDate.of(2023, 10, 1));
    assertEquals("Error: Command execution failed", output);
  }

  @Test
  public void testGetEventsOutputForDay_failureWithOutput()
          throws Exception {
    parser = new FakeCommandParser() {
      @Override
      public boolean executeCommand(String command) {
        System.out.print("Error: Something went wrong");
        return false;
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    String output = bridge.getEventsOutputForDay(
            LocalDate.of(2023, 10, 1));
    assertEquals("Error: Something went wrong", output);
  }

  @Test
  public void testGetAllCalendarNames() {
    calendarManager.addCalendarName("Cal1");
    calendarManager.addCalendarName("Cal2");
    List<String> names = bridge.getAllCalendarNames();
    assertEquals(2, names.size());
    assertTrue(names.contains("Cal1"));
    assertTrue(names.contains("Cal2"));
  }

  @Test
  public void testCreateCalendar_normal() {
    bridge.createCalendar("MyCal", "UTC");
    assertEquals("use calendar --name MyCal",
            parser.getLastCommand());
  }

  @Test
  public void testCreateCalendar_exception() {
    parser.setException(new Exception("fail"));
    bridge.createCalendar("MyCal", "UTC");
    assertNotNull(parser);
  }

  @Test
  public void testGetCalendarTimeZone_nullOrEmpty() {
    String defaultTZ =
            TimeZone.getDefault().getID();
    assertEquals(defaultTZ,
            bridge.getCalendarTimeZone(null));
    assertEquals(defaultTZ,
            bridge.getCalendarTimeZone("  "));
  }

  @Test
  public void testGetCalendarTimeZone_valid() {
    final Calendar cal = new Calendar("TestCalendar",
            ZoneId.of("UTC"), new MultiCalendarEventStorage());
    bridge = new GUIControllerBridge(parser,
            calendarManager) {
      @Override
      public CalendarManager getCalendarManager() {
        return new FakeCalendarManager() {
          @Override
          public Calendar getCalendar(String calendarName) {
            if ("TestCalendar".equals(calendarName)) {
              return cal;
            }
            return null;
          }
        };
      }
    };
    assertEquals("UTC",
            bridge.getCalendarTimeZone("TestCalendar"));
  }

  @Test
  public void testGetCalendarTimeZone_exception() {
    calendarManager = new FakeCalendarManager() {
      @Override
      public Calendar getCalendar(String calendarName) {
        throw new RuntimeException("fail");
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    String defaultTZ =
            TimeZone.getDefault().getID();
    assertEquals(defaultTZ,
            bridge.getCalendarTimeZone("AnyCalendar"));
  }

  @Test
  public void testGetParser() {
    assertEquals(parser, bridge.getParser());
  }

  @Test
  public void testGetCalendarManager() {
    assertEquals(calendarManager,
            bridge.getCalendarManager());
  }

  @Test
  public void testExecuteCommandWithConsoleCapture_success()
          throws Exception {
    parser = new FakeCommandParser() {
      @Override
      public boolean executeCommand(String command) {
        System.out.print("Captured Output");
        return true;
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    String output = bridge.executeCommandWithConsoleCapture(
            "test command");
    assertEquals("Captured Output", output);
  }

  @Test
  public void testExecuteCommandWithConsoleCapture_failureEmptyOutput() {
    parser.setReturnValue(false);
    try {
      bridge.executeCommandWithConsoleCapture("test command");
      fail("Expected Exception");
    } catch (Exception e) {
      assertTrue(e.getMessage()
              .contains("Command execution failed"));
    }
  }

  @Test
  public void testExecuteCommandWithConsoleCapture_failureWithOutput()
          throws Exception {
    parser = new FakeCommandParser() {
      @Override
      public boolean executeCommand(String command) {
        System.out.print("Error: Something went wrong");
        return false;
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    String output = bridge.executeCommandWithConsoleCapture(
            "test command");
    assertEquals("Error: Something went wrong", output);
  }

  @Test
  public void testCreateDefaultCalendar_printStackTrace() {
    PrintStream originalErr = System.err;
    ByteArrayOutputStream errContent =
            new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    parser.setException(new Exception("Default Calendar Error"));
    bridge.createDefaultCalendar("TestTZ");
    System.setErr(originalErr);
    String errOutput = errContent.toString();
    assertTrue(errOutput.contains("Default Calendar Error"));
  }

  @Test
  public void testCreateCalendar_printStackTrace() {
    PrintStream originalErr = System.err;
    ByteArrayOutputStream errContent =
            new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    parser.setException(new Exception("Create Calendar Error"));
    bridge.createCalendar("MyCal", "UTC");
    System.setErr(originalErr);
    String errOutput = errContent.toString();
    assertTrue(errOutput.contains("Create Calendar Error"));
  }

  @Test
  public void testGetEventsForDay_errorPrinting() {
    calendarManager = new FakeCalendarManager() {
      @Override
      public Calendar getCurrentCalendar() {
        throw new RuntimeException("Events Error");
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    PrintStream originalErr = System.err;
    ByteArrayOutputStream errContent =
            new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    List<CalendarEvent> events =
            bridge.getEventsForDay(LocalDate.now());
    System.setErr(originalErr);
    String errOutput = errContent.toString();
    assertTrue(errOutput.contains("Error getting events: " +
            "Events Error"));
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsOutputForDay_exceptionBranch() {
    parser.setException(new Exception("Parser Failure"));
    String output = bridge.getEventsOutputForDay(
            LocalDate.of(2023, 10, 1));
    assertTrue(output.contains("Error: java.lang.Exception: " +
            "Parser Failure"));
  }

  @Test
  public void testGetCalendarTimeZone_errorPrinting() {
    calendarManager = new FakeCalendarManager() {
      @Override
      public Calendar getCalendar(String calendarName) {
        throw new RuntimeException("Calendar Error");
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    PrintStream originalErr = System.err;
    ByteArrayOutputStream errContent =
            new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    String tz = bridge.getCalendarTimeZone("AnyCalendar");
    System.setErr(originalErr);
    String errOutput = errContent.toString();
    assertTrue(errOutput.contains("Calendar Error"));
    String defaultTZ =
            TimeZone.getDefault().getID();
    assertEquals(defaultTZ, tz);
  }

  @Test
  public void testExecuteCommandWithConsoleCapture_restoresSystemOut()
          throws Exception {
    PrintStream originalOut = System.out;
    parser = new FakeCommandParser() {
      @Override
      public boolean executeCommand(String command) {
        System.out.print("Some Output");
        return true;
      }
    };
    bridge = new GUIControllerBridge(parser,
            calendarManager);
    String output = bridge.executeCommandWithConsoleCapture(
            "dummy");
    assertEquals(originalOut, System.out);
    assertEquals("Some Output", output);
  }
}