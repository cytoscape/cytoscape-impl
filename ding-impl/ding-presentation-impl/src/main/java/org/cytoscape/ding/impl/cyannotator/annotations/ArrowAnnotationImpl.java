package org.cytoscape.ding.impl.cyannotator.annotations;

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

import org.cytoscape.model.CyNode;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation.ArrowType;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation.ArrowEnd;
import org.cytoscape.ding.impl.cyannotator.dialogs.ArrowAnnotationDialog;

public class ArrowAnnotationImpl extends AbstractAnnotation implements ArrowAnnotation {
	private Paint lineColor = Color.BLACK; // These are paint's so we can do gradients
	private double lineOpacity = 100.0;
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
	protected static final String ARROWOPACITY = "lineOpacity";
	protected static final String SOURCETYPE = "sourceType";
	protected static final String SOURCESIZE = "sourceSize";
	protected static final String SOURCECOLOR = "sourceColor";

	protected static final String TARGETTYPE = "targetType";
	protected static final String TARGETSIZE = "targetSize";
	protected static final String TARGETCOLOR = "targetColor";


	public ArrowAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view) {
		super(cyAnnotator, view);
	}

	public ArrowAnnotationImpl(ShapeAnnotationImpl c) {
		super(c);
	}

  public ArrowAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view,
	                           Annotation source, Object target,
	                           ArrowType arrowType, Paint arrowColor,
	                           float arrowThickness) {
		// super(cyAnnotator, view, view.getZoom());
		super(cyAnnotator, view, source.getComponent().getX(), source.getComponent().getY(), view.getZoom());
  }

  public ArrowAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
    super(cyAnnotator, view, argMap);
  }

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,NAME);
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

		// Get the stroke
		float border = lineWidth;
		if (border < 1.0f) border = 1.0f;
		g2.setPaint(lineColor);
		g2.setStroke(new BasicStroke(border));
		
		Line2D relativeLine = getRelativeLine(arrowLine, scaleFactor);
		g2.draw(relativeLine);

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
		drawArrow(g);
	}

	public void drawArrow(Graphics g) {
		if ( (source == null || target == null) && !usedForPreviews ) return;

		if (!usedForPreviews)
			updateBounds();
		else
			arrowLine = getArrowLine(target, source);

		// Draw the line
		Graphics2D g2 = (Graphics2D)g;

		// Get the stroke
		float border = lineWidth;
		if (border < 1.0f) border = 1.0f;
		g2.setPaint(lineColor);
		g2.setStroke(new BasicStroke(border));
		
		Line2D relativeLine = getRelativeLine(arrowLine, 1.0);
		g2.draw(relativeLine);

		// Add the head
		if (sourceType != ArrowType.NONE) {
			GraphicsUtilities.drawArrow(g, relativeLine, ArrowEnd.SOURCE, sourceColor, sourceSize*10.0*getZoom(), sourceType);
		}

		if (targetType != ArrowType.NONE) {
			GraphicsUtilities.drawArrow(g, relativeLine, ArrowEnd.TARGET, targetColor, targetSize*10.0*getZoom(), targetType);
		}
	}

	public void setSize(double width, double height) {
		shapeWidth = width;
		shapeHeight = height;
		setSize((int)shapeWidth, (int)shapeHeight);
	}

	public JFrame getModifyDialog() {
		return new ArrowAnnotationDialog(this);
	}

	private Line2D getRelativeLine(Line2D line, double scaleFactor) {
		if (usedForPreviews) 
			return line;

		double x1 = line.getX1();
		double x2 = line.getX2();
		double width = Math.abs(x2-x1)+xOffset;
		double y1 = line.getY1();
		double y2 = line.getY2();
		double height = Math.abs(y2-y1)+yOffset;
		if (y2 < y1) {
			y1 = height;
			y2 = yOffset;
		} else {
			y1 = yOffset;
			y2 = height;
		}
		if (x2 < x1) {
			x1 = width;
			x2 = xOffset;
		} else {
			x1 = xOffset;
			x2 = width;
		}
		return new Line2D.Double(x1*scaleFactor, y1*scaleFactor, x2*scaleFactor, y2*scaleFactor);
	}

	private void updateBounds() {
		xOffset = 0.0; yOffset = 0.0;

		// We need to take into account our arrows
		if (targetType != ArrowType.NONE) {
			xOffset = targetSize*10.0*getZoom();
			yOffset = targetSize*10.0*getZoom();
		}

		if (sourceType != ArrowType.NONE) {
			xOffset += sourceSize*10.0*getZoom();
			yOffset += sourceSize*10.0*getZoom();
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
}
