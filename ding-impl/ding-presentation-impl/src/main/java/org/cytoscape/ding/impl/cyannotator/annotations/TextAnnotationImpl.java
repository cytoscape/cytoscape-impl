package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.dialogs.TextAnnotationDialog;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
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

	public TextAnnotationImpl(DGraphView view, boolean usedForPreviews) { 
		super(view, usedForPreviews); 
		
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
			DGraphView view,
			int x,
			int y,
			String text,
			int compCount,
			double zoom
	) {
		super(view, x, y, zoom);

		this.text = text;
		this.font = new Font("Arial", Font.PLAIN, initialFontSize);
		this.fontSize = (float) initialFontSize;
		setSize(getAnnotationWidth(), getAnnotationHeight());
	}

	// This constructor is used to construct a text annotation from an
	// argument map.
	// Need to make sure all arguments have reasonable options
	public TextAnnotationImpl(DGraphView view, Map<String, String> argMap) {
		super(view, argMap);
		
		font = ViewUtils.getArgFont(argMap, "Arial", Font.PLAIN, initialFontSize);
		textColor = (Color) ViewUtils.getColor(argMap, COLOR, Color.BLACK);
		text = ViewUtils.getString(argMap, TEXT, "");
		fontSize = font.getSize();
		
		if (name == null && text != null && !text.trim().isEmpty())
			name = text.trim();
		
		setSize(getAnnotationWidth(), getAnnotationHeight());
	}

	@Override
	public Map<String, String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
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
		this.fontSize = (float) size;
		font = font.deriveFont((float) (fontSize));
		if (!usedForPreviews)
			setSize(getAnnotationWidth(), getAnnotationHeight());
		update();
	}

	public void setFontSizeRelative(double factor) {
		if (savedFontSize != 0.0) {
			setFontSize(savedFontSize*factor);
		} else {
			setFontSize(fontSize*factor);
		}
	}

	@Override
	public double getFontSize() {
		return this.fontSize;
	}

	@Override
	public void saveBounds() {
		super.saveBounds();
		savedFontSize = fontSize;
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
		return new TextAnnotationDialog(this, ViewUtil.getActiveWindow(view));
	}

	
	@Override
	public void resizeAnnotationRelative(Rectangle2D initialBounds, Rectangle2D outlineBounds) {
		super.resizeAnnotationRelative(initialBounds, outlineBounds);
		// XXX This doesn't work!  Need to preserve font size in order for this to work right
		double deltaW = outlineBounds.getWidth()/initialBounds.getWidth();
		setFontSizeRelative(deltaW);
	}
	
	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		if (text == null) return;
		super.drawAnnotation(g, x, y, scaleFactor);

		Graphics2D g2 = (Graphics2D) g;
		// System.out.println("drawAnnotation: setting text color to: "+textColor);
		g2.setPaint(textColor);
		// Font tFont = font.deriveFont(((float)(scaleFactor/getZoom()))*font.getSize2D());
		Font tFont = font.deriveFont(((float)(scaleFactor/getZoom()))*font.getSize2D());
		FontMetrics fontMetrics=g.getFontMetrics(tFont);

		int width = (int)((double)getWidth()*scaleFactor/getZoom());
		int halfWidth = (width-fontMetrics.stringWidth(text))/2;

		// Note, this is + because we start at the baseline
		int height = (int)((double)getHeight()*scaleFactor/getZoom());
		int halfHeight = (height+fontMetrics.getHeight()/2)/2;

		int xLoc = (int)(x*scaleFactor + halfWidth);
		int yLoc = (int)(y*scaleFactor + halfHeight);

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
	public Rectangle getBounds() {
		return new Rectangle(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
	}

	@Override
	public void paint(Graphics g) {
		if (text == null) return;
		super.paint(g);

		if (text == null || textColor == null || font == null) return;

		Graphics2D g2=(Graphics2D)g;

		g2.setPaint(textColor);
		g2.setFont(font);

		// Handle opacity
		int alpha = textColor.getAlpha();
		float opacity = (float)alpha/(float)255;
		final Composite originalComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		int halfWidth = (int)((double)getWidth()-getTextWidth(g2))/2;
		int halfHeight = (int)((double)getHeight()+getTextHeight(g2)/2.0)/2; // Note, this is + because we start at the baseline

		if(usedForPreviews) {
			g2.drawString(text, halfWidth, halfHeight);
			return;
		}

		g2.drawString(text, halfWidth, halfHeight);

		if (isSelected()) {
      //Selected Annotations will have a yellow border
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2.0f));
			// g2.drawRect(getX()-4, getY()-4, getTextWidth(g2)+8, getTextHeight(g2)+8);
			g2.drawRect(0, 0, getAnnotationWidth(), getAnnotationHeight());
		}
		g2.setComposite(originalComposite);
	}

	@Override
	public void print(Graphics g) {
		boolean saveSelected = isSelected();
		selected = false;
		paint(g);
		selected = saveSelected;
	}
	
	@Override
	protected String getDefaultName() {
		return text != null ? text : DEF_TEXT;
	}

	int getAnnotationWidth() {
		return (int) (getTextWidth((Graphics2D) this.getGraphics()) + 1.0);
	}

	int getAnnotationHeight() {
		return (int)(getTextHeight((Graphics2D)this.getGraphics())+1.0);
	}

	double getTextWidth(Graphics2D g2) {
		if (text == null) return 0.0;
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
		if (text == null) return 0.0;
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
}
