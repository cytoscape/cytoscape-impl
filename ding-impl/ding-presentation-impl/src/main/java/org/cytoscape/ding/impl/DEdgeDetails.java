package org.cytoscape.ding.impl;

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
import static org.cytoscape.ding.DVisualLexicon.EDGE_CURVED;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.strokes.DAnimatedStroke;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.intr.LongEnumerator;
import org.cytoscape.util.intr.MinLongHeap;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.EdgeStackingVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.EdgeStacking;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Position;


public final class DEdgeDetails implements EdgeDetails {
	
	public static final float HANDLE_SIZE = 12.0f; 

	private final DRenderingEngine re;
	private Map<View<CyEdge>,DAnimatedStroke> animatedStrokes = null;
	
	public DEdgeDetails(DRenderingEngine re) {
		this.re = re;
	}
	
	@Override
	public boolean isSelected(View<CyEdge> edgeView) {
		return Boolean.TRUE.equals(edgeView.getVisualProperty(BasicVisualLexicon.EDGE_SELECTED));
	}

	@Override
	public boolean isVisible(View<CyEdge> edgeView) {
		return Boolean.TRUE.equals(edgeView.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE));
	}
	
	@Override
	public Color getColorLowDetail(CyNetworkViewSnapshot netView, View<CyEdge> edgeView) {
		if (isSelected(edgeView))
			return getSelectedColorLowDetail(netView, edgeView);
		else
			return getUnselectedColorLowDetail(netView, edgeView);
	}

	private Color getUnselectedColorLowDetail(CyNetworkViewSnapshot netView, View<CyEdge> edgeView) {
		Paint paint = edgeView.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT);
		if(paint instanceof Color)
			return (Color) paint;
		
		paint = netView.getViewDefault(EDGE_STROKE_UNSELECTED_PAINT);
		if(paint instanceof Color)
			return (Color) paint;
		
		return (Color) EDGE_STROKE_UNSELECTED_PAINT.getDefault();
	}

	private Color getSelectedColorLowDetail(CyNetworkViewSnapshot netView, View<CyEdge> edgeView) {
		Paint paint = edgeView.getVisualProperty(EDGE_STROKE_SELECTED_PAINT);
		if(paint instanceof Color)
			return (Color) paint;
		
		paint = netView.getViewDefault(EDGE_STROKE_SELECTED_PAINT);
		if(paint instanceof Color)
			return (Color) paint;
		
		return (Color) EDGE_STROKE_SELECTED_PAINT.getDefault();
	}
	
	@Override
	public ArrowShape getSourceArrowShape(View<CyEdge> edgeView) {
		return edgeView.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE);
	}

	@Override
	public Paint getSourceArrowPaint(View<CyEdge> edgeView) {
		if (isSelected(edgeView))
			return getSelectedPaint(edgeView);
		else
			return getSourceArrowUnselectedPaint(edgeView);
	}

	private final Paint getSourceArrowUnselectedPaint(View<CyEdge> edgeView) {
		Paint paint = edgeView.getVisualProperty(EDGE_SOURCE_ARROW_UNSELECTED_PAINT);
		Integer trans = edgeView.getVisualProperty(EDGE_TRANSPARENCY);
		return DNodeDetails.getTransparentColor(paint, trans);
	}

	@Override
	public ArrowShape getTargetArrowShape(View<CyEdge> edgeView) {
		return edgeView.getVisualProperty(EDGE_TARGET_ARROW_SHAPE);
	}

	@Override
	public Paint getTargetArrowPaint(View<CyEdge> edgeView) {
		if (isSelected(edgeView))
			return getSelectedPaint(edgeView);
		else
			return getTargetArrowUnselectedPaint(edgeView);
	}

	private final Paint getTargetArrowUnselectedPaint(View<CyEdge> edgeView) {
		Paint paint = edgeView.getVisualProperty(EDGE_TARGET_ARROW_UNSELECTED_PAINT);
		Integer trans = edgeView.getVisualProperty(EDGE_TRANSPARENCY);
		return DNodeDetails.getTransparentColor(paint, trans);
	}
	
	@Override
	public float getWidth(View<CyEdge> edgeView) {
		return edgeView.getVisualProperty(EDGE_WIDTH).floatValue();
	}

	
	private Stroke getEdgeStroke(View<CyEdge> edgeView) {
		LineType lineType = edgeView.getVisualProperty(EDGE_LINE_TYPE);
		float width = (float) getWidth(edgeView);
		return DLineType.getDLineType(lineType).getStroke(width);
	}
	
	@Override
	public Stroke getStroke(View<CyEdge> edgeView) {
		Stroke stroke = animatedStrokes == null ? null : animatedStrokes.get(edgeView);
		if(stroke == null) {
			stroke = getEdgeStroke(edgeView);
		}
		return stroke;
	}
	
	@Override
	public void updateAnimatedEdges(Collection<View<CyEdge>> edges) {
		if(edges.isEmpty()) {
			animatedStrokes = null;
			return;
		}
		
		if(animatedStrokes == null) {
			animatedStrokes = new HashMap<>();
		} else {
			animatedStrokes.keySet().retainAll(edges);
		}
		
		for(View<CyEdge> edge : edges) {
			DAnimatedStroke animatedStroke = animatedStrokes.get(edge);
			Stroke stroke = getEdgeStroke(edge);
			if(animatedStroke == null || !sameStroke(animatedStroke, stroke)) {
				animatedStrokes.put(edge, (DAnimatedStroke)stroke);
			}
		}
	}

	private static boolean sameStroke(DAnimatedStroke animatedStroke, Stroke stroke) {
		return animatedStroke.getClass().equals(stroke.getClass())
			&& animatedStroke.getWidth() == ((DAnimatedStroke)stroke).getWidth();
	}
	
	@Override
	public void advanceAnimatedEdges() {
		animatedStrokes.replaceAll((edge,stroke) -> stroke.newInstanceForNextOffset());
	}
	
	
	@Override
	public Paint getPaint(View<CyEdge> edgeView) {
		return isSelected(edgeView) ? getSelectedPaint(edgeView) : getUnselectedPaint(edgeView);
	}

	@Override
	public Paint getUnselectedPaint(View<CyEdge> edgeView) {
		Paint paint = edgeView.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT);
		if(paint == null)
			paint = edgeView.getVisualProperty(EDGE_UNSELECTED_PAINT);
		Integer trans = edgeView.getVisualProperty(EDGE_TRANSPARENCY);
		return DNodeDetails.getTransparentColor(paint, trans);
	}

	@Override
	public Paint getSelectedPaint(View<CyEdge> edgeView) {
		Paint paint = edgeView.getVisualProperty(EDGE_STROKE_SELECTED_PAINT);
		if(paint == null)
			paint = edgeView.getVisualProperty(EDGE_SELECTED_PAINT);
		Integer trans = edgeView.getVisualProperty(EDGE_TRANSPARENCY);
		return DNodeDetails.getTransparentColor(paint, trans);
	}

	@Override
	public int getLabelCount(View<CyEdge> edgeView) {
		String label = getLabelText(edgeView);
		return (label == null || label.isEmpty()) ? 0 : 1;
	}

	@Override
	public String getLabelText(View<CyEdge> edgeView) {
		return edgeView.getVisualProperty(EDGE_LABEL);
	}

	@Override
	public String getTooltipText(View<CyEdge> edgeView) {
		return edgeView.getVisualProperty(EDGE_TOOLTIP);
	}

	@Override
	public Font getLabelFont(View<CyEdge> edgeView, boolean forPdf) {
		Number size = edgeView.getVisualProperty(EDGE_LABEL_FONT_SIZE);
		Font font = edgeView.getVisualProperty(EDGE_LABEL_FONT_FACE);
		return DNodeDetails.computeFont(font, size, forPdf);
	}

	@Override
	public Paint getLabelPaint(View<CyEdge> edgeView) {
		Paint paint = edgeView.getVisualProperty(EDGE_LABEL_COLOR);
		Integer trans = edgeView.getVisualProperty(EDGE_LABEL_TRANSPARENCY);
		return DNodeDetails.getTransparentColor(paint, trans);
	}

	@Override
	public Paint getLabelBackgroundPaint(View<CyEdge> edgeView) {
		Paint paint = edgeView.getVisualProperty(EDGE_LABEL_BACKGROUND_COLOR);
		Integer trans = edgeView.getVisualProperty(EDGE_LABEL_BACKGROUND_TRANSPARENCY);
		return DNodeDetails.getTransparentColor(paint, trans);
	}
	
	@Override
	public byte getLabelBackgroundShape(View<CyEdge> edgeView) {
		return DNodeDetails.getLabelBackgroundShape(edgeView.getVisualProperty(EDGE_LABEL_BACKGROUND_SHAPE));
	}
	
	@Override
	public EdgeStacking getStacking(View<CyEdge> edgeView) {
		return edgeView.getVisualProperty(DVisualLexicon.EDGE_STACKING);
	}
	
	@Override
	public float getStackingDensity(View<CyEdge> edgeView) {
		Double radius = edgeView.getVisualProperty(DVisualLexicon.EDGE_STACKING_DENSITY);
		if(radius == null)
			return DVisualLexicon.EDGE_STACKING_DENSITY.getDefault().floatValue();
		float density = (float) Math.min(1.0, Math.max(0.0, radius));
		
		if(getStacking(edgeView) == EdgeStackingVisualProperty.AUTO_BEND) {
			// Multiply by 2 so the default of 0.5 results in a modifier of 1.0 which has no effect and maintains backwards compatibility.
			density *= 2.0f; 
		}
		return density;
	}
	
	@Override
	public double getLabelWidth(View<CyEdge> edgeView) {
		return edgeView.getVisualProperty(EDGE_LABEL_WIDTH);
	}

	@Override
	public Double getLabelRotation(View<CyEdge> edgeView) {
		Double dAngle = edgeView.getVisualProperty(EDGE_LABEL_ROTATION);
		return dAngle;
	}

	@Override
	public Double getLabelRotation(View<CyEdge> edgeView, double rise, double run) {
		Double dAngle = edgeView.getVisualProperty(EDGE_LABEL_ROTATION);
    if (!getLabelAutorotate(edgeView) || edgeView.isValueLocked(EDGE_LABEL_ROTATION))
      return dAngle;

    // Get the rotation
    double rot = Math.atan2(rise, run);

    // Bound it to avoid having the label upside-down
    if (rot < -1.570796)
      rot += 3.141593;
    if (rot > 1.570796)
      rot -= 3.141593;

    // Return the value in degrees
    return Math.toDegrees(rot);
	}

	@Override
	public boolean getLabelAutorotate(View<CyEdge> edgeView) {
		Boolean auto = edgeView.getVisualProperty(EDGE_LABEL_AUTOROTATE);
		return auto;
	}

	@Override
	public Position getLabelTextAnchor(View<CyEdge> edgeView) {
		ObjectPosition pos = edgeView.getVisualProperty(EDGE_LABEL_POSITION);
		return pos == null ? null : pos.getAnchor();
	}

	@Override
	public Position getLabelEdgeAnchor(View<CyEdge> edgeView) {
		ObjectPosition pos = edgeView.getVisualProperty(EDGE_LABEL_POSITION);
		return pos == null ? null : pos.getTargetAnchor();
	}

	@Override
	public float getLabelOffsetVectorX(View<CyEdge> edgeView) {
		ObjectPosition pos = edgeView.getVisualProperty(EDGE_LABEL_POSITION);
		return pos == null ? 0.0f : (float) pos.getOffsetX();
	}

	@Override
	public float getLabelOffsetVectorY(View<CyEdge> edgeView) {
		ObjectPosition pos = edgeView.getVisualProperty(EDGE_LABEL_POSITION);
		return pos == null ? 0.0f : (float) pos.getOffsetY();
	}

	@Override
	public Justification getLabelJustify(View<CyEdge> edgeView) {
		ObjectPosition pos = edgeView.getVisualProperty(EDGE_LABEL_POSITION);
		return pos == null ? null : pos.getJustify();
	}
	

	@Override
	public float getSourceArrowSize(View<CyEdge> edgeView) {
		Double size = edgeView.getVisualProperty(EDGE_SOURCE_ARROW_SIZE);
		return adjustArrowSize(edgeView, getSourceArrowShape(edgeView), size);
	}
	
	@Override
	public float getTargetArrowSize(View<CyEdge> edgeView) {
		Double size = edgeView.getVisualProperty(EDGE_TARGET_ARROW_SIZE);
		return adjustArrowSize(edgeView, getSourceArrowShape(edgeView), size);
	}
	
	private float adjustArrowSize(View<CyEdge> edgeView, ArrowShape arrowType, Number size) {
		// For the half arrows, we need to scale multiplicatively so that the arrow matches the line.
		if (arrowType == ArrowShapeVisualProperty.HALF_TOP || arrowType == ArrowShapeVisualProperty.HALF_BOTTOM)
			return (float) getWidth(edgeView) * size.floatValue();
		else // For all other arrows, we can scale additively. This produces less egregious big arrows.
			return (float) getWidth(edgeView) + size.floatValue();
	}

	@Override
	public Integer getLineCurved(View<CyEdge> edgeView) {
		Boolean curved = edgeView.getVisualProperty(EDGE_CURVED);
		return Boolean.TRUE.equals(curved) ? CURVED_LINES : STRAIGHT_LINES;
	}


	@Override
	public Bend getBend(View<CyEdge> edgeView) {
		return edgeView.getVisualProperty(EDGE_BEND);
	}

	
	@Override
	public float getAnchorSize(View<CyEdge> edgeView, int anchorInx) {
		if (isSelected(edgeView) && getNumAnchors(edgeView) > 0)
			return HANDLE_SIZE;
		return 0.0f;
	}
	
	public boolean hasHandles(View<CyEdge> edgeView) {
		Bend bend = getBend(edgeView);
		if(bend == null)
			return false;
		return !bend.getAllHandles().isEmpty();
	}
	
	@Override
	public Paint getAnchorPaint(View<CyEdge> edgeView, int anchorInx) {
		if (getLineCurved(edgeView) == STRAIGHT_LINES)
			anchorInx = anchorInx / 2;
		
		BendStore bendStore = re == null ? null : re.getBendStore();
		
		Bend bend = getBend(edgeView);
		List<Handle> handles = bend.getAllHandles();
		try {
			Handle handle = handles.get(anchorInx);
			if(bendStore != null && bendStore.isHandleSelected(new HandleInfo(edgeView, bend, handle))) {
				return getSelectedPaint(edgeView);
			}
		} catch(IndexOutOfBoundsException e) {
		}
		return getUnselectedPaint(edgeView);
	}


	private int getNumAnchors(View<CyEdge> edgeView) {
		Bend bend = getBend(edgeView); 
		if(bend == null)
			return 0;
		var handles = bend.getAllHandles();
		if(handles == null)
			return 0;
		int numHandles = handles.size();
		if(numHandles == 0)
			return 0;
		return getLineCurved(edgeView) == CURVED_LINES ? numHandles : 2 * numHandles;
	}
	
	/**
	 * Actual method to be used in the Graph Renderer.
	 */
	private void getAnchor(View<CyEdge> edgeView, int anchorIndex, float[] anchorArr) {
		if(re == null)
			return;
		
		Bend bend = getBend(edgeView);
		
		Handle handle;
		if (getLineCurved(edgeView) == CURVED_LINES)
			handle = bend.getAllHandles().get(anchorIndex);
		else
			handle = bend.getAllHandles().get(anchorIndex/2);

		Point2D newPoint = handle.calculateHandleLocation(re.getViewModelSnapshot(), edgeView);
		anchorArr[0] = (float) newPoint.getX();
		anchorArr[1] = (float) newPoint.getY();
	}
	
	
	public static List<View<CyEdge>> getConnectingEdgeList(CyNetworkViewSnapshot netView, View<CyNode> source, View<CyNode> target) {
		return getConnectingEdgeList(netView, source.getSUID(), target.getSUID());
	}
	
	public static List<View<CyEdge>> getConnectingEdgeList(CyNetworkViewSnapshot netView, long sourceSuid, long targetSuid) {
		// MKTODO this may need to be optimized
		List<View<CyEdge>> connectingEdges = new ArrayList<>();
		var adjacentEdgeIterable = netView.getAdjacentEdgeIterable(sourceSuid);
		for(var edge : adjacentEdgeIterable) {
			var edgeInfo = netView.getEdgeInfo(edge);
			long otherNode = sourceSuid ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
			if(targetSuid == otherNode) {
				connectingEdges.add(edge);
			}
		}
		return connectingEdges;
	}
	
	
	private class VisualPropertyEdgeAnchors implements EdgeAnchors {
		private final View<CyEdge> edgeView;
		public VisualPropertyEdgeAnchors(View<CyEdge> edgeView) {
			this.edgeView = edgeView;
		}
		public int numAnchors() { 
			return DEdgeDetails.this.getNumAnchors(edgeView); 
		}
		public void getAnchor(int anchorIndex, float[] anchorArr) {
			DEdgeDetails.this.getAnchor(edgeView, anchorIndex, anchorArr);
		}
	}
	
	
	@Override
	public EdgeAnchors getAnchors(final CyNetworkViewSnapshot netView, final View<CyEdge> edgeView) {
		if (edgeView == null)
			return null;
		
		if(edgeView.getVisualProperty(DVisualLexicon.EDGE_STACKING) != EdgeStackingVisualProperty.AUTO_BEND) {
			// no bends when using haystack edges
			return null;
		}
		
		int numAnchors = getNumAnchors(edgeView);
		if (numAnchors > 0) {
			return new VisualPropertyEdgeAnchors(edgeView);
		}

		float[]	extentsBuff = new float[4];
		SnapshotEdgeInfo edgeInfo = netView.getEdgeInfo(edgeView);
		final long srcNodeIndex = edgeInfo.getSourceViewSUID();
		final long trgNodeIndex = edgeInfo.getTargetViewSUID();

		// Calculate anchors necessary for self edges.
		if (srcNodeIndex == trgNodeIndex) {
			netView.getSpacialIndex2D().get(srcNodeIndex, extentsBuff);

			final double w = ((double) extentsBuff[2]) - extentsBuff[0];
			final double h = ((double) extentsBuff[3]) - extentsBuff[1];
			final double x = (((double) extentsBuff[0]) + extentsBuff[2]) / 2.0d;
			final double y = (((double) extentsBuff[1]) + extentsBuff[3]) / 2.0d;
			final double nodeSize = Math.max(w, h);
			
			List<View<CyEdge>> selfEdgeList = getConnectingEdgeList(netView, srcNodeIndex, srcNodeIndex);

			int i = 0;
			for (View<CyEdge> selfEdge : selfEdgeList) {
				if (selfEdge.getSUID() == edgeView.getSUID())
					break;
				if (getNumAnchors(selfEdge) == 0)
					i++;
			}

			final int inx = i;
			final float densityModifier = getStackingDensity(edgeView);

			return new EdgeAnchors() {
				@Override
				public int numAnchors() {
					return 2;
				}
				@Override
				public void getAnchor(int anchorInx, float[] anchorArr) {
					if (anchorInx == 0) {
						anchorArr[0] = (float) (x - ((((inx + 3) * nodeSize) / 2.0d) * densityModifier));
						anchorArr[1] = (float) y;
					} else if (anchorInx == 1) {
						anchorArr[0] = (float) x;
						anchorArr[1] = (float) (y - ((((inx + 3) * nodeSize) / 2.0d) * densityModifier)) ;
					}
				}
			};
		}

		
		// Now add "invisible" anchors to edges for the case where multiple edges
		// exist between two nodes. This has no effect if user specified anchors exist on the edge.
		while (true) {
			MinLongHeap heap = new MinLongHeap();
			// By consistently ordering the source and target nodes, dx and dy will always
			// be calculated according to the same orientation. This allows the offset
			// calculation to toggle the edges from side to side without any overlap.
			long tmpSrcIndex = Math.min(srcNodeIndex, trgNodeIndex);
			long tmpTrgIndex = Math.max(srcNodeIndex, trgNodeIndex);

			// Sort the connecting edges.
			View<CyNode> tmpSrc = netView.getNodeView(tmpSrcIndex);
			View<CyNode> tmpTrg = netView.getNodeView(tmpTrgIndex);
			List<View<CyEdge>> conEdgeList = getConnectingEdgeList(netView, tmpSrc.getSUID(), tmpTrg.getSUID());

			for (View<CyEdge> conEdge : conEdgeList) {
				heap.toss(conEdge.getSUID());
			}

			LongEnumerator otherEdges = heap.orderedElements(false);
			long otherEdge = otherEdges.nextLong();

			// If the first other edge is the same as this edge,
			// (i.e. we're at the end of the list?).
			if (otherEdge == edgeView.getSUID())
				break;

			// So we don't count the other edge twice?
			View<CyEdge> otherEdgeView = netView.getEdgeView(otherEdge);
			if (otherEdgeView == null)
				continue;
			
			int i = (getNumAnchors(otherEdgeView) == 0) ? 1 : 0;

			// Count the number of other edges.
			while (true) {
				if (edgeView.getSUID() == (otherEdge = otherEdges.nextLong()) || otherEdge == -1)
					break;
				if (!hasHandles(otherEdgeView))
					i++;
			}

			final int inx = i;

			// Get source node size and position.
			netView.getSpacialIndex2D().get(tmpSrcIndex, extentsBuff);
			final double srcW = ((double) extentsBuff[2]) - extentsBuff[0];
			final double srcH = ((double) extentsBuff[3]) - extentsBuff[1];
			final double srcX = (((double) extentsBuff[0]) + extentsBuff[2]) / 2.0d;
			final double srcY = (((double) extentsBuff[1]) + extentsBuff[3]) / 2.0d;

			// Get target node size and position.
			netView.getSpacialIndex2D().get(tmpTrgIndex, extentsBuff);
			final double trgW = ((double) extentsBuff[2]) - extentsBuff[0];
			final double trgH = ((double) extentsBuff[3]) - extentsBuff[1];
			final double trgX = (((double) extentsBuff[0]) + extentsBuff[2]) / 2.0d;
			final double trgY = (((double) extentsBuff[1]) + extentsBuff[3]) / 2.0d;

			// Used for determining the space between the edges.
			final double nodeSize = Math.max(Math.max(Math.max(srcW, srcH), trgW), trgH);

			// Midpoint between nodes.
			final double midX = (srcX + trgX) / 2;
			final double midY = (srcY + trgY) / 2;

			// Distance in X and Y dimensions.
			// Note that dx and dy may be negative. This is OK, because this will ensure
			// that the handle is always correctly placed offset from the midpoint of,
			// and perpendicular to, the original edge.
			final double dx = trgX - srcX;
			final double dy = trgY - srcY;

			// Distance or length between nodes.
			final double len = Math.sqrt((dx * dx) + (dy * dy));

			if (((float) len) == 0.0f)
				break;

						
			// This determines which side of the first edge and how far from the first
			// edge the other edge should be placed.
			// - Divide by 2 puts consecutive edges at the same distance from the center because of integer math.
			// - Modulo puts consecutive edges on opposite sides.
			// - Node size is for consistent scaling.
			final float densityModifier = getStackingDensity(edgeView);
			final double offset = (((inx + 1) / 2) * (inx % 2 == 0 ? 1 : -1) * nodeSize) * densityModifier;
			
			// Depending on orientation sine or cosine. This adjusts the length
			// of the offset according the appropriate X and Y dimensions.
			final double normX = dx / len;
			final double normY = dy / len;

			// Calculate the anchor points.
			final double anchorX = midX + (offset * normY);
			final double anchorY = midY - (offset * normX);

			return new EdgeAnchors() {
				public int numAnchors() {
					return 1;
				}

				public void getAnchor(int inx, float[] arr) {
					arr[0] = (float) anchorX;
					arr[1] = (float) anchorY;
				}
			};
		}
		
		return new VisualPropertyEdgeAnchors(edgeView);
	}

	
	
}
