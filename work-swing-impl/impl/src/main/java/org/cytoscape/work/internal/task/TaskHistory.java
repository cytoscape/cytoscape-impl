package org.cytoscape.work.internal.task;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.cytoscape.work.TaskMonitor;

public class TaskHistory implements Iterable<TaskHistory.History> {
  public static final TaskMonitor.Level[] levels = TaskMonitor.Level.values();

  // Use bytes instead of enums to save memory
  public static final byte TASK_SUCCESS = 0;
  public static final byte TASK_FAILED = 1;
  public static final byte TASK_CANCELLED = 2;

  public static class Message {
    // Use a byte to reference TaskMonitor.Level to save memory
    final byte levelOrdinal;
    final String message;

    public Message(final TaskMonitor.Level level, final String message) {
      this.levelOrdinal = level == null ? -1 : (byte) level.ordinal();
      this.message = message;
    }

    public TaskMonitor.Level level() {
      return (this.levelOrdinal < 0) ? null : levels[this.levelOrdinal];
    }

    public String message() {
      return message;
    }
  }

  public static class History implements Iterable<Message> {
    volatile byte completionStatus = -1;
    final AtomicReference<String> title = new AtomicReference<String>();
    final ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<Message>();
    final AtomicIntegerArray numberOfMessagesByLevel = new AtomicIntegerArray(levels.length);

    protected History() {}

    public void setTitle(final String newTitle) {
      if (!title.compareAndSet(null, newTitle)) {
        addMessage(null, newTitle);
      }
    }

    public void setCompletionStatus(final byte completionStatus) {
      this.completionStatus = completionStatus;
    }

    public void addMessage(final TaskMonitor.Level level, final String message) {
      messages.add(new Message(level, message));
      if (level != null) {
        final int levelOrdinal = level.ordinal();
        numberOfMessagesByLevel.getAndIncrement(levelOrdinal);
      }
    }

    public Iterator<Message> iterator() {
      return messages.iterator();
    }

    public String getTitle() {
      return title.get();
    }

    public byte getCompletionStatus() {
      return completionStatus;
    }

    public boolean hasMessages() {
      return messages.size() > 0;
    }

    public int numberOfMessagesWithLevel(final TaskMonitor.Level level) {
      return numberOfMessagesByLevel.get(level.ordinal());
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