package org.cytoscape.ding.impl;

import static org.cytoscape.graph.render.stateful.RenderDetailFlags.*;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_POSITION;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.LabelInfo;
import org.cytoscape.graph.render.stateful.LabelInfoProvider;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.spacial.EdgeSpacialIndex2DEnumerator;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.EdgeStackingVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.EdgeStacking;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Position;


/**
 * This class provides methods that will return the network elements (nodes/edges/annotations/handles)
 * that are under a mouse point on the main network view.
 * 
 * All arguments to methods in this class must be in image (swing) coordinates.
 */
public class NetworkPicker {

	private final DRenderingEngine re;
	private final NodeDetails nodeDetails;
	private final EdgeDetails edgeDetails;
	
	private RenderDetailFlags renderDetailFlags;
	
	public NetworkPicker(DRenderingEngine re, RenderDetailFlags renderDetailFlags) {
		this.re = re;
		this.nodeDetails = re.getNodeDetails();
		this.edgeDetails = re.getEdgeDetails();
		this.renderDetailFlags = renderDetailFlags;
	}
	
	public RenderDetailFlags getLastRenderDetail() {
		return this.renderDetailFlags;
	}
	
	public RenderDetailFlags getFlags() {
		return this.renderDetailFlags;
	}
	
	public void setRenderDetailFlags(RenderDetailFlags renderDetailFlags) {
		this.renderDetailFlags = renderDetailFlags;
	}
	
	private boolean treatNodeShapesAsRectangle() {
		return renderDetailFlags.treatNodeShapesAsRectangle();
	}

	/**
	 * utility that returns the nodeView that is located at input point
	 */
	public View<CyNode> getNodeAt(Point2D pt) {
		double[] locn = {pt.getX(), pt.getY()};
		re.getTransform().xformImageToNodeCoords(locn);
		float x = (float) locn[0];
		float y = (float) locn[1];
		
		List<Long> suids = getNodesIntersectingRectangle(x, y, x, y);
		if(suids.isEmpty())
			return null;
		
		Long suid = suids.get(suids.size() - 1);
		return re.getViewModelSnapshot().getNodeView(suid);
	}
	
	// WARNING Only to be used when loading arrow annotations
	public View<CyNode> getNodeForArrowAnnotation(double centerX, double centerY) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		float x = (float)centerX, y = (float)centerY;
		
		SpacialIndex2DEnumerator<Long> under = snapshot.getSpacialIndex2D().queryOverlap(x, y, x, y);
		
		Long suid = null;
		while(under.hasNext()) {
			suid = under.next();
		}
		
