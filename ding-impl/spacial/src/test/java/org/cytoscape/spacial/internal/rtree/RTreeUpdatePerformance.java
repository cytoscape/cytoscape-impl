
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
public class RTreeUpdatePerformance {
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
		final float[] data;
		final int N = Integer.parseInt(args[1]);
		final int branches = Integer.parseInt(args[0]);
		// Populate the array with entries.
		{
			data = new float[N * 4];

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
				data[inx * 4] = (float) (centerX - (width / 2.0d));
				data[(inx * 4) + 1] = (float) (centerY - (height / 2.0d));
				data[(inx * 4) + 2] = (float) (centerX + (width / 2.0d));
				data[(inx * 4) + 3] = (float) (centerY + (height / 2.0d));
				inx++;
			}

			if (inx < N)
				throw new IOException("premature end of input");
		}

		final RTree tree = new RTree(branches);
		// Initial insertion test.
		{
			for (int i = 0; i < 2; i++) {
				System.gc();
				Thread.sleep(1000);
			}

			final long millisBegin = System.currentTimeMillis();
			int objKey = 0;
			int inx = 0;

			while (objKey < N) {
				tree.insert(objKey++, data[inx++], data[inx++], data[inx++], data[inx++]);
			}

			final long millisEnd = System.currentTimeMillis();
			System.err.println("initial insertions took " + (millisEnd - millisBegin)
			                   + " milliseconds");
		}
		// Initial area query test.
		{
			for (int i = 0; i < 2; i++) {
				System.gc();
				Thread.sleep(1000);
			}

			final long millisBegin = System.currentTimeMillis();

			for (int i = 0; i < 5; i++) {
				tree.queryOverlap(((float) i) * 0.1f, ((float) i) * 0.1f, ((float) (i + 1)) * 0.1f,
				                  ((float) (i + 1)) * 0.1f, null, 0, false);
			}

			final long millisEnd = System.currentTimeMillis();
			System.err.println("initial area queries (5) took " + (millisEnd - millisBegin)
			                   + " milliseconds");
		}
		// Initial point query test.
		{
			for (int i = 0; i < 2; i++) {
				System.gc();
				Thread.sleep(1000);
			}

			final long millisBegin = System.currentTimeMillis();

			for (int i = 0; i < 5; i++) {
				tree.queryOverlap(((float) i) * 0.1f, ((float) i) * 0.1f, ((float) i) * 0.1f,
				                  ((float) i) * 0.1f, null, 0, false);
			}

			final long millisEnd = System.currentTimeMillis();
			System.err.println("initial point queries (5) took " + (millisEnd - millisBegin)
			                   + " milliseconds");
		}
		// Initial extents test.
		{
			for (int i = 0; i < 2; i++) {
				System.gc();
				Thread.sleep(1000);
			}

			final float[] extents = new float[4];
			final long millisBegin = System.currentTimeMillis();
			int objKey = 0;

			while (objKey < N) {
				tree.exists(objKey++, extents, 0);
			}

			final long millisEnd = System.currentTimeMillis();
			System.err.println("all extents queries took " + (millisEnd - millisBegin)
			                   + " milliseconds");
		}

		for (byte a = 0; a < 3; a++) {
			// Update test.
			{
				for (int i = 0; i < 2; i++) {
					System.gc();
					Thread.sleep(1000);
				}

				final long millisBegin = System.currentTimeMillis();
				int objKey = 0;
				int inx = 0;

				while (objKey < N) {
					tree.delete(objKey);
					tree.insert(objKey++, data[inx++], data[inx++], data[inx++], data[inx++]);
				}

				final long millisEnd = System.currentTimeMillis();
				System.err.println("updates took " + (millisEnd - millisBegin) + " milliseconds");
			}
			// Repeated area query test.
			{
				for (int i = 0; i < 2; i++) {
					System.gc();
					Thread.sleep(1000);
				}

				final long millisBegin = System.currentTimeMillis();

				switch (a) {
					case 0:

						for (int i = 0; i < 5; i++) {
							tree.queryOverlap(((float) i) * 0.1f, ((float) (9 - i)) * 0.1f,
							                  ((float) (i + 1)) * 0.1f, ((float) (10 - i)) * 0.1f,
							                  null, 0, false);
						}

						break;

					case 1:

						for (int i = 0; i < 5; i++) {
							tree.queryOverlap(((float) (9 - i)) * 0.1f, ((float) i) * 0.1f,
							                  ((float) (10 - i)) * 0.1f, ((float) (i + 1)) * 0.1f,
							                  null, 0, false);
						}

						break;

					case 2:

						for (int i = 0; i < 5; i++) {
							tree.queryOverlap(((float) (9 - i)) * 0.1f, ((float) (9 - i)) * 0.1f,
							                  ((float) (10 - i)) * 0.1f, ((float) (10 - i)) * 0.1f,
							                  null, 0, false);
						}

						break;
				}

				final long millisEnd = System.currentTimeMillis();
				System.err.println("repeated area queries (5) took " + (millisEnd - millisBegin)
				                   + " milliseconds");
			}
			// Repeated point query test.
			{
				for (int i = 0; i < 2; i++) {
					System.gc();
					Thread.sleep(1000);
				}

				final long millisBegin = System.currentTimeMillis();

				switch (a) {
					case 0:

						for (int i = 0; i < 5; i++) {
							tree.queryOverlap(((float) i) * 0.1f, ((float) (10 - i)) * 0.1f,
							                  ((float) i) * 0.1f, ((float) (10 - i)) * 0.1f, null,
							                  0, false);
						}

						break;

					case 1:

						for (int i = 0; i < 5; i++) {
							tree.queryOverlap(((float) (10 - i)) * 0.1f, ((float) i) * 0.1f,
							                  ((float) (10 - i)) * 0.1f, ((float) i) * 0.1f, null,
							                  0, false);
						}

						break;

					case 2:

						for (int i = 0; i < 5; i++) {
							tree.queryOverlap(((float) (10 - i)) * 0.1f, ((float) (10 - i)) * 0.1f,
							                  ((float) (10 - i)) * 0.1f, ((float) (10 - i)) * 0.1f,
							                  null, 0, false);
						}

						break;
				}

				final long millisEnd = System.currentTimeMillis();
				System.err.println("repeated point queries (5) took " + (millisEnd - millisBegin)
				                   + " milliseconds");
			}
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
