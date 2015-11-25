package org.cytoscape.work.internal.task;

import org.cytoscape.work.TaskMonitor;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

public class CyUserLogAppender implements PaxAppender {
  final TaskStatusBar statusBar;
  final TaskHistory history;

  public CyUserLogAppender(final TaskStatusBar statusBar, final TaskHistory history) {
    this.statusBar = statusBar;
    this.history = history;
  }

  public void doAppend(final PaxLoggingEvent event) {
    final TaskMonitor.Level level = getCorrespondingLevel(event.getLevel().toInt());
    final String message = event.getMessage();

    history.addUnnestedMessage(level, message);
    statusBar.setTitle(level, message);
    
    if(event.getThrowableStrRep() != null) {
    	for(String line: event.getThrowableStrRep())
    		System.err.println(line);
    }
  }

  private static TaskMonitor.Level getCorrespondingLevel(final int level) {
    switch (level) {
      case 40000:
        return TaskMonitor.Level.ERROR;
      case 30000:
        return TaskMonitor.Level.WARN;
      default:
        return TaskMonitor.Level.INFO;
    }
  }
}
