
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

package org.cytoscape.spacial.internal.rtree;

import java.io.IOException;
import java.io.InputStream;


/**
 *
 */
public class ArrayTestMethod3 {
	/**
	 *  DOCUMENT ME!
	 *
	 * @param args DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public static void main(String[] args) throws Exception {
		final int nodeSize = Integer.parseInt(args[0]);
		final int yMinOffset = nodeSize;
		final int xMaxOffset = nodeSize * 2;
		final int yMaxOffset = nodeSize * 3;
		final int nPrime = 51437;
		final Node[] nodeArr = new Node[nPrime];

		for (int i = 0; i < nodeArr.length; i++)
			nodeArr[i] = new Node(nodeSize);
		// Read random data from standard in, populate node rectangles.
		{
			double sqrtN = Math.sqrt((double) nPrime);
			InputStream in = System.in;
			byte[] buff = new byte[16 * nodeSize];
			int inx = 0;
			int off = 0;
			int read;

			while ((inx < nodeArr.length) && ((read = in.read(buff, off, buff.length - off)) > 0)) {
				off += read;

				if (off < buff.length)
					continue;
				else
					off = 0;

				for (int i = 0; i < nodeSize; i++) {
					int nonnegative = 0x7fffffff & assembleInt(buff, 0 + (i * 16));
					double centerX = ((double) nonnegative) / ((double) 0x7fffffff);
					nonnegative = 0x7fffffff & assembleInt(buff, 4 + (i * 16));

					double centerY = ((double) nonnegative) / ((double) 0x7fffffff);
					nonnegative = 0x7fffffff & assembleInt(buff, 8 + (i * 16));

					double width = (((double) nonnegative) / ((double) 0x7fffffff)) / sqrtN;
					nonnegative = 0x7fffffff & assembleInt(buff, 12 + (i * 16));

					double height = (((double) nonnegative) / ((double) 0x7fffffff)) / sqrtN;
					nodeArr[inx].arr[i] = centerX - (width / 2.0d);
					nodeArr[inx].arr[i + yMinOffset] = centerY - (height / 2.0d);
					nodeArr[inx].arr[i + xMaxOffset] = centerX + (width / 2.0d);
					nodeArr[inx].arr[i + yMaxOffset] = centerY + (height / 2.0d);
				}

				inx++;
			}

			if (inx < nodeArr.length)
				throw new IOException("premature end of input");
		}
		// Sequential access test.
		{
			final int incr = 797;
			double foo;
			int inx = 0;
			final long millisBegin = System.currentTimeMillis();

			for (int i = 0; i < nodeArr.length; i++) {
				final Node n = nodeArr[inx];

				for (int j = 0; j < nodeSize; j++) {
					foo = n.arr[j] + n.arr[j + yMinOffset] + n.arr[j + xMaxOffset]
					      + n.arr[j + yMaxOffset];

					//           System.out.println(foo);
				}

				inx = (inx + incr) % nodeArr.length;
			}

			final long millisEnd = System.currentTimeMillis();
			System.err.println("sequential rectangle access took " + (millisEnd - millisBegin)
			                   + " milliseconds");
		}
	}

	private static int assembleInt(byte[] bytes, int offset) {
		int firstByte = (((int) bytes[offset]) & 0x000000ff) << 24;
		int secondByte = (((int) bytes[offset + 1]) & 0x000000ff) << 16;
		int thirdByte = (((int) bytes[offset + 2]) & 0x000000ff) << 8;
		int fourthByte = (((int) bytes[offset + 3]) & 0x000000ff) << 0;

		return firstByte | secondByte | thirdByte | fourthByte;
	}

	private final static class Node {
		private final double[] arr;

		private Node(final int size) {
			arr = new double[size * 4];
		}
	}
}
