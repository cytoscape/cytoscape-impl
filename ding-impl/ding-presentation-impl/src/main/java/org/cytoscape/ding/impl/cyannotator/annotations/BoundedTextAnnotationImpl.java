package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.dialogs.BoundedTextAnnotationDialog;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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
	
	private String text;
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
		this.text = DEF_TEXT;
		super.setSize(getTextWidth() + 4, getTextHeight() + 4);
	}

	public BoundedTextAnnotationImpl(DRenderingEngine re, double width, double height) {
		super(re, width, height, false);
		this.font = new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float) initialFontSize;
		this.text = DEF_TEXT;
	}

	public BoundedTextAnnotationImpl(BoundedTextAnnotationImpl c) {
		super(c, 100, 100, false);
		this.text = c.getText();
		this.textColor = c.getTextColor();
		this.fontSize = (float) c.getFontSize();
		this.font = c.getFont();
	}

	public BoundedTextAnnotationImpl(
			DRenderingEngine re,
			double x,
			double y,
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
		super(re, x, y, shapeType, width, height, fillColor, edgeColor, edgeThickness);
		this.text = text;
		this.font = new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float) initialFontSize;
	}

	public BoundedTextAnnotationImpl(DRenderingEngine re, Map<String,String> argMap) {
		super(re, argMap);
		
		this.font = ViewUtils.getArgFont(argMap, "Arial", Font.PLAIN, initialFontSize);
		double zoom = getLegacyZoom(argMap);
		if(zoom != 1.0) {
			font = font.deriveFont(font.getSize2D() / (float)zoom);
		}
		
		this.textColor = (Color) ViewUtils.getColor(argMap, COLOR, Color.BLACK);
		this.text = ViewUtils.getString(argMap, TEXT, "");
		this.fontSize = font.getSize();

		if (text != null && !text.trim().isEmpty())
			name = text.trim();
		
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
		argMap.put(TEXT, this.text);
		argMap.put(COLOR, ViewUtils.convertColor((Paint) this.textColor));
		argMap.put(FONTFAMILY, this.font.getFamily());
		argMap.put(FONTSIZE, Integer.toString(this.font.getSize()));
		argMap.put(FONTSTYLE, Integer.toString(this.font.getStyle()));
		return argMap;
	}

	@Override
	public void fitShapeToText() {
		double width = getTextWidth()+8;
		double height = getTextHeight()+8;
		shapeIsFit = true;

		// Different depending on the type...
		ShapeType shapeType = getShapeTypeInt();
		switch (shapeType) {
		case ELLIPSE:
			width = getTextWidth()*3/2+8;
			height = getTextHeight()*2;
			break;
		case TRIANGLE:
			width = getTextWidth()*3/2+8;
			height = getTextHeight()*2;
			break;
		case PENTAGON:
		case HEXAGON:
		case STAR5:
		case STAR6:
			width = getTextWidth()*9/7+8;
			height = width;
			break;
		}

		super.setSize(width, height);
		setSize((int)width+2, (int)height+2);
	}
	
	@Override
	public JDialog getModifyDialog() {
		return new BoundedTextAnnotationDialog(this, ViewUtil.getActiveWindow(re));
	}
	
	@Override
	public void paint(Graphics graphics, boolean showSelection) {
		super.paint(graphics, showSelection);

		Graphics2D g = (Graphics2D)graphics.create();
		g.setColor(textColor);
		g.setFont(font);
		g.setClip(getBounds());

		// Handle opacity
		int alpha = textColor.getAlpha();
		float opacity = (float)alpha/(float)255;
		final Composite originalComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		int halfWidth  = (int)(getWidth() - getTextWidth())/2;
		int halfHeight = (int)(getHeight() + getTextHeight()/2)/2; // Note, this is + because we start at the baseline

		g.drawString(text, (int)getX() + halfWidth, (int)getY() + halfHeight);
		g.setComposite(originalComposite);
	}


	@Override
	public void setText(String text) {
		this.text = text;

		if (updateNameFromText)
			name = text != null ? text.trim() : "";
		
		if (shapeIsFit)
			fitShapeToText();

		updateBounds();
		update();
	}

	@Override
	public String getText() {
		return this.text;
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
		setFontSize(size, true);
		update();
	}

	// A method that can be used for group resizing
	public void setFontSize(double size, boolean updateBounds) {
		this.fontSize = (float) size;
		font = font.deriveFont((float) (fontSize));
		if (updateBounds)
			updateBounds();
		update();
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
		return this.fontSize;
	}

//	@Override
//	public void saveBounds() {
//		super.saveBounds();
//		savedFontSize = fontSize;
//	}

	@Override
	public void setFontStyle(int style) {
		font = font.deriveFont(style, (float) (fontSize));
		update();
	}

	@Override
	public int getFontStyle() {
		return font.getStyle();
	}

	@Override
	public void setFontFamily(String family) {
		font = new Font(family, font.getStyle(), (int) fontSize);
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
		updateBounds();
		update();
	}

	@Override
	protected String getDefaultName() {
		return text != null ? text : DEF_TEXT;
	}

	private void updateBounds() {
		if (shapeIsFit) {
			fitShapeToText();
			return;
		}
		// Our bounds should be the larger of the shape or the text
		double xBound = Math.max(getTextWidth(),  width);
		double yBound = Math.max(getTextHeight(), height);
		setSize(xBound + 4, yBound + 4);
	}

	double getTextWidth() {
		return font.getStringBounds(text, new FontRenderContext(null, true, true)).getWidth();
	}

	double getTextHeight() {
		return font.getStringBounds(text, new FontRenderContext(null, true, true)).getHeight();
	}

	Font getArgFont(Map<String, String> argMap) {
		String family = argMap.get(FONTFAMILY);
		int size = Integer.parseInt(argMap.get(FONTSIZE));
		int style = Integer.parseInt(argMap.get(FONTSTYLE));
		return new Font(family, style, size);
	}
}
