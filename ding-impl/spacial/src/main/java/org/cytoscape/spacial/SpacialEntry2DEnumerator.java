
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

package org.cytoscape.spacial;

import org.cytoscape.util.intr.IntEnumerator;


/**
 * An enumeration over a set of entries in a SpacialIndex2D structure.
 * The purpose of this class above and beyond IntEnumerator (which it extends)
 * is to efficiently provide minimum bounding rectangle information for each
 * entry returned.
 */
public interface SpacialEntry2DEnumerator extends IntEnumerator {
	/**
	 * Copies into the supplied array [starting at specified offset] the minimum
	 * bounding rectangle of the next entry, and returns that next entry.
	 * The behavior of this method is identical to nextInt() except that in
	 * addition to returning a value, extents information is returned as well.
	 * The information written into the supplied extentsArr parameter consists
	 * of the following:
	 * <blockquote><table border="1" cellpadding="5" cellspacing="0">
	 *   <tr>  <th>array index</th>  <th>information written</th>  </tr>
	 *   <tr>  <td>offset</td>       <td>xMin of MBR</td>          </tr>
	 *   <tr>  <td>offset+1</td>     <td>yMin of MBR</td>          </tr>
	 *   <tr>  <td>offset+2</td>     <td>xMax of MBR</td>          </tr>
	 *   <tr>  <td>offset+3</td>     <td>yMax of MBR</td>          </tr>
	 * </table></blockquote><p>
	 * NOTE: If the retrieval of minimum bounding rectangle information for
	 * an entry is not important to the user of this enumeration, it is
	 * preferable to call nextInt() instead of nextExtents() for performance
	 * reasons.
	 * @param extentsArr an array to which extent values will be written by this
	 *   method; cannot be null.
	 * @param offset specifies the beginning index of where to write extent
	 *   values into extentsArr; exactly four entries are written starting
	 *   at this index (see above table).
	 * @return the next entry (objKey) in the enumeration.
	 * @exception ArrayIndexOutOfBoundsException if extentsArr cannot be
	 *   written to in the index range [offset, offset+3].
	 */
	public int nextExtents(float[] extentsArr, int offset);
}
