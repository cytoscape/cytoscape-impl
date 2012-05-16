package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.modify.mShapeAnnotation;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;


public class ShapeAnnotation extends Annotation {

	private int otherCornerX=0, otherCornerY=0;
	private float edgeThickness=2.0f;
	public boolean cornersAdjusted=false;
	private double shapeWidth=0.0, shapeHeight=0.0;

	private static String EDGECOLOR = "edgeColor";
	private static String EDGETHICKNESS = "edgeThickness";
	private static String FILLCOLOR = "fillColor";
	private static String SHAPETYPE = "shapeType";
	private static String WIDTH = "width";
	private static String HEIGHT = "height";

	public static final String NAME="SHAPE";

	public ShapeAnnotation(int width, int height) {
		super();
		shapeWidth=width/2;
		shapeHeight=height/3;
		rVal=10;
	}
	
	public ShapeAnnotation(CyAnnotator cyAnnotator, DGraphView view, 
	                       int x, int y, int shapeType, 
	                       int width, int height,
	                       Color fillColor, Color edgeColor, 
	                       float edgeThickness) {
		super(cyAnnotator, view, x, y, 
	        cyAnnotator.getForeGroundCanvas().getComponentCount(), view.getZoom());

		this.shapeType=shapeType;
		this.fillColor=fillColor;
		setFillColor(fillColor);
		this.edgeColor=edgeColor;
		this.edgeThickness=edgeThickness;
		this.shapeWidth = width;
		this.shapeHeight = height;
		otherCornerX = x+(int)shapeWidth;
		otherCornerY = y+(int)shapeHeight;
		setSize((int)shapeWidth, (int)shapeHeight);
		updateAnnotationAttributes();
	}

	public ShapeAnnotation(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		super(cyAnnotator, view, argMap);
		this.edgeColor = getColor(argMap.get(EDGECOLOR));
		this.fillColor = getColor(argMap.get(FILLCOLOR));
		this.shapeWidth = Double.parseDouble(argMap.get(WIDTH));
		this.shapeHeight = Double.parseDouble(argMap.get(HEIGHT));
		setFillColor(fillColor);
		this.edgeThickness = Float.parseFloat(argMap.get(EDGETHICKNESS));
		this.shapeType = Integer.parseInt(argMap.get(SHAPETYPE));
		otherCornerX = getX()+(int)shapeWidth;
		otherCornerY = getY()+(int)shapeHeight;
		setSize((int)shapeWidth, (int)shapeHeight);
		cornersAdjusted = true;
		updateAnnotationAttributes();
	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,NAME);
		if (this.fillColor != null)
			argMap.put(FILLCOLOR,convertColor(this.fillColor));
		if (this.edgeColor != null)
			argMap.put(EDGECOLOR,convertColor(this.edgeColor));
		argMap.put(EDGETHICKNESS,Float.toString(this.edgeThickness));
		argMap.put(SHAPETYPE, Integer.toString(this.shapeType));
		argMap.put(WIDTH, Double.toString(this.shapeWidth));
		argMap.put(HEIGHT, Double.toString(this.shapeHeight));
		return argMap;
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		Graphics2D g2=(Graphics2D)g;
		float stroke = (float)(edgeThickness*scaleFactor);
		if (stroke < 1.0f) stroke = 1.0f;

		if(getArrowDrawn())
			super.paint(g);		

		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		// System.out.println("drawAnnotation: x,y="+x+","+y+" "+shapeWidth+"x"+shapeHeight+" zoom = "+view.getZoom());
		int width = (int)(shapeWidth*scaleFactor/view.getZoom());
		int height = (int)(shapeHeight*scaleFactor/view.getZoom());

