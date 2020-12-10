package org.cytoscape.ding.impl;

import static org.cytoscape.graph.render.stateful.RenderDetailFlags.LOD_EDGE_ANCHORS;
import static org.cytoscape.graph.render.stateful.RenderDetailFlags.LOD_EDGE_ARROWS;
import static org.cytoscape.graph.render.stateful.RenderDetailFlags.LOD_HIGH_DETAIL;
import static org.cytoscape.graph.render.stateful.RenderDetailFlags.LOD_NODE_LABELS;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.MeasuredLineCreator;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
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

	public DLabelSelection getNodeLabelAt(Point2D pt) {
		if(!renderDetailFlags.has(LOD_NODE_LABELS))
			return null;
		return  selectLabel(re.getViewModelSnapshot(), pt);
	}
	
	/**
	 * 
	 * @param snapshot
	 * @param pt   point where the mouse was clicked.
	 * @return a DLabelSelection object if there is a node label under the specified point. null if no labels are found at this point.
	 */
	private DLabelSelection selectLabel(CyNetworkViewSnapshot snapshot, Point2D pt ) {
		double[] locn = {pt.getX(), pt.getY()};
		re.getTransform().xformImageToNodeCoords(locn);
		double xP = locn[0];
		double yP = locn[1];	
		
		Rectangle networkPaneBound = re.getComponentBounds();
		
		double[] ptBuff = {0d, 0d};
		re.getTransform().xformImageToNodeCoords(ptBuff);
		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] =  networkPaneBound.width;
		ptBuff[1] =  networkPaneBound.height;
		re.getTransform().xformImageToNodeCoords(ptBuff);
		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
				
		DLabelSelection selection = null;
		
		SpacialIndex2DEnumerator<Long> under = snapshot.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
		
		float[] extentsBuff = new float[4];

		while (under.hasNext()) {
			final Long suid = under.nextExtents(extentsBuff);
			View<CyNode> nodeView = snapshot.getNodeView(suid.longValue());
		
			// compute the actual size of label text rectangle
			String labelText = nodeDetails.getLabelText(nodeView);
			
			Font   font = nodeDetails.getLabelFont(nodeView);
			double labelWidth = nodeDetails.getLabelWidth(nodeView);
			
			MeasuredLineCreator mlCreator = new MeasuredLineCreator(labelText, font,  new FontRenderContext(null,true,true), 1.0, true, labelWidth);
			
			double h = mlCreator.getTotalHeight();  // actual label text box height
			double w =  mlCreator.getMaxLineWidth();  // actual label text box width. 
			
			// compute the actual position of the label text box.
			double x = nodeDetails.getXPosition(nodeView); 
			double y = nodeDetails.getYPosition(nodeView); 
			double nodeWidth = nodeDetails.getWidth(nodeView);
			double nodeHeight = nodeDetails.getHeight(nodeView);
			
			final Position textAnchor = nodeDetails.getLabelTextAnchor(nodeView);
			final Position nodeAnchor = nodeDetails.getLabelNodeAnchor(nodeView);
			final float offsetVectorX = nodeDetails.getLabelOffsetVectorX(nodeView);
			final float offsetVectorY = nodeDetails.getLabelOffsetVectorY(nodeView);

			final double[] doubleBuff1 = new double[4];
			final double[] doubleBuff2 = new double[2];
			
			doubleBuff1[0] = x - nodeWidth/2;
			doubleBuff1[1] = y - nodeHeight/2;
			doubleBuff1[2] = x + nodeWidth/2;
			doubleBuff1[3] = y + nodeHeight/2;
			
			GraphRenderer.lemma_computeAnchor(nodeAnchor, doubleBuff1, doubleBuff2);

			final double nodeAnchorPointX = doubleBuff2[0];
			final double nodeAnchorPointY = doubleBuff2[1];
			
			doubleBuff1[0] = -0.5d * w;
			doubleBuff1[1] = -0.5d * h;
			doubleBuff1[2] = 0.5d * w;
			doubleBuff1[3] = 0.5d * h;
			GraphRenderer.lemma_computeAnchor(textAnchor, doubleBuff1, doubleBuff2);

			final double textXCenter = nodeAnchorPointX - doubleBuff2[0] + offsetVectorX;
			final double textYCenter = nodeAnchorPointY - doubleBuff2[1] + offsetVectorY;			
					
			double aMin = textXCenter - (w/2);
			double aMax = textXCenter + (w/2);
			double bMin = textYCenter - (h/2);
			double bMax = textYCenter + (h/2); 
			
			if( xP > aMin && xP < aMax && yP > bMin && yP <bMax ) {
				locn[0] = aMin;
				locn[1] = bMin;
				re.getTransform().xformNodeToImageCoords (locn);
				double scaleFactor =  re.getTransform().getScaleFactor();
				
				Rectangle r = new Rectangle ((int)locn[0],(int)locn[1], (int)(w*scaleFactor), (int)(h*scaleFactor));
				
				ObjectPosition pos = nodeView.isSet(DVisualLexicon.NODE_LABEL_POSITION) ?
						 nodeView.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION) : null;

				selection = (new DLabelSelection ( nodeView, r, pos, nodeDetails.isSelected(nodeView)));
			}
		} 
				
		return selection;
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
	
	public List<HandleInfo> getHandlesIntersecting(Rectangle r, int maxCount) {
		Rectangle2D selectionArea = re.getTransform().getNodeCoordinates(r);
		
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		EdgeDetails edgeDetails = re.getEdgeDetails();
		
		Rectangle2D.Float area = re.getTransform().getNetworkVisibleAreaNodeCoords();
		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(area.x, area.y, area.x + area.width, area.y + area.height);
		
		List<HandleInfo> resultHandles = maxCount == 1 ? new ArrayList<>(1) : new ArrayList<>();
		Set<Long> processedNodes = new HashSet<>();
		while(nodeHits.hasNext()) {
			long node = nodeHits.next();
			
			Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
			for(View<CyEdge> e : touchingEdges) {
				if(edgeDetails.isSelected(e)) {
					SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(e);
					long otherNode = node ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
					
					if(!processedNodes.contains(otherNode)) {
						Bend bend = edgeDetails.getBend(e);
						if(bend != null) {
							List<Handle> handles = bend.getAllHandles();
							if(handles != null) {
								for(Handle handle : handles) {
									Point2D p = handle.calculateHandleLocation(snapshot, e);
									var size = DEdgeDetails.HANDLE_SIZE;
									if(selectionArea.intersects(p.getX()-(size/2), p.getY()-(size/2), size, size)) {
										resultHandles.add(new HandleInfo(e, bend, handle));
										if(maxCount > 0 && resultHandles.size() >= maxCount) {
											return resultHandles;
										}
									}
								}
							}
						}
					}
				}
			}
			processedNodes.add(node);
		}
		
		return resultHandles;
	}
	
	
	
	private static boolean intersectsLine(Line2D line, GeneralPath path) {
		// This assumes the path is made up of straight line segments
		Point2D p1 = null;
		Point2D p2 = null;
		float[] coords = new float[6];
		
		for(PathIterator iter = path.getPathIterator(null); !iter.isDone(); iter.next()) {
			iter.currentSegment(coords);
			p1 = p2;
			p2 = new Point2D.Float(coords[0], coords[1]);
			
			if(p1 != null) {
				Line2D seg = new Line2D.Float(p1, p2);
				if(seg.intersectsLine(line)) {
					return true;
				}
			}
		}
		return false;
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

	public List<View<CyEdge>> getEdgesInPath(GeneralPath path) {
		List<Long> edges = computeEdgesIntersecting(path);
		return suidsToEdges(edges);
	}
	
	
	private List<Long> computeEdgesIntersecting(GeneralPath path) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		path = re.getTransform().pathInNodeCoords(path);
		if(path == null)
			return Collections.emptyList();
		
		Line2D.Float line = new Line2D.Float();
		float[] extentsBuff = new float[4];
		
		// get viewport bounds in node coords
		Rectangle2D.Float area = re.getTransform().getNetworkVisibleAreaNodeCoords();
		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(area.x, area.y, area.x + area.width, area.y + area.height);
		
		Set<Long> processedNodes = new HashSet<>();
		List<Long> resultEdges = new ArrayList<>();
		
		// AWT has no API for computing the intersection of two general paths, so we must default to treating edges as lines.
		while(nodeHits.hasNext()) {
			long node = nodeHits.nextExtents(extentsBuff);
			
			// MKTODO make this into a utility method
			float nodeX = (extentsBuff[0] + extentsBuff[2]) / 2;
			float nodeY = (extentsBuff[1] + extentsBuff[3]) / 2;
			
			Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
			
			for(View<CyEdge> e : touchingEdges) {
				SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(e);
				long edge = e.getSUID();
				long otherNode = node ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
				
				if(!processedNodes.contains(otherNode)) {
					snapshot.getSpacialIndex2D().get(otherNode, extentsBuff);
					float otherNodeX = (extentsBuff[0] + extentsBuff[2]) / 2;
					float otherNodeY = (extentsBuff[1] + extentsBuff[3]) / 2;
					line.setLine(nodeX, nodeY, otherNodeX, otherNodeY);
					
					if(intersectsLine(line, path)) {
						resultEdges.add(edge);
					}
				}
			}
			processedNodes.add(node);
		}
		return resultEdges;
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
		
		Line2D.Float line = new Line2D.Float();
		float[] extentsBuff = new float[4];
		
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		Rectangle2D.Float area = re.getTransform().getNetworkVisibleAreaNodeCoords();
		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(area.x, area.y, area.x + area.width, area.y + area.height);
		
		Set<Long> processedNodes = new HashSet<>();
		List<Long> resultEdges = new ArrayList<>();
		
		if (getFlags().not(LOD_HIGH_DETAIL)) {
			// We won't need to look up arrows and their sizes.
			while(nodeHits.hasNext()) {
				long node = nodeHits.nextExtents(extentsBuff);
				
				// MKTODO make this into a utility method
				float nodeX = (extentsBuff[0] + extentsBuff[2]) / 2;
				float nodeY = (extentsBuff[1] + extentsBuff[3]) / 2;
				
				Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
				
				for(View<CyEdge> e : touchingEdges) {
					SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(e);
					long edge = e.getSUID();
					long otherNode = node ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
					
					if(!processedNodes.contains(otherNode)) {
						snapshot.getSpacialIndex2D().get(otherNode, extentsBuff);
						float otherNodeX = (extentsBuff[0] + extentsBuff[2]) / 2;
						float otherNodeY = (extentsBuff[1] + extentsBuff[3]) / 2;
						line.setLine(nodeX, nodeY, otherNodeX, otherNodeY);
						
						if(line.intersects(xMin, yMin, xMax - xMin, yMax - yMin)) {
							resultEdges.add(edge);
						}
					}
				}
				processedNodes.add(node);
			}
		} else { // Last render high detail.
			byte[] haystackDataBuff = new byte[16];
				
			float[] extentsBuff2 = new float[4];
			
			while(nodeHits.hasNext()) {
				long node = nodeHits.nextExtents(extentsBuff);
				View<CyNode> nodeView = snapshot.getNodeView(node);
				byte nodeShape = nodeDetails.getShape(nodeView);
				
				Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
				
				for(View<CyEdge> edge : touchingEdges) {
					boolean haystack = edgeDetails.isHaystack(edge);
					
					SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(edge);
					long edgeSuid = edgeInfo.getSUID();
					double segThicknessDiv2 = edgeDetails.getWidth(edge) / 2.0d;
					long otherNode = node ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
					View<CyNode> otherNodeView = snapshot.getNodeView(otherNode);
					
					if(!processedNodes.contains(otherNode)) {
						snapshot.getSpacialIndex2D().get(otherNode, extentsBuff2);
						
						final byte otherNodeShape = nodeDetails.getShape(otherNodeView);
						final byte srcShape;
						final byte trgShape;
						final float[] srcExtents;
						final float[] trgExtents;
						final long srcSuid;
						final long trgSuid;

						if (node == edgeInfo.getSourceViewSUID()) {
							srcShape = nodeShape;
							trgShape = otherNodeShape;
							srcExtents = extentsBuff;
							trgExtents = extentsBuff2;
							srcSuid = node;
							trgSuid = otherNode;
						} else { // node == graph.edgeTarget(edge).
							srcShape = otherNodeShape;
							trgShape = nodeShape;
							srcExtents = extentsBuff2;
							trgExtents = extentsBuff;
							srcSuid = otherNode;
							trgSuid = node;
						}

						final ArrowShape srcArrow;
						final ArrowShape trgArrow;
						final float srcArrowSize;
						final float trgArrowSize;

						final float[] floatBuff1 = new float[2];
						final float[] floatBuff2 = new float[2];
						GeneralPath path  = new GeneralPath();
						GeneralPath path2 = new GeneralPath();
						
						if (getFlags().not(LOD_EDGE_ARROWS) || haystack) {
							srcArrow = trgArrow = ArrowShapeVisualProperty.NONE;
							srcArrowSize = trgArrowSize = 0.0f;
						} else {
							srcArrow = edgeDetails.getSourceArrowShape(edge);
							trgArrow = edgeDetails.getTargetArrowShape(edge);
							srcArrowSize = ((srcArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : edgeDetails.getSourceArrowSize(edge));
							trgArrowSize = ((trgArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : edgeDetails.getTargetArrowSize(edge));
						}

						final EdgeAnchors anchors = getFlags().not(LOD_EDGE_ANCHORS) ? null : edgeDetails.getAnchors(snapshot, edge);

						
						if(haystack) {
							float radiusModifier = edgeDetails.getHaystackRadius(edge);
							if (!GraphRenderer.computeEdgeEndpointsHaystack(srcExtents, trgExtents, floatBuff1, floatBuff2, 
									srcSuid, trgSuid, edgeSuid, haystackDataBuff, radiusModifier))
								continue;
						} else {
							if (!GraphRenderer.computeEdgeEndpoints(srcExtents, srcShape, srcArrow,
				                          srcArrowSize, anchors, trgExtents, trgShape,
				                          trgArrow, trgArrowSize, floatBuff1, floatBuff2))
								continue;
						}

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

				processedNodes.add(node);
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
	
	public List<View<CyNode>> getNodesInPath(GeneralPath path) {
		path = re.getTransform().pathInNodeCoords(path);
		if(path == null)
			return Collections.emptyList();
		List<Long> nodesXSect = getNodesIntersectingPath(path);
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


	public List<Long> getNodesIntersectingPath(GeneralPath path) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		Rectangle2D mbr = path.getBounds2D();
		SpacialIndex2DEnumerator<Long> under = snapshot.getSpacialIndex2D()
				.queryOverlap((float)mbr.getMinX(), (float)mbr.getMinY(), (float)mbr.getMaxX(), (float)mbr.getMaxY());
		if(!under.hasNext())
			return Collections.emptyList();
		
		List<Long> result = new ArrayList<>(under.size());
		float[] extents = new float[4];
		
		if(treatNodeShapesAsRectangle()) {
			while(under.hasNext()) {
				Long suid = under.nextExtents(extents);
				float x = extents[0];
				float y = extents[1];
				float w = extents[2] - x;
				float h = extents[3] - y;
				if(path.intersects(x, y, w, h)) {
					result.add(suid);
				}
			}
		} else {
			while(under.hasNext()) {
				Long suid = under.nextExtents(extents);
				View<CyNode> nodeView = snapshot.getNodeView(suid);
				GeneralPath nodeShape = new GeneralPath();
				GraphGraphics.getNodeShape(nodeDetails.getShape(nodeView),
						extents[0], extents[1],
						extents[2], extents[3], nodeShape);
				Area pathArea = new Area(path);
				Area nodeArea = new Area(nodeShape);
				pathArea.intersect(nodeArea);
				if(!pathArea.isEmpty()) {
					result.add(suid);
				}
			}
		}
		
		return result;
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
