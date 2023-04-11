package org.cytoscape.graph.render.stateful;

import static org.cytoscape.graph.render.stateful.RenderDetailFlags.*;

import java.awt.Color;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.impl.work.DiscreteProgressMonitor;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.ding.internal.util.MurmurHash3;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphLOD.RenderEdges;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.spacial.EdgeSpacialIndex2DEnumerator;
import org.cytoscape.view.model.spacial.NodeSpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.EdgeStackingVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.EdgeStacking;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.Position;
import org.cytoscape.view.vizmap.VisualPropertyDependency;


/**
 * This class contains a chunk of procedural code that stitches together
 * several external modules in an effort to efficiently render graphs.
 */
public final class GraphRenderer {
	
	
	public static void renderEdges(ProgressMonitor pm, GraphGraphics grafx, CyNetworkViewSnapshot netView,
			RenderDetailFlags flags, NodeDetails nodeDetails, EdgeDetails edgeDetails,
			LabelInfoProvider labelInfoProvider) {
		
		// Render the edges first.  No edge shall be rendered twice.  Render edge labels.  
		// A label is not necessarily on top of every edge; it is only on top of the edge it belongs to.

		if(flags.renderEdges() == RenderEdges.NONE) {
			return;
		}
		
		final float[] floatBuff1 = new float[4];
		final float[] floatBuff2 = new float[4];
		final float[] floatBuff3 = new float[2];
		final float[] floatBuff4 = new float[2];
		final float[] floatBuff5 = new float[8];
		final double[] doubleBuff1 = new double[4];
		final double[] doubleBuff2 = new double[2];
		final double[] offsetBuff = new double[2];
		final GeneralPath path2d = new GeneralPath();
		
		EdgeSpacialIndex2DEnumerator edgeHits;
		Rectangle2D.Float area = grafx.getTransform().getNetworkVisibleAreaNodeCoords();
		
		if (flags.renderEdges() == RenderEdges.ALL)
			edgeHits = netView.getSpacialIndex2D().queryAllEdges(pm::isCancelled);
		else
			edgeHits = netView.getSpacialIndex2D().queryOverlapEdges(area.x, area.y, area.x + area.width, area.y + area.height, pm::isCancelled);
		
		if(edgeHits == null) // cancelled
			return;
		
		if (flags.not(LOD_HIGH_DETAIL)) { // Low detail.

			ProgressMonitor[] subPms = pm.split(1,0); // no labels at all, still need labelPm for debug panel
			ProgressMonitor shapePm = subPms[0];
			ProgressMonitor labelPm = subPms[1];
			
			shapePm.start("Line");
			DiscreteProgressMonitor shapeDpm = shapePm.toDiscrete(edgeHits.size());
			
			while(edgeHits.hasNext()) {
				if(pm.isCancelled()) {
					return;
				}
				
				View<CyEdge> edge = edgeHits.nextEdgeWithNodeExtents(floatBuff1, floatBuff2, null);
				
				float sourceNodeX = (floatBuff1[0] + floatBuff1[2]) / 2;
				float sourceNodeY = (floatBuff1[1] + floatBuff1[3]) / 2;
				float targetNodeX = (floatBuff2[0] + floatBuff2[2]) / 2;
				float targetNodeY = (floatBuff2[1] + floatBuff2[3]) / 2;
				
				Color color = edgeDetails.getColorLowDetail(netView, edge);
				grafx.drawEdgeLow(sourceNodeX, sourceNodeY, targetNodeX, targetNodeY, color);
				
				shapeDpm.increment();
			}
			
			shapePm.done();
			labelPm.emptyTask("Label");
			
		} else { // High detail.
			
			ProgressMonitor[] subPms = pm.split(1,1); // labels usually take longer
			ProgressMonitor shapePm = subPms[0];
			ProgressMonitor labelPm = subPms[1];
			DiscreteProgressMonitor shapeDpm = shapePm.toDiscrete(edgeHits.size());
			DiscreteProgressMonitor labelDpm = labelPm.toDiscrete(edgeHits.size());
			
			byte[] haystackDataBuff = new byte[16];
			@SuppressWarnings("unchecked")
			View<CyNode>[] nodeBuff = new View[2];
			
			while (edgeHits.hasNext()) {
				if(pm.isCancelled())
					return;
				
				// Hidden edges are not included in the results
				View<CyEdge> edge = edgeHits.nextEdgeWithNodeExtents(floatBuff1, floatBuff2, nodeBuff);
					
				SnapshotEdgeInfo edgeInfo = netView.getEdgeInfo(edge);
				final long srcSuid = edgeInfo.getSourceViewSUID();
				final long trgSuid = edgeInfo.getTargetViewSUID();
				final View<CyNode> sourceNode = nodeBuff[0];
				final View<CyNode> targetNode = nodeBuff[1];
				final byte srcShape = nodeDetails.getShape(sourceNode);
				final byte trgShape = nodeDetails.getShape(targetNode);
				
				shapePm.start("Line");
				final EdgeStacking stacking = edgeDetails.getStacking(edge);

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

				if (flags.not(LOD_EDGE_ARROWS) || stacking == EdgeStackingVisualProperty.HAYSTACK) { // Not rendering arrows.
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
				final EdgeAnchors anchors = flags.not(LOD_EDGE_ANCHORS) ? null : edgeDetails.getAnchors(netView, edge);

				if(stacking == EdgeStackingVisualProperty.HAYSTACK) {
					float radiusModifier = edgeDetails.getStackingDensity(edge);
					computeEdgeEndpointsHaystack(floatBuff1, floatBuff2, srcSuid, trgSuid, edgeInfo.getSUID(), radiusModifier, stacking, 
							floatBuff3, floatBuff4, haystackDataBuff);
				} else /* auto bend */ {
					computeEdgeEndpoints(floatBuff1, srcShape, srcArrow, srcArrowSize, anchors, floatBuff2, 
							trgShape,  trgArrow, trgArrowSize, floatBuff3, floatBuff4);
				}
				
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
							anchors.getAnchor(k, floatBuff4);
							grafx.drawNodeFull(GraphGraphics.SHAPE_RECTANGLE,
							                   (float) (floatBuff4[0] - (anchorSize / 2.0d)),
							                   (float) (floatBuff4[1] - (anchorSize / 2.0d)),
							                   (float) (floatBuff4[0] + (anchorSize / 2.0d)),
							                   (float) (floatBuff4[1] + (anchorSize / 2.0d)),
							                   edgeDetails.getAnchorPaint(edge, k), 0.0f, null, null);
						}
					}
				}

				shapePm.done();
				
				labelPm.start("Label");
				
				// Take care of label rendering.
				if (flags.has(LOD_EDGE_LABELS) && !(stacking == EdgeStackingVisualProperty.HAYSTACK)) {
					final int labelCount = edgeDetails.getLabelCount(edge);
					for (int labelInx = 0; labelInx < labelCount; labelInx++) {
						if(pm.isCancelled()) {
							return;
						}

            final String text = edgeDetails.getLabelText(edge);
            final Font font = edgeDetails.getLabelFont(edge, flags.has(OPT_PDF_FONT_HACK));
            final Paint paint = edgeDetails.getLabelPaint(edge);
            final double rise = floatBuff4[1]-floatBuff3[1];
            final double run = floatBuff4[0]-floatBuff3[0];
            final double theta = edgeDetails.getLabelRotation(edge, rise, run)*.01745329252;
            final Paint backgroundPaint = edgeDetails.getLabelBackgroundPaint(edge);
            final byte backgroundShape = edgeDetails.getLabelBackgroundShape(edge);
            final double edgeLabelWidth = edgeDetails.getLabelWidth(edge);

            LabelInfo labelInfo = labelInfoProvider.getLabelInfo(text, font, edgeLabelWidth, grafx.getFontRenderContextFull());

            getEdgeLabelPosition(edge, edgeDetails, flags, labelInfo, floatBuff3, floatBuff4, anchors, offsetBuff, doubleBuff1);

						final double textXCenter = doubleBuff1[0];
						final double textYCenter = doubleBuff1[1];
            final double edgeAnchorPointX = doubleBuff1[2];
            final double edgeAnchorPointY = doubleBuff1[3];

            // System.out.println("textCenter = "+textXCenter+","+textYCenter);
            // System.out.println("edgeAnchorPoint = "+edgeAnchorPointX+","+edgeAnchorPointY);

            final Justification justify = edgeDetails.getLabelJustify(edge);

						renderText(grafx, labelInfo, (float) textXCenter, (float) textYCenter,
								textXCenter, textYCenter, justify, paint, backgroundPaint, backgroundShape, theta, flags.has(LOD_TEXT_AS_SHAPE));
					}
				}
				
				labelPm.done();

				shapeDpm.increment();
				labelDpm.increment();
			}
		}
	}

  public static void getEdgeLabelPosition(View<CyEdge> edge, EdgeDetails edgeDetails, RenderDetailFlags flags, 
                                          LabelInfo labelInfo,
                                          float[] floatBuff3, float[] floatBuff4, EdgeAnchors anchors, double[] offsetBuff,
                                          double[] returnBuff) {

    final String text = edgeDetails.getLabelText(edge);
    final Font font = edgeDetails.getLabelFont(edge, flags.has(OPT_PDF_FONT_HACK));
    final Position textAnchor = edgeDetails.getLabelTextAnchor(edge);
    final Position edgeAnchor = edgeDetails.getLabelEdgeAnchor(edge);
    float offsetVectorX = edgeDetails.getLabelOffsetVectorX(edge);
    float offsetVectorY = edgeDetails.getLabelOffsetVectorY(edge);
		final float thickness = (float) edgeDetails.getWidth(edge);
    final double rise = floatBuff4[1]-floatBuff3[1];
    final double run = floatBuff4[0]-floatBuff3[0];
    final double slope = rise/run;
    final double lineAngle = Math.atan2(rise, run);
    final double theta = edgeDetails.getLabelRotation(edge, rise, run)*.01745329252;
    final Justification justify = edgeDetails.getLabelJustify(edge);
		final GeneralPath path2d = new GeneralPath();
		final float[] floatBuff5 = new float[8];
		final double[] doubleBuff1 = new double[4];
    final double[] anchorBuff = new double[2];


    final double edgeAnchorPointX;
    final double edgeAnchorPointY;

    final float srcXAdj = floatBuff3[0];
    final float srcYAdj = floatBuff3[1];
    final float trgXAdj = floatBuff4[0];
    final float trgYAdj = floatBuff4[1];

    // Note that we reuse the position enum here.  West == source and East == target
    // This is sort of safe since we don't provide an API for changing this
    // in any case.
		// Handle above/below...
		if (edgeAnchor == Position.NORTH || edgeAnchor == Position.NORTH_WEST || edgeAnchor == Position.NORTH_EAST) {
			// We sort of "fake" this by adding the edge width to the offsetVectorY.  That way, any trigonometry only needs
			// to get done once
			offsetVectorY = offsetVectorY - thickness/2f;
		} else if (edgeAnchor == Position.SOUTH || edgeAnchor == Position.SOUTH_WEST || edgeAnchor == Position.SOUTH_EAST) {
			offsetVectorY = offsetVectorY + thickness/2f;
		}
    if (edgeAnchor == Position.WEST || edgeAnchor == Position.SOUTH_WEST || edgeAnchor == Position.NORTH_WEST) {
      edgeAnchorPointX = srcXAdj; 
      edgeAnchorPointY = srcYAdj;
    } else if (edgeAnchor == Position.EAST || edgeAnchor == Position.SOUTH_EAST || edgeAnchor == Position.NORTH_EAST) { 
      edgeAnchorPointX = trgXAdj; 
      edgeAnchorPointY = trgYAdj;
    } else if (edgeAnchor == Position.CENTER || edgeAnchor == Position.SOUTH || edgeAnchor == Position.NORTH) {
      // Compute arrows.
      final ArrowShape srcArrow;
      final ArrowShape trgArrow;
      final float srcArrowSize;
      final float trgArrowSize;
      final Paint srcArrowPaint;
      final Paint trgArrowPaint;

      if (flags.not(LOD_EDGE_ARROWS)) { // Not rendering arrows.
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
      if (!GraphGraphics.getEdgePath(srcArrow, srcArrowSize, trgArrow,
                    trgArrowSize, srcXAdj, srcYAdj, anchors,  trgXAdj, trgYAdj, path2d)) {
        return;
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
          throw new IllegalStateException("got unexpected PathIterator segment type: " + subPathType);
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
              throw new IllegalStateException("got unexpected PathIterator segment type: " + subPathType);
          }

          pathIter.next();
        }

        final int subPathType = pathIter.currentSegment(floatBuff5);

        if (subPathType == PathIterator.SEG_LINETO) {
          edgeAnchorPointX = (0.5d * floatBuff5[6]) + (0.5d * floatBuff5[0]);
          edgeAnchorPointY = (0.5d * floatBuff5[7]) + (0.5d * floatBuff5[1]);
        } else if (subPathType == PathIterator.SEG_QUADTO) {
          edgeAnchorPointX = (0.25d * floatBuff5[6]) + (0.5d * floatBuff5[0]) + (0.25d * floatBuff5[2]);
          edgeAnchorPointY = (0.25d * floatBuff5[7]) + (0.5d * floatBuff5[1]) + (0.25d * floatBuff5[3]);
        } else if (subPathType == PathIterator.SEG_CUBICTO) {
          edgeAnchorPointX = (0.125d * floatBuff5[6]) + (0.375d * floatBuff5[0]) + (0.375d * floatBuff5[2]) + (0.125d * floatBuff5[4]);
          edgeAnchorPointY = (0.125d * floatBuff5[7]) + (0.375d * floatBuff5[1]) + (0.375d * floatBuff5[3]) + (0.125d * floatBuff5[5]);
        } else
          throw new IllegalStateException("got unexpected PathIterator segment type: " + subPathType);
      }
    } else
      throw new IllegalStateException("encountered an invalid EDGE_ANCHOR_* constant: " + edgeAnchor);

    doubleBuff1[0] = -0.5d * labelInfo.getMaxLineWidth();
    doubleBuff1[1] = -0.5d * labelInfo.getTotalHeight(); 
    doubleBuff1[2] =  0.5d * labelInfo.getMaxLineWidth(); 
    doubleBuff1[3] =  0.5d * labelInfo.getTotalHeight(); 
    computeAnchor(textAnchor, doubleBuff1, anchorBuff);

    updateOffset(offsetVectorX, offsetVectorY, slope, lineAngle, offsetBuff);

    // System.out.println("offsetVectorX,Y="+offsetVectorX+","+offsetVectorY+" offsetBuff="+offsetBuff[0]+","+offsetBuff[1]);

    returnBuff[0] = edgeAnchorPointX - anchorBuff[0] + offsetBuff[0];
    returnBuff[1] = edgeAnchorPointY - anchorBuff[1] + offsetBuff[1];
    returnBuff[2] = edgeAnchorPointX;
    returnBuff[3] = edgeAnchorPointY;

    return;

  }

  public static void reverseOffset(double xOffset, double yOffset, double slope, double lineAngle, double[] xy) {
    // We have two equations and two unknowns:
    // (1) a*xy[0]+b*xy[1] = e
    // (2) c*xy[0]+d*xy[1] = f
    // where a = Math.cos(angle)
    //       b = Math.sqrt(1/(1+Math.pow((-1/slope),2.0)))
    //       c = Math.sin(angle)
    //       d = -1/slope * b

    // Deal with our "flipping"
    // double deg = Math.toDegrees(lineAngle);
    // if (deg > 0 && deg < 90)
    //   yOffset = -yOffset;
    // if (deg < -90 && deg > -180)
    //   yOffset = -yOffset;

    double perpSlope = -1/slope;
    double a = Math.cos(lineAngle);
    double b = Math.sqrt(1/(1+Math.pow(perpSlope,2.0)));
    double c = Math.sin(lineAngle);
    double d = perpSlope*b;
    double e = xOffset;
    double f = yOffset;

    // Solve using Cramer's method
    double determinant = a*d - b*c;
    if (determinant != 0) {
        xy[0] = (e*d - b*f)/determinant;
        xy[1] = (a*f - e*c)/determinant;
        // Deal with our "flipping"
        double deg = Math.toDegrees(lineAngle);
        if (deg > 0 && deg < 90)
          xy[1] = -xy[1];
        if (deg < -90 && deg > -180)
          xy[1] = -xy[1];
        return;
    }

    return;
  }

  public static void updateOffset(double xOffset, double yOffset, double slope, double lineAngle, double[] xy) {
    // (1) a*xOffset+b*yOffset = xy[0]
    // (2) c*xOffset+d*yOffset = xy[1]
    // where a = Math.cos(angle)
    //       b = Math.sqrt(1/(1+Math.pow((-1/slope),2.0)))
    //       c = Math.sin(angle)
    //       d = -1/slope * b
    // However, we do want to recognize that yOffset < 0 should always be above the edge and
    // yOffset > 0 should be below the edge
    double deg = Math.toDegrees(lineAngle);
    if (deg > 0 && deg < 90)
      yOffset = -yOffset;
    if (deg < -90 && deg > -180)
      yOffset = -yOffset;

    double perpSlope = -1/slope;
    double a = Math.cos(lineAngle);
    double b = Math.sqrt(1/(1+Math.pow(perpSlope,2.0)));
    double c = Math.sin(lineAngle);
    double d = perpSlope*b;

    xy[0] = a*xOffset + b*yOffset;
    xy[1] = c*xOffset + d*yOffset;

    // xy[1] = Math.copySign(xy[1], yOffset); // Make sure the sign is consistent

    return;
  }
	
	public static void renderNodes(ProgressMonitor pm, GraphGraphics grafx, CyNetworkViewSnapshot netView,
			RenderDetailFlags flags, NodeDetails nodeDetails, Set<VisualPropertyDependency<?>> dependencies, 
			LabelInfoProvider labelInfoProvider) {
		
		// Render nodes and labels.  A label is not necessarily on top of every
		// node; it is only on top of the node it belongs to.
		final float[] floatBuff1 = new float[4];
		final double[] doubleBuff1 = new double[4];
		final double[] doubleBuff2 = new double[2];
		
		Rectangle2D.Float area = grafx.getTransform().getNetworkVisibleAreaNodeCoords();
		NodeSpacialIndex2DEnumerator nodeHits = netView.getSpacialIndex2D().queryOverlapNodes(area.x, area.y, area.x + area.width, area.y + area.height, pm::isCancelled);
		
		if(nodeHits == null) // cancelled
			return;
		
		if (flags.not(LOD_HIGH_DETAIL)) { // Low detail.
			
			ProgressMonitor[] subPms = pm.split(1,0); // no labels at all, still need labelPm for debug panel
			ProgressMonitor shapePm = subPms[0];
			ProgressMonitor labelPm = subPms[1];
			
			shapePm.start("Shape");
			final int nodeHitCount = nodeHits.size();
			
			
			DiscreteProgressMonitor shapeDpm = shapePm.toDiscrete(nodeHitCount);

			for (int i = 0; i < nodeHitCount; i++) {
				if(pm.isCancelled())
					return;
				
				View<CyNode> node = nodeHits.nextNodeExtents(floatBuff1);
				
				if ((floatBuff1[0] != floatBuff1[2]) && (floatBuff1[1] != floatBuff1[3])) {
					Color color = nodeDetails.getColorLowDetail(netView, node);
					grafx.drawNodeLow(floatBuff1[0], floatBuff1[1], floatBuff1[2], floatBuff1[3], color);
				}
				shapeDpm.increment();
			}
			
			shapePm.done();
			labelPm.emptyTask("Label");
			
		} else { // High detail.
			
			ProgressMonitor[] subPms = pm.split(1,2); // labels usually take longer
			ProgressMonitor shapePm = subPms[0];
			ProgressMonitor labelPm = subPms[1];
			DiscreteProgressMonitor shapeDpm = shapePm.toDiscrete(nodeHits.size());
			DiscreteProgressMonitor labelDpm = labelPm.toDiscrete(nodeHits.size());
			
			while (nodeHits.hasNext()) {
				if(pm.isCancelled())
					return;
				
				View<CyNode> node = nodeHits.nextNodeExtents(floatBuff1);
				
				shapePm.start("Shape");
				
				renderNodeHigh(netView, grafx, node, floatBuff1, doubleBuff1, doubleBuff2, nodeDetails, flags, dependencies);

				shapeDpm.increment();
				shapePm.done();
				
				labelPm.start("Label");
				
				// Take care of label rendering.
				if (flags.has(LOD_NODE_LABELS)) { // Potential label rendering.
					final String text = nodeDetails.getLabelText(node);
					
					if(text != null && !text.isEmpty()) {
						final Font font = nodeDetails.getLabelFont(node, flags.has(OPT_PDF_FONT_HACK));
						final Paint paint = nodeDetails.getLabelPaint(node);
						final Position textAnchor = nodeDetails.getLabelTextAnchor(node);
						final Position nodeAnchor = nodeDetails.getLabelNodeAnchor(node);
						final float offsetVectorX = nodeDetails.getLabelOffsetVectorX(node);
						final float offsetVectorY = nodeDetails.getLabelOffsetVectorY(node);
						final double theta = nodeDetails.getLabelRotation(node)*.01745329252;
						final double nodeLabelWidth = nodeDetails.getLabelWidth(node);
						final Justification justify = nodeDetails.getLabelJustify(node);
						final Paint backgroundPaint = nodeDetails.getLabelBackgroundPaint(node);
						final byte backgroundShape = nodeDetails.getLabelBackgroundShape(node);
						
						doubleBuff1[0] = floatBuff1[0];
						doubleBuff1[1] = floatBuff1[1];
						doubleBuff1[2] = floatBuff1[2];
						doubleBuff1[3] = floatBuff1[3];
						computeAnchor(nodeAnchor, doubleBuff1, doubleBuff2);

						final double nodeAnchorPointX = doubleBuff2[0];
						final double nodeAnchorPointY = doubleBuff2[1];
						
						LabelInfo labelInfo = labelInfoProvider.getLabelInfo(text, font, nodeLabelWidth, grafx.getFontRenderContextFull());

						doubleBuff1[0] = -0.5d * labelInfo.getMaxLineWidth();
						doubleBuff1[1] = -0.5d * labelInfo.getTotalHeight();
						doubleBuff1[2] =  0.5d * labelInfo.getMaxLineWidth();
						doubleBuff1[3] =  0.5d * labelInfo.getTotalHeight();
						computeAnchor(textAnchor, doubleBuff1, doubleBuff2);

						final double textXCenter = nodeAnchorPointX - doubleBuff2[0] + offsetVectorX;
						final double textYCenter = nodeAnchorPointY - doubleBuff2[1] + offsetVectorY;
						
						renderText(grafx, labelInfo, (float) textXCenter,(float) textYCenter, nodeAnchorPointX+offsetVectorX,
                       nodeAnchorPointY+offsetVectorY, justify, paint, backgroundPaint, backgroundShape, theta, flags.has(LOD_TEXT_AS_SHAPE));

					}
				}
				
				labelDpm.increment();
				labelPm.done();
			}
		}
	}
	
	
	public final static void renderText(final GraphGraphics grafx, final LabelInfo measuredText,
			final float textXCenter, final float textYCenter,
			final double textXAnchor, final double textYAnchor,
			final Justification textJustify,
			final Paint paint, final Paint backgroundPaint, final byte backgroundShape, final double theta, final boolean textAsShape) {

		double currHeight = measuredText.getTotalHeight() / -2.0d;
		final double overallWidth = measuredText.getMaxLineWidth();

		for (LabelLineInfo line : measuredText.getMeasuredLines()) {
			final double yCenter = currHeight + textYCenter + (line.getHeight() / 2.0d);
			final double xCenter;

			if (textJustify == Justification.JUSTIFY_CENTER)
				xCenter = textXCenter;
			else if (textJustify == Justification.JUSTIFY_LEFT)
				xCenter = (-0.5d * (overallWidth - line.getWidth())) + textXCenter;
			else if (textJustify == Justification.JUSTIFY_RIGHT)
				xCenter = (0.5d * (overallWidth - line.getWidth())) + textXCenter;
			else
				throw new IllegalStateException("textJustify value unrecognized");

			grafx.drawTextFull(line, (float) xCenter, (float) yCenter, textXAnchor, textYAnchor, (float) theta, paint, backgroundPaint, backgroundShape, textAsShape);
			currHeight += line.getHeight();
		}
	}
	
	
	/**
	 * 
	 * @param anchor
	 * @param input4x An array of 4 elements: x0,y0,x1, y1 of a rectangle
	 * @param rtrn2x  An array of 2 element. x and y coordinates of the center of
	 *                the object.
	 */
	public final static void computeAnchor(final Position anchor, final double[] input4x, final double[] rtrn2x) {
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
			throw new IllegalStateException("encoutered an invalid ANCHOR_* constant: " + anchor);
		}
	}

	public final static void computeEdgeEndpointsHaystack(
			float[] srcNodeExtents, float[] trgNodeExtents,
			long srcSuid, long trgSuid, long edgeSuid,
			float radiusModifier, EdgeStacking stacking,
			float[] rtnValSrc, float[] rtnValTrg, byte[] dataBuff) {

		// center coordinates of source and target nodes
		final float x0 = (srcNodeExtents[0] + srcNodeExtents[2]) / 2.0f;
		final float y0 = (srcNodeExtents[1] + srcNodeExtents[3]) / 2.0f;
		final float x1 = (trgNodeExtents[2] + trgNodeExtents[0]) / 2.0f;
		final float y1 = (trgNodeExtents[3] + trgNodeExtents[1]) / 2.0f;
		
		final float srcWidth  = srcNodeExtents[2] - srcNodeExtents[0];
		final float srcHeight = srcNodeExtents[3] - srcNodeExtents[1];
		final float srcRadius = (Math.min(srcWidth, srcHeight) / 2.0f) * 0.9f * radiusModifier;
		
		final float trgWidth  = trgNodeExtents[2] - trgNodeExtents[0];
		final float trgHeight = trgNodeExtents[3] - trgNodeExtents[1];
		final float trgRadius = (Math.min(trgWidth, trgHeight) / 2.0f) * 0.9f * radiusModifier;
		
		crossHaystackEndpoint(x0, y0, srcRadius, rtnValSrc, dataBuff, srcSuid, edgeSuid, radiusModifier);
		crossHaystackEndpoint(x1, y1, trgRadius, rtnValTrg, dataBuff, trgSuid, edgeSuid, radiusModifier);
	}
	

	/**
	 * Computes a 'random' point around the circumference of a circle within the node.
	 */
	private final static void crossHaystackEndpoint(float centerX, float centerY, float radius, float[] rtnVal, 
			byte[] dataBuff, long nodeSuid, long edgeSuid, float radiusModifier) {
		// Hash the node and edge SUIDs to a value between 0.0 and 1.0. This is done instead of 
		// generating a random number so that the edge endpoints stay consistent between frames.
		float h1 = hashSuids(dataBuff, nodeSuid, edgeSuid, 99);
		
		// Use the hash to get a 'random' point around the circumference
		// of a circle that lies within the node boundaries.
		double theta = h1 * Math.PI * 2;
		double x = centerX + Math.cos(theta) * radius;
		double y = centerY + Math.sin(theta) * radius;
		
		rtnVal[0] = (float) x;
		rtnVal[1] = (float) y;
	}
	

	private static void longToBytes(long l, byte[] buff, int offset) {
		for (int i = offset + 7; i >= offset; i--) {
			buff[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
	}

	/**
	 * Returns a hashed value of the given suids in the range 0.0 to 1.0
	 */
	private static float hashSuids(byte[] dataBuff, long nodeSuid, long edgeSuid, int seed) {
		longToBytes(nodeSuid, dataBuff, 0);
		longToBytes(edgeSuid, dataBuff, 8);
		int hash = MurmurHash3.murmurhash3_x86_32(dataBuff, 0, dataBuff.length, seed);
		float r = Math.abs((float) hash / Integer.MAX_VALUE);
		return r;
	}
	
	
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
	public final static void computeEdgeEndpoints(final float[] srcNodeExtents,
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
			anchors.getAnchor(0, floatBuff);
			srcXOut = floatBuff[0];
			srcYOut = floatBuff[1];
			anchors.getAnchor(anchors.numAnchors() - 1, floatBuff);
			trgXOut = floatBuff[0];
			trgYOut = floatBuff[1];
		}

		calcIntersection(srcNodeShape, srcNodeExtents, srcX, srcY, srcXOut, srcYOut, floatBuff); 
		final float srcXAdj = floatBuff[0];
		final float srcYAdj = floatBuff[1];

		calcIntersection(trgNodeShape, trgNodeExtents, trgX, trgY, trgXOut, trgYOut, floatBuff); 
		final float trgXAdj = floatBuff[0];
		final float trgYAdj = floatBuff[1];

		rtnValSrc[0] = srcXAdj;
		rtnValSrc[1] = srcYAdj;
		rtnValTrg[0] = trgXAdj;
		rtnValTrg[1] = trgYAdj;
	}

	private static void calcIntersection(byte nodeShape, 
	                                     float[] nodeExtents, float x, float y,
	                                     float xOut, float yOut, float[] retVal) {
		if ((nodeExtents[0] == nodeExtents[2]) || 
		    (nodeExtents[1] == nodeExtents[3])) {
			retVal[0] = x;
			retVal[1] = y;
		} else {
			if (!GraphGraphics.computeEdgeIntersection(nodeShape, nodeExtents[0],
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

				GraphGraphics.computeEdgeIntersection(nodeShape, nodeExtents[0],
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
											 final RenderDetailFlags flags,
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

			if (flags.not(LOD_NODE_BORDERS)) { // Not rendering borders.
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
		if (flags.has(LOD_CUSTOM_GRAPHICS)) {
			// draw any nested networks first
			final TexturePaint nestedNetworkPaint = nodeDetails.getNestedNetworkTexturePaint(netView, cyNode);
			if (nestedNetworkPaint != null) {
				doubleBuff1[0] = floatBuff1[0];
				doubleBuff1[1] = floatBuff1[1];
				doubleBuff1[2] = floatBuff1[2];
				doubleBuff1[3] = floatBuff1[3];
				computeAnchor(Position.CENTER, doubleBuff1, doubleBuff2);
				grafx.drawCustomGraphicImage(nestedNetworkPaint.getAnchorRect(), (float)doubleBuff2[0],  (float)doubleBuff2[1], nestedNetworkPaint); 
			}

			// draw custom graphics on top of nested networks 
			// don't allow our custom graphics to mutate while we iterate over them:
//			synchronized (nodeDetails.customGraphicsLock(cyNode)) {
			synchronized (nodeDetails) {
				// This method should return CustomGraphics in rendering order:
				final Map<VisualProperty<CyCustomGraphics>, CustomGraphicsInfo> cgMap = nodeDetails.getCustomGraphics(cyNode);
				if(cgMap != null) {
					final List<CustomGraphicsInfo> infoList = new ArrayList<>(cgMap.values());
					
					// MKTODO I guess there's no way around doing this? The charts need access to the underlying table model.
					CyNetworkView netViewForCharts = netView.getMutableNetworkView();
					View<CyNode> mutableNode = netView.getMutableNodeView(cyNode.getSUID());
					
					if(mutableNode != null) {
						for(CustomGraphicsInfo cgInfo : infoList) {
							List<CustomGraphicLayer> layers = cgInfo.createLayers(netViewForCharts, mutableNode, nodeDetails, dependencies);
							
							for (CustomGraphicLayer layer : layers) {
								float offsetVectorX = nodeDetails.graphicOffsetVectorX(cyNode);
								float offsetVectorY = nodeDetails.graphicOffsetVectorY(cyNode);
								doubleBuff1[0] = floatBuff1[0];
								doubleBuff1[1] = floatBuff1[1];
								doubleBuff1[2] = floatBuff1[2];
								doubleBuff1[3] = floatBuff1[3];
								computeAnchor(Position.CENTER, doubleBuff1, doubleBuff2);
								
								float xOffset = (float) (doubleBuff2[0] + offsetVectorX);
								float yOffset = (float) (doubleBuff2[1] + offsetVectorY);
								nodeShape = createCustomGraphicsShape(nodeShape, layer, -xOffset, -yOffset);
								
								grafx.drawCustomGraphicFull(netViewForCharts, mutableNode, nodeShape, layer, xOffset, yOffset);
							}
						}
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

}
