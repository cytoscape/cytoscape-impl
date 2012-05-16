package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.modify.mBoundedAnnotation;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;

public class BoundedAnnotation extends TextAnnotation {
	private float edgeThickness=2.0f;
	private boolean fillVal = false;

	private static String EDGECOLOR = "edgeColor";
	private static String EDGETHICKNESS = "edgeThickness";
	private static String FILLCOLOR = "fillColor";
	private static String SHAPETYPE = "shapeType";

	public static final String NAME="BOUNDED";

	public BoundedAnnotation() { super(); }
	
	public BoundedAnnotation(CyAnnotator cyAnnotator, DGraphView view, int x, int y, 
	                         String text, int compCount, double zoom, 
	                         Color fillColor, Color edgeColor, int shapeType, float edgeThickness){
		super(cyAnnotator, view, x, y, text, compCount, zoom);
		this.shapeType=shapeType;
		setFillColor(fillColor);
		this.edgeColor=edgeColor;
		this.edgeThickness=edgeThickness;
		updateAnnotationAttributes();
	}

	public BoundedAnnotation(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		super(cyAnnotator, view, argMap);
		this.edgeColor = getColor(argMap.get(EDGECOLOR));
		String color = argMap.get(FILLCOLOR);
		if (color != null)
			this.fillColor = getColor(color);
		setFillColor(fillColor);
		this.edgeThickness = Float.parseFloat(argMap.get(EDGETHICKNESS));
		this.shapeType = Integer.parseInt(argMap.get(SHAPETYPE));
		updateAnnotationAttributes();
	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,NAME);
		if ( this.fillColor != null )
			argMap.put(FILLCOLOR,convertColor(this.fillColor));
		argMap.put(EDGECOLOR,convertColor(this.edgeColor));
		argMap.put(EDGETHICKNESS,Float.toString(this.edgeThickness));
		argMap.put(SHAPETYPE, Integer.toString(this.shapeType));
		return argMap;
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);
		Graphics2D g2=(Graphics2D)g;
		float stroke = (float)(edgeThickness*scaleFactor);
		if (stroke < 1.0f) stroke = 1.0f;

		//Setting up anti-aliasing for high quality rendering
		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		int width = (int)(getAnnotationWidth()*scaleFactor/view.getZoom());
		int height = (int)(getAnnotationHeight()*scaleFactor/view.getZoom());

			x -= (getAnnotationWidth(g2)-getTextWidth(g2))/2; // Provide a little padding

		// Get the scaled font metrics
		int tWidth = getTextWidth(g2, scaleFactor);
		int offset = (width - tWidth) / 2;

		boolean selected = isSelected();
		setSelected(false);
		drawShape(g2, (int)(x*scaleFactor)-offset, (int)(y*scaleFactor), 
		          width, height, stroke);
		setSelected(selected);
	}
	
	public void paint(Graphics g) {
		Graphics2D g2=(Graphics2D)g;

		// Disable the selection for our parent
		boolean selected = isSelected();
		setSelected(false);
		super.paint(g);		
		setSelected(selected);

		float stroke = edgeThickness;
		if (stroke < 1.0f) stroke = 1.0f;

		//Setting up anti-aliasing for high quality rendering
		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		int x=getX(),y=getY();
		if(usedForPreviews){
			x+=(int)(getWidth()-getAnnotationWidth(g2))/2;
			y+=(int)(getHeight()-getAnnotationHeight(g2))/2;
		} else {
			x -= (getAnnotationWidth(g2)-getTextWidth(g2))/2; // Provide a little padding
		}

		drawShape(g2, x, y, getAnnotationWidth(), getAnnotationHeight(), stroke);
	}	
	
	public void adjustZoom(double newZoom){
		float factor=((float)(newZoom/getZoom()));
		edgeThickness*=factor;
		super.adjustZoom(newZoom);
	}	

	public void setEdgeThickness(float val){
		edgeThickness=val;
		updateAnnotationAttributes();
	}
	
	public void setShapeType(int val){
		shapeType=val;
		updateAnnotationAttributes();
	}

	public void setFillVal(boolean val){
		fillVal=val;
		updateAnnotationAttributes();
	}
	
	public void setFillColor(Color newColor){
		
		if(newColor!=null){
			this.fillColor=newColor;
			this.fillVal=true;
		}
		else
			this.fillVal=false;

		updateAnnotationAttributes();
	} 
	
	public void setEdgeColor(Color newColor){
		this.edgeColor=newColor;
		updateAnnotationAttributes();
	}	

	public boolean getFillVal(){
		return fillVal;
	}

	public int getShapeType(){
		return shapeType;
	}
	
	public Color getFillColor(){
		
		return fillColor;
	}
	
	public Color getEdgeColor(){
		
		return edgeColor;
	}
		
	public int getTopX(){
		
		return getX();
	}
	 
	@Override
	public int getAnnotationWidth(){
		if(shapeType==0 || shapeType==1)
			return getTextWidth()+getTextHeight()/2;
		else
			return getTextWidth()*3/2;
	}

	@Override
	public int getAnnotationHeight(){
		return getTextHeight()*3/2;
	}
   
	public int getAnnotationWidth(Graphics2D g2){
		if(shapeType==0 || shapeType==1)
			return getTextWidth(g2)+getTextHeight(g2)/2;
		else
			return getTextWidth(g2)*3/2;
	}

	public int getAnnotationHeight(Graphics2D g2){
		return getTextHeight(g2)*3/2;
	}	
	
	public float getEdgeThickness(){
		return this.edgeThickness;
	}

	public boolean isBoundedAnnotation(){
		return true;
	}	  
	
	public void addModifyMenuItem(JPopupMenu popup){
		JMenuItem modify=new JMenuItem("Modify Properties");
		modify.addActionListener(new modifyBoundedAnnotationListener());
		
		popup.add(modify);
	}
	
	class modifyBoundedAnnotationListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			mBoundedAnnotation mBAnnotation=new mBoundedAnnotation(BoundedAnnotation.this);

			mBAnnotation.setVisible(true);
			mBAnnotation.setSize(480, 504);		
			mBAnnotation.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			mBAnnotation.setResizable(false);
			
			mBAnnotation.setLocation(BoundedAnnotation.this.getX(), BoundedAnnotation.this.getY());			
		}

	}	  
		  
	public boolean isPointInComponentOnly(int pX, int pY){
		int x=getX(), y=getY();
		if( pX>=x && pX<=(x+getAnnotationWidth()) && pY>=y && pY<=(y+getAnnotationHeight()) )
			return true;
		else
			return false;
	}	
}
