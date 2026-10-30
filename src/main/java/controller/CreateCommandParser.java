package controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.CalendarEvent;
import model.EventStorage;
import model.RecurringEventStorage;

/**
 * Parses and executes commands for creating calendar events. Supports creating
 * single events, recurring events, all-day events, and recurring all-day events.
 */
public class CreateCommandParser implements CommandExecutor {
  private EventStorage eventStorage;
  private String eventName;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String location;
  private String description;
  private String eventType;
  private List<DayOfWeek> repeatDays;
  private String lowerCaseCommand;
  private int repeatTimes;

  /**
   * Constructs a CreateCommandParser with the given EventStorage.
   *
   * @param eventStorage the EventStorage to be used for storing events
   */
  public CreateCommandParser(EventStorage eventStorage) {
    this.eventStorage = eventStorage;
  }

  /**
   * Executes the given command by parsing it and creating the appropriate event.
   *
   * @param command the command string to be executed
   * @return true if the command was executed successfully, false otherwise
   */
  @Override
  public boolean executeCommand(String command) {
    lowerCaseCommand = command.toLowerCase();
    if (!lowerCaseCommand.startsWith("create event")) {
      System.out.println("Error: Command must start with 'create event'.");
      return false;
    }
    if (isSimpleEvent(lowerCaseCommand)) {
      return handleSimpleEvent(command);
    }
    if (isRecurringByTimes(lowerCaseCommand)) {
      return handleRecurringEventByTimes(command);
    }
    if (isRecurringByUntilDate(lowerCaseCommand)) {
      return handleRecurringEventByUntilDate(command);
    }
    if (isAllDayEvent(lowerCaseCommand)) {
      return handleAllDayEvent(command);
    }
    if (isRecurringAllDayByTimes(lowerCaseCommand)) {
      return handleRecurringAllDayEventByTimes(command);
    }
    if (isRecurringAllDayByUntilDate(lowerCaseCommand)) {
      return handleRecurringAllDayEventUntilDate(command);
    }

    System.out.println("Error: Invalid event creation format.");
    return false;
  }

  /**
   * Checks if the command is for creating a simple event.
   *
   * @param command the command string to check
   * @return true if the command is for a simple event, false otherwise
   */
  private boolean isSimpleEvent(String command) {
    return !command.contains("repeats") && command.contains("from");
  }

  /**
   * Checks if the command is for creating a recurring event with a fixed number
   * of repetitions.
   *
   * @param command the command string to check
   * @return true if the command is for a recurring event by times, false otherwise
   */
  private boolean isRecurringByTimes(String command) {
    return !command.contains("until") && command.contains("from") && command.contains("for");
  }

  /**
   * Checks if the command is for creating a recurring event until a specific date.
   *
   * @param command the command string to check
   * @return true if the command is for a recurring event by until date, false otherwise
   */
  private boolean isRecurringByUntilDate(String command) {
    return !command.contains("times") && command.contains("from") && command.contains("until");
  }

  /**
   * Checks if the command is for creating an all-day event.
   *
   * @param command the command string to check
   * @return true if the command is for an all-day event, false otherwise
   */
  private boolean isAllDayEvent(String command) {
    return !command.contains("repeats") && command.contains("on");
  }

  /**
   * Checks if the command is for creating a recurring all-day event with a fixed
   * number of repetitions.
   *
   * @param command the command string to check
   * @return true if the command is for a recurring all-day event by times, false otherwise
   */
  private boolean isRecurringAllDayByTimes(String command) {
    return !command.contains("until") && command.contains("on") && command.contains("for");
  }

  /**
   * Checks if the command is for creating a recurring all-day event until a
   * specific date.
   *
   * @param command the command string to check
   * @return true if the command is for a recurring all-day event by until date, false otherwise
   */
  private boolean isRecurringAllDayByUntilDate(String command) {
    return !command.contains("times") && command.contains("on") && command.contains("until");
  }

