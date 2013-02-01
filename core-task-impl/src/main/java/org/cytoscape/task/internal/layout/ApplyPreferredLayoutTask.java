package org.cytoscape.task.internal.layout;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.util.Collection;
import java.util.Properties;

import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ApplyPreferredLayoutTask extends AbstractNetworkViewCollectionTask {

	private static final String DEF_LAYOUT = "force-directed";

	private Properties props;
	private final CyLayoutAlgorithmManager layouts;

	public ApplyPreferredLayoutTask(final Collection<CyNetworkView> networkViews,
			final CyLayoutAlgorithmManager layouts, final Properties props) {
		super(networkViews);
		this.layouts = layouts;
		this.props = props;
	}

	public ApplyPreferredLayoutTask(final Collection<CyNetworkView> networkViews, final CyLayoutAlgorithmManager layouts) {
		super(networkViews);
		this.layouts = layouts;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setProgress(0.0d);
		tm.setStatusMessage("Applying Default Layout...");

		int i = 0;
		int viewCount = networkViews.size();
		for (final CyNetworkView view : networkViews) {
			String pref = CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME;
			if (props != null)
				pref = props.getProperty("preferredLayoutAlgorithm", DEF_LAYOUT);
			tm.setProgress(0.2d);
			final CyLayoutAlgorithm layout = layouts.getLayout(pref);
			if (layout != null) {
				//clearEdgeBends(view);
				final TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
						CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
				insertTasksAfterCurrentTask(itr);

			} else {
				throw new IllegalArgumentException("Couldn't find layout algorithm: " + pref);
			}

			i++;
			tm.setProgress((i / (double) viewCount));
		}

		tm.setProgress(1.0);
	}

	/**
	 * Clears edge bend values ASSIGNED TO EACH EDGE. Default Edge Bend value
	 * will not be cleared.
	 * 
	 * TODO: should we clear mapping, too?
	 */
	private final void clearEdgeBends(final CyNetworkView networkView) {
		final Collection<View<CyEdge>> edgeViews = networkView.getEdgeViews();
		if (edgeViews.isEmpty())
			return;

		final View<CyEdge> first = edgeViews.iterator().next();
		if (first.isSet(BasicVisualLexicon.EDGE_BEND) == false)
			return;

		for (final View<CyEdge> edgeView : edgeViews) {
			edgeView.setVisualProperty(BasicVisualLexicon.EDGE_BEND, null);
			edgeView.clearValueLock(BasicVisualLexicon.EDGE_BEND);
		}
	}
}
