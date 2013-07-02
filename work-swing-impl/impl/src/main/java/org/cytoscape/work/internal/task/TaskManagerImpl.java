package org.cytoscape.work.internal.task;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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

import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.internal.tunables.JDialogTunableMutator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.TaskStatusPanelFactory;

public class TaskManagerImpl extends AbstractTaskManager<JDialog,Window> implements DialogTaskManager, TaskStatusPanelFactory {
	final TaskWindow taskWindow = new TaskWindow();
	final ExecutorService executor = Executors.newCachedThreadPool();
	final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	final JDialogTunableMutator dialogTunableMutator;
	final CyProperty<Properties> property;

	Window parentWindow;

	public TaskManagerImpl(final JDialogTunableMutator dialogTunableMutator, final CyProperty<Properties> property) {
		super(dialogTunableMutator);
		this.dialogTunableMutator = dialogTunableMutator;
		this.property = property;
		parentWindow = null;
		addShutdownHookForService(executor);
	}

	private static void addShutdownHookForService(final ExecutorService service) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				service.shutdownNow();
			}
		});
	}

	public void setExecutionContext(final Window parentWindow) {
		this.parentWindow = parentWindow;
	}

	public JDialog getConfiguration(TaskFactory f, Object tc) {
		throw new UnsupportedOperationException();
	}

	public void execute(TaskIterator iterator) {
		execute(iterator, null);
	}	

	public void execute(final TaskIterator iterator, Object tunableContext) {
		dialogTunableMutator.setConfigurationContext(parentWindow);
		final TaskRunner taskRunner = new TaskRunner(this, executor, scheduledExecutor, iterator, taskWindow);
		executor.submit(taskRunner);
	}

	public boolean showTunables(final Object task) throws Exception {
		if (task == null) {
			return true;
		}
		
		final boolean result = dialogTunableMutator.validateAndWriteBack(task);

		for (TunableRecorder ti : super.tunableRecorders) 
			ti.recordTunableState(task);

		return result;	
	}

	public void updateParent() {
		dialogTunableMutator.setConfigurationContext(parentWindow);
	}

	public void clearParent() {
		setExecutionContext(null);
		dialogTunableMutator.setConfigurationContext(null);
	}

	public JPanel createTaskStatusPanel() {
		return new TaskStatusBar(taskWindow);
	}
}

class TaskRunner implements Runnable {
	final TaskManagerImpl manager;
	final ExecutorService cancelExecutor;
	final ScheduledExecutorService scheduledExecutor;
	final TaskIterator iterator;
	final TaskMonitorImpl monitor;

	boolean cancelled = false;
	Task currentTask = null;

	public TaskRunner(final TaskManagerImpl manager, ExecutorService cancelExecutor, ScheduledExecutorService scheduledExecutor, TaskIterator iterator, TaskWindow window) {
		this.manager = manager;
		this.cancelExecutor = cancelExecutor;
		this.scheduledExecutor = scheduledExecutor;
		this.iterator = iterator;
		monitor = new TaskMonitorImpl(window);
		monitor.setCancelListener(new CancelListener());
	}

	public void run() {
		try {
			manager.updateParent();
			if (!iterator.hasNext())
				return;
			currentTask = iterator.next();
			if (!manager.showTunables(currentTask))
				return;
			final Future<?> showUILater = scheduledExecutor.schedule(new Runnable() {
				public void run() {
					monitor.showUI();
				}
			}, 1000L, TimeUnit.MILLISECONDS);
			currentTask.run(monitor);
			while (iterator.hasNext() && !cancelled) {
				currentTask = iterator.next();
				if (manager.showTunables(currentTask)) {
					currentTask.run(monitor);
				} else {
					cancelled = true;
				}
			}
			if (cancelled) {
				monitor.setAsCancelled();
			} else {
				monitor.setAsFinished();
			}
			showUILater.cancel(false);
		} catch (Exception e) {
			monitor.setAsExceptionOccurred(e);
			e.printStackTrace();
		} finally {
			manager.clearParent();
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

	final TaskWindow window;
	TaskUI ui = null;
	final int[] levelCounts = new int[NUM_LEVELS];

	String title = null;
	String secondaryTitle = null;
	double progress = -1.0;
	List<TaskMonitor.Level> messageLevels = new ArrayList<TaskMonitor.Level>();
	List<String> messages = new ArrayList<String>();
	ActionListener cancelListener = null;

	public TaskMonitorImpl(TaskWindow window) {
		this.window = window;
	}

	public void setTitle(final String newTitle) {
		if (title == null)
			this.title = newTitle;
		else
			secondaryTitle = newTitle;

		if (ui != null) {
			if (secondaryTitle == null)
				ui.setTitle(title);
			else
				ui.setTitle(String.format("<html>%s&nbsp;&nbsp;&nbsp;&nbsp;<font size=\"-1\">%s</font></html>", title, secondaryTitle));
		}
	}

	public void setProgress(double progress) {
		if (ui == null) {
			this.progress = progress;
		} else {
			ui.setProgress((float) progress);
		}
	}

	public void setStatusMessage(String message) {
		showMessage(TaskMonitor.Level.INFO, message);
	}

	public void showMessage(TaskMonitor.Level level, String message) {
		levelCounts[level.ordinal()]++;
		if (ui == null) {
			messageLevels.add(level);
			messages.add(message);
		} else {
			ui.addMessage(ICONS.get(level.toString().toLowerCase()), message);
		}
	}

	public void setAsFinished() {
		if (ui == null) {
			if (levelCounts[TaskMonitor.Level.WARN.ordinal()] + levelCounts[TaskMonitor.Level.ERROR.ordinal()] > 0)
				showUI();
			else
				return;
		}

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
	}

	public void setAsExceptionOccurred(final Exception exception) {
		if (ui == null)
			showUI();
		ui.addMessage(ICONS.get("error"), "Could not be completed: " + exception.getMessage());
		ui.setTaskAsCompleted();
		window.show();
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

		if (title != null) {
			setTitle(title);
		}

		if (progress >= 0.0) {
			setProgress(progress);
		}

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