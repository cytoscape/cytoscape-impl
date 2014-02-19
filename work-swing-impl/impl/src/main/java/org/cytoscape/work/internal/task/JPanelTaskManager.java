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


import javax.swing.JPanel;

import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
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
		dtm.execute(iterator, null, null);	
	}

	@Override
	public void execute(final TaskIterator iterator, final TaskObserver observer) {
		dtm.execute(iterator, null, observer);	
	}
}

