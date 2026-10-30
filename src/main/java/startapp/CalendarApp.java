package startapp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.SwingUtilities;

import controller.CommandParser;
import controller.InvalidCommandException;
import model.CalendarManager;
import model.MultiCalendarEventStorage;
import view.gui.CalendarFrame;

import startapp.ai.AiIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Calendar system that supports interactive,
 * headless, and GUI modes of operation, now powered by Spring Boot and Spring AI.
 */
@SpringBootApplication
public class CalendarApp implements CommandLineRunner {

  @Autowired
  private AiIntegrationService aiService;

  public static void main(String[] args) throws InvalidCommandException {
    SpringApplication.run(CalendarApp.class, args);
  }

  /**
   * After Spring Boot context is initialized, routes to appropriate mode.
   */
  @Override
  public void run(String... args) throws Exception {
    if (args.length == 0 || "gui".equalsIgnoreCase(args[0])) {
      runGUIMode();
      return;
    }

    if (args.length < 2 || !"--mode".equals(args[0])) {
      System.out.println("Usage: --mode [interactive|headless <filename>|gui]");
      return;
    }

    String operationMode = args[1].toLowerCase();
    switch (operationMode) {
      case "interactive":
        runInteractiveMode();
        break;
      case "headless":
        if (args.length < 3) {
          System.out.println("Error: Missing filename for headless mode.");
          return;
        }
        runHeadlessMode(args[2]);
        break;
      default:
        System.out.println("Error: Invalid mode. Use 'interactive', 'headless', or 'gui'.");
    }
  }

  /**
   * Runs the application in GUI mode, injecting the AI service into the frame.
   */
  private void runGUIMode() {
    SwingUtilities.invokeLater(() -> {
      try {
        MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
        CalendarManager manager = new CalendarManager(storage);
        CommandParser parser = new CommandParser(manager);
        // Pass AiIntegrationService into GUI
        new CalendarFrame(parser, manager, "America/New_York", aiService)
                .setVisible(true);
      } catch (Exception e) {
        System.err.println("Failed to initialize GUI: " + e.getMessage());
        e.printStackTrace();
      }
    });
  }

  /**
   * Runs the application in interactive mode, accepting commands from user input.
   */
  private void runInteractiveMode() throws InvalidCommandException {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    CalendarManager manager = new CalendarManager(storage);
    CommandParser parser = new CommandParser(manager);
    Scanner inputScanner = new Scanner(System.in);

    while (true) {
      System.out.print("Enter command: ");
      String userCommand = inputScanner.nextLine().trim();

      if (userCommand.equalsIgnoreCase("exit")) {
        System.out.println("Exiting interactive mode.");
        break;
      }

      // You can invoke AI features here via aiService if desired
      if (!parser.executeCommand(userCommand)) {
        System.out.println("Command failed: " + userCommand);
      }
    }
  }

  /**
   * Runs the application in headless mode, reading commands from a file.
   */
  private void runHeadlessMode(String inputFilename) {
    MultiCalendarEventStorage storage = new MultiCalendarEventStorage();
    CalendarManager manager = new CalendarManager(storage);
    CommandParser parser = new CommandParser(manager);

    try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFilename))) {
      String fileCommand;
      while ((fileCommand = fileReader.readLine()) != null) {
        fileCommand = fileCommand.trim();
        if (fileCommand.equalsIgnoreCase("exit")) {
          System.out.println("Exiting headless mode.");
          break;
        }

        try {
          if (!parser.executeCommand(fileCommand)) {
            System.out.println("Command failed: " + fileCommand);
          }
        } catch (InvalidCommandException e) {
          System.out.println("Error executing command: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }
}
