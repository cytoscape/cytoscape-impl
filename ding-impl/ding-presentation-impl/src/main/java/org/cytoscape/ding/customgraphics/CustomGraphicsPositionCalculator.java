package org.cytoscape.ding.customgraphics;

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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.Position;
import org.cytoscape.ding.impl.DNodeView;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.graph.render.stateful.PaintFactory;

public class CustomGraphicsPositionCalculator {
	
	/**
	 * Defines displacement.
	 */
	private static final Map<Position, Float[]> DISPLACEMENT_MAP;
	
	static {
		DISPLACEMENT_MAP = new HashMap<Position, Float[]>();
		
		DISPLACEMENT_MAP.put(Position.CENTER, new Float[]{0f, 0f} );
		
		DISPLACEMENT_MAP.put(Position.NORTH,  new Float[]{0f, -0.5f});
		DISPLACEMENT_MAP.put(Position.NORTH_WEST, new Float[]{-0.5f, -0.5f});
		DISPLACEMENT_MAP.put(Position.NORTH_EAST, new Float[]{0.5f, -0.5f});
		
		DISPLACEMENT_MAP.put(Position.SOUTH,  new Float[]{0f, 0.5f});
		DISPLACEMENT_MAP.put(Position.SOUTH_WEST,  new Float[]{-0.5f, 0.5f});
		DISPLACEMENT_MAP.put(Position.SOUTH_EAST,  new Float[]{0.5f, 0.5f});
		
		DISPLACEMENT_MAP.put(Position.WEST,  new Float[]{-0.5f, 0f});
		
		DISPLACEMENT_MAP.put(Position.EAST,  new Float[]{0.5f, 0f});
	}
	
	
	/**
	 * Creates new custom graphics in new location
	 * 
	 * @param p
	 * @param nv
	 * @param cg
	 * @return
	 */
	public static CustomGraphicLayer transform(final ObjectPosition p, final DNodeView nv, final CustomGraphicLayer cg) {		
		final Position anc = p.getAnchor();
		final Position ancN = p.getTargetAnchor();
		
		final double nodeW = nv.getWidth();
		final double nodeH = nv.getHeight();
		final double cgW = cg.getBounds2D().getWidth();
		final double cgH = cg.getBounds2D().getHeight();
		
		final Float[] disp1 = DISPLACEMENT_MAP.get(anc);
		final Float[] disp2 = DISPLACEMENT_MAP.get(ancN);
		
		// 1. Displacement for graphics
		final double dispX = -disp1[0] * nodeW;
		final double dispY = -disp1[1] * nodeH;
		
		final double dispNX = disp2[0] * cgW;
		final double dispNY = disp2[1] * cgH;
		
		// calc total and apply transform
		double totalDispX = dispX + dispNX + p.getOffsetX();
		double totalDispY = dispY + dispNY + p.getOffsetY();
		
		final AffineTransform tf = AffineTransform.getTranslateInstance(totalDispX, totalDispY);

		return cg.transform(tf);
	}
}
