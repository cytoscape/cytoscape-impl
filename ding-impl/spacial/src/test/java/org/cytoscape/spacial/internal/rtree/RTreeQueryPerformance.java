
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

import org.cytoscape.util.intr.IntEnumerator;
import org.cytoscape.util.intr.MinIntHeap;

import java.io.IOException;
import java.io.InputStream;


/**
 *
 */
public class RTreeQueryPerformance {
	/**
	 * For given N, creates N rectangles whose centers are in the space
	 * [0,1] X [0,1].  Each rectangle has width and height no greater
	 * than 1/sqrt(N).  The location of the centers of the rectangles
	 * and the choice of width and height for each rectangle is determined
	 * by the input stream, which in most cases will be a randomly generated
	 * stream of bytes.  Please see the actual code for an explanation of
	 * how the input stream of bytes is converted into the rectangle
	 * information.
	 */
	public static void main(String[] args) throws Exception {
		final RTree tree;
		// Populate the tree with entries.
		{
			int branches = Integer.parseInt(args[0]);
			int N = Integer.parseInt(args[1]);
			tree = new RTree(branches);

			double sqrtN = Math.sqrt((double) N);
			InputStream in = System.in;
			byte[] buff = new byte[16];
			int inx = 0;
			int off = 0;
			int read;

			while ((inx < N) && ((read = in.read(buff, off, buff.length - off)) > 0)) {
				off += read;

				if (off < buff.length)
					continue;
				else
					off = 0;

				int nonnegative = 0x7fffffff & assembleInt(buff, 0);
				double centerX = ((double) nonnegative) / ((double) 0x7fffffff);
				nonnegative = 0x7fffffff & assembleInt(buff, 4);

				double centerY = ((double) nonnegative) / ((double) 0x7fffffff);
				nonnegative = 0x7fffffff & assembleInt(buff, 8);

				double width = (((double) nonnegative) / ((double) 0x7fffffff)) / sqrtN;
				nonnegative = 0x7fffffff & assembleInt(buff, 12);

				double height = (((double) nonnegative) / ((double) 0x7fffffff)) / sqrtN;
				tree.insert(inx, (float) (centerX - (width / 2.0d)),
				            (float) (centerY - (height / 2.0d)),
				            (float) (centerX + (width / 2.0d)), (float) (centerY + (height / 2.0d)));
				inx++;
			}

			if (inx < N)
				throw new IOException("premature end of input");
		}

		final MinIntHeap[] pointQueries;
		// Test 121 Point queries.
		{
			pointQueries = new MinIntHeap[121];

			for (int i = 0; i < pointQueries.length; i++)
				pointQueries[i] = new MinIntHeap();

			for (int i = 0; i < 3; i++) {
				System.gc();
				Thread.sleep(1000);
			}

			final long millisBegin = System.currentTimeMillis();
			int inx = 0;
			float currX = -0.1f;

			for (int i = 0; i < 11; i++) {
				currX += 0.1f;

				float currY = -0.1f;

				for (int j = 0; j < 11; j++) {
					currY += 0.1f;

					final IntEnumerator iter = tree.queryOverlap(currX, currY, currX, currY, null,
					                                             0, false);
					final MinIntHeap heap = pointQueries[inx++];

					while (iter.numRemaining() > 0)
						heap.toss(iter.nextInt());
				}
			}

			final long millisEnd = System.currentTimeMillis();
			System.err.println("point queries took " + (millisEnd - millisBegin) + " milliseconds");
		}

		final MinIntHeap[] areaQueries;
		// Test 5 area queries - each area is 0.1 X 0.1.
		{
			areaQueries = new MinIntHeap[5];

			for (int i = 0; i < areaQueries.length; i++)
				areaQueries[i] = new MinIntHeap();

			for (int i = 0; i < 3; i++) {
				System.gc();
				Thread.sleep(1000);
			}

			final long millisBegin = System.currentTimeMillis();

			for (int i = 0; i < 5; i++) {
				final IntEnumerator iter = tree.queryOverlap(((float) i) * 0.1f,
				                                             ((float) i) * 0.1f,
				                                             ((float) (i + 1)) * 0.1f,
				                                             ((float) (i + 1)) * 0.1f, null, 0,
				                                             false);
				final MinIntHeap heap = areaQueries[i];

				while (iter.numRemaining() > 0)
					heap.toss(iter.nextInt());
			}

			final long millisEnd = System.currentTimeMillis();
			System.err.println("area queries took " + (millisEnd - millisBegin) + " milliseconds");
		}

		final int[] countQueries;
		// Test 5 count queries - each area is 0.6 X 0.6.
		{
			countQueries = new int[5];

			for (int i = 0; i < 3; i++) {
				System.gc();
				Thread.sleep(1000);
			}

			final long millisBegin = System.currentTimeMillis();

			for (int i = 0; i < 5; i++) {
				final IntEnumerator iter = tree.queryOverlap(((float) i) * 0.1f,
				                                             ((float) i) * 0.1f,
				                                             ((float) (i + 6)) * 0.1f,
				                                             ((float) (i + 6)) * 0.1f, null, 0,
				                                             false);
				countQueries[i] = iter.numRemaining();
			}

			final long millisEnd = System.currentTimeMillis();
			System.err.println("count queries took " + (millisEnd - millisBegin) + " milliseconds");
		}

		for (int i = 0; i < pointQueries.length; i++) {
			final MinIntHeap heap = pointQueries[i];

			while (heap.size() > 0)
				System.out.print(" " + heap.deleteMin());

			System.out.println();
		}

		for (int i = 0; i < areaQueries.length; i++) {
			final MinIntHeap heap = areaQueries[i];

			while (heap.size() > 0)
				System.out.print(" " + heap.deleteMin());

			System.out.println();
		}

		for (int i = 0; i < countQueries.length; i++) {
			System.out.println(countQueries[i]);
		}
	}

	private static int assembleInt(byte[] bytes, int offset) {
		int firstByte = (((int) bytes[offset]) & 0x000000ff) << 24;
		int secondByte = (((int) bytes[offset + 1]) & 0x000000ff) << 16;
		int thirdByte = (((int) bytes[offset + 2]) & 0x000000ff) << 8;
		int fourthByte = (((int) bytes[offset + 3]) & 0x000000ff) << 0;

		return firstByte | secondByte | thirdByte | fourthByte;
	}
}
