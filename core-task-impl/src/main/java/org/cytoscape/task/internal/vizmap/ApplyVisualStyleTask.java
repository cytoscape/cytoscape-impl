package org.cytoscape.task.internal.vizmap;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class ApplyVisualStyleTask extends AbstractNetworkViewCollectionTask {

	private final VisualMappingManager vmm;

	@ProvidesTitle
	public String getTitle() {
		return "Style to be applied";
	}

	@Tunable(description="Style:")
	public ListSingleSelection<VisualStyle> styles;

	public ApplyVisualStyleTask(final Collection<CyNetworkView> views, final VisualMappingManager vmm) {
		super(views);

		if (vmm == null)
			throw new NullPointerException("VisualMappingManager is null");
		this.vmm = vmm;

		final List<VisualStyle> vsList = new ArrayList<>(vmm.getAllVisualStyles());
		styles = new ListSingleSelection<>(vsList);
		if (!vsList.isEmpty())
			styles.setSelectedValue(vsList.get(0));
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);

		int i = 0;
		int viewCount = networkViews.size();
		final VisualStyle selected = styles.getSelectedValue();
		
		for (final CyNetworkView view : networkViews) {
			selected.apply(view);
			view.updateView();
			vmm.setVisualStyle(selected, view);

			i++;
			tm.setProgress((i / (double) viewCount));
		}

		tm.setProgress(1.0);
	}
}
