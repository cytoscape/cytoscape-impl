package org.cytoscape.graph.render.stateful;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.customgraphics.CustomGraphicsPositionCalculator;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.vizmap.VisualPropertyDependency;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

@SuppressWarnings("rawtypes")
public class CustomGraphicsInfo {
	
	private final VisualProperty<CyCustomGraphics> visualProperty;
	private CyCustomGraphics<? extends CustomGraphicLayer> customGraphics;
	private ObjectPosition position;
	private Double size;
	
	public CustomGraphicsInfo(final VisualProperty<CyCustomGraphics> visualProperty) {
		this.visualProperty = visualProperty;
	}

	public VisualProperty<CyCustomGraphics> getVisualProperty() {
		return visualProperty;
	}
	
	public CyCustomGraphics<? extends CustomGraphicLayer> getCustomGraphics() {
		return customGraphics;
	}

	public void setCustomGraphics(CyCustomGraphics<? extends CustomGraphicLayer> customGraphics) {
		this.customGraphics = customGraphics;
	}

	public ObjectPosition getPosition() {
		return position;
	}

	public void setPosition(ObjectPosition position) {
		this.position = position;
	}

	public Double getSize() {
		return size;
	}

	public void setSize(Double size) {
		this.size = size;
	}
	
	public List<CustomGraphicLayer> createLayers(final CyNetworkView netView, final View<CyNode> nodeView, final NodeDetails details, final Set<VisualPropertyDependency<?>> dependencies) {
		final List<CustomGraphicLayer> transformedLayers = new ArrayList<>();
		
		if (customGraphics == null)
			return transformedLayers;
		
		final List<? extends CustomGraphicLayer> originalLayers = customGraphics.getLayers(netView, nodeView);

		if (originalLayers == null || originalLayers.isEmpty())
			return transformedLayers;

		final float fitRatio = customGraphics.getFitRatio();
		
		// Check dependency. Sync size or not.
		boolean sync = syncToNode(dependencies);
		Double cgSize = size;
		ObjectPosition cgPos = position;
		final double nw = details.getWidth(nodeView);
		final double nh = details.getHeight(nodeView);
		
		for (CustomGraphicLayer layer : originalLayers) {
			// Assume it's a Ding layer
			CustomGraphicLayer finalLayer = layer;

			// Resize the layer
			double cgw = 0.0;
			double cgh = 0.0;
			
			if (sync) {
				// Size is locked to node size.
				final float bw = details.getBorderWidth(nodeView);
				cgw = nw - bw;
				cgh = nh - bw;
			} else {
				// Width and height should be set to custom size
				if (cgSize == null) {
					final VisualProperty<Double> sizeVP =
							DVisualLexicon.getAssociatedCustomGraphicsSizeVP(visualProperty);
					cgSize = nodeView.getVisualProperty(sizeVP);
				}
				
				if (cgSize != null)
					cgw = cgh = cgSize;
			}
			
			if (cgw > 0.0 && cgh > 0.0)
				finalLayer = syncSize(layer, cgw, cgh, fitRatio);
			
			// Move the layer to the correct position
			if (cgPos == null)
				cgPos = ObjectPosition.DEFAULT_POSITION;
			
			finalLayer = moveLayer(finalLayer, cgPos, nw, nh);
			
			transformedLayers.add(finalLayer);
		}
		
		return transformedLayers;
	}
	
	private boolean syncToNode(final Set<VisualPropertyDependency<?>> dependencies) {
		boolean sync = false;
		
		if (dependencies != null) {
			for (VisualPropertyDependency<?> dep:dependencies) {
				if (dep.getIdString().equals("nodeCustomGraphicsSizeSync")) {
					sync = dep.isDependencyEnabled();
					break;
				}
			}
		}
		
		return sync;
	}

	private CustomGraphicLayer syncSize(CustomGraphicLayer layer, double width, double height, float fitRatio) {
		final Rectangle2D originalBounds = layer.getBounds2D();
		
		// If this is just a paint, getBounds2D will return null and we can use our own width and height
		if (originalBounds == null) return layer;

		if (width == 0.0 || height == 0.0) return layer;

		final double cgW = originalBounds.getWidth();
		final double cgH = originalBounds.getHeight();

		// In case size is same, return the original.
		if (width == cgW && height == cgH)
			return layer;

		final AffineTransform xform;

		if (layer instanceof ImageCustomGraphicLayer) {
			// Case 1: Images - Find the maximum scale to which the graphics can be scaled while
			//         fitting within the node's rectangle and maintaining its original aspect ratio
			double scale = Math.min(width / cgW, height / cgH);
			xform = AffineTransform.getScaleInstance(scale * fitRatio, scale * fitRatio);
		} else {
			// Case 2: If custom graphic is a vector or other implementation, fit to node's width and height
			xform = AffineTransform.getScaleInstance(fitRatio * width / cgW, fitRatio * height / cgH);
		}
		
		return layer.transform(xform);
	}
	
	private CustomGraphicLayer moveLayer(CustomGraphicLayer layer, ObjectPosition position, double nodeWidth,
			double nodeHeight) {
		var newLayer = position != null ?
				CustomGraphicsPositionCalculator.transform(position, nodeWidth, nodeHeight, layer) : layer;

		return newLayer;
	}
	
	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 5;
		result = prime
				* result
				+ ((visualProperty == null) ? 0 : visualProperty.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomGraphicsInfo other = (CustomGraphicsInfo) obj;
		if (visualProperty == null) {
			if (other.visualProperty != null)
				return false;
		} else if (!visualProperty.equals(other.visualProperty))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CGInfo [customGraphics=" + customGraphics + ", position=" + position + ", size=" + size + "]";
	}
}
