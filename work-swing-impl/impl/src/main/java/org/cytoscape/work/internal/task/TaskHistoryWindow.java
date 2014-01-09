package org.cytoscape.work.internal.task;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;


import java.awt.Dimension;
import java.awt.GridBagLayout;

import org.cytoscape.work.TaskMonitor;

public class TaskHistoryWindow {
  final JDialog dialog;
  final JEditorPane pane;

  public TaskHistoryWindow(final TaskHistory taskHistory) {
    dialog = new JDialog(null, "Cytoscape Task History", JDialog.ModalityType.MODELESS);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setPreferredSize(new Dimension(500, 400));

    pane = new JEditorPane();
    pane.setEditable(false);
    pane.setContentType("text/html");
    final HTMLEditorKit htmlEditorKit = (HTMLEditorKit) pane.getEditorKit();
    final StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
    styleSheet.addRule("ul { list-style-type: none; }");

    final JScrollPane scrollPane = new JScrollPane(pane);

    dialog.setLayout(new GridBagLayout());
    final EasyGBC c = new EasyGBC();
    dialog.add(scrollPane, c.expandBoth());

    populate(taskHistory);

    dialog.pack();
    dialog.setVisible(true);
  }

  public void close() {
    dialog.dispose();
  }

  static String safeString(final String str, final String alternative) {
    if (str == null || str.length() == 0)
      return alternative;
    else
      return str;
  }

  private void populate(final TaskHistory histories) {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("<html>");

    for (final TaskHistory.History history : histories) {
      buffer.append("<p>");
      buffer.append("<h1>&nbsp;");
      buffer.append("<img src=\"");
      switch (history.getCompletionStatus()) {
        case TaskHistory.TASK_SUCCESS:    buffer.append(TaskDialog.ICON_URLS.get("finished").toString()); break;
        case TaskHistory.TASK_FAILED:     buffer.append(TaskDialog.ICON_URLS.get("error").toString()); break;
        case TaskHistory.TASK_CANCELLED:  buffer.append(TaskDialog.ICON_URLS.get("cancelled").toString()); break;
      }
      buffer.append("\">&nbsp;");
      buffer.append(safeString(history.getTitle(), "<i>Untitled</i>"));
      buffer.append("</h1>");

      buffer.append("<ul type=\"none\">");
      for (final TaskHistory.Message message : history) {
        buffer.append("<li>");
        final TaskMonitor.Level level = message.level();
        if (level != null) {
          buffer.append("<img src=\"");
          switch(level) {
            case INFO: buffer.append(TaskDialog.ICONS.get("info").toString()); break;
            case WARN: buffer.append(TaskDialog.ICONS.get("warn").toString()); break;
            case ERROR: buffer.append(TaskDialog.ICONS.get("error").toString()); break;
          }
          buffer.append("\">&nbsp;");
        } else {
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
    pane.setText(buffer.toString());
  }
}
