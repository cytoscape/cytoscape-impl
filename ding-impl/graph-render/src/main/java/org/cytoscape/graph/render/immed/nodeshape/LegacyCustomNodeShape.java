/*
 Copyright (c) 2009, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.graph.render.immed.nodeshape;


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
