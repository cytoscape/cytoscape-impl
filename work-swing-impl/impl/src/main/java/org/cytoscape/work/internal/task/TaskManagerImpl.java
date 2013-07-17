package org.cytoscape.work.internal.task;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.internal.tunables.JDialogTunableMutator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.TaskStatusPanelFactory;

/**
 * Handles execution of tasks, cancellation of tasks,
 * and execution of tunables. Delegates UI to {@code TaskWindow}
 * and {@code TaskStatusBar}.
 */
public class TaskManagerImpl extends AbstractTaskManager<JDialog,Window> implements DialogTaskManager, TaskStatusPanelFactory {
	final TaskWindow taskWindow = new TaskWindow();
	final TaskStatusBar statusBar = new TaskStatusBar(taskWindow);

	/**
	 * Executes the task's {@code run} and {@code cancel} methods.
	 */
	final ExecutorService executor;

	/**
	 * Used for showing the UI for long running tasks.
	 */
	final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	/**
	 * Tunable stuff.
	 */
	final JDialogTunableMutator dialogTunableMutator;

	/**
	 * Tunable stuff.
	 */
	Window parentWindow;

	/**
	 * We need to refer to the latest task monitor so that its updates are shown in the {@code statusBar}.
	 * We wrap this in a {@code WeakReference} because we want the latest monitor to be
	 * garbage collected after the task has completed.
	 */
	WeakReference<TaskMonitorImpl> latestMonitor = null;

	public TaskManagerImpl(final JDialogTunableMutator dialogTunableMutator, final CyProperty<Properties> property) {
		super(dialogTunableMutator);
		executor = Executors.newCachedThreadPool(new TaskThreadFactory(property));
		this.dialogTunableMutator = dialogTunableMutator;
		parentWindow = null;
		addShutdownHookForService(executor);
		addShutdownHookForService(scheduledExecutor);
	}

	/**
	 * When the JVM exits, we shut down our executors.
	 */
	private static void addShutdownHookForService(final ExecutorService service) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				service.shutdownNow();
			}
		});
	}

	/**
	 * Tunable stuff.
	 */
	public void setExecutionContext(final Window parentWindow) {
		this.parentWindow = parentWindow;
	}

	/**
	 * Tunable stuff.
	 */
	public JDialog getConfiguration(TaskFactory f, Object tc) {
		throw new UnsupportedOperationException();
	}

	public void execute(TaskIterator iterator) {
		execute(iterator, null, null);
	}	

	public void execute(TaskIterator iterator, TaskObserver observer) {
		execute(iterator, null, observer);
	}

	public void execute(final TaskIterator iterator, Object tunableContext, TaskObserver observer) {
		dialogTunableMutator.setConfigurationContext(parentWindow);
		final TaskRunner taskRunner = new TaskRunner(this, executor, scheduledExecutor, iterator, 
		                                             observer, taskWindow);
		executor.submit(taskRunner);
	}

	/**
	 * Handy method for showing the tunables for a given task.
	 * This method will return once the user clicks "OK" or "Cancel"
	 * in the tunable dialog.
	 * @returns true if the user clicked "OK"
	 */
	public boolean showTunables(final Object task) throws Exception {
		if (task == null) {
			return true;
		}
		
		final boolean result = dialogTunableMutator.validateAndWriteBack(task);

		for (TunableRecorder ti : super.tunableRecorders) 
			ti.recordTunableState(task);

		return result;	
	}

	/**
	 * Needed by the tunable dialog. This method gets called by the task's
	 * {@code TaskRunner} before making calls to {@code showTunables}.
	 */
	public void updateParent() {
		dialogTunableMutator.setConfigurationContext(parentWindow);
	}

	/**
	 * Needed by the tunable dialog. This method gets called by the task's
	 * {@code TaskRunner} after finishing making calls to {@code showTunables}.
	 */
	public void clearParent() {
		setExecutionContext(null);
		dialogTunableMutator.setConfigurationContext(null);
	}

	/**
	 * Used to implement the {@code TaskStatusPanelFactory} interface.
	 */
	public JPanel createTaskStatusPanel() {
		return statusBar;
	}

	/**
	 * Called by {@code TaskMonitorImpl}, when it's time for it
	 * to show its UI, to "register" itself as the latest
	 * task that's being executed.
	 */
	public void setLatestMonitor(TaskMonitorImpl monitor) {
		if (latestMonitor != null) {
			statusBar.resetStatusBar();
			latestMonitor.clear();
			latestMonitor = null;
		}
		latestMonitor = new WeakReference<TaskMonitorImpl>(monitor);
	}

	/**
	 * Used by {@code TaskMonitorImpl} to see if its task is the
	 * latest task that's being executed.
	 */
	public boolean isLatestMonitor(TaskMonitorImpl monitor) {
		return latestMonitor.get() == monitor;
	}

	/**
	 * While this does the same thing as {@code createTaskStatusPanel},
	 * it's used by {@code TaskMonitorImpl} to manipulate the task status bar
	 * only when it's the latest executing task.
	 */
	public TaskStatusBar getStatusBar() {
		return statusBar;
	}
}

