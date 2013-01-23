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
import java.awt.geom.Ellipse2D;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.customgraphics.paint.RadialGradientPaintFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class GradientOvalLayer extends GradientLayerCustomGraphics {
	
	// Name of this custom graphics.
	private static final String NAME = "Oval Gradient";
	
	
	public GradientOvalLayer(final Long id) {
		super(id, NAME);
	}
	
	protected void renderImage(Graphics graphics) {
		super.renderImage(graphics);
		
		final Graphics2D g2d = (Graphics2D) graphics;
		// Render
		update();
		g2d.setPaint(paintFactory.getPaint(shape.getBounds2D()));
		g2d.fillOval(rendered.getMinX(), rendered.getMinY(), 
				width, height);
	}
	
	public void update() {
		// First, remove all layers.
		layers.clear();
		shape = new Ellipse2D.Double(-width / 2, -height / 2, width, height);
		paintFactory = new RadialGradientPaintFactory(colorList, stopList);
		final CustomGraphicLayer cg = new PaintCustomGraphic(shape, paintFactory);
		
		layers.add(cg);
	}

	@Override
	public String toSerializableString() {
		return makeSerializableString(displayName);
	}

}
