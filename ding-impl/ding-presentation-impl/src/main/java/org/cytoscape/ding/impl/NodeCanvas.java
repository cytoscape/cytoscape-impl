package org.cytoscape.ding.impl;

import java.awt.Image;
import java.util.Set;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

/**
 * Canvas to be used for drawing actual network visualization
 */
public class NodeCanvas extends DingCanvas {

	private final VisualMappingManager vmm;
	private final DRenderingEngine re;
	
//	private boolean isPrinting;

	
	public NodeCanvas(CompositeCanvas parent, DRenderingEngine re, CyServiceRegistrar registrar) {
		super(parent.getWidth(), parent.getHeight());
		this.re = re;
		this.vmm = registrar.getService(VisualMappingManager.class);
	}
	
	private Set<VisualPropertyDependency<?>> getVPDeps() {
		CyNetworkView netView = re.getViewModel();
		return vmm.getVisualStyle(netView).getAllVisualPropertyDependencies();
	}


	@Override
	public Image paintImage(RenderDetailFlags flags) {
		Set<VisualPropertyDependency<?>> dependencies = getVPDeps();
		CyNetworkViewSnapshot netViewSnapshot = re.getViewModelSnapshot();
		// MKTODO don't need to create a graphics object on every frame
		GraphGraphics graphics = new GraphGraphics(image);
		EdgeDetails edgeDetails = re.getEdgeDetails();
		NodeDetails nodeDetails = re.getNodeDetails();
		
		GraphRenderer.renderNodes(graphics, netViewSnapshot, flags, nodeDetails, edgeDetails, dependencies);
		
		return image.getImage();
	}
	
	
	
//
//	@Override
//	public void print(Graphics g) {
//		isPrinting = true;
//		
//		final int w = getWidth();
//		final int h = getHeight();
//		
//		if (re != null && w > 0 && h > 0)
//			renderGraph(
//					new GraphGraphics(new ImageImposter(g, w, h), /* debug = */ false, /* clear = */ false), 
//					/* setLastRenderDetail = */ false, re.getPrintLOD());
//		
//		isPrinting = false;
//	}
//
//	@Override
//	public void printNoImposter(Graphics g) {
//		isPrinting = true;
//		final Image img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
//		
//		if (re != null)
//			renderGraph(new GraphGraphics(img, false, false), /* setLastRenderDetail = */ false, re.getPrintLOD());
//		
//		isPrinting = false;
//	}
//
//	/**
// 	 * Return true if this view is curerntly being printed (as opposed to painted on the screen)
// 	 * @return true if we're currently being printed, false otherwise
// 	 */
//	public boolean isPrinting() { 
//		return isPrinting; 
//	}
	
}
