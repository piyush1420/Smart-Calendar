## USEME - Project Instructions

### Running the Application

You can run the application in several modes. Below are the instructions for each mode:

#### GUI Mode:
To open the graphical user interface, simply run:
```bash
java -jar Program.jar
```
**Example:**
```bash
java -jar cs5010-group-project.jar
```
Alternatively, you can double-click the JAR file directly.

#### Headless Mode:
Execute a script file and exit immediately:
```bash
java -jar Program.jar --mode headless path-of-script-file
```
**Example:**
```bash
java -jar cs5010-group-project.jar --mode headless </absolute/path/to/script>
```

#### Interactive Mode:
Execute commands interactively in a text-based interface:
```bash
java -jar Program.jar --mode interactive
```
**Example:**
```bash
java -jar cs5010-group-project.jar --mode interactive
```

### Main Features
- **Create Calendar:** Click on the New Calendar Button
- **Edit Calendar:** Click on the Edit Calendar Button
- **View Calendar:** The main window displays a monthly calendar view at the bottom you can see the current calendar you are working on 
- **Switch Calendar:** Use the drop-down box at the top left to switch between available calendars.
- **Create Events:** `Right-click` on a `date` to create a new event
- **Edit Events:** `Right-click` on a `date` to edit existing events
- **View Events:** `Click` on a `date` to see all events for that day
- **Navigate:** Use the navigation buttons to move between months
- **Export Events:** Click on the export button at the top to export events present in the calendar
- **Import Events:** Click on the import button at the top to import events present in the CSV file

## Using the Application (GUI)

When you run the application in GUI mode, the application window opens and displays the calendar.

1. **Default Calendar:**
    - Initially, a default calendar is loaded.
    - You can check which calendar you're currently viewing at the bottom left of the window.

2. **Creating a New Calendar:**
    - Click the **New Calendar** button at the top left.
    - A popup will appear allowing you to enter the calendar name and timezone.

3. **Switching Between Calendars:**
    - Use the drop-down box at the top left to switch between available calendars.

4. **Editing a Calendar:**
    - Click the **Edit Calendar** button.
    - A popup will appear allowing you to select the calendar and edit its name or timezone.

5.  **Creating Events:**
    - **Right-click** on a date in the calendar. 
    - Select "Create Event" from the context menu
    - Fill in the event details in the dialog
    - Click "Create Event" to save the event
    - **[OR]** Click on the New Event button on the top.

6. **Editing Events:**
    - **Right-click** on a date in the calendar 
    - Select "Edit Events" from the context menu
    - Choose the event to edit
    - Modify the event details
    - Click "Submit" to update the event
    - **[OR]** Click on the Edit Event button on the top.

7. **Viewing Events:**
    - Click on a date in the calendar.
    - A popup will show all events for that day. 
    - From this popup, you can view all the events on that particular day.
    - Close the popup (using the "OK" button)
    - **[OR]**
    - Click on the View Events button on the top.

8. **Export Events:**
    - Click on the export button at the top.
    - The generated CSV file with name `events.csv` will be stored in the class path: src/main/java

9. **Import Events:**
    - Click on the import button at the top.
    - Select the CSV file to import from your local storage.


### Resource Directory

Navigate to your resource directory (`res`) using the following command:
```bash
cd </absolute/path/to/res>
```
**Example:**
```bash
cd </Users/atultiwary/Documents/PDP/group/Assignment_5/Group-Project-CS5010/Assignment_5_Part_II/res>
```

### Edit All Occurrences of a Recurring Event

•	When a recurring event is created, it can be edited in two ways:

•	By right-clicking on the day that contains the recurring event and selecting “Edit Event”.

•	Or by selecting the event and clicking the “Edit Event” button in the toolbar.

•	In the edit dialog that appears, a checkbox labeled “Edit all occurrences” will be visible 
    for recurring events.

•	Selecting this checkbox allows users to apply the changes to all occurrences of the event, 
    rather than just the selected one.

•	This is useful for bulk editing attributes such as the event’s subject, time, location, or 
    description across the entire recurring series.

