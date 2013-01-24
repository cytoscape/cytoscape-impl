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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import org.cytoscape.ding.customgraphics.paint.LinearGradientPaintFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class GradientRoundRectangleLayer extends GradientLayerCustomGraphics {
	
	// Name of this custom graphics.
	private static final String NAME = "Round Rectangle Gradient";
	private int r =20;
	
	public GradientRoundRectangleLayer(final Long id) {
		super(id, NAME);
	}
	
	protected void renderImage(Graphics graphics) {
		super.renderImage(graphics);
		
		final Graphics2D g2d = (Graphics2D) graphics;
		// Render
		update();
		g2d.setPaint(paintFactory.getPaint(shape.getBounds2D()));
		g2d.fillRoundRect(rendered.getMinX(), rendered.getMinY(), 
				rendered.getWidth(), rendered.getHeight(), r, r);
	}
	
	
	public void update() {
		// First, remove all layers.
		layers.clear();
		
		r = (int)(Math.min(width, height)/4f);
		shape = new RoundRectangle2D.Double(-width / 2, -height / 2,
																	width, height, r, r);
		paintFactory = new LinearGradientPaintFactory(colorList, stopList);
		final PaintCustomGraphic cg = new PaintCustomGraphic(shape, paintFactory);
		layers.add(cg);
	}

	@Override
	public String toSerializableString() {
		return makeSerializableString(displayName);
	}

}
