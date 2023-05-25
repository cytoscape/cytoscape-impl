package org.cytoscape.ding;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_POSITION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_POSITION;
import static org.cytoscape.view.presentation.property.values.Position.NONE;
import static org.jdesktop.swingx.color.ColorUtil.setAlpha;
import static org.jdesktop.swingx.color.ColorUtil.setBrightness;
import static org.jdesktop.swingx.color.ColorUtil.setSaturation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.view.presentation.property.ObjectPositionVisualProperty;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Position;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * A GUI for placing an object (e.g. a label) relative to a node.
 */
@SuppressWarnings("serial")
public class ObjectPlacerGraphic extends JPanel implements PropertyChangeListener {

	protected static final String OBJECT_POSITION_CHANGED = "OBJECT_POSITION_CHANGED";
	
	private static final String SOURCE_NODE_TXT = "SOURCE";
	private static final String TARGET_NODE_TXT = "TARGET";
	/** Instructions text */
	private static final String CLICK_TXT = "(click and drag)";

	// Default dimensions of panel
	private static final int DEF_PANEL_SIZE = 500;
	private static final int DEF_EDGE_PANEL_WIDTH = 600;
	private static final int DEF_EDGE_PANEL_HEIGHT = 400;
	
	private static final float TGT_FONT_SIZE = 32.0f;
	private static final float NODES_FONT_SIZE = 18.0f;
	
	/** "Snap" distance */
	private static final double GRAVITY_DISTANCE = 10;
	
	// Color scheme for GUI
	private final Color BG_COLOR = UIManager.getColor("Table.background");
	
	private final Color OBJ_FG_COLOR = UIManager.getColor("CyColor.complement(-2)");
	private final Color OBJ_BG_COLOR = setAlpha(OBJ_FG_COLOR, 20);
	private final Color OBJ_BORDER_COLOR = setAlpha(OBJ_FG_COLOR, 150);
	private final Color OBJ_INFO_COLOR = setAlpha(OBJ_FG_COLOR, 150);
	
	private final Color TGT_FG_COLOR = setAlpha(BG_COLOR, 200);
	private final Color TGT_BG_COLOR = setAlpha(UIManager.getColor("CyColor.complement(+1)"), 50);
	private final Color TGT_BORDER_COLOR = setAlpha(UIManager.getColor("CyColor.complement(+1)"), 125);
	private final Color TGT_POINT_COLOR = UIManager.getColor("CyColor.complement(+1)");
	
	/** The color of the source and target nodes (EDGE_LABEL_POSITION only)  */
	private final Color NODES_BG_COLOR = setSaturation(
			setBrightness(UIManager.getColor("CyColor.complement(+2)"), 0.8f),
			0.25f
	);
	private final Color NODES_FG_COLOR = setAlpha(UIManager.getColor("Table.background"), 150);
	
	private final Color POINT_HIGHLIGHT_COLOR = UIManager.getColor("CyColor.primary(+2)");
	
	/** The width of the target box (node/edge) */
	private int tw;
	/** The height of the target box (node/edge) */
	private int th;

	/** x values of target (node/edge) points */
	private int[] txPoints;
	/** y values of target (node/edge) points */
	private int[] tyPoints;

	/** The width of the object box (e.g. label, custom graphics) */
	private int ow;
	/** The height of the object box (e.g. label, custom graphics) */
	private int oh;

	/** x values of the object (e.g. label, graphics) points */
	private int[] oxPoints;
	/** y values of the object (e.g. label, graphics) points */
	private int[] oyPoints;

	/** Diameter of a point */
	private int dot;
	/** Diameter of source/target nodes (EDGE_LABEL_POSITION only) */
	private int nd;

	/** x value for the object box position */
	private int xPos;
	/** y value for the object box position */
	private int yPos;

	// Indices of the closest points
	private int bestObjX = 1;
	private int bestObjY = 1;
	private int bestTgtX = 1;
	private int bestTgtY = 1;

	// Mouse drag state
	private boolean canDrag;
	private boolean isDragging;

