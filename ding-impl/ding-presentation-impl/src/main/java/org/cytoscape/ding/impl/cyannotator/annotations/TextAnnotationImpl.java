package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.JFrame;

import java.util.Map;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.TextAnnotation;
import org.cytoscape.ding.impl.cyannotator.dialogs.TextAnnotationDialog;

public class TextAnnotationImpl extends AbstractAnnotation implements TextAnnotation {
	private String text;

	public static final String NAME="TEXT";
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

	public TextAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view) { 
		super(cyAnnotator, view); 
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
		this.text = "Text Annotation";
	}

	public TextAnnotationImpl(TextAnnotationImpl c) {
		super(c);
		this.text = c.getText();
		this.textColor = c.getTextColor();
		this.fontSize = (float)c.getFontSize();
		this.font = c.getFont();
	}

	public TextAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, 
	                          int x, int y, String text, int compCount, double zoom){
		super(cyAnnotator, view, x, y, zoom);
		this.text=text;
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
		setSize(getAnnotationWidth(), getAnnotationHeight());
		updateAnnotationAttributes();
	}

	// This constructor is used to construct a text annotation from an
	// argument map.
	public TextAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		super(cyAnnotator, view, argMap);
		this.font = getArgFont(argMap);
		this.textColor = getColor(argMap.get(COLOR));
		this.text = argMap.get(TEXT);
		this.fontSize = font.getSize2D();
		setSize(getAnnotationWidth(), getAnnotationHeight());
		updateAnnotationAttributes();
	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE,NAME);
		argMap.put(TEXT,this.text);
		argMap.put(COLOR,convertColor(this.textColor));
		argMap.put(FONTFAMILY,this.font.getFamily());
		argMap.put(FONTSIZE,Integer.toString(this.font.getSize()));
		argMap.put(FONTSTYLE,Integer.toString(this.font.getStyle()));
		return argMap;
	}

	@Override
	public void setZoom(double zoom) {
		font=font.deriveFont(((float)(zoom/getZoom()))*font.getSize2D());

		setSize(getAnnotationWidth(), getAnnotationHeight());
		super.setZoom(zoom);
	}

	@Override
	public void setSpecificZoom(double zoom) {
		font=font.deriveFont(((float)(zoom/getSpecificZoom()))*font.getSize2D());
				
		setSize(getAnnotationWidth(), getAnnotationHeight());
		super.setZoom(zoom);
	}

	@Override
	public void setText(String text) {
		this.text = text;
		updateAnnotationAttributes();
	}

	@Override
	public String getText() { return this.text; }


	@Override
	public void setTextColor(Color color) {
		this.textColor = color;
		updateAnnotationAttributes();
	}

	@Override
	public Color getTextColor() { return textColor; }

	@Override
	public void setFontSize(double size) {
		this.fontSize = (float)size;
		scaledFont = font.deriveFont((float)(fontSize*getSpecificZoom()));
		updateAnnotationAttributes();
	}

	@Override
	public double getFontSize() { return this.fontSize; }


	@Override
	public void setFontStyle(int style) {
		font = font.deriveFont(style, fontSize);
		scaledFont = font.deriveFont((float)(fontSize*getSpecificZoom()));
		updateAnnotationAttributes();
	}

	@Override
	public int getFontStyle() {
		return font.getStyle();
	}


	@Override
	public void setFontFamily(String family) {
		font = new Font(family, font.getStyle(), (int)fontSize);
		scaledFont = font.deriveFont((float)(fontSize*getSpecificZoom()));
		updateAnnotationAttributes();
	}

	@Override
	public String getFontFamily() {
		return font.getFamily();
	}

	public Font getFont() { return this.font; }

	public void setFont(Font font) { 
		this.font = font; 
	}

	public JFrame getModifyDialog() {
		return new TextAnnotationDialog(this);
	}


	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);

		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(textColor);
		Font tFont = font.deriveFont(((float)(scaleFactor/getZoom()))*font.getSize2D());
		FontMetrics fontMetrics=g.getFontMetrics(tFont);
		g2.setFont(tFont);
		g2.drawChars(getText().toCharArray(), 0, getText().length(),
 		             (int)(x*scaleFactor), (int)(y*scaleFactor)+fontMetrics.getHeight());
	}

	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2=(Graphics2D)g;
		g2.setColor(textColor);
		g2.setFont(font);

		if(usedForPreviews) {
			System.out.println("Printing preview text at: "+(getX()+(int)(getWidth()-getTextWidth(g2))/2)+","+(getY()+(int)(getHeight()+getTextHeight(g2))/2));
			Thread.dumpStack();
			g2.drawChars(getText().toCharArray(), 0, getText().length(),
			             getX()+(int)(getWidth()-getTextWidth(g2))/2,
			             getY()+(int)(getHeight()+getTextHeight(g2))/2 );
			return;
		}

		g2.drawChars(getText().toCharArray(), 0, getText().length(),
		             getX(), getY()+getTextHeight(g2));

		if(isSelected()) {
      //Selected Annotations will have a yellow border
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2.0f));
			g2.drawRect(getX()-4, getY()-4, getTextWidth(g2)+8, getTextHeight(g2)+8);
		}
	}

	int getAnnotationWidth() {
		return getTextWidth((Graphics2D)this.getGraphics());
	}

	int getAnnotationHeight() {
		return getTextHeight((Graphics2D)this.getGraphics());
	}

	int getTextWidth(Graphics2D g2) {
		FontMetrics fontMetrics=g2.getFontMetrics(font);
		return fontMetrics.stringWidth(text);
	}

	int getTextHeight(Graphics2D g2) {
		FontMetrics fontMetrics=g2.getFontMetrics(font);
		return fontMetrics.getHeight();
	}

	Font getArgFont(Map<String, String> argMap) {
		String family = argMap.get(FONTFAMILY);
		int size = Integer.parseInt(argMap.get(FONTSIZE));
		int style = Integer.parseInt(argMap.get(FONTSTYLE));
		return new Font(family, style, size);
	}

}
