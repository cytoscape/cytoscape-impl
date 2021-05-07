package org.cytoscape.graph.render.stateful;

import org.cytoscape.ding.impl.canvas.NetworkImageBuffer;
import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;

import junit.framework.TestCase;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class GraphRendererTest extends TestCase {
	private GraphGraphics grafx;

	public void setUp() {
		NetworkTransform transform = new NetworkTransform(500, 500);
		NetworkImageBuffer img = new NetworkImageBuffer(transform);
		grafx = new GraphGraphics(img);
	}

	// normal without anchors
	public void testComputeEdgeEndpoints_1() {
		float[] srcNodeExtents = {10.0f,10.0f,20.0f,20.0f};
		float[] trgNodeExtents = {110.0f,110.0f,120.0f,120.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_ELLIPSE,
		                                  ArrowShapeVisualProperty.CIRCLE,5.0f,
										  null /*anchors*/,
										  trgNodeExtents,GraphGraphics.SHAPE_RECTANGLE,
										  ArrowShapeVisualProperty.DELTA,7.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("1 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("1 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		//1 source X: 18.535534  Y: 18.535534
		//1 target X: 110.0  Y: 110.0
		assertEquals( 18.5355f, rtnValSrc[0], 0.001f );
		assertEquals( 18.5355f, rtnValSrc[1], 0.001f );
		assertEquals( 110.0f, rtnValTrg[0], 0.001f );
		assertEquals( 110.0f, rtnValTrg[1], 0.001f );
	}

	// normal with anchors
	public void testComputeEdgeEndpoints_2() {
		float[] srcNodeExtents = {10.0f,10.0f,20.0f,20.0f};
		float[] trgNodeExtents = {110.0f,110.0f,120.0f,120.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_TRIANGLE,
		                                  ArrowShapeVisualProperty.NONE,5.0f,
										  new SingleEdgeAnchor(new float[]{100.0f,100.0f}),
										  trgNodeExtents,GraphGraphics.SHAPE_VEE,
										  ArrowShapeVisualProperty.T,7.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("2 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("2 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		// 2 source X: 20.0  Y: 20.0
		// 2 target X: 110.0  Y: 110.0
		assertEquals( 20.0f, rtnValSrc[0], 0.001f );
		assertEquals( 20.0f, rtnValSrc[1], 0.001f );
		assertEquals( 110.0f, rtnValTrg[0], 0.001f );
		assertEquals( 110.0f, rtnValTrg[1], 0.001f );
	}

	
	// srcExtents width zero
	public void testComputeEdgeEndpoints_3() {
		float[] srcNodeExtents = {10.0f,10.0f,10.0f,20.0f};
		float[] trgNodeExtents = {110.0f,110.0f,120.0f,120.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_TRIANGLE,
                                          ArrowShapeVisualProperty.NONE,5.0f,
										  null /*anchors*/,
										  trgNodeExtents,GraphGraphics.SHAPE_VEE,
										  ArrowShapeVisualProperty.T,0.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("3 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("3 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		// 3 source X: 10.0  Y: 15.0
		// 3 target X: 110.22727  Y: 110.454544
		assertEquals( 10.0f, rtnValSrc[0], 0.001f );
		assertEquals( 15.0f, rtnValSrc[1], 0.001f );
		assertEquals( 110.227f, rtnValTrg[0], 0.001f );
		assertEquals( 110.455f, rtnValTrg[1], 0.001f );
	}

	// srcExtents height zero
	public void testComputeEdgeEndpoints_4() {
		float[] srcNodeExtents = {10.0f,10.0f,20.0f,10.0f};
		float[] trgNodeExtents = {110.0f,110.0f,120.0f,120.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_TRIANGLE,
                                          ArrowShapeVisualProperty.NONE,5.0f,
										  new SingleEdgeAnchor(new float[]{75.0f,150.0f}),
										  trgNodeExtents,GraphGraphics.SHAPE_VEE,
										  ArrowShapeVisualProperty.T,7.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("4 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("4 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		// 4 source X: 15.0  Y: 10.0
		// 4 target X: 113.26087  Y: 116.521736
		assertEquals( 15.0f, rtnValSrc[0], 0.001f );
		assertEquals( 10.0f, rtnValSrc[1], 0.001f );
		assertEquals( 113.261f, rtnValTrg[0], 0.001f );
		assertEquals( 116.522f, rtnValTrg[1], 0.001f );
	}
	
	// trgExtents width zero
	public void testComputeEdgeEndpoints_5() {
		float[] srcNodeExtents = {10.0f,10.0f,20.0f,20.0f};
		float[] trgNodeExtents = {110.0f,110.0f,110.0f,120.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_HEXAGON,
                                          ArrowShapeVisualProperty.DIAMOND,8.0f,
										  null /*anchors*/,
										  trgNodeExtents,GraphGraphics.SHAPE_OCTAGON,
										  ArrowShapeVisualProperty.ARROW,57.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("5 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("5 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		// 5 source X: 17.938145  Y: 18.092783
		// 5 target X: 110.0  Y: 115.0
		// TODO - why are these tests suddenly wrong?
		//assertEquals( 17.938f, rtnValSrc[0], 0.001f );
		//assertEquals( 18.093f, rtnValSrc[1], 0.001f );
		assertEquals( 110.0f, rtnValTrg[0], 0.001f );
		assertEquals( 115.0f, rtnValTrg[1], 0.001f );
	}

	// trgExtents height zero
	public void testComputeEdgeEndpoints_6() {
		float[] srcNodeExtents = {10.0f,10.0f,20.0f,20.0f};
		float[] trgNodeExtents = {110.0f,110.0f,120.0f,110.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_PARALLELOGRAM,
                                          ArrowShapeVisualProperty.HALF_BOTTOM,1.0f,
										  new SingleEdgeAnchor(new float[]{0.0f,50.0f}),
										  trgNodeExtents,GraphGraphics.SHAPE_ROUNDED_RECTANGLE,
										  ArrowShapeVisualProperty.HALF_TOP,17.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("6 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("6 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		// 6 source X: 13.125  Y: 19.375
		// 6 target X: 115.0  Y: 110.0
		assertEquals( 13.125f, rtnValSrc[0], 0.001f );
		assertEquals( 19.375f, rtnValSrc[1], 0.001f );
		assertEquals( 115.0f, rtnValTrg[0], 0.001f );
		assertEquals( 110.0f, rtnValTrg[1], 0.001f );
	}

	// one node within the other - different center
	public void testComputeEdgeEndpoints_7() {
		float[] srcNodeExtents = {10.0f,10.0f,20.0f,20.0f};
		float[] trgNodeExtents = {12.0f,12.0f,17.0f,17.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_PARALLELOGRAM,
                                          ArrowShapeVisualProperty.HALF_BOTTOM,1.0f,
										  new SingleEdgeAnchor(new float[]{0.0f,50.0f}),
										  trgNodeExtents,GraphGraphics.SHAPE_ROUNDED_RECTANGLE,
										  ArrowShapeVisualProperty.HALF_TOP,17.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("7 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("7 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		// 7 source X: 13.125  Y: 19.375
		// 7 target X: 13.478873  Y: 17.0
		assertEquals( 13.125f, rtnValSrc[0], 0.001f );
		assertEquals( 19.375f, rtnValSrc[1], 0.001f );
		assertEquals( 13.479f, rtnValTrg[0], 0.001f );
		assertEquals( 17.0f, rtnValTrg[1], 0.001f );
	}

	// one node within the other - same center
	public void testComputeEdgeEndpoints_8() {
		float[] srcNodeExtents = {10.0f,10.0f,20.0f,20.0f};
		float[] trgNodeExtents = {12.0f,12.0f,18.0f,18.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_PARALLELOGRAM,
                                          ArrowShapeVisualProperty.HALF_BOTTOM,1.0f,
										  new SingleEdgeAnchor(new float[]{0.0f,50.0f}),
										  trgNodeExtents,GraphGraphics.SHAPE_ROUNDED_RECTANGLE,
										  ArrowShapeVisualProperty.HALF_TOP,17.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("8 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("8 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		// 8 source X: 13.125  Y: 19.375
		// 8 target X: 13.714286  Y: 18.0
		assertEquals( 13.125f, rtnValSrc[0], 0.001f );
		assertEquals( 19.375f, rtnValSrc[1], 0.001f );
		assertEquals( 13.714f, rtnValTrg[0], 0.001f );
		assertEquals( 18.0f, rtnValTrg[1], 0.001f );
	}

	// nodes the same
	public void testComputeEdgeEndpoints_9() {
		float[] srcNodeExtents = {10.0f,10.0f,20.0f,20.0f};
		float[] trgNodeExtents = {10.0f,10.0f,20.0f,20.0f};
		float[] rtnValSrc = new float[2];
		float[] rtnValTrg = new float[2];
		GraphRenderer.computeEdgeEndpoints(
		                                  srcNodeExtents,GraphGraphics.SHAPE_PARALLELOGRAM,
                                          ArrowShapeVisualProperty.HALF_BOTTOM,1.0f,
										  new SingleEdgeAnchor(new float[]{0.0f,50.0f}),
										  trgNodeExtents,GraphGraphics.SHAPE_ROUNDED_RECTANGLE,
										  ArrowShapeVisualProperty.HALF_TOP,17.0f,
										  rtnValSrc,rtnValTrg);

		System.out.println("9 source X: " + rtnValSrc[0] + "  Y: " + rtnValSrc[1]);
		System.out.println("9 target X: " + rtnValTrg[0] + "  Y: " + rtnValTrg[1]);
		// 9 source X: 13.125  Y: 19.375
		// 9 target X: 12.857142  Y: 20.0
		assertEquals( 13.125f, rtnValSrc[0], 0.001f );
		assertEquals( 19.375f, rtnValSrc[1], 0.001f );
		assertEquals( 12.857f, rtnValTrg[0], 0.001f );
		assertEquals( 20.0f, rtnValTrg[1], 0.001f );
	}


	private class SingleEdgeAnchor implements EdgeAnchors {
		private float[] pt;	
		SingleEdgeAnchor(float[] pt) {
			if ( pt.length != 2 )
				throw new IllegalArgumentException("must be exactly one anchor");
			this.pt = pt;
		}
    	public int numAnchors() { return 1;	}
    	public void getAnchor(int anchorIndex, float[] anchorArr) {
			anchorArr[0] = pt[0];
			anchorArr[1] = pt[1];
		}
	}
}

