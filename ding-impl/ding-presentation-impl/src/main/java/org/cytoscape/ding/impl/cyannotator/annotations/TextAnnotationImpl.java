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
import java.util.Objects;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.impl.strokes.EqualDashStroke;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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
	public TextAnnotationImpl(DRenderingEngine re, Map<String,String> argMap) {
		super(re, argMap);
		
		font = ViewUtils.getArgFont(argMap, "Arial", Font.PLAIN, initialFontSize);
		double zoom = getLegacyZoom(argMap);
		if(zoom != 1.0) {
			font = font.deriveFont(font.getSize2D() / (float)zoom);
		}
		
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
		argMap.put(TEXT, text);
		argMap.put(COLOR, ViewUtils.convertColor(textColor));
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
		if (!Objects.equals(text, this.text)) {
			this.text = text;
			
			if (updateNameFromText)
				name = text != null ? text.trim() : "";
			
			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());
			
			update();
		}
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setTextColor(Color color) {
		if (!Objects.equals(textColor, color)) {
			textColor = color;
			update();
		}
	}

	@Override
	public Color getTextColor() {
		return textColor;
	}

	@Override
	public void setFontSize(double size) {
		if (font == null || font.getSize() != (int) size) {
			fontSize = (float) size;
			font = font.deriveFont(fontSize);

			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());

			update();
		}
	}

	@Override
	public double getFontSize() {
		return this.fontSize;
	}

	@Override
	public void setFontStyle(int style) {
		if (font == null || style != font.getStyle()) {
			font = font.deriveFont(style, fontSize);
			
			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());
			
			update();
		}
	}

	@Override
	public int getFontStyle() {
		return font.getStyle();
	}

	@Override
	public void setFontFamily(String family) {
		if (font == null || (family != null && family.equalsIgnoreCase(font.getFamily()))) {
			font = new Font(family, font.getStyle(), (int) fontSize);
			
			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());
			
			update();
		}
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
		if (!Objects.equals(this.font, font)) {
			this.font = font;
			this.fontSize = font.getSize2D();
			
			if (!usedForPreviews)
				setSize(getAnnotationWidth(), getAnnotationHeight());
			
			update();
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

			this.fontSize = (float) fontSize;
			this.font = font.deriveFont((float) fontSize);
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
