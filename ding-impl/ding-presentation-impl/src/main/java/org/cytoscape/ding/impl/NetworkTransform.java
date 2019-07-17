package org.cytoscape.ding.impl;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

public interface NetworkTransform {
	
	public AffineTransform getAffineTransform();
	
	public void xformImageToNodeCoords(double[] coords);
	
	public void xformNodeToImageCoords(double[] coords);
	
	public GeneralPath pathInNodeCoords(GeneralPath path);

	public Rectangle2D.Float getNetworkVisibleArea();

}
