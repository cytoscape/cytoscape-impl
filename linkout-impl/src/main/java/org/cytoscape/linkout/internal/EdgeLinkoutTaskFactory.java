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

import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class EdgeLinkoutTaskFactory extends AbstractEdgeViewTaskFactory {

	private String link;
	private final OpenBrowser browser;

	public EdgeLinkoutTaskFactory(final OpenBrowser browser, final String link) {
		super();
		this.link = link;
		this.browser = browser;
	}

	@Override
	public TaskIterator createTaskIterator(final View<CyEdge> edgeView, final CyNetworkView netView) {
		return new TaskIterator(
				new LinkoutTask(link, browser, netView.getModel(), edgeView.getModel().getSource(),
				edgeView.getModel().getTarget(), edgeView.getModel()));
	}

	public String getLink() {
		return link;
	}

	public void setLink(final String link) {
		this.link = link;
	}
}
