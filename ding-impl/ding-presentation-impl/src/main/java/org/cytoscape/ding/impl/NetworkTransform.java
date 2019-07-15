package org.cytoscape.ding.impl;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.impl.BendStore.HandleKey;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;


/**
 * This class stores the state of transform between the window viewport and the network.
 *
 */
public class NetworkTransform {
	
	private int width;
	private int height;
	
	private double x = 0;
	private double y = 0;
	private double scaleFactor = 1;
	
	private final AffineTransform xform = new AffineTransform();
	
	public NetworkTransform(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getScaleFactor() {
		return scaleFactor;
	}
	
	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
		updateTransform();
	}
	
	public void setCenter(double x, double y)	 {
		this.x = x;
		this.y = y;
		updateTransform();
	}
	
	public void setViewport(int width, int height) {
		this.width = width;
		this.height = height;
		updateTransform();
	}
	
	private void updateTransform() {
		xform.setToTranslation(0.5d * getWidth(), 0.5d * getHeight());
		xform.scale(scaleFactor, scaleFactor);
		xform.translate(-x, -y);
	}
	
	public AffineTransform getAffineTransform() {
		return xform;
	}
	
	public final void xformImageToNodeCoords(final double[] coords) {
		try {
			xform.inverseTransform(coords, 0, coords, 0, 1);
		} catch (java.awt.geom.NoninvertibleTransformException e) {
			throw new RuntimeException("noninvertible matrix - cannot happen");
		}
	}
	