/**
 * This class actually executes the task and responds to
 * the user clicking the cancel button.
 */
class TaskRunner implements Runnable {
	static final long TASK_UI_SHOW_DELAY_MS = 1000L;
	final TaskManagerImpl manager;

	/**
	 * Used to invoke the task's cancel method.
	 */
	final ExecutorService cancelExecutor;

	/**
	 * Used to show the task's UI if it's a long running task.
	 */
	final ScheduledExecutorService scheduledExecutor;
	final TaskIterator iterator;
	final TaskMonitorImpl monitor;
	final TaskObserver observer;

	boolean cancelled = false;
	Task currentTask = null;

	public TaskRunner(final TaskManagerImpl manager, ExecutorService cancelExecutor, ScheduledExecutorService scheduledExecutor, 
	                  TaskIterator iterator, TaskObserver observer, TaskWindow window) {
		this.manager = manager;
		this.cancelExecutor = cancelExecutor;
		this.scheduledExecutor = scheduledExecutor;
		this.iterator = iterator;
		this.observer = observer;
		monitor = new TaskMonitorImpl(manager, window);
		monitor.setCancelListener(new CancelListener());
	}

	public void run() {
		try {
			if (!iterator.hasNext())
				return;
			// Get the first task in the iterator, then show its tunables.
			// If the user clicks cancel, exit this method and never show a UI for this task.
			currentTask = iterator.next();
			manager.updateParent();
			if (!manager.showTunables(currentTask))
				return;

			// Create a UI for the task if its been executing longer than 1 second.
			final Future<?> showUILater = scheduledExecutor.schedule(new Runnable() {
				public void run() {
					monitor.showUI();
				}
			}, TASK_UI_SHOW_DELAY_MS, TimeUnit.MILLISECONDS);

			// Record the task in the montor (for logging purposes)
			monitor.setTask(currentTask);

			// Run the first task
			currentTask.run(monitor);
			handleObserver(currentTask);

			// run all subsequent tasks in the iterator
			while (iterator.hasNext() && !cancelled) {
				currentTask = iterator.next();
				if (manager.showTunables(currentTask)) {
					// Record the task in the montor (for logging purposes)
					monitor.setTask(currentTask);
					currentTask.run(monitor);
					handleObserver(currentTask);
				} else {
					// if the user clicked "cancel" for the tunable dialog,
					// tell the task's UI that the task has been cancelled.
					cancelled = true;
				}
			}
			if (cancelled) {
				monitor.setAsCancelled();
			} else {
				monitor.setAsFinished();
			}

			// The task has finished successfully. If the task
			// is short lived (it ran under a second), don't show
			// the task's UI.
			showUILater.cancel(false);
		} catch (Exception e) {
			monitor.setAsExceptionOccurred(e);
			e.printStackTrace();
		} finally {
			manager.clearParent();
			if (observer != null) 
				observer.allFinished();
		}
	}

	private void handleObserver(Task task) {
		if (task instanceof ObservableTask && observer != null) {
			observer.taskFinished((ObservableTask)task);
		}
	}

