package org.cytoscape.graph.render.stateful;

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


import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.intr.LongHash;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.Position;
import org.cytoscape.view.vizmap.VisualPropertyDependency;


/**
 * This class contains a chunk of procedural code that stitches together
 * several external modules in an effort to efficiently render graphs.
 */
public final class GraphRenderer {
	/**
	 * A bit representing....
	 */
	public final static int LOD_HIGH_DETAIL = 0x1;

	/**
	 * DOCUMENT ME!
	 */
	public final static int LOD_NODE_BORDERS = 0x2;

	/**
	 * DOCUMENT ME!
	 */
	public final static int LOD_NODE_LABELS = 0x4;

	/**
	 * DOCUMENT ME!
	 */
	public final static int LOD_EDGE_ARROWS = 0x8;

	/**
	 * DOCUMENT ME!
	 */
	public final static int LOD_DASHED_EDGES = 0x10;

	/**
	 * DOCUMENT ME!
	 */
	public final static int LOD_EDGE_ANCHORS = 0x20;

	/**
	 * DOCUMENT ME!
	 */
	public final static int LOD_EDGE_LABELS = 0x40;

	/**
	 * DOCUMENT ME!
	 */
	public final static int LOD_TEXT_AS_SHAPE = 0x80;

	/**
	 * DOCUMENT ME!
	 */
	public final static int LOD_CUSTOM_GRAPHICS = 0x100;

	// No constructor.
	private GraphRenderer() {
	}

