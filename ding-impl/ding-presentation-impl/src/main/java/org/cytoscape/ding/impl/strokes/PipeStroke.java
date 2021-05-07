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

public class PipeStroke extends ShapeStroke {

	public enum Type {
		VERTICAL(0f), FORWARD(-1f), BACKWARD(1f), ;

		private float adjust;

		private Type(float adjust) {
			this.adjust = adjust;
		}

		public float adjust(float input) {
			return adjust * input;
		}
	}

	private Type offsetType;

	PipeStroke(float width, Type offsetType) {
		super(new Shape[] { getShape(width, offsetType) }, width,
				width);
		this.offsetType = offsetType;
		
		//TODO: is this necessary?
		//this.lineStyle = lineStyle;
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new PipeStroke(w, offsetType);
	}

	private static Shape getShape(final float input, final Type offsetType) {
		GeneralPath shape = new GeneralPath();
		float height = input;
		float width = input / 5f;
		float offset = offsetType.adjust(input);

		shape.moveTo(0f, -height);
		shape.lineTo(width, -height);
		shape.lineTo(width + offset, height);
		shape.lineTo(0f + offset, height);
		shape.lineTo(0f, -height);

		return shape;
	}
}
