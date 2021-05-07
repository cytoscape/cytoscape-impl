package org.cytoscape.task.internal.networkobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTask;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public abstract class AbstractPropertyTask extends AbstractTask {
	
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected AbstractPropertyTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public VisualProperty<?> getProperty(CyNetwork network, CyIdentifiable target, String propertyName) {
		Class<? extends CyIdentifiable> type = DataUtils.getIdentifiableClass(target);
		
		if (!propertyName.startsWith(DataUtils.getIdentifiableType(type)))
			propertyName = DataUtils.getIdentifiableType(type)+" "+propertyName;
		
		CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		RenderingEngineManager reManager = serviceRegistrar.getService(RenderingEngineManager.class);
		
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		
		for (CyNetworkView view: views) {
			for (RenderingEngine<?> rEngine: reManager.getRenderingEngines(view)) {
				VisualLexicon lex = rEngine.getVisualLexicon();
				Set<VisualProperty<?>> vProps = lex.getAllVisualProperties();
				
				for (VisualProperty<?> prop: vProps) {
					if (propertyName.equalsIgnoreCase(prop.getDisplayName()))
						return prop;
				}
			}
		}
		
		throw new RuntimeException("Property "+propertyName+" doesn't exist");
	}

	protected class VisualPropertyObjectTuple {
		public VisualProperty<Object> visualProperty;
		public Object object;
		
		public VisualPropertyObjectTuple(VisualProperty<Object> visualProperty, Object object) {
			this.visualProperty = visualProperty;
			this.object = object;
		}
	}
	
	public static String getVisualPropertyJSON(VisualProperty<Object> vp, Object object) {
		StringBuilder output = new StringBuilder("{\n");
		output.append("   \"visualProperty\":\"" + vp.getIdString() + "\",\n");
		output.append("   \"value\":");
		final Class<?> type = vp.getRange().getType();

		if (type == String.class) {
			output.append("\"" + object.toString() + "\"");
		} else if (type == Boolean.class
				|| type == Double.class
				|| type == Integer.class
				|| type == Long.class
				|| type == Float.class
				) {
			output.append(object.toString());
		} else {
			output.append("\"" + vp.toSerializableString(object) + "\"");
		}
		output.append("\n}");
		return output.toString(); 
	}
	
	protected static String getVisualPropertiesJSON(Map<? extends CyIdentifiable, Map<String, VisualPropertyObjectTuple>> map) {
		StringBuilder output = new StringBuilder("[\n");
		int count =  map.size();
		for (Map.Entry<? extends CyIdentifiable, Map<String, VisualPropertyObjectTuple>> entry : map.entrySet()) {
			output.append("   {\"SUID\":" + entry.getKey().getSUID() + ",\n");
			output.append("    \"visualProperties\": [");
			int count2 = entry.getValue().size();
			for (Map.Entry<String, VisualPropertyObjectTuple> entry2 : entry.getValue().entrySet()) {
				output.append(getVisualPropertyJSON(entry2.getValue().visualProperty, entry2.getValue().object).replace((CharSequence)"   ", (CharSequence)"         "));
				if (count2 > 1) {
					output.append(",");
				}
				output.append("\n");
				count2--;
			}
			output.append("      ]");
			output.append("   }\n");
			if (count > 1) {
				output.append(",");
			}
			output.append("\n");
			count--;
		}
		output.append("]\n");
		
		return output.toString();
	}
	
	public Object getPropertyValue(CyNetwork network, CyIdentifiable target, VisualProperty vp) {
		CyNetworkView networkView = getViewForNetwork(network);
		Class<? extends CyIdentifiable> vpTargetType = vp.getTargetDataType();
		if (target instanceof CyNetwork) {
			if (vpTargetType != CyNetwork.class)
				throw new RuntimeException("Property '"+vp.getDisplayName()+"' not available for networks");
			return networkView.getVisualProperty(vp);
		} else if (target instanceof CyNode) {
			if (vpTargetType != CyNode.class)
				throw new RuntimeException("Property '"+vp.getDisplayName()+"' not available for nodes");
			View<CyNode> nodeView = networkView.getNodeView((CyNode)target);
			return nodeView.getVisualProperty(vp);
		} else if (target instanceof CyEdge) {
			if (vpTargetType != CyEdge.class)
				throw new RuntimeException("Property '"+vp.getDisplayName()+"' not available for edges");
			View<CyEdge> edgeView = networkView.getEdgeView((CyEdge)target);
			return edgeView.getVisualProperty(vp);
		} 
		return null;
	}

	public Object getStringPropertyValue(CyNetwork network, CyIdentifiable target, VisualProperty vp) {
		Object v = getPropertyValue(network, target, vp);
		return vp.toSerializableString(v);
	}

	public void setPropertyValue(CyNetwork network, CyIdentifiable target, VisualProperty vp, String value) {
		setPropertyValue(network, target, vp, value, false);
	}

	public void setPropertyValue(CyNetwork network, CyIdentifiable target, 
	                             VisualProperty vp, String value, boolean locked) {
		CyNetworkView networkView = getViewForNetwork(network);
		Class<? extends CyIdentifiable> vpTargetType = vp.getTargetDataType();

		if (target instanceof CyNetwork) {
			if (vpTargetType != CyNetwork.class)
				throw new RuntimeException("Property '"+vp.getDisplayName()+"' not available for networks");
			Object t =  vp.parseSerializableString(value);
			if (locked)
				networkView.setLockedValue(vp, t);
			else {
				if (networkView.isValueLocked(vp))
					networkView.clearValueLock(vp);
				networkView.setVisualProperty(vp, t);
			}
		} else if (target instanceof CyNode) {
			if (vpTargetType != CyNode.class)
				throw new RuntimeException("Property '"+vp.getDisplayName()+"' not available for nodes");
			View<CyNode> nodeView = networkView.getNodeView((CyNode)target);
			Object t =  vp.parseSerializableString(value);
			if (locked)
				nodeView.setLockedValue(vp, t);
			else {
				if (nodeView.isValueLocked(vp))
					nodeView.clearValueLock(vp);
				nodeView.setVisualProperty(vp, t);
			}
		} else if (target instanceof CyEdge) {
			if (vpTargetType != CyEdge.class)
				throw new RuntimeException("Property '"+vp.getDisplayName()+"' not available for edges");
			View<CyEdge> edgeView = networkView.getEdgeView((CyEdge)target);
			Object t =  vp.parseSerializableString(value);
			if (locked)
				edgeView.setLockedValue(vp, t);
			else {
				if (edgeView.isValueLocked(vp))
					edgeView.clearValueLock(vp);
				edgeView.setVisualProperty(vp, t);
			}
		} 
		return;
	}

	public List<String> listProperties(Class <? extends CyIdentifiable> type, CyNetwork network) {
		CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		RenderingEngineManager reManager = serviceRegistrar.getService(RenderingEngineManager.class);
		
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		List<String> propertyList = new ArrayList<>();
		
		for (CyNetworkView view: views) {
			for (RenderingEngine<?> rEngine: reManager.getRenderingEngines(view)) {
				VisualLexicon lex = rEngine.getVisualLexicon();
				
				for (VisualProperty<?> vp: lex.getAllVisualProperties()) {
					if (vp.getTargetDataType().equals(type)) {
						// Get rid of the redundant leading string
						String s = vp.getDisplayName();
						int x = s.indexOf(' ');
						propertyList.add(s.substring(x+1));
					}
				}
				
				return propertyList;
			}
		}
		
		return null;
	}

	private CyNetworkView getViewForNetwork(CyNetwork network) {
		CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		
		if (views == null || views.size() == 0)
			return null;

		for (CyNetworkView view : views)
			return view;

		return null;
	}

}