  /**
   * Validates the command by checking for required fields and extracting values.
   *
   * @param command the command string to validate
   * @return true if the command is valid, false otherwise
   */
  private boolean validateCommand(String command) {
    if (!eventNameCheckHelper(command)) {
      return false;
    }
    if (!extractAndValidateEventName(command)) {
      return false;
    }
    return extractAndValidateOptionalFields(command);
  }

  /**
   * Handles the creation of a simple event.
   *
   * @param command the command string to process
   * @return true if the event was created successfully, false otherwise
   */
  private boolean handleSimpleEvent(String command) {
    final Pattern commandPattern = Pattern.compile(
            "(?i)create event(?" +
                    ": --autoDecline)?\\s+(.+?)\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:" +
                    "\\d{2})\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})(?:\\s+location\\s+"
                    +
                    "(.+?))?(?:\\s+description\\s+(.+?))?(?:\\s+type\\s+(public|private))?$"
    );

    if (!validateBasics(command, commandPattern)) {
      return false;
    }

    if (!extractAndValidateBothDateAndTime(command)) {
      return false;
    }

    if (!checkFullCommandSyntax(command, commandPattern)) {
      return false;
    }


    boolean autoDecline = command.contains("--autoDecline");
    CalendarEvent event = new CalendarEvent(eventName, startDate, endDate, description,
            location, eventType);
    eventStorage.addEvent(event, autoDecline);
    return true;
  }

  /**
   * Validates basic command requirements and syntax.
   *
   * @param command        the command string to validate
   * @param commandPattern the pattern to match against
   * @return true if validation passes, false otherwise
   */
  private boolean validateBasics(String command, Pattern commandPattern) {
    return validateCommand(command);
  }

  /**
   * Checks if the event name is present in the command.
   *
   * @param command the command string to check
   * @return true if the event name is present, false otherwise
   */
  private boolean eventNameCheckHelper(String command) {
    if (!command.matches("(?i).*create event(?: --autoDecline)?\\s+[^\\s]+.*")) {
      System.out.println("Error: Event name is required.");
      return false;
    }
    return true;
  }

  /**
   * Extracts and validates the event name from the command.
   *
   * @param command the command string to process
   * @return true if the event name is valid, false otherwise
   */
  private boolean extractAndValidateEventName(String command) {
    eventName = extractEventName(command);
    if (eventName == null || eventName.isEmpty()) {
      System.out.println("Error: Event name is required.");
      return false;
    }
    if (eventName.equals("--autoDecline")) {
      System.out.println("Error: Event name is required.");
      return false;
    }
    return true;
  }

