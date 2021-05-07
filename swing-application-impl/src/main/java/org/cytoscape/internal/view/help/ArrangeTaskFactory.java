package org.cytoscape.internal.view.help;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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


import org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType;
import org.cytoscape.internal.view.CyDesktopManager;
import org.cytoscape.internal.view.NetworkViewMediator;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class ArrangeTaskFactory extends AbstractTaskFactory {

	private final ArrangeType arrange;
	private final CyDesktopManager desktopMgr;
	private final NetworkViewMediator netViewMediator;

	public ArrangeTaskFactory(final ArrangeType arrange, final CyDesktopManager desktopMgr,
			final NetworkViewMediator netViewMediator) {
		this.arrange = arrange;
		this.desktopMgr = desktopMgr;
		this.netViewMediator = netViewMediator;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ArrangeTask(desktopMgr, arrange));
	}
	
	@Override
	public boolean isReady() {
		return super.isReady() && !netViewMediator.getAllNetworkViewFrames().isEmpty();
	}
}
