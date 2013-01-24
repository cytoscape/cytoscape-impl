package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.awt.Color;
import java.awt.Graphics;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

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

	private static Map<CyNetworkView,CyNode> sourceNodes = new WeakHashMap<CyNetworkView,CyNode>();
	private static Map<CyNetworkView,Point2D> sourcePoints = new WeakHashMap<CyNetworkView,Point2D>();

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
