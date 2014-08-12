package org.cytoscape.ding.customgraphics.vector;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractDCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsPropertyImpl;
import org.cytoscape.graph.render.stateful.PaintFactory;

/**
 * Proof of concept code to generate Custom Graphics dynamically as vector graphics.
 * 
 */
public abstract class GradientLayerCustomGraphics extends AbstractDCustomGraphics implements VectorCustomGraphics {

	// Paint fot this graphics
	protected PaintFactory paintFactory;
	
	// Shape of this paint
	protected Shape shape;

	// User-visible name of this graphic
	private final String NAME;
	
	private static final float FIT = 0.9f;
	
	protected static final String COLOR1 = "Color 1";
	protected static final String COLOR2 = "Color 2";
	
	protected final CustomGraphicsProperty<Color> c1;
	protected final CustomGraphicsProperty<Color> c2;

	protected List<Color> colorList;
	protected List<Float> stopList;
	
	// Pre-Rendered image for icon.
	protected BufferedImage rendered;
	
	private static final Color transparentWhite = new Color(255, 255, 255, 100);
	private static final Color transparentBlack = new Color(100, 100, 100, 100);
	
	private static final int DEF_W = 100;
	private static final int DEF_H = 100;
	
	protected final Map<String, CustomGraphicsProperty<?>> props;

	
	public GradientLayerCustomGraphics(final Long id, final String name) {
		super(id, name);
		NAME = name;
		width = DEF_W;
		height = DEF_H;
		props = new HashMap<String, CustomGraphicsProperty<?>>();
		colorList = new ArrayList<Color>();
		stopList = new ArrayList<Float>();

		c1 = new CustomGraphicsPropertyImpl<Color>(transparentWhite);
		colorList.add(c1.getValue()); stopList.add(0.0f);
		c2 = new CustomGraphicsPropertyImpl<Color>(transparentBlack);
		colorList.add(c2.getValue()); stopList.add(1.0f);
		
		this.props.put(COLOR1, c1);
		this.props.put(COLOR2, c2);
		this.tags.add("vector image, gradient");
		this.fitRatio = FIT;

		// Render it for static icons.
		getRenderedImage();
	}

	
	public Map<String, CustomGraphicsProperty<?>> getGraphicsProps() {
		return this.props;
	}
	
	protected void renderImage(Graphics graphics) {
		rendered.flush();
		final Graphics2D g2d = (Graphics2D) graphics;
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
				RenderingHints.VALUE_RENDER_QUALITY );
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON );
	}
	

	public Image getRenderedImage() {
		if(rendered == null || (rendered != null && (rendered.getWidth() != width || rendered.getHeight() != height))) {
			rendered = new BufferedImage(width, 
				height, BufferedImage.TYPE_INT_ARGB);
			renderImage(rendered.getGraphics());
		}
		
		return rendered;
	}

	public String toString() {
		return NAME;
	}
}
