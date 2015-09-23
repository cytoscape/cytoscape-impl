package org.cytoscape.ding.internal.util;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Color;

/**
 * A control point defining a gradient
 */
public class ControlPoint {
	
	/** The color at this control point */
	private Color color;
	/** The position of this control point (0.0 -> 1.0) */
	private float position;
	
	public ControlPoint() {
		this(Color.WHITE, 0.0f);
	}
	
	/**
	 * Create a new control point
	 * 
	 * @param color The color at this control point
	 * @param position The position of this control point (0 -> 1)
	 */
	public ControlPoint(final Color color, final float position) {
		if (color == null)
			throw new IllegalArgumentException("'color' must not be null.");
		
		this.setColor(color);
		this.setPosition(position);
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public float getPosition() {
		return position;
	}

	public void setPosition(float position) {
		this.position = position;
	}
	
	@Override
	public int hashCode() {
		final int prime = 11;
		int result = 5;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + Float.floatToIntBits(position);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControlPoint other = (ControlPoint) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color)) {
			return false;
		}
		if (Float.floatToIntBits(position) != Float.floatToIntBits(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getPosition() + ":" + ColorUtil.toHexString(getColor());
	}
}
