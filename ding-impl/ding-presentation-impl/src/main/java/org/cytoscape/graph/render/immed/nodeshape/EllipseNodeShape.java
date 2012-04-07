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

