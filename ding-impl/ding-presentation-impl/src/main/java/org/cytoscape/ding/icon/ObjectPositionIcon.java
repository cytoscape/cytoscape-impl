package org.cytoscape.ding.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.cytoscape.ding.ObjectPlacerGraphic;
import org.cytoscape.view.presentation.property.ObjectPositionVisualProperty;
import org.cytoscape.view.presentation.property.values.ObjectPosition;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
public class ObjectPositionIcon extends VisualPropertyIcon<ObjectPosition> {
	
	private final ObjectPlacerGraphic lp;

	public ObjectPositionIcon(
			ObjectPosition op,
			ObjectPositionVisualProperty vp, // may be null
			int width,
			int height,
			String name
	) {
		super(op, width, height, name);
		this.lp = new ObjectPlacerGraphic(op, vp, width, height, false);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		var g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.translate(x, y);
		g2.fillRect(0, 0, width, height);
		
		lp.applyPosition();
		lp.paint(g2);
		g2.translate(-x, -y);
	}
}
