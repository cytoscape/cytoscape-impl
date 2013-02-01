package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Class displaying two circles with different sizes and an arrow between them.
 * 
 * @author Nadezhda Doncheva
 */
public class NodeEdgeCanvas extends Canvas {

	/**
	 * Initializes a new instance of <code>NodeEdgeCanvas</code>
	 * 
	 * @param aSmallToBig
	 *            Flag indicating that the small circle is drawn before the big
	 *            one it <code>true</code> and after it otherwise.
	 * @param aNodeCanvas
	 *            Flag indicating that the node mapping is drawn if
	 *            <code>true</code> or the edge size mapping otherwise.
	 */
	public NodeEdgeCanvas(boolean aSmallToBig, boolean aNodeCanvas) {
		smallToBig = aSmallToBig;
		nodeCanvas = aNodeCanvas;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final Dimension size = getSize();
		// Size of the circles depends on the height of the panel.
		final int h = size.height;
		final int bigR = h - 1;
		final int smallR = h / 2;
		final int y = h / 2;
		final int bigLeftAlign = h;
		final int smallLeftAlign = bigLeftAlign + smallR/2;
		final int bigRightAlign = size.width - 2 * h;
		final int smallRightAlign = bigRightAlign + smallR/2;
		final int edgeWidth1 = 2;
		final int edgeWidth2 = 2*edgeWidth1;
		if (nodeCanvas) {
			if (smallToBig) {
				// Draw a small and a big pink circle
				drawNode(g, y, smallLeftAlign, bigRightAlign, smallR, bigR);
			} else {
				// Draw a big and a small pink circle
				drawNode(g, y, smallRightAlign, bigLeftAlign, smallR, bigR);
			}
		} else {
			if (smallToBig) {
				// Draw a small and a big pink circle
				drawEdge(g, smallLeftAlign, smallRightAlign, smallR, h, edgeWidth1, edgeWidth2);
			} else {
				// Draw a big and a small pink circle
				drawEdge(g, smallLeftAlign, smallRightAlign, smallR, h, edgeWidth2, edgeWidth1);
			}			
		}
		// Draw line between the two circles (nodes)
		drawArrow(g, y, smallR, size.width, bigLeftAlign);
	}

	private void drawNode(Graphics g, int y, int smallAlign, int bigAlign, int smallR, int bigR){
		g.setColor(Color.PINK);
		g.fillOval(smallAlign, y/2, smallR, smallR);
		g.fillOval(bigAlign, 0, bigR, bigR);
		g.setColor(Color.BLACK);
		g.drawOval(smallAlign, y/2, smallR, smallR);
		g.drawOval(bigAlign, 0, bigR, bigR);
	}
	
	private void drawEdge(Graphics g, int leftAlign, int rightAlign, int smallR, int h, int edgeWidth1, int edgeWidth2){
		g.setColor(Color.BLACK);
//		g.drawLine(leftAlign, h, leftAlign+smallR, 0);
		for (int i = 0; i< edgeWidth1; i++) {
			g.drawLine(leftAlign + i, h, leftAlign+smallR + i, 0);
		}
		for (int i = 0; i< edgeWidth2; i++) {
			g.drawLine(rightAlign + i, h, rightAlign+smallR + i, 0);
		}
	}
	
	private void drawArrow(Graphics g, int y, int smallR, int width, int bigLeftAlign) {
		g.setColor(Color.BLACK);
		final int x1 = 3 * bigLeftAlign;
		final int x2 = width - x1;
		g.drawLine(x1, y, x2, y);
		g.drawLine(x2 - smallR, y - smallR, x2, y);
		g.drawLine(x2 - smallR, y + smallR, x2, y);
	}
	
	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -9126275712496557815L;

	/**
	 * Flag indicating which circle should be drawn first. If <code>true</code>
	 * first the small and then the big one is drawn, and if <code>false</code>
	 * otherwise.
	 */
	private boolean smallToBig;
	
	private boolean nodeCanvas;
}