		return suid == null ? null : re.getViewModelSnapshot().getMutableNodeView(suid);
	}

	
	/**
	 * Returns a rectangular shape that contains the label, the shape may be rotated.
	 */
	public LabelSelection getLabelShape(View<CyNode> node, LabelInfoProvider labelProvider) {
		String text = nodeDetails.getLabelText(node);
		if(text == null || text.isBlank())
			return null;
	
		final double[] doubleBuff1 = new double[4];
		final double[] doubleBuff2 = new double[2];
		
		final ObjectPosition originalPosition = node.getVisualProperty(NODE_LABEL_POSITION);
		
		final Font font = nodeDetails.getLabelFont(node);
		final Position textAnchor = nodeDetails.getLabelTextAnchor(node);
		final Position nodeAnchor = nodeDetails.getLabelNodeAnchor(node);
		final float offsetVectorX = nodeDetails.getLabelOffsetVectorX(node);
		final float offsetVectorY = nodeDetails.getLabelOffsetVectorY(node);
		final double degrees = nodeDetails.getLabelRotation(node);
		final double nodeLabelWidth = nodeDetails.getLabelWidth(node);
		
		double x = nodeDetails.getXPosition(node); 
		double y = nodeDetails.getYPosition(node); 
		double nodeWidth = nodeDetails.getWidth(node);
		double nodeHeight = nodeDetails.getHeight(node);
		
		doubleBuff1[0] = x - nodeWidth  / 2;
		doubleBuff1[1] = y - nodeHeight / 2;
		doubleBuff1[2] = x + nodeWidth  / 2;
		doubleBuff1[3] = y + nodeHeight / 2;
		GraphRenderer.computeAnchor(nodeAnchor, doubleBuff1, doubleBuff2);

		double nodeAnchorPointX = doubleBuff2[0];
		double nodeAnchorPointY = doubleBuff2[1];
		
		var frc = new FontRenderContext(null, false, false);
		
		LabelInfo labelInfo = labelProvider.getLabelInfo(text, font, nodeLabelWidth, frc);

		doubleBuff1[0] = -0.5d * labelInfo.getMaxLineWidth();
		doubleBuff1[1] = -0.5d * labelInfo.getTotalHeight();
		doubleBuff1[2] =  0.5d * labelInfo.getMaxLineWidth();
		doubleBuff1[3] =  0.5d * labelInfo.getTotalHeight();
		GraphRenderer.computeAnchor(textAnchor, doubleBuff1, doubleBuff2);

		final double textXCenter = nodeAnchorPointX - doubleBuff2[0] + offsetVectorX;
		final double textYCenter = nodeAnchorPointY - doubleBuff2[1] + offsetVectorY;
		
		double h = labelInfo.getTotalHeight();  // actual label text box height
		double w = labelInfo.getMaxLineWidth();  // actual label text box width. 
		
		double xMin = textXCenter - (w/2);
		double yMin = textYCenter - (h/2);
//		double xMax = textXCenter + (w/2);
//		double yMax = textYCenter + (h/2); 
		
		double labelAnchorX = nodeAnchorPointX + offsetVectorX;
		double labelAnchorY = nodeAnchorPointY + offsetVectorY;
		
		Shape shape = new Rectangle2D.Double(xMin, yMin, w, h);
		if(degrees != 0.0) {
			double angle = degrees * 0.01745329252;
			var rotateTransform = AffineTransform.getRotateInstance(angle, labelAnchorX, labelAnchorY);
			shape = rotateTransform.createTransformedShape(shape);
		}
		
		return new LabelSelection(node, shape, originalPosition, labelAnchorX, labelAnchorY, degrees);
	}
	
	
	
	public LabelSelection getNodeLabelAt(Point2D mousePoint) {
		if(!renderDetailFlags.has(LOD_NODE_LABELS))
			return null;
		
		Point2D point = re.getTransform().getNodeCoordinates(mousePoint);
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		
		Rectangle2D.Float area = re.getTransform().getNetworkVisibleAreaNodeCoords();
		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(area.x, area.y, area.x + area.width, area.y + area.height);
		
		LabelInfoProvider labelProvider = re.getGraphLOD().isLabelCacheEnabled() ? re.getLabelCache() : LabelInfoProvider.NO_CACHE;
		
		while(nodeHits.hasNext()) {
			Long suid = nodeHits.next();
			View<CyNode> node = snapshot.getNodeView(suid);
			
			var labelSelection = getLabelShape(node, labelProvider);
			if(labelSelection != null && labelSelection.getShape().contains(point)) {
				return labelSelection;
			}
		}
		return null;
	}
	
	
	public View<CyEdge> getEdgeAt(Point2D pt) {
		List<Long> edges = getEdgesIntersecting((int)pt.getX(), (int)pt.getY(), (int)pt.getX(), (int)pt.getY());
		if(edges.isEmpty())
			return null;
		long chosenEdge = edges.get(edges.size()-1);
		return re.getViewModelSnapshot().getEdgeView(chosenEdge);
	}
	
	
	
	public HandleInfo getHandleAt(Point2D pt) {
		Rectangle r = new Rectangle((int)pt.getX(), (int)pt.getY(), 1, 1);
		List<HandleInfo> handles = getHandlesIntersecting(r, 1);
		if(handles.isEmpty())
			return null;
		return handles.get(0);
	}
	
	public List<HandleInfo> getHandlesInRectangle(Rectangle r) {
		return getHandlesIntersecting(r, -1);
	}
	
	
	private List<HandleInfo> getHandlesIntersecting(Rectangle r, int maxCount) {
		Rectangle2D selectionArea = re.getTransform().getNodeCoordinates(r);
		
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		List<HandleInfo> resultHandles = maxCount == 1 ? new ArrayList<>(1) : new ArrayList<>();
		
		Rectangle2D.Float area = re.getTransform().getNetworkVisibleAreaNodeCoords();
		EdgeSpacialIndex2DEnumerator edgeHits = snapshot.getSpacialIndex2D().queryOverlapEdges(area.x, area.y, area.x + area.width, area.y + area.height, null);
		
		while(edgeHits.hasNext()) {
			View<CyEdge> edge = edgeHits.nextEdge();
			getHandles(snapshot, edge, selectionArea, resultHandles);
			if(maxCount > 0 && resultHandles.size() >= maxCount) {
				return resultHandles.subList(0, maxCount);
			}
		}
		
		return resultHandles;
	}
	
	
	private void getHandles(CyNetworkViewSnapshot snapshot, View<CyEdge> edge, Rectangle2D selectionAreaNode, List<HandleInfo> resultHandles) {
		if(edgeDetails.isSelected(edge)) {
			Bend bend = edgeDetails.getBend(edge);
			if(bend != null) {
				List<Handle> handles = bend.getAllHandles();
				if(handles != null) {
					for(Handle handle : handles) {
						Point2D p = handle.calculateHandleLocation(snapshot, edge);
						var size = DEdgeDetails.HANDLE_SIZE;
						if(selectionAreaNode.intersects(p.getX()-(size/2), p.getY()-(size/2), size, size)) {
							resultHandles.add(new HandleInfo(edge, bend, handle));
						}
					}
				}
			}
		}
	}

	private List<View<CyNode>> suidsToNodes(List<Long> suids) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		List<View<CyNode>> selectedNodes = new ArrayList<>(suids.size());
		for(Long suid : suids) {
			View<CyNode> node = snapshot.getNodeView(suid);
			if(!nodeDetails.isSelected(node)) { // MKTODO is this check necessary? so what if it re-selects a node
				selectedNodes.add(node);
			}
		}
		return selectedNodes;
	}
	
	private List<View<CyEdge>> suidsToEdges(List<Long> suids) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		List<View<CyEdge>> selectedEdges = new ArrayList<>(suids.size());
		for(Long suid : suids) {
			View<CyEdge> edge = snapshot.getEdgeView(suid);
			if(!edgeDetails.isSelected(edge)) { // MKTODO is this check necessary? so what if it re-selects a node
				selectedEdges.add(edge);
			}
		}
		return selectedEdges;
	}
	
	
	
	public List<View<CyEdge>> getEdgesInRectangle(Rectangle r) {
		List<Long> suids = getEdgesIntersecting(r.x, r.y, r.x + r.width, r.y + r.height);
		return suidsToEdges(suids);
	}

	
	public List<Long> getEdgesIntersecting(int xMini, int yMini, int xMaxi, int yMaxi) {
		double[] ptBuff = new double[2];
		ptBuff[0] = xMini;
		ptBuff[1] = yMini;
		re.getTransform().xformImageToNodeCoords(ptBuff);

		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] = xMaxi;
		ptBuff[1] = yMaxi;
		re.getTransform().xformImageToNodeCoords(ptBuff);
		
		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
		
		final float[] srcExtents = new float[4];
		final float[] trgExtents = new float[4];
		
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		Rectangle2D.Float area = re.getTransform().getNetworkVisibleAreaNodeCoords();
		EdgeSpacialIndex2DEnumerator edgeHits = snapshot.getSpacialIndex2D().queryOverlapEdges(area.x, area.y, area.x + area.width, area.y + area.height, null);
		
		List<Long> resultEdges = new ArrayList<>();
		
		if (getFlags().not(LOD_HIGH_DETAIL)) {
			Line2D.Float line = new Line2D.Float();
			
			// We won't need to look up arrows and their sizes.
			while(edgeHits.hasNext()) {
				View<CyEdge> edge = edgeHits.nextEdgeWithNodeExtents(srcExtents, trgExtents, null);
				
				float sourceNodeX = (srcExtents[0] + srcExtents[2]) / 2;
				float sourceNodeY = (srcExtents[1] + srcExtents[3]) / 2;
				float targetNodeX = (trgExtents[0] + trgExtents[2]) / 2;
				float targetNodeY = (trgExtents[1] + trgExtents[3]) / 2;
				
				line.setLine(sourceNodeX, sourceNodeY, targetNodeX, targetNodeY);
				
				if(line.intersects(xMin, yMin, xMax - xMin, yMax - yMin)) {
					resultEdges.add(edge.getSUID());
				}
			}
			
		} else { // Last render high detail.
			byte[] haystackDataBuff = new byte[16];
			final float[] floatBuff1 = new float[4];
			final float[] floatBuff2 = new float[4];
			
			while(edgeHits.hasNext()) {
				View<CyEdge> edge = edgeHits.nextEdgeWithNodeExtents(srcExtents, trgExtents, null);
				
				SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(edge);
				long edgeSuid = edgeInfo.getSUID();
				double segThicknessDiv2 = edgeDetails.getWidth(edge) / 2.0d;
				EdgeStacking stacking = edgeDetails.getStacking(edge);
				
				View<CyNode> sourceNode = edgeInfo.getSourceNodeView();
				View<CyNode> targetNode = edgeInfo.getTargetNodeView();
				
				byte srcShape = nodeDetails.getShape(sourceNode);
				byte trgShape = nodeDetails.getShape(targetNode);
				
				final long srcSuid = sourceNode.getSUID();
				final long trgSuid = targetNode.getSUID();
			
				final ArrowShape srcArrow;
				final ArrowShape trgArrow;
				final float srcArrowSize;
				final float trgArrowSize;
				
				if (getFlags().not(LOD_EDGE_ARROWS) || stacking == EdgeStackingVisualProperty.HAYSTACK) {
					srcArrow = trgArrow = ArrowShapeVisualProperty.NONE;
					srcArrowSize = trgArrowSize = 0.0f;
				} else {
					srcArrow = edgeDetails.getSourceArrowShape(edge);
					trgArrow = edgeDetails.getTargetArrowShape(edge);
					srcArrowSize = ((srcArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : edgeDetails.getSourceArrowSize(edge));
					trgArrowSize = ((trgArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : edgeDetails.getTargetArrowSize(edge));
				}

				EdgeAnchors anchors = getFlags().not(LOD_EDGE_ANCHORS) ? null : edgeDetails.getAnchors(snapshot, edge);

				if(stacking == EdgeStackingVisualProperty.HAYSTACK) {
					float radiusModifier = edgeDetails.getStackingDensity(edge);
					GraphRenderer.computeEdgeEndpointsHaystack(srcExtents, trgExtents, srcSuid, trgSuid, edgeSuid, radiusModifier, stacking, 
							floatBuff1, floatBuff2, haystackDataBuff);
				} else {
					GraphRenderer.computeEdgeEndpoints(srcExtents, srcShape, srcArrow,
		                          srcArrowSize, anchors, trgExtents, trgShape,
		                          trgArrow, trgArrowSize, floatBuff1, floatBuff2);
				}

				GeneralPath path  = new GeneralPath();
				GeneralPath path2 = new GeneralPath();
				
				GraphGraphics.getEdgePath(srcArrow, srcArrowSize, trgArrow, trgArrowSize,
				                    floatBuff1[0], floatBuff1[1], anchors,
				                    floatBuff2[0], floatBuff2[1], path);
				GraphRenderer.computeClosedPath(path.getPathIterator(null), path2);

				if (path2.intersects(xMin - segThicknessDiv2, yMin - segThicknessDiv2,
				                       (xMax - xMin) + (segThicknessDiv2 * 2),
				                       (yMax - yMin) + (segThicknessDiv2 * 2)))
					resultEdges.add(edge.getSUID());
			}
		}
		
		return resultEdges;
	}
	
	
	public List<View<CyNode>> getNodesInRectangle(Rectangle r) {
		double[] ptBuff = {r.x, r.y};
		re.getTransform().xformImageToNodeCoords(ptBuff);
		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] = r.x + r.width;
		ptBuff[1] = r.y + r.height;
		re.getTransform().xformImageToNodeCoords(ptBuff);
		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
		
		List<Long> nodesXSect = getNodesIntersectingRectangle(xMin, yMin, xMax, yMax);
		return suidsToNodes(nodesXSect);
	}
	
	
	public List<Long> getNodesIntersectingRectangle(double xMinimum, double yMinimum, double xMaximum, double yMaximum) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		final float xMin = (float) xMinimum;
		final float yMin = (float) yMinimum;
		final float xMax = (float) xMaximum;
		final float yMax = (float) yMaximum;
		SpacialIndex2DEnumerator<Long> under = snapshot.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
		if (!under.hasNext())
			return Collections.emptyList();

		List<Long> returnVal = new ArrayList<>(under.size());

		if (treatNodeShapesAsRectangle()) {
			while (under.hasNext()) {
				returnVal.add(under.next());
			}
		} else {
			final double x = xMin;
			final double y = yMin;
			final double w = ((double) xMax) - xMin;
			final double h = ((double) yMax) - yMin;

			float[] extentsBuff = new float[4];

			while (under.hasNext()) {
				final long suid = under.nextExtents(extentsBuff);
				View<CyNode> cyNode = snapshot.getNodeView(suid);

				// The only way that the node can miss the intersection query is
				// if it intersects one of the four query rectangle's corners.
				if (((extentsBuff[0] < xMin) && (extentsBuff[1] < yMin))
						|| ((extentsBuff[0] < xMin) && (extentsBuff[3] > yMax))
						|| ((extentsBuff[2] > xMax) && (extentsBuff[3] > yMax))
						|| ((extentsBuff[2] > xMax) && (extentsBuff[1] < yMin))) {

					GeneralPath path = new GeneralPath();
					GraphGraphics.getNodeShape(nodeDetails.getShape(cyNode), extentsBuff[0], extentsBuff[1],
							extentsBuff[2], extentsBuff[3], path);

					if ((w > 0) && (h > 0)) {
						if (path.intersects(x, y, w, h))
							returnVal.add(suid);
					} else {
						if (path.contains(x, y))
							returnVal.add(suid);
					}
				} else {
					returnVal.add(suid);
				}
			}
		}
		return returnVal;
	}


