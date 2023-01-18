package org.cytoscape.work.internal.task;

import static org.cytoscape.work.internal.tunables.utils.ViewUtil.invokeOnEDT;

import java.awt.Window;
import java.util.concurrent.ExecutorService;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.internal.view.TaskDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	final private CyServiceRegistrar serviceRegistrar;

	private volatile boolean cancelled;
	private volatile TaskDialog dialog;
	private volatile Task task;
	private volatile String firstTitle;
	private volatile String title;
	private volatile TaskMonitor.Level statusMessageLevel;
	private volatile String statusMessage;
	private volatile double progress;
	private volatile Exception exception;
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

	public SwingTaskMonitor(
			final ExecutorService cancelExecutorService,
			final Window parent,
			final TaskHistory.History history,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.cancelExecutorService = cancelExecutorService;
		this.parent = parent;
		this.history = history;
		this.serviceRegistrar = serviceRegistrar;
		this.thisLog = LoggerFactory.getLogger(LOG_PREFIX);
	}

	public void setExpectedNumTasks(final int expectedNumTasks) {
		this.expectedNumTasks = expectedNumTasks;
		this.fractionOfOverall = 1.0 / (double) expectedNumTasks;
	}

	public void setTask(final Task newTask) {
		this.currentTaskNum++;	
		this.task = newTask;
		this.thisLog = LoggerFactory.getLogger(LOG_PREFIX + "." + newTask.getClass().getName());
	}

	public void open() {
		invokeOnEDT(() -> {
			synchronized(this) {
				if (dialog != null)
					return;
	
				dialog = new TaskDialog(parent, serviceRegistrar);
				dialog.addPropertyChangeListener(TaskDialog.CLOSE_EVENT, evt -> close());
				dialog.addPropertyChangeListener(TaskDialog.CANCEL_EVENT, evt -> cancel());
	
				if (firstTitle != null && firstTitle != title /* don't need to call firstTitle.equals() */)
					dialog.setTaskTitle(firstTitle);
				if (title != null)
					dialog.setTaskTitle(title);
	
				if (exception == null) {
					if (statusMessage != null) {
						if (statusMessageLevel == null)
							dialog.setStatus(null, null, statusMessage);
						else
							dialog.setStatus(
									GUIDefaults.getIconText(statusMessageLevel),
									GUIDefaults.getForeground(statusMessageLevel),
									statusMessage
							);
					}
					
					if (progress != 0)
						dialog.setPercentCompleted((float) progress);
				} else {
					dialog.setException(exception);
					exception = null;
				}
	
				dialog.setVisible(showDialog);
			}
		});
	}

	/**
	 * Used to toggle the monitor dialog so that when tunables
	 * are being displayed there are no Swing related threading 
	 * issues.
	 */
	public void showDialog(final boolean sd) {
		invokeOnEDT(() -> {
			showDialog = sd;
			
			if (dialog != null && dialog.isVisible() != showDialog) {
				if (showDialog == false) {
					dialog.dispose();
				} else {
					dialog.pack();
					dialog.setVisible(true);
				}
			}
		});
	}

	public void close() {
		invokeOnEDT(() -> {
			synchronized(this) {
				if (dialog != null) {
					dialog.dispose();
					dialog = null;
				}
				
				task = null;
			}
		});
	}

	public void cancel() {
		// we issue the Task's cancel method in its own thread
		// to prevent Swing from freezing if the Tasks's cancel
		// method takes too long to finish
		cancelled = true;
		Runnable cancel = () -> task.cancel();
		cancelExecutorService.submit(cancel);
		
		/* Do NOT close the dialog here; dialog closes when the task terminates itself after its cancel method is invoked */
	}

	protected boolean cancelled() {
		return cancelled;
	}

	@Override
	public void setTitle(final String title) {
		invokeOnEDT(() -> {
			if (this.firstTitle == null)
				this.firstTitle = title;

			this.title = title;
			history.setTitle(title);
			
			if (dialog != null)
				dialog.setTaskTitle(title);
		});
	}

	@Override
	public void setStatusMessage(final String statusMessage) {
		showMessage(TaskMonitor.Level.INFO, statusMessage);
	}

	@Override
	public void showMessage(TaskMonitor.Level level, String message) {
    showMessage(level, message, -2);
  }

	@Override
	public void showMessage(TaskMonitor.Level level, String message, int wait) {
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
		showStatusMessage(level, message, wait);
	}

	@Override
	public void setProgress(final double newProgress) {
		invokeOnEDT(() -> {
			if (newProgress < 0.0) {
				this.progress = -1.0;
				if (dialog != null)
					dialog.setPercentCompleted(-1.0f);
			} else {
				if (currentTaskNum < expectedNumTasks)
					this.progress = (newProgress * fractionOfOverall)
							+ ((double) currentTaskNum / (double) expectedNumTasks);
				else
					this.progress = newProgress;
				if (dialog != null)
					dialog.setPercentCompleted((float) this.progress);
			}
		});
	}

	public void showException(final Exception exception) {
		invokeOnEDT(() -> {
			// force the dialog box to be created if the Task throws an exception
			if (dialog == null) {
				this.exception = exception;
				open();
			} else {
				dialog.setException(exception);
			}
		});
	}

	public boolean isClosed() {
		return task == null;
	}

	private void showStatusMessage(final TaskMonitor.Level level, final String statusMessage, int wait) {
		invokeOnEDT(() -> {
			this.statusMessage = statusMessage;
			this.statusMessageLevel = level;
			
			if (dialog != null) {
        if (level.equals(TaskMonitor.Level.ERROR)) {
          dialog.setErrorStatus(statusMessage);
        } else if (level.equals(TaskMonitor.Level.WARN) && wait > 0) {
          dialog.setWarnStatus(statusMessage);
        } else {
          dialog.setStatus(
              GUIDefaults.getIconText(statusMessageLevel),
              GUIDefaults.getForeground(statusMessageLevel),
              statusMessage
          );
        }
      }
			
			history.addMessage(level, statusMessage);
		});

    if (level.equals(TaskMonitor.Level.WARN)) {
      if (wait == -2)
        waitForTime(1);
      else if (wait > 0)
        waitForTime(wait);
      else if (wait == -1) // Wait forever
        waitUntilClosed();
    } else if (level.equals(TaskMonitor.Level.ERROR)) {
      if (wait == -2)
        waitForTime(10);
      else if (wait == -1) // Wait forever
        waitUntilClosed();
      else if (wait > 0)
        waitForTime(wait);
    }
	}

	public String getFirstTitle() {
		return firstTitle;
	}

  private void waitForTime(int wait) {
    try {
      int count = wait*1000/500; // Don't wait forever...
      while (!isClosed()) {
        Thread.sleep(500);
        if (count-- <= 0) {
          break;
        }
      }
    } catch (Exception e) {}
  }

  private void waitUntilClosed() {
    try {
      while (!isClosed()) {
        Thread.sleep(500);
      }
    } catch (Exception e) {}
  }
}
