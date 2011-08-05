package org.cytoscape.ding.impl.customgraphics;

import org.cytoscape.ding.customgraphics.Layer;
import org.cytoscape.graph.render.stateful.CustomGraphic;


/**
 * Ding implementation of Custom graphics layers.
 *
 */
public class DLayer implements Layer<CustomGraphic> {
	
	private final CustomGraphic layer;
	private final int zOrder;
	
	public DLayer(final CustomGraphic layer, final int zOrder) {
		this.layer = layer;
		this.zOrder = zOrder;
	}

	public int getZorder() {
		return zOrder;
	}
	
	public CustomGraphic getLayerObject() {
		return layer;
	}

}
