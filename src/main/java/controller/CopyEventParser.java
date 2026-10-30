package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.CalendarManager;
import model.EventCopier;

/**
 * Handles parsing of copy commands and delegates to EventCopier.
 */
public class CopyEventParser implements CommandExecutor {
  private final EventCopier eventCopier;

  private static final Pattern copyEventPattern = Pattern.compile(
          "copy event (.+?) on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " +
                  "--target (\\w+) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
  );

  private static final Pattern copyEventsOnPattern = Pattern.compile(
          "copy events on (\\d{4}-\\d{2}-\\d{2}) --target (\\w+) to (\\d{4}-\\d{2}-\\d{2})"
  );

  private static final Pattern copyEventsBetweenPattern = Pattern.compile(
          "copy events between (\\d{4}-\\d{2}-\\d{2}) and (\\d{4}-\\d{2}-\\d{2})" +
                  " --target (\\w+) to (\\d{4}-\\d{2}-\\d{2})"
  );

  /**
   * Constructs a CopyEventParser with the specified CalendarManager.
   * Initializes the EventCopier to handle event copying logic.
   *
   * @param calendarManager the CalendarManager instance used for event
   *                        management during the copy process
   */
  public CopyEventParser(CalendarManager calendarManager) {
    this.eventCopier = new EventCopier(calendarManager);
  }

  @Override
  public boolean executeCommand(String command) {
    Matcher copyEventMatcher = copyEventPattern.matcher(command);
    Matcher copyEventsOnMatcher = copyEventsOnPattern.matcher(command);
    Matcher copyEventsBetweenMatcher = copyEventsBetweenPattern.matcher(command);

    if (copyEventMatcher.matches()) {
      return eventCopier.copyEvent(
              copyEventMatcher.group(1),
              LocalDateTime.parse(copyEventMatcher.group(2)),
              copyEventMatcher.group(3),
              LocalDateTime.parse(copyEventMatcher.group(4))
      );
    } else if (copyEventsOnMatcher.matches()) {
      return eventCopier.copyEventsOnDate(
              LocalDate.parse(copyEventsOnMatcher.group(1)),
              copyEventsOnMatcher.group(2),
              LocalDate.parse(copyEventsOnMatcher.group(3))
      );
    } else if (copyEventsBetweenMatcher.matches()) {
      return eventCopier.copyEventsBetween(
              LocalDate.parse(copyEventsBetweenMatcher.group(1)),
              LocalDate.parse(copyEventsBetweenMatcher.group(2)),
              copyEventsBetweenMatcher.group(3),
              LocalDate.parse(copyEventsBetweenMatcher.group(4))
      );
    }

    throw new IllegalArgumentException("Invalid command format.");
  }
}