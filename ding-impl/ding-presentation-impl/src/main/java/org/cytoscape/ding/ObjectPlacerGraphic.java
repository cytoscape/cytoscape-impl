package org.cytoscape.ding;

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

import static org.cytoscape.ding.Justification.JUSTIFY_LEFT;
import static org.cytoscape.ding.Justification.JUSTIFY_RIGHT;
import static org.cytoscape.ding.Position.NONE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.ding.impl.ObjectPositionImpl;

public class ObjectPlacerGraphic extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = -1091948204116900740L;
	
	protected static final String OBJECT_POSITION_CHANGED = "OBJECT_POSITION_CHANGED";

	private ObjectPosition p;

	// dimensions of panel
	private static final int DEFAULT_WINDOW_SIZE = 500;
	
	// "Snap" distance
	private static final double GRAVITY_DISTANCE = 10;
	
	// Color scheme for GUI
	private final Color BG_COLOR = UIManager.getColor("TextField.background");
	
	private final Color LABEL_BOX_FG_COLOR = UIManager.getColor("CyColor.secondary2(-2)");
	// Same color, but transparent
	private final Color LABEL_BOX_BG_COLOR =
			new Color(LABEL_BOX_FG_COLOR.getRed(), LABEL_BOX_FG_COLOR.getGreen(), LABEL_BOX_FG_COLOR.getBlue(), 30);
	
	private final Color NODE_BOX_FG_COLOR = UIManager.getColor("CyColor.secondary2(+1)");
	// Same color, but transparent
	private final Color NODE_BOX_BG_COLOR =
			new Color(NODE_BOX_FG_COLOR.getRed(), NODE_BOX_FG_COLOR.getGreen(), NODE_BOX_FG_COLOR.getBlue(), 30);
	
	private final Color POINT_HIGHLIGHT_COLOR = UIManager.getColor("CyColor.secondary1(+2)");
	
	private int center;
	private float offsetRatio;

	// dimensions for node box
	private int nxy;

	// locations of node points
	private int[] npoints;

	// dimensions for label box
	private int lx;
	private int ly;

	// locations for label points
	private int[] lxpoints;
	private int[] lypoints;

	// diameter of a point
	private int dot;

	// x/y positions for label box, initially offset
	private int xPos;
	private int yPos;

	// indices of the closest points
	private int bestLabelX = 1;
	private int bestLabelY = 1;
	private int bestNodeX = 1;
	private int bestNodeY = 1;

	// mouse drag state
	private boolean beenDragged;
	private boolean canOffsetDrag;

	// click offset
	private int xClickOffset;
	private int yClickOffset;

	// the x and y offsets for the label rendering
	private int xOffset;
	private int yOffset;

	// default text justify rule
	private Justification justify;
	
	// used to determine the render level of detail
	private boolean renderDetail;

	// strings for the graphic
	private String objectLabel = "LABEL";
	private String targetLabel = "NODE";
	private String click = "(click and drag)";

	// font metrics for strings
	private int labelLen;
	private int clickLen;
	private int ascent;
	private int detailStrokeWidth = 3;
	private int lowStrokeWidth = 1;
	private final Stroke detailStroke = new BasicStroke(detailStrokeWidth);
	private final Stroke lowStroke = new BasicStroke(lowStrokeWidth);

	private Integer graphicSize;
	

	/**
	 * A GUI for placing an object (e.g. a label) relative to a node.
	 * 
	 * @param graphicSize number of pixels square the that graphic should be
	 * @param fullDetail whether or not to render at full detail
	 * @param objectName
	 */
	public ObjectPlacerGraphic(final Integer graphicSize, boolean fullDetail, final String objectName) {
		super();
		this.p = new ObjectPositionImpl();
		this.objectLabel = objectName;
		renderDetail = fullDetail;

		this.graphicSize = graphicSize;
		
		if (graphicSize == null)
			initSize(DEFAULT_WINDOW_SIZE);
		else
			initSize(graphicSize);

		setBackground(BG_COLOR);

		addMouseListener(new MouseClickHandler());
		addMouseMotionListener(new MouseDragHandler());

		applyPosition();
		repaint();
	}
	
	public void setObjectPosition(final ObjectPosition op) {
		this.p = op;
	}

	private void initSize(int size) {
		// dimensions of panel
		setMinimumSize(new Dimension(size, size));
		setPreferredSize(new Dimension(size, size));
		
		center = size / 2;

		offsetRatio = (float) size / DEFAULT_WINDOW_SIZE;

		// dimensions for node box
		nxy = (int) (0.3 * size);

		// locations of node points
		int[] tnpoints = { center - (nxy / 2), center, center + (nxy / 2) };
		npoints = tnpoints;

		// dimensions for object box
		lx = (int) (0.4 * size);
		ly = (int) (0.1 * size);

		// locations for label points
		int[] tlxpoints = { 0, lx / 2, lx };
		int[] tlypoints = { 0, ly / 2, ly };
		lxpoints = tlxpoints;
		lypoints = tlypoints;

		// diameter of a point
		dot = (int) (0.02 * size);

		// x/y positions for label box, initially offset
		xPos = dot;
		yPos = dot;
	}

	/**
	 * The method that handles the rendering of placement gui.
	 */
	@Override
	public void paint(Graphics gin) {
		int w = graphicSize != null ? graphicSize : getSize().width;
		int h = graphicSize != null ? graphicSize : getSize().height;
		
		final Graphics2D g = (Graphics2D) gin;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// calculate the font
		if (labelLen <= 0) {
			FontMetrics fm = g.getFontMetrics();
			labelLen = fm.stringWidth(objectLabel);
			clickLen = fm.stringWidth(click);
			ascent = fm.getMaxAscent();
		}

		// clear the screen
		g.setColor(UIManager.getColor("Table.background"));
		g.fillRect(0, 0, w, h);

		// draw the node box
		int x = center - (nxy / 2);
		int y = center - (nxy / 2);

		g.setColor(NODE_BOX_BG_COLOR);
		g.fillOval(x, y, nxy, nxy);

		if (renderDetail)
			g.setStroke(detailStroke);
		else
			g.setStroke(lowStroke);

		g.setColor(NODE_BOX_FG_COLOR);
		g.drawLine(x, y, x + nxy, y);
		g.drawLine(x + nxy, y, x + nxy, y + nxy);
		g.drawLine(x + nxy, y + nxy, x, y + nxy);
		g.drawLine(x, y + nxy, x, y);

		if (renderDetail) {
			g.drawString(targetLabel, center - (nxy / 12), center - (nxy / 6));

			// draw the node box points
			for (int i = 0; i < npoints.length; i++) {
				for (int j = 0; j < npoints.length; j++) {
					if ((i == bestNodeX) && (j == bestNodeY) && !beenDragged)
						g.setColor(POINT_HIGHLIGHT_COLOR);
					else
						g.setColor(NODE_BOX_FG_COLOR);
					g.fillOval(npoints[i] - (dot / 2), npoints[j] - (dot / 2), dot, dot);
				}
			}
		}

		// draw the label box
		g.setColor(LABEL_BOX_BG_COLOR);
		g.fillRect(xOffset + xPos, yOffset + yPos, lx, ly);

		g.setColor(LABEL_BOX_FG_COLOR);
		g.drawLine(xOffset + xPos, yOffset + yPos, xOffset + xPos + lx, yOffset + yPos);
		g.drawLine(xOffset + xPos + lx, yOffset + yPos, xOffset + xPos + lx, yOffset + yPos + ly);
		g.drawLine(xOffset + xPos + lx, yOffset + yPos + ly, xOffset + xPos, yOffset + yPos + ly);
		g.drawLine(xOffset + xPos, yOffset + yPos + ly, xOffset + xPos, yOffset + yPos);

		// draw the string in the justified location
		if (renderDetail) {
			int vspace = (ly - ascent - ascent) / 3;

			if (justify == JUSTIFY_LEFT) {
				g.drawString(objectLabel, xOffset + xPos + detailStrokeWidth, yOffset + yPos + vspace + ascent);
				g.drawString(click, xOffset + xPos + detailStrokeWidth, yOffset + yPos + (2 * (vspace + ascent)));
			} else if (justify == JUSTIFY_RIGHT) {
				g.drawString(objectLabel, xOffset + xPos + (lx - labelLen), yOffset + yPos + vspace + ascent);
				g.drawString(click, xOffset + xPos + (lx - clickLen), yOffset + yPos + (2 * (vspace + ascent)));
			} else { // center
				g.drawString(objectLabel, (xOffset + xPos + ((lx - labelLen) / 2)) - detailStrokeWidth, yOffset + yPos
						+ vspace + ascent);
				g.drawString(click, (xOffset + xPos + ((lx - clickLen) / 2)) - detailStrokeWidth, yOffset + yPos
						+ (2 * (vspace + ascent)));
			}
		}

		if (renderDetail) {
			// draw the label box points
			g.setColor(LABEL_BOX_FG_COLOR);

			for (int i = 0; i < lxpoints.length; i++)
				for (int j = 0; j < lypoints.length; j++) {
					if ((i == bestLabelX) && (j == bestLabelY) && !beenDragged)
						g.setColor(POINT_HIGHLIGHT_COLOR);

					g.fillOval((xPos + xOffset + lxpoints[i]) - (dot / 2), (yPos + yOffset + lypoints[j]) - (dot / 2),
							dot, dot);

					if ((i == bestLabelX) && (j == bestLabelY))
						g.setColor(LABEL_BOX_FG_COLOR);
				}
		}
	}

	private class MouseClickHandler extends MouseAdapter {
		
		/**
		 * Only allows dragging if we're in the label box. Also sets the offset
		 * from where the click is and where the box is, so the box doesn't
		 * appear to jump around too much.
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();

			// click+drag within box
			if ((x >= (xPos + xOffset)) && (x <= (xPos + xOffset + lx))
					&& (y >= (yPos + yOffset)) && (y <= (yPos + yOffset + ly))) {
				canOffsetDrag = true;
				xClickOffset = x - xPos;
				yClickOffset = y - yPos;
			}
		}

		/**
		 * Finds the closest points once the dragging is finished.
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if (beenDragged) {

				int x = e.getX();
				int y = e.getY();

				// top right
				xPos = x - xClickOffset + xOffset;
				yPos = y - yClickOffset + yOffset;

				double best = Double.POSITIVE_INFINITY;
				double offX = 0;
				double offY = 0;

				// loop over each point in the node box
				for (int i = 0; i < npoints.length; i++) {
					for (int j = 0; j < npoints.length; j++) {
						Point nodePoint = new Point(npoints[i] - (dot / 2),
								npoints[j] - (dot / 2));

						// loop over each point in the label box
						for (int a = 0; a < lxpoints.length; a++) {
							for (int b = 0; b < lypoints.length; b++) {
								Point labelPoint = new Point(
										(xPos + lxpoints[a]) - (dot / 2),
										(yPos + lypoints[b]) - (dot / 2));

								double dist = labelPoint
										.distance((Point2D) nodePoint);

								if (dist < best) {
									best = dist;
									bestLabelX = a;
									bestLabelY = b;
									bestNodeX = i;
									bestNodeY = j;
									offX = labelPoint.getX() - nodePoint.getX();
									offY = labelPoint.getY() - nodePoint.getY();
								}
							}
						}
					}
				}

				xPos = npoints[bestNodeX] - lxpoints[bestLabelX];
				yPos = npoints[bestNodeY] - lypoints[bestLabelY];

				if (Math.sqrt(offX * offX + offY * offY) > (GRAVITY_DISTANCE + (dot / 2))) {
					xOffset = (int) offX;
					yOffset = (int) offY;
				} else {
					xOffset = 0;
					yOffset = 0;
				}

				p.setOffsetX(xOffset);
				p.setOffsetY(yOffset);
				p.setAnchor(Position.parse(bestLabelX + (3 * bestLabelY)));
				p.setTargetAnchor(Position.parse(bestNodeX + (3 * bestNodeY)));
				firePropertyChange(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED, null, p);

				repaint();
				beenDragged = false;
				canOffsetDrag = false;
			}
		}
	}

	private class MouseDragHandler extends MouseMotionAdapter {
		
		/**
		 * Handles redrawing for dragging.
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			// dragging within normal box
			if (canOffsetDrag) {
				xPos = e.getX() - xClickOffset;
				yPos = e.getY() - yClickOffset;

				beenDragged = true;
				repaint();
			}
		}
	}

	/**
	 * Applies the new ObjectPosition to the graphic.
	 */
	public void applyPosition() {
		xOffset = (int) (p.getOffsetX() * offsetRatio);
		yOffset = (int) (p.getOffsetY() * offsetRatio);
		justify = p.getJustify();

		final Position nodeAnchor = p.getTargetAnchor();

		if (nodeAnchor != NONE) {
			bestNodeX = nodeAnchor.getConversionConstant() % 3;
			bestNodeY = nodeAnchor.getConversionConstant() / 3;
		}

		final Position labelAnchor = p.getAnchor();

		if (labelAnchor != NONE) {
			bestLabelX = labelAnchor.getConversionConstant() % 3;
			bestLabelY = labelAnchor.getConversionConstant() / 3;
		}

		if ((nodeAnchor != NONE) || (labelAnchor != NONE)) {
			xPos = npoints[bestNodeX] - lxpoints[bestLabelX];
			yPos = npoints[bestNodeY] - lypoints[bestLabelY];
		}
	}
	
	void setPosition(ObjectPosition p) {
		this.p = p;
		this.applyPosition();
		repaint();
	}

	/**
	 * Handles all property changes that the panel listens for.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		final String type = e.getPropertyName();

		if (type.equals(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED)) {
			p = (ObjectPosition) e.getNewValue();
			applyPosition();
			repaint();
		}
	}
}
