package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Window;
import java.awt.font.FontRenderContext;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.dialogs.BoundedTextAnnotationDialog;
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

@SuppressWarnings("serial")
public class BoundedTextAnnotationImpl extends ShapeAnnotationImpl 
                                       implements BoundedTextAnnotation, TextAnnotation {
	private String text;
	private boolean shapeIsFit = false;

	protected float fontSize = 0.0f;
	protected Font font = null;
	protected int initialFontSize=12;
	protected Color textColor = Color.BLACK;

	private static int instanceCount = 0;
	
	public BoundedTextAnnotationImpl(DGraphView view, Window owner) { 
		super(view, 100, 100, owner);
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
		this.text = "Text Annotation";
		Graphics2D graphics = (Graphics2D)this.getGraphics();
		super.setSize(getTextWidth(graphics)+4, getTextHeight(graphics)+4);
		super.name = "BoundedTextAnnotation_"+instanceCount;
		instanceCount++;
	}

	public BoundedTextAnnotationImpl(DGraphView view, double width, double height,
			Window owner) { 
		super(view, width, height, owner);
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
		this.text = "Text Annotation";
		super.name = "BoundedTextAnnotation_"+instanceCount;
		instanceCount++;
	}

	public BoundedTextAnnotationImpl(BoundedTextAnnotationImpl c, Window owner) { 
		super(c, 100, 100, owner);
		this.text = c.getText();
		this.textColor = c.getTextColor();
		this.fontSize = (float)c.getFontSize();
		this.font = c.getFont();
		super.name = c.getName();
	}

	public BoundedTextAnnotationImpl(DGraphView view,
                                   double x, double y, ShapeType shapeType,
                                   double width, double height,
                                   Paint fillColor, Paint edgeColor,
                                   float edgeThickness, String text, int compCount, double zoom,
                                   Window owner) {
		super(view, x, y, shapeType, width, height, fillColor, edgeColor, edgeThickness, owner);
		this.text=text;
		this.font=new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float)initialFontSize;
		super.name = "BoundedTextAnnotation_"+instanceCount;
		instanceCount++;
	}

	public BoundedTextAnnotationImpl(DGraphView view, Map<String, String> argMap, Window owner) {
		super(view, argMap, owner);
		this.font = ViewUtils.getArgFont(argMap, "Arial", Font.PLAIN, initialFontSize);
		this.textColor = (Color)ViewUtils.getColor(argMap, COLOR, Color.BLACK);
		this.text = ViewUtils.getString(argMap, TEXT, "");
		this.fontSize = font.getSize();

		if (!argMap.containsKey(BoundedTextAnnotation.WIDTH)) {
			Graphics2D graphics = (Graphics2D)this.getGraphics();
			double width = getTextWidth(graphics)+8;
			double height = getTextHeight(graphics)+8;
			super.setSize(width, height);
		}
		super.name = "BoundedTextAnnotation_"+instanceCount;
		instanceCount++;
	}

	@Override
	public Class<? extends Annotation> getType() {
		return BoundedTextAnnotation.class;
	}
	
	@Override
	public Map<String, String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
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
		Graphics2D graphics = (Graphics2D)this.getGraphics();
		double width = getTextWidth(graphics)+8;
		double height = getTextHeight(graphics)+8;
		shapeIsFit = true;

		// System.out.println("Fitting shape to text: "+width+"x"+height);

		// Different depending on the type...
		ShapeType shapeType = getShapeTypeInt();
		switch (shapeType) {
		case ELLIPSE:
			width = getTextWidth(graphics)*3/2+8;
			height = getTextHeight(graphics)*2;
			break;
		case TRIANGLE:
			width = getTextWidth(graphics)*3/2+8;
			height = getTextHeight(graphics)*2;
			break;
		case PENTAGON:
		case HEXAGON:
		case STAR5:
		case STAR6:
			width = getTextWidth(graphics)*9/7+8;
			height = width;
			break;
		}

		super.setSize(width, height);
		setSize((int)width+2, (int)height+2);
	}
	
	@Override
	public JDialog getModifyDialog() {
			return new BoundedTextAnnotationDialog(this, owner);
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);

		if (text == null || textColor == null || font == null) return;

		// For now, we put the text in the middle of the shape.  At some point, we may
		// want to add other options
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(textColor);

		int width = (int)((double)getWidth()*scaleFactor/getZoom());
		int height = (int)((double)getHeight()*scaleFactor/getZoom());
		Font tFont = font.deriveFont(((float)(scaleFactor/getZoom()))*font.getSize2D());
		FontMetrics fontMetrics=g.getFontMetrics(tFont);

		int halfWidth = (width-(int)(fontMetrics.stringWidth(text)))/2;

		// Note, this is + because we start at the baseline
		// int halfHeight = ((int)(getHeight()*scaleFactor)+fontMetrics.getHeight()/2)/2;
		int halfHeight = (height+fontMetrics.getHeight()/2)/2;

		int xLoc = (int)(x*scaleFactor) + halfWidth;
		int yLoc = (int)(y*scaleFactor) + halfHeight;

		g2.setFont(tFont);

		// Handle opacity
		int alpha = textColor.getAlpha();
		float opacity = (float)alpha/(float)255;
		final Composite originalComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		g2.drawString(text, xLoc, yLoc);
		g2.setComposite(originalComposite);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2=(Graphics2D)g;
		g2.setColor(textColor);
		g2.setFont(font);

		// Handle opacity
		int alpha = textColor.getAlpha();
		float opacity = (float)alpha/(float)255;
		final Composite originalComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		int halfWidth = (int)(getWidth()-getTextWidth(g2))/2;
		int halfHeight = (int)(getHeight()+getTextHeight(g2)/2)/2; // Note, this is + because we start at the baseline

		if(usedForPreviews) {
			g2.drawString(text, halfWidth, halfHeight);
			g2.setComposite(originalComposite);
			return;
		}

		g2.drawString(text, halfWidth, halfHeight);
		g2.setComposite(originalComposite);
	}

	@Override
	public void setSpecificZoom(double zoom) {
		if (zoom == getSpecificZoom())
			return;
		
		fontSize = (float)((zoom/getSpecificZoom())*fontSize);
		font=font.deriveFont(fontSize);
		super.setSpecificZoom(zoom);		
	}

	@Override
	public void setZoom(double zoom) {
		if (zoom == getZoom())
			return;
		
		fontSize = (float)((zoom/getZoom())*fontSize);
		font=font.deriveFont(fontSize);
		super.setZoom(zoom);
	}

	@Override
	public void setText(String text) {
		this.text = text;
		if (shapeIsFit)
			fitShapeToText();

		updateBounds();
		update();
	}

	@Override
	public String getText() { return this.text; }


	@Override
	public void setTextColor(Color color) {
		this.textColor = color;
		update();
	}

	@Override
	public Color getTextColor() { return textColor; }

	@Override
	public void setFontSize(double size) {
		setFontSize(size, true);
		update();
	}

	// A method that can be used for group resizing
	public void setFontSize(double size, boolean updateBounds) {
		this.fontSize = (float)size;
		font = font.deriveFont((float)(fontSize));
		if (updateBounds)
			updateBounds();
		update();
	}

	@Override
	public double getFontSize() { return this.fontSize; }


	@Override
	public void setFontStyle(int style) {
		font = font.deriveFont(style, (float)(fontSize));
		update();
	}

	@Override
	public int getFontStyle() {
		return font.getStyle();
	}

	@Override
	public void setFontFamily(String family) {
		font = new Font(family, font.getStyle(), (int)fontSize);
		update();
	}

	@Override
	public String getFontFamily() {
		return font.getFamily();
	}

	@Override
	public Font getFont() { return this.font; }

	@Override
	public void setFont(Font font) { 
		this.font = font; 
		this.fontSize = font.getSize2D();
		updateBounds();
		update();
	}

	private void updateBounds() {
		if (shapeIsFit) {
			fitShapeToText();
			return;
		}
		// Our bounds should be the larger of the shape or the text
		double xBound = Math.max(getTextWidth((Graphics2D)this.getGraphics()), shapeWidth);
		double yBound = Math.max(getTextHeight((Graphics2D)this.getGraphics()), shapeHeight);
		setSize(xBound+4, yBound+4);
	}

	double getTextWidth(Graphics2D g2) {
		return font.getStringBounds(text, new FontRenderContext(null, true, true)).getWidth();
		/*
		if (g2 != null) {
			FontMetrics fontMetrics=g2.getFontMetrics(font);
			return fontMetrics.stringWidth(text);
		}
		// If we don't have a graphics context, yet, make some assumptions
		return (int)(text.length()*fontSize);
		*/
	}

	double getTextHeight(Graphics2D g2) {
		return font.getStringBounds(text, new FontRenderContext(null, true, true)).getHeight();
		/*
		if (g2 != null) {
			FontMetrics fontMetrics=g2.getFontMetrics(font);
			return fontMetrics.getHeight();
		}
		// If we don't have a graphics context, yet, make some assumptions
		return (int)(fontSize*1.5);
		*/
	}

	Font getArgFont(Map<String, String> argMap) {
		String family = argMap.get(FONTFAMILY);
		int size = Integer.parseInt(argMap.get(FONTSIZE));
		int style = Integer.parseInt(argMap.get(FONTSTYLE));
		return new Font(family, style, size);
	}

}
