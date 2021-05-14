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

import java.awt.Stroke;

/**
 * A simple extension of Stroke that allows Stroke objects to be
 * coupled with LineStyle enum values and allows the width of the
 * stroke to be adjusted.
 */
public interface WidthStroke extends Stroke {
	
	/**
	 * @return A new instance of this WidthStroke with the specified width.
	 */
	WidthStroke newInstanceForWidth(final float width);

//	/**
//	 * @return the LineStyle associated with this particular WidthStroke.
//	 */
//	LineStyle getLineStyle();
}