//	public HandleKey getHandleAt(Point2D pt) {
//		double[] ptBuff = {pt.getX(), pt.getY()};
//		re.getTransform().xformImageToNodeCoords(ptBuff);
//		HandleKey handleKey = re.getBendStore().pickHandle((float)ptBuff[0], (float)ptBuff[1]);
//		return handleKey;
//	}
//
//	public List<HandleKey> getHandlesInRectangle(Rectangle r) {
//		BendStore bendStore = re.getBendStore();
//		if(getFlags().has(LOD_EDGE_ANCHORS)) {
//			Rectangle2D area = re.getTransform().getNodeCoordinates(r);
//			return bendStore.queryOverlap(area);
//		}
//		return Collections.emptyList();
//	}
//	
//	public List<HandleKey> getHandlesInPath(GeneralPath path) {
//		BendStore bendStore = re.getBendStore();
//		if(getFlags().has(LOD_EDGE_ANCHORS)) {
//			GeneralPath area = re.getTransform().pathInNodeCoords(path);
//			return bendStore.queryOverlap(area);
//		}
//		return Collections.emptyList();
//	}
	
	
	// Annotations
	
	
	public DingAnnotation getAnnotationAt(CanvasID canvasId, Point2D p) {
		List<DingAnnotation> annotations = re.getCyAnnotator().getAnnotations(canvasId, true);
		Point2D nodeP = re.getTransform().getNodeCoordinates(p);
		
		DingAnnotation hit = null;
		for(DingAnnotation a : annotations) {
			if(a.contains(nodeP)) {
				hit = a;
				break;
			}
		}
		
		if(hit != null) {
			while(hit.getGroupParent() != null) {
				hit = (DingAnnotation) hit.getGroupParent();
			}
		}
		
		return hit;
	}
	
	
	public DingAnnotation getAnnotationAt(Point2D p) {
		DingAnnotation a = getAnnotationAt(CanvasID.FOREGROUND, p);
		if(a == null)
			a = getAnnotationAt(CanvasID.BACKGROUND, p);
		return a;
	}
	
	
	public List<DingAnnotation> getAnnotationsAt(CanvasID canvasId, Point p) {
		List<DingAnnotation> annotations = re.getCyAnnotator().getAnnotations(canvasId, true); 
		Point2D nodeP = re.getTransform().getNodeCoordinates(p);
		
		List<DingAnnotation> list = new ArrayList<>();
		for(DingAnnotation a : annotations) {
			if(a.contains(nodeP)) {
				// Make sure to find the parent if this is a group
				while(a.getGroupParent() != null) {
					a = (DingAnnotation) a.getGroupParent();
				}
				if(!list.contains(a)) {
					list.add(a);
				}
			}
		}
		return list;
	}
	
	
	public List<DingAnnotation> getAnnotationsAt(Point p) {
		List<DingAnnotation> a = getAnnotationsAt(CanvasID.FOREGROUND, p);
		a.addAll(getAnnotationsAt(CanvasID.BACKGROUND, p));
		return a;
	}
	

	public List<DingAnnotation> getAnnotationsInRectangle(Rectangle rect) {
		Rectangle2D nodeRect = re.getTransform().getNodeCoordinates(rect);
		List<DingAnnotation> anns = new ArrayList<>();
		for(DingAnnotation a : re.getCyAnnotator().getAnnotations()) {
			Rectangle2D bounds = a.getBounds();
			if (a.getGroupParent() == null && nodeRect.contains(bounds))
				anns.add(a);
		}
		return anns;
	}
	
	public List<DingAnnotation> getAnnotationsInPath(GeneralPath path) {
		GeneralPath nodePath = re.getTransform().pathInNodeCoords(path);
		List<DingAnnotation> anns = new ArrayList<>();
		for(DingAnnotation a : re.getCyAnnotator().getAnnotations()) {
			Rectangle2D bounds = a.getBounds();
			if(a.getGroupParent() == null && nodePath.intersects(bounds))
				anns.add(a);
		}
		return anns;
	}
}
