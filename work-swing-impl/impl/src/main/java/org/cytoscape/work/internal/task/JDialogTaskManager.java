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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.internal.tunables.JDialogTunableMutator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Swing components to create a user interface for the <code>Task</code>.
 *
 * This will not work if the application is running in headless mode.
 */
public class JDialogTaskManager extends AbstractTaskManager<JDialog,Window> implements DialogTaskManager {

	private static final Logger logger = LoggerFactory.getLogger(JDialogTaskManager.class);

	/**
	 * The delay between the execution of the <code>Task</code> and
	 * showing its task dialog.
	 *
	 * When a <code>Task</code> is executed, <code>JDialogTaskManager</code>
	 * will not show its task dialog immediately. It will delay for a
	 * period of time before showing the dialog. This way, short lived
	 * <code>Task</code>s won't have a dialog box.
	 */
	static final long DELAY_BEFORE_SHOWING_DIALOG = 1;

	/**
	 * The time unit of <code>DELAY_BEFORE_SHOWING_DIALOG</code>.
	 */
	static final TimeUnit DELAY_TIMEUNIT = TimeUnit.SECONDS;
	
	/**
	 * The default size (in bytes) of the stack used to execute a Task.
	 */
	private final static long DEFAULT_STACK_SIZE = 10485760;
	
	/**
	 * The default Cytoscape property object.
	 */
	private final CyProperty<Properties> cyProperty;

	/**
	 * Display the user of the latest task information
	 */
	private final TaskStatusBar taskStatusBar;

	/**
	 * Record task history
	 */
	private final TaskHistory taskHistory;
	
	/**
	 * Used to create Threads for executed tasks.
	 */
	private TaskThreadFactory taskThreadFactory;
	
	/**
	 * Used for calling <code>Task.run()</code>.
	 */
	private ExecutorService taskExecutorService;

	/**
	 * Used for opening dialogs after a specific amount of delay.
	 */
	private ScheduledExecutorService timedDialogExecutorService;

	/**
	 * Used for calling <code>Task.cancel()</code>.
	 * <code>Task.cancel()</code> must be called in a different
	 * thread from the thread running Swing. This is done to
	 * prevent Swing from freezing if <code>Task.cancel()</code>
	 * takes too long to finish.
	 *
	 * This can be the same as <code>taskExecutorService</code>.
	 */
	private ExecutorService cancelExecutorService;

	// Parent component of Task Monitor GUI.
	private Window parent;

	// this is set with the first setExecutionContext value
	private Window initialParent;

	private final JDialogTunableMutator dialogTunableMutator;

	/**
	 * Construct with default behavior.
	 * <ul>
	 * <li><code>owner</code> is set to null.</li>
	 * <li><code>taskExecutorService</code> is a cached thread pool.</li>
	 * <li><code>timedExecutorService</code> is a single thread executor.</li>
	 * <li><code>cancelExecutorService</code> is the same as <code>taskExecutorService</code>.</li>
	 * </ul>
	 */
	public JDialogTaskManager(final JDialogTunableMutator tunableMutator, final CyProperty<Properties> cyProperty, final TaskStatusBar taskStatusBar, final TaskHistory taskHistory) {
		super(tunableMutator);
		this.dialogTunableMutator = tunableMutator;
		this.cyProperty = cyProperty;
		this.taskStatusBar = taskStatusBar;
		this.taskHistory = taskHistory;

		parent = null;
		initialParent = null;
		taskThreadFactory = new TaskThreadFactory();
		taskExecutorService = Executors.newCachedThreadPool(taskThreadFactory);
		addShutdownHook(taskExecutorService);
		
		timedDialogExecutorService = Executors.newSingleThreadScheduledExecutor(taskThreadFactory);
		addShutdownHook(timedDialogExecutorService);
		
		cancelExecutorService = taskExecutorService;
	}

	/**
	 * Adds a shutdown hook to the JVM that shuts down an
	 * <code>ExecutorService</code>. <code>ExecutorService</code>s
	 * need to be told to shut down, otherwise the JVM won't
	 * cleanly terminate.
	 */
	void addShutdownHook(final ExecutorService serviceToShutdown) {
		// Used to create a thread that is executed by the shutdown hook
		ThreadFactory threadFactory = Executors.defaultThreadFactory();

		Runnable shutdownHook = new Runnable() {
			public void run() {
				serviceToShutdown.shutdownNow();
			}
		};
		Runtime.getRuntime().addShutdownHook(threadFactory.newThread(shutdownHook));
	}

	/**
	 * @param parent JDialogs created by this TaskManager will use this 
	 * to set the parent of the dialog. 
	 */
	@Override 
	public void setExecutionContext(final Window parent) {
		this.parent = parent;
		if (initialParent == null) {
			initialParent = parent;
		}
	}

	@Override 
	public JDialog getConfiguration(TaskFactory factory, Object tunableContext) {
		throw new UnsupportedOperationException("There is no configuration available for a DialogTaskManager");	
	}


	@Override
	public void execute(final TaskIterator iterator) {
		execute(iterator, null, null);
	}

	@Override
	public void execute(final TaskIterator iterator, final TaskObserver observer) {
		execute(iterator, null, observer);
	}

