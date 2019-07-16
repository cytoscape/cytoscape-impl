package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Timer;

import org.cytoscape.ding.impl.BendStore.HandleKey;
import org.cytoscape.graph.render.export.ImageImposter;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.ArrowShape;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

/**
 * Canvas to be used for drawing actual network visualization
 */
@SuppressWarnings("serial")
public class InnerCanvas extends DingCanvas {

	public GraphGraphics grafx;
	private final DRenderingEngine re;
	private final CyServiceRegistrar serviceRegistrar;
	
	final DingLock dingLock;
	
	protected GraphLOD lod;
	
	double xCenter;
	double yCenter;
	double scaleFactor;
	
	private int lastRenderDetail;
	private boolean isPrinting;
	private FontMetrics fontMetrics;
	
	private Timer hideEdgesTimer;
	
	InnerCanvas(DingLock lock, DRenderingEngine re, CyServiceRegistrar serviceRegistrar) {
		super(DRenderingEngine.Canvas.NETWORK_CANVAS);
		this.dingLock = lock;
		this.re = re;
		this.serviceRegistrar = serviceRegistrar;
		this.lod = new GraphLOD(); // Default LOD.
		this.m_backgroundColor = Color.WHITE;
		setOpaque(false);
		this.xCenter = 0.0d;
		this.yCenter = 0.0d;
		this.scaleFactor = 1.0d;

		// Timer to reset edge drawing
		ActionListener taskPerformer = evt -> {
			hideEdgesTimer.stop();
			lod.setDrawEdges(true);
			re.setViewportChanged();
			repaint();
		};
		hideEdgesTimer = new Timer(600, taskPerformer);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		boolean resized = (this.getWidth() != width) || (this.getHeight() != height);
        boolean moved = (this.getX() != x) || (this.getY() != y);
        
        if (!resized && !moved)
            return;
		
		super.setBounds(x, y, width, height);

		if ((width > 0) && (height > 0)) {
			Image img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			GraphGraphics grafx = new GraphGraphics(img, false, true);

			synchronized (dingLock) {
				this.grafx = grafx;
				if (re != null)
					re.setViewportChanged();
			}
		}
	}

	@Override
	public void update(Graphics g) {
		if (grafx == null || re == null)
			return;

		// This is the magical portion of code that transfers what is in the
		// visual data structures into what's on the image.
		boolean contentChanged = false;
		boolean viewportChanged = false;
		double xCenter = 0.0d;
		double yCenter = 0.0d;
		double scaleFactor = 1.0d;

		this.fontMetrics = g.getFontMetrics();

		synchronized (dingLock) {
			if (re != null && re.isDirty()) {
				contentChanged = re.isContentChanged();
				viewportChanged = re.isViewportChanged();
				renderGraph(grafx,/* setLastRenderDetail = */ true, lod);
				xCenter = this.xCenter;
				yCenter = this.yCenter;
				scaleFactor = this.scaleFactor;
				
				// set the publicly accessible image object *after* it has been rendered
				m_img = grafx.image;
			}
		}

		// if canvas is visible, draw it
		if (isVisible()) {
			// TODO Should this be on the AWT thread?
			g.drawImage(grafx.image, 0, 0, null);
		}

		if (contentChanged && re != null) {
			re.fireContentChanged();
		}
		if (viewportChanged && re != null) {
			re.fireViewportChanged(getWidth(), getHeight(), xCenter, yCenter, scaleFactor);
		}
	}


	@Override
	public void paint(Graphics g) {
		update(g);
	}

	@Override
	public void print(Graphics g) {
		isPrinting = true;
		
		final int w = getWidth();
		final int h = getHeight();
		
		if (re != null && w > 0 && h > 0)
			renderGraph(
					new GraphGraphics(new ImageImposter(g, w, h), /* debug = */ false, /* clear = */ false), 
					/* setLastRenderDetail = */ false, re.getPrintLOD());
		
		isPrinting = false;
	}

	@Override
	public void printNoImposter(Graphics g) {
		isPrinting = true;
		final Image img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		if (re != null)
			renderGraph(new GraphGraphics(img, false, false), /* setLastRenderDetail = */ false, re.getPrintLOD());
		
		isPrinting = false;
	}

