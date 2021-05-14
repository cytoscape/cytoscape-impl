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

import org.cytoscape.view.presentation.property.LineTypeVisualProperty;

public class SolidStroke extends BasicStroke implements WidthStroke {

	private float width;

	public SolidStroke(float width) {
		super(width,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
		this.width = width;
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new SolidStroke(w);
	}

//	public LineStyle getLineStyle() {
//		return LineStyle.SOLID;
//	}

	@Override public String toString() { return LineTypeVisualProperty.SOLID.toString() + " " + Float.toString(width); }
}