	class CancelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			cancelled = true;
			monitor.setAsCancelling();
			if (currentTask != null) {
				cancelExecutor.submit(new Runnable() {
					public void run() {
						currentTask.cancel();
					}
				});
			}
		}
	}
}

/**
 * Acts as a bridge between {@code TaskRunner} and {@code TaskUI}
 * and {@code TaskStatusBar}.
 * Method calls are stored internally until {@code showUI} is invoked.
 * For example, {@code setTitle} does not have any effect on the task's UI
 * until {@code showUI} is invoked. This class updates the task's UI to the
 * last set title when {@code showUI} is invoked. This mechanism allows
 * short-living tasks to invoke methods on the monitor without affecting the UI.
 */
class TaskMonitorImpl implements TaskMonitor {
	public static final Map<String,URL> ICON_URLS = new HashMap<String,URL>();
	static {
		ICON_URLS.put("info", TaskRunner.class.getResource("/images/info-icon.png"));
		ICON_URLS.put("warn", TaskRunner.class.getResource("/images/warn-icon.png"));
		ICON_URLS.put("error", TaskRunner.class.getResource("/images/error-icon.png"));
		ICON_URLS.put("finished", TaskRunner.class.getResource("/images/finished-icon.png"));
		ICON_URLS.put("cancelled", TaskRunner.class.getResource("/images/cancelled-icon.png"));
	}

	public static final Map<String,Icon> ICONS = new HashMap<String,Icon>();
	static {
		for (Map.Entry<String,URL> iconURL : ICON_URLS.entrySet()) {
			ICONS.put(iconURL.getKey(), new ImageIcon(iconURL.getValue()));
		}
	}

	private static int NUM_LEVELS = TaskMonitor.Level.values().length;

	final TaskManagerImpl manager;
	final TaskWindow window;
	Task thisTask;

	/**
	 * This becomes a valid TaskUI after {@code showUI} is invoked.
	 */
	TaskUI ui = null;

	/**
	 * Counts the number of info, warning, and error messages shown.
	 * This is used for when the task is finished to show a summary
	 * and to pop open the task window if the task had any errors or warnings.
	 */
	final int[] levelCounts = new int[NUM_LEVELS];

	/**
	 * Used to cache the last {@code setTitle} invocation
	 */
	String title = null;

	/**
	 * Also used to cache the last {@code setTitle} invocation,
	 * but typically secondary titles are filled in by
	 * task iterators with more than one task.
	 */
	String secondaryTitle = null;

	/**
	 * Used to cache the last {@code setProgress} invocation.
	 */
	double progress = -1.0;

	/**
	 * Used to cache the last {@code showMessage} invocation.
	 */
	List<TaskMonitor.Level> messageLevels = new ArrayList<TaskMonitor.Level>();

	/**
	 * Used to cache the last {@code showMessage} invocation.
	 */
	List<String> messages = new ArrayList<String>();

	/**
	 * Used to cache the last {@code setCancelListener} invocation.
	 */
	ActionListener cancelListener = null;

	/**
 	 * We log all messages to our log channel.  This allows interested listeners
 	 * to "hear" them.
 	 */
	Logger thisLog = null;

	/**
 	 * This is the root of the log we log to
 	 */
	private static final String LOG_PREFIX = "TaskMonitor";

	public TaskMonitorImpl(final TaskManagerImpl manager, final TaskWindow window) {
		this.manager = manager;
		this.window = window;
		this.thisLog = LoggerFactory.getLogger(LOG_PREFIX);
	}

	public void setTask(final Task newTask) {
		this.thisTask = newTask;
		this.thisLog = LoggerFactory.getLogger(LOG_PREFIX+"."+newTask.getClass().getName());
	}

	public void setTitle(final String newTitle) {
		if (title == null)
			this.title = newTitle;
		else
			secondaryTitle = newTitle;

        if (title != null && secondaryTitle != null && title.equals(secondaryTitle))
            secondaryTitle = null; // don't show a 2ary title if the main title's the same

		if (ui != null) {
			String titleString = title;
			if (secondaryTitle != null)
				titleString = String.format("<html>%s<br><br><font size=\"-2\">%s</font></html>", title, secondaryTitle);
			ui.setTitle(titleString);

			if (manager.isLatestMonitor(this)) {
				manager.getStatusBar().setTitle(title);
			}
		}
	}

