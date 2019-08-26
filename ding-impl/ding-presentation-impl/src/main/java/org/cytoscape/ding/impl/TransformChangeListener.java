package org.cytoscape.ding.impl;

import org.cytoscape.ding.impl.canvas.NetworkTransform;

@FunctionalInterface
public interface TransformChangeListener {

	void transformChanged(NetworkTransform transform);
	
}
