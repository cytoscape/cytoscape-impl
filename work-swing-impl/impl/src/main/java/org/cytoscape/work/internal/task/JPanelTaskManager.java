package org.cytoscape.work.internal.task;


import javax.swing.JPanel;

import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.internal.tunables.JPanelTunableMutator;
import org.cytoscape.work.swing.PanelTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public JPanel getConfiguration(TaskFactory factory, Object tunableContext) {
		return panelTunableMutator.buildConfiguration(tunableContext);
	}

	@Override
	public boolean validateAndApplyTunables(Object tunableContext) {
		return panelTunableMutator.validateAndWriteBack(tunableContext);
	}
	
	@Override
	public void setExecutionContext(final JPanel tunablePanel) {
		panelTunableMutator.setConfigurationContext(tunablePanel);
	}

	@Override
	public void execute(final TaskIterator iterator) {
		dtm.execute(iterator, null);	
	}
}

