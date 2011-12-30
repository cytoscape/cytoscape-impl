package org.cytoscape.ding;

import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DEdgeView;
import org.cytoscape.ding.impl.DGraphView;


public interface Handle {

	Point2D getPoint(DGraphView graphView, DEdgeView view);
	void setPoint(DGraphView graphView, DEdgeView view, double x, double y);
}
