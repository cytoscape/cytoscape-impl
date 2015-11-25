package org.cytoscape.linkout.internal;

/*
 * #%L
 * Cytoscape Linkout Impl (linkout-impl)
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

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class NodeLinkoutTaskFactory extends AbstractNodeViewTaskFactory {

	private String link;
	private final OpenBrowser browser;

	public NodeLinkoutTaskFactory(final OpenBrowser browser, final String link) {
		super();
		this.link = link;
		this.browser = browser;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		return new TaskIterator(createTask(nodeView, netView));
	}
	
	private LinkoutTask createTask(View<CyNode> nodeView, CyNetworkView netView) {
		return new LinkoutTask(link, browser, netView.getModel(), nodeView.getModel());
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		if(!super.isReady(nodeView, netView))
			return false;
		LinkoutTask task = createTask(nodeView, netView);
		return task.isValidUrl();
	}
}
