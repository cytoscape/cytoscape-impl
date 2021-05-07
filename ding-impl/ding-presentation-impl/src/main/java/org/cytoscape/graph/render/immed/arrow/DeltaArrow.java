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


import java.awt.geom.GeneralPath;


public class DeltaArrow extends AbstractArrow {
	public DeltaArrow() {
		super(2.0);

		// create the arrow
		final GeneralPath arrowGP = new GeneralPath();
		arrowGP.moveTo(-2.0f, -0.5f);
		arrowGP.lineTo(0.0f, 0.0f);
		arrowGP.lineTo(-2.0f, 0.5f);
		arrowGP.closePath();

		arrow = arrowGP;

		// no  cap
	}
}
