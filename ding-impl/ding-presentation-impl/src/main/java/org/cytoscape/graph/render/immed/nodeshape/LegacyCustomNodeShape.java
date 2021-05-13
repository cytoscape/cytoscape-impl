package org.cytoscape.graph.render.immed.nodeshape;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2021 The Cytoscape Consortium
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


import org.cytoscape.graph.render.immed.GraphGraphics;

import java.awt.geom.GeneralPath;
import java.awt.geom.AffineTransform;
import java.awt.Shape;


public class LegacyCustomNodeShape extends AbstractNodeShape {
	private final GeneralPath path; 
	private final AffineTransform xform; 
	private final double[] coords;
	private final double[] xformCoords;;

	public LegacyCustomNodeShape(final double[] coords, final byte type) {
		super(type);
		this.coords = coords;
		this.xformCoords = new double[coords.length];
		path = new GeneralPath(); 
		xform = new AffineTransform(); 
	}

	public float[] getCoords() {
		final float[] returnThis = new float[coords.length];

		for (int i = 0; i < returnThis.length; i++) 
			returnThis[i] = (float) coords[i];

		return returnThis;
	}
		
	public Shape getShape(float xMin, float yMin, float xMax, float yMax) {
		final double desiredXCenter = (xMin + xMax) / 2.0;
		final double desiredYCenter = (yMin + yMax) / 2.0;
		final double desiredWidth = xMax - xMin;
		final double desiredHeight = yMax - yMin;
		xform.setToTranslation(desiredXCenter, desiredYCenter);
		xform.scale(desiredWidth, desiredHeight);
		xform.transform(coords, 0, xformCoords, 0, coords.length/2);

		path.reset();

		path.moveTo((float) xformCoords[0], (float) xformCoords[1]);

		for (int i = 2; i < xformCoords.length;)
			path.lineTo((float) xformCoords[i++], (float) xformCoords[i++]);

		path.closePath();

		return path;
	}
}
