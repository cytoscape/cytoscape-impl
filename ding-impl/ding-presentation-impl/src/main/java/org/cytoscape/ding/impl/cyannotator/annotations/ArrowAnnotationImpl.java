package org.cytoscape.ding.impl.cyannotator.annotations;

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

import java.util.Map;
import java.util.UUID;

import org.cytoscape.model.CyNode;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DNodeView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation.ArrowType;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation.ArrowEnd;
import org.cytoscape.ding.impl.cyannotator.dialogs.ArrowAnnotationDialog;

public class ArrowAnnotationImpl extends AbstractAnnotation implements ArrowAnnotation {
	private Paint lineColor = Color.BLACK; // These are paint's so we can do gradients
	private float lineWidth = 1.0f;

	private Annotation source = null;
	private ArrowType sourceType = ArrowType.NONE;
	private Paint sourceColor = null;
	private double sourceSize = 1.0;

	private Object target = null;
	private ArrowType targetType = ArrowType.OPEN;
	private Paint targetColor = null;
	private double targetSize = 1.0;

	private double previousZoom = 1.0;
	private double shapeWidth = 0.0;
	private double shapeHeight = 0.0;

	private double xOffset = 0.0;
	private double yOffset = 0.0;

	private Line2D arrowLine = null;

	public static final String NAME="ARROW";
	protected static final String ARROWCOLOR = "lineColor";
	protected static final String ARROWTHICKNESS = "lineThickness";

	protected static final String SOURCEANN = "sourceAnnotation";
	protected static final String SOURCETYPE = "sourceType";
	protected static final String SOURCESIZE = "sourceSize";
	protected static final String SOURCECOLOR = "sourceColor";

	protected static final String TARGETPOINT = "targetPoint";
	protected static final String TARGETANN = "targetAnnotation";
	protected static final String TARGETNODE = "targetNode";

	protected static final String TARGETTYPE = "targetType";
	protected static final String TARGETSIZE = "targetSize";
	protected static final String TARGETCOLOR = "targetColor";


