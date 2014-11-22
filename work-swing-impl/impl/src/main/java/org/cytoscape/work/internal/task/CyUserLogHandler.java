package org.cytoscape.work.internal.task;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Level;

import org.cytoscape.work.TaskMonitor;

public class CyUserLogHandler extends Handler {
  final TaskStatusBar statusBar;
  final TaskHistory history;

  public CyUserLogHandler(final TaskStatusBar statusBar, final TaskHistory history) {
    this.statusBar = statusBar;
    this.history = history;
  }

  public void close() {}
  public void flush() {}

  public void publish(final LogRecord record) {
    if (record == null) {
      return;
    }
    final TaskMonitor.Level level = getCorrespondingLevel(record.getLevel());
    final String message = record.getMessage();
    history.addUnnestedMessage(level, message);
    statusBar.setTitle(level, message);
  }

  private static TaskMonitor.Level getCorrespondingLevel(final Level julLevel) {
    if (julLevel.equals(Level.INFO)) {
      return TaskMonitor.Level.INFO;
    } else if (julLevel.equals(Level.WARNING)) {
      return TaskMonitor.Level.WARN;
    } else if (julLevel.equals(Level.SEVERE)) {
      return TaskMonitor.Level.ERROR;
    } else {
      return null;
    }
  }
}