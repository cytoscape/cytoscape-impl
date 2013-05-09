package org.cytoscape.task.internal;

import java.util.LinkedHashSet;

import org.cytoscape.work.TaskObserver;

public class ObservableDelegate<R> {
	private final LinkedHashSet<TaskObserver<R>> observers;

	public ObservableDelegate() {
		observers = new LinkedHashSet<TaskObserver<R>>();
	}
	
	public void addObserver(TaskObserver<R> observer) {
		observers.add(observer);
	}
	
	public void removeObserver(TaskObserver<R> observer) {
		observers.remove(observer);
	}
	
	public void finish(R result) {
		for (TaskObserver<R> observer : observers) {
			observer.taskFinished(result);
		}
	}
}
