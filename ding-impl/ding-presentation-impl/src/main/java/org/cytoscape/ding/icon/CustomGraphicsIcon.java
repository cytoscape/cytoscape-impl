package org.cytoscape.ding.icon;

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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

public class CustomGraphicsIcon extends VisualPropertyIcon<CyCustomGraphics<?>> {

	private static final long serialVersionUID = -216647303312376087L;
	
	
	public CustomGraphicsIcon(final CyCustomGraphics<?> value, int width, int height, String name) {
		super(value, width, height, name);
		this.setImage(value.getRenderedImage());
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2d = (Graphics2D) g;

		// AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Image img = this.getImage();
		img = img.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		
		g2d.drawImage(img, x, y, width, height, c);
	}
}
