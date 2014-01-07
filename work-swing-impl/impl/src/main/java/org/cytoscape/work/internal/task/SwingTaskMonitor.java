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

import javax.swing.SwingUtilities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SwingTaskMonitor implements TaskMonitor {
	private static final String LOG_PREFIX = "TaskMonitor";
	
	/**
	 * This is used to execute the task's cancel method.
	 */
	final private ExecutorService cancelExecutorService;

	/**
	 * This is used for the task dialog's constructor.
	 */
	final private Window parent;

	final private TaskHistory.History history;

	private volatile boolean cancelled;
	//private TaskDialog dialog;
	private volatile TaskDialog2 dialog;
	private volatile Task task;
	private String title;
	private TaskMonitor.Level statusMessageLevel;
	private String statusMessage;
	private double progress;
	private Exception exception;
	private int expectedNumTasks = 1;
	private int currentTaskNum = -1; // so that the first task is numbered 0
	private volatile boolean showDialog = true;

	/**
	 * Based on the expected number of tasks, this is the fraction of the overall
	 * task monitor that a given task is allocated. So, if there are 4 tasks 
	 * executed with this task monitor, each task is allocated 0.25 of the 
	 * space in the progress bar.
	 */
	private double fractionOfOverall = 1.0;

	/**
 	 * We log all messages to our log channel.  This allows interested listeners
 	 * to "hear" them.
 	 */
	Logger thisLog = null;

	public SwingTaskMonitor(final ExecutorService cancelExecutorService, final Window parent, final TaskHistory taskHistory) {
		this.cancelExecutorService = cancelExecutorService;
		this.parent = parent;
		this.history = taskHistory.newHistory();
		this.thisLog = LoggerFactory.getLogger(LOG_PREFIX);
	}

	public void setExpectedNumTasks(final int expectedNumTasks) {
		this.expectedNumTasks = expectedNumTasks;
		this.fractionOfOverall = 1.0/(double)expectedNumTasks;
	}

	public void setTask(final Task newTask) {
		this.currentTaskNum++;	
		this.task = newTask;
		this.thisLog = LoggerFactory.getLogger(LOG_PREFIX+"."+newTask.getClass().getName());
	}

	public void open() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					open();
				}
			});
			return;
		}
		
		synchronized(this) {
			if (dialog != null)
				return;

			dialog = new TaskDialog2(parent);
			//dialog = new TaskDialog(parent, this);
			dialog.addPropertyChangeListener(TaskDialog2.CLOSE_EVENT, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					close();
				}
			});
			dialog.addPropertyChangeListener(TaskDialog2.CANCEL_EVENT, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					cancel();
				}
			});
			
			if (title != null)
				dialog.setTaskTitle(title);
			
			if (exception == null) {
				if (statusMessage != null) {
					if (statusMessageLevel == null) {
						dialog.setStatus(null, statusMessage);
					} else {
						dialog.setStatus(statusMessageLevel.name().toLowerCase(), statusMessage);
					}
				}
				if (progress != 0)
					dialog.setPercentCompleted((float) progress);
			} else {
				dialog.setException(exception);
				exception = null;
			}
			
			dialog.setVisible(showDialog);
		}
	}

	/**
	 * Used to toggle the monitor dialog so that when tunables
	 * are being displayed there are no Swing related threading 
	 * issues.
	 */
	public void showDialog(final boolean sd) {
		if(sd == showDialog) {
			return;
		}
		
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showDialog(sd);
				}
			});
			return;
		}
	
		synchronized (this) {
			showDialog = sd;
		}
		if (dialog != null && dialog.isVisible() != showDialog) {
			dialog.setVisible(showDialog);
		}
	}

	public void close() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					close();
				}
			});
			return;
		}

		synchronized(this) {
			if (dialog != null) {
				dialog.dispose();
				dialog = null;
			}
			task = null;
		}
	}

	public void cancel() {
		// we issue the Task's cancel method in its own thread
		// to prevent Swing from freezing if the Tasks's cancel
		// method takes too long to finish
		cancelled = true;
		Runnable cancel = new Runnable() {
			public void run() {
				task.cancel();
			}
		};
		cancelExecutorService.submit(cancel);
		
		/* Do NOT close the dialog here; dialog closes when the task terminates itself after its cancel method is invoked */
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
		history.setTitle(title);
		if (dialog != null)
			dialog.setTaskTitle(title);
	}

	public void setStatusMessage(final String statusMessage) {
		showMessage(TaskMonitor.Level.INFO, statusMessage);
	}

	public void showMessage(TaskMonitor.Level level, String message) {
		switch (level) {
		case ERROR:
			thisLog.error(message);
			break;
		case WARN:
			thisLog.warn(message);
			break;
		case INFO:
			thisLog.info(message);
			break;
		}
		showStatusMessage(level, message);
	}

	public void setProgress(final double newProgress) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setProgress(newProgress);
				}
			});
			return;
		}
		if ( newProgress < 0.0 ) {
			this.progress = -1.0;
			if (dialog != null)
				dialog.setPercentCompleted(-1.0f);
		} else {
			if(currentTaskNum < expectedNumTasks) 
				this.progress = (newProgress * fractionOfOverall) + 
					((double)currentTaskNum/(double)expectedNumTasks);
			else
				this.progress = newProgress;
			if (dialog != null)
				dialog.setPercentCompleted((float) this.progress);
		}
	}

	public void showException(final Exception exception) {
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
			dialog.setException(exception);
		}
	}

	public boolean isClosed() {
		return task == null;
	}

	private void showStatusMessage(final TaskMonitor.Level level, final String statusMessage) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showStatusMessage(level, statusMessage);
				}
			});
			return;
		}
		this.statusMessage = statusMessage;
		this.statusMessageLevel = level;
		if (dialog != null)
			dialog.setStatus(statusMessageLevel.name().toLowerCase(), statusMessage);
		history.addMessage(level, statusMessage);
	}

	public String getTitle() {
		return title;
	}
}