	public void setProgress(double progress) {
		if (ui == null) {
			this.progress = progress;
		} else {
			ui.setProgress((float) progress);
			if (manager.isLatestMonitor(this)) {
				manager.getStatusBar().setProgress((float) progress);
			}
		}
	}

	public void setStatusMessage(String message) {
		showMessage(TaskMonitor.Level.INFO, message);
	}

	public void showMessage(TaskMonitor.Level level, String message) {
		levelCounts[level.ordinal()]++;

		// Log the message
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

		if (ui == null) {
			messageLevels.add(level);
			messages.add(message);
		} else {
			ui.addMessage(ICONS.get(level.toString().toLowerCase()), message);
		}
	}

	/**
	 * Did the task have at least one error or warning?
	 */
	private boolean warningsOrErrors() {
		return levelCounts[TaskMonitor.Level.WARN.ordinal()] + levelCounts[TaskMonitor.Level.ERROR.ordinal()] > 0;
	}

	public void setAsFinished() {
		if (ui == null) {
			if (warningsOrErrors()) {
				showUI(); // force the UI to show for even short-living tasks if an error or warning occurred
			} else {
				return; // we had a short-living task with no errors or warnings, so don't show a UI
			}
		}

		// Create a "Finished" status message with the number of infos, warnings, and errors.
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<html>Finished.&nbsp;&nbsp;");
		for (final TaskMonitor.Level level : TaskMonitor.Level.values()) {
			final int levelCount = levelCounts[level.ordinal()];
			if (levelCount == 0)
				continue;
			buffer.append("&nbsp;&nbsp;");
			buffer.append("<img align=\"baseline\" src=\"");
			buffer.append(ICON_URLS.get(level.toString().toLowerCase()));
			buffer.append("\">");
			buffer.append("&nbsp;");
			buffer.append(levelCount);
		}
		buffer.append("</html>");
		ui.addMessage(ICONS.get("finished"), buffer.toString());
		ui.setTaskAsCompleted();

		if (warningsOrErrors()) {
			window.show(); // Force the task window to open if there's been an error or warning.
		}

		if (manager.isLatestMonitor(this)) {
			manager.getStatusBar().setTitleIcon(ICONS.get("finished"));
			manager.getStatusBar().hideProgress();
		}
	}

	public void setAsCancelling() {
		ui.disableCancelButton();
		ui.setCancelStatus("Cancelling");
	}

	public void setAsCancelled() {
		if (ui == null)
			showUI();
		ui.addMessage(ICONS.get("cancelled"), "Cancelled.");
		ui.setTaskAsCompleted();

		if (manager.isLatestMonitor(this)) {
			manager.getStatusBar().setTitleIcon(ICONS.get("cancelled"));
			manager.getStatusBar().hideProgress();
		}
	}

	public void setAsExceptionOccurred(final Exception exception) {
		if (ui == null)
			showUI();
		ui.addMessage(ICONS.get("error"), "Could not be completed: " + exception.getMessage());
		ui.setTaskAsCompleted();
		window.show();

		if (manager.isLatestMonitor(this)) {
			manager.getStatusBar().setTitleIcon(ICONS.get("error"));
			manager.getStatusBar().hideProgress();
		}
	}

	public void setCancelListener(ActionListener listener) {
		if (ui == null) {
			cancelListener = listener;
		} else {
			ui.addCancelListener(listener);
		}
	}

	public synchronized void showUI() {
		if (ui != null)
			return;

		ui = window.createTaskUI();
		manager.setLatestMonitor(this); // we're the latest task! we should start updating the task status bar

		// update the UI with cached values

		if (title != null) {
			setTitle(title);
		}

        setProgress(progress);

		for (int i = 0; i < messages.size(); i++) {
			final TaskMonitor.Level level = messageLevels.get(i);
			final String message = messages.get(i);
			showMessage(level, message);
		}
		messages.clear();
		messages = null;
		messageLevels.clear();
		messageLevels = null;

		if (cancelListener != null) {
			setCancelListener(cancelListener);
			cancelListener = null;
		}
	}
}