	// Click offset
	private int xClickOffset;
	private int yClickOffset;

	// The x and y offsets for the label rendering
	private int xOffset;
	private int yOffset;
	
	private float offsetRatio;

	/** Default text justification rule */
	private Justification justify;
	
	// Font metrics for strings
	private Map<String, Integer> txtWidths = new HashMap<>();
	private int txtHeight = -1;
	private int detailStrokeWidth = 3;
	private int lowStrokeWidth = 1;
	private final Stroke detailStroke = new BasicStroke(detailStrokeWidth);
	private final Stroke lowStroke = new BasicStroke(lowStrokeWidth);

	private ObjectPosition op;
	private final ObjectPositionVisualProperty vp;
	/** Used to determine the render level of detail */
	private final boolean fullDetail;

	private final Integer prefWidth;
	private final Integer prefHeight;
	
	public ObjectPlacerGraphic(
			ObjectPosition op,
			ObjectPositionVisualProperty vp, // may be null
			Integer width,
			Integer height,
			boolean fullDetail
	) {
		this.op = op;
		this.vp = vp;
		this.fullDetail = fullDetail;

		setBackground(BG_COLOR);

		addMouseListener(new MouseClickHandler());
		addMouseMotionListener(new MouseDragHandler());

		if (width == null)
			width = EDGE_LABEL_POSITION.equals(vp) ? DEF_EDGE_PANEL_WIDTH : DEF_PANEL_SIZE;
		if (height == null)
			height = EDGE_LABEL_POSITION.equals(vp) ? DEF_EDGE_PANEL_HEIGHT : DEF_PANEL_SIZE;
		
		this.prefWidth = width;
		this.prefHeight = height;
		
		initSize(width, height);
		applyPosition();
	}
	
