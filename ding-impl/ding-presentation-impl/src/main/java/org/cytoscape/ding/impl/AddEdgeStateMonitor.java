/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.ding.impl;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

/**
 * This class is designed to keep track of the state of AddEdgeTasks throughout ding.
 * An instance of this class is used by InnerCanvas to keep track of the state of adding 
 * edges for that particular view.  This class also maintains several static maps that
 * keep track of the values needed to add edges for individual network views, which are
 * queried by the AddEdgeTask and TaskFactory.
 */
class AddEdgeStateMonitor {

	// for drawing the rubber band when dropping
	private Point2D nextPoint;
	private double saveX1;
	private double saveX2;
	private double saveY1;
	private double saveY2;

	private final InnerCanvas canvas;
	private final DGraphView m_view;

	private static Map<CyNetworkView,CyNode> sourceNodes = new HashMap<CyNetworkView,CyNode>();
	private static Map<CyNetworkView,Point2D> sourcePoints = new HashMap<CyNetworkView,Point2D>();

	AddEdgeStateMonitor(InnerCanvas canvas, DGraphView m_view) {
		this.canvas = canvas;
		this.m_view = m_view;
	}

	boolean addingEdge() {
		return sourceNodes.containsKey(m_view);
	}

	static CyNode getSourceNode(CyNetworkView view) {
		return sourceNodes.get(view);
	}

	static void setSourceNode(CyNetworkView view, CyNode n) {
		if ( n == null )
			sourceNodes.remove(view);
		else
			sourceNodes.put(view,n);
	}

	static Point2D getSourcePoint(CyNetworkView view) {
		return sourcePoints.get(view);
	}

	static void setSourcePoint(CyNetworkView view, Point2D p) {
		if ( p == null )
			sourcePoints.remove(view);
		else
			sourcePoints.put(view,p);
	}
	static void reset(CyNetworkView view) {
		setSourceNode(view,null);
		setSourcePoint(view,null);
	}
	void drawRubberBand(MouseEvent e) {
		nextPoint = e.getPoint();

		Point2D startPoint = getSourcePoint(m_view);
		if ( startPoint == null )
			return;

        if (nextPoint == null) 
        	nextPoint = startPoint; 

        double x1 = startPoint.getX();
        double y1 = startPoint.getY();
        double x2 = nextPoint.getX();
        double y2 = nextPoint.getY();
        double lineLen = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
        double offset = 5;

        if (lineLen == 0) {
            lineLen = 1;
        }

        y2 = y2 + (((y1 - y2) / lineLen) * offset);
        x2 = x2 + (((x1 - x2) / lineLen) * offset);

        Graphics g = canvas.getGraphics();

        Color saveColor = g.getColor();

        if (saveX1 != Double.MIN_VALUE) {
            DingCanvas backgroundCanvas = m_view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS);
            g.setColor(backgroundCanvas.getBackground());
            g.drawLine(((int) saveX1) - 1, ((int) saveY1) - 1, ((int) saveX2) + 1,
                   ((int) saveY2) + 1);
        }

        canvas.update(g);
        g.setColor(Color.BLACK);
        g.drawLine(((int) x1) - 1, ((int) y1) - 1, ((int) x2) + 1, ((int) y2) + 1);
        g.setColor(saveColor);

        saveX1 = x1;
        saveX2 = x2;
        saveY1 = y1;
        saveY2 = y2;
	}
}
