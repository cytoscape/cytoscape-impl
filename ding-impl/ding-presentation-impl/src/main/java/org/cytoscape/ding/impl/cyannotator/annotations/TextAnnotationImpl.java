package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.dialogs.TextAnnotationDialog;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.impl.strokes.EqualDashStroke;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

public class TextAnnotationImpl extends AbstractAnnotation implements TextAnnotation {
	
	private static final String DEF_TEXT = "Text";

	private String text = "";

	protected float fontSize;
	protected float savedFontSize;
	protected Font font;
	protected int initialFontSize = 12;
	protected Color textColor = Color.BLACK;

	/** Initially, the name is the same as the text */
	private boolean updateNameFromText = true;

	public TextAnnotationImpl(DRenderingEngine re, boolean usedForPreviews) { 
		super(re, usedForPreviews); 
		
		this.font = new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float) initialFontSize;
		this.text = DEF_TEXT;
	}

	public TextAnnotationImpl(TextAnnotationImpl c, boolean usedForPreviews) {
		super(c, usedForPreviews);
		
		text = c.getText();
		textColor = c.getTextColor();
		fontSize = (float) c.getFontSize();
		font = c.getFont();
		name = c.getName() != null ? c.getName() : getDefaultName();
	}

	public TextAnnotationImpl(
			DRenderingEngine re,
			int x,
			int y,
			String text,
			int compCount,
			double zoom
	) {
		super(re, x, y);

		this.text = text;
		this.font = new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float) initialFontSize;
		setSize(getAnnotationWidth(), getAnnotationHeight());
	}

	// This constructor is used to construct a text annotation from an
	// argument map.
	// Need to make sure all arguments have reasonable options
	public TextAnnotationImpl(DRenderingEngine re, Map<String, String> argMap) {
		super(re, argMap);
		
		font = ViewUtils.getArgFont(argMap, "Arial", Font.PLAIN, initialFontSize);
		textColor = (Color) ViewUtils.getColor(argMap, COLOR, Color.BLACK);
		text = ViewUtils.getString(argMap, TEXT, "");
		fontSize = font.getSize();
		
		if (name == null && text != null && !text.trim().isEmpty())
			name = text.trim();
		
		setSize(getAnnotationWidth(), getAnnotationHeight());
	}

	@Override
	public Map<String,String> getArgMap() {
		var argMap = super.getArgMap();
		argMap.put(TYPE, TextAnnotation.class.getName());
		argMap.put(TEXT, this.text);
		argMap.put(COLOR, ViewUtils.convertColor(this.textColor));
		argMap.put(FONTFAMILY, this.font.getFamily());
		argMap.put(FONTSIZE, Integer.toString(this.font.getSize()));
		argMap.put(FONTSTYLE, Integer.toString(this.font.getStyle()));
		return argMap;
	}

	@Override
	public void setName(String name) {
		// Assuming setName() is called by an app or the UI,
		// we no longer want it to automatically update the name from the Annotation text.
		if (name != null && !name.isEmpty())
			updateNameFromText = false;

		super.setName(name);
	}
	
	@Override
	public Class<? extends Annotation> getType() {
		return TextAnnotation.class;
	}
	
	@Override
	public void setZoom(double zoom) {
		if (zoom == getZoom())
			return;

		fontSize = (float) ((zoom / getZoom()) * fontSize);
		font = font.deriveFont(fontSize);

		if (!usedForPreviews)
			setSize(getAnnotationWidth(), getAnnotationHeight());

		super.setZoom(zoom);
	}

	@Override
	public void setSpecificZoom(double zoom) {
		if (zoom == getSpecificZoom())
			return;
		
		// font=font.deriveFont(((float)(zoom/getSpecificZoom()))*font.getSize2D());
		fontSize = (float) ((zoom / getSpecificZoom()) * fontSize);
		font = font.deriveFont(fontSize);

		if (!usedForPreviews)
			setSize(getAnnotationWidth(), getAnnotationHeight());
		
		super.setSpecificZoom(zoom);
	}

	@Override
	public void setText(String text) {
		this.text = text;
		
		if (updateNameFromText)
			name = text != null ? text.trim() : "";
		
		if (!usedForPreviews)
			setSize(getAnnotationWidth(), getAnnotationHeight());
		
		update();
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setTextColor(Color color) {
		this.textColor = color;
		update();
	}

	@Override
	public Color getTextColor() {
		return textColor;
	}

	@Override
	public void setFontSize(double size) {
		this.fontSize = (float)size;
		font = font.deriveFont((float)fontSize);
		if (!usedForPreviews)
			setSize(getAnnotationWidth(), getAnnotationHeight());
		update();
	}


	@Override
	public double getFontSize() {
		return this.fontSize;
	}

	@Override
	public void setFontStyle(int style) {
		font = font.deriveFont(style, (float) (fontSize));
		if (!usedForPreviews)
			setSize(getAnnotationWidth(), getAnnotationHeight());
		update();
	}

	@Override
	public int getFontStyle() {
		return font.getStyle();
	}

	@Override
	public void setFontFamily(String family) {
		font = new Font(family, font.getStyle(), (int) fontSize);
		if (!usedForPreviews)
			setSize(getAnnotationWidth(), getAnnotationHeight());
		update();
	}

	@Override
	public String getFontFamily() {
		return font.getFamily();
	}

	@Override
	public Font getFont() {
		return this.font;
	}

	@Override
	public void setFont(Font font) {
		this.font = font;
		this.fontSize = font.getSize2D();
		if (!usedForPreviews)
			setSize(getAnnotationWidth(), getAnnotationHeight());
		update();
	}

	@Override
	public JDialog getModifyDialog() {
		return new TextAnnotationDialog(this, ViewUtil.getActiveWindow(re));
	}

	@Override
	public void setBounds(Rectangle2D newBounds) {
		if(newBounds.getWidth() == 0 || newBounds.getHeight() == 0)
			return;
			
		Rectangle2D initialBounds = getBounds();

		if(initialBounds.getWidth() != 0) {
			double factor = newBounds.getWidth() / initialBounds.getWidth();
			
			double fontSize;
			if(savedFontSize != 0.0)
				fontSize = (this.savedFontSize * factor);
			else
				fontSize = (this.fontSize * factor);
			
			
			this.fontSize = (float) fontSize;
			this.font = font.deriveFont((float)fontSize);
		}
		
		super.setBounds(newBounds);
		update();
	}
	
	
	@Override
	public void paint(Graphics graphics, boolean showSelection) {
		if (text == null || textColor == null || font == null) 
			return;

		super.paint(graphics, showSelection);
		Graphics2D g = (Graphics2D)graphics.create();

		g.setPaint(textColor);
		g.setFont(font);
		g.setClip(getBounds());

		// Handle opacity
		int alpha = textColor.getAlpha();
		float opacity = (float)alpha/(float)255;
		final Composite originalComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		float ascent = font.getLineMetrics(text , new FontRenderContext(null, true, true)).getAscent();
		g.drawString(text, (float)getX(), (float)getY()+ascent);

		if(showSelection && isSelected()) {
			g.setColor(Color.GRAY);
			g.setStroke(new EqualDashStroke(2.0f));
			g.draw(getBounds());
		}
		
		g.setComposite(originalComposite);
		g.dispose();
	}
	
	@Override
	protected String getDefaultName() {
		return text != null ? text : DEF_TEXT;
	}

	private double getAnnotationWidth() {
		return getTextWidth() + 1.0;
	}

	private double getAnnotationHeight() {
		return getTextHeight() + 1.0;
	}

	double getTextWidth() {
		if (text == null) 
			return 0.0;
		return font.getStringBounds(text, new FontRenderContext(null, true, true)).getWidth();
	}

	double getTextHeight() {
		if (text == null) 
			return 0.0;
		return font.getStringBounds(text, new FontRenderContext(null, true, true)).getHeight();
	}
}
