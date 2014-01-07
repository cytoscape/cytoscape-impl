package org.cytoscape.work.internal.task;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.cytoscape.work.TaskMonitor;

public class TaskHistory implements Iterable<TaskHistory.History> {
  public static final TaskMonitor.Level[] levels = TaskMonitor.Level.values();
  
  public static final byte TASK_SUCCESS = 0;
  public static final byte TASK_FAILED = 1;
  public static final byte TASK_CANCELLED = 2;

  public static class Message {

    final byte levelOrdinal;
    final String message;

    public Message(final TaskMonitor.Level level, final String message) {
      this.levelOrdinal = level == null ? -1 : (byte) level.ordinal();
      this.message = message;
    }

    public TaskMonitor.Level level() {
      return levels[this.levelOrdinal];
    }

    public String message() {
      return message;
    }
  }

  public static class History implements Iterable<Message> {
    volatile byte completionStatus;
    volatile String title = null;
    final ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<Message>();

    protected History() {}

    public void setTitle(final String newTitle) {
      if (this.title == null) {
        this.title = newTitle;
      } else {
        addMessage(null, newTitle);
      }
    }

    public void setCompletionStatus(final byte completionStatus) {
      this.completionStatus = completionStatus;
    }

    public void addMessage(final TaskMonitor.Level level, final String message) {
      messages.add(new Message(level, message));
    }

    public Iterator<Message> iterator() {
      return messages.iterator();
    }

    public String getTitle() {
      return title;
    }

    public byte getCompletionStatus() {
      return completionStatus;
    }
  }

  final ConcurrentLinkedQueue<History> histories = new ConcurrentLinkedQueue<History>();

  public History newHistory() {
    final History history = new History();
    histories.add(history);
    return history;
  }

  public Iterator<History> iterator() {
    return histories.iterator();
  }
}