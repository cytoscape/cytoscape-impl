
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.graph.render.immed;


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
	public void getAnchor(int anchorIndex, float[] anchorArr, int offset);
}