		boolean selected = isSelected();
		setSelected(false);
		drawShape(g2, (int)(x*scaleFactor), (int)(y*scaleFactor), width, height, stroke);
		setSelected(selected);
	}


	@Override
	public void paint(Graphics g) {
		Graphics2D g2=(Graphics2D)g;
		float stroke = edgeThickness;
		if (stroke < 1.0f) stroke = 1.0f;

		// System.out.println("paint: x,y="+getX()+","+getY()+" "+shapeWidth+"x"+shapeHeight);
		
		if(getArrowDrawn())
			super.paint(g);		

		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		Point p1;
		int width, height;

		if(!cornersAdjusted) {
			//We haven't finalized the shape yet
			//Comes into play when we created a ShapeAnnotation and move the mouse

			p1=getFirstCorner();//To obtain topLeftCorner
			Point p2=getSecondCorner();//To obtain the bottomRightCorner

			width=Math.abs(p2.x-p1.x);
			height=Math.abs(p2.y-p1.y);
		} else {
			if(usedForPreviews)
				p1=new Point(getX()+(int)(shapeWidth/2), getY()+(int)(shapeHeight));
			else
				p1=new Point(getX(), getY());

			width=(int)shapeWidth;
			height=(int)shapeHeight;			
		}

		drawShape(g2, p1.x, p1.y, width, height, stroke);
	}

	public boolean isShapeAnnotation() {
		return true;
	}

	public boolean isTextAnnotation() {
		return false;
	}	
	
	public boolean isPointInComponentOnly(int pX, int pY) {
		int x=getX(), y=getY();

		if(pX>=x && pX<=(x+shapeWidth) && pY>=y && pY<=(y+shapeHeight))
			return true;
		else
			return false;
	}	

	public int getAnnotationWidth() {
		return (int)shapeWidth;
	}


	public int getAnnotationHeight() {
		return (int)shapeHeight;
	}

	@Override
	public void adjustZoom(double newZoom) {
		float factor=(float)(newZoom/getZoom());
		
		edgeThickness*=factor;
		
		adjustArrowThickness(newZoom);
		
		setZoom(newZoom);
										
		shapeWidth*=factor;
		shapeHeight*=factor;
		
		setSize((int)shapeWidth, (int)shapeHeight);
		updateAnnotationAttributes();
	}

	@Override
	public void adjustSpecificZoom(double newZoom) {
		float factor=(float)(newZoom/getTempZoom());
		
		setTempZoom(newZoom);		
				
		shapeWidth*=factor;
		shapeHeight*=factor;
		
		setSize((int)shapeWidth, (int)shapeHeight);
		updateAnnotationAttributes();
	}
	
	public void addModifyMenuItem(JPopupMenu popup) {
		JMenuItem modify=new JMenuItem("Modify Properties");
		modify.addActionListener(new modifyShapeAnnotationListener());
		
		popup.add(modify);
	}
	
	class modifyShapeAnnotationListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {

			mShapeAnnotation mSAnnotation=new mShapeAnnotation(ShapeAnnotation.this);

			mSAnnotation.setVisible(true);
			mSAnnotation.setSize(474, 504);		
			mSAnnotation.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			mSAnnotation.setResizable(false);
			
			mSAnnotation.setLocation(ShapeAnnotation.this.getX(), ShapeAnnotation.this.getY());			
		}

	}	   
	
	public void adjustCorners() {
		//Comes into play when the shape has been created completely
		//We finalize the topLeft and bottomRight corners of the shape

		Point p1=getFirstCorner(), p2=getSecondCorner();

		this.setLocation(p1);

		((ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS))).modifyComponentLocation(this.getX(), this.getY(), this.getComponentNumber());

		shapeWidth=Math.abs(p2.x-p1.x);
		shapeHeight=Math.abs(p2.y-p1.y);

		cornersAdjusted=true;
		updateAnnotationAttributes();
	}

	public Point getFirstCorner() {
		int x=getX(), y=getY();

		if(x<=otherCornerX && y<=otherCornerY)
			return new Point(x,y);

		else if(x>=otherCornerX && y<=otherCornerY)
			return new Point(otherCornerX, y);

		else if(x<=otherCornerX && y>=otherCornerY)
			return new Point(x, otherCornerY);

		else
			return new Point(otherCornerX, otherCornerY);
	}

	public Point getSecondCorner() {
		int x=getX(), y=getY();

		if(x<=otherCornerX && y<=otherCornerY)
			return new Point(otherCornerX,otherCornerY);

		else if(x>=otherCornerX && y<=otherCornerY)
			return new Point(x, otherCornerY);

		else if(x<=otherCornerX && y>=otherCornerY)
			return new Point(otherCornerX, y);

		else
			return new Point(x, y);
	}
	
	public float getEdgeThickness() {
		return this.edgeThickness;
	}
	
	public Color getFillColor() {
		return fillColor;
	}
	
	public Color getEdgeColor() {
		return edgeColor;
	}
	
	public int getShapeType() {
		return this.shapeType;
	}
		
	public void setFillColor(Color val) {
		fillColor=val;
		updateAnnotationAttributes();
	}
	
	public void setEdgeColor(Color val) {
		edgeColor=val;
		updateAnnotationAttributes();
	}

	public void setOtherCorner(int x, int y) {
		otherCornerX=x;
		otherCornerY=y;
		updateAnnotationAttributes();
	}
	
	public void setEdgeThickness(int val) {
		edgeThickness=val;
		updateAnnotationAttributes();
	}

	public void setShapeType(int val) {
		shapeType=val;
		updateAnnotationAttributes();
	}
		
}

