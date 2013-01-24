package org.cytoscape.spacial.internal.rtree;

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

import java.io.IOException;
import java.io.InputStream;


/**
 *
 */
public class ArrayTestMethod1 {
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
					nodeArr[inx].xMins[i] = centerX - (width / 2.0d);
					nodeArr[inx].yMins[i] = centerY - (height / 2.0d);
					nodeArr[inx].xMaxs[i] = centerX + (width / 2.0d);
					nodeArr[inx].yMaxs[i] = centerY + (height / 2.0d);
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
					foo = n.xMins[j] + n.yMins[j] + n.xMaxs[j] + n.yMaxs[j];

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
		private final double[] xMins;
		private final double[] yMins;
		private final double[] xMaxs;
		private final double[] yMaxs;

		private Node(final int size) {
			xMins = new double[size];
			yMins = new double[size];
			xMaxs = new double[size];
			yMaxs = new double[size];
		}
	}
}
