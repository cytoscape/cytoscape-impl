package org.cytoscape.work.internal.task;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.cytoscape.work.TaskMonitor;

public class TaskHistoryWindow {
  final JDialog dialog;
  final JPanel tasksPanel;

  public TaskHistoryWindow(final TaskHistory taskHistory) {
    dialog = new JDialog(null, "Cytoscape Task History", JDialog.ModalityType.MODELESS);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setPreferredSize(new Dimension(500, 400));

    tasksPanel = new JPanel();
    tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
    final JScrollPane scrollPane = new JScrollPane(tasksPanel);

    dialog.setLayout(new GridBagLayout());
    final EasyGBC c = new EasyGBC();
    dialog.add(scrollPane, c.expandBoth());

    populate(taskHistory);

    dialog.pack();
    dialog.setVisible(true);
  }

  static JLabel newLabelWithFont(final int style, final int size, final String text) {
    final Font defaultFont = UIManager.getFont("Label.font");
    final Font font = new Font(defaultFont == null ? null : defaultFont.getName(), style, size);
    final JLabel label = new JLabel(text);
    label.setFont(font);
    return label;
  }

  private void populate(final TaskHistory histories) {
    tasksPanel.removeAll();
    for (final TaskHistory.History history : histories) {
      System.out.println(String.format("History[%x].setTitle: %s", history.hashCode(), history.getTitle()));
      final JPanel taskPanel = new JPanel();
      taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));

      final JLabel titleLabel = newLabelWithFont(Font.BOLD, 16, history.getTitle());
      switch (history.getCompletionStatus()) {
        case TaskHistory.TASK_SUCCESS:    titleLabel.setIcon(TaskDialog2.ICONS.get("finished")); break;
        case TaskHistory.TASK_FAILED:     titleLabel.setIcon(TaskDialog2.ICONS.get("error")); break;
        case TaskHistory.TASK_CANCELLED:  titleLabel.setIcon(TaskDialog2.ICONS.get("cancelled")); break;
      }

      final JPanel messagesPanel = new JPanel();
      messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
      for (final TaskHistory.Message message : history) {
        final JLabel messageLabel = new JLabel(message.message());
        final TaskMonitor.Level level = message.level();
        if (level != null) {
          switch(level) {
            case INFO: messageLabel.setIcon(TaskDialog2.ICONS.get("info")); break;
            case WARN: messageLabel.setIcon(TaskDialog2.ICONS.get("warn")); break;
            case ERROR: messageLabel.setIcon(TaskDialog2.ICONS.get("error")); break;
          }
        }
        messagesPanel.add(messageLabel);
      }
      messagesPanel.setVisible(false);

      final JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      final DiscloseTriangle triangle = new DiscloseTriangle();
      triangle.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          messagesPanel.setVisible(triangle.isOpen());
        }
      });
      titlePanel.add(triangle);
      titlePanel.add(titleLabel);
      taskPanel.add(titlePanel);
      taskPanel.add(messagesPanel);
      tasksPanel.add(taskPanel);
    }
  }
}