	public ObjectPlacerGraphic(ObjectPosition op, ObjectPositionVisualProperty vp, boolean fullDetail) {
		this(op, vp, null, null, fullDetail);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	/**
	 * Applies the new ObjectPosition to the graphic.
	 */
	public void applyPosition() {
		xOffset = (int) (op.getOffsetX() * offsetRatio);
		yOffset = (int) (op.getOffsetY() * offsetRatio);
		justify = op.getJustify();

		var nodeAnchor = op.getTargetAnchor();

		if (nodeAnchor != NONE) {
			bestTgtX = nodeAnchor.ordinal() % 3;
			bestTgtY = nodeAnchor.ordinal() / 3;
		}

		var labelAnchor = op.getAnchor();

		if (labelAnchor != NONE) {
			bestObjX = labelAnchor.ordinal() % 3;
			bestObjY = labelAnchor.ordinal() / 3;
		}

		if ((nodeAnchor != NONE || labelAnchor != NONE)
				&& txPoints != null && tyPoints != null
				&& oxPoints != null && oyPoints != null) {
			if (txPoints.length > bestTgtX && oxPoints.length > bestObjX)
				xPos = txPoints[bestTgtX] - oxPoints[bestObjX];
			if (tyPoints.length > bestTgtY && oyPoints.length > bestObjY)
				yPos = tyPoints[bestTgtY] - oyPoints[bestObjY];
		}
	}

	/**
	 * The method that handles the rendering of placement gui.
	 */
	@Override
	public void paint(Graphics g) {
		var g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		var f1 = g2.getFont();
		var f2 = fullDetail ? createTargetFont(f1) : null;
		var f3 = fullDetail ? createSrcTgtNodesFont(f1) : null;
		
		var fm1 = g2.getFontMetrics();
		var fm2 = f2 != null ? g2.getFontMetrics(f2) : null;
		var fm3 = f3 != null ? g2.getFontMetrics(f3) : null;
		
		if (txtHeight <= 0)
			txtHeight = fm1.getHeight();
		
		var isNodeLabel = vp == null || NODE_LABEL_POSITION.equals(vp);
		var isEdgeLabel = EDGE_LABEL_POSITION.equals(vp);
		var isCGLabel = vp != null && vp.getIdString().startsWith("NODE_CUSTOMGRAPHICS_POSITION");
		
		var tgtTxt = isEdgeLabel ? "EDGE" : "NODE";
		var objTxt = "OBJECT";
		
		if (isNodeLabel || isEdgeLabel) objTxt =  "LABEL";
		else if (isCGLabel) objTxt =  "IMAGE/CHART";
		
		int w = getWidth();
		int h = getHeight();
		
		if (w <= 0)
			w = prefWidth;
		if (h <= 0)
			h = prefHeight;
		
		// clear the screen
		g2.setColor(BG_COLOR);
		g2.fillRect(0, 0, w, h);

		// draw the target shape
		g2.setColor(TGT_BG_COLOR);
		
		var xc = w / 2; // x of center point
		var yc = h / 2; // y of center point
		int x = xc - (tw / 2);
		int y = yc - (th / 2);
		
		if (isEdgeLabel) {
			// draw as an edge, from the source node's center to the target's center
			g2.fillRect(x - nd / 2, y, tw + nd, th);
			
			// also draw the source and target nodes
			g2.setColor(NODES_BG_COLOR);
			
			var yn = yc - (nd / 2);
			g2.fillArc(x - nd, yn, nd, nd, -90, 180); // source node
			g2.fillArc(x + tw, yn, nd, nd, -90, -180); // target node
			
			if (fullDetail) {
				// draw the SOURCE/TARGET (NODES) text
				int snTxtWidth = getTextWidth(fm3, SOURCE_NODE_TXT);
				int hpad = 10;
				
				int sty = yc + fm3.getMaxAscent() / 2 - fm3.getMaxDescent() / 2;
				
				g2.setFont(f3);
				g2.setColor(NODES_FG_COLOR);
				
				g2.drawString(SOURCE_NODE_TXT, nd / 2 - snTxtWidth - hpad, sty);
				g2.drawString(TARGET_NODE_TXT, x + tw + hpad, sty);

				g2.setFont(f1);
			}
		} else {
			// draw as a node
			g2.fillOval(x, y, tw, th);
		}

		g2.setStroke(fullDetail ? detailStroke : lowStroke);
		
		if (!isEdgeLabel) { // it looks better if we do not draw the border when the target is an edge
			g2.setColor(TGT_BORDER_COLOR);
			g2.drawRect(x, y, tw, th);
		}

		if (fullDetail) {
			g2.setFont(f2);
			
			int tgtTxtWidth = getTextWidth(fm2, tgtTxt);
			
			// draw the target's text
			g2.setColor(TGT_FG_COLOR);
			g2.drawString(tgtTxt, xc - (tgtTxtWidth / 2), yc + fm2.getMaxAscent() / 2 - fm2.getMaxDescent() / 2);
			
			g2.setFont(f1);

			// draw the node box points
			for (int i = 0; i < txPoints.length; i++) {
				for (int j = 0; j < tyPoints.length; j++) {
					if (i == bestTgtX && j == bestTgtY && !isDragging)
						g2.setColor(POINT_HIGHLIGHT_COLOR);
					else
						g2.setColor(TGT_POINT_COLOR);
					
					g2.fillOval(txPoints[i] - (dot / 2), tyPoints[j] - (dot / 2), dot, dot);
				}
			}
		}

		// draw the label box
		g2.setColor(OBJ_BG_COLOR);
		g2.fillRect(xOffset + xPos, yOffset + yPos, ow, oh);
		g2.setColor(OBJ_BORDER_COLOR);
		g2.drawRect(xOffset + xPos, yOffset + yPos, ow, oh);
		
		// draw the string in the justified location
		if (fullDetail) {
			// calculate the dimensions of the texts, if they haven't been calculated yet
			int ascent = fm1.getMaxAscent();
			int descent = fm1.getMaxDescent();
			int objTxtWidth = getTextWidth(fm1, objTxt);
			int clickTxtWidth = getTextWidth(fm1, CLICK_TXT);
			
			int yObjTxt = yOffset + yPos + txtHeight - descent + detailStrokeWidth;
			int yClickTxt = yOffset + yPos + oh - txtHeight + ascent - detailStrokeWidth;

			// draw the object's texts
			g2.setColor(OBJ_FG_COLOR);
			
			if (justify == Justification.JUSTIFY_LEFT) {
				g2.drawString(
						objTxt,
						xOffset + xPos + detailStrokeWidth,
						yObjTxt
				);
				g2.setColor(OBJ_INFO_COLOR);
				g2.drawString(
						CLICK_TXT,
						xOffset + xPos + detailStrokeWidth,
						yClickTxt
				);
			} else if (justify == Justification.JUSTIFY_RIGHT) {
				g2.drawString(
						objTxt,
						xOffset + xPos + (ow - objTxtWidth) - detailStrokeWidth,
						yObjTxt
				);
				g2.setColor(OBJ_INFO_COLOR);
				g2.drawString(
						CLICK_TXT,
						xOffset + xPos + (ow - clickTxtWidth),
						yClickTxt
				);
			} else { // JUSTIFY_CENTER
				g2.drawString(
						objTxt,
						(xOffset + xPos + ((ow - objTxtWidth) / 2)),
						yObjTxt
				);
				g2.setColor(OBJ_INFO_COLOR);
				g2.drawString(
						CLICK_TXT,
						(xOffset + xPos + ((ow - clickTxtWidth) / 2)),
						yClickTxt
				);
			}
		}

		if (fullDetail) {
			// draw the label box points
			g2.setColor(OBJ_FG_COLOR);

			for (int i = 0; i < oxPoints.length; i++) {
				for (int j = 0; j < oyPoints.length; j++) {
					if (i == bestObjX && j == bestObjY && !isDragging)
						g2.setColor(POINT_HIGHLIGHT_COLOR);

					g2.fillOval(
							(xPos + xOffset + oxPoints[i]) - (dot / 2),
							(yPos + yOffset + oyPoints[j]) - (dot / 2),
							dot,
							dot
					);

					if (i == bestObjX && j == bestObjY)
						g2.setColor(OBJ_FG_COLOR);
				}
			}
		}
	}

	/**
	 * Handles all property changes that the panel listens for.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		var type = e.getPropertyName();

		if (type.equals(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED)) {
			op = (ObjectPosition) e.getNewValue();
			applyPosition();
			repaint();
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void initSize(int w, int h) {
		// dimensions of panel
		setMinimumSize(new Dimension(w, h));
		setPreferredSize(new Dimension(w, h));
		
		// center point (x, y)
		var xc = w / 2;
		var yc = h / 2;
		
		offsetRatio = (float) w / DEF_PANEL_SIZE;

		// dimensions for target (node/edge) box
		if (EDGE_LABEL_POSITION.equals(vp)) {
			tw = (int) (0.7 * w);
			th = (int) (h / 6.0);
			
			nd = w - tw;
		} else {
			tw = th = (int) (0.3 * Math.min(w, h));
		}
			
		// coordinates of target (node/edge) points
		txPoints = new int[]{ xc - (tw / 2), xc, xc + (tw / 2) };
		tyPoints = new int[]{ yc - (th / 2), yc, yc + (th / 2) };

		// dimensions for object box
		ow = (int) (0.4 * ((w + h) / 2));
		oh = (int) (0.1 * ((w + h) / 2));

		// locations for object (e.g. label) points
		oxPoints = new int[]{ 0, ow / 2, ow };
		oyPoints = new int[]{ 0, oh / 2, oh };

		// diameter of a point
		dot = (int) (0.02 * ((w + h) / 2.0));

		// x/y positions for object box, initially offset
		xPos = dot;
		yPos = dot;
	}
	
	private Position parsePosition(int positionConstant) {
		for (var p : Position.values()) {
			if (p.ordinal() == positionConstant)
				return p;
		}

		return null;
	}
	
	private boolean isInsideObjectBox(int x, int y) {
		return (
				x >= (xPos + xOffset) &&
				x <= (xPos + xOffset + ow) &&
				y >= (yPos + yOffset) &&
				y <= (yPos + yOffset + oh)
		);
	}
	
	private void updateCursor(int x, int y) {
		if (canDrag || isDragging)
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		else if (isInsideObjectBox(x, y))
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		else
			setCursor(Cursor.getDefaultCursor());
	}
	
	/** Create a derived FONT that is larger, bold and has a larger spacing between characters. */
	private Font createTargetFont(Font f) {
		return f.deriveFont(Font.BOLD, TGT_FONT_SIZE).deriveFont(Map.of(TextAttribute.TRACKING, 0.2));
	}
	
	/** Create a derived FONT that is larger and bold for the SOURCE and TARGET labels (edge label position only). */
	private Font createSrcTgtNodesFont(Font f) {
		return f.deriveFont(Font.BOLD, NODES_FONT_SIZE);
	}
	
	/**
	 * Calculate the dimensions of the text and cache it, if it hasn't been calculated yet.
	 */
	private int getTextWidth(FontMetrics fm, String txt) {
		int w = txtWidths.get(txt) == null ? -1 : (int) txtWidths.get(txt);
		
		if (w <= 0)
			txtWidths.put(txt, w = fm.stringWidth(txt));
		
		return w;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class MouseClickHandler extends MouseAdapter {
		
		/**
		 * Only allows dragging if we're in the label box. Also sets the offset from
		 * where the click is and where the box is, so the box doesn't appear to jump around too much.
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();

			// click+drag within box
			if (isInsideObjectBox(x, y)) {
				canDrag = true;
				xClickOffset = x - xPos;
				yClickOffset = y - yPos;
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
		}

		/**
		 * Finds the closest points once the dragging is finished.
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if (isDragging) {
				int x = e.getX();
				int y = e.getY();

				// top right
				xPos = x - xClickOffset + xOffset;
				yPos = y - yClickOffset + yOffset;

				double best = Double.POSITIVE_INFINITY;
				double offX = 0;
				double offY = 0;

				// loop over each point in the node box
				for (int i = 0; i < txPoints.length; i++) {
					for (int j = 0; j < tyPoints.length; j++) {
						var tp = new Point(
								txPoints[i] - (dot / 2),
								tyPoints[j] - (dot / 2)
						);

						// loop over each point in the label box
						for (int a = 0; a < oxPoints.length; a++) {
							for (int b = 0; b < oyPoints.length; b++) {
								var op = new Point(
										(xPos + oxPoints[a]) - (dot / 2),
										(yPos + oyPoints[b]) - (dot / 2)
								);
								double dist = op.distance(tp);

								if (dist < best) {
									best = dist;
									bestObjX = a;
									bestObjY = b;
									bestTgtX = i;
									bestTgtY = j;
									offX = op.getX() - tp.getX();
									offY = op.getY() - tp.getY();
								}
							}
						}
					}
				}

				xPos = txPoints[bestTgtX] - oxPoints[bestObjX];
				yPos = tyPoints[bestTgtY] - oyPoints[bestObjY];

				if (Math.sqrt(offX * offX + offY * offY) > (GRAVITY_DISTANCE + (dot / 2))) {
					xOffset = (int) offX;
					yOffset = (int) offY;
				} else {
					xOffset = 0;
					yOffset = 0;
				}

				op.setOffsetX(xOffset);
				op.setOffsetY(yOffset);
				op.setAnchor(parsePosition(bestObjX + (3 * bestObjY)));
				op.setTargetAnchor(parsePosition(bestTgtX + (3 * bestTgtY)));
				firePropertyChange(ObjectPlacerGraphic.OBJECT_POSITION_CHANGED, null, op);

				repaint();
				isDragging = false;
				canDrag = false;
				updateCursor(x, y);
			}
		}
	}
	
	private class MouseDragHandler extends MouseMotionAdapter {
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if (!canDrag && !isDragging)
				updateCursor(e.getX(), e.getY());
		}
		
		/**
		 * Handles redrawing for dragging.
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			// dragging within normal box
			if (canDrag) {
				xPos = e.getX() - xClickOffset;
				yPos = e.getY() - yClickOffset;

				isDragging = true;
				repaint();
			}
		}
	}
}
