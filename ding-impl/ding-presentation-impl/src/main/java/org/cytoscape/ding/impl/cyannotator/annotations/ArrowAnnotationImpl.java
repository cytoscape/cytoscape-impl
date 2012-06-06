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
import org.cytoscape.ding.impl.cyannotator.dialogs.ArrowAnnotationDialog;

public class ArrowAnnotationImpl extends AbstractAnnotation implements ArrowAnnotation {
	private Paint arrowColor = Color.BLACK; // These are paint's so we can do gradients
	private double arrowOpacity = 100.0;
	private Annotation source = null;
	private Object target = null;
	private ArrowDirection direction = ArrowDirection.FORWARD;
	private ArrowType arrowType = ArrowType.OPEN;
	private float lineWidth = 1.0f;
	private double arrowSize;

	public static final String NAME="ARROW";
	protected static final String ARROWCOLOR = "arrowColor";
	protected static final String ARROWTHICKNESS = "arrowThickness";
	protected static final String ARROWOPACITY = "arrowOpacity";
	protected static final String ARROWTYPE = "arrowType";


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
	public void setSource(Annotation source) { this.source = source; }

	public Object getTarget() { return this.target; }
	public void setTarget(Annotation target) { this.target = target; }
	public void setTarget(CyNode target) { this.target = target; }
	public void setTarget(Point2D target) { this.target = target; }

	public double getLineWidth() { return (double)lineWidth; }
	public void setLineWidth(double width) { this.lineWidth = (float)width; }

	public double getArrowSize() { return this.arrowSize; }
	public void setArrowSize(double width) { this.arrowSize = width; }

	public ArrowType getArrowType() { return this.arrowType; }
	public void setArrowType(ArrowType type) { this.arrowType = type; }

	public Paint getArrowColor() { return this.arrowColor; }
	public void setArrowColor(Paint color) { this.arrowColor = color; }

	public ArrowDirection getArrowDirection() { return this.direction; }
	public void setArrowDirection(ArrowDirection direction) { this.direction = direction; }



	@Override
	public void setZoom(double zoom) {
		float factor=(float)(zoom/getZoom());
		lineWidth*=factor;
		arrowSize*=factor;
		super.setZoom(zoom);
	}

	@Override
	public void setSpecificZoom(double zoom) {
		float factor=(float)(zoom/getSpecificZoom());
		super.setSpecificZoom(zoom);		
	}
    
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);

		boolean selected = isSelected();
		setSelected(false);

		setSelected(selected);
	}

	public void drawArrow(Graphics g) {
		// Get the location of our target
		Line2D arrowLine = getArrowLine(target, source);
		// Draw the line
		Graphics2D g2 = (Graphics2D)g;

		// Get the stroke
		float border = lineWidth;
		if (border < 1.0f) border = 1.0f;
		g2.setPaint(arrowColor);
		g2.setStroke(new BasicStroke(border));
		g2.draw(arrowLine);

		// Add the head
	}

	public JFrame getModifyDialog() {
		return new ArrowAnnotationDialog(this);
	}

	private Line2D getArrowLine(Object target, Annotation source) {
		Point2D targetPoint = null;
		Point2D sourceCenter = centerPoint(source.getComponent().getBounds());
		if (target instanceof Point)
			targetPoint = (Point2D)target;

		if (target instanceof Annotation) {
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
