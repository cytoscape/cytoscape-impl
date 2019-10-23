package org.cytoscape.task.internal.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

public class DestroyNetworkViewTask extends AbstractNetworkViewCollectionTask implements ObservableTask {

	@Tunable(
			description = "Deprecated",
			longDescription = "Deprecated since version 3.6.",
			context = "nogui"
	)
	@Deprecated
	public boolean destroyCurrentNetworkView = true;
	
	@Tunable(
			description = "<html>The selected views will be lost.<br />Do you want to continue?</html>",
			params = "ForceSetDirectly=true;ForceSetTitle=Destroy Views",
			context = "gui"
	)
	public boolean confirm = true;

	private final Map<Long, String> destroyedSUIDs = new LinkedHashMap<>();
	
	private final CyServiceRegistrar serviceRegistrar;

	public DestroyNetworkViewTask(Collection<CyNetworkView> views, CyServiceRegistrar serviceRegistrar) {
		super(views);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		if (confirm && destroyCurrentNetworkView) { // Also checks destroyCurrentNetworkView for backwards compatibility
			tm.setTitle("Destroy Network View(s)");
			tm.setProgress(0.0);
			
			CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
			int i = 0;
			int viewCount = networkViews.size();
			
			for (final CyNetworkView nv : networkViews) {
				if (cancelled)
					return;
				
				Long suid = nv.getSUID();
				String title = DataUtils.getViewTitle(nv);
				viewManager.destroyNetworkView(nv);
				destroyedSUIDs.put(suid, title);
				
				i++;
				tm.setProgress(i / (double) viewCount);
			}
			
			tm.setProgress(1.0);
		}
	}
	
	/**
	 * Returns the SUID's of destroyed views.
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (type == String.class) {
			StringBuilder sb = new StringBuilder();
			
			if (destroyedSUIDs != null && !destroyedSUIDs.isEmpty()) {
				sb.append("Destroyed views:\n");
				
				destroyedSUIDs.forEach((suid, title) -> {
					sb.append(title + " (SUID: " + suid + ")" + "\n");
				});
				
				sb.substring(0, sb.length() - 1);
			} else {
				sb.append("No views were destroyed.");
			}
			
			return sb.toString();
		}
		
		if (type == JSONResult.class) {
			JsonArray jsonArr = new JsonArray();
			JsonObject jsonObject = new JsonObject();
			
			if (destroyedSUIDs != null && !destroyedSUIDs.isEmpty()) {
				destroyedSUIDs.keySet().forEach(suid -> {
					jsonArr.add(new JsonPrimitive(suid));
				});
			}

			jsonObject.add("views", jsonArr);
			
			String json = new Gson().toJson(jsonObject);
			JSONResult res = () -> { return json; };
			
			return res;
		}
		
		return destroyedSUIDs != null ? new ArrayList<>(destroyedSUIDs.keySet()) : Collections.emptyList();
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}
}
