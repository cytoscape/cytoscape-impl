package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

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

public class TextAnnotationImpl extends AbstractAnnotation implements TextAnnotation {
	
	public static final String DEF_TEXT = "Text";
	
	protected static final Font DEF_FONT = new Font("Arial", Font.PLAIN, 12);

  // public static final String splitPattern = "[^" + Pattern.quote("\\")+"]"+Pattern.quote("\\n");
  public static final String splitPattern = "(?<!\\\\)"+Pattern.quote("\\n");

	private String[] text = new String[]{DEF_TEXT};
	private Font font = DEF_FONT;
	private float fontSize = DEF_FONT.getSize();
	private Color textColor = Color.BLACK;

	private float savedFontSize;
	
	/** Initially, the name is the same as the text */
	private boolean updateNameFromText = true;

	public TextAnnotationImpl(DRenderingEngine re, boolean usedForPreviews) { 
		super(re, usedForPreviews); 
	}

	public TextAnnotationImpl(TextAnnotationImpl c, boolean usedForPreviews) {
		super(c, usedForPreviews);
		
		text = splitString(c.getText());
		textColor = c.getTextColor();
		fontSize = (float) c.getFontSize();
		
		if (c.getFont() != null)
			font = c.getFont();
		
		name = c.getName() != null ? c.getName() : getDefaultName();
	}

	public TextAnnotationImpl(
			DRenderingEngine re,
			int x,
			int y,
			double rotation,
			String text,
			int compCount,
			double zoom
	) {
		super(re, x, y, rotation);

		this.text = splitString(text);
		setSize(getAnnotationWidth(), getAnnotationHeight());
	}

	public TextAnnotationImpl(DRenderingEngine re, Map<String,String> argMap) {
		super(re, argMap);
		
		if (name == null && text != null && !text[0].trim().isEmpty())
			name = text[0].trim();
		
		text = splitString(ViewUtils.getString(argMap, TEXT, ""));
		
		textColor = (Color) ViewUtils.getColor(argMap, COLOR, Color.BLACK);
		
		font = ViewUtils.getArgFont(argMap, font.getFamily(), font.getStyle(), font.getSize());
		double zoom = getLegacyZoom(argMap);
		
		if (zoom != 1.0)
			font = font.deriveFont(font.getSize2D() / (float) zoom);
		
		fontSize = font.getSize();
		setSize(getAnnotationWidth(), getAnnotationHeight());
	}

