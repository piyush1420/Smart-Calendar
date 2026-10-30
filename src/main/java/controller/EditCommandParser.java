package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import model.CalendarEvent;
import model.EditEvent;
import model.EventStorage;

/**
 * Parses and executes commands for editing calendar events.
 */
public class EditCommandParser implements CommandExecutor {
  private final EventStorage eventStorage;
  private static final String dateTimePattern = "yyyy-MM-dd'T'HH:mm";
  private static final DateTimeFormatter dateTimeFormatter =
          DateTimeFormatter.ofPattern(dateTimePattern);

  private static final Pattern singleEventPattern = Pattern.compile(
          "edit\\s+event\\s+(\\w+)\\s+(.+?)\\s+from\\s+(.+?)\\s+to.+?with\\s+(.+)",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern eventsFromPattern = Pattern.compile(
          "edit\\s+events\\s+(\\w+)\\s+(.+?)\\s+from\\s+(.+?)\\s+with\\s+(.+)",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern allEventsPattern = Pattern.compile(
          "edit\\s+events\\s+(\\w+)\\s+([^`\\s]+(?:\\s+[^`\\s]+)*)\\s+(`([^`]+)`|[^`]+)",
          Pattern.CASE_INSENSITIVE);

  public EditCommandParser(EventStorage eventStorage) {
    this.eventStorage = eventStorage;
  }

  @Override
  public boolean executeCommand(String command) {
    if (command == null || !command.toLowerCase().startsWith("edit")) {
      System.out.println("Invalid command format. Must start with 'edit'");
      return false;
    }

    Matcher singleEventMatcher = singleEventPattern.matcher(command);
    if (singleEventMatcher.find()) {
      return handleSingleEventEdit(
              singleEventMatcher.group(1),
              singleEventMatcher.group(2),
              singleEventMatcher.group(3),
              singleEventMatcher.group(4));
    }

    Matcher eventsFromMatcher = eventsFromPattern.matcher(command);
    if (eventsFromMatcher.find()) {
      return handleMultipleEventsFromTime(
              eventsFromMatcher.group(1),
              eventsFromMatcher.group(2),
              eventsFromMatcher.group(3),
              eventsFromMatcher.group(4));
    }

    Matcher allEventsMatcher = allEventsPattern.matcher(command);
    if (allEventsMatcher.find()) {
      return handleAllEventsByName(
              allEventsMatcher.group(1),
              allEventsMatcher.group(2),
              allEventsMatcher.group(3));
    }

    return fallbackCommandParsing(command);
  }

  private boolean handleSingleEventEdit(String property, String eventName,
                                        String dateTimeString, String newValue) {
    try {
      validateParameters(property, eventName, newValue);
    } catch (IllegalArgumentException ex) {
      System.out.println("Error : " + ex.getMessage());
      return false;
    }
    LocalDateTime startDateTime = parseDateTime(dateTimeString);
    if (startDateTime == null) {
      return false;
    }

    CalendarEvent event = eventStorage.findEvent(eventName.trim(), startDateTime);
    if (event == null) {
      System.out.println("Event not found: " + eventName + " at " + dateTimeString);
      return false;
    }

    return new EditEvent(event, eventStorage).executeEdit(property, stripBackticks(newValue));
  }

  private boolean handleMultipleEventsFromTime(String property, String eventName,
                                               String dateTimeString, String newValue) {
    try {
      validateParameters(property, eventName, newValue);
    } catch (IllegalArgumentException ex) {
      System.out.println("Error : " + ex.getMessage());
      return false;
    }
    LocalDateTime startDateTime = parseDateTime(dateTimeString);
    if (startDateTime == null) {
      return false;
    }

    List<CalendarEvent> matchingEvents = eventStorage.getAllEvents().values().stream()
            .flatMap(List::stream)
            .filter(e -> e.getSubject().equalsIgnoreCase(eventName.trim()))
            .filter(e -> !e.getStartDateTime().isBefore(startDateTime))
            .collect(Collectors.toList());

    if (matchingEvents.isEmpty()) {
      System.out.println("No events found matching: " + eventName
              + " starting at or after " + dateTimeString);
      return false;
    }

    return EditEvent.executeMultipleEdits(
            matchingEvents, property, stripBackticks(newValue), eventStorage);
  }

  private boolean handleAllEventsByName(String property, String eventName, String newValue) {
    validateParameters(property, eventName, newValue);

    List<CalendarEvent> matchingEvents = eventStorage.getAllEvents().values().stream()
            .flatMap(List::stream)
            .filter(e -> e.getSubject().equalsIgnoreCase(eventName.trim()))
            .collect(Collectors.toList());

    if (matchingEvents.isEmpty()) {
      System.out.println("No events found with name: " + eventName);
      return false;
    }

    return EditEvent.executeMultipleEdits(
            matchingEvents, property, stripBackticks(newValue), eventStorage);
  }

  private boolean fallbackCommandParsing(String command) {
    List<String> partsList = tokenizeCommand(command);
    String[] parts = partsList.toArray(new String[0]);

    if (parts.length < 4) {
      System.out.println("Invalid command format.");
      return false;
    }

    String editType = parts[1].toLowerCase();
    String property = parts[2];

    if (property == null || property.isEmpty()) {
      throw new IllegalArgumentException("Property to edit must be specified.");
    }

    StringBuilder eventNameBuilder = new StringBuilder();
    int i = 3;
    while (i < parts.length && !parts[i].equalsIgnoreCase("from")
            && !parts[i].equalsIgnoreCase("with")) {
      eventNameBuilder.append(parts[i]).append(" ");
      i++;
    }
    String eventName = eventNameBuilder.toString().trim();

    if (eventName.isEmpty()) {
      throw new IllegalArgumentException("Event name must be specified.");
    }

    if (editType.equals("event")) {
      if (!command.contains("from") || !command.contains("to") || !command.contains("with")) {
        System.out.println("Invalid format for editing a single event.");
        return false;
      }

      String dateTimeString = extractBetween(command, "from", "to").trim();
      String newValue = extractAfter(command, "with").trim();

      return handleSingleEventEdit(property, eventName, dateTimeString, newValue);

    } else if (editType.equals("events")) {
      if (command.contains("from") && command.contains("with")) {
        String dateTimeString = extractBetween(command, "from", "with").trim();
        String newValue = extractAfter(command, "with").trim();

        return handleMultipleEventsFromTime(property, eventName, dateTimeString, newValue);
      } else if (command.contains("`")) {
        String newValue = extractBacktickedValue(command, i, parts);
        return handleAllEventsByName(property, eventName, newValue);
      } else {
        throw new IllegalArgumentException("Invalid format for editing multiple events.");
      }

    } else {
      System.out.println("Invalid edit type: " + editType);
      return false;
    }
  }

  private List<String> tokenizeCommand(String input) {
    List<String> tokens = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inBackticks = false;

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (c == '`') {
        current.append(c);
        inBackticks = !inBackticks;
      } else if (c == ' ' && !inBackticks) {
        if (current.length() > 0) {
          tokens.add(current.toString());
          current.setLength(0);
        }
      } else {
        current.append(c);
      }
    }

    if (current.length() > 0) {
      tokens.add(current.toString());
    }

    return tokens;
  }

  private String extractBetween(String command, String start, String end) {
    int startIndex = command.toLowerCase().indexOf(start.toLowerCase());
    if (startIndex == -1) {
      throw new IllegalArgumentException("Missing keyword '" + start + "' in command.");
    }
    startIndex += start.length();

    int endIndex = command.toLowerCase().indexOf(end.toLowerCase(), startIndex);
    if (endIndex == -1) {
      throw new IllegalArgumentException("Missing keyword '" + end + "' in command.");
    }

    return command.substring(startIndex, endIndex).trim();
  }

  private String extractAfter(String command, String keyword) {
    int index = command.toLowerCase().indexOf(keyword.toLowerCase());
    if (index == -1) {
      throw new IllegalArgumentException("Missing keyword '" + keyword + "' in command.");
    }
    return command.substring(index + keyword.length()).trim();
  }

  private String extractBacktickedValue(String command, int startIndex, String[] parts) {
    int openBacktick = command.indexOf('`', startIndex > 0 ? startIndex : 0);
    if (openBacktick == -1) {
      throw new IllegalArgumentException("New value must be enclosed in backticks (`).");
    }

    int closeBacktick = command.indexOf('`', openBacktick + 1);
    if (closeBacktick == -1) {
      throw new IllegalArgumentException("Missing closing backtick (`) for new value.");
    }

    return command.substring(openBacktick + 1, closeBacktick).trim();
  }

  private LocalDateTime parseDateTime(String dateTimeString) {
    try {
      return LocalDateTime.parse(dateTimeString.trim(), dateTimeFormatter);
    } catch (RuntimeException ex) {
      System.out.println("Invalid date format: " + dateTimeString);
      return null;
    }
  }

  private void validateParameters(String property, String eventName, String newValue) {
    if (property == null || property.isEmpty()) {
      throw new IllegalArgumentException("Property to edit must be specified.");
    }

    if (eventName == null || eventName.trim().isEmpty()) {
      throw new IllegalArgumentException("Event name must be specified.");
    }

    if (newValue == null || newValue.isEmpty()) {
      throw new IllegalArgumentException("New property value must be specified.");
    }

    if ("start".equalsIgnoreCase(property)) {
      try {
        parseDateTime(newValue);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid date format for new start time.");
      }
    }

    try {
      EditEvent.getEventPropertyFromString(property);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid property name: " + property);
    }
  }

  private String stripBackticks(String input) {
    if (input == null) {
      return null;
    }
    if (input.startsWith("`") && input.endsWith("`") && input.length() >= 2) {
      return input.substring(1, input.length() - 1).trim();
    }
    return input.trim();
  }
}