package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.dialogs.ArrowAnnotationDialog;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

public class ArrowAnnotationImpl extends AbstractAnnotation implements ArrowAnnotation {
	
	private Paint lineColor = Color.BLACK; // These are paint's so we can do gradients
	private float lineWidth = 1.0f;

	private DingAnnotation source;
	private ArrowType sourceType = ArrowType.NONE;
	private AnchorType sourceAnchorType = AnchorType.ANCHOR;
	private Paint sourceColor;
	private double sourceSize = 1.0;

	private Object target;
	private ArrowType targetType = ArrowType.OPEN;
	private AnchorType targetAnchorType = AnchorType.ANCHOR;
	private Paint targetColor;
	private double targetSize = 1.0;

	private double xOffset;
	private double yOffset;

	private Line2D arrowLine;

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

	public enum ArrowType {
		CIRCLE("Circle"),
		CLOSED("Closed Arrow"),
		CONCAVE("Concave Arrow"),
		DIAMOND("Diamond"),
		OPEN("Open Arrow"),
		NONE("No Arrow"),
		X("X Arrow"),
		TRIANGLE("Triangular Head"),
		TSHAPE("T-Shape");

		private final String name;

		ArrowType(String name) {
			this.name = name;
		}

		public String arrowName() {
			return this.name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	public ArrowAnnotationImpl(DRenderingEngine re, boolean usedForPreviews) {
		super(re, usedForPreviews);
	}


	public ArrowAnnotationImpl(DRenderingEngine re, Map<String,String> argMap) {
		super(re, argMap);

		this.lineColor = ViewUtils.getColor(argMap, ARROWCOLOR, Color.BLACK);
		
		double zoom = getLegacyZoom(argMap);
		this.lineWidth = ViewUtils.getFloat(argMap, ARROWTHICKNESS, 1.0f) / (float)zoom;

		// Source
		if (argMap.containsKey(SOURCEANN)) {
			UUID uuid = UUID.fromString(argMap.get(SOURCEANN));
			source = cyAnnotator.getAnnotation(uuid);
		}

		// Source Arrow
		this.sourceType = GraphicsUtilities.getArrowType(argMap, SOURCETYPE, ArrowType.NONE);
		this.sourceSize = ViewUtils.getDouble(argMap, SOURCESIZE, 5.0);
		this.sourceColor = ViewUtils.getColor(argMap, SOURCECOLOR, null); // A null color = line color

		// Target Arrow
		this.targetType = GraphicsUtilities.getArrowType(argMap, TARGETTYPE, ArrowType.NONE);
		this.targetSize = ViewUtils.getDouble(argMap, TARGETSIZE, 5.0);
		this.targetColor = ViewUtils.getColor(argMap, TARGETCOLOR, null); // A null color = line color

		// Figure out the target
		if (argMap.containsKey(TARGETPOINT)) {
			String point = argMap.get(TARGETPOINT);
			String[] xy = point.split(",");
			double x = Double.parseDouble(xy[0]);
			double y = Double.parseDouble(xy[1]);
			target = new Point2D.Double(x, y);
		} else if (argMap.containsKey(TARGETANN)) {
			UUID uuid = UUID.fromString(argMap.get(TARGETANN));
			target = cyAnnotator.getAnnotation(uuid);
		} else if (argMap.containsKey(TARGETNODE)) {
			String point = argMap.get(TARGETNODE);
			String[] xy = point.split(",");
			double centerX = Double.parseDouble(xy[0]);
			double centerY = Double.parseDouble(xy[1]);
			// MKTODO This is a terrible way of looking up the node. What if there are overlapping nodes???
			target = re.getPicker().getNodeForArrowAnnotation(centerX, centerY);
		}
		updateBounds();
	}
	
	@Override
	public Class<? extends Annotation> getType() {
		return ArrowAnnotation.class;
	}

	@Override
	public Map<String, String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE, ArrowAnnotation.class.getName());

		if (this.lineColor != null)
			argMap.put(ARROWCOLOR, ViewUtils.convertColor(this.lineColor));

		argMap.put(ARROWTHICKNESS, Float.toString(this.lineWidth));

		if (source != null)
			argMap.put(SOURCEANN, source.getUUID().toString());

		argMap.put(SOURCETYPE, Integer.toString(this.sourceType.ordinal()));
		argMap.put(SOURCESIZE, Double.toString(this.sourceSize));

		if (this.sourceColor != null)
			argMap.put(SOURCECOLOR, ViewUtils.convertColor(this.sourceColor));

