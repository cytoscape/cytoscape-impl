package org.cytoscape.ding.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import org.cytoscape.ding.customgraphics.image.URLVectorCustomGraphics;
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

	public CustomGraphicsIcon(CyCustomGraphics<?> value, int width, int height, String name) {
		super(value, width, height, name);
		var img = value.getRenderedImage();
		
		if (img != null)
			setImage(img);
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		var g2 = (Graphics2D) g.create();
		var cg = getValue();
		
		if (cg instanceof URLVectorCustomGraphics) {
			((URLVectorCustomGraphics) cg).draw(g2, new Rectangle(x, y, width, height));
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
