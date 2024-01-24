package org.cytoscape.work.internal.task;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * A data structure for representing histories of task iterators. The
 * {@code TaskHistory} instance can consist of many {@code History} instances,
 * which represents the history of a single task iterator execution.
 * {@code History}'s contain the task's title, completion status, and
 * {@code Message}s.
 *
 * <p> This class is thread-safe. </p>
 */
public class TaskHistory implements Iterable<TaskHistory.History> {
	
	// In an automation environment its important that the task history doesn't grow forever.
	private static final int HISTORY_CAPACITY = 5000;

	public static interface FinishListener {
		public void taskFinished(History history);
	}

	
	public static record Message(TaskMonitor.Level level, String message) { }

	
	/**
	 * The history for a TaskIterator
	 */
	public class History implements Iterable<Message> {

		private volatile FinishStatus.Type finishType;
		private volatile Class<?> firstTaskClass;
		private final boolean isUnnested;
		
		private final AtomicReference<String> title = new AtomicReference<>();
		private final ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<>();

		private History() {
			isUnnested = false;
		}
		
		private History(TaskMonitor.Level level, String message) {
			addMessage(level, message);
			isUnnested = true;
		}

		public void setTitle(final String newTitle) {
			if (!title.compareAndSet(null, newTitle)) {
				addMessage(null, newTitle);
			}
		}

		public void setFinishType(FinishStatus.Type finishType) {
			this.finishType = finishType;
			if (finishListener != null) {
				finishListener.taskFinished(this);
			}
		}

		public void addMessage(TaskMonitor.Level level, String message) {
			messages.add(new Message(level, message));
		}

		public Message getFirstMessage() {
			return messages.isEmpty() ? null : messages.peek();
		}
		
		@Override
		public Iterator<Message> iterator() {
			return messages.iterator();
		}

		public String getTitle() {
			return title.get();
		}

		public FinishStatus.Type getFinishType() {
			return finishType;
		}

		public boolean hasMessages() {
			return !messages.isEmpty();
		}

		public boolean isUnnested() {
			return isUnnested;
		}
		
		public void setFirstTaskClass(final Class<?> klass) {
			this.firstTaskClass = klass;
		}

		public Class<?> getFirstTaskClass() {
			return firstTaskClass;
		}
	}
	

	private final ConcurrentLinkedQueue<History> histories = new ConcurrentLinkedQueue<>();
	private final AtomicInteger counter = new AtomicInteger();
	
	private volatile FinishListener finishListener;
	private final int drainAmount = HISTORY_CAPACITY / 10;
	
	/**
	 * Create a new {@code History} for a TaskIterator.
	 */
	public History newHistory() {
		// Use an atomic counter to keep track of size of the history without locks.
		// Can't use the ConcurrentLinkedQueue.size() method because it's O(n).
		int size = counter.getAndUpdate(count ->
			1 + (count > HISTORY_CAPACITY ? count-drainAmount : count)
		);
		
		// Clear the oldest entries from the history in chunks.
		if(size > HISTORY_CAPACITY) {
			for(int i = 0; i < drainAmount; i++) {
				histories.poll();
			}
		}
		
		History history = new History();
		histories.add(history);
		return history;
	}

	/**
	 * Return all {@code History}'s contained in thiwads instance.
	 */
	@Override
	public Iterator<History> iterator() {
		return histories.iterator();
	}

	/**
	 * Clear out all {@code History}'s contained in this instance.
	 */
	public void clear() {
		counter.set(0);
		histories.clear();
	}

	public void setFinishListener(FinishListener finishListener) {
		this.finishListener = finishListener;
	}

	public void addUnnestedMessage(TaskMonitor.Level level, String message) {
		histories.add(new History(level, message));
	}
}