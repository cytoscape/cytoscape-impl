package org.cytoscape.ding.impl.cyannotator.api;

import java.awt.geom.Point2D;

import org.cytoscape.model.CyNode;


public interface ArrowAnnotation extends Annotation {
	public Annotation getSource();
	public void setSource(Annotation source);

	public CyNode getNodeTarget();
	public Point2D getPointTarget();
	public void setTarget(CyNode target);

	public int getDirection();
	public void setDirection(int direction);

	public double getLineWidth();
	public void setLineWidth(double width);

	public double getArrowSize();
	public void setArrowSize(double width);
}