	/**
	 * Renders a graph.
	 * @param netView the network view; nodes in this graph must correspond to
	 *   objKeys in nodePositions (the SpacialIndex2D parameter) and vice versa.
	 * @param nodePositions defines the positions and extents of nodes in graph;
	 *   each entry (objKey) in this structure must correspond to a node in graph
	 *   (the CyNetwork parameter) and vice versa; the order in which nodes are
	 *   rendered is defined by a non-reversed overlap query on this structure.
	 * @param lod defines the different levels of detail; an appropriate level
	 *   of detail is chosen based on the results of method calls on this
	 *   object.
	 * @param nodeDetails defines details of nodes such as colors, node border
	 *   thickness, and shape; the node arguments passed to methods on this
	 *   object will be nodes in the graph parameter.
	 * @param edgeDetails defines details of edges such as colors, thickness,
	 *   and arrow type; the edge arguments passed to methods on this
	 *   object will be edges in the graph parameter.
	 * @param nodeBuff this is a computational helper that is required in the
	 *   implementation of this method; when this method returns, nodeBuff is
	 *   in a state such that an edge in graph has been rendered by this method
	 *   if and only if it touches at least one node in this nodeBuff set;
	 *   no guarantee made regarding edgeless nodes.
	 * @param grafx the graphics context that is to render this graph.
	 * @param bgPaint the background paint to use when calling grafx.clear().
	 * @param xCenter the xCenter parameter to use when calling grafx.clear().
	 * @param yCenter the yCenter parameter to use when calling grafx.clear().
	 * @param scaleFactor the scaleFactor parameter to use when calling
	 *   grafx.clear().
	 * @param dependencies 
	 * @return bits representing the level of detail that was rendered; the
	 *   return value is a bitwise-or'ed value of the LOD_* constants.
	 */
	public final static int renderGraph(final CyNetworkViewSnapshot netView,
	                                    final GraphLOD lod,
	                                    final NodeDetails nodeDetails,
	                                    final EdgeDetails edgeDetails,
	                                    final LongHash nodeBuff,
	                                    final GraphGraphics grafx,
	                                    final Paint bgPaint,
	                                    final double xCenter,
	                                    final double yCenter,
	                                    final double scaleFactor,
	                                    final boolean haveZOrder,
	                                    final Set<VisualPropertyDependency<?>> dependencies) {
		nodeBuff.empty(); // Make sure we keep our promise.

		if (grafx == null || grafx.image == null)
			return 0;
		
//		final CyNetwork graph = netView.getModel();
		
		// Define the visible window in node coordinate space.
		final float xMin;

		// Define the visible window in node coordinate space.
		final float yMin;

		// Define the visible window in node coordinate space.
		final float xMax;

		// Define the visible window in node coordinate space.
		final float yMax;
		xMin = (float) (xCenter - ((0.5d * grafx.image.getWidth(null)) / scaleFactor));
		yMin = (float) (yCenter - ((0.5d * grafx.image.getHeight(null)) / scaleFactor));
		xMax = (float) (xCenter + ((0.5d * grafx.image.getWidth(null)) / scaleFactor));
		yMax = (float) (yCenter + ((0.5d * grafx.image.getHeight(null)) / scaleFactor));

		// Define buffers.  These are of the few objects we're instantiating
		// directly in this method.
		final float[] floatBuff1;

		// Define buffers.  These are of the few objects we're instantiating
		// directly in this method.
		final float[] floatBuff2;

		// Define buffers.  These are of the few objects we're instantiating
		// directly in this method.
		final float[] floatBuff3;

		// Define buffers.  These are of the few objects we're instantiating
		// directly in this method.
		final float[] floatBuff4;

		// Define buffers.  These are of the few objects we're instantiating
		// directly in this method.
		final float[] floatBuff5;
		final double[] doubleBuff1;
		final double[] doubleBuff2;
		final GeneralPath path2d;
		floatBuff1 = new float[4];
		floatBuff2 = new float[4];
		floatBuff3 = new float[2];
		floatBuff4 = new float[2];
		floatBuff5 = new float[8];
		doubleBuff1 = new double[4];
		doubleBuff2 = new double[2];
		path2d = new GeneralPath();

		// Determine the number of nodes and edges that we are about to render.
		final int renderNodeCount;
		final int renderEdgeCount;
		final byte renderEdges;
		long start = System.currentTimeMillis();

		{
			SpacialIndex2DEnumerator<Long> nodeHits = netView.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
			
			final int visibleNodeCount = nodeHits.size();
			final int totalNodeCount = netView.getNodeCount();
			final int totalEdgeCount = netView.getEdgeCount();
			renderEdges = lod.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);

			if (renderEdges > 0) {
				int runningNodeCount = 0;

				for (int i = 0; i < visibleNodeCount; i++) {
					nodeHits.nextExtents(floatBuff1);

					if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3]))
						runningNodeCount++;
				}

				renderNodeCount = runningNodeCount;
				renderEdgeCount = totalEdgeCount;
			} else if (renderEdges < 0) {
				int runningNodeCount = 0;

				for (int i = 0; i < visibleNodeCount; i++) {
					nodeHits.nextExtents(floatBuff1);

					if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3]))
						runningNodeCount++;
				}

				renderNodeCount = runningNodeCount;
				renderEdgeCount = 0;
			} else {
				int runningNodeCount = 0;
				int runningEdgeCount = 0;

				for (int i = 0; i < visibleNodeCount; i++) {
					final long nodeSuid = nodeHits.nextExtents(floatBuff1);

					if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3]))
						runningNodeCount++;

					Iterable<View<CyEdge>> touchingEdges = netView.getAdjacentEdgeIterable(nodeSuid);

					for ( View<CyEdge> e : touchingEdges ) {
						SnapshotEdgeInfo edgeInfo = netView.getEdgeInfo(e);
						if (!edgeDetails.isVisible(e))
							continue;
						final long edge = e.getSUID(); 
						final long otherNode = nodeSuid ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();

						if (nodeBuff.get(otherNode) < 0)
							runningEdgeCount++;
					}

					nodeBuff.put(nodeSuid);
				}

				renderNodeCount = runningNodeCount;
				renderEdgeCount = runningEdgeCount;
				nodeBuff.empty();
			}
		}	
		// System.out.println("renderEdgeCount: "+renderEdgeCount);
		// System.out.println("time: "+(System.currentTimeMillis()-start)+"ms");

		// Based on number of objects we are going to render, determine LOD.
		final int lodBits;

		{
			int lodTemp = 0;

			if (lod.detail(renderNodeCount, renderEdgeCount)) {
				lodTemp |= LOD_HIGH_DETAIL;

				if (lod.nodeBorders(renderNodeCount, renderEdgeCount))
					lodTemp |= LOD_NODE_BORDERS;

				if (lod.nodeLabels(renderNodeCount, renderEdgeCount))
					lodTemp |= LOD_NODE_LABELS;

				if (lod.edgeArrows(renderNodeCount, renderEdgeCount))
					lodTemp |= LOD_EDGE_ARROWS;

				if (lod.dashedEdges(renderNodeCount, renderEdgeCount))
					lodTemp |= LOD_DASHED_EDGES;

				if (lod.edgeAnchors(renderNodeCount, renderEdgeCount))
					lodTemp |= LOD_EDGE_ANCHORS;

				if (lod.edgeLabels(renderNodeCount, renderEdgeCount))
					lodTemp |= LOD_EDGE_LABELS;

				if ((((lodTemp & LOD_NODE_LABELS) != 0) || ((lodTemp & LOD_EDGE_LABELS) != 0))
				    && lod.textAsShape(renderNodeCount, renderEdgeCount))
					lodTemp |= LOD_TEXT_AS_SHAPE;

				if (lod.customGraphics(renderNodeCount, renderEdgeCount))
					lodTemp |= LOD_CUSTOM_GRAPHICS;
			}

			lodBits = lodTemp;
		}
		// Clear the background.
		{
			if (bgPaint != null)
				grafx.clear(bgPaint, xCenter, yCenter, scaleFactor);
		}

		// Render the edges first.  No edge shall be rendered twice.  Render edge
		// labels.  A label is not necessarily on top of every edge; it is only
		// on top of the edge it belongs to.
		if (renderEdges >= 0) {
			final SpacialIndex2DEnumerator<Long> nodeHits;

			// System.out.println("Rendering edges: high detail = "+(lodBits & LOD_HIGH_DETAIL));
			// System.out.println("time: "+(System.currentTimeMillis()-start)+"ms");

			if (renderEdges > 0)
				// We want to render edges in the same order (back to front) that
				// we would use to render just edges on visible nodes; this is assuming
				// that our spacial index has the subquery order-preserving property.
				nodeHits = netView.getSpacialIndex2D().queryAll();
			else
				nodeHits = netView.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
		
			if ((lodBits & LOD_HIGH_DETAIL) == 0) { // Low detail.

				final int nodeHitCount = nodeHits.size();

				for (int i = 0; i < nodeHitCount; i++) {
					final long nodeSuid = nodeHits.nextExtents(floatBuff1);

					// Casting to double and then back we could achieve better accuracy
					// at the expense of performance.
					final float nodeX = (floatBuff1[0] + floatBuff1[2]) / 2;
					final float nodeY = (floatBuff1[1] + floatBuff1[3]) / 2;

					Iterable<View<CyEdge>> touchingEdges = netView.getAdjacentEdgeIterable(nodeSuid);

					for ( View<CyEdge> edge : touchingEdges ) {
						if (!edgeDetails.isVisible(edge))
							continue;
						SnapshotEdgeInfo edgeInfo = netView.getEdgeInfo(edge);
						final long otherNode = nodeSuid ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();

						if (nodeBuff.get(otherNode) < 0) { // Has not yet been rendered.
							netView.getSpacialIndex2D().get(otherNode, floatBuff2);
							grafx.drawEdgeLow(nodeX, nodeY, 
							                  // Again, casting issue - tradeoff between
							                  // accuracy and performance.
							                  (floatBuff2[0] + floatBuff2[2]) / 2,
							                  (floatBuff2[1] + floatBuff2[3]) / 2,
							                  edgeDetails.getColorLowDetail(netView, edge));
						}
					}

					nodeBuff.put(nodeSuid);
				}
			} else { // High detail.
				while (nodeHits.size() > 0) {
					final long nodeSuid = nodeHits.nextExtents(floatBuff1);
					final View<CyNode> node = netView.getNodeView(nodeSuid);
					final byte nodeShape = nodeDetails.getShape(node);
					Iterable<View<CyEdge>> touchingEdges = netView.getAdjacentEdgeIterable(node);
					for (View<CyEdge> edge : touchingEdges) {
						if (!edgeDetails.isVisible(edge))
							continue;
						SnapshotEdgeInfo edgeInfo = netView.getEdgeInfo(edge);
						final long otherNode = nodeSuid ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
						final View<CyNode> otherCyNode = netView.getNodeView(otherNode);

						if (nodeBuff.get(otherNode) < 0) { // Has not yet been rendered.

							if (!netView.getSpacialIndex2D().get(otherNode, floatBuff2))
								continue;
								// throw new IllegalStateException("nodePositions not recognizing node that exists in graph: "+otherCyNode.toString());

							final byte otherNodeShape = nodeDetails.getShape(otherCyNode);

							// Compute node shapes, center positions, and extents.
							final byte srcShape;

							// Compute node shapes, center positions, and extents.
							final byte trgShape;
							final float[] srcExtents;
							final float[] trgExtents;
							if (nodeSuid == edgeInfo.getSourceViewSUID()) {
								srcShape = nodeShape;
								trgShape = otherNodeShape;
								srcExtents = floatBuff1;
								trgExtents = floatBuff2;
							} else { // node == graph.edgeTarget(edge).
								srcShape = otherNodeShape;
								trgShape = nodeShape;
								srcExtents = floatBuff2;
								trgExtents = floatBuff1;
							}

							// Compute visual attributes that do not depend on LOD.
							final float thickness = (float) edgeDetails.getWidth(edge);
							final Stroke edgeStroke = edgeDetails.getStroke(edge);
							final Paint segPaint = edgeDetails.getPaint(edge);

							// Compute arrows.
							final ArrowShape srcArrow;
							final ArrowShape trgArrow;
							final float srcArrowSize;
							final float trgArrowSize;
							final Paint srcArrowPaint;
							final Paint trgArrowPaint;

							if ((lodBits & LOD_EDGE_ARROWS) == 0) { // Not rendering arrows.
								trgArrow = srcArrow = ArrowShapeVisualProperty.NONE;
								trgArrowSize = srcArrowSize = 0.0f;
								trgArrowPaint = srcArrowPaint = null;
							} else { // Rendering edge arrows.
								srcArrow = edgeDetails.getSourceArrowShape(edge);
								trgArrow = edgeDetails.getTargetArrowShape(edge);
								srcArrowSize  = ((srcArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : edgeDetails.getSourceArrowSize(edge));
								trgArrowSize  = ((trgArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : edgeDetails.getTargetArrowSize(edge));
								srcArrowPaint = ((srcArrow == ArrowShapeVisualProperty.NONE) ? null : edgeDetails.getSourceArrowPaint(edge));
								trgArrowPaint = ((trgArrow == ArrowShapeVisualProperty.NONE) ? null : edgeDetails.getTargetArrowPaint(edge));
							}

							// Compute the anchors to use when rendering edge.
							final EdgeAnchors anchors = (((lodBits & LOD_EDGE_ANCHORS) == 0) ? null : edgeDetails.getAnchors(edge));

							if (!computeEdgeEndpoints(grafx, srcExtents, srcShape, srcArrow,
							                          srcArrowSize, anchors, trgExtents, trgShape,
							                          trgArrow, trgArrowSize, floatBuff3, floatBuff4))
								continue;

							final float srcXAdj = floatBuff3[0];
							final float srcYAdj = floatBuff3[1];
							final float trgXAdj = floatBuff4[0];
							final float trgYAdj = floatBuff4[1];

							grafx.drawEdgeFull(srcArrow, srcArrowSize, srcArrowPaint, trgArrow,
							                   trgArrowSize, trgArrowPaint, srcXAdj, srcYAdj,
							                   anchors, trgXAdj, trgYAdj, thickness, edgeStroke, segPaint);

							// Take care of edge anchor rendering.
							if (anchors != null) {
								for (int k = 0; k < anchors.numAnchors(); k++) {
									final float anchorSize;

									if ((anchorSize = edgeDetails.getAnchorSize(edge, k)) > 0.0f) {
										anchors.getAnchor(k, floatBuff4, 0);
										grafx.drawNodeFull(GraphGraphics.SHAPE_RECTANGLE,
										                   (float) (floatBuff4[0] - (anchorSize / 2.0d)),
										                   (float) (floatBuff4[1] - (anchorSize / 2.0d)),
										                   (float) (floatBuff4[0] + (anchorSize / 2.0d)),
										                   (float) (floatBuff4[1] + (anchorSize / 2.0d)),
										                   edgeDetails.getAnchorPaint(edge, k), 0.0f, null, null);
									}
								}
							}

							// Take care of label rendering.
							if ((lodBits & LOD_EDGE_LABELS) != 0) {
								
								final int labelCount = edgeDetails.getLabelCount(edge);
								for (int labelInx = 0; labelInx < labelCount; labelInx++) {
									final String text = edgeDetails.getLabelText(edge);
									final Font font = edgeDetails.getLabelFont(edge);
									final double fontScaleFactor = edgeDetails.getLabelScaleFactor(edge);
									final Paint paint = edgeDetails.getLabelPaint(edge);
									final Position textAnchor = edgeDetails.getLabelTextAnchor(edge);
									final Position edgeAnchor = edgeDetails.getLabelEdgeAnchor(edge);
									final float offsetVectorX = edgeDetails.getLabelOffsetVectorX(edge);
									final float offsetVectorY = edgeDetails.getLabelOffsetVectorY(edge);
									final Justification justify;

									if (text.indexOf('\n') >= 0)
										justify = edgeDetails.getLabelJustify(edge);
									else
										justify = Justification.JUSTIFY_CENTER;

									final double edgeAnchorPointX;
									final double edgeAnchorPointY;

									final double edgeLabelWidth = edgeDetails.getLabelWidth(edge);

									// Note that we reuse the position enum here.  West == source and East == target
									// This is sort of safe since we don't provide an API for changing this
									// in any case.
									if (edgeAnchor == Position.WEST) {		edgeAnchorPointX = srcXAdj;   edgeAnchorPointY = srcYAdj;
									} else if (edgeAnchor == Position.EAST) { edgeAnchorPointX = trgXAdj; edgeAnchorPointY = trgYAdj;
									} else if (edgeAnchor == Position.CENTER) {
										if (!grafx.getEdgePath(srcArrow, srcArrowSize, trgArrow,
										              trgArrowSize, srcXAdj, srcYAdj, anchors,  trgXAdj, trgYAdj, path2d)) {
											continue;
										}

										// Count the number of path segments.  This count
										// includes the initial SEG_MOVETO.  So, for example, a
										// path composed of 2 cubic curves would have a numPaths
										// of 3.  Note that numPaths will be at least 2 in all
										// cases.
										final int numPaths;

										{
											final PathIterator pathIter = path2d.getPathIterator(null);
											int numPathsTemp = 0;

											while (!pathIter.isDone()) {
												numPathsTemp++; // pathIter.currentSegment().
												pathIter.next();
											}

											numPaths = numPathsTemp;
										}

										// Compute "midpoint" of edge.
										if ((numPaths % 2) != 0) {
											final PathIterator pathIter = path2d.getPathIterator(null);

											for (int i = numPaths / 2; i > 0; i--)
												pathIter.next();

											final int subPathType = pathIter.currentSegment(floatBuff5);

											if (subPathType == PathIterator.SEG_LINETO) {
												edgeAnchorPointX = floatBuff5[0];
												edgeAnchorPointY = floatBuff5[1];
											} else if (subPathType == PathIterator.SEG_QUADTO) {
												edgeAnchorPointX = floatBuff5[2];
												edgeAnchorPointY = floatBuff5[3];
											} else if (subPathType == PathIterator.SEG_CUBICTO) {
												edgeAnchorPointX = floatBuff5[4];
												edgeAnchorPointY = floatBuff5[5];
											} else
												throw new IllegalStateException("got unexpected PathIterator segment type: "
												                                + subPathType);
										} else { // numPaths % 2 == 0.

											final PathIterator pathIter = path2d.getPathIterator(null);

											for (int i = numPaths / 2; i > 0; i--) {
												if (i == 1) {
													final int subPathType = pathIter.currentSegment(floatBuff5);

													if ((subPathType == PathIterator.SEG_MOVETO)
													    || (subPathType == PathIterator.SEG_LINETO)) {
														floatBuff5[6] = floatBuff5[0];
														floatBuff5[7] = floatBuff5[1];
													} else if (subPathType == PathIterator.SEG_QUADTO) {
														floatBuff5[6] = floatBuff5[2];
														floatBuff5[7] = floatBuff5[3];
													} else if (subPathType == PathIterator.SEG_CUBICTO) {
														floatBuff5[6] = floatBuff5[4];
														floatBuff5[7] = floatBuff5[5];
													} else
														throw new IllegalStateException("got unexpected PathIterator segment type: "
														                                + subPathType);
												}

												pathIter.next();
											}

											final int subPathType = pathIter.currentSegment(floatBuff5);

											if (subPathType == PathIterator.SEG_LINETO) {
												edgeAnchorPointX = (0.5d * floatBuff5[6])
												                   + (0.5d * floatBuff5[0]);
												edgeAnchorPointY = (0.5d * floatBuff5[7])
												                   + (0.5d * floatBuff5[1]);
											} else if (subPathType == PathIterator.SEG_QUADTO) {
												edgeAnchorPointX = (0.25d * floatBuff5[6])
												                   + (0.5d * floatBuff5[0])
												                   + (0.25d * floatBuff5[2]);
												edgeAnchorPointY = (0.25d * floatBuff5[7])
												                   + (0.5d * floatBuff5[1])
												                   + (0.25d * floatBuff5[3]);
											} else if (subPathType == PathIterator.SEG_CUBICTO) {
												edgeAnchorPointX = (0.125d * floatBuff5[6])
												                   + (0.375d * floatBuff5[0])
												                   + (0.375d * floatBuff5[2])
												                   + (0.125d * floatBuff5[4]);
												edgeAnchorPointY = (0.125d * floatBuff5[7])
												                   + (0.375d * floatBuff5[1])
												                   + (0.375d * floatBuff5[3])
												                   + (0.125d * floatBuff5[5]);
											} else
												throw new IllegalStateException("got unexpected PathIterator segment type: "
												                                + subPathType);
										}
									} else
										throw new IllegalStateException("encountered an invalid EDGE_ANCHOR_* constant: "
										                                + edgeAnchor);

									final MeasuredLineCreator measuredText = 
										new MeasuredLineCreator(text,font,
										                         grafx.getFontRenderContextFull(),
										                         fontScaleFactor, 
										                         (lodBits&LOD_TEXT_AS_SHAPE)!= 0, 
										                         edgeLabelWidth);

									doubleBuff1[0] = -0.5d * measuredText.getMaxLineWidth();
									doubleBuff1[1] = -0.5d * measuredText.getTotalHeight(); 
									doubleBuff1[2] = 0.5d * measuredText.getMaxLineWidth(); 
									doubleBuff1[3] = 0.5d * measuredText.getTotalHeight(); 
									lemma_computeAnchor(textAnchor, doubleBuff1, doubleBuff2);

									final double textXCenter = edgeAnchorPointX - doubleBuff2[0]
									                           + offsetVectorX;
									final double textYCenter = edgeAnchorPointY - doubleBuff2[1]
									                           + offsetVectorY;
									TextRenderingUtils.renderHorizontalText(grafx, measuredText, 
									                                        font, fontScaleFactor,
									                                        (float) textXCenter,
									                                        (float) textYCenter,
									                                        justify, paint,
									                                        (lodBits
									                                        & LOD_TEXT_AS_SHAPE) != 0);
								}
							}
						}
					}

					nodeBuff.put(nodeSuid);
				}
			}
		}
		// Render nodes and labels.  A label is not necessarily on top of every
		// node; it is only on top of the node it belongs to.
		{
			SpacialIndex2DEnumerator<Long> nodeHits = netView.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
			// System.out.println("Rendering nodes: high detail = "+(lodBits & LOD_HIGH_DETAIL));
			// System.out.println("time: "+(System.currentTimeMillis()-start)+"ms");

			if ((lodBits & LOD_HIGH_DETAIL) == 0) { // Low detail.

				final int nodeHitCount = nodeHits.size();

				for (int i = 0; i < nodeHitCount; i++) {
					final View<CyNode> node = netView.getNodeView( nodeHits.nextExtents(floatBuff1) );

					if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3]))
						grafx.drawNodeLow(floatBuff1[0], floatBuff1[1], floatBuff1[2],
						                  floatBuff1[3], nodeDetails.getColorLowDetail(netView, node));
				}
			} else { // High detail.
				SpacialIndex2DEnumerator<Long> zHits = nodeHits;
				if (haveZOrder) {
					zHits = new SpacialEntry2DEnumeratorZSort(netView.getSpacialIndex2D(), nodeHits);
				}
				while (zHits.hasNext()) {
					final long node = zHits.nextExtents(floatBuff1);
					final View<CyNode> cyNode = netView.getNodeView(node);

					renderNodeHigh(netView, grafx, cyNode, floatBuff1, doubleBuff1, doubleBuff2,
							nodeDetails, lodBits, dependencies);

					// Take care of label rendering.
					if ((lodBits & LOD_NODE_LABELS) != 0) { // Potential label rendering.

						final int labelCount = nodeDetails.getLabelCount(cyNode);

						for (int labelInx = 0; labelInx < labelCount; labelInx++) {
							final String text = nodeDetails.getLabelText(cyNode);
							final Font font = nodeDetails.getLabelFont(cyNode);
							final double fontScaleFactor = nodeDetails.getLabelScaleFactor(cyNode);
							final Paint paint = nodeDetails.getLabelPaint(cyNode);
							final Position textAnchor = nodeDetails.getLabelTextAnchor(cyNode);
							final Position nodeAnchor = nodeDetails.getLabelNodeAnchor(cyNode);
							final float offsetVectorX = nodeDetails.getLabelOffsetVectorX(cyNode);
							final float offsetVectorY = nodeDetails.getLabelOffsetVectorY(cyNode);
							final Justification justify;

							if (text.indexOf('\n') >= 0)
								justify = nodeDetails.getLabelJustify(cyNode);
							else
								justify = Justification.JUSTIFY_CENTER;

							final double nodeLabelWidth = nodeDetails.getLabelWidth(cyNode);

							doubleBuff1[0] = floatBuff1[0];
							doubleBuff1[1] = floatBuff1[1];
							doubleBuff1[2] = floatBuff1[2];
							doubleBuff1[3] = floatBuff1[3];
							lemma_computeAnchor(nodeAnchor, doubleBuff1, doubleBuff2);

							final double nodeAnchorPointX = doubleBuff2[0];
							final double nodeAnchorPointY = doubleBuff2[1];
							final MeasuredLineCreator measuredText = new MeasuredLineCreator(
							    text, font, grafx.getFontRenderContextFull(), fontScaleFactor,
							    (lodBits & LOD_TEXT_AS_SHAPE) != 0, nodeLabelWidth);

							doubleBuff1[0] = -0.5d * measuredText.getMaxLineWidth();
							doubleBuff1[1] = -0.5d * measuredText.getTotalHeight();
							doubleBuff1[2] = 0.5d * measuredText.getMaxLineWidth();
							doubleBuff1[3] = 0.5d * measuredText.getTotalHeight();
							lemma_computeAnchor(textAnchor, doubleBuff1, doubleBuff2);

							final double textXCenter = nodeAnchorPointX - doubleBuff2[0]
							                           + offsetVectorX;
							final double textYCenter = nodeAnchorPointY - doubleBuff2[1]
							                           + offsetVectorY;
							TextRenderingUtils.renderHorizontalText(grafx, measuredText, font,
							                                        fontScaleFactor,
							                                        (float) textXCenter,
							                                        (float) textYCenter, justify,
							                                        paint,
							                                        (lodBits & LOD_TEXT_AS_SHAPE) != 0);
						}
					}
				}
			}
		}
		// System.out.println("total time: "+(System.currentTimeMillis()-start)+"ms");
		return lodBits;
	}

	private final static void lemma_computeAnchor(final Position anchor, final double[] input4x,
	                                              final double[] rtrn2x) {
		switch (anchor) {
			case CENTER:
				rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
				rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;

				break;

			case SOUTH:
				rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
				rtrn2x[1] = input4x[3];

				break;

			case SOUTH_EAST:
				rtrn2x[0] = input4x[2];
				rtrn2x[1] = input4x[3];

				break;

			case EAST:
				rtrn2x[0] = input4x[2];
				rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;

				break;

			case NORTH_EAST:
				rtrn2x[0] = input4x[2];
				rtrn2x[1] = input4x[1];

				break;

			case NORTH:
				rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
				rtrn2x[1] = input4x[1];

				break;

			case NORTH_WEST:
				rtrn2x[0] = input4x[0];
				rtrn2x[1] = input4x[1];

				break;

			case WEST:
				rtrn2x[0] = input4x[0];
				rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;

				break;

			case SOUTH_WEST:
				rtrn2x[0] = input4x[0];
				rtrn2x[1] = input4x[3];

				break;

			default:
				throw new IllegalStateException("encoutered an invalid ANCHOR_* constant: "
				                                + anchor);
		}
	}

	private final static float[] s_floatBuff = new float[2];

	/**
	 * Calculates the edge endpoints given two nodes, any edge anchors, and any arrows. 
	 *
	 * @param grafx The GraphGraphics being used to render everything. Used only to 
	 * calculate the edge intersection of the node.
	 * @param srcNodeExtents The extents of the source node.
	 * @param srcNodeShape The node shape type.
	 * @param srcArrow The source arrow type.
	 * @param srcArrowSize The source arrow size.
	 * @param anchors an EdgeAnchors object listing any anchors for the edge, possibly null.
	 * @param trgNodeExtents The extends of the target node.
	 * @param trgNodeShape The target node type.
	 * @param trgArrow The target arrow type.
	 * @param trgArrowSize The target arrow size.
	 * @param rtnValSrc The array where X,Y positions of the source end of the edge are stored. 
	 * @param rtnValTrg The array where X,Y positions of the target end of the edge are stored. 
	 *
	 * @return DOCUMENT ME!
	 */
	public final static boolean computeEdgeEndpoints(final GraphGraphics grafx,
	                                                 final float[] srcNodeExtents,
	                                                 final byte srcNodeShape, final ArrowShape srcArrow,
	                                                 final float srcArrowSize, EdgeAnchors anchors,
	                                                 final float[] trgNodeExtents,
	                                                 final byte trgNodeShape, final ArrowShape trgArrow,
	                                                 final float trgArrowSize,
	                                                 final float[] rtnValSrc,
	                                                 final float[] rtnValTrg) {

		final float srcX = (float) ((((double) srcNodeExtents[0]) + srcNodeExtents[2]) / 2.0d);
		final float srcY = (float) ((((double) srcNodeExtents[1]) + srcNodeExtents[3]) / 2.0d);
		final float trgX = (float) ((((double) trgNodeExtents[0]) + trgNodeExtents[2]) / 2.0d);
		final float trgY = (float) ((((double) trgNodeExtents[1]) + trgNodeExtents[3]) / 2.0d);
		final float srcXOut;
		final float srcYOut;
		final float trgXOut;
		final float trgYOut;

		final float[] floatBuff = new float[2];

		if ((anchors != null) && (anchors.numAnchors() == 0))
			anchors = null;

		if (anchors == null) {
			srcXOut = trgX;
			srcYOut = trgY;
			trgXOut = srcX;
			trgYOut = srcY;
		} else {
			anchors.getAnchor(0, floatBuff, 0);
			srcXOut = floatBuff[0];
			srcYOut = floatBuff[1];
			anchors.getAnchor(anchors.numAnchors() - 1, floatBuff, 0);
			trgXOut = floatBuff[0];
			trgYOut = floatBuff[1];
		}

		calcIntersection(grafx, srcNodeShape, srcNodeExtents, srcX, srcY, 
		                 srcXOut, srcYOut, floatBuff); 
		final float srcXAdj = floatBuff[0];
		final float srcYAdj = floatBuff[1];

		calcIntersection(grafx, trgNodeShape, trgNodeExtents, trgX, trgY, 
		                 trgXOut, trgYOut, floatBuff); 
		final float trgXAdj = floatBuff[0];
		final float trgYAdj = floatBuff[1];

		rtnValSrc[0] = srcXAdj;
		rtnValSrc[1] = srcYAdj;
		rtnValTrg[0] = trgXAdj;
		rtnValTrg[1] = trgYAdj;

		return true;
	}

	private static void calcIntersection(GraphGraphics grafx, byte nodeShape, 
	                                     float[] nodeExtents, float x, float y,
	                                     float xOut, float yOut, float[] retVal) {
		if ((nodeExtents[0] == nodeExtents[2]) || 
		    (nodeExtents[1] == nodeExtents[3])) {
			retVal[0] = x;
			retVal[1] = y;
		} else {
			if (!grafx.computeEdgeIntersection(nodeShape, nodeExtents[0],
			                                   nodeExtents[1], nodeExtents[2],
			                                   nodeExtents[3], 0.0f, xOut, yOut,
			                                   retVal)) {

				final float newXOut;
				final float newYOut;

				final double xCenter = (((double) nodeExtents[0]) + nodeExtents[2]) / 2.0d;
				final double yCenter = (((double) nodeExtents[1]) + nodeExtents[3]) / 2.0d;
				final double desiredDist = Math.max(((double) nodeExtents[2])
						                                    - nodeExtents[0],
						                                    ((double) nodeExtents[3])
						                                    - nodeExtents[1]);
				final double dX = xOut - xCenter;
				final double dY = yOut - yCenter;
				final double len = Math.sqrt((dX * dX) + (dY * dY));

				if (len == 0.0d) {
					newXOut = (float) (xOut + desiredDist);
					newYOut = yOut;
				} else {
					newXOut = (float) (((dX / len) * desiredDist) + xOut);
					newYOut = (float) (((dY / len) * desiredDist) + yOut);
				}

				grafx.computeEdgeIntersection(nodeShape, nodeExtents[0],
				                              nodeExtents[1], nodeExtents[2],
				                              nodeExtents[3], 0.0f, newXOut,
				                              newYOut, retVal);
			}
		}
	}

	private final static float[] s_floatTemp = new float[6];
	private final static int[] s_segTypeBuff = new int[200];
	private final static float[] s_floatBuff2 = new float[1200];

	/**
	 * DOCUMENT ME!
	 *
	 * @param origPath DOCUMENT ME!
	 * @param rtnVal DOCUMENT ME!
	 */
	public final static void computeClosedPath(final PathIterator origPath, final GeneralPath rtnVal) {
		synchronized (s_floatTemp) {
			// First fill our buffers with the coordinates and segment types.
			int segs = 0;
			int offset = 0;

			if ((s_segTypeBuff[segs++] = origPath.currentSegment(s_floatTemp)) != PathIterator.SEG_MOVETO)
				throw new IllegalStateException("expected a SEG_MOVETO at the beginning of origPath");

			for (int i = 0; i < 2; i++)
				s_floatBuff2[offset++] = s_floatTemp[i];

			origPath.next();

			while (!origPath.isDone()) {
				final int segType = origPath.currentSegment(s_floatTemp);
				s_segTypeBuff[segs++] = segType;

				if ((segType == PathIterator.SEG_MOVETO) || (segType == PathIterator.SEG_CLOSE))
					throw new IllegalStateException("did not expect SEG_MOVETO or SEG_CLOSE");

				// This is a rare case where I rely on the actual constant values
				// to do a computation efficiently.
				final int coordCount = segType * 2;

				for (int i = 0; i < coordCount; i++)
					s_floatBuff2[offset++] = s_floatTemp[i];

				origPath.next();
			}

			rtnVal.reset();
			offset = 0;

			// Now add the forward path to rtnVal.
			for (int i = 0; i < segs; i++) {
				switch (s_segTypeBuff[i]) {
					case PathIterator.SEG_MOVETO:
						rtnVal.moveTo(s_floatBuff2[offset++], s_floatBuff2[offset++]);

						break;

					case PathIterator.SEG_LINETO:
						rtnVal.lineTo(s_floatBuff2[offset++], s_floatBuff2[offset++]);

						break;

					case PathIterator.SEG_QUADTO:
						rtnVal.quadTo(s_floatBuff2[offset++], s_floatBuff2[offset++],
						              s_floatBuff2[offset++], s_floatBuff2[offset++]);

						break;

					default: // PathIterator.SEG_CUBICTO.
						rtnVal.curveTo(s_floatBuff2[offset++], s_floatBuff2[offset++],
						               s_floatBuff2[offset++], s_floatBuff2[offset++],
						               s_floatBuff2[offset++], s_floatBuff2[offset++]);

						break;
				}
			}

			// Now add the return path.
			for (int i = segs - 1; i > 0; i--) {
				switch (s_segTypeBuff[i]) {
					case PathIterator.SEG_LINETO:
						offset -= 2;
						rtnVal.lineTo(s_floatBuff2[offset - 2], s_floatBuff2[offset - 1]);

						break;

					case PathIterator.SEG_QUADTO:
						offset -= 4;
						rtnVal.quadTo(s_floatBuff2[offset], s_floatBuff2[offset + 1],
						              s_floatBuff2[offset - 2], s_floatBuff2[offset - 1]);

						break;

					default: // PathIterator.SEG_CUBICTO.
						offset -= 6;
						rtnVal.curveTo(s_floatBuff2[offset + 2], s_floatBuff2[offset + 3],
						               s_floatBuff2[offset], s_floatBuff2[offset + 1],
						               s_floatBuff2[offset - 2], s_floatBuff2[offset - 1]);

						break;
				}
			}

			rtnVal.closePath();
		}
	}

	static final boolean _computeEdgeIntersection(final float nodeX, final float nodeY,
	                                              final float offset, final float ptX,
	                                              final float ptY, final boolean alwaysCompute,
	                                              final float[] returnVal) {
		if (offset == 0.0f) {
			returnVal[0] = nodeX;
			returnVal[1] = nodeY;

			return true;
		} else {
			final double dX = ptX - nodeX;
			final double dY = ptY - nodeY;
			final double len = Math.sqrt((dX * dX) + (dY * dY));

			if (len < offset) {
				if (!alwaysCompute)
					return false;

				if (len == 0.0d) {
					returnVal[0] = offset + nodeX;
					returnVal[1] = nodeY;

					return true;
				}
			}

			returnVal[0] = (float) (((dX / len) * offset) + nodeX);
			returnVal[1] = (float) (((dY / len) * offset) + nodeY);

			return true;
		}
	}
	
	/**
	 * Render node view with details, including custom graphics.
	 */
	@SuppressWarnings("rawtypes")
	private static final void renderNodeHigh(final CyNetworkViewSnapshot netView,
											 final GraphGraphics grafx,
											 final View<CyNode> cyNode,
											 final float[] floatBuff1,
											 final double[] doubleBuff1,
											 final double[] doubleBuff2,
											 final NodeDetails nodeDetails,
											 final int lodBits,
											 final Set<VisualPropertyDependency<?>> dependencies) {
		Shape nodeShape = null;

		if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3])) {
						
			// Compute visual attributes that do not depend on LOD.
			final byte shape = nodeDetails.getShape(cyNode);
			final Paint fillPaint = nodeDetails.getFillPaint(cyNode);

			// Compute node border information.
			final float borderWidth;
			final Paint borderPaint;
			Stroke borderStroke = null;

			if ((lodBits & LOD_NODE_BORDERS) == 0) { // Not rendering borders.
				borderWidth = 0.0f;
				borderPaint = null;
			} else { // Rendering node borders.
				borderWidth = nodeDetails.getBorderWidth(cyNode);
				borderStroke = nodeDetails.getBorderStroke(cyNode);
				if (borderWidth == 0.0f)
					borderPaint = null;
				else
					borderPaint = nodeDetails.getBorderPaint(cyNode);
			}

			// Draw the node.
			nodeShape = grafx.drawNodeFull(shape, floatBuff1[0], floatBuff1[1], floatBuff1[2], floatBuff1[3], 
			                               fillPaint, borderWidth, borderStroke, borderPaint);
		}

		// Take care of custom graphic rendering.
		if ((lodBits & LOD_CUSTOM_GRAPHICS) != 0) {

			// draw any nested networks first
			final TexturePaint nestedNetworkPaint = nodeDetails.getNestedNetworkTexturePaint(netView, cyNode);
			if (nestedNetworkPaint != null) {
				doubleBuff1[0] = floatBuff1[0];
				doubleBuff1[1] = floatBuff1[1];
				doubleBuff1[2] = floatBuff1[2];
				doubleBuff1[3] = floatBuff1[3];
				lemma_computeAnchor(Position.CENTER, doubleBuff1, doubleBuff2);
				grafx.drawCustomGraphicImage(nestedNetworkPaint.getAnchorRect(), (float)doubleBuff2[0],  (float)doubleBuff2[1], nestedNetworkPaint); 
			}

			// draw custom graphics on top of nested networks 
			// don't allow our custom graphics to mutate while we iterate over them:
//			synchronized (nodeDetails.customGraphicsLock(cyNode)) {
			synchronized (nodeDetails) {
				
				// This method should return CustomGraphics in rendering order:
				final Map<VisualProperty<CyCustomGraphics>, CustomGraphicsInfo> cgMap = nodeDetails.getCustomGraphics(cyNode);
				final List<CustomGraphicsInfo> infoList = new ArrayList<>(cgMap.values());
				
				for (final CustomGraphicsInfo cgInfo : infoList) {
					final List<CustomGraphicLayer> layers = cgInfo.createLayers(netView, cyNode, nodeDetails, dependencies);
					
					// The graphic index used to retrieve non custom graphic info corresponds to the zero-based
					// index of the CustomGraphicLayer returned by the iterator:
					int graphicInx = 0;
					
					for (final CustomGraphicLayer layer : layers) {
						final float offsetVectorX = nodeDetails.graphicOffsetVectorX(cyNode);
						final float offsetVectorY = nodeDetails.graphicOffsetVectorY(cyNode);
						doubleBuff1[0] = floatBuff1[0];
						doubleBuff1[1] = floatBuff1[1];
						doubleBuff1[2] = floatBuff1[2];
						doubleBuff1[3] = floatBuff1[3];
						lemma_computeAnchor(Position.CENTER, doubleBuff1, doubleBuff2);
						
						float xOffset = (float) (doubleBuff2[0] + offsetVectorX);
						float yOffset = (float) (doubleBuff2[1] + offsetVectorY);
						nodeShape = createCustomGraphicsShape(nodeShape, layer, -xOffset, -yOffset);
						
						grafx.drawCustomGraphicFull(netView, cyNode, nodeShape, layer, xOffset, yOffset);
						graphicInx++;
					}
				}
			}
		}
	}

	private static Shape createCustomGraphicsShape(final Shape nodeShape, final CustomGraphicLayer layer,
			                                       float xOffset, float yOffset) {
		final Rectangle2D nsb = nodeShape.getBounds2D();
		final Rectangle2D cgb = layer.getBounds2D();
		
		final AffineTransform xform = new AffineTransform();
		xform.scale(cgb.getWidth() / nsb.getWidth(), cgb.getHeight() / nsb.getHeight());
		xform.translate(xOffset, yOffset);
		
		return xform.createTransformedShape(nodeShape);
	}

	private static class SpacialEntry2DEnumeratorZSort implements SpacialIndex2DEnumerator<Long> {
		private final Iterator<ZSpacialEntry> entries;
		private final int size;

		public SpacialEntry2DEnumeratorZSort(SpacialIndex2D<Long> nodePositions, SpacialIndex2DEnumerator<Long> nodeHits) {
			// Get arrays of SUIDs, extents, Z
			List<ZSpacialEntry> entryList = new ArrayList<>();
			while (nodeHits.hasNext()) {
				float[] extents = new float[4];
				long suid = nodeHits.nextExtents(extents);
				// Create an index
				ZSpacialEntry entry = new ZSpacialEntry(suid, extents, nodePositions.getZOrder(suid));
				entryList.add(entry);
			}
			Collections.sort(entryList);
			this.size = entryList.size();
			this.entries = entryList.iterator();
		}
		
		private static long copyExtents(float[] extents, ZSpacialEntry entry) {
			float[] entryExtents = entry.getExtents();
			extents[0] = entryExtents[0];
			extents[1] = entryExtents[1];
			extents[2] = entryExtents[2];
			extents[3] = entryExtents[3];
			return entry.getSUID();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean hasNext() {
			return entries.hasNext();
		}

		@Override
		public Long nextExtents(float[] extents) {
			return copyExtents(extents, entries.next());
		}
	}

	private static class ZSpacialEntry implements Comparable<ZSpacialEntry> {
		private long suid;
		private float[] extentsArr;
		private double z;

		public ZSpacialEntry(long suid, float[] extentsArr, double z) {
			this.suid = suid;
			this.extentsArr = extentsArr;
			this.z = z;
		}

		public long getSUID() { return this.suid; }
		public float[] getExtents() { return this.extentsArr; }
		public double getZ() { return this.z; }
		public String toString() { return "Suid: "+suid+" z = "+z; }

		public int compareTo(ZSpacialEntry other) {
			if (other == null || z == other.getZ()) return 0;
			if (z < other.getZ()) return -1;
			return 1;
		}
	}
}