	/**
 	 * Return true if this view is curerntly being printed (as opposed to painted on the screen)
 	 * @return true if we're currently being printed, false otherwise
 	 */
	public boolean isPrinting() { 
		return isPrinting; 
	}

	/**
	 * This method exposes the JComponent processMouseEvent so that canvases on
	 * top of us can pass events they don't want down.
	 * 
	 * @param e the MouseEvent to process
	 */
	@Override
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
	}

	
	private List<View<CyNode>> suidsToNodes(List<Long> suids) {
		NodeDetails nodeDetails = re.getNodeDetails();
		List<View<CyNode>> selectedNodes = new ArrayList<>(suids.size());
		for(Long suid : suids) {
			View<CyNode> node = re.getViewModelSnapshot().getNodeView(suid);
			if(!nodeDetails.isSelected(node)) { // MKTODO is this check necessary? so what if it re-selects a node
				selectedNodes.add(node);
			}
		}
		return selectedNodes;
	}
	
	private List<View<CyEdge>> suidsToEdges(List<Long> suids) {
		EdgeDetails details = re.getEdgeDetails();
		List<View<CyEdge>> selectedEdges = new ArrayList<>(suids.size());
		for(Long suid : suids) {
			View<CyEdge> edge = re.getViewModelSnapshot().getEdgeView(suid);
			if(!details.isSelected(edge)) { // MKTODO is this check necessary? so what if it re-selects a node
				selectedEdges.add(edge);
			}
		}
		return selectedEdges;
	}
	
	private GeneralPath pathInNodeCoords(GeneralPath path) {
		try {
			GeneralPath transformedPath = new GeneralPath(path);
			transformedPath.transform(getAffineTransform().createInverse());
			return transformedPath;
		} catch (NoninvertibleTransformException e) {
			return null;
		}
	}
	
	private boolean treatNodeShapesAsRectangle() {
		return (lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0;
	}
	
	public List<View<CyNode>> getNodesInRectangle(Rectangle r) {
		double[] ptBuff = {r.x, r.y};
		re.xformComponentToNodeCoords(ptBuff);
		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] = r.x + r.width;
		ptBuff[1] = r.y + r.height;
		re.xformComponentToNodeCoords(ptBuff);
		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
		
		List<Long> nodesXSect = re.getNodesIntersectingRectangle(xMin, yMin, xMax, yMax, treatNodeShapesAsRectangle());
		return suidsToNodes(nodesXSect);
	}
	
	public List<View<CyNode>> getNodesInPath(GeneralPath path) {
		path = pathInNodeCoords(path);
		if(path == null)
			return Collections.emptyList();
		List<Long> nodesXSect = re.getNodesIntersectingPath(path, treatNodeShapesAsRectangle());
		return suidsToNodes(nodesXSect);
	}

	public List<HandleKey> getHandlesInRectangle(Rectangle r) {
		if((lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
			double[] ptBuff = {r.x, r.y};
			re.xformComponentToNodeCoords(ptBuff);
			final float xMin = (float) ptBuff[0];
			final float yMin = (float) ptBuff[1];
			ptBuff[0] = r.x + r.width;
			ptBuff[1] = r.y + r.height;
			re.xformComponentToNodeCoords(ptBuff);
			final float xMax = (float) ptBuff[0];
			final float yMax = (float) ptBuff[1];

			SpacialIndex2DEnumerator<HandleKey> handles = re.getBendStore().queryOverlap(xMin, yMin, xMax, yMax);
			List<HandleKey> list = new ArrayList<>(handles.size());
			while(handles.hasNext()) {
				list.add(handles.next());
			}
			return list;
		}
		return Collections.emptyList();
	}
	
	public List<HandleKey> getHandlesInPath(GeneralPath path) {
		path = pathInNodeCoords(path);
		if(path == null)
			return Collections.emptyList();
	
		Rectangle2D mbr = path.getBounds2D();
		if(mbr == null)
			return Collections.emptyList();
		
		SpacialIndex2DEnumerator<HandleKey> handles = re.getBendStore()
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
	
	public List<View<CyEdge>> getEdgesInRectangle(Rectangle r) {
		List<Long> suids = computeEdgesIntersecting(r.x, r.y, r.x + r.width, r.y + r.height);
		return suidsToEdges(suids);
	}

	public List<View<CyEdge>> getEdgesInPath(GeneralPath path) {
		List<Long> edges = computeEdgesIntersecting(path);
		return suidsToEdges(edges);
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
	

	
	final List<Long> computeEdgesIntersecting(GeneralPath path) {
		path = pathInNodeCoords(path);
		if(path == null)
			return Collections.emptyList();
		
		Line2D.Float line = new Line2D.Float();
		float[] extentsBuff = new float[4];
		
		// MKTODO this code was copied from GraphRenderer.renderGraph()
		// get viewport bounds
		float image_xMin = (float) (xCenter - ((0.5d * grafx.image.getWidth(null)) / scaleFactor));
		float image_yMin = (float) (yCenter - ((0.5d * grafx.image.getHeight(null)) / scaleFactor));
		float image_xMax = (float) (xCenter + ((0.5d * grafx.image.getWidth(null)) / scaleFactor)); 
		float image_yMax = (float) (yCenter + ((0.5d * grafx.image.getHeight(null)) / scaleFactor));

		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
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
	
	
	
	// Puts [last drawn] edges intersecting onto stack; as RootGraph indices.
	// Depends on the state of several member variables, such as m_hash.
	// Clobbers m_stack and m_ptBuff.
	// The rectangle extents are in component coordinate space.
	// IMPORTANT: Code that calls this method should be holding m_lock.
	final List<Long> computeEdgesIntersecting(final int xMini, final int yMini, final int xMaxi, final int yMaxi) {
		double[] ptBuff = new double[2];
		ptBuff[0] = xMini;
		ptBuff[1] = yMini;
		re.xformComponentToNodeCoords(ptBuff);

		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] = xMaxi;
		ptBuff[1] = yMaxi;
		re.xformComponentToNodeCoords(ptBuff);
		
		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
		
		Line2D.Float line = new Line2D.Float();
		float[] extentsBuff = new float[4];
		
		// MKTODO this code was copied from GraphRenderer.renderGraph()
		// get viewport bounds
		float image_xMin = (float) (xCenter - ((0.5d * grafx.image.getWidth(null)) / scaleFactor));
		float image_yMin = (float) (yCenter - ((0.5d * grafx.image.getHeight(null)) / scaleFactor));
		float image_xMax = (float) (xCenter + ((0.5d * grafx.image.getWidth(null)) / scaleFactor)); 
		float image_yMax = (float) (yCenter + ((0.5d * grafx.image.getHeight(null)) / scaleFactor));

		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(image_xMin, image_yMin, image_xMax, image_yMax);
		
		Set<Long> processedNodes = new HashSet<>();
		List<Long> resultEdges = new ArrayList<>();
		
		if ((lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0) {
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
				byte nodeShape = re.getNodeDetails().getShape(nodeView);
				
				Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
				
				for(View<CyEdge> edge : touchingEdges) {
					SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(edge);
					double segThicknessDiv2 = re.getEdgeDetails().getWidth(edge) / 2.0d;
					long otherNode = node ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
					View<CyNode> otherNodeView = snapshot.getNodeView(otherNode);
					
					if(!processedNodes.contains(otherNode)) {
						snapshot.getSpacialIndex2D().get(otherNode, extentsBuff2);
						
						final byte otherNodeShape = re.getNodeDetails().getShape(otherNodeView);
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
						
						if ((lastRenderDetail & GraphRenderer.LOD_EDGE_ARROWS) == 0) {
							srcArrow = trgArrow = ArrowShapeVisualProperty.NONE;
							srcArrowSize = trgArrowSize = 0.0f;
						} else {
							srcArrow = re.getEdgeDetails().getSourceArrowShape(edge);
							trgArrow = re.getEdgeDetails().getTargetArrowShape(edge);
							srcArrowSize = ((srcArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : re.getEdgeDetails().getSourceArrowSize(edge));
							trgArrowSize = ((trgArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : re.getEdgeDetails().getTargetArrowSize(edge));
						}

						final EdgeAnchors anchors = (((lastRenderDetail
						                              & GraphRenderer.LOD_EDGE_ANCHORS) == 0)
						                             ? null : re.getEdgeDetails().getAnchors(snapshot, edge));

						if (!GraphRenderer.computeEdgeEndpoints(grafx, srcExtents, srcShape,
						                                        srcArrow, srcArrowSize, anchors,
						                                        trgExtents, trgShape, trgArrow,
						                                        trgArrowSize, floatBuff1, floatBuff2))
							continue;

						grafx.getEdgePath(srcArrow, srcArrowSize, trgArrow, trgArrowSize,
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


    /**
     * When the center is changed, this method ought to be called rather than modifying m_xCenter and m_yCenter
     * directly so that edges maintain appropriate starting points at the center of whatever node they are associated with.
     */
    void setCenter(double x, double y)  {
        xCenter = x;
        yCenter = y;
    }

	public void adjustZoom(int ticks) {
		if(re.getViewModelSnapshot().isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR))
			return;
		
		double factor;
		if (ticks < 0)
			factor = 1.1; // scroll up, zoom in
		else if (ticks > 0)
			factor = 0.9; // scroll down, zoom out
		else
			return;
		
		synchronized(dingLock) {
			scaleFactor *= factor;
		}
		
		setHideEdges();
		re.setViewportChanged();
		re.getViewModel().batch(netView -> {
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, scaleFactor);
		}, false);
		repaint();
	}


	public int getLastRenderDetail() {
		return lastRenderDetail;
	}

	public FontMetrics getFontMetrics() {
		return fontMetrics;
	}
	
	/**
	 * Called to get the tranform matrix used by the inner canvas
	 * to move the nodes.
	 */
	public AffineTransform getAffineTransform() {
		return (grafx != null) ? grafx.getTransform() : null;
	}

	public void updateSubgraph(List<View<CyNode>> nodes, List<View<CyEdge>> edges) {
		renderSubgraph(grafx, false, lod, nodes, edges);
	}

	// Render just a portion of the graph.  This is used for selections when we want to overwrite
	// a limited number of nodes and edges
	private void renderSubgraph(GraphGraphics graphics, final boolean setLastRenderDetail, final GraphLOD lod, 
	                            List<View<CyNode>> nodes, List<View<CyEdge>> edges) {

		// Pass the color even though we won't use it if we actually only render the subgraph.  If we're
		// not in largeModel mode, or we're only painting a small portion of the network, we'll wind up
		// calling renderGraph anyways and we'll need to clear the image
		final Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(), m_backgroundColor.getBlue(), 0);

//		synchronized (m_lock) {
			int lastRenderDetail = re.renderSubgraph(graphics, lod, backgroundColor, xCenter, yCenter, scaleFactor, nodes, edges);
			if (setLastRenderDetail)
				this.lastRenderDetail = lastRenderDetail;
//		}

		repaint();
	}
	
	/**
	 *  @param setLastRenderDetail if true, "m_lastRenderDetail" will be updated, otherwise it will not be updated.
	 */
	private void renderGraph(GraphGraphics graphics, boolean setLastRenderDetail, GraphLOD lod) {
		int alpha = (isOpaque()) ? 255 : 0;
		Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(), m_backgroundColor.getBlue(), alpha);

		// long timeBegin = System.currentTimeMillis();
		int lastRenderDetail = re.renderGraph(graphics, lod, backgroundColor, xCenter, yCenter, scaleFactor);
		// System.out.println("Rendered graph in "+(System.currentTimeMillis()-timeBegin)+"ms");

		if (setLastRenderDetail)
			this.lastRenderDetail = lastRenderDetail;
		// repaint();
	}



	public double getScaleFactor(){
		return scaleFactor;
	}

	public void dispose() {
	}
	
	public void ensureInitialized() {
		if (!grafx.isInitialized()) {
			grafx.setTransform(xCenter, yCenter, scaleFactor);
		}
	}

	public void setHideEdges() {
		hideEdgesTimer.stop();
		lod.setDrawEdges(false);
		hideEdgesTimer.start();
	}
}
