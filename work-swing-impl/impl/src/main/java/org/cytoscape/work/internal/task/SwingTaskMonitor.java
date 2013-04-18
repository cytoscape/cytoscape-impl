package org.cytoscape.work.internal.task;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.awt.Window;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

class SwingTaskMonitor implements TaskMonitor {
	
	private static final String ERROR_MESSAGE = "The task could not be completed because an error has occurred.";
	
	final private ExecutorService cancelExecutorService;
	final private Window parent;

	private volatile boolean cancelled;
	private TaskDialog dialog;
	private Task task;
	private String title;
	private String statusMessage;
	private int progress;
	private Exception exception;
	private Future<?> future;
	private int expectedNumTasks = 1;
	private int currentTaskNum = -1; // so that the first task is numbered 0
	private boolean showDialog = true;

	/**
	 * Based on the expected number of tasks, this is the fraction of the overall
	 * task monitor that a given task is allocated. So, if there are 4 tasks 
	 * executed with this task monitor, each task is allocated 0.25 of the 
	 * space in the progress bar.
	 */
	private double fractionOfOverall = 1.0;

	public SwingTaskMonitor(final ExecutorService cancelExecutorService, final Window parent) {
		this.cancelExecutorService = cancelExecutorService;
		this.parent = parent;
	}

	public void setExpectedNumTasks(final int expectedNumTasks) {
		this.expectedNumTasks = expectedNumTasks;
		this.fractionOfOverall = 1.0/(double)expectedNumTasks;
	}

	public void setTask(final Task newTask) {
		this.currentTaskNum++;	
		this.task = newTask;
	}

	public void setFuture(final Future<?> future) {
		this.future = future;
	}

	public synchronized void open() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					open();
				}
			});
			return;
		}
		
		if (dialog != null)
			return;

		dialog = new TaskDialog(parent, this);
		dialog.setLocationRelativeTo(parent);
		
		if (title != null)
			dialog.setTaskTitle(title);
		
		if (exception == null) {
			if (statusMessage != null)
				dialog.setStatus(statusMessage);
			if (progress != 0)
				dialog.setPercentCompleted(progress);
		} else {
			dialog.setException(exception, ERROR_MESSAGE);
			exception = null;
		}
		
		dialog.setVisible(showDialog);
	}

	/**
	 * Used to toggle the monitor dialog so that when tunables
	 * are being displayed there are no Swing related threading 
	 * issues.
	 */
	public void showDialog(final boolean sd) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showDialog(sd);
				}
			});
			return;
		}
		showDialog = sd;
		if ( dialog != null ) {
			dialog.setVisible(showDialog);
		}
	}

	public synchronized void close() {
		if (dialog != null) {
			dialog.dispose();
			dialog = null;
		}
		future = null;
		task = null;
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

	protected boolean cancelled() {
		return cancelled;
	}

	public void setTitle(final String title) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setTitle(title);
				}
			});
			return;
		}
		this.title = title;
		if (dialog != null)
			dialog.setTaskTitle(title);
	}

	public void setStatusMessage(final String statusMessage) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setStatusMessage(statusMessage);
				}
			});
			return;
		}
		this.statusMessage = statusMessage;
		if (dialog != null)
			dialog.setStatus(statusMessage);
	}

	public void setProgress(final double progress) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setProgress(progress);
				}
			});
			return;
		}
		if ( progress < 0 ) {
			this.progress = -1;
			if (dialog != null)
				dialog.setPercentCompleted(-1);
		} else {
			double adjustedProgress;
			if(currentTaskNum < expectedNumTasks) 
				adjustedProgress = (progress * fractionOfOverall) + 
					((double)currentTaskNum/(double)expectedNumTasks);
			else
				adjustedProgress = progress;
			this.progress = (int) Math.floor(100.0 * adjustedProgress);
			if (dialog != null)
				dialog.setPercentCompleted(this.progress);
		}
	}

	public synchronized void showException(final Exception exception) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showException(exception);
				}
			});
			return;
		}
		
		// force the dialog box to be created if the Task throws an exception
		if (dialog == null) {
			this.exception = exception;
			open();
		} else {
			dialog.setException(exception, ERROR_MESSAGE);
		}
	}

	public synchronized boolean isShowingException() {
		return dialog != null && dialog.errorOccurred();
	}

	public synchronized boolean isOpened() {
		return dialog != null;
	}

	public synchronized boolean isClosed() {
		return task == null;
	}
}
