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
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableMutator;
import org.cytoscape.work.TunableRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.work.internal.tunables.JPanelTunableMutator;

/**
 * Uses Swing components to create a user interface for the <code>Task</code>.
 *
 * This will not work if the application is running in headless mode.
 */
public class JPanelTaskManager extends AbstractTaskManager<JPanel,JPanel> implements PanelTaskManager {

	private static final Logger logger = LoggerFactory.getLogger(JPanelTaskManager.class);
	private final JDialogTaskManager dtm;
	private final JPanelTunableMutator panelTunableMutator;

	/**
	 * Construct with default behavior.
	 * <ul>
	 * <li><code>owner</code> is set to null.</li>
	 * <li><code>taskExecutorService</code> is a cached thread pool.</li>
	 * <li><code>timedExecutorService</code> is a single thread executor.</li>
	 * <li><code>cancelExecutorService</code> is the same as <code>taskExecutorService</code>.</li>
	 * </ul>
	 */
	public JPanelTaskManager(final JPanelTunableMutator tunableMutator, JDialogTaskManager dtm) {
		super(tunableMutator);
		this.panelTunableMutator = tunableMutator;
		this.dtm = dtm;
	}

	@Override 
	public JPanel getConfiguration(TaskFactory tf) {
		return panelTunableMutator.buildConfiguration(tf);
	}

	@Override
	public void setExecutionContext(final JPanel tunablePanel) {
		panelTunableMutator.setConfigurationContext(tunablePanel);
	}

	@Override
	public void execute(final TaskFactory factory) {
		dtm.execute(factory,false);	
	}
}

