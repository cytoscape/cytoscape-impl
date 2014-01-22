package org.cytoscape.work.internal.task;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskMonitor;

public class TaskHistoryWindow {
  final TaskHistory taskHistory;
  final JDialog dialog;
  final JEditorPane pane;
  boolean isOpen = false;

  public TaskHistoryWindow(final TaskHistory taskHistory) {
    this.taskHistory = taskHistory;

    dialog = new JDialog(null, "Cytoscape Task History", JDialog.ModalityType.MODELESS);
    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        dialog.dispose();
        isOpen = false;
      }
    });
    dialog.setPreferredSize(new Dimension(500, 400));

    pane = new JEditorPane();
    pane.setEditable(false);
    pane.setContentType("text/html");
    final HTMLEditorKit htmlEditorKit = (HTMLEditorKit) pane.getEditorKit();
    final StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
    styleSheet.addRule("ul {list-style-type: none;}");

    final JButton cleanButton = new JButton("Clean");
    cleanButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        taskHistory.clear();
        update();
      }
    });

    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonsPanel.add(cleanButton);

    final JScrollPane scrollPane = new JScrollPane(pane);

    dialog.setLayout(new GridBagLayout());
    final EasyGBC c = new EasyGBC();
    dialog.add(scrollPane, c.expandBoth());
    dialog.add(buttonsPanel, c.down().expandHoriz());

    taskHistory.setFinishListener(new TaskHistory.FinishListener() {
      public void taskFinished(final TaskHistory.History history) {
        update();
      }
    });

    update();
    open();
  }

  public void close() {
    dialog.dispose();
    isOpen = false;
  }

  public void open() {
    if (!isOpen) {
      dialog.pack();
    }
    dialog.setVisible(true);
    update();
    isOpen = true;
  }

  private static String getIconURL(final FinishStatus.Type finishType) {
    if (finishType == null)
      return null;
    String name = null;
    switch (finishType) {
      case SUCCEEDED: name = "finished"; break;
      case FAILED:    name = "error"; break;
      case CANCELLED: name = "cancelled"; break;
    }
    if (name == null)
      return null;
    return TaskDialog.ICON_URLS.get(name).toString();
  }

  private static String getIconURL(final TaskMonitor.Level level) {
    if (level == null)
      return null;
    String name = null;
    switch (level) {
      case INFO:  name = "info"; break;
      case WARN:  name = "warn"; break;
      case ERROR: name = "error"; break;
    }
    if (name == null)
      return null;
    return TaskDialog.ICON_URLS.get(name).toString();
  }


  private String generateHistoryHTML() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("<html>");

    for (final TaskHistory.History history : taskHistory) {
      if (history.getFirstTaskClass() == null) {
        // skip task iterators that never called history.setFirstTaskClass() -- these
        // iterators were never started because they were cancelled by its first tunable dialog
        continue;
      }

      buffer.append("<p>");
      buffer.append("<h1 style=\"margin-top: 0px; margin-bottom: 0px;\">&nbsp;");

      final FinishStatus.Type finishType = history.getFinishType();
      final String finishIconURL = getIconURL(finishType);
      if (finishIconURL != null) {
        buffer.append("<img src=\"");
        buffer.append(finishIconURL);
        buffer.append("\">&nbsp;");
      }

      final String title = history.getTitle();
      if (title == null || title.length() == 0) {
        buffer.append("<i>Untitled</i>");
        final Class<?> klass = history.getFirstTaskClass();
        if (klass != null) {
          buffer.append(" <font size=\"-1\">(");
          buffer.append(klass.getName());
          buffer.append(")</font>");
        }
      } else {
        buffer.append(title);
      }
      buffer.append("</h1>");

      buffer.append("<ul style=\"margin-top: 0px; margin-bottom: 0px;\">");
      for (final TaskHistory.Message message : history) {
        final TaskMonitor.Level level = message.level();
        final String levelIconURL = getIconURL(level);
        if (levelIconURL != null) {
          buffer.append("<li style=\"margin-top: 5px;\">");
          buffer.append("<img src=\"");
          buffer.append(levelIconURL);
          buffer.append("\">&nbsp;");
        } else {
          buffer.append("<li style=\"margin-top: 10px;\">");
          buffer.append("<b>");
        }
        buffer.append(message.message());
        if (level == null) {
          buffer.append("</b>");
        }
        buffer.append("</li>");
      }
      buffer.append("</p>");
    }
    buffer.append("</html>");
    return buffer.toString();
  }

  public void update() {
    final String content = generateHistoryHTML();

    if (SwingUtilities.isEventDispatchThread()) {
      pane.setText(content);
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          pane.setText(content);
        }
      });
    }
  }
}
