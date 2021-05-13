package org.cytoscape.graph.render.immed;

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


/**
 * Specifies edge anchor points to use when rendering edges in full detail
 * mode.
 */
public interface EdgeAnchors {
	
	
	/**
	 * Returns the number of edge anchors.
	 */
	public int numAnchors();

	/**
	 * Writes an edge anchor point into the array provided, at offset
	 * specified.  The information written into the supplied anchorArr parameter
	 * consists of the following:
	 * <blockquote><table border="1" cellpadding="5" cellspacing="0">
	 *   <tr>  <th>array index</th>  <th>information written</th>     </tr>
	 *   <tr>  <td>offset</td>       <td>X coordinate of anchor</td>  </tr>
	 *   <tr>  <td>offset+1</td>     <td>Y coordinate of anchor</td>  </tr>
	 * </table></blockquote>
	 * @exception IndexOutOfBoundsException if anchorIndex is not in the
	 *   range [0, numAnchors()-1].
	 */
	public void getAnchor(int anchorIndex, float[] anchorArr);
	
}
