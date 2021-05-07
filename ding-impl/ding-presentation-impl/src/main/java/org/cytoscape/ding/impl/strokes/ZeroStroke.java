package org.cytoscape.ding.impl.strokes;

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

import java.awt.BasicStroke;

/**
 * Rather than handle strokes of width 0 for all implementations of WidthStroke,
 * use this wrapper class that, when the width is less than or equal to 0 a
 * BasicStroke is returned, whereas when the width is greater than 0, return the
 * specified actual WidthStroke.
 */
public class ZeroStroke extends BasicStroke implements WidthStroke {

	final WidthStroke actualStroke;

	/**
	 * @param actualStroke The actual WidthStroke that this ZeroStroke represents. This
	 * object will be used whenever the width for a new instance is greater than 0.
	 */
	public ZeroStroke(WidthStroke actualStroke) {
		super(0);
		this.actualStroke = actualStroke;
	}

	public WidthStroke newInstanceForWidth(float w) {
		if ( w <= 0 )
			return new ZeroStroke(actualStroke);
		else
			return actualStroke.newInstanceForWidth(w);
	}

	
	//TODO: is this correct?
	@Override public String toString() { return actualStroke.getClass().getSimpleName() + " 0.0"; }
}


