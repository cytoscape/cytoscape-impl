package org.cytoscape.work.internal.task;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.cytoscape.work.TaskMonitor;

class TaskHistory implements Iterable<TaskHistory.History> {
  public static class Message {
    static final TaskMonitor.Level[] levels = TaskMonitor.Level.values();

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

    public void addMessage(final TaskMonitor.Level level, final String message) {
      messages.add(new Message(level, message));
    }

    public Iterator<Message> iterator() {
      return messages.iterator();
    }

    public String getTitle() {
      return title;
    }
  }

  final ConcurrentLinkedQueue<History> histories = new ConcurrentLinkedQueue<History>();

  public History newHistory() {
    return new History();
  }

  public Iterator<History> iterator() {
    return histories.iterator();
  }
}