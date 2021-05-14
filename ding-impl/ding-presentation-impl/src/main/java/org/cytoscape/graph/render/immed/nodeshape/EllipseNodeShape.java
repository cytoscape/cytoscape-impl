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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;


public class EllipseNodeShape extends AbstractNodeShape {
	private final Ellipse2D.Float ellipse;

	public EllipseNodeShape() {
		super(GraphGraphics.SHAPE_ELLIPSE);
		ellipse = new Ellipse2D.Float(0.0f,0.0f,1.0f,1.0f);	
	}
		
	public Shape getShape(float xMin, float yMin, float xMax, float yMax) {
		ellipse.setFrame(xMin, yMin, xMax - xMin, yMax - yMin);
		return ellipse;
	}

	public boolean computeEdgeIntersection(final float xMin, final float yMin, final float xMax,
	                                       final float yMax, final float ptX, final float ptY, 
	                                       final float[] returnVal)
	{
			final double centerX = ((double)xMax + (double)xMin)/2.0;
			final double centerY = ((double)yMax + (double)yMin)/2.0;

            if ((centerX == ptX) && (centerY == ptY)) 
                return false;

            final double ptPrimeX = ptX - centerX;
            final double ptPrimeY = ptY - centerY;
            final double ellpW = ((double) xMax) - xMin;
            final double ellpH = ((double) yMax) - yMin;
            final double xScaleFactor = 2.0 / ellpW;
            final double yScaleFactor = 2.0 / ellpH;
            final double xformedPtPrimeX = ptPrimeX * xScaleFactor;            
			final double xformedPtPrimeY = ptPrimeY * yScaleFactor;
            final double xformedDist = Math.sqrt((xformedPtPrimeX * xformedPtPrimeX) + (xformedPtPrimeY * xformedPtPrimeY));
            final double xsectXformedPtPrimeX = xformedPtPrimeX / xformedDist;
            final double xsectXformedPtPrimeY = xformedPtPrimeY / xformedDist;
            final double xsectPtPrimeX = xsectXformedPtPrimeX / xScaleFactor;
            final double xsectPtPrimeY = xsectXformedPtPrimeY / yScaleFactor;
            returnVal[0] = (float) (xsectPtPrimeX + centerX);
            returnVal[1] = (float) (xsectPtPrimeY + centerY);

            return true;
	}
}

