package org.cytoscape.work.internal.sync;


import java.util.Map;

import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Uses Swing components to create a user interface for the <code>Task</code>.
 *
 * This will not work if the application is running in headless mode.
 */
public class SyncTaskManager extends AbstractTaskManager<Object, Map<String, Object>> implements
		SynchronousTaskManager<Object> {

	private static final Logger logger = LoggerFactory.getLogger(SyncTaskManager.class);


	private final SyncTunableMutator<?> syncTunableMutator;

	/**
	 * Construct with default behavior.
	 */
	public SyncTaskManager(final SyncTunableMutator<?> tunableMutator) {
		super(tunableMutator);
		this.syncTunableMutator = tunableMutator;
	}


	@Override 
	public void setExecutionContext(final Map<String,Object> o) {
		syncTunableMutator.setConfigurationContext(o);
	}


	@Override 
	public Object getConfiguration(TaskFactory factory, Object context) {
		throw new UnsupportedOperationException("There is no configuration available for a SyncrhonousTaskManager");	
	}


	@Override
	public void execute(final TaskIterator taskIterator) {
		final LoggingTaskMonitor taskMonitor = new LoggingTaskMonitor();
		
		try {
			while (taskIterator.hasNext()) {
				final Task task = taskIterator.next();
				taskMonitor.setTask(task);

				if (!displayTunables(task))
					return;

				task.run(taskMonitor);
			}

		} catch (Exception exception) {
			taskMonitor.showException(exception);
		}
	}

	private boolean displayTunables(final Object task) throws Exception {
		if (task == null) {
			return true;
		}
		boolean ret = syncTunableMutator.validateAndWriteBack(task);

		for ( TunableRecorder ti : tunableRecorders ) 
			ti.recordTunableState(task);

		return ret;
	}
}

