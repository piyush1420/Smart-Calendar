package view.gui.utils;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Utility method for dialog displays in the Calendar application.
 * Centralizes common dialog rendering logic.
 */
public class DialogUtils {

  /**
   * Shows a dialog with formatted text output.
   *
   * @param title   Dialog title
   * @param content Text content to display
   */
  public static void showOutputDialog(String title, String content) {
    JTextArea textArea = new JTextArea(content);
    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(500, 300));

    JOptionPane.showMessageDialog(null,
            scrollPane,
            title,
            JOptionPane.PLAIN_MESSAGE);
  }
}
