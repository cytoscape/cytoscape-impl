package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
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

public class BoundedTextAnnotationImpl extends ShapeAnnotationImpl 
                                       implements BoundedTextAnnotation, TextAnnotation {
	
	private static final String DEF_TEXT = "Text";
	private static final int HPAD = 4;
	private static final int VPAD = 4;
	
	private String[] text = new String[]{DEF_TEXT};
	private boolean shapeIsFit;
	protected float fontSize;
	protected float savedFontSize;
	protected Font font;
	protected int initialFontSize = 12;
	protected Color textColor = Color.BLACK;
	
	/** Initially, the name is the same as the text */
	private boolean updateNameFromText = true;

	public BoundedTextAnnotationImpl(DRenderingEngine re, boolean usedForPreviews) { 
		super(re, 100, 100, usedForPreviews);
		this.font = new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float) initialFontSize;
		super.setSize(getTextWidth() + HPAD, getTextHeight() + VPAD);
	}

	public BoundedTextAnnotationImpl(DRenderingEngine re, double width, double height) {
		super(re, width, height, false);
		this.font = new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float) initialFontSize;
	}

	public BoundedTextAnnotationImpl(BoundedTextAnnotationImpl c) {
		super(c, 100, 100, false);
		this.text = TextAnnotationImpl.splitString(c.getText());
		this.textColor = c.getTextColor();
		this.fontSize = (float) c.getFontSize();
		this.font = c.getFont();
	}

	public BoundedTextAnnotationImpl(
			DRenderingEngine re,
			double x,
			double y,
			double rotation,
			ShapeType shapeType,
			double width,
			double height,
			Paint fillColor,
			Paint edgeColor,
			float edgeThickness,
			String text,
			int compCount,
			double zoom
	) {
		super(re, x, y, rotation, shapeType, width, height, fillColor, edgeColor, edgeThickness);
		this.text = TextAnnotationImpl.splitString(text);
		this.font = new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float) initialFontSize;
	}

	public BoundedTextAnnotationImpl(DRenderingEngine re, Map<String, String> argMap) {
		super(re, argMap);

		this.font = ViewUtils.getArgFont(argMap, "Arial", Font.PLAIN, initialFontSize);
		double zoom = getLegacyZoom(argMap);

		if (zoom != 1.0)
			font = font.deriveFont(font.getSize2D() / (float) zoom);

		this.textColor = (Color) ViewUtils.getColor(argMap, COLOR, Color.BLACK);
		text = TextAnnotationImpl.splitString(ViewUtils.getString(argMap, TEXT, ""));
		this.fontSize = font.getSize();

		if (argMap.containsKey(NAME)) {
			this.name = ViewUtils.getString(argMap, NAME, "");
		} else {
      if (name == null && text != null && !text[0].trim().isEmpty())
        name = text[0].trim();
		}

		if (!argMap.containsKey(BoundedTextAnnotation.WIDTH)) {
			double width = getTextWidth() + 8;
			double height = getTextHeight() + 8;
			super.setSize(width, height);
		}
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
		return BoundedTextAnnotation.class;
	}
	
	@Override
	public Map<String,String> getArgMap() {
		var argMap = super.getArgMap();
		argMap.put(TYPE, BoundedTextAnnotation.class.getName());
		argMap.put(TEXT, TextAnnotationImpl.joinString(text));
		argMap.put(COLOR, ViewUtils.serialize((Paint) this.textColor));
		argMap.put(FONTFAMILY, this.font.getFamily());
		argMap.put(FONTSIZE, Integer.toString(this.font.getSize()));
		argMap.put(FONTSTYLE, Integer.toString(this.font.getStyle()));
		
		return argMap;
	}
	
	/**
	 * This applies only text color, font and the ShapeAnnotation's style properties.
	 */
	@Override
	public void setStyle(Map<String, String> argMap) {
		super.setStyle(argMap);
		
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
	public void fitShapeToText() {
		double width = getTextWidth() + 8;
		double height = getTextHeight() + 8;
		shapeIsFit = true;

		// Different depending on the type...
		var shapeType = getShapeTypeEnum();
		
		switch (shapeType) {
			case ELLIPSE:
				width = getTextWidth() * 3 / 2 + 8;
				height = getTextHeight() * 2;
				break;
			case TRIANGLE:
				width = getTextWidth() * 3 / 2 + 8;
				height = getTextHeight() * 2;
				break;
			case PENTAGON:
			case HEXAGON:
			case STAR5:
			case STAR6:
				width = getTextWidth() * 9 / 7 + 8;
				height = width;
				break;
		}

		super.setSize(width, height);
		setSize((int) width + 2, (int) height + 2);
	}
	
	@Override
	public void paint(Graphics g, boolean showSelection) {
		super.paint(g, showSelection);

		var g2 = (Graphics2D) g.create();
		g2.setColor(textColor);
		g2.setFont(font);

		// Handle opacity
		int alpha = textColor.getAlpha();
		float opacity = (float) alpha / (float) 255;
		var originalComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		float halfWidth = (float) (getWidth() - getTextWidth()) / 2;
		float halfHeight = (float) (getHeight() - getTextHeight() / 2); // Note, this is + because we start at the baseline
		var currentTransform = g2.getTransform();

		
    float yValue = (float)(getY()+getHeight()/2-getTextHeight()/2);
		if (rotation != 0) {
			g2.rotate(Math.toRadians(rotation), (int) (getX() + getWidth() / 2), (int) (getY() + getHeight() / 2));
			g2.setClip(getBounds());
      // g2.drawString(text, (int) getX() + halfWidth, (int) getY() + halfHeight);
      for (String t: text) {
        LineMetrics metrics = font.getLineMetrics(t, new FontRenderContext(null, true, true));
        float ascent = metrics.getAscent();
        g2.drawString(t, (float) getX()+halfWidth, yValue + ascent);
        yValue += metrics.getHeight();
      }
			g2.setTransform(currentTransform);
		} else {
			g2.setClip(getBounds());
      for (String t: text) {
        LineMetrics metrics = font.getLineMetrics(t, new FontRenderContext(null, true, true));
        float ascent = metrics.getAscent();
        g2.drawString(t, (float) getX()+halfWidth, yValue + ascent);
        yValue += metrics.getHeight();
      }
		}
		g2.setComposite(originalComposite);
	}

	@Override
	public void setText(String text) {
    String[] lines = TextAnnotationImpl.splitString(text);
		if (!Objects.equals(lines, this.text)) {
			var oldValue = TextAnnotationImpl.joinString(this.text);
			this.text = lines;

			if (updateNameFromText)
				name = text != null ? this.text[0].trim() : "";

			if (shapeIsFit)
				fitShapeToText();

			updateBounds();
			update();
			firePropertyChange("text", oldValue, text);
		}
	}

	@Override
	public String getText() {
		return TextAnnotationImpl.joinString(text);
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
		setFontSize(size, true);
	}

	/**
	 * A method that can be used for group resizing.
	 */
	public void setFontSize(double size, boolean updateBounds) {
		if (fontSize != (float) size) {
			var oldValue = fontSize;
			fontSize = (float) size;

			if (font != null)
				font = font.deriveFont(fontSize);

			if (updateBounds)
				updateBounds();

			update();
			firePropertyChange("fontSize", oldValue, fontSize);
		}
	}
//
//	public void setFontSizeRelative(double factor) {
//		if (savedFontSize != 0.0) {
//			setFontSize(savedFontSize*factor, false);
//		} else {
//			setFontSize(fontSize*factor, false);
//		}
//	}

	@Override
	public double getFontSize() {
		return fontSize;
	}

//	@Override
//	public void saveBounds() {
//		super.saveBounds();
//		savedFontSize = fontSize;
//	}

	@Override
	public void setFontStyle(int style) {
		if (font == null || style != font.getStyle()) {
			var oldValue = font.getStyle();
			font = font.deriveFont(style, fontSize);
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
		return this.font;
	}

	@Override
	public void setFont(Font font) {
		if (!Objects.equals(font, this.font)) {
			var oldValue = this.font;
			this.font = font;
			this.fontSize = font.getSize2D();
			updateBounds();
			update();
			firePropertyChange("font", oldValue, font);
		}
	}

	@Override
	protected String getDefaultName() {
		return text != null ? text[0] : DEF_TEXT;
	}

	private void updateBounds() {
		if (shapeIsFit) {
			fitShapeToText();
			return;
		}
		
		// Our bounds should be the larger of the shape or the text
		double xBound = Math.max(getTextWidth() + HPAD,  width);
		double yBound = Math.max(getTextHeight() + VPAD, height);
		setSize(xBound, yBound);
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

	Font getArgFont(Map<String, String> argMap) {
		String family = argMap.get(FONTFAMILY);
		int size = Integer.parseInt(argMap.get(FONTSIZE));
		int style = Integer.parseInt(argMap.get(FONTSTYLE));
		return new Font(family, style, size);
	}
}