	public final void xformNodeToImageCoords(final double[] coords) {
		xform.transform(coords, 0, coords, 0, 1);
	}
	
	
	private GeneralPath pathInNodeCoords(GeneralPath path) {
		try {
			GeneralPath transformedPath = new GeneralPath(path);
			transformedPath.transform(xform.createInverse());
			return transformedPath;
		} catch (NoninvertibleTransformException e) {
			return null;
		}
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
	
	private List<View<CyNode>> suidsToNodes(CyNetworkViewSnapshot snapshot, NodeDetails nodeDetails, List<Long> suids) {
		List<View<CyNode>> selectedNodes = new ArrayList<>(suids.size());
		for(Long suid : suids) {
			View<CyNode> node = snapshot.getNodeView(suid);
			if(!nodeDetails.isSelected(node)) { // MKTODO is this check necessary? so what if it re-selects a node
				selectedNodes.add(node);
			}
		}
		return selectedNodes;
	}
	
	private List<View<CyEdge>> suidsToEdges(CyNetworkViewSnapshot snapshot, EdgeDetails edgeDetails, List<Long> suids) {
		List<View<CyEdge>> selectedEdges = new ArrayList<>(suids.size());
		for(Long suid : suids) {
			View<CyEdge> edge = snapshot.getEdgeView(suid);
			if(!edgeDetails.isSelected(edge)) { // MKTODO is this check necessary? so what if it re-selects a node
				selectedEdges.add(edge);
			}
		}
		return selectedEdges;
	}
	
	public List<View<CyEdge>> getEdgesInRectangle(CyNetworkViewSnapshot snapshot, EdgeDetails edgeDetails, NodeDetails nodeDetails, int renderDetail, Rectangle r) {
		List<Long> suids = computeEdgesIntersecting(snapshot, edgeDetails, nodeDetails, renderDetail, r.x, r.y, r.x + r.width, r.y + r.height);
		return suidsToEdges(snapshot, edgeDetails, suids);
	}

	public List<View<CyEdge>> getEdgesInPath(CyNetworkViewSnapshot snapshot, EdgeDetails edgeDetails, NodeDetails nodeDetails, int renderDetail, GeneralPath path) {
		List<Long> edges = computeEdgesIntersecting(snapshot, path);
		return suidsToEdges(snapshot, edgeDetails, edges);
	}
	
	
	private List<Long> computeEdgesIntersecting(CyNetworkViewSnapshot snapshot, GeneralPath path) {
		path = pathInNodeCoords(path);
		if(path == null)
			return Collections.emptyList();
		
		Line2D.Float line = new Line2D.Float();
		float[] extentsBuff = new float[4];
		
		// MKTODO this code was copied from GraphRenderer.renderGraph()
		// get viewport bounds
		float image_xMin = (float) (x - ((0.5d * getWidth())  / scaleFactor));
		float image_yMin = (float) (y - ((0.5d * getHeight()) / scaleFactor));
		float image_xMax = (float) (x + ((0.5d * getWidth())  / scaleFactor)); 
		float image_yMax = (float) (y + ((0.5d * getHeight()) / scaleFactor));

		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(image_xMin, image_yMin, image_xMax, image_yMax);
		
		Set<Long> processedNodes = new HashSet<>();
		List<Long> resultEdges = new ArrayList<>();
		
		// AWT has no API for computing the intersection of two general paths, so we must default to treating
		// edges as lines.
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
	
	
	public List<Long> computeEdgesIntersecting(CyNetworkViewSnapshot snapshot, EdgeDetails edgeDetails, NodeDetails nodeDetails, 
			int renderDetail, int xMini, int yMini, int xMaxi, int yMaxi) {
		
		double[] ptBuff = new double[2];
		ptBuff[0] = xMini;
		ptBuff[1] = yMini;
		xformImageToNodeCoords(ptBuff);

		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] = xMaxi;
		ptBuff[1] = yMaxi;
		xformImageToNodeCoords(ptBuff);
		
		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
		
		Line2D.Float line = new Line2D.Float();
		float[] extentsBuff = new float[4];
		
		// MKTODO this code was copied from GraphRenderer.renderGraph()
		// get viewport bounds
		float image_xMin = (float) (x - ((0.5d * getWidth())  / scaleFactor));
		float image_yMin = (float) (y - ((0.5d * getHeight()) / scaleFactor));
		float image_xMax = (float) (x + ((0.5d * getWidth())  / scaleFactor)); 
		float image_yMax = (float) (y + ((0.5d * getHeight()) / scaleFactor));

		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(image_xMin, image_yMin, image_xMax, image_yMax);
		
		Set<Long> processedNodes = new HashSet<>();
		List<Long> resultEdges = new ArrayList<>();
		
		if ((renderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0) {
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
			float[] extentsBuff2 = new float[4];
			
			while(nodeHits.hasNext()) {
				long node = nodeHits.nextExtents(extentsBuff);
				View<CyNode> nodeView = snapshot.getNodeView(node);
				byte nodeShape = nodeDetails.getShape(nodeView);
				
				Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
				
				for(View<CyEdge> edge : touchingEdges) {
					SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(edge);
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

						if (node == edgeInfo.getSourceViewSUID()) {
							srcShape = nodeShape;
							trgShape = otherNodeShape;
							srcExtents = extentsBuff;
							trgExtents = extentsBuff2;
						} else { // node == graph.edgeTarget(edge).
							srcShape = otherNodeShape;
							trgShape = nodeShape;
							srcExtents = extentsBuff2;
							trgExtents = extentsBuff;
						}

						final ArrowShape srcArrow;
						final ArrowShape trgArrow;
						final float srcArrowSize;
						final float trgArrowSize;

						final float[] floatBuff1 = new float[2];
						final float[] floatBuff2 = new float[2];
						GeneralPath path  = new GeneralPath();
						GeneralPath path2 = new GeneralPath();
						
						if ((renderDetail & GraphRenderer.LOD_EDGE_ARROWS) == 0) {
							srcArrow = trgArrow = ArrowShapeVisualProperty.NONE;
							srcArrowSize = trgArrowSize = 0.0f;
						} else {
							srcArrow = edgeDetails.getSourceArrowShape(edge);
							trgArrow = edgeDetails.getTargetArrowShape(edge);
							srcArrowSize = ((srcArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : edgeDetails.getSourceArrowSize(edge));
							trgArrowSize = ((trgArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : edgeDetails.getTargetArrowSize(edge));
						}

						final EdgeAnchors anchors = (((renderDetail
						                              & GraphRenderer.LOD_EDGE_ANCHORS) == 0)
						                             ? null : edgeDetails.getAnchors(snapshot, edge));

						if (!GraphRenderer.computeEdgeEndpoints(srcExtents, srcShape,
						                                        srcArrow, srcArrowSize, anchors,
						                                        trgExtents, trgShape, trgArrow,
						                                        trgArrowSize, floatBuff1, floatBuff2))
							continue;

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
	
	
	public List<View<CyNode>> getNodesInRectangle(CyNetworkViewSnapshot snapshot, NodeDetails nodeDetails, Rectangle r, boolean treatNodeShapesAsRectangle) {
		double[] ptBuff = {r.x, r.y};
		xformImageToNodeCoords(ptBuff);
		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] = r.x + r.width;
		ptBuff[1] = r.y + r.height;
		xformImageToNodeCoords(ptBuff);
		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
		
		List<Long> nodesXSect = getNodesIntersectingRectangle(snapshot, nodeDetails, xMin, yMin, xMax, yMax, treatNodeShapesAsRectangle);
		return suidsToNodes(snapshot, nodeDetails, nodesXSect);
	}
	
	public List<View<CyNode>> getNodesInPath(CyNetworkViewSnapshot snapshot, NodeDetails nodeDetails, GeneralPath path, boolean treatNodeShapesAsRectangle) {
		path = pathInNodeCoords(path);
		if(path == null)
			return Collections.emptyList();
		List<Long> nodesXSect = getNodesIntersectingPath(snapshot, nodeDetails, path, treatNodeShapesAsRectangle);
		return suidsToNodes(snapshot, nodeDetails, nodesXSect);
	}
	
	public List<Long> getNodesIntersectingRectangle(CyNetworkViewSnapshot snapshot, NodeDetails nodeDetails,
			double xMinimum, double yMinimum, double xMaximum, double yMaximum, boolean treatNodeShapesAsRectangle) {

		final float xMin = (float) xMinimum;
		final float yMin = (float) yMinimum;
		final float xMax = (float) xMaximum;
		final float yMax = (float) yMaximum;

		SpacialIndex2DEnumerator<Long> under = snapshot.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
		if (!under.hasNext())
			return Collections.emptyList();

		List<Long> returnVal = new ArrayList<>(under.size());

		if (treatNodeShapesAsRectangle) {
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


	public List<Long> getNodesIntersectingPath(CyNetworkViewSnapshot snapshot, NodeDetails nodeDetails, 
			GeneralPath path, boolean treatNodeShapesAsRectangle) {
		
		Rectangle2D mbr = path.getBounds2D();
		SpacialIndex2DEnumerator<Long> under = snapshot.getSpacialIndex2D()
				.queryOverlap((float)mbr.getMinX(), (float)mbr.getMinY(), (float)mbr.getMaxX(), (float)mbr.getMaxY());
		if(!under.hasNext())
			return Collections.emptyList();
		
		List<Long> result = new ArrayList<>(under.size());
		float[] extents = new float[4];
		
		if(treatNodeShapesAsRectangle) {
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
	

	public List<HandleKey> getHandlesInRectangle(BendStore bendStore, Rectangle r, int renderDetail) {
		if((renderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
			double[] ptBuff = {r.x, r.y};
			xformImageToNodeCoords(ptBuff);
			final float xMin = (float) ptBuff[0];
			final float yMin = (float) ptBuff[1];
			ptBuff[0] = r.x + r.width;
			ptBuff[1] = r.y + r.height;
			xformImageToNodeCoords(ptBuff);
			final float xMax = (float) ptBuff[0];
			final float yMax = (float) ptBuff[1];

			SpacialIndex2DEnumerator<HandleKey> handles = bendStore.queryOverlap(xMin, yMin, xMax, yMax);
			List<HandleKey> list = new ArrayList<>(handles.size());
			while(handles.hasNext()) {
				list.add(handles.next());
			}
			return list;
		}
		return Collections.emptyList();
	}
	
	public List<HandleKey> getHandlesInPath(BendStore bendStore, GeneralPath path) {
		path = pathInNodeCoords(path);
		if(path == null)
			return Collections.emptyList();
	
		Rectangle2D mbr = path.getBounds2D();
		if(mbr == null)
			return Collections.emptyList();
		
		SpacialIndex2DEnumerator<HandleKey> handles = bendStore
				.queryOverlap((float)mbr.getMinX(), (float)mbr.getMinY(), (float)mbr.getMaxX(), (float)mbr.getMaxY());
		
		List<HandleKey> list = new ArrayList<>(handles.size());
		float[] extents = new float[4];
		
		while(handles.hasNext()) {
			HandleKey key = handles.nextExtents(extents);
			float x = extents[0];
			float y = extents[1];
			float w = extents[2] - x;
			float h = extents[3] - y;
			if(path.intersects(x, y, w, h)) {
				list.add(key);
			}
			list.add(handles.next());
		}
		return list;
	}
	
}
