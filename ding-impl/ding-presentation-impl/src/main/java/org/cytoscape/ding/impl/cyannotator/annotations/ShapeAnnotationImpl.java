package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

import java.util.Map;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation.ShapeType;
import org.cytoscape.ding.impl.cyannotator.dialogs.ShapeAnnotationDialog;

public class ShapeAnnotationImpl extends AbstractAnnotation implements ShapeAnnotation {
	private ShapeType shapeType;
	private double borderWidth;
	private Paint borderColor = Color.BLACK; // These are paint's so we can do gradients
	private Paint fillColor = null; // These are paint's so we can do gradients
	private double borderOpacity = 100.0;
	private double fillOpacity = 100.0;
	private	Shape shape = null;
	protected double shapeWidth = 0.0;
	protected double shapeHeight = 0.0;

	public static final String NAME="SHAPE";
	protected static final String WIDTH="width";
	protected static final String HEIGHT="height";
	protected static final String EDGECOLOR = "edgeColor";
	protected static final String EDGETHICKNESS = "edgeThickness";
	protected static final String EDGEOPACITY = "edgeOpacity";
	protected static final String FILLCOLOR = "fillColor";
	protected static final String FILLOPACITY = "fillOpacity";
	protected static final String SHAPETYPE = "shapeType";


