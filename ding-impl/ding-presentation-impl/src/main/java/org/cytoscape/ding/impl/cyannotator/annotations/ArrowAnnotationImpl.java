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
import java.util.Objects;
import java.util.UUID;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.model.CyIdentifiable;
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

	public static final String ARROWCOLOR = "lineColor";
	protected static final String ARROWTHICKNESS = "lineThickness";

	protected static final String SOURCEANN = "sourceAnnotation";
	protected static final String SOURCETYPE = "sourceType";
	protected static final String SOURCESIZE = "sourceSize";
	public static final String SOURCECOLOR = "sourceColor";

	protected static final String TARGETPOINT = "targetPoint";
	protected static final String TARGETANN = "targetAnnotation";
	protected static final String TARGETNODE = "targetNode";

	protected static final String TARGETTYPE = "targetType";
	protected static final String TARGETSIZE = "targetSize";
	public static final String TARGETCOLOR = "targetColor";

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

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public ArrowAnnotationImpl(DRenderingEngine re, boolean usedForPreviews) {
		super(re, usedForPreviews);
	}

	public ArrowAnnotationImpl(DRenderingEngine re, Map<String, String> argMap) {
		super(re, argMap);

		lineColor = ViewUtils.getColor(argMap, ARROWCOLOR, Color.BLACK);
		
		double zoom = getLegacyZoom(argMap);
		lineWidth = ViewUtils.getFloat(argMap, ARROWTHICKNESS, 1.0f) / (float)zoom;

		// Source
		if (argMap.containsKey(SOURCEANN)) {
			var uuid = UUID.fromString(argMap.get(SOURCEANN));
			source = cyAnnotator.getAnnotation(uuid);
		}

		// Source Arrow
		sourceType = GraphicsUtilities.getArrowType(argMap, SOURCETYPE, ArrowType.NONE);
		sourceSize = ViewUtils.getDouble(argMap, SOURCESIZE, 5.0);
		sourceColor = ViewUtils.getColor(argMap, SOURCECOLOR, null); // A null color = line color

		// Target Arrow
		targetType = GraphicsUtilities.getArrowType(argMap, TARGETTYPE, ArrowType.NONE);
		targetSize = ViewUtils.getDouble(argMap, TARGETSIZE, 5.0);
		targetColor = ViewUtils.getColor(argMap, TARGETCOLOR, null); // A null color = line color

		// Figure out the target
		if (argMap.containsKey(TARGETPOINT)) {
			var point = argMap.get(TARGETPOINT);
			var xy = point.split(",");
			double x = Double.parseDouble(xy[0]);
			double y = Double.parseDouble(xy[1]);
			target = new Point2D.Double(x, y);
		} else if (argMap.containsKey(TARGETANN)) {
			var uuid = UUID.fromString(argMap.get(TARGETANN));
			target = cyAnnotator.getAnnotation(uuid);
		} else if (argMap.containsKey(TARGETNODE)) {
			var point = argMap.get(TARGETNODE);
			var xy = point.split(",");
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
	@SuppressWarnings("unchecked")
	public Map<String, String> getArgMap() {
		var argMap = super.getArgMap();
		argMap.put(TYPE, ArrowAnnotation.class.getName());

		if (lineColor != null)
			argMap.put(ARROWCOLOR, ViewUtils.serialize(lineColor));

		argMap.put(ARROWTHICKNESS, Float.toString(lineWidth));

		if (source != null)
			argMap.put(SOURCEANN, source.getUUID().toString());

		argMap.put(SOURCETYPE, Integer.toString(sourceType.ordinal()));
		argMap.put(SOURCESIZE, Double.toString(sourceSize));

		if (sourceColor != null)
			argMap.put(SOURCECOLOR, ViewUtils.serialize(sourceColor));

		if (target instanceof Point2D) {
			var xy = (Point2D) target;
			argMap.put(TARGETPOINT, Double.toString(xy.getX()) + "," + Double.toString(xy.getY()));
		} else if (target instanceof Annotation) {
			argMap.put(TARGETANN, ((DingAnnotation) target).getUUID().toString());
		} else if (target instanceof View) {
			var nv = (View<CyNode>) target;
			
			if (nv != null) {
				double xCenter = re.getNodeDetails().getXPosition(nv);
				double yCenter = re.getNodeDetails().getYPosition(nv);
				argMap.put(TARGETNODE, Double.toString(xCenter) + "," + Double.toString(yCenter));
			}
		}

		argMap.put(TARGETTYPE, Integer.toString(targetType.ordinal()));
		argMap.put(TARGETSIZE, Double.toString(targetSize));

		if (targetColor != null)
			argMap.put(TARGETCOLOR, ViewUtils.serialize(targetColor));

		return argMap;
	}
	
	@Override
	public void setStyle(Map<String, String> argMap) {
		if (argMap != null) {
			// Stroke
			setLineColor(ViewUtils.getColor(argMap, ARROWCOLOR, Color.BLACK));

			double zoom = getLegacyZoom(argMap);
			setLineWidth(ViewUtils.getFloat(argMap, ARROWTHICKNESS, 1.0f) / (float) zoom);

			// Source Arrow
			setArrowType(ArrowEnd.SOURCE, GraphicsUtilities.getArrowType(argMap, SOURCETYPE, ArrowType.NONE).getName());
			setArrowSize(ArrowEnd.SOURCE, ViewUtils.getDouble(argMap, SOURCESIZE, 5.0));
			setArrowColor(ArrowEnd.SOURCE, ViewUtils.getColor(argMap, SOURCECOLOR, null));

			// Target Arrow
			setArrowType(ArrowEnd.TARGET, GraphicsUtilities.getArrowType(argMap, TARGETTYPE, ArrowType.NONE).getName());
			setArrowSize(ArrowEnd.TARGET, ViewUtils.getDouble(argMap, TARGETSIZE, 5.0));
			setArrowColor(ArrowEnd.TARGET, ViewUtils.getColor(argMap, TARGETCOLOR, null));
		}
	}

	@Override
	public Annotation getSource() { 
		return source;
	}
	
	@Override
	public void setSource(Annotation source) { 
		if (this.source != null)
			((DingAnnotation) source).removeArrow(this);
		
		this.source = (DingAnnotation) source;

		if (source != null)
			source.addArrow(this);

		update();
	}
	
	@Override
	public Object getTarget() {
		return target; 
	}

	@Override
	public void setTarget(Annotation target) {
		if (!Objects.equals(this.target, target)) {
			this.target = target;
			update();
		}
	}

	/**
	 * This is only here for backwards compatibility, do not use!
	 */
	@Override
	@Deprecated
	public void setTarget(CyNode target) {
		var nv = re.getViewModelSnapshot().getNodeView(target);
		setTarget(nv);
	}

	@Override
	public void setTarget(View<? extends CyIdentifiable> target) {
		if (!Objects.equals(this.target, target)) {
			this.target = target;
			update();
		}
	}

	@Override
	public void setTarget(Point2D target) {
		if (!Objects.equals(this.target, target)) {
			this.target = target;
			update();
		}
	}

	@Override
	public double getLineWidth() {
		return lineWidth;
	}

	@Override
	public void setLineWidth(double width) {
		if (lineWidth != (float) width) {
			var oldValue = lineWidth;
			lineWidth = (float) width;
			update();
			firePropertyChange("lineWidth", oldValue, width);
		}
	}

	@Override
	public double getArrowSize(ArrowEnd end) {
		return end == ArrowEnd.SOURCE ? sourceSize : targetSize;
	}

	@Override
	public void setArrowSize(ArrowEnd end, double width) {
		String propName = null;
		double oldValue = 0.0;
		
		if (end == ArrowEnd.SOURCE) {
			if (sourceSize != width) {
				oldValue = sourceSize;
				sourceSize = width;
				propName = "sourceArrowSize";
			}
		} else {
			if (targetSize != width) {
				oldValue = targetSize;
				targetSize = width;
				propName = "targetArrowSize";
			}
		}
		
		if (propName != null) { // Changed?
			update();
			firePropertyChange(propName, oldValue, width);
		}
	}

	@Override
	public String getArrowType(ArrowEnd end) { 
		return end == ArrowEnd.SOURCE ? sourceType.getName() : targetType.getName(); 
	}

	@Override
	public void setArrowType(ArrowEnd end, String type) {
		ArrowType aType = null;

		for (var t : ArrowType.values()) {
			if (t.getName().equals(type)) {
				aType = t;
				break;
			}
		}

		if (aType == null)
			return;

		String propName = null;
		ArrowType oldValue = null;
		
		if (end == ArrowEnd.SOURCE) {
			if (sourceType != aType) {
				oldValue = sourceType;
				sourceType = aType;
				propName = "sourceArrowType";
			}
		} else {
			if (targetType != aType) {
				oldValue = targetType;
				targetType = aType;
				propName = "targetArrowType";
			}
		}
		
		if (propName != null) { // Changed?
			update();
			firePropertyChange(propName, oldValue, type);
		}
	}

	@Override
	public AnchorType getAnchorType(ArrowEnd end) {
		return end == ArrowEnd.SOURCE ? sourceAnchorType : targetAnchorType;
	}

	@Override
	public void setAnchorType(ArrowEnd end, AnchorType type) {
		String propName = null;
		AnchorType oldValue = null;
		
		if (end == ArrowEnd.SOURCE) {
			if (sourceAnchorType != type) {
				oldValue = sourceAnchorType;
				sourceAnchorType = type;
				propName = "sourceAnchorType";
			}
		} else {
			if (targetAnchorType != type) {
				oldValue = targetAnchorType;
				targetAnchorType = type;
				propName = "targetAnchorType";
			}
		}
		
		if (propName != null) { // Changed?
			update();
			firePropertyChange(propName, oldValue, type);
		}
	}

	@Override
	public Paint getLineColor() {
		return lineColor;
	}

	@Override
	public void setLineColor(Paint color) {
		if (!Objects.equals(lineColor, color)) {
			var oldValue = lineColor;
			lineColor = color;
			update();
			firePropertyChange("lineColor", oldValue, color);
		}
	}

	@Override
	public Paint getArrowColor(ArrowEnd end) {
		return end == ArrowEnd.SOURCE ? sourceColor : targetColor;
	}

	@Override
	public void setArrowColor(ArrowEnd end, Paint color) {
		String propName = null;
		Paint oldValue = null;
		
		if (end == ArrowEnd.SOURCE) {
			if (!Objects.equals(sourceColor, color)) {
				oldValue = sourceColor;
				sourceColor = color;
				propName = "sourceArrowColor";
			}
		} else {
			if (!Objects.equals(targetColor, color)) {
				oldValue = targetColor;
				targetColor = color;
				propName = "targetArrowColor";
			}
		}
		
		if (propName != null) { // Changed?
			update();
			firePropertyChange(propName, oldValue, color);
		}
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
		var g2 = (Graphics2D) g;

		// Get the stroke
		float border = (float) (lineWidth / 2.0);
		
		if (!isPrinting && border < 1.0f) 
			border = 1.0f;
		
		g2.setPaint(lineColor);
		g2.setStroke(new BasicStroke(border, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, 10.0f));
		
		if (arrowLine != null) {
			// Handle opacity
			if (lineColor instanceof Color) {
				int alpha = ((Color) lineColor).getAlpha();
				float opacity = (float) alpha / (float) 255;
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
			
			double arrowSize = getArrowSize(sourceType, sourceSize);
			GraphicsUtilities.drawArrow(g, arrowLine, ArrowEnd.SOURCE, sourceColor, arrowSize, sourceType);
		}

		if (targetType != ArrowType.NONE) {
			if (targetColor == null)
				targetColor = lineColor;

			double arrowSize = getArrowSize(targetType, targetSize);
			GraphicsUtilities.drawArrow(g, arrowLine, ArrowEnd.TARGET, targetColor, arrowSize, targetType);
		}
	}

	@Override
	public void update() {
		updateBounds();
		super.update();
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

	@SuppressWarnings("unchecked")
	private Line2D getArrowLine(Object target, DingAnnotation source) {
		if (usedForPreviews)
			return new Line2D.Double(10.0, getHeight() / 2, getWidth() - 20.0, getHeight() / 2);

		Point2D targetPoint = null;
		Point2D sourceCenter = centerPoint(source.getBounds());
		
		if (target instanceof Point2D) {
			targetPoint = (Point2D) target;
		} else if (target instanceof DingAnnotation) {
			var a = (DingAnnotation) target;
			// get the bounds
			var targetBounds = a.getBounds();
			// Find the closest face and return
			double arrowSize = getArrowSize(targetType, targetSize);
			targetPoint = findFace(sourceCenter, targetBounds, targetAnchorType, targetType, arrowSize);
		} else if (target instanceof View) {
			// get the target point from ding
			var nv = (View<CyNode>) target;
			var nodeBounds = getNodeBounds(nv);
			double arrowSize = getArrowSize(targetType, targetSize);
			targetPoint = findFace(sourceCenter, nodeBounds, targetAnchorType, targetType, arrowSize);
		}

		var sourceBounds = source.getBounds();
		double arrowSize = getArrowSize(sourceType, sourceSize);
		var sourcePoint = findFace(targetPoint, sourceBounds, sourceAnchorType, sourceType, arrowSize);
		
		return targetPoint != null ? new Line2D.Double(sourcePoint, targetPoint) : null;
	}
	
	private double getArrowSize(ArrowType type, double defaultArrowSize) {
		if (type == ArrowType.NONE)
			return 0.0;
		
		double factor = type == ArrowType.DIAMOND ? 5.0 : 10;
		
		return defaultArrowSize * factor * getZoom();
	}

	private static Point2D centerPoint(Rectangle2D bounds) {
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}

	// Find the mid point to draw the target to
	private static Point2D findFace(
			Point2D source,
			Rectangle2D target,
			AnchorType anchorType,
			ArrowType arrowType,
			double arrowSize
	) {
		if (source == null || target == null)
			return null;

		double x = target.getX();
		double y = target.getY();
		double w = target.getWidth();
		double h = target.getHeight();
		
		if (anchorType == AnchorType.CENTER)
			return new Point2D.Double(x + w / 2, y + h / 2);

		if (arrowSize > 0) {
			if (arrowType == ArrowType.X) {
				// Pretend the target is larger so the arrow line ends at the center of the X arrow
				w += arrowSize;
				h += arrowSize;
				x -= arrowSize / 2;
				y -= arrowSize / 2;
			}
		}
		
		var left = new Point2D.Double(x, y + h / 2.0);
		var right = new Point2D.Double(x + w, y + h / 2.0);
		var top = new Point2D.Double(x + w / 2.0, y);
		var bottom = new Point2D.Double(x + w / 2.0, y + h);

		var topline = new Line2D.Double(x, y, x + w, y);
		var bottomline = new Line2D.Double(x, y + h, x + w, y + h);
		var rightline = new Line2D.Double(x + w, y, x + w, y + h);
		var leftline = new Line2D.Double(x, y, x, y + h);

		if (source.getX() <= x) {
			// Left
			if (source.getY() == y) {
				return left;
			} else if (source.getY() < y) {
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
			if (source.getY() == y) {
				return right;
			} else if (source.getY() < y) {
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
		
		var nodeDetails = re.getNodeDetails();
		
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
