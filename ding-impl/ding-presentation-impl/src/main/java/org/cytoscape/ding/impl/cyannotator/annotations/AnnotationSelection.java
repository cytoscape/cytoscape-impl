package org.cytoscape.ding.impl.cyannotator.annotations;

import static org.cytoscape.view.presentation.property.values.Position.*;

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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.view.presentation.property.values.Position;

@SuppressWarnings("serial")
public class AnnotationSelection extends JComponent implements Iterable<DingAnnotation> {
	
	Rectangle2D union;
	Rectangle2D[] anchors = new Rectangle2D[8];
	double zoom;
	CyAnnotator cyAnnotator;
	static float border = 2f;
	static float[] dash = { 10.0f, 10.0f };
	Point2D initialPos;
	Rectangle2D initialBounds;
	Rectangle2D initialUnion;
	Position anchor;
	boolean resizing;
	boolean moving;

	private Set<DingAnnotation> selectedAnnotations;

	public AnnotationSelection(CyAnnotator cyAnnotator) {
		this.cyAnnotator = cyAnnotator;
		selectedAnnotations = new HashSet<>();
	}

	public void add(DingAnnotation e) {
		if (selectedAnnotations.size() == 0) {
			selectedAnnotations.add(e);
			updateBounds();
			cyAnnotator.getForeGroundCanvas().setSelection(this);
		} else {
			selectedAnnotations.add(e);
			updateBounds();
		}
		cyAnnotator.getForeGroundCanvas().repaint();
	}

	public boolean isEmpty() {
		return selectedAnnotations.isEmpty();
	}

	public Set<DingAnnotation> getSelectedAnnotations() {
		return new HashSet<>(selectedAnnotations);
	}

	public void clear() {
		for (DingAnnotation a: selectedAnnotations)
			a.setOffset(null);
		
		selectedAnnotations.clear();
		cyAnnotator.getForeGroundCanvas().setSelection(null);
	}

	public boolean contains(Object e) {
		return selectedAnnotations.contains(e);
	}

	@Override
	public Iterator<DingAnnotation> iterator() {
		return selectedAnnotations.iterator();
	}

	public int count() {
		return selectedAnnotations.size();
	}

	public void remove(Object e) {
		selectedAnnotations.remove(e);
		
		if (selectedAnnotations.isEmpty())
			cyAnnotator.getForeGroundCanvas().setSelection(null);
		else
			updateBounds();
		
		cyAnnotator.getForeGroundCanvas().repaint();
	}

	public void initialPosition(int x, int y, Position anchor) {
		initialPos = new Point2D.Double(x,y);
		this.anchor = anchor;
	}

	public void saveBounds() {
		initialBounds = ViewUtils.getNodeCoordinates(cyAnnotator.getView(), getBounds().getBounds2D());
		initialUnion  = ViewUtils.getNodeCoordinates(cyAnnotator.getView(), union.getBounds2D());
		
		for(DingAnnotation da : selectedAnnotations) {
			da.saveBounds();
		}
	}

	public Rectangle2D getInitialBounds() {
		return initialBounds;
	}

	public Position overAnchor(int x, int y) {
		// Get our current transform
		double[] nextLocn = new double[2];
		nextLocn[0] = (double) x - getX();
		nextLocn[1] = (double) y - getY();
		
		return overAnchor(nextLocn[0], nextLocn[1]);
	}

