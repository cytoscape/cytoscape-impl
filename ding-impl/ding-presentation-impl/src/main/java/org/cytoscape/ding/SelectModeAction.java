package org.cytoscape.ding;

import static org.cytoscape.ding.DVisualLexicon.*;

import java.awt.event.ActionEvent;

import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;

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

@SuppressWarnings("serial")
public class SelectModeAction extends AbstractCyAction {

	public static final String NODES = "Nodes Only";
	public static final String EDGES = "Edges Only";
	public static final String ANNOTATIONS = "Annotations Only";
	public static final String NODES_EDGES = "Nodes and Edges";
	public static final String NODE_LABELS = "Node Labels Only";
	public static final String ALL = "All";
	
	private final CyServiceRegistrar serviceRegistrar;

	public SelectModeAction(final String name, float gravity, final CyServiceRegistrar serviceRegistrar) {
		super(name);
		this.serviceRegistrar = serviceRegistrar;
		
		useCheckBoxMenuItem = true;
		setPreferredMenu("Select.Mouse Drag Selects[1]");
		setMenuGravity(gravity);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkView view = applicationManager.getCurrentNetworkView();

		if (view != null) {
			final String name = this.name;
			view.batch(nv -> {
				if (name.equalsIgnoreCase(NODES)) {
					view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.TRUE);
					view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_NODE_LABEL_SELECTION, Boolean.FALSE);
				} else if (name.equalsIgnoreCase(EDGES)) {
					view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.TRUE);
					view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_NODE_LABEL_SELECTION, Boolean.FALSE);
				} else if (name.equalsIgnoreCase(ANNOTATIONS)) {
					view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.TRUE);
					view.setLockedValue(NETWORK_NODE_LABEL_SELECTION, Boolean.FALSE);
				} else if (name.equalsIgnoreCase(NODES_EDGES)) {
					view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.TRUE);
					view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.TRUE);
					view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_NODE_LABEL_SELECTION, Boolean.FALSE);
				} else if (name.equalsIgnoreCase(NODE_LABELS)) {
					view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.FALSE);
					view.setLockedValue(NETWORK_NODE_LABEL_SELECTION, Boolean.TRUE);
				} else if (name.equalsIgnoreCase(ALL)) {
					view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.TRUE);
					view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.TRUE);
					view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.TRUE);
					view.setLockedValue(NETWORK_NODE_LABEL_SELECTION, Boolean.TRUE);
				}
			}, false); // don't set the dirty flag, don't want to force redraw for this
		} 
	}
	
	@Override
	public boolean isEnabled() {
		CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		
		if (view == null)
			return false;
		
		NetworkViewRenderer renderer = applicationManager.getNetworkViewRenderer(view.getRendererId());
		RenderingEngineFactory<CyNetwork> factory = renderer == null ? null
				: renderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT);
		VisualLexicon lexicon = factory == null ? null : factory.getVisualLexicon();
		
		if (lexicon == null)
			return false; // Should never happen!
		
		// At least the properties for node and edge selection must be supported
		VisualProperty<?> vp1 = lexicon.lookup(NETWORK_NODE_SELECTION.getTargetDataType(), NETWORK_NODE_SELECTION.getIdString());
		VisualProperty<?> vp2 = lexicon.lookup(NETWORK_EDGE_SELECTION.getTargetDataType(), NETWORK_EDGE_SELECTION.getIdString());
		
		return vp1 != null && lexicon.isSupported(vp1) && vp2 != null && lexicon.isSupported(vp2);
	}
	
	@Override
	public void menuSelected(MenuEvent e) {
		boolean select = false;
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkView view = applicationManager.getCurrentNetworkView();
		
		if (view != null) {
			Boolean nodeSelection = view.getVisualProperty(NETWORK_NODE_SELECTION);
			Boolean edgeSelection = view.getVisualProperty(NETWORK_EDGE_SELECTION);
			Boolean annotationSelection = view.getVisualProperty(NETWORK_ANNOTATION_SELECTION);
			Boolean nodeLabelSelection = view.getVisualProperty(NETWORK_NODE_LABEL_SELECTION);
			
			if (nodeSelection && edgeSelection && annotationSelection && nodeLabelSelection)
				select = name.equalsIgnoreCase(ALL);
			else if (nodeSelection && edgeSelection)
				select = name.equalsIgnoreCase(NODES_EDGES);
			else if (nodeSelection)
				select = name.equalsIgnoreCase(NODES);
			else if (edgeSelection)
				select = name.equalsIgnoreCase(EDGES);
			else if (annotationSelection)
				select = name.equalsIgnoreCase(ANNOTATIONS);
			else if (nodeLabelSelection)
				select = name.equalsIgnoreCase(NODE_LABELS);
		}
		
		putValue(SELECTED_KEY, select);
		updateEnableState();
	}
}