	@Override
	public Map<String,String> getArgMap() {
		var argMap = super.getArgMap();
		argMap.put(TYPE, TextAnnotation.class.getName());
		argMap.put(TEXT, joinString(text));
		argMap.put(COLOR, ViewUtils.serialize(textColor));
		argMap.put(FONTFAMILY, font.getFamily());
		argMap.put(FONTSIZE, Integer.toString(font.getSize()));
		argMap.put(FONTSTYLE, Integer.toString(font.getStyle()));
		
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
    String[] lines = splitString(text);
		if (!Objects.equals(lines, this.text)) {
			var oldValue = joinString(this.text);
			this.text = lines;
			
			if (updateNameFromText)
				name = text != null ? this.text[0].trim() : "";
			
			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());
			
			update();
			firePropertyChange("text", oldValue, text);
		}
	}

	@Override
	public String getText() {
		return joinString(text);
	}

	@Override
	public void setTextColor(Color color) {
		if (!Objects.equals(textColor, color)) {
			var oldValue = textColor;
			textColor = color;
			update();
			firePropertyChange("textColor", oldValue, textColor);
		}
	}

	@Override
	public Color getTextColor() {
		return textColor;
	}

	@Override
	public void setFontSize(double size) {
		if (fontSize != (float) size) {
			var oldValue = fontSize;
			fontSize = (float) size;
			
			if (font != null)
				font = font.deriveFont(fontSize);

			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());

			update();
			firePropertyChange("fontSize", oldValue, fontSize);
		}
	}

	@Override
	public double getFontSize() {
		return fontSize;
	}

	@Override
	public void setFontStyle(int style) {
		if (font != null && style != font.getStyle()) {
			var oldValue = font.getStyle();
			font = font.deriveFont(style, fontSize);
			
			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());
			
			update();
			firePropertyChange("fontStyle", oldValue, style);
		}
	}

	@Override
	public int getFontStyle() {
		return font != null ? font.getStyle() : Font.PLAIN;
	}

	@Override
	public void setFontFamily(String family) {
		if (family != null && !family.equalsIgnoreCase(getFontFamily())) {
			var oldValue = getFontFamily();
			font = new Font(family, getFontStyle(), (int) fontSize);
			
			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());
			
			update();
			firePropertyChange("fontFamily", oldValue, family);
		}
	}

	@Override
	public String getFontFamily() {
		return font != null ? font.getFamily() : null;
	}

	@Override
	public Font getFont() {
		return font;
	}

	@Override
	public void setFont(Font font) {
		if (font != null && !font.equals(this.font)) {
			var oldValue = this.font;
			this.font = font;
			this.fontSize = font.getSize2D();
			
			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());
			
			update();
			firePropertyChange("font", oldValue, font);
		}
	}

	@Override
	public void setBounds(Rectangle2D newBounds) {
		if (newBounds.getWidth() == 0 || newBounds.getHeight() == 0)
			return;

		var initialBounds = getBounds();

		if (initialBounds.getWidth() != 0) {
			double factor = newBounds.getWidth() / initialBounds.getWidth();

			double fontSize;

			if (savedFontSize != 0.0)
				fontSize = (savedFontSize * factor);
			else
				fontSize = (this.fontSize * factor);

			setFontSize((float) fontSize);
		}

		super.setBounds(newBounds);
		update();
	}
	
	/**
	 * This applies only text color and font.
	 */
	@Override
	public void setStyle(Map<String, String> argMap) {
		if (argMap != null) {
			setTextColor((Color) ViewUtils.getColor(argMap, COLOR, Color.BLACK));
			
			var newFont = ViewUtils.getArgFont(argMap, font.getFamily(), font.getStyle(), font.getSize());
			double zoom = getLegacyZoom(argMap);
			
			if (zoom != 1.0)
				newFont = newFont.deriveFont(newFont.getSize2D() / (float) zoom);
			
			setFont(newFont);
		}
	}
	
	@Override
	public void paint(Graphics g, boolean showSelection) {
		if (text == null || textColor == null || font == null)
			return;

		if (font.getSize2D() <= 0.0) // trying to render 0 sized fonts when exporting PDF causes an exception
			return;
		
		super.paint(g, showSelection);

		var g2 = (Graphics2D) g.create();

		g2.setPaint(textColor);
		g2.setFont(font);

		// Handle opacity
		int alpha = textColor.getAlpha();
		float opacity = (float) alpha / (float) 255;
		var originalComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		var currentTransform = g2.getTransform();
    float yValue = (float)getY();
		if (rotation != 0) {
			g2.rotate(Math.toRadians(rotation), (int) (getX() + getWidth() / 2), (int) (getY() + getHeight() / 2));
			g2.setClip(getBounds());
      for (String t: text) {
        LineMetrics metrics = font.getLineMetrics(t, new FontRenderContext(null, true, true));
        float ascent = metrics.getAscent();
        g2.drawString(t, (float) getX(), yValue + ascent);
        yValue += metrics.getHeight();
      }
			g2.setTransform(currentTransform);
		} else {
			g2.setClip(getBounds());
      for (String t: text) {
        LineMetrics metrics = font.getLineMetrics(t, new FontRenderContext(null, true, true));
        float ascent = metrics.getAscent();
        g2.drawString(t, (float) getX(), yValue + ascent);
        yValue += metrics.getHeight();
      }
		}

		g2.setComposite(originalComposite);
		g2.dispose();
	}
	
	@Override
	protected String getDefaultName() {
		return text != null ? text[0] : DEF_TEXT;
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
		
    // We need to find the longest text string
    double width = 0.0;
    for (String t: text) 
      width = Math.max(width,font.getStringBounds(t, new FontRenderContext(null, true, true)).getWidth());
    return width;
	}

	double getTextHeight() {
		if (text == null) 
			return 0.0;
		
    double height = 0.0;
    for (String t: text) 
		  height += font.getStringBounds(t, new FontRenderContext(null, true, true)).getHeight();
    return height;
	}

  /**
   * Split string with possible newlines into separate lines.  This scans for actual newlines
   * as well as '\n' characters
   */
  static protected String[] splitString(String str) {
    String[] res = str.split("\n");   // First, try splitting on real newlines
    if (res.length > 1) return res;

    // OK, now split on "\n" characters, but allow escaping
    return str.split(splitPattern);
  }

  static protected String joinString(String[] lines) {
    return String.join("\\n", lines);
  }
}