	public Position overAnchor(double x, double y) {
		// OK, now given our current selection, we need to see if we're over an anchor
		for (int pos = 0; pos < 8; pos++) {
			if (isOver(x, y, anchors[pos]))
				return getPosition(pos);
		}
		
		return null;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setResizing(boolean resizing) {
		this.resizing = resizing;
	}

	public boolean isResizing() {
		return resizing;
	}

	/**
	 * Assumes x and y are component (mouse) coordinates
	 */
	public void moveSelection(int x, int y) {
  	// Get our current transform
		Point2D pt = ViewUtils.getNodeCoordinates(cyAnnotator.getView(), x, y);

		for (DingAnnotation annotation: selectedAnnotations) {
			// OK, now update
			annotation.moveAnnotationRelative(pt);
			annotation.update();
			annotation.getCanvas().repaint();
		}

		updateBounds();
		cyAnnotator.getForeGroundCanvas().repaint();
	}

	public void resizeAnnotationsRelative(int x, int y) {
		// Get our current transform, and compensate for the border
		Point2D mouse = ViewUtils.getNodeCoordinates(cyAnnotator.getView(), x, y);
		
		// compensate for the border
		final float d = border * 2;
		double mouseX = mouse.getX();
		double mouseY = mouse.getY();
		
		if(isNorth(anchor))
			mouseY += d;
		if(isSouth(anchor))
			mouseY -= d;
		if(isWest(anchor))
			mouseX += d;
		if(isEast(anchor))
			mouseX -= d;
		
		// OutlineBounds is in node coordinates!
		Rectangle2D outlineBounds = resize(anchor, initialUnion, mouseX, mouseY);

		for (DingAnnotation da : selectedAnnotations) {
			((AbstractAnnotation)da).resizeAnnotationRelative(initialUnion, outlineBounds);

			// OK, now update
			da.update();
			da.getCanvas().repaint();
		}

		updateBounds();
		cyAnnotator.getForeGroundCanvas().repaint();
	}

	// NOTE: bounds, mouseX and mouseY should be in node coordinates
	private Rectangle2D resize(Position anchor, Rectangle2D bounds, double mouseX, double mouseY) {
		if(anchor == NONE || anchor == CENTER)
			return null;
		
		final double boundsX = bounds.getX();
		final double boundsY = bounds.getY();
		final double boundsWidth = bounds.getWidth();
		final double boundsHeight = bounds.getHeight();
		final double boundsYBottom = boundsY + boundsHeight;
		final double boundsXLeft = boundsX + boundsWidth;

		double x = boundsX;
		double y = boundsY;
		double width = boundsWidth;
		double height = boundsHeight;
		
		// y and height
		if(isNorth(anchor)) {
			if(mouseY > boundsYBottom) {
				y = boundsYBottom;
				height = mouseY - boundsYBottom;
			} else {
				y = mouseY;
				height = boundsYBottom - mouseY;
			}
		} else if(isSouth(anchor)) {
			if(mouseY < boundsY) {
				y = mouseY;
				height = boundsY - mouseY;
			} else {
				height = mouseY - boundsY;
			}
		}
		
		// x and width
		if(isWest(anchor)) {
			if(mouseX > boundsXLeft) {
				x = boundsXLeft;
				width = mouseX - boundsXLeft;
			} else {
				x = mouseX;
				width = boundsXLeft - mouseX;
			}
		} else if(isEast(anchor)) {
			if(mouseX < boundsX) {
				x = mouseX;
				width = boundsX - mouseX;
			} else {
				width = mouseX - boundsX; 
			}
		}
		
		return new Rectangle2D.Double(x, y, width, height);
	}

	
	private void updateBounds() {
		if (selectedAnnotations.size() == 0)
			return;

		union = null;
		for (DingAnnotation a: selectedAnnotations) {
			if (union == null)
				union = a.getComponent().getBounds().getBounds2D();
			else
				union = union.createUnion(a.getComponent().getBounds().getBounds2D());
		}
		setSize((int)(union.getWidth()+border*8), (int)(union.getHeight()+border*8));
		setLocation((int)(union.getX()-border*4), (int)(union.getY()-border*4));
	}

	@Override
	public void paint(Graphics g) {
		updateBounds();

		Graphics2D g2 = (Graphics2D)g;

		/* Set up all of our anti-aliasing, etc. here to avoid doing it redundantly */
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		// High quality color rendering is ON.
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		// Text antialiasing is ON.
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		final Composite originalComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

		g2.setPaint(Color.YELLOW);
		g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f));

		// Draw the bounding box
		Rectangle2D shape = new Rectangle2D.Double(border*4,border*4,union.getWidth()+border*2,union.getHeight()+border*2);
		g2.draw(shape);

		if (cyAnnotator.getView().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
			g2.setPaint(Color.GRAY);

			// Draw the anchors
			// g2.setStroke(new BasicStroke(1.0f));
			anchors[0] = new Rectangle2D.Double(0, 0, border*4, border*4);
			g2.fill(anchors[0]);

			anchors[1] = new Rectangle2D.Double(shape.getWidth()/2, 0, border*4,border*4);
			g2.fill(anchors[1]);

			anchors[2] = new Rectangle2D.Double(shape.getWidth()+border*2, 0, border*4, border*4);
			g2.fill(anchors[2]);

			anchors[3] = new Rectangle2D.Double(shape.getWidth()+border*2, shape.getHeight()/2, border*4, border*4);
			g2.fill(anchors[3]);

			anchors[4] = new Rectangle2D.Double(shape.getWidth()+border*2, shape.getHeight()+border*2, border*4, border*4);
			g2.fill(anchors[4]);

			anchors[5] = new Rectangle2D.Double(shape.getWidth()/2, shape.getHeight()+border*2, border*4, border*4);
			g2.fill(anchors[5]);

			anchors[6] = new Rectangle2D.Double(0, shape.getHeight()+border*2, border*4, border*4);
			g2.fill(anchors[6]);

			anchors[7] = new Rectangle2D.Double(0, shape.getHeight()/2, border*4, border*4);
			g2.fill(anchors[7]);
		}

		g2.setComposite(originalComposite);
	}

	private boolean isOver(double x, double y, Rectangle2D anchor) {
		return anchor.contains(x, y);
	}

	private Position getPosition(int pos) {
		switch (pos) {
			case 0:
				return Position.NORTH_WEST;
			case 1:
				return Position.NORTH;
			case 2:
				return Position.NORTH_EAST;
			case 3:
				return Position.EAST;
			case 4:
				return Position.SOUTH_EAST;
			case 5:
				return Position.SOUTH;
			case 6:
				return Position.SOUTH_WEST;
			case 7:
				return Position.WEST;
		}
		return null;
	}
	
	private static boolean isNorth(Position anchor) {
		return anchor == NORTH || anchor == NORTH_EAST || anchor == NORTH_WEST;
	}
	
	private static boolean isSouth(Position anchor) {
		return anchor == SOUTH || anchor == SOUTH_EAST || anchor == SOUTH_WEST;
	}
	
	private static boolean isWest(Position anchor) {
		return anchor == WEST || anchor == NORTH_WEST || anchor == SOUTH_WEST;
	}
	
	private static boolean isEast(Position anchor) {
		return anchor == EAST || anchor == NORTH_EAST || anchor == SOUTH_EAST;
	}

}