	public ShapeAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, double width, double height) {
		super(cyAnnotator, view);
		shapeWidth=width;
		shapeHeight=height;
		shapeType = ShapeType.RECTANGLE;
	}

	public ShapeAnnotationImpl(ShapeAnnotationImpl c, double width, double height) {
		super(c);
		shapeWidth=width;
		shapeHeight=height;
		shapeType = c.getShapeType();
		borderColor = c.getBorderColor();
		borderWidth = c.getBorderWidth();
		fillColor = c.getFillColor();
		shape = GraphicsUtilities.getShape(shapeType, 0.0, 0.0, shapeWidth, shapeHeight);
	}

  public ShapeAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view,
                             double x, double y, ShapeType shapeType,
                             double width, double height,
                             Paint fillColor, Paint edgeColor,
                             float edgeThickness) {
    super(cyAnnotator, view, x, y, view.getZoom());

    this.shapeType=shapeType;
    this.fillColor=fillColor;
    setFillColor(fillColor);
    this.borderColor=edgeColor;
    this.borderWidth=edgeThickness;
		this.shapeWidth = width;
		this.shapeHeight = height;
		this.shape = GraphicsUtilities.getShape(shapeType, 0.0, 0.0, shapeWidth, shapeHeight);
    setSize((int)(shapeWidth+borderWidth*2), (int)(shapeHeight+borderWidth*2));
  }

  public ShapeAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
    super(cyAnnotator, view, argMap);
    this.borderColor = getColor(argMap.get(EDGECOLOR));
    this.fillColor = getColor(argMap.get(FILLCOLOR));
    setFillColor(fillColor);
		if (argMap.containsKey(FILLOPACITY))
    	this.fillOpacity = Double.parseDouble(argMap.get(FILLOPACITY));

		// If this is an old bounded text, we might not (yet) have a width or height
		if (argMap.containsKey(WIDTH))
    	this.shapeWidth = Double.parseDouble(argMap.get(WIDTH));
		else
			this.shapeWidth = 100;

		if (argMap.containsKey(HEIGHT))
    	this.shapeHeight = Double.parseDouble(argMap.get(HEIGHT));
		else
			this.shapeHeight = 100;

    this.borderWidth = Double.parseDouble(argMap.get(EDGETHICKNESS));

		if (argMap.containsKey(EDGEOPACITY))
	    this.borderOpacity = Double.parseDouble(argMap.get(EDGEOPACITY));

    this.shapeType = GraphicsUtilities.getShapeType(Integer.parseInt(argMap.get(SHAPETYPE)));
		this.shape = GraphicsUtilities.getShape(shapeType, 0.0, 0.0, shapeWidth, shapeHeight);
    setSize((int)(shapeWidth+borderWidth*2), (int)(shapeHeight+borderWidth*2));
  }

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,NAME);
		if (this.fillColor != null)
			argMap.put(FILLCOLOR,convertColor(this.fillColor));
		argMap.put(FILLOPACITY, Double.toString(this.fillOpacity));

		if (this.borderColor != null)
			argMap.put(EDGECOLOR,convertColor(this.borderColor));
		argMap.put(EDGETHICKNESS,Double.toString(this.borderWidth));
		argMap.put(EDGEOPACITY, Double.toString(this.borderOpacity));
		argMap.put(SHAPETYPE, Integer.toString(this.shapeType.ordinal()));
		argMap.put(WIDTH, Double.toString(this.shapeWidth));
		argMap.put(HEIGHT, Double.toString(this.shapeHeight));
		return argMap;
	}

	public ShapeType[] getSupportedShapes() {
		return GraphicsUtilities.getSupportedShapes();
	}

	@Override
	public void setZoom(double zoom) {
		float factor=(float)(zoom/getZoom());

		borderWidth*=factor;
										
		shapeWidth*=factor;
		shapeHeight*=factor;
		// System.out.println("setZoom: size = "+shapeWidth+"x"+shapeHeight);
		
    setSize((int)(shapeWidth+borderWidth*2), (int)(shapeHeight+borderWidth*2));
		super.setZoom(zoom);
	}

	@Override
	public void setSpecificZoom(double zoom) {
		float factor=(float)(zoom/getSpecificZoom());

		shapeWidth*=factor;
		shapeHeight*=factor;

    setSize((int)(shapeWidth+borderWidth*2), (int)(shapeHeight+borderWidth*2));
		super.setSpecificZoom(zoom);		
	}

	public Rectangle getBounds() {
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
	}

	public Rectangle2D getBounds2D() {
		return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
	}

	public Shape getShape() {
		return shape;
	}

	public ShapeType getShapeType() {return shapeType;}

  public void setShapeType(ShapeType type) { 
		shapeType = type; 
		this.shape = GraphicsUtilities.getShape(shapeType, 0.0, 0.0, shapeWidth, shapeHeight);
	}
  
  public double getBorderWidth() {return borderWidth;}
  public void setBorderWidth(double width) { 
		borderWidth = width*getZoom();
	}
  
  public Paint getBorderColor() {return borderColor;}
  public Paint getFillColor() {return fillColor;}
    
  public void setBorderColor(Paint border) {borderColor = border;}
  public void setFillColor(Paint fill) {fillColor = fill;}
    
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);

		int width = (int)(shapeWidth*scaleFactor/getZoom());
		int height = (int)(shapeHeight*scaleFactor/getZoom());

		boolean selected = isSelected();
		setSelected(false);
		GraphicsUtilities.drawShape(g, (int)(x*scaleFactor), (int)(y*scaleFactor),
		                            width, height, this);
		setSelected(selected);
	}

	public void paint(Graphics g) {
		super.paint(g);

		if (usedForPreviews()) {
			// For previews, our size and position is managed by the surrounding component
			GraphicsUtilities.drawShape(g, borderWidth/2, borderWidth/2, 
			                            shapeWidth-borderWidth, shapeHeight-borderWidth, this);
			return;
		}

		Point2D p1 = getLocation();
		GraphicsUtilities.drawShape(g, (int)p1.getX(), (int)p1.getY(), shapeWidth, shapeHeight, this);
	}

	public void setSize(double width, double height) {
		shapeWidth = width;
		shapeHeight = height;
    setSize((int)(shapeWidth+borderWidth*2), (int)(shapeHeight+borderWidth*2));
	}

	public JFrame getModifyDialog() {
		return new ShapeAnnotationDialog(this);
	}

}