	public ArrowAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view) {
		super(cyAnnotator, view);
	}

	public ArrowAnnotationImpl(ArrowAnnotationImpl c) {
		super(c);

		// Line parameters
		this.lineColor = c.lineColor;
		this.lineWidth = c.lineWidth;

		// Source arrow parameters
		this.source = c.source;
		this.sourceType = c.sourceType;
		this.sourceColor = c.sourceColor;
		this.sourceSize = c.sourceSize;

		// Target arrow parameters
		this.target = c.target;
		this.targetType = c.targetType;
		this.targetColor = c.targetColor;
		this.targetSize = c.targetSize;
	}

  public ArrowAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view,
	                           Annotation source, Object target, float lineWidth,
	                           Paint lineColor, 
	                           ArrowType sourceType, Paint sourceColor, float sourceSize,
	                           ArrowType targetType, Paint targetColor, float targetSize) {
		super(cyAnnotator, view, source.getComponent().getX(), source.getComponent().getY(), view.getZoom());

		// Line parameters
		this.lineColor = lineColor;
		this.lineWidth = lineWidth;

		// Source arrow parameters
		this.source = source;
		this.sourceType = sourceType;
		this.sourceColor = sourceColor;
		this.sourceSize = sourceSize;

		// Target arrow parameters
		this.target = target;
		this.targetType = targetType;
		this.targetColor = targetColor;
		this.targetSize = targetSize;

		updateBounds();
  }

  public ArrowAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
    super(cyAnnotator, view, argMap);

    this.lineColor = getColor(argMap, ARROWCOLOR, Color.BLACK);
		this.lineWidth = getFloat(argMap, ARROWTHICKNESS, 1.0f);

		// Source
		if (argMap.containsKey(SOURCEANN)) {
			UUID uuid = UUID.fromString(argMap.get(SOURCEANN));
			source = cyAnnotator.getAnnotation(uuid);
		}

		// Source Arrow
    this.sourceType = GraphicsUtilities.getArrowType(argMap, SOURCETYPE, ArrowType.NONE);
    this.sourceSize = getDouble(argMap, SOURCESIZE, 5.0);
    this.sourceColor = getColor(argMap, SOURCECOLOR, null); // A null color = line color

		// Target Arrow
    this.targetType = GraphicsUtilities.getArrowType(argMap, TARGETTYPE, ArrowType.NONE);
    this.targetSize = getDouble(argMap, TARGETSIZE, 5.0);
    this.targetColor = getColor(argMap, TARGETCOLOR, null); // A null color = line color

		// Figure out the target
		if (argMap.containsKey(TARGETPOINT)) {
			String point = argMap.get(TARGETPOINT);
			String[] xy = point.split(",");
			double[] nextLocn = new double[2];
			nextLocn[0] = Double.parseDouble(xy[0]);
			nextLocn[1] = Double.parseDouble(xy[1]);
			view.xformNodeToComponentCoords(nextLocn);
			target = new Point2D.Double(nextLocn[0], nextLocn[1]);
		} else if (argMap.containsKey(TARGETANN)) {
			UUID uuid = UUID.fromString(argMap.get(TARGETANN));
			target = cyAnnotator.getAnnotation(uuid);
		} else if (argMap.containsKey(TARGETNODE)) {
			String point = argMap.get(TARGETNODE);
			String[] xy = point.split(",");
			double x = Double.parseDouble(xy[0]);
			double y = Double.parseDouble(xy[1]);
			DNodeView nv = (DNodeView)view.getPickedNodeView(new Point2D.Double(x,y));
			target = nv.getModel();
		}
		updateBounds();
  }

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,NAME);
		if (this.lineColor != null)
			argMap.put(ARROWCOLOR,convertColor(this.lineColor));
		argMap.put(ARROWTHICKNESS, Float.toString(this.lineWidth));

		argMap.put(SOURCEANN, source.getUUID().toString());

		argMap.put(SOURCETYPE, Integer.toString(this.sourceType.ordinal()));
		argMap.put(SOURCESIZE, Double.toString(this.sourceSize));
		if (this.sourceColor != null)
			argMap.put(SOURCECOLOR,convertColor(this.sourceColor));

		if (target instanceof Point2D) {
			Point2D xy = (Point2D) target;
			argMap.put(TARGETPOINT,Double.toString(xy.getX())+","+Double.toString(xy.getY()));
		} else if (target instanceof Annotation) {
			argMap.put(TARGETANN,((Annotation)target).getUUID().toString());
		} else if (target instanceof CyNode) {
			CyNode node = (CyNode)target;
			DNodeView nv = (DNodeView)view.getNodeView((CyNode)target);
			double xCenter = nv.getXPosition();
			double yCenter = nv.getYPosition();
			argMap.put(TARGETNODE,
			           Double.toString(xCenter)+","+Double.toString(yCenter));
		}

		argMap.put(TARGETTYPE, Integer.toString(this.targetType.ordinal()));
		argMap.put(TARGETSIZE, Double.toString(this.targetSize));
		if (this.targetColor != null)
			argMap.put(TARGETCOLOR,convertColor(this.targetColor));
		return argMap;
	}

	public Annotation getSource() { return this.source; }
	public void setSource(Annotation source) { 
		if (this.source != null)
			source.removeArrow(this);
		this.source = source; 
		source.addArrow(this);

		updateBounds();
	}

	public Object getTarget() { return this.target; }
	public void setTarget(Annotation target) { 
		this.target = target; 
		updateBounds();
	}
	public void setTarget(CyNode target) { 
		this.target = target; 
		updateBounds();
	}
	public void setTarget(Point2D target) { 
		// Convert target to node coordinates
		this.target = getNodeCoordinates(target.getX(), target.getY()); 
		updateBounds();
	}

	public double getLineWidth() { return (double)lineWidth; }
	public void setLineWidth(double width) { this.lineWidth = (float)width; }

	public double getArrowSize(ArrowEnd end) { 
		if (end == ArrowEnd.SOURCE)
			return this.sourceSize; 
		else
			return this.targetSize; 
	}
	public void setArrowSize(ArrowEnd end, double width) { 
		if (end == ArrowEnd.SOURCE)
			this.sourceSize = width; 
		else
			this.targetSize = width; 
	}

	public ArrowType getArrowType(ArrowEnd end) { 
		if (end == ArrowEnd.SOURCE)
			return this.sourceType; 
		else
			return this.targetType; 
	}

	public void setArrowType(ArrowEnd end, ArrowType type) { 
		if (end == ArrowEnd.SOURCE)
			this.sourceType = type; 
		else
			this.targetType = type; 
	}

	public Paint getLineColor() { 
		return this.lineColor;
	}

	public void setLineColor(Paint clr) { 
		this.lineColor = clr;
	}

	public Paint getArrowColor(ArrowEnd end) { 
		if (end == ArrowEnd.SOURCE)
			return this.sourceColor; 
		else
			return this.targetColor; 
	}
	public void setArrowColor(ArrowEnd end, Paint color) { 
		if (end == ArrowEnd.SOURCE)
			this.sourceColor = color; 
		else
			this.targetColor = color; 
	}

	@Override
	public void setZoom(double zoom) {
		float factor=(float)(zoom/getZoom());
		lineWidth*=factor;
		updateBounds();
		super.setZoom(zoom);
	}

	@Override
	public void setSpecificZoom(double zoom) {
		float factor=(float)(zoom/getSpecificZoom());
		super.setSpecificZoom(zoom);		
	}

	public ArrowType[] getSupportedArrows() { return GraphicsUtilities.getSupportedArrowTypes(); }
    
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);

		// Draw the line
		Graphics2D g2 = (Graphics2D)g;

		boolean selected = isSelected();
		setSelected(false);

		double scale = scaleFactor/getZoom();

		// Get the stroke
		double border = lineWidth*scale;
		if (border < 1.0) border = 1.0;
		g2.setPaint(lineColor);
		g2.setStroke(new BasicStroke((float)border));
		
		Line2D relativeLine = getRelativeLine(arrowLine, 
		                                      x*scaleFactor, y*scaleFactor, scale, border);

		if (relativeLine != null) {
			g2.draw(relativeLine);
		}

		// Add the head
		if (sourceType != ArrowType.NONE) {
			Paint color = sourceColor;
			if (color == null)
				color = lineColor;

			GraphicsUtilities.drawArrow(g, relativeLine, ArrowEnd.SOURCE, color, sourceSize*10.0*scaleFactor, sourceType);
		}

		if (targetType != ArrowType.NONE) {
			Paint color = targetColor;
			if (color == null)
				color = lineColor;

			GraphicsUtilities.drawArrow(g, relativeLine, ArrowEnd.TARGET, color, targetSize*10.0*scaleFactor, targetType);
		}

		setSelected(selected);
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (canvas.isPrinting())
			drawArrow(g, true);
		else
			drawArrow(g, false);
	}

	public void drawArrow(Graphics g, boolean isPrinting) {
		if ( (source == null || target == null) && !usedForPreviews ) return;

		if (!usedForPreviews)
			updateBounds();
		else
			arrowLine = getArrowLine(target, source);

		// Draw the line
		Graphics2D g2 = (Graphics2D)g;

		// Get the stroke
		float border = (float)(lineWidth/2.0);
		if (!isPrinting && border < 1.0f) border = 1.0f;
		g2.setPaint(lineColor);
		g2.setStroke(new BasicStroke(border, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, 10.0f));
		
		Line2D relativeLine = getRelativeLine(arrowLine, 0.0, 0.0, 1.0, border);
		g2.draw(relativeLine);

		g2.setStroke(new BasicStroke(border));

		// Add the head
		if (sourceType != ArrowType.NONE) {
			GraphicsUtilities.drawArrow(g, relativeLine, ArrowEnd.SOURCE, sourceColor, sourceSize*10.0*getZoom(), sourceType);
		}

		if (targetType != ArrowType.NONE) {
			GraphicsUtilities.drawArrow(g, relativeLine, ArrowEnd.TARGET, targetColor, targetSize*10.0*getZoom(), targetType);
		}
	}

	@Override
	public void update() {
		updateBounds();
		super.update();
	}

	public void setSize(double width, double height) {
		shapeWidth = width;
		shapeHeight = height;
		setSize((int)shapeWidth, (int)shapeHeight);
	}

	public JFrame getModifyDialog() {
		return new ArrowAnnotationDialog(this);
	}

	private Line2D getRelativeLine(Line2D line, double x, double y, 
	                               double scaleFactor, double stroke) {
		if (usedForPreviews || line == null) 
			return line;

		double x1 = line.getX1()*scaleFactor;
		double x2 = line.getX2()*scaleFactor;
		double width = Math.abs(x2-x1)+xOffset*scaleFactor;
		double y1 = line.getY1()*scaleFactor;
		double y2 = line.getY2()*scaleFactor;
		double height = Math.abs(y2-y1)+yOffset*scaleFactor;
		if (y2 < y1) {
			y1 = height+y-stroke;
			y2 = yOffset*scaleFactor+y+stroke;
		} else {
			y1 = yOffset*scaleFactor+y+stroke;
			y2 = height+y-stroke;
		}
		if (x2 < x1) {
			x1 = width+x-stroke;
			x2 = xOffset*scaleFactor+x+stroke;
		} else {
			x1 = xOffset*scaleFactor+x+stroke;
			x2 = width+x-stroke;
		}
		return new Line2D.Double(x1, y1, x2, y2);
	}

	private void updateBounds() {
		xOffset = 0.0; yOffset = 0.0;

		// We need to take into account our arrows
		if (targetType != ArrowType.NONE) {
			xOffset = targetSize*10.0*getZoom() + lineWidth;
			yOffset = targetSize*10.0*getZoom() + lineWidth;
		}

		if (sourceType != ArrowType.NONE) {
			xOffset += sourceSize*10.0*getZoom() + lineWidth;
			yOffset += sourceSize*10.0*getZoom() + lineWidth;
		}

		// Update our bounds
		if (source != null && target != null) {
			arrowLine = getArrowLine(target, source);
			double x1 = arrowLine.getX1();
			double y1 = arrowLine.getY1();
			double x2 = arrowLine.getX2();
			double y2 = arrowLine.getY2();
			setLocation((int)(Math.min(x1, x2)-xOffset), (int)(Math.min(y1, y2)-yOffset));
			setSize(Math.abs(x1-x2)+xOffset*2, Math.abs(y1-y2)+yOffset*2);
		}
	}

	private Line2D getArrowLine(Object target, Annotation source) {
		if (usedForPreviews) {
			return new Line2D.Double(10.0, shapeHeight/2, shapeWidth-20.0, shapeHeight/2);
		}

		Point2D targetPoint = null;
		Point2D sourceCenter = centerPoint(source.getComponent().getBounds());
		if (target instanceof Point2D) {
			targetPoint = getComponentCoordinates(((Point2D)target).getX(), ((Point2D)target).getY());
		} else if (target instanceof Annotation) {
			Annotation a = (Annotation)target;
			// get the bounds
			Rectangle targetBounds = a.getComponent().getBounds();
			// Find the closest face and return
			targetPoint = findFace(sourceCenter, targetBounds);
		} else if (target instanceof CyNode) {
			// get the target point from ding
			DNodeView nv = (DNodeView)view.getNodeView((CyNode)target);
			Rectangle2D nodeBounds = getNodeBounds(nv);
			targetPoint = findFace(sourceCenter, nodeBounds);
		}

		Rectangle sourceBounds = source.getComponent().getBounds();
		Point2D sourcePoint = findFace(targetPoint, sourceBounds);
		return new Line2D.Double(sourcePoint, targetPoint);
	}

	private Point2D centerPoint(Rectangle2D bounds) {
		return new Point2D.Double(bounds.getX()+bounds.getWidth()/2.0,
		                          bounds.getY()+bounds.getHeight()/2.0);
		
	}

	// Find the mid point to draw the target to
	private Point2D findFace(Point2D source, Rectangle2D target) {
		Point2D.Double left = new Point2D.Double(target.getX(), target.getY()+target.getHeight()/2.0);
		Point2D.Double right = new Point2D.Double(target.getX()+target.getWidth(), target.getY()+target.getHeight()/2.0);
		Point2D.Double top = new Point2D.Double(target.getX()+target.getWidth()/2.0, target.getY());
		Point2D.Double bottom = new Point2D.Double(target.getX()+target.getWidth()/2.0, target.getY()+target.getHeight());

		Line2D.Double topline = new Line2D.Double(target.getX(), target.getY(), 
		                                          target.getX()+target.getWidth(), target.getY());
		Line2D.Double bottomline = new Line2D.Double(target.getX(), target.getY()+target.getHeight(), 
		                                             target.getX()+target.getWidth(), target.getY()+target.getHeight());
		Line2D.Double rightline = new Line2D.Double(target.getX()+target.getWidth(), target.getY(), 
		                                            target.getX()+target.getWidth(), target.getY()+target.getHeight());
		Line2D.Double leftline = new Line2D.Double(target.getX(), target.getY(), 
		                                           target.getX(), target.getY()+target.getHeight());

		if (source.getX() <= target.getX()) {
			// Left
			if (source.getY() == target.getY()) {
				return left;
			} else if (source.getY() < target.getY()) {
				// Top or left
				if (topline.ptSegDist(source) < leftline.ptSegDist(source))
					return top;
				else
					return left;
			} else {
				// Bottom left (maybe)
				if (bottomline.ptSegDist(source) < leftline.ptSegDist(source))
					return bottom;
				else
					return left;
			}
		} else {
			// Right
			if (source.getY() == target.getY()) {
				return right;
			} else if (source.getY() < target.getY()) {
				// Top right (maybe)
				if (topline.ptSegDist(source) < rightline.ptSegDist(source))
					return top;
				else
					return right;
			} else {
				// Bottom right (maybe)
				if (bottomline.ptSegDist(source) < rightline.ptSegDist(source))
					return bottom;
				else
					return right;
			}
		}
	}

	private Rectangle2D getNodeBounds(DNodeView nv) {
		double[] nextLocn = new double[2];

		// First, get our starting and ending points in node coordinates
		double xCenter = nv.getXPosition();
		double yCenter = nv.getYPosition();

		double width = nv.getWidth();
		double height = nv.getHeight();

		double xStart = xCenter-width/2.0;
		double yStart = yCenter-height/2.0;

		double xEnd = xStart+width;
		double yEnd = yStart+height;

		// Now convert to component coordinates
		nextLocn[0] = xStart;
		nextLocn[1] = yStart;
		view.xformNodeToComponentCoords(nextLocn);

		double x = nextLocn[0];
		double y = nextLocn[1];

		nextLocn[0] = xEnd;
		nextLocn[1] = yEnd;
		view.xformNodeToComponentCoords(nextLocn);

		width = nextLocn[0]-x;
		height = nextLocn[1]-y;

		return new Rectangle2D.Double(x, y, width, height);
	}
}