  /**
   * Extracts and validates the start and end dates from the command.
   *
   * @param command the command string to process
   * @return true if the dates are valid, false otherwise
   */
  private boolean extractAndValidateBothDateAndTime(String command) {
    startDate = extractDateTime(command, "from");
    endDate = extractDateTime(command, "to");
    // Check if either date is null
    if (startDate == null || endDate == null) {
      return false; // Exit early if dates are invalid
    }
    if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
      System.out.println("Error: End date must be after start date.");
      return false;
    }
    return true;
  }

  /**
   * Extracts and validates optional fields (location, description, type) from
   * the command.
   *
   * @param command the command string to process
   * @return true if the optional fields are valid, false otherwise
   */
  private boolean extractAndValidateOptionalFields(String command) {
    location = extractField(command, "location");
    description = extractField(command, "description");
    eventType = extractField(command, "type");
    if (lowerCaseCommand.contains("location") && (location == null || location.isEmpty())) {
      System.out.println("Error: Missing value for location.");
      return false;
    }
    if (lowerCaseCommand.contains("description") && (description == null
            || description.isEmpty())) {
      System.out.println("Error: Missing value for description.");
      return false;
    }
    if (lowerCaseCommand.contains("type")) {
      if (eventType == null || eventType.isEmpty()) {
        System.out.println("Error: Missing value for type.");
        return false;
      }
      if (!eventType.toLowerCase().equalsIgnoreCase("public")
              && !eventType.toLowerCase().equalsIgnoreCase("private")) {
        System.out.println("Error: Type must be either 'public' or 'private'.");
        return false;
      }
    }
    return true;
  }

  /**
   * Extracts and validates the repeat days from the command.
   *
   * @param command the command string to process
   * @return true if the repeat days are valid, false otherwise
   */
  private boolean extractAndValidateRepeatDays(String command) {
    String repeatDaysStr = extractRepeatDays(command);
    if (repeatDaysStr == null || repeatDaysStr.isEmpty()) {
      System.out.println("Error: Either repeat day is invalid must be [MRUWFST] or " +
              "Repeat days must be specified " +
              "between 'repeats' and 'until or for'.");
      return false;
    }
    repeatDays = extractWeekdays(repeatDaysStr);
    if (repeatDays == null || repeatDays.isEmpty()) {
      System.out.println("Error: At least one valid repeat day must be specified.");
      return false;
    }
    return true;
  }

  /**
   * Extracts the repeat days from the command.
   *
   * @param command the command string to process
   * @return the repeat days as a string, or null if not found
   */
  private static String extractRepeatDays(String command) {
    Matcher matcher = Pattern.
            compile("(?i)repeats\\s+([MRUWFST]+)\\s+(?:until|for)").matcher(command);
    if (matcher.find()) {
      return matcher.group(1).trim();
    }
    return null;
  }

  /**
   * Converts a string of weekday characters to a list of DayOfWeek objects.
   *
   * @param weekdaysStr the string of weekday characters
   * @return a list of DayOfWeek objects, or empty list invalid characters are found
   */
  private List<DayOfWeek> extractWeekdays(String weekdaysStr) {
    List<DayOfWeek> weekdays = new ArrayList<>();
    for (char dayChar : weekdaysStr.toUpperCase().toCharArray()) {
      DayOfWeek day = convertCharToDayOfWeek(dayChar);
      if (day != null) {
        weekdays.add(day);
      }
    }
    return weekdays;
  }

  /**
   * Converts a character to a DayOfWeek object.
   *
   * @param dayChar the character representing the day
   * @return the corresponding DayOfWeek object, or null if invalid
   */
  private DayOfWeek convertCharToDayOfWeek(char dayChar) {
    switch (dayChar) {
      case 'M':
        return DayOfWeek.MONDAY;
      case 'T':
        return DayOfWeek.TUESDAY;
      case 'W':
        return DayOfWeek.WEDNESDAY;
      case 'R':
        return DayOfWeek.THURSDAY;
      case 'F':
        return DayOfWeek.FRIDAY;
      case 'S':
        return DayOfWeek.SATURDAY;
      case 'U':
        return DayOfWeek.SUNDAY;
      default:
        return null;
    }
  }

  /**
   * Extracts a date from the command.
   *
   * @param command the command string to process
   * @param keyword the keyword to search for (e.g., "on", "until")
   * @return the extracted LocalDateTime, or null if invalid
   */
  private LocalDateTime extractDate(String command, String keyword) {
    Matcher matcher = Pattern.compile("(?i)" + keyword +
            "\\s+(\\d{4}-\\d{2}-\\d{2})").matcher(command);
    if (matcher.find()) {
      return parseDate(matcher.group(1));
    }
    System.out.println("Error: Missing or invalid " + keyword + " date.");
    return null;
  }


  /**
   * Parses a date string into a LocalDateTime object.
   *
   * @param dateString the date string to parse
   * @return the parsed LocalDateTime, or null if invalid
   */
  private LocalDateTime parseDate(String dateString) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      return LocalDate.parse(dateString, formatter).atStartOfDay();
    } catch (Exception e) {
      System.out.println("Error: Invalid date format. Use 'yyyy-MM-dd'. Error in: " + dateString);
      return null;
    }
  }

  /**
   * Extracts the number of times to repeat from the command.
   *
   * @param command the command string to process
   * @return the number of times as a string, or null if not found
   */
  private static String extractRepeatTimes(String command) {
    Matcher matcher = Pattern.compile("(?i)for\\s+(\\d+)\\s+times").matcher(command);
    if (matcher.find()) {
      return matcher.group(1).trim();
    }
    return null;
  }

  /**
   * Extracts the event name from the command.
   *
   * @param command the command string to process
   * @return the event name, or null if not found
   */
  private static String extractEventName(String command) {
    Matcher matcher = Pattern.
            compile("(?i)create event(?: --autoDecline)?\\s+(.+?)\\s+(?:from|on)").matcher(command);
    if (matcher.find()) {
      return matcher.group(1).trim();
    }
    return null;
  }

  /**
   * Extracts a date and time from the command.
   *
   * @param command the command string to process
   * @param keyword the keyword to search for (e.g., "from", "to")
   * @return the extracted LocalDateTime, or null if invalid
   */
  private static LocalDateTime extractDateTime(String command, String keyword) {
    Matcher matcher = Pattern.compile("(?i)" + keyword +
            "\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})").matcher(command);
    if (matcher.find()) {
      return parseDateTime(matcher.group(1));
    }
    System.out.println("Error: Missing or invalid " + keyword + " date.");
    return null;
  }

  /**
   * Extracts a field (e.g., location, description, type) from the command.
   *
   * @param command   the command string to process
   * @param fieldName the name of the field to extract
   * @return the extracted field value, or null if not found
   */
  private static String extractField(String command, String fieldName) {
    Matcher matcher = Pattern.compile("(?i)" + fieldName + "\\s+([^\\s].+?)(?=\\s+(?i)(?:" +
            "location|description|type)|$)").matcher(command);
    if (matcher.find()) {
      String value = matcher.group(1).trim();
      String firstWord = value.trim().split("\\s+")[0].toLowerCase();
      if (firstWord.equalsIgnoreCase("location") || firstWord.equalsIgnoreCase(
              "description") || firstWord.equalsIgnoreCase("type")) {
        return null;
      }
      return value;
    }
    return null;
  }

  /**
   * Validates the full command syntax against a given pattern.
   *
   * @param command        the command string to validate
   * @param commandPattern the pattern to match against
   * @return true if the command matches the pattern, false otherwise
   */
  private boolean checkFullCommandSyntax(String command, Pattern commandPattern) {
    Matcher matcher = commandPattern.matcher(command);
    if (!matcher.matches()) {
      System.out.println("Error: Invalid command syntax.");
      return false;
    }
    return true;
  }

  /**
   * Parses a date and time string into a LocalDateTime object.
   *
   * @param dateTimeString the date and time string to parse
   * @return the parsed LocalDateTime, or null if invalid
   */
  private static LocalDateTime parseDateTime(String dateTimeString) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
      return LocalDateTime.parse(dateTimeString, formatter);
    } catch (Exception e) {
      System.out.println("Error: Invalid date format. Use 'yyyy-MM-ddTHH:mm'. Error in: " +
              dateTimeString);
      return null;
    }
  }

  /**
   * Handles the creation of a recurring event with a fixed number of repetitions.
   *
   * @param command the command string to process
   * @return true if the event was created successfully, false otherwise
   */
  private boolean handleRecurringEventByTimes(String command) {
    final Pattern commandPattern = Pattern.compile(
            "(?i)create event(?: --autoDecline)?\\s+(.+?)\\s+from\\s+" +
                    "(\\d{4}-\\d{2}-\\d{2}T" +
                    "\\d{2}:\\d{2})\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})" +
                    "\\s+repeats\\s+([A-Z]+)\\s+for\\s+(\\d+)\\s+times(?:\\s+" +
                    "location\\s+(.+?))?(?:\\s+description\\s+(.+?))?(?:\\s+type\\s+" +
                    "(public|private))?$"
    );

    if (!validateBasics(command, commandPattern)) {
      return false;
    }

    if (!validateRecurringEventFields(command)) {
      return false;
    }

    if (!extractAndValidateTimesN(command)) {
      return false;
    }
    if (!checkFullCommandSyntax(command, commandPattern)) {
      return false;
    }
    boolean autoDecline = true;
    return createAndAddRecurringEvent(autoDecline, repeatTimes);
  }

  /**
   * Validates the fields for a recurring event.
   *
   * @param command the command string to validate
   * @return true if validation passes, false otherwise
   */
  private boolean validateRecurringEventFields(String command) {


    return extractAndValidateBothDateAndTime(command)
            && extractAndValidateRepeatDays(command);
  }

  /**
   * Creates and adds a recurring event.
   *
   * @param autoDecline            whether to auto-decline conflicts
   * @param repeatTimesOrUntilDate number of repetitions or until date
   * @return true if successful, false otherwise
   */
  private boolean createAndAddRecurringEvent(boolean autoDecline, Object repeatTimesOrUntilDate) {
    CalendarEvent event = new CalendarEvent(eventName, startDate, endDate, description, location,
            eventType);
    RecurringEventStorage recurringEventStorage;

    if (repeatTimesOrUntilDate instanceof Integer) {
      recurringEventStorage = new RecurringEventStorage(event, repeatDays,
              (Integer) repeatTimesOrUntilDate);
    } else {
      recurringEventStorage = new RecurringEventStorage(event, repeatDays,
              (LocalDateTime) repeatTimesOrUntilDate);
    }

    recurringEventStorage.setEventStorage(eventStorage);
    recurringEventStorage.addRecurringEvents(autoDecline);
    return true;
  }

  /**
   * Handles the creation of a recurring event until a specific date.
   *
   * @param command the command string to process
   * @return true if the event was created successfully, false otherwise
   */
  private boolean handleRecurringEventByUntilDate(String command) {
    final Pattern commandPattern = Pattern.compile(
            "(?i)create event(?: --autoDecline)?\\s+(.+?)\\s+from\\s+" +
                    "(\\d{4}-\\d{2}-\\d{2}T" +
                    "\\d{2}:\\d{2})\\s+to\\s+" +
                    "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+repeats\\s" +
                    "+([MRUWFST]+)\\s+until\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})(?:\\s+" +
                    "location\\s+(.+?))?(?:\\s+description\\s+(.+?))?(?:\\s+type\\s+" +
                    "(public|private))?$"
    );

    if (!validateBasics(command, commandPattern)) {
      return false;
    }

    if (!validateRecurringEventFields(command)) {
      return false;
    }

    LocalDateTime untilDate = extractDateTime(command, "until");
    if (untilDate == null) {
      System.out.println("Error: Invalid or missing 'until' date.");
      return false;
    }
    if (!checkFullCommandSyntax(command, commandPattern)) {
      return false;
    }
    boolean autoDecline = true;
    return createAndAddRecurringEvent(autoDecline, untilDate.plusMinutes(1));
  }

  /**
   * Handles the creation of an all-day event.
   *
   * @param command the command string to process
   * @return true if the event was created successfully, false otherwise
   */
  private boolean handleAllDayEvent(String command) {
    final Pattern commandPattern = Pattern.compile(
            "(?i)create event(?: --autoDecline)?\\s+(.+?)\\s+on\\s+" +
                    "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:" +
                    "\\d{2})(?:\\s+location\\s+(.+?))?(?:\\s+description\\s+" +
                    "(.+?))?(?:\\s+type\\s+(public|private))?$"
    );

    if (!validateBasics(command, commandPattern)) {
      return false;
    }

    LocalDateTime startDate = extractDateTime(command, "on");
    if (startDate == null) {
      System.out.println("Error: Invalid or missing 'on' date.");
      return false;
    }

    LocalDateTime endDate = startDate.withHour(23).withMinute(59);
    if (!checkFullCommandSyntax(command, commandPattern)) {
      return false;
    }
    boolean autoDecline = command.contains("--autoDecline");
    CalendarEvent event = new CalendarEvent(eventName,
            startDate.withHour(0).withMinute(0), endDate, description,
            location, eventType);
    eventStorage.addEvent(event, autoDecline);
    return true;
  }

  /**
   * Validates all-day recurring event fields.
   *
   * @param command the command string to validate
   * @return true if validation passes, false otherwise
   */
  private boolean validateAllDayRecurringFields(String command) {
    LocalDateTime startDate = extractDate(command, "on");
    if (startDate == null) {
      System.out.println("Error: Invalid or missing 'on' date.");
      return false;
    }

    this.startDate = startDate.withHour(0).withMinute(0);
    this.endDate = this.startDate.withHour(23).withMinute(59);

    return extractAndValidateRepeatDays(command);
  }

  /**
   * Handles the creation of a recurring all-day event until a specific date.
   *
   * @param command the command string to process
   * @return true if the event was created successfully, false otherwise
   */
  private boolean handleRecurringAllDayEventUntilDate(String command) {
    final Pattern commandPattern = Pattern.compile(
            "(?i)create event(?: --autoDecline)?\\s+(.+?)\\s" +
                    "+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+" +
                    "repeats\\s+([MRUWFST]+)\\s+until\\s+(\\d{4}-\\d{2}-\\d{2})(?:\\s+" +
                    "location\\s+(.+?))?(?:\\s+description\\s+(.+?))?(?:\\s+type\\s+" +
                    "(public|private))?$"
    );

    if (!validateBasics(command, commandPattern)) {
      return false;
    }

    if (!validateAllDayRecurringFields(command)) {
      return false;
    }

    LocalDateTime untilDate = extractDate(command, "until");
    if (untilDate == null) {
      System.out.println("Error: Invalid or missing 'until' date.");
      return false;
    }
    untilDate = untilDate.plusDays(1).withHour(0).withMinute(0);
    //    untilDate = untilDate.withHour(23).withMinute(59);
    if (!checkFullCommandSyntax(command, commandPattern)) {
      return false;
    }
    boolean autoDecline = true;
    return createAndAddRecurringEvent(autoDecline, untilDate);
  }

  /**
   * Handles the creation of a recurring all-day event with a fixed number of
   * repetitions.
   *
   * @param command the command string to process
   * @return true if the event was created successfully, false otherwise
   */
  private boolean handleRecurringAllDayEventByTimes(String command) {
    final Pattern commandPattern = Pattern.compile(
            "(?i)create event(?: --autoDecline)" +
                    "?\\s+(.+?)\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats\\s+([MRUWFST]+)\\s+" +
                    "for\\s+(\\d+)\\s+times(?:\\s+location\\s+(.+?))?(?:\\s+description\\s+" +
                    "(.+?))?(?:\\s+type\\s+(public|private))?$"
    );

    if (!validateBasics(command, commandPattern)) {
      return false;
    }

    if (!validateAllDayRecurringFields(command)) {
      return false;
    }

    if (!extractAndValidateTimesN(command)) {
      return false;
    }
    if (!checkFullCommandSyntax(command, commandPattern)) {
      return false;
    }

    boolean autoDecline = true;
    return createAndAddRecurringEvent(autoDecline, repeatTimes);
  }

  /**
   * Extracts and validates the number of times to repeat.
   *
   * @param command the command string to process
   * @return true if the repeat times are valid, false otherwise
   */
  private boolean extractAndValidateTimesN(String command) {
    String repeatTimesStr = extractRepeatTimes(command);
    try {
      repeatTimes = Integer.parseInt(repeatTimesStr);
    } catch (NumberFormatException e) {
      System.out.println("Error: Invalid number for repeat times.");
      return false;
    }
    if (repeatTimes <= 0) {
      System.out.println("Error: Repeat times must be greater than zero.");
      return false;
    }
    return true;
  }
}