	/**
	 * For users of this class.
	 */
	public void execute(final TaskIterator taskIterator, Object tunableContext, 
	                    final TaskObserver observer) {
		final TaskHistory.History history = taskHistory.newHistory();
		final SwingTaskMonitor taskMonitor = new SwingTaskMonitor(cancelExecutorService, parent, history);
		
		final Task first; 

		try {
			dialogTunableMutator.setConfigurationContext(parent,true);

			if ( tunableContext != null && !displayTunables(tunableContext) ) {
				taskMonitor.cancel();
				return;
			}

			taskMonitor.setExpectedNumTasks( taskIterator.getNumTasks() );

			// Get the first task and display its tunables.  This is a bit of a hack.  
			// We do this outside of the thread so that the task monitor only gets
			// displayed AFTER the first tunables dialog gets displayed.
			first = taskIterator.next();
			if (!displayTunables(first)) {
				taskMonitor.cancel();
                if (observer != null) observer.allFinished(FinishStatus.newCancelled(first));
				return;
			}

		} catch (Exception exception) {
			logger.warn("Caught exception getting and validating task. ", exception);	
			taskMonitor.showException(exception);
			return;
		}

		// create the task thread
		final Runnable tasks = new TaskRunnable(first, taskMonitor, taskIterator, observer, history); 

		// submit the task thread for execution
		final Future<?> executorFuture = taskExecutorService.submit(tasks);

		openTaskMonitorOnDelay(taskMonitor, executorFuture);
	}

	// This creates a thread on delay that conditionally displays the task monitor gui
	// if the task thread has not yet finished.
	private void openTaskMonitorOnDelay(final SwingTaskMonitor taskMonitor, 
	                                    final Future<?> executorFuture) {
		final Runnable timedOpen = new Runnable() {
			public void run() {
				if (!(executorFuture.isDone() || executorFuture.isCancelled())) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (!taskMonitor.isClosed()) {
								taskMonitor.open();
							}
						}
					});
				}
			}
		};

		timedDialogExecutorService.schedule(timedOpen, DELAY_BEFORE_SHOWING_DIALOG, DELAY_TIMEUNIT);
	}
	
	private class TaskThreadFactory implements ThreadFactory {

		int thread = 1;

		@Override
		public Thread newThread(Runnable r) {
			long stackSize;
			try {
				Properties props = cyProperty.getProperties();
				stackSize = Long.parseLong(props.getProperty("taskStackSize"));
			} catch (Exception e) {	
				stackSize = DEFAULT_STACK_SIZE;
			}
			return new Thread(null, r, String.format("Task-Thread-%d-Factory-0x%x", thread++, super.hashCode()), stackSize);
		}

	}
	
	private class TaskRunnable implements Runnable {
		
		private final SwingTaskMonitor taskMonitor;
		private final TaskIterator taskIterator;
		private final TaskObserver observer;
		private final TaskHistory.History history;
		private Task task;

		TaskRunnable(final Task first, final SwingTaskMonitor tm, final TaskIterator ti, 
		             final TaskObserver observer, TaskHistory.History history) {
			this.task = first;
			this.taskMonitor = tm;
			this.taskIterator = ti;
			this.observer = observer;
			this.history = history;
		}

		/**
		 * Loop through each task, show their tunables, execute them, and update the task observer.
		 * This is in its own method in order to allow cleanly exiting this method when dealing with 
		 * the first task.
		 */
		private void innerRun() throws Exception {
				// actually run the first task 
				// don't dispaly the tunables here - they were handled above. 
			taskMonitor.setTask(task);
			task.run(taskMonitor);
			handleObserver(task);

			if (taskMonitor.cancelled()) {
				if (observer != null) observer.allFinished(FinishStatus.newCancelled(task));
				return;
			}

				// now execute all subsequent tasks
			while (taskIterator.hasNext()) {
				task = taskIterator.next();
				taskMonitor.setTask(task);

				// hide the modal task dialog to prevent
				// the tunable dialog from being locked
				taskMonitor.showDialog(false);

				if (!displayTunables(task)) {
					if (observer != null) observer.allFinished(FinishStatus.newCancelled(task));
					return;
				}

				taskMonitor.showDialog(true);

				task.run(taskMonitor);
				handleObserver(task);

				if (taskMonitor.cancelled()) {
					if (observer != null) observer.allFinished(FinishStatus.newCancelled(task));
					return;
				}
			}
			if (observer != null) observer.allFinished(FinishStatus.getSucceeded());
		}
		
		public void run() {
			try {
				innerRun();
				taskMonitor.close();
				final boolean cancelled = taskMonitor.cancelled();
				taskStatusBar.setTitle(TaskDialog2.ICONS.get(cancelled ? "cancelled" : "finished"), taskMonitor.getFirstTitle());
				history.setCompletionStatus(cancelled ? TaskHistory.TASK_CANCELLED : TaskHistory.TASK_SUCCESS);
			} catch (Exception exception) {
				logger.warn("Caught exception executing task. ", exception);
				taskMonitor.showException(exception);
				taskStatusBar.setTitle(TaskDialog2.ICONS.get("error"), taskMonitor.getFirstTitle());
				history.setCompletionStatus(TaskHistory.TASK_FAILED);
				if (observer != null) observer.allFinished(FinishStatus.newFailed(task, exception));
			} finally {
				parent = initialParent;
				dialogTunableMutator.setConfigurationContext(null,true);
			}
		}

		private void handleObserver(Task t) {
			if (t instanceof ObservableTask && observer != null) {
				observer.taskFinished((ObservableTask)t);
			}
		}
	}

	private boolean displayTunables(final Object task) throws Exception {
		if (task == null) {
			return true;
		}
		
		boolean ret = dialogTunableMutator.validateAndWriteBack(task);

		for ( TunableRecorder ti : tunableRecorders ) 
			ti.recordTunableState(task);

		return ret;
	}

}

