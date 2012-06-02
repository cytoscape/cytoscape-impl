package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import java.util.Map;

import javax.swing.JFrame;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.BoundedTextAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.TextAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.dialogs.BoundedTextAnnotationDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoundedTextAnnotationImpl extends ShapeAnnotationImpl 
                                       implements BoundedTextAnnotation, TextAnnotation {
	private String text;
	private boolean shapeIsFit = false;

	public static final String NAME="BOUNDED";
	public static final String FONTCOLOR="fontColor";
	public static final String TEXT="text";
	public static final String COLOR="color";
	public static final String FONTFAMILY="fontFamily";
	public static final String FONTSIZE="fontSize";
	public static final String FONTSTYLE="fontStyle";

	private Font scaledFont = null;
	private double lastScaleFactor = -1;

	protected float fontSize = 0.0f;
	protected Font font = null;
	protected int initialFontSize=12;
	protected Color textColor = Color.BLACK;
	
	public BoundedTextAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view) { 
		super(cyAnnotator, view, 100, 100);
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
		this.text = "Text Annotation";
		super.setSize(getTextWidth(null)+4, getTextHeight(null)+4);
	}

	public BoundedTextAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, double width, double height) { 
		super(cyAnnotator, view, width, height);
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
		this.text = "Text Annotation";
	}

	public BoundedTextAnnotationImpl(BoundedTextAnnotationImpl c) { 
		super(c, 100, 100);
		this.text = c.getText();
		this.textColor = c.getTextColor();
		this.fontSize = (float)c.getFontSize();
		this.font = c.getFont();
	}

	public BoundedTextAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view,
                                   double x, double y, ShapeType shapeType,
                                   double width, double height,
                                   Paint fillColor, Paint edgeColor,
                                   float edgeThickness, String text, int compCount, double zoom){
		super(cyAnnotator, view, x, y, shapeType, width, height, fillColor, edgeColor, edgeThickness);
		this.text=text;
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
	}

	public BoundedTextAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, 
	                                 Map<String, String> argMap) {
		super(cyAnnotator, view, argMap);
		this.font = getArgFont(argMap);
		this.textColor = getColor(argMap.get(COLOR));
		this.text = argMap.get(TEXT);
		this.fontSize = font.getSize2D();
		if (!argMap.containsKey(ShapeAnnotationImpl.WIDTH)) {
			double width = getTextWidth(null)+8;
			double height = getTextHeight(null)+8;
			super.setSize(width, height);
		}
	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE, NAME);
		argMap.put(TEXT,this.text);
		argMap.put(COLOR,convertColor(this.textColor));
		argMap.put(FONTFAMILY,this.font.getFamily());
		argMap.put(FONTSIZE,Integer.toString(this.font.getSize()));
		argMap.put(FONTSTYLE,Integer.toString(this.font.getStyle()));
		return argMap;
	}

	public void fitShapeToText() {
		double width = getTextWidth(null)+8;
		double height = getTextHeight(null)+8;
		shapeIsFit = true;

		// Different depending on the type...
		switch (getShapeType()) {
		case ELLIPSE:
			width = getTextWidth(null)*3/2+8;
			height = getTextHeight(null)*2;
			break;
		case TRIANGLE:
			width = getTextWidth(null)*3/2+8;
			height = getTextHeight(null)*2;
			break;
		case PENTAGON:
		case HEXAGON:
		case STAR5:
		case STAR6:
			width = getTextWidth(null)*9/7+8;
			height = width;
			break;
		}

		super.setSize(width, height);
		setSize((int)width+2, (int)height+2);
	}
	
	public JFrame getModifyDialog() {
			return new BoundedTextAnnotationDialog(this);
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);

		// For now, we put the text in the middle of the shape.  At some point, we may
		// want to add other options
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(textColor);
		Font tFont = font.deriveFont(((float)(scaleFactor/getZoom()))*font.getSize2D());
		FontMetrics fontMetrics=g.getFontMetrics(tFont);
		x = x + (getWidth()-getTextWidth(g2))/2;
		y = y + (getHeight()-getTextHeight(g2))/2;
		g2.setFont(tFont);
		g2.drawChars(getText().toCharArray(), 0, getText().length(),
 		             (int)(x*scaleFactor), (int)(y*scaleFactor));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2=(Graphics2D)g;
		g2.setColor(textColor);
		g2.setFont(font);

		if(usedForPreviews) {
			g2.drawChars(getText().toCharArray(), 0, getText().length(),
			             (int)(getWidth()-getTextWidth(g2))/2,
			             (int)(getHeight())/2 );
			return;
		}

		g2.drawChars(getText().toCharArray(), 0, getText().length(),
			           getX()+(int)(getWidth()-getTextWidth(g2))/2,
			           getY()+(int)(getHeight())/2 );
	}

	@Override
	public void setSpecificZoom(double zoom) {
		font=font.deriveFont(((float)(zoom/getSpecificZoom()))*font.getSize2D());
		super.setSpecificZoom(zoom);		
	}

	@Override
	public void setZoom(double zoom) {
		font=font.deriveFont(((float)(zoom/getZoom()))*font.getSize2D());
		super.setZoom(zoom);
	}

	@Override
	public void setText(String text) {
		this.text = text;
		if (shapeIsFit)
			fitShapeToText();

		updateBounds();
	}

	@Override
	public String getText() { return this.text; }


	@Override
	public void setTextColor(Color color) {
		this.textColor = color;
	}

	@Override
	public Color getTextColor() { return textColor; }

	@Override
	public void setFontSize(double size) {
		this.fontSize = (float)size;
		scaledFont = font.deriveFont((float)(fontSize*getSpecificZoom()));
		updateBounds();
	}

	@Override
	public double getFontSize() { return this.fontSize; }


	@Override
	public void setFontStyle(int style) {
		font = font.deriveFont(style, fontSize);
		scaledFont = font.deriveFont((float)(fontSize*getSpecificZoom()));
	}

	@Override
	public int getFontStyle() {
		return font.getStyle();
	}

	@Override
	public void setFontFamily(String family) {
		font = new Font(family, font.getStyle(), (int)fontSize);
		scaledFont = font.deriveFont((float)(fontSize*getSpecificZoom()));
	}

	@Override
	public String getFontFamily() {
		return font.getFamily();
	}

	public Font getFont() { return this.font; }

	public void setFont(Font font) { 
		this.font = font; 
	}

	private void updateBounds() {
		// Our bounds should be the larger of the shape or the text
		int xBound = Math.max(getTextWidth(null), (int)shapeWidth);
		int yBound = Math.max(getTextHeight(null), (int)shapeHeight);
		setSize(xBound+4, yBound+4);
	}

	int getTextWidth(Graphics2D g2) {
		if (g2 != null) {
			FontMetrics fontMetrics=g2.getFontMetrics(font);
			return fontMetrics.stringWidth(text);
		}
		// If we don't have a graphics context, yet, make some assumptions
		return (int)(text.length()*fontSize);
	}

	int getTextHeight(Graphics2D g2) {
		if (g2 != null) {
			FontMetrics fontMetrics=g2.getFontMetrics(font);
			return fontMetrics.getHeight();
		}
		// If we don't have a graphics context, yet, make some assumptions
		return (int)(fontSize*1.5);
	}

	Font getArgFont(Map<String, String> argMap) {
		String family = argMap.get(FONTFAMILY);
		int size = Integer.parseInt(argMap.get(FONTSIZE));
		int style = Integer.parseInt(argMap.get(FONTSTYLE));
		return new Font(family, style, size);
	}

}
