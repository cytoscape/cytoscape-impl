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

public class AnimatedDashDotStroke extends BasicStroke implements DAnimatedStroke {

	private final float width;
	private final float offset;

	public AnimatedDashDotStroke(float width) {
		this(width, INITIAL_OFFSET);
	}

	private AnimatedDashDotStroke(float width, float offset) {
		super(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f,
				new float[] { width * 4f, width * 2f, width, width * 2f }, width*9f*offset);

		this.width = width;
		this.offset = offset;
	}

	@Override
	public WidthStroke newInstanceForWidth(float w) {
		if (offset >= 0)
			return new AnimatedDashDotStroke(w, offset);
		else
			return new AnimatedDashDotStroke(w);
	}

	@Override
	public AnimatedDashDotStroke newInstanceForNextOffset() {
		return new AnimatedDashDotStroke(width, DAnimatedStroke.nextOffset(offset));
	}
	
	@Override
	public float getWidth() { 
		return width;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + width;
	}
}