		if (target instanceof Point2D) {
			Point2D xy = (Point2D) target;
			argMap.put(TARGETPOINT, Double.toString(xy.getX()) + "," + Double.toString(xy.getY()));
		} else if (target instanceof Annotation) {
			argMap.put(TARGETANN, ((DingAnnotation) target).getUUID().toString());
		} else if (target instanceof View) {
			View<CyNode> nv = (View<CyNode>) target;
			if (nv != null) {
				double xCenter = re.getNodeDetails().getXPosition(nv);
				double yCenter = re.getNodeDetails().getYPosition(nv);
				argMap.put(TARGETNODE, Double.toString(xCenter) + "," + Double.toString(yCenter));
			}
		}

		argMap.put(TARGETTYPE, Integer.toString(this.targetType.ordinal()));
		argMap.put(TARGETSIZE, Double.toString(this.targetSize));

		if (this.targetColor != null)
			argMap.put(TARGETCOLOR, ViewUtils.convertColor(this.targetColor));

		return argMap;
	}

	@Override
	public Annotation getSource() { 
		return this.source;
	}
	
	
	@Override
	public void setSource(Annotation source) { 
		if (this.source != null)
			((DingAnnotation)source).removeArrow(this);
		this.source = (DingAnnotation)source; 
		
		if (source != null)
			source.addArrow(this);

		update();
	}
	
	@Override
	public Object getTarget() {
		return this.target; 
	}
	
	@Override
	public void setTarget(Annotation target) { 
		this.target = target; 
		update();
	}

	@Override
	@Deprecated
	public void setTarget(CyNode target) { 
		// This is only here for backwards compatibility, do not use
		View<CyNode> nv = re.getViewModelSnapshot().getNodeView(target);
		setTarget(nv);
	}
	
	public void setTarget(View<CyNode> target) { 
		this.target = target; 
		update();
	}

	@Override
	public void setTarget(Point2D target) { 
		this.target = target; 
		update();
	}

	@Override
	public double getLineWidth() { 
		return (double)lineWidth; 
		
	}
	
	@Override
	public void setLineWidth(double width) { 
		this.lineWidth = (float)width; 
		update();
	}

	@Override
	public double getArrowSize(ArrowEnd end) { 
		return (end == ArrowEnd.SOURCE) ? sourceSize : targetSize; 
	}
	@Override
	public void setArrowSize(ArrowEnd end, double width) { 
		if (end == ArrowEnd.SOURCE)
			this.sourceSize = width; 
		else
			this.targetSize = width; 
		update();
	}

	@Override
	public String getArrowType(ArrowEnd end) { 
		return (end == ArrowEnd.SOURCE) ? sourceType.arrowName() : targetType.arrowName(); 
	}

	@Override
	public void setArrowType(ArrowEnd end, String type) { 
		ArrowType aType = null;

		for (ArrowType t: ArrowType.values()) {
			if (t.arrowName().equals(type)) {
				aType = t;
			}
		}

		if (aType == null) 
			return;

		if (end == ArrowEnd.SOURCE)
			this.sourceType = aType; 
		else
			this.targetType = aType; 
		update();
	}

	@Override
	public AnchorType getAnchorType(ArrowEnd end) {
		return (end == ArrowEnd.SOURCE) ? sourceAnchorType : targetAnchorType; 
	}

	@Override
	public void setAnchorType(ArrowEnd end, AnchorType type) {
		if (end == ArrowEnd.SOURCE)
			this.sourceAnchorType = type;
		else
			this.targetAnchorType = type;
		update();
	}

	@Override
	public Paint getLineColor() { 
		return this.lineColor;
	}

	@Override
	public void setLineColor(Paint clr) { 
		this.lineColor = clr;
		update();
	}

	@Override
	public Paint getArrowColor(ArrowEnd end) { 
		return (end == ArrowEnd.SOURCE) ? sourceColor : targetColor; 
	}

	@Override
	public void setArrowColor(ArrowEnd end, Paint color) { 
		if (end == ArrowEnd.SOURCE)
			this.sourceColor = color; 
		else
			this.targetColor = color; 
		update();
	}

	@Override
	public List<String> getSupportedArrows() {
		return GraphicsUtilities.getSupportedArrowTypeNames();
	}

	@Override
	public void paint(Graphics g, boolean showSelected) {
		super.paint(g, showSelected);
//		if (canvas.isPrinting())
//			drawArrow(g, true);
//		else
			drawArrow(g, false);
	}

