/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.graph.render.stateful;


import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.Iterator;

import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.spacial.SpacialEntry2DEnumerator;
import org.cytoscape.spacial.SpacialIndex2D;
import org.cytoscape.util.intr.LongHash;
import java.util.List;


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
	 * @param graph the graph topology; nodes in this graph must correspond to
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
	 * @return bits representing the level of detail that was rendered; the
	 *   return value is a bitwise-or'ed value of the LOD_* constants.
	 */
	public final static int renderGraph(final CyNetwork graph, final SpacialIndex2D nodePositions,
	                                    final GraphLOD lod, final NodeDetails nodeDetails,
	                                    final EdgeDetails edgeDetails, final LongHash nodeBuff,
	                                    final GraphGraphics grafx, final Paint bgPaint,
	                                    final double xCenter, final double yCenter,
	                                    final double scaleFactor) {
	
		nodeBuff.empty(); // Make sure we keep our promise.

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

		{
			final SpacialEntry2DEnumerator nodeHits = nodePositions.queryOverlap(xMin, yMin, xMax,
			                                                                     yMax, null, 0,
			                                                                     false);
			final int visibleNodeCount = nodeHits.numRemaining();
			final int totalNodeCount = graph.getNodeCount();
			final int totalEdgeCount = graph.getEdgeCount();
			renderEdges = lod.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);

			if (renderEdges > 0) {
				int runningNodeCount = 0;

				for (int i = 0; i < visibleNodeCount; i++) {
					nodeHits.nextExtents(floatBuff1, 0);

					if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3]))
						runningNodeCount++;
				}

				renderNodeCount = runningNodeCount;
				renderEdgeCount = totalEdgeCount;
			} else if (renderEdges < 0) {
				int runningNodeCount = 0;

				for (int i = 0; i < visibleNodeCount; i++) {
					nodeHits.nextExtents(floatBuff1, 0);

					if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3]))
						runningNodeCount++;
				}

				renderNodeCount = runningNodeCount;
				renderEdgeCount = 0;
			} else {
				int runningNodeCount = 0;
				int runningEdgeCount = 0;

				for (int i = 0; i < visibleNodeCount; i++) {
					final long node = nodeHits.nextExtents(floatBuff1, 0);

					if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3]))
						runningNodeCount++;

					final Iterable<CyEdge> touchingEdges = graph.getAdjacentEdgeIterable(graph.getNode(node),CyEdge.Type.ANY);

					for ( CyEdge e : touchingEdges ) {
						final long edge = e.getIndex(); 
						final long otherNode = node ^ e.getSource().getIndex() ^ e.getTarget().getIndex();

						if (nodeBuff.get(otherNode) < 0)
							runningEdgeCount++;
					}

					nodeBuff.put(node);
				}

				renderNodeCount = runningNodeCount;
				renderEdgeCount = runningEdgeCount;
				nodeBuff.empty();
			}
		}

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
			grafx.clear(bgPaint, xCenter, yCenter, scaleFactor);
		}

		// Render the edges first.  No edge shall be rendered twice.  Render edge
		// labels.  A label is not necessarily on top of every edge; it is only
		// on top of the edge it belongs to.
		if (renderEdges >= 0) {
			final SpacialEntry2DEnumerator nodeHits;

			if (renderEdges > 0)
				// We want to render edges in the same order (back to front) that
				// we would use to render just edges on visible nodes; this is assuming
				// that our spacial index has the subquery order-preserving property.
				nodeHits = nodePositions.queryOverlap(Float.NEGATIVE_INFINITY,
				                                      Float.NEGATIVE_INFINITY,
				                                      Float.POSITIVE_INFINITY,
				                                      Float.POSITIVE_INFINITY, null, 0, false);
			else
				nodeHits = nodePositions.queryOverlap(xMin, yMin, xMax, yMax, null, 0, false);
		
			if ((lodBits & LOD_HIGH_DETAIL) == 0) { // Low detail.

				final int nodeHitCount = nodeHits.numRemaining();

				for (int i = 0; i < nodeHitCount; i++) {
					final long node = nodeHits.nextExtents(floatBuff1, 0);

					// Casting to double and then back we could achieve better accuracy
					// at the expense of performance.
					final float nodeX = (floatBuff1[0] + floatBuff1[2]) / 2;
					final float nodeY = (floatBuff1[1] + floatBuff1[3]) / 2;

					Iterable<CyEdge> touchingEdges = graph.getAdjacentEdgeIterable(graph.getNode(node),CyEdge.Type.ANY);

					for ( CyEdge edge : touchingEdges ) {
						final long otherNode = node ^ edge.getSource().getIndex() ^ edge.getTarget().getIndex();

						if (nodeBuff.get(otherNode) < 0) { // Has not yet been rendered.
							nodePositions.exists(otherNode, floatBuff2, 0);
							grafx.drawEdgeLow(nodeX, nodeY, 
							                  // Again, casting issue - tradeoff between
							                  // accuracy and performance.
							                  (floatBuff2[0] + floatBuff2[2]) / 2,
							                  (floatBuff2[1] + floatBuff2[3]) / 2,
							                  edgeDetails.colorLowDetail(edge));
						}
					}

					nodeBuff.put(node);
				}
			} else { // High detail.
				while (nodeHits.numRemaining() > 0) {
					final long node = nodeHits.nextExtents(floatBuff1, 0);
					final CyNode cyNode = graph.getNode(node);
					final byte nodeShape = nodeDetails.shape(cyNode);
					Iterable<CyEdge> touchingEdges = graph.getAdjacentEdgeIterable(cyNode,CyEdge.Type.ANY);
					for ( CyEdge edge : touchingEdges ) {
						final long otherNode = node ^ edge.getSource().getIndex()
							^ edge.getTarget().getIndex();
						final CyNode otherCyNode = graph.getNode(otherNode);

						if (nodeBuff.get(otherNode) < 0) { // Has not yet been rendered.

							if (!nodePositions.exists(otherNode, floatBuff2, 0))
								throw new IllegalStateException("nodePositions not recognizing node that exists in graph");

							final byte otherNodeShape = nodeDetails.shape(otherCyNode);

							// Compute node shapes, center positions, and extents.
							final byte srcShape;

							// Compute node shapes, center positions, and extents.
							final byte trgShape;
							final float[] srcExtents;
							final float[] trgExtents;
							if (node == edge.getSource().getIndex()) {
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
							final float thickness = edgeDetails.segmentThickness(edge);
							final Stroke edgeStroke = edgeDetails.segmentStroke(edge);
							final Paint segPaint = edgeDetails.segmentPaint(edge);

							// Compute arrows.
							final byte srcArrow;

							// Compute arrows.
							final byte trgArrow;
							final float srcArrowSize;
							final float trgArrowSize;
							final Paint srcArrowPaint;
							final Paint trgArrowPaint;

							if ((lodBits & LOD_EDGE_ARROWS) == 0) { // Not rendering arrows.
								trgArrow = srcArrow = GraphGraphics.ARROW_NONE;
								trgArrowSize = srcArrowSize = 0.0f;
								trgArrowPaint = srcArrowPaint = null;
							} else { // Rendering edge arrows.
								srcArrow = edgeDetails.sourceArrow(edge);
								trgArrow = edgeDetails.targetArrow(edge);
								srcArrowSize = ((srcArrow == GraphGraphics.ARROW_NONE) 
								                 ? 0.0f
								                 : edgeDetails.sourceArrowSize(edge));
								trgArrowSize = ((trgArrow == GraphGraphics.ARROW_NONE)
								                 ? 0.0f
								                 : edgeDetails.targetArrowSize(edge));
								srcArrowPaint = ((srcArrow == GraphGraphics.ARROW_NONE)
								                 ? null : edgeDetails.sourceArrowPaint(edge));
								trgArrowPaint = ((trgArrow == GraphGraphics.ARROW_NONE)
								                 ? null : edgeDetails.targetArrowPaint(edge));
							}

							// Compute the anchors to use when rendering edge.
							final EdgeAnchors anchors = (((lodBits & LOD_EDGE_ANCHORS) == 0) ? null
							                                                                 : edgeDetails
							                                                                   .anchors(edge));

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

									if ((anchorSize = edgeDetails.anchorSize(edge, k)) > 0.0f) {
										anchors.getAnchor(k, floatBuff4, 0);
										grafx.drawNodeFull(GraphGraphics.SHAPE_RECTANGLE,
										                   (float) (floatBuff4[0]
										                   - (anchorSize / 2.0d)),
										                   (float) (floatBuff4[1]
										                   - (anchorSize / 2.0d)),
										                   (float) (floatBuff4[0]
										                   + (anchorSize / 2.0d)),
										                   (float) (floatBuff4[1]
										                   + (anchorSize / 2.0d)),
										                   edgeDetails.anchorPaint(edge, k), 0.0f,
										                   null);
									}
								}
							}

							// Take care of label rendering.
							if ((lodBits & LOD_EDGE_LABELS) != 0) {
								final int labelCount = edgeDetails.labelCount(edge);

								for (int labelInx = 0; labelInx < labelCount; labelInx++) {
									final String text = edgeDetails.labelText(edge, labelInx);
									final Font font = edgeDetails.labelFont(edge, labelInx);
									final double fontScaleFactor = edgeDetails.labelScaleFactor(edge,
									                                                            labelInx);
									final Paint paint = edgeDetails.labelPaint(edge, labelInx);
									final byte textAnchor = edgeDetails.labelTextAnchor(edge,
									                                                    labelInx);
									final byte edgeAnchor = edgeDetails.labelEdgeAnchor(edge,
									                                                    labelInx);
									final float offsetVectorX = edgeDetails.labelOffsetVectorX(edge,
									                                                           labelInx);
									final float offsetVectorY = edgeDetails.labelOffsetVectorY(edge,
									                                                           labelInx);
									final byte justify;

									if (text.indexOf('\n') >= 0)
										justify = edgeDetails.labelJustify(edge, labelInx);
									else
										justify = NodeDetails.LABEL_WRAP_JUSTIFY_CENTER;

									final double edgeAnchorPointX;
									final double edgeAnchorPointY;

									final double edgeLabelWidth = edgeDetails.labelWidth(edge);

									if (edgeAnchor == EdgeDetails.EDGE_ANCHOR_SOURCE) {
										edgeAnchorPointX = srcXAdj;
										edgeAnchorPointY = srcYAdj;
									} else if (edgeAnchor == EdgeDetails.EDGE_ANCHOR_TARGET) {
										edgeAnchorPointX = trgXAdj;
										edgeAnchorPointY = trgYAdj;
									} else if (edgeAnchor == EdgeDetails.EDGE_ANCHOR_MIDPOINT) {
										grafx.getEdgePath(srcArrow, srcArrowSize, trgArrow,
										                  trgArrowSize, srcXAdj, srcYAdj, anchors,
										                  trgXAdj, trgYAdj, path2d);

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

					nodeBuff.put(node);
				}
			}
		}
		// Render nodes and labels.  A label is not necessarily on top of every
		// node; it is only on top of the node it belongs to.
		{
			final SpacialEntry2DEnumerator nodeHits = nodePositions.queryOverlap(xMin, yMin, xMax,
			                                                                     yMax, null, 0,
			                                                                     false);

			if ((lodBits & LOD_HIGH_DETAIL) == 0) { // Low detail.

				final int nodeHitCount = nodeHits.numRemaining();

				for (int i = 0; i < nodeHitCount; i++) {
					final CyNode node = graph.getNode( nodeHits.nextExtents(floatBuff1, 0) );

					if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3]))
						grafx.drawNodeLow(floatBuff1[0], floatBuff1[1], floatBuff1[2],
						                  floatBuff1[3], nodeDetails.colorLowDetail(node));
				}
			} else { // High detail.
				while (nodeHits.numRemaining() > 0) {
					final long node = nodeHits.nextExtents(floatBuff1, 0);
					final CyNode cyNode = graph.getNode(node);
					
					renderNodeHigh(graph, grafx, node, cyNode, floatBuff1, doubleBuff1, doubleBuff2, nodeDetails, lodBits);
				

					// Take care of label rendering.
					if ((lodBits & LOD_NODE_LABELS) != 0) { // Potential label rendering.

						final int labelCount = nodeDetails.labelCount(cyNode);

						for (int labelInx = 0; labelInx < labelCount; labelInx++) {
							final String text = nodeDetails.labelText(cyNode, labelInx);
							final Font font = nodeDetails.labelFont(cyNode, labelInx);
							final double fontScaleFactor = nodeDetails.labelScaleFactor(cyNode,
							                                                            labelInx);
							final Paint paint = nodeDetails.labelPaint(cyNode, labelInx);
							final byte textAnchor = nodeDetails.labelTextAnchor(cyNode, labelInx);
							final byte nodeAnchor = nodeDetails.labelNodeAnchor(cyNode, labelInx);
							final float offsetVectorX = nodeDetails.labelOffsetVectorX(cyNode,
							                                                           labelInx);
							final float offsetVectorY = nodeDetails.labelOffsetVectorY(cyNode,
							                                                           labelInx);
							final byte justify;

							if (text.indexOf('\n') >= 0)
								justify = nodeDetails.labelJustify(cyNode, labelInx);
							else
								justify = NodeDetails.LABEL_WRAP_JUSTIFY_CENTER;

							final double nodeLabelWidth = nodeDetails.labelWidth(cyNode);

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
		return lodBits;
	}

	private final static void lemma_computeAnchor(final int anchor, final double[] input4x,
	                                              final double[] rtrn2x) {
		switch (anchor) {
			case NodeDetails.ANCHOR_CENTER:
				rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
				rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;

				break;

			case NodeDetails.ANCHOR_SOUTH:
				rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
				rtrn2x[1] = input4x[3];

				break;

			case NodeDetails.ANCHOR_SOUTHEAST:
				rtrn2x[0] = input4x[2];
				rtrn2x[1] = input4x[3];

				break;

			case NodeDetails.ANCHOR_EAST:
				rtrn2x[0] = input4x[2];
				rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;

				break;

			case NodeDetails.ANCHOR_NORTHEAST:
				rtrn2x[0] = input4x[2];
				rtrn2x[1] = input4x[1];

				break;

			case NodeDetails.ANCHOR_NORTH:
				rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
				rtrn2x[1] = input4x[1];

				break;

			case NodeDetails.ANCHOR_NORTHWEST:
				rtrn2x[0] = input4x[0];
				rtrn2x[1] = input4x[1];

				break;

			case NodeDetails.ANCHOR_WEST:
				rtrn2x[0] = input4x[0];
				rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;

				break;

			case NodeDetails.ANCHOR_SOUTHWEST:
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
	                                                 final byte srcNodeShape, final byte srcArrow,
	                                                 final float srcArrowSize, EdgeAnchors anchors,
	                                                 final float[] trgNodeExtents,
	                                                 final byte trgNodeShape, final byte trgArrow,
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
	 * 
	 * @param graph
	 * @param grafx
	 * @param node
	 * @param floatBuff1
	 * @param doubleBuff1
	 * @param doubleBuff2
	 * @param nodeDetails
	 * @param lodBits
	 */
	private static final void renderNodeHigh(final CyNetwork graph, final GraphGraphics grafx, 
			final long node, final CyNode cyNode, final float[] floatBuff1, final double[] doubleBuff1, 
			final double[] doubleBuff2, final NodeDetails nodeDetails, final int lodBits) {
		if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3])) {
						
			// Compute visual attributes that do not depend on LOD.
			final byte shape = nodeDetails.shape(cyNode);
			final Paint fillPaint = nodeDetails.fillPaint(cyNode);

			// Compute node border information.
			final float borderWidth;
			final Paint borderPaint;

			if ((lodBits & LOD_NODE_BORDERS) == 0) { // Not rendering borders.
				borderWidth = 0.0f;
				borderPaint = null;
			} else { // Rendering node borders.
				borderWidth = nodeDetails.borderWidth(cyNode);

				if (borderWidth == 0.0f)
					borderPaint = null;
				else
					borderPaint = nodeDetails.borderPaint(cyNode);
			}

			// Draw the node.
			grafx.drawNodeFull(shape, floatBuff1[0], floatBuff1[1], floatBuff1[2], floatBuff1[3], fillPaint, borderWidth, borderPaint);
		}

		// Take care of custom graphic rendering.
		if ((lodBits & LOD_CUSTOM_GRAPHICS) != 0) {

			// draw any nested networks first
			final TexturePaint nestedNetworkPaint = nodeDetails.getNestedNetworkTexturePaint(cyNode);
			if (nestedNetworkPaint != null) {
				doubleBuff1[0] = floatBuff1[0];
				doubleBuff1[1] = floatBuff1[1];
				doubleBuff1[2] = floatBuff1[2];
				doubleBuff1[3] = floatBuff1[3];
				lemma_computeAnchor(NodeDetails.ANCHOR_CENTER, doubleBuff1, doubleBuff2);
				grafx.drawCustomGraphicFull(nestedNetworkPaint.getAnchorRect(), (float)doubleBuff2[0],  (float)doubleBuff2[1], nestedNetworkPaint); 
			}

			// draw custom graphics on top of nested networks 
			// don't allow our custom graphics to mutate while we iterate over them:
			synchronized (nodeDetails.customGraphicsLock(cyNode)) {
				// This iterator will return CustomGraphics in rendering order:
				Iterator<CustomGraphic> dNodeIt = nodeDetails.customGraphics(cyNode);
				CustomGraphic cg = null;
				// The graphic index used to retrieve non custom graphic info corresponds to the zero-based
				// index of the CustomGraphic returned by the iterator:
				int graphicInx = 0;
				while (dNodeIt.hasNext()) {
					cg = dNodeIt.next();
					final float offsetVectorX = nodeDetails.graphicOffsetVectorX(cyNode, graphicInx);
					final float offsetVectorY = nodeDetails.graphicOffsetVectorY(cyNode, graphicInx);
					doubleBuff1[0] = floatBuff1[0];
					doubleBuff1[1] = floatBuff1[1];
					doubleBuff1[2] = floatBuff1[2];
					doubleBuff1[3] = floatBuff1[3];
					lemma_computeAnchor(NodeDetails.ANCHOR_CENTER, doubleBuff1, doubleBuff2);
					grafx.drawCustomGraphicFull(cg.getShape(), (float) (doubleBuff2[0] + offsetVectorX), (float) (doubleBuff2[1] + offsetVectorY),
								    cg.getPaint());
					graphicInx++;
				}
			}
		}
	}
}
