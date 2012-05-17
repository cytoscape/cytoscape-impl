package org.cytoscape.ding.impl.cyannotator.annotations;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.modify.mArrow;
import org.cytoscape.ding.impl.cyannotator.modify.mTextAnnotation;
import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.DGraphView;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Avinash Thummala
 */

//A BasicTextAnnotation Class
//
// TODO: We need to refactor this into an Annotation base class and various Annotation
// types.

public class TextAnnotation extends Annotation {
	private String text;

	public static final String NAME="TEXT";

	private Font scaledFont = null;
	private double lastScaleFactor = -1;

	protected float fontSize = 0.0f;
	protected Font font = null;
	protected int initialFontSize=12;

	public TextAnnotation() { super(); }

	public TextAnnotation(CyAnnotator cyAnnotator, DGraphView view, 
	                      int x, int y, String text, int compCount, double zoom){
		super(cyAnnotator, view, x, y, compCount, zoom);
		this.text=text;
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
		updateAnnotationAttributes();
	}

	// This constructor is used to construct a text annotation from an
	// argument map.
	public TextAnnotation(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		super(cyAnnotator, view, argMap);
		this.font = getArgFont(argMap);
		this.color = getColor(argMap.get(COLOR));
		this.text = argMap.get(TEXT);
		this.fontSize = font.getSize2D();
		updateAnnotationAttributes();
	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,NAME);
		argMap.put(TEXT,this.text);
		argMap.put(COLOR,convertColor(this.color));
		argMap.put(FONTFAMILY,this.font.getFamily());
		argMap.put(FONTSIZE,Integer.toString(this.font.getSize()));
		argMap.put(FONTSTYLE,Integer.toString(this.font.getStyle()));
		return argMap;
	}

	protected Font getArgFont(Map<String, String> argMap) {
		String family = argMap.get(FONTFAMILY);
		int size = Integer.parseInt(argMap.get(FONTSIZE));
		int style = Integer.parseInt(argMap.get(FONTSTYLE));
		return new Font(family, style, size);
	}

	//Verification methods
	public boolean isTextAnnotation() { return true; }
	
	//Get Methods	
	public String getText() {
		return text;
	}

	public Color getTextColor() {
		return color;
	}

	public Font getFont() {
		return font;
	}

	public int getTopX(){
		return getX();
	}

	public int getTopY(){
		return getY();
	}
	
	public int getAnnotationWidth(){
		return getTextWidth();
	}

	public int getAnnotationHeight(){
		return getTextHeight();
	}

	public int getTextWidth(){
		FontMetrics fontMetrics=this.getGraphics().getFontMetrics(font);
		return fontMetrics.stringWidth(text);
	}

	public int getTextHeight(){
		FontMetrics fontMetrics=this.getGraphics().getFontMetrics(font);
		return fontMetrics.getHeight();
	}

	public int getTextHeight(Graphics2D g2, double scaleFactor) {
		if (scaleFactor == lastScaleFactor || scaledFont == null) {
			scaledFont = getScaledFont(scaleFactor);
			lastScaleFactor = scaleFactor;
		}
		return g2.getFontMetrics(scaledFont).getHeight();
	}

	public int getTextWidth(Graphics2D g2, double scaleFactor) {
		if (scaleFactor == lastScaleFactor || scaledFont == null) {
			scaledFont = getScaledFont(scaleFactor);
			lastScaleFactor = scaleFactor;
		}
		return g2.getFontMetrics(scaledFont).stringWidth(text);
	}
	
	public int getTextWidth(Graphics2D g2){
		FontMetrics fontMetrics=g2.getFontMetrics(font);
		return fontMetrics.stringWidth(text);
	}

	public int getTextHeight(Graphics2D g2){
		FontMetrics fontMetrics=g2.getFontMetrics(font);
		return fontMetrics.getHeight();
	}	

	private Font getScaledFont(double scaleFactor) {
		return font.deriveFont(((float)(scaleFactor/zoom))*font.getSize2D());
	}

	//Set methods
	public void setText(String newText) {
		this.text=newText;
		updateAnnotationAttributes();
	}

	public void setTextColor(Color color) {
		this.color = color;
		updateAnnotationAttributes();
	}

	@Override
	public void setFont(Font tFont) {
		float fZoom = 1.0f;
		if(!usedForPreviews) {			
			// Calculate our current zoom
			float currentSize = font.getSize2D();
			fZoom = currentSize/fontSize;
		}

		fontSize = tFont.getSize2D();
		this.font=tFont.deriveFont(tFont.getSize2D()*fZoom);
		scaledFont = null;
		updateAnnotationAttributes();
	}

	public void adjustSpecificZoom(double newZoom){
		font=font.deriveFont(((float)(newZoom/getTempZoom()))*font.getSize2D());
		setTempZoom(newZoom);

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
		updateAnnotationAttributes();
	}

	public void adjustZoom(double newZoom){
		font=font.deriveFont(((float)(newZoom/getZoom()))*font.getSize2D());

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
		
		adjustArrowThickness(newZoom);
		
		setZoom(newZoom);
		updateAnnotationAttributes();
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.paint(g);
		Graphics2D g2=(Graphics2D)g;
		g2.setColor(color);
		Font tFont=getScaledFont(scaleFactor);
		FontMetrics fontMetrics=g.getFontMetrics(tFont);
		g2.setFont(tFont);
		g2.drawChars(getText().toCharArray(), 0, getText().length(), 
		             (int)(x*scaleFactor), (int)(y*scaleFactor)+fontMetrics.getHeight());
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2=(Graphics2D)g;
		g2.setColor(color);
		g2.setFont(font);
			
		if(usedForPreviews) {			
			g2.drawChars(getText().toCharArray(), 0, getText().length(), 
			             getX()+(int)(getWidth()-getTextWidth(g2))/2, 
			             getY()+(int)(getHeight()+getTextHeight(g2))/2 );
			return;
		}
		g2.drawChars(getText().toCharArray(), 0, getText().length(), 
		             getX(), getY()+getTextHeight());				

		if(isSelected()) {
			//Selected Annotations will have a yellow border
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2.0f));
			g2.drawRect(getTopX(), getTopY(), getAnnotationWidth(), (int)(getAnnotationHeight()*1.5));
		}
	}
	
	public void addModifyMenuItem(JPopupMenu popup) {
		JMenuItem modify=new JMenuItem("Modify Properties");
		modify.addActionListener(new modifyTextAnnotationListener());
		
		popup.add(modify);
	}

	class modifyTextAnnotationListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			mTextAnnotation mTAnnotation=new mTextAnnotation(TextAnnotation.this, cyAnnotator);

			mTAnnotation.setVisible(true);
			mTAnnotation.setSize(474, 504);		
			mTAnnotation.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			mTAnnotation.setResizable(false);
			
			mTAnnotation.setLocation(TextAnnotation.this.getX(), TextAnnotation.this.getY());			
		}
	}  
}
