package org.cytoscape.ding.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.customgraphics.image.SVGLayer;
import org.cytoscape.ding.customgraphics.image.URLVectorCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

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

@SuppressWarnings("serial")
public class CustomGraphicsIcon extends VisualPropertyIcon<CyCustomGraphics<?>> {

	private List<? extends Cy2DGraphicLayer> cy2DLayers;
	
	public CustomGraphicsIcon(CyCustomGraphics<?> value, int width, int height, String name) {
		super(value, width, height, name);
		var img = value.getRenderedImage();
		
		if (img != null)
			setImage(img);
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (width <= 0 || height <= 0)
			return;
		
		var g2 = (Graphics2D) g.create();
		var cg = getValue();
		
		if (cg instanceof URLVectorCustomGraphics) {
			if (cy2DLayers == null)
				cy2DLayers = ((URLVectorCustomGraphics) cg).getLayers(null, null);
			
			var rect = new Rectangle2D.Float(x + width / 2.0f, y + height / 2.0f, width, height);
			
			for (var cgl : cy2DLayers) {
				// Much easier to use the SVGLayer draw method than have calculate and apply
				// the same scale factor and translation transform already done by the layer!
				if (cgl instanceof SVGLayer)
					((SVGLayer) cgl).draw(g2, rect, rect, null, null);
			}
		} else {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			var img = getImage();
			
			if (img != null) {
				img = img.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
				g2.drawImage(img, x, y, width, height, c);
			}
		}
		
		g2.dispose();
	}
}
