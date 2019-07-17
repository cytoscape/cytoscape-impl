package org.cytoscape.ding.impl;

import java.util.Set;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
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
public class InnerCanvas extends DingCanvas {

	private final CyServiceRegistrar registrar;
	private final VisualMappingManager vmm;
	
	private final DRenderingEngine re;
	private final CompositeCanvas parent;
	private final DingLock dingLock;
	
	private GraphLOD lod;
	private int lastRenderDetail;
	
//	private boolean isPrinting;

	
	public InnerCanvas(DingLock lock, CompositeCanvas parent, DRenderingEngine re, CyServiceRegistrar registrar) {
		super(parent.getWidth(), parent.getHeight());
		this.dingLock = lock;
		this.parent = parent;
		this.re = re;
		this.registrar = registrar;
		this.lod = new GraphLOD(); // Default LOD.
		this.vmm = registrar.getService(VisualMappingManager.class);
	}
	
	public int getLastRenderDetail() {
		return lastRenderDetail;
	}

	@Override
	public void paintImage() {

		// This is the magical portion of code that transfers what is in the
		// visual data structures into what's on the image.
//		boolean contentChanged = false;
//		boolean viewportChanged = false;
//		double xCenter = 0.0d;
//		double yCenter = 0.0d;
//		double scaleFactor = 1.0d;

//		this.fontMetrics = g.getFontMetrics();

//		synchronized (dingLock) {
//			if (re != null && re.isDirty()) {
//				contentChanged = re.isContentChanged();
//				viewportChanged = re.isViewportChanged();
				lastRenderDetail = renderGraph(image);
//				xCenter = this.xCenter;
//				yCenter = this.yCenter;
//				scaleFactor = this.scaleFactor;
				
				// set the publicly accessible image object *after* it has been rendered
//				image = grafx.image;
//			}
//		}

//		g.drawImage(image, 0, 0, null);

//		if (contentChanged && re != null) {
//			re.fireContentChanged();
//		}
//		if (viewportChanged && re != null) {
//			re.fireViewportChanged(getWidth(), getHeight(), xCenter, yCenter, scaleFactor);
//		}
	}

//	/**
//	 *  @param setLastRenderDetail if true, "m_lastRenderDetail" will be updated, otherwise it will not be updated.
//	 */
//	private void renderGraph(GraphGraphics graphics, boolean setLastRenderDetail, GraphLOD lod) {
//		int lastRenderDetail = re.renderGraph(graphics, lod, xCenter, yCenter, scaleFactor);
//		if (setLastRenderDetail)
//			this.lastRenderDetail = lastRenderDetail;
//	}
	
	
	private int renderGraph(NetworkImageBuffer imageBuffer) {
		int lastRenderDetail = 0;
		
		try {
			synchronized (dingLock) {
				CyNetworkView netView = re.getViewModel();
				Set<VisualPropertyDependency<?>> dependencies = vmm.getVisualStyle(netView).getAllVisualPropertyDependencies();
				
				CyNetworkViewSnapshot netViewSnapshot = re.getViewModelSnapshot();
				
				GraphGraphics graphics = new GraphGraphics(imageBuffer, true); // MKTODO pass true or false?
				
				lastRenderDetail = GraphRenderer.renderGraph(
											 netViewSnapshot,
				  						     lod,
				  						     re.getNodeDetails(),
				  						     re.getEdgeDetails(),
				  						     graphics, 
//				  						     parent.getCenterX(),
//				  						     parent.getCenterY(), 
//				  						     parent.getScaleFactor(), 
				  						     dependencies);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return lastRenderDetail;
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
