package org.cytoscape.work.internal.task;


import java.awt.Window;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JPanel;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.GUITaskManager;
import org.cytoscape.work.swing.GUITunableInterceptor;
import org.cytoscape.work.TunableInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Swing components to create a user interface for the <code>Task</code>.
 *
 * This will not work if the application is running in headless mode.
 */
public class SwingTaskManager extends AbstractTaskManager implements GUITaskManager {

	private static final Logger logger = LoggerFactory.getLogger(SwingTaskManager.class);

	/**
	 * The delay between the execution of the <code>Task</code> and
	 * showing its task dialog.
	 *
	 * When a <code>Task</code> is executed, <code>SwingTaskManager</code>
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

	//
	//
	private List<TunableInterceptor> secondaryTunableInterceptors;

	/**
	 * Construct with default behavior.
	 * <ul>
	 * <li><code>owner</code> is set to null.</li>
	 * <li><code>taskExecutorService</code> is a cached thread pool.</li>
	 * <li><code>timedExecutorService</code> is a single thread executor.</li>
	 * <li><code>cancelExecutorService</code> is the same as <code>taskExecutorService</code>.</li>
	 * </ul>
	 */
	public SwingTaskManager(final GUITunableInterceptor tunableInterceptor) {
		super(tunableInterceptor);

		secondaryTunableInterceptors = new ArrayList<TunableInterceptor>();

		taskExecutorService = Executors.newCachedThreadPool();
		addShutdownHook(taskExecutorService);
		
		timedDialogExecutorService = Executors.newSingleThreadScheduledExecutor();
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
	 * @param owner JDialogs created by this <code>TaskManager</code>
	 * will have its owner set to this parameter.
	 */
	@Override 
	public void setParent(final Window parent) {
		this.parent = parent;
	}

	@Override
	public void setTunablePanel(final JPanel tunablePanel) {
		((GUITunableInterceptor)tunableInterceptor).setTunablePanel(tunablePanel);
	}

	@Override
	public void execute(final TaskFactory factory) {
		final SwingTaskMonitor taskMonitor = new SwingTaskMonitor(cancelExecutorService, parent);
		
		TaskIterator taskIterator;
		final Task first; 

		try {
			if ( tunableInterceptor.hasTunables(factory) && 
			    !tunableInterceptor.validateAndWriteBackTunables(factory))
				throw new IllegalArgumentException("Tunables are not valid");

			// pass the parent (Cytoscape desktop Window) for TunableDialog
			if (tunableInterceptor instanceof GUITunableInterceptor){
				GUITunableInterceptor guiTI = (GUITunableInterceptor)tunableInterceptor;				
				guiTI.setParent(parent);
			}
			
			taskIterator = factory.getTaskIterator();

			// Get the first task and display its tunables.  This is a bit of a hack.  
			// We do this outside of the thread so that the task monitor only gets
			// displayed AFTER the first tunables dialog gets displayed.
			first = taskIterator.next();
			if (!displayTunables(first))
				return;

		} catch (Exception exception) {
			taskIterator = null;
			logger.warn("Caught exception getting and validating task. ", exception);	
			taskMonitor.showException(exception);
			return;
		}

		// create the task thread
		final Runnable tasks = new TaskThread(first, taskMonitor, taskIterator); 

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
					taskMonitor.setFuture(executorFuture);
					taskMonitor.open();
				}
			}
		};

		timedDialogExecutorService.schedule(timedOpen, DELAY_BEFORE_SHOWING_DIALOG, DELAY_TIMEUNIT);
	}

	private class TaskThread implements Runnable {
		
		private final SwingTaskMonitor taskMonitor;
		private final TaskIterator taskIterator;
		private final Task first;

		TaskThread(final Task first, final SwingTaskMonitor tm, final TaskIterator ti) {
			this.first = first;
			this.taskMonitor = tm;
			this.taskIterator = ti;
		}
		
		public void run() {
			try {
				// actually run the first task 
				// don't dispaly the tunables here - they were handled above. 
				taskMonitor.setTask(first);
				first.run(taskMonitor);

				if (taskMonitor.cancelled())
					return;

				// now execute all subsequent tasks
				while (taskIterator.hasNext()) {
					final Task task = taskIterator.next();
					taskMonitor.setTask(task);

					if (!displayTunables(task))
						return;

					task.run(taskMonitor);

					if (taskMonitor.cancelled())
						break;
				}
			} catch (Exception exception) {
				logger.warn("Caught exception executing task. ", exception);
				taskMonitor.showException(exception);
			}

			// clean up the task monitor
			if (taskMonitor.isOpened() && !taskMonitor.isShowingException())
				taskMonitor.close();

		}
	}

	private boolean displayTunables(final Task task) throws Exception {
		if (tunableInterceptor == null)
			return true;

		// load the tunables from the object
		tunableInterceptor.loadTunables(task);

		// create the UI based on the object
		boolean ret = tunableInterceptor.execUI(task);

		// Run any additional tunable interceptors. In general, these
		// should NOT display a GUI.
		for ( TunableInterceptor ti : secondaryTunableInterceptors ) {
			ti.loadTunables(task);
			ti.execUI(task);
		}

		return ret;
	}

	@Override
	public JPanel getConfigurationPanel(final TaskFactory taskFactory) {
		tunableInterceptor.loadTunables(taskFactory);
		return ((GUITunableInterceptor)tunableInterceptor).getUI(taskFactory);
	}

	public void addTunableInterceptor(TunableInterceptor ti, Map props) {
		// The reason we compare strings instead of the objects directly is
		// that object provided here is actually a proxy object created by
		// Spring, not the actual object.  We should really find another
		// way of handling this.
		String existing = tunableInterceptor.toString();
		String tis = ti.toString();
		if ( ti != null && !tis.equals(existing) ) 
			secondaryTunableInterceptors.add(ti);
	}

	public void removeTunableInterceptor(TunableInterceptor ti, Map props) {
		if ( ti != null )
			secondaryTunableInterceptors.remove(ti);
	}	
}

