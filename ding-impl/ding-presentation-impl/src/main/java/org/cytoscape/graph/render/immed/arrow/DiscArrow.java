package org.cytoscape.graph.render.immed.arrow;

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

