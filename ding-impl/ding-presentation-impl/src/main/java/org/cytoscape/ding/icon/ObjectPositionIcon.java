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
import java.awt.RenderingHints;

import org.cytoscape.ding.ObjectPlacerGraphic;
import org.cytoscape.view.presentation.property.values.ObjectPosition;

public class ObjectPositionIcon extends VisualPropertyIcon<ObjectPosition> {
	
	private static final long serialVersionUID = 6852491198236306710L;
	
	private Graphics2D g2d;

	public ObjectPositionIcon(final ObjectPosition value, int width, int height, String name) {
		super(value, width, height, name);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.translate(x,  y);
		g2d.fillRect(0, 0, width, height);
		
		final ObjectPlacerGraphic lp = new ObjectPlacerGraphic(width, false, "LABEL");
		lp.setObjectPosition(value);
		lp.applyPosition();
		lp.paint(g2d);
		g2d.translate(-x,  -y);
	}
}
