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

public class SeparateArrowStroke extends ShapeStroke {

	public SeparateArrowStroke(float width) {
		super(new Shape[] { getArrowStroke(width) }, 5f * width, width);
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new SeparateArrowStroke(w);
	}

	private static Shape getArrowStroke(final float width) {
		GeneralPath shape = new GeneralPath();

		// change these to change the arrow proportions

		// length of shape
		float length = 4f * width;

		// width of center line
		float lineWidth = width;

		// width of arrow, from one arm to its opposite
		float arrowWidth = 3f * width;

		// fraction of the length of the total shape
		// where the arrow head should begin
		float arrowHeadStart = length / 2f;

		// ====================================================================
		// don't change these - they should always stay
		// the same for this shape!
		float begin = 0f;
		float halfLineWidth = 0.5f * lineWidth;
		float halfArrowWidth = 0.5f * arrowWidth;

		// make the actual shape
		// X Y
		shape.moveTo(begin, halfLineWidth);
		shape.lineTo(arrowHeadStart, halfLineWidth);
		shape.lineTo(arrowHeadStart, halfArrowWidth);
		shape.lineTo(length, 0f);
		shape.lineTo(arrowHeadStart, -halfArrowWidth);
		shape.lineTo(arrowHeadStart, -halfLineWidth);
		shape.lineTo(begin, -halfLineWidth);
		shape.lineTo(begin, halfLineWidth);

		return shape;
	}
}
