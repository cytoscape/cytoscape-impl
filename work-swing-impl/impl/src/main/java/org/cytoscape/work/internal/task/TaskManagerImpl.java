package org.cytoscape.work.internal.task;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
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

public class TaskManagerImpl extends AbstractTaskManager<JDialog,Window> implements DialogTaskManager {
	final TaskWindow taskWindow = new TaskWindow();
	final ExecutorService executor = Executors.newCachedThreadPool();

	final JDialogTunableMutator dialogTunableMutator;
	final CyProperty<Properties> property;

	Window parentWindow;

	public TaskManagerImpl(final JDialogTunableMutator dialogTunableMutator, final CyProperty<Properties> property) {
		super(dialogTunableMutator);
		this.dialogTunableMutator = dialogTunableMutator;
		this.property = property;
		parentWindow = null;
		addShutdownHookForService(executor);
		taskWindow.show();
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
		final TaskRunner taskRunner = new TaskRunner(this, executor, iterator, taskWindow);
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
}

class TaskRunner implements Runnable {
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

	final TaskManagerImpl manager;
	final ExecutorService cancelExecutor;
	final TaskIterator iterator;
	final TaskUI ui;

	boolean cancelled = false;
	Task currentTask = null;

	public TaskRunner(final TaskManagerImpl manager, ExecutorService cancelExecutor, TaskIterator iterator, TaskWindow window) {
		this.manager = manager;
		this.cancelExecutor = cancelExecutor;
		this.iterator = iterator;
		ui = window.createTaskUI();
		ui.addCancelListener(new CancelListener());
	}

	public void run() {
		final TaskMonitorImpl monitor = new TaskMonitorImpl(ui);
		try {
			manager.updateParent();
			while (iterator.hasNext() && !cancelled) {
				currentTask = iterator.next();
				if (manager.showTunables(currentTask)) {
					currentTask.run(monitor);
				} else {
					cancelled = true;
				}
			}
			if (cancelled) {
				ui.addMessage(ICONS.get("cancelled"), "Cancelled.");
			} else {
				ui.addMessage(ICONS.get("finished"), buildFinishedString(monitor));
			}
		} catch (Exception e) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Could not be completed: " + e.getMessage());
			e.printStackTrace();
		} finally {
			manager.clearParent();
		}
		ui.setTaskAsCompleted();
	}

	private String buildFinishedString(final TaskMonitorImpl monitor) {
		final int[] levelCounts = monitor.getLevelCounts();
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<html>Finished.&nbsp;&nbsp;");
		for (final TaskMonitor.Level level : TaskMonitor.Level.values()) {
			final int levelCount = levelCounts[level.ordinal()];
			if (levelCount == 0)
				continue;
			buffer.append("&nbsp;&nbsp;");
			buffer.append("<img align=\"baseline\" src=\"");
			buffer.append(ICONS.get(level.toString().toLowerCase()));
			buffer.append("\">");
			buffer.append("&nbsp;");
			buffer.append(levelCount);
		}
		buffer.append("</html>");
		return buffer.toString();
	}

	class CancelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ui.disableCancelButton();
			ui.setCancelStatus("Cancelling");
			cancelled = true;
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
	private static int NUM_LEVELS = TaskMonitor.Level.values().length;

	final TaskUI ui;
	final int[] levelCounts = new int[NUM_LEVELS];

	public TaskMonitorImpl(TaskUI ui) {
		this.ui = ui;
	}

	public void setTitle(final String title) {
		ui.setTitle(title);
	}

	public void setProgress(double progress) {
		ui.setProgress((float) progress);
	}

	public void setStatusMessage(String message) {
		showMessage(TaskMonitor.Level.INFO, message);
	}

	public void showMessage(TaskMonitor.Level level, String message) {
		levelCounts[level.ordinal()]++;
		ui.addMessage(TaskRunner.ICONS.get(level.toString().toLowerCase()), message);
	}

	public int[] getLevelCounts() {
		return levelCounts;
	}
}