	public void drawArrow(Graphics g, boolean isPrinting) {
		if ((source == null || target == null) && !usedForPreviews)
			return;

		if (!usedForPreviews && !isPrinting)
			updateBounds();
		else
			arrowLine = getArrowLine(target, source);

		// Draw the line
		Graphics2D g2 = (Graphics2D)g;

		// Get the stroke
		float border = (float)(lineWidth/2.0);
		if (!isPrinting && border < 1.0f) 
			border = 1.0f;
		g2.setPaint(lineColor);
		g2.setStroke(new BasicStroke(border, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, 10.0f));
		
		if (arrowLine != null) {
			// Handle opacity
			if (lineColor instanceof Color) {
				int alpha = ((Color)lineColor).getAlpha();
				float opacity = (float)alpha/(float)255;
				final Composite originalComposite = g2.getComposite();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				g2.draw(arrowLine);
				g2.setComposite(originalComposite);
			} else {
				g2.draw(arrowLine);
			}
		}

		g2.setStroke(new BasicStroke(border));

		// Add the head
		if (sourceType != ArrowType.NONE) {
			if (sourceColor == null) 
				sourceColor = lineColor;
			GraphicsUtilities.drawArrow(g, arrowLine, ArrowEnd.SOURCE, sourceColor, sourceSize*10.0*getZoom(), sourceType);
		}

		if (targetType != ArrowType.NONE) {
			if (targetColor == null) 
				targetColor = lineColor;
			GraphicsUtilities.drawArrow(g, arrowLine, ArrowEnd.TARGET, targetColor, targetSize*10.0*getZoom(), targetType);
		}
	}

	@Override
	public void update() {
		updateBounds();
		super.update();
	}

	@Override
	public ArrowAnnotationDialog getModifyDialog() {
		return new ArrowAnnotationDialog(this, ViewUtil.getActiveWindow(re));
	}


	private void updateBounds() {
		xOffset = 0.0; yOffset = 0.0;

		// We need to take into account our arrows
		if (targetType != ArrowType.NONE) {
			xOffset = targetSize * 10.0 * getZoom() + lineWidth;
			yOffset = targetSize * 10.0 * getZoom() + lineWidth;
		}

		if (sourceType != ArrowType.NONE) {
			xOffset += sourceSize * 10.0 * getZoom() + lineWidth;
			yOffset += sourceSize * 10.0 * getZoom() + lineWidth;
		}

		// Update our bounds
		if (source != null && target != null) {
			arrowLine = getArrowLine(target, source);
			
			if (arrowLine != null) {
				double x1 = arrowLine.getX1();
				double y1 = arrowLine.getY1();
				double x2 = arrowLine.getX2();
				double y2 = arrowLine.getY2();
				setLocation((int) (Math.min(x1, x2) - xOffset), (int) (Math.min(y1, y2) - yOffset));
				setSize(Math.abs(x1 - x2) + xOffset * 2, Math.abs(y1 - y2) + yOffset * 2);
			}
		}
	}

	private Line2D getArrowLine(Object target, DingAnnotation source) {
		if (usedForPreviews)
			return new Line2D.Double(10.0, getHeight()/2, getWidth()-20.0, getHeight()/2);

		Point2D targetPoint = null;
		Point2D sourceCenter = centerPoint(source.getBounds());
		
		if (target instanceof Point2D) {
			targetPoint = (Point2D) target;
		} else if (target instanceof DingAnnotation) {
			DingAnnotation a = (DingAnnotation)target;
			// get the bounds
			Rectangle2D targetBounds = a.getBounds();
			// Find the closest face and return
			targetPoint = findFace(sourceCenter, targetBounds, targetAnchorType);
		} else if (target instanceof View) {
			// get the target point from ding
			View<CyNode> nv = (View<CyNode>) target;
			Rectangle2D nodeBounds = getNodeBounds(nv);
			targetPoint = findFace(sourceCenter, nodeBounds, targetAnchorType);
		}

		Rectangle2D sourceBounds = source.getBounds();
		Point2D sourcePoint = findFace(targetPoint, sourceBounds, sourceAnchorType);
		
		return targetPoint != null ? new Line2D.Double(sourcePoint, targetPoint) : null;
	}

	private static Point2D centerPoint(Rectangle2D bounds) {
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}

	// Find the mid point to draw the target to
	private static Point2D findFace(Point2D source, Rectangle2D target, AnchorType anchorType) {
		if (source == null || target == null)
			return null;
		
		if (anchorType == AnchorType.CENTER)
			return new Point2D.Double(target.getX()+target.getWidth()/2, target.getY()+target.getHeight()/2);
		
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

	private Rectangle2D getNodeBounds(View<CyNode> nv) {
		if (nv == null)
			return null;
		
		NodeDetails nodeDetails = re.getNodeDetails();
		
		// First, get our starting and ending points in node coordinates
		double xCenter = nodeDetails.getXPosition(nv);
		double yCenter = nodeDetails.getYPosition(nv);

		double width  = nodeDetails.getWidth(nv);
		double height = nodeDetails.getHeight(nv);

		double xStart = xCenter - width/2.0;
		double yStart = yCenter - height/2.0;

		return new Rectangle2D.Double(xStart, yStart, width, height);
	}
}
