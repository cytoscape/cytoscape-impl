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
package org.cytoscape.graph.render.immed.arrow;


import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;


public class DiscArrow extends AbstractArrow {
	
	private final GeneralPath capGP; 
	private final Arc2D.Double capA;

	private double currentRatio;

	public DiscArrow() {
		super(1.0);

		// create the arrow
		final Ellipse2D.Double arrowE = new Ellipse2D.Double();
		arrowE.setFrame(-1.0, -0.5, 1.0d, 1.0d);

		arrow = arrowE;

		// cap is calculated dynamically below!
		capGP = new GeneralPath();
		capA = new Arc2D.Double();

		currentRatio = Double.NaN;
	}

	public synchronized Shape getCapShape(final double ratio) {
		// only recreate the shape if we need to
		if ( currentRatio != Double.NaN && ratio == currentRatio )
			return capGP;

		currentRatio = ratio;
		final double theta = Math.toDegrees(Math.asin(1.0d / ratio));

		capA.setArc(0.0d, ratio / -2.0d, ratio, ratio, 180.0d - theta, theta * 2, Arc2D.OPEN);

		capGP.reset();
		capGP.append(capA, false);
		capGP.lineTo(0.0f, 0.5f);
		capGP.lineTo(0.0f, -0.5f);
		capGP.closePath();

		return capGP;
	}
}

