package org.cytoscape.work.internal.task;


import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

class SwingTaskMonitor implements TaskMonitor {
	
	private Task task;
	final private ExecutorService cancelExecutorService;
	final private Window parent;

	private boolean cancelled = false;
	private TaskDialog dialog = null;
	
	private String title = null;
	private String statusMessage = null;
	private int progress = 0;
	private Future<?> future = null;

	public SwingTaskMonitor(final ExecutorService cancelExecutorService, final Window parent) {
		this.cancelExecutorService = cancelExecutorService;
		this.parent = parent;
	}

	public void setTask(final Task newTask) {
		this.task = newTask;
	}

	public void setFuture(final Future<?> future) {
		this.future = future;
	}

	public synchronized void open() {
		if (dialog != null)
			return;

		dialog = new TaskDialog(parent, this);
		dialog.setLocationRelativeTo(parent);
		
		
		if (title != null)
			dialog.setTaskTitle(title);
		if (statusMessage != null)
			dialog.setStatus(statusMessage);
		if (progress > 0)
			dialog.setPercentCompleted(progress);
		
		dialog.setVisible(true);
	}

	public synchronized void close() {
		if (dialog != null) {
			dialog.dispose();
			dialog = null;
		}
	}

	public void cancel() {
		// we issue the Task's cancel method in its own thread
		// to prevent Swing from freezing if the Tasks's cancel
		// method takes too long to finish
		Runnable cancel = new Runnable() {
			public void run() {
				task.cancel();
				try { Thread.sleep(1000); } catch (Exception e) {}
				if ( future != null && !future.isDone() && !future.isCancelled() )
					future.cancel(true);
			}
		};
		cancelExecutorService.submit(cancel);
		cancelled = true;
		
		// Close dialog (if necessary)
		close();
	}

	public boolean cancelled() {
		return cancelled;
	}

	public void setTitle(final String title) {
		this.title = title;
		if (dialog != null)
			dialog.setTaskTitle(title);
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
		if (dialog != null)
			dialog.setStatus(statusMessage);
	}

	public void setProgress(double progress) {
		this.progress = (int) Math.floor(progress * 100);
		if (dialog != null)
			dialog.setPercentCompleted(this.progress);
	}

	public synchronized void showException(Exception exception) {
		// force the dialog box to be created if
		// the Task throws an exception
		if (dialog == null)
			open();
		dialog.setException(exception, "The task could not be completed because an error has occurred.");
	}

	public synchronized boolean isShowingException() {
		return dialog.errorOccurred();
	}

	public synchronized boolean isOpened() {
		return dialog != null;
	}
}
