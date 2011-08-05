
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

package org.cytoscape.util.intr;

import org.cytoscape.util.intr.IntBTree;
import org.cytoscape.util.intr.IntEnumerator;

import java.io.IOException;
import java.io.InputStream;


/**
 *
 */
 // TODO Turn into JUnit tests
public class IntBTreeQueryTuner {
	/**
	 *  DOCUMENT ME!
	 *
	 * @param args DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 * @throws IllegalStateException DOCUMENT ME!
	 */
	public static void main(String[] args) throws IOException {
		int branches = Integer.parseInt(args[0]);
		int N = Integer.parseInt(args[1]);

		if (N < 1000)
			throw new IllegalStateException("N should be at least 1000");

		int[] elements = new int[N];
		InputStream in = System.in;
		byte[] buff = new byte[4];
		int inx = 0;
		int off = 0;
		int read;

		while ((inx < N) && ((read = in.read(buff, off, buff.length - off)) > 0)) {
			off += read;

			if (off < buff.length)
				continue;
			else
				off = 0;

			elements[inx++] = (0x7fffffff & assembleInt(buff)) % N;
		}

		if (inx < N)
			throw new IOException("premature end of input");

		IntBTree tree = new IntBTree(branches);

		for (int i = 0; i < elements.length; i++)
			tree.insert(elements[i]);

		int querySpan = N / 3;
		boolean reverseOrder = false;
		long timeBegin = System.currentTimeMillis();

		for (int i = 0; i < 1000; i += 3) {
			IntEnumerator iter = tree.searchRange(i, i + querySpan, reverseOrder);
			reverseOrder = !reverseOrder;
		}

		long timeEnd = System.currentTimeMillis();
		System.out.println((timeEnd - timeBegin) + " milliseconds");
	}

	private static int assembleInt(byte[] fourConsecutiveBytes) {
		int firstByte = (((int) fourConsecutiveBytes[0]) & 0x000000ff) << 24;
		int secondByte = (((int) fourConsecutiveBytes[1]) & 0x000000ff) << 16;
		int thirdByte = (((int) fourConsecutiveBytes[2]) & 0x000000ff) << 8;
		int fourthByte = (((int) fourConsecutiveBytes[3]) & 0x000000ff) << 0;

		return firstByte | secondByte | thirdByte | fourthByte;
	}
}
