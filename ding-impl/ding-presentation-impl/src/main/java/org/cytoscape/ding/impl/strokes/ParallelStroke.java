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

import java.awt.Shape;
import java.awt.geom.GeneralPath;

public class ParallelStroke extends ShapeStroke {

	public ParallelStroke(float width) {
		super(new Shape[] { getParallelStroke(width) }, 1f, width);
		this.width = width;
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new ParallelStroke(w);
	}

	private static Shape getParallelStroke(final float width) {
		GeneralPath shape = new GeneralPath();

		shape.moveTo(0f, -0.5f * width);
		shape.lineTo(1f, -0.5f * width);
		shape.lineTo(1f, -1f * width);
		shape.lineTo(0f, -1f * width);
		shape.lineTo(0f, -0.5f * width);

		shape.moveTo(0f, 0.5f * width);
		shape.lineTo(1f, 0.5f * width);
		shape.lineTo(1f, 1f * width);
		shape.lineTo(0f, 1f * width);
		shape.lineTo(0f, 0.5f * width);

		return shape;
	}
}
