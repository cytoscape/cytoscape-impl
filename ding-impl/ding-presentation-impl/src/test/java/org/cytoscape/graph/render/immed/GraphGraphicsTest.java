/*
 Copyright (c) 2009, 2010, The Cytoscape Consortium (www.cytoscape.org)

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


import junit.framework.*;

import java.util.List;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.awt.BasicStroke;

import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.commons.math.MathException;


public class GraphGraphicsTest extends TestCase {
	GraphGraphics currentGraphGraphics;
	OldGraphGraphics oldGraphGraphics;
	int numNodes; 
	int numEdges;
	BufferedImage image;
	int canvasSize = 1000;
	int numTests = 20;

	public GraphGraphicsTest() {
		super();
		String prop = System.getProperties().getProperty("num.graph.objects","5000");
		int numGraphObjs = 5000;
		if ( prop.matches("^\\d+$") )
			numGraphObjs = Integer.parseInt(prop);

		numNodes = numGraphObjs; 
		numEdges = numGraphObjs; 
	}

	public void setUp() {
		image = new BufferedImage(canvasSize,canvasSize,BufferedImage.TYPE_INT_ARGB);
		currentGraphGraphics = new GraphGraphics(image,false);
		currentGraphGraphics.clear(Color.white,0,0,1.0);
		oldGraphGraphics = new OldGraphGraphics(image,false);
		oldGraphGraphics.clear(Color.white,0,0,1.0);
	}

	public void testRenderGraphFull() {
		// run everything once, to prime the system
		Random rand = new Random(10);
		long oldDur = drawOldFull(rand);
		rand = new Random(10);
		long currDur = drawCurrentFull(rand);

		// now run each test numTests times and sum the durations
		long totalOldDur = 0;
		long totalCurrDur = 0;
		double[] oldDurs = new double[numTests];
		double[] currDurs = new double[numTests];
		for ( int i = 0; i < numTests; i++ ) {
			rand = new Random(i);
			oldDur = drawOldFull(rand);
			oldDurs[i] = (double)oldDur;
			totalOldDur += oldDur;

			rand = new Random(i);
			currDur = drawCurrentFull(rand);
			currDurs[i] = (double)currDur;
			totalCurrDur += currDur;

			System.out.println("Old: " + oldDur + "   Current: " + currDur + 
			                   "    diff: " + (oldDur - currDur) );
		}

		System.out.println("Total Old    : " + totalOldDur);
		System.out.println("Total Current: " + totalCurrDur);

		// Because the variance of the durations is so high, we can't just
		// just fail whenever we happen to be slower.  Therefore, we perform 
		// a t-test on the old and current durations and then evaluate the
		// p-value to determine if we are actually slower.
		double pValue = 1.0; 
		try {
			pValue = TestUtils.tTest(oldDurs, currDurs);
		} catch (MathException me) {
			me.printStackTrace();
		}

		System.out.println("T-test p-value: " + pValue);

		long diff = (totalOldDur-totalCurrDur)/numTests;
		// If the new code is faster than the old code, then we're good
		// and just move on.  No sense in evaluating the p-value because
		// if we're faster AND statistically significantly faster, then
		// that's a good thing and we don't want to fail.
		if ( diff >= 0 ) {
			System.out.println("     Faster on avg by: " + diff);

		// If we're slower than the old code (i.e. a bad thing), then
		// evaluate the p-value to determine if the difference is
		// statistically significant and only fail if it is.
		} else {
			System.out.println("     Slower on avg by: " + diff);

			assertTrue(pValue > 0.05);
		}
	}

	private long drawCurrentFull(Random rand) {
		final float nodeSizeFactor = 50f;
		float size = (float) canvasSize;

		long begin = System.nanoTime();
		for ( int i = 0; i < numNodes; i++ ) {
			float x = rand.nextFloat() * (rand.nextBoolean() ? size : -size); 
			float y = rand.nextFloat() * (rand.nextBoolean() ? size : -size); 
			currentGraphGraphics.drawNodeFull( (byte)(i % (int) GraphGraphics.s_last_shape), 
							x,
							y,
							(x + (rand.nextFloat() * nodeSizeFactor)),	
							(y + (rand.nextFloat() * nodeSizeFactor)),
							Color.blue,
							1.0f + (i % 10), null,
					    	Color.yellow);
		}
		long end = System.nanoTime();
		long nodeDur = end - begin;

		BasicStroke edgeStroke = new BasicStroke(1f);

		begin = System.nanoTime();
		for ( int i = 0; i < numEdges; i++ ) {
			currentGraphGraphics.drawEdgeFull(
				(byte)((i % 7)-8),
				rand.nextFloat() * (20f),
				Color.red, 
				(byte)((i % 7)-8),
				rand.nextFloat() * (20f),
				Color.orange, 
				rand.nextFloat() * (rand.nextBoolean() ? size : -size),
				rand.nextFloat() * (rand.nextBoolean() ? size : -size), 
				currentGraphGraphics.m_noAnchors,
				rand.nextFloat() * (rand.nextBoolean() ? size : -size),
				rand.nextFloat() * (rand.nextBoolean() ? size : -size), 
				1f, 
				edgeStroke, 
				Color.green);
		}
		end = System.nanoTime();
		long duration = (end - begin) + nodeDur;

//		try {
//			ImageIO.write(image,"PNG",new File("/tmp/homer-current-" + rand.nextInt(100) + ".png"));
//		} catch (IOException ioe) { ioe.printStackTrace(); }
		return duration;
	}

	private long drawOldFull(Random rand) {
		final float nodeSizeFactor = 50f;
		float size = (float) canvasSize;

		long begin = System.nanoTime();
		for ( int i = 0; i < numNodes; i++ ) {
			float x = rand.nextFloat() * (rand.nextBoolean() ? size : -size); 
			float y = rand.nextFloat() * (rand.nextBoolean() ? size : -size); 
			oldGraphGraphics.drawNodeFull( (byte)(i % (int) OldGraphGraphics.s_last_shape), 
							x,
							y,
							(x + (rand.nextFloat() * nodeSizeFactor)),	
							(y + (rand.nextFloat() * nodeSizeFactor)),
							Color.blue,
							1.0f + (i % 10),
					    	Color.yellow);
		}
		long end = System.nanoTime();
		long nodeDur = end - begin;

		BasicStroke edgeStroke = new BasicStroke(1f);

		begin = System.nanoTime();
		for ( int i = 0; i < numEdges; i++ ) {
			oldGraphGraphics.drawEdgeFull(
				(byte)((i % 7)-8),
				rand.nextFloat() * (20f),
				Color.red, 
				(byte)((i % 7)-8),
				rand.nextFloat() * (20f),
				Color.orange, 
				rand.nextFloat() * (rand.nextBoolean() ? size : -size),
				rand.nextFloat() * (rand.nextBoolean() ? size : -size), 
				oldGraphGraphics.m_noAnchors,
				rand.nextFloat() * (rand.nextBoolean() ? size : -size),
				rand.nextFloat() * (rand.nextBoolean() ? size : -size), 
				1f, 
				Color.green,
				0f);
		}
		end = System.nanoTime();
		
		long duration = (end - begin) + nodeDur;
		return duration;	
	}
}
