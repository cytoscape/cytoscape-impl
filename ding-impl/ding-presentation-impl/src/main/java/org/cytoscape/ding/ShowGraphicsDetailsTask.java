package org.cytoscape.ding;

import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

public class ShowGraphicsDetailsTask extends AbstractTask {

	private final CyNetworkView view;

	public ShowGraphicsDetailsTask(CyNetworkView view) {
		this.view = view;
	}
	
	@Override
	public void run(TaskMonitor tm) {
		if (DingRenderer.ID.equals(view.getRendererId())) {
			Boolean hd = view.getVisualProperty(DVisualLexicon.NETWORK_FORCE_HIGH_DETAIL);
			view.setLockedValue(DVisualLexicon.NETWORK_FORCE_HIGH_DETAIL, !hd);
		}
	}
}
