package org.cytoscape.task.internal.vizmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cytoscape.model.json.CyJSONUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class ApplyVisualStyleTask extends AbstractNetworkViewCollectionTask implements ObservableTask {

	private final CyServiceRegistrar serviceRegistrar;

	@ProvidesTitle
	public String getTitle() {
		return "Apply Style";
	}

	@Tunable(
			description = "Style:",
			longDescription = "Name of Style to be applied to the selected views.",
			required = true
	)
	public ListSingleSelection<VisualStyle> styles;
	
	private VisualStyle selectedStyle;

	public ApplyVisualStyleTask(final Collection<CyNetworkView> views, CyServiceRegistrar serviceRegistrar) {
		super(views);
		this.serviceRegistrar = serviceRegistrar;

		VisualMappingManager vmManager = serviceRegistrar.getService(VisualMappingManager.class);
		
		final List<VisualStyle> vsList = new ArrayList<>(vmManager.getAllVisualStyles());
		styles = new ListSingleSelection<>(vsList);
		
		if (!vsList.isEmpty())
			styles.setSelectedValue(vsList.get(0));
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);

		VisualMappingManager vmManager = serviceRegistrar.getService(VisualMappingManager.class);

		int i = 0;
		int viewCount = networkViews.size();
		selectedStyle = styles.getSelectedValue();

		for (final CyNetworkView view : networkViews) {
			selectedStyle.apply(view);
			view.updateView();
			vmManager.setVisualStyle(selectedStyle, view);

			i++;
			tm.setProgress((i / (double) viewCount));
		}

		tm.setProgress(1.0);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (type == String.class) {
			String strRes = "";
			
			if (networkViews != null && !networkViews.isEmpty()) {
				strRes += "Style applied to views:\n";
				
				for (CyNetworkView view : networkViews)
					strRes += DataUtils.getViewTitle(view) + "\n";
				
				strRes = strRes.substring(0, strRes.length() - 1);
			} else {
				strRes = "Please select one or more views before applying a style.";
			}
			
			return strRes;
		}
		
		if (type == JSONResult.class) {
			String json = serviceRegistrar.getService(CyJSONUtil.class).cyIdentifiablesToJson(networkViews);
			JSONResult res = () -> { return json; };
			
			return res;
		}
		
		return networkViews != null ? new ArrayList<>(networkViews) : Collections.emptyList();
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}
}
