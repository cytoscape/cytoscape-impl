package org.cytoscape.ding.impl;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Icon;
import javax.swing.Timer;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.PrintLOD;
import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.ding.impl.BendStore.HandleKey;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.internal.util.CoalesceTimer;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.model.spacial.SpacialIndex2DFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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


public class DRenderingEngine implements RenderingEngine<CyNetwork>, Printable, ActionListener, CyNetworkViewListener {

	private static final Logger logger = LoggerFactory.getLogger(DRenderingEngine.class);
	
	
	protected static int DEF_SNAPSHOT_SIZE = 400;
	
	public enum Canvas {
		BACKGROUND_CANVAS, NETWORK_CANVAS, FOREGROUND_CANVAS;
	}

	private final CyServiceRegistrar serviceRegistrar;
	private final DVisualLexicon lexicon;

	private final CyNetworkView viewModel;
	private CyNetworkViewSnapshot viewModelSnapshot;
	
	// Common object lock used for state synchronization
	final DingLock dingLock = new DingLock();

	private final NodeDetails nodeDetails;
	private final EdgeDetails edgeDetails;
	
	private PrintLOD printLOD;

	private InnerCanvas networkCanvas;
	private ArbitraryGraphicsCanvas backgroundCanvas;
	private ArbitraryGraphicsCanvas foregroundCanvas;
	
	private int imageWidth = 0;
	private int imageHeight = 0;

	private boolean nodeSelection = true;
	private boolean edgeSelection = true;
	private boolean annotationSelection = true;

	//Flag that indicates that the content has changed and the graph needs to be redrawn.
	private volatile boolean contentChanged;
	// State variable for when zooming/panning have changed.
	private volatile boolean viewportChanged;

	private final List<ContentChangeListener>  contentChangeListeners  = new CopyOnWriteArrayList<>();
	private final List<ViewportChangeListener> viewportChangeListeners = new CopyOnWriteArrayList<>();


	// Snapshot of current view.  Will be updated by CONTENT_CHANGED event.
	private BufferedImage snapshotImage;
	// Represents current snapshot is latest version or not.
	private boolean latest;

	private final Properties props;
	private final CyAnnotator cyAnnotator;
	private boolean annotationsLoaded;
	private boolean largeModel = false;
	private boolean haveZOrder = true; // MKTODO assume node zorder matters, maybe remove this
	private final DingGraphLODAll dingGraphLODAll = new DingGraphLODAll();
	private final DingGraphLOD dingGraphLOD;
	
	private Timer animationTimer;
	private final Timer checkDirtyTimer;
	private final CoalesceTimer coalesceTimer;
	
	private final BendStore bendStore;
	private InputHandlerGlassPane inputHandler = null;
	
	
	public DRenderingEngine(
			final CyNetworkView view,
			final DVisualLexicon dingLexicon,
			final AnnotationFactoryManager annMgr,
			final DingGraphLOD dingGraphLOD,
			final HandleFactory handleFactory,
			final CyServiceRegistrar registrar
	) {
		this.serviceRegistrar = registrar;
		this.props = new Properties();
		this.viewModel = view;
		this.lexicon = dingLexicon;
		this.dingGraphLOD = dingGraphLOD;
		
		SpacialIndex2DFactory spacialIndexFactory = registrar.getService(SpacialIndex2DFactory.class);
		this.bendStore = new BendStore(this, handleFactory, spacialIndexFactory);
		
		nodeDetails = new DNodeDetails(this);
		edgeDetails = new DEdgeDetails(this);
		printLOD = new PrintLOD();
		
		networkCanvas = new InnerCanvas(dingLock, this, registrar);
		backgroundCanvas = new ArbitraryGraphicsCanvas(this, Canvas.BACKGROUND_CANVAS, networkCanvas, Color.white, true);
		addViewportChangeListener(backgroundCanvas);
		foregroundCanvas = new ArbitraryGraphicsCanvas(this, Canvas.FOREGROUND_CANVAS, networkCanvas, Color.white, false);
		addViewportChangeListener(foregroundCanvas);

		setGraphLOD(dingGraphLOD);

		// Finally, intialize our annotations
		cyAnnotator = new CyAnnotator(this, annMgr, registrar);
		registrar.registerService(cyAnnotator, SessionAboutToBeSavedListener.class, new Properties());
		
		//Updating the snapshot for nested networks
		this.addContentChangeListener(new DGraphViewContentChangeListener());

		CyNetworkViewSnapshot snapshot = view.createSnapshot();
		if (!dingGraphLOD.detail(snapshot.getNodeCount(), snapshot.getEdgeCount()))
			largeModel = true;

		viewModelSnapshot = viewModel.createSnapshot();
		
		coalesceTimer = new CoalesceTimer();
		
		// Check if the view model has changed approximately 30 times per second
		checkDirtyTimer = new Timer(30, e -> checkModelIsDirty());
		checkDirtyTimer.setRepeats(true);
		checkDirtyTimer.start();
	}
	
	
	/**
	 * TEMPORARY
	 * 
	 * This is being called by a Swing Timer, so this method is being run on the EDT.
	 * Painting is also done on the EDT. This is how we make sure that viewModelSnapshot does not
	 * change while a frame is being rendered.
	 * 
	 * Also the EDT will coalesce paint events, so if the timer runs faster than the frame rate the
	 * EDT will take care of that.
	 * 
	 * MKTODO Move drawing off the EDT.
	 * If we move drawing off the EDT then we need another solution for ensuring that viewModelSnapshot
	 * does not get re-assigned while a frame is being drawn.
	 */
	private void checkModelIsDirty() {
		if(viewModel.isDirty()) {
			updateSnapshotAndView();
		}
	}
	
	private void updateSnapshotAndView() {
		// create a new snapshot, this should be very fast
		viewModelSnapshot = viewModel.createSnapshot();
		
		// Check for important changes between snapshots
		Paint backgroundPaint = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		if(!backgroundPaint.equals(networkCanvas.getBackground())) {
			setBackgroundPaint(backgroundPaint);
		}
		
		Collection<View<CyEdge>> selectedEdges = viewModelSnapshot.getTrackedEdges(CyNetworkViewConfig.SELECTED_EDGES);
		bendStore.updateSelectedEdges(selectedEdges);
		
		Collection<View<CyEdge>> animatedEdges = viewModelSnapshot.getTrackedEdges(DingNetworkViewFactory.ANIMATED_EDGES);
		edgeDetails.updateAnimatedEdges(animatedEdges);
		if(animatedEdges.isEmpty() && animationTimer != null) {
			animationTimer.stop();
			animationTimer = null;
		} else if(!animatedEdges.isEmpty() && animationTimer == null) {
			animationTimer = new Timer(200, e -> advanceAnimatedEdges());
			animationTimer.setRepeats(true);
			animationTimer.start();
		}
		
		// update LOD
		boolean hd = viewModelSnapshot.getVisualProperty(DVisualLexicon.NETWORK_FORCE_HIGH_DETAIL);
		setGraphLOD(hd ? dingGraphLODAll : dingGraphLOD);
		
		// update view (for example if "fit selected" was run)
		double x = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
		double y = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
		if(networkCanvas.xCenter != x || networkCanvas.yCenter != y) {
			setCenter(x, y);
		}
		
		double scaleFactor = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		if(networkCanvas.scaleFactor != scaleFactor) {
			setZoom(scaleFactor);
		}
		
		updateView(true);
	}
	
	
	private void advanceAnimatedEdges() {
		edgeDetails.advanceAnimatedEdges();
		// This is more lightweight than calling updateView(). And if the animation thread is faster 
		// than the renderer the EDT will coalesce the extra paint events.
		setContentChanged();
		networkCanvas.repaint();
	}
	
	
	public BendStore getBendStore() {
		return bendStore;
	}
	
	public synchronized InputHandlerGlassPane getInputHandlerGlassPane() {
		if(inputHandler == null) {
			inputHandler = new InputHandlerGlassPane(serviceRegistrar, this);
		}
		return inputHandler;
	}

	public boolean isNodeSelectionEnabled() {
		return nodeSelection;
	}

	public boolean isEdgeSelectionEnabled() {
		return edgeSelection;
	}
	
	public boolean isAnnotationSelectionEnabled() {
		return annotationSelection;
	}

	public void enableNodeSelection() {
		synchronized (dingLock) {
			nodeSelection = true;
		}
	}

	public void disableNodeSelection() {
		synchronized (dingLock) {
			nodeSelection = false;
		}
	}

	public void enableEdgeSelection() {
		synchronized (dingLock) {
			edgeSelection = true;
		}
	}

	public void disableEdgeSelection() {
		synchronized (dingLock) {
			edgeSelection = false;
		}
	}
	
	public void enableAnnotationSelection() {
		synchronized (dingLock) {
			annotationSelection = true;
		}
	}
	
	public void disableAnnotationSelection() {
		synchronized (dingLock) {
			annotationSelection = false;
		}
	}

	public void setBackgroundPaint(Paint paint) {
		synchronized (dingLock) {
			if (paint instanceof Color) {
				backgroundCanvas.setBackground((Color) paint);
				networkCanvas.setBackground((Color)paint); // for antialiasing...
				foregroundCanvas.setBackground((Color)paint); // for antialiasing...
				setContentChanged();
			} else {
				logger.debug("DGraphView.setBackgroundPaint(), Color not found.");
			}
		}
	}

	public Paint getBackgroundPaint() {
		return backgroundCanvas.getBackground();
	}

	
	public boolean isNodeSelected(long suid) {
		return nodeDetails.isSelected(getViewModelSnapshot().getNodeView(suid));
	}
	
	public boolean isNodeSelected(View<CyNode> nodeView) {
		return nodeDetails.isSelected(nodeView);
	}
	
	public boolean isEdgeSelected(long suid) {
		return edgeDetails.isSelected(getViewModelSnapshot().getEdgeView(suid));
	}
	
	public boolean isEdgeSelected(View<CyEdge> edgeView) {
		return edgeDetails.isSelected(edgeView);
	}
	
	public NodeDetails getNodeDetails() {
		return nodeDetails;
	}
	
	public EdgeDetails getEdgeDetails() {
		return edgeDetails;
	}
	
	public boolean isDirty() {
		// MKTODO this method probably isn't needed anymore, right?
		return isContentChanged() || isViewportChanged();
	}
	
	public boolean isContentChanged() {
		return contentChanged;
	}
	
	public void setContentChanged() {
		setContentChanged(true);
	}
	
	private void setContentChanged(final boolean b) {
		contentChanged = b;
	}
	
	public boolean isViewportChanged() {
		return viewportChanged;
	}
	
	public void setViewportChanged() {
		setViewportChanged(true);
	}
	
	private void setViewportChanged(final boolean b) {
		viewportChanged = b;
	}
	

	public double getZoom() {
		return networkCanvas.scaleFactor;
	}

	public boolean isLargeModel() {
		return largeModel;
	}
	
	public PrintLOD getPrintLOD() {
		return printLOD;
	}
	
	/**
	 * Set the zoom level and redraw the view.
	 */
	public void setZoom(double zoom) {
		synchronized (dingLock) {
			networkCanvas.scaleFactor = checkZoom(zoom, networkCanvas.scaleFactor);
			setViewportChanged();
		}
	}

	private void fitContent(final boolean updateView) {
		serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();

		// MKTODO why does this have to run on the edt?
		invokeOnEDT(() -> {
			synchronized (dingLock) {
				// make sure we use the latest snapshot
				CyNetworkViewSnapshot netViewSnapshot = viewModel.createSnapshot();
				if(netViewSnapshot.getNodeCount() == 0)
					return;
				if (networkCanvas.getWidth() == 0 || networkCanvas.getHeight() == 0)
					return;
				
				double[] extentsBuff = new double[4];
				netViewSnapshot.getSpacialIndex2D().getMBR(extentsBuff);
	
				// Adjust the content based on the foreground canvas
				foregroundCanvas.adjustBounds(extentsBuff);
				// Adjust the content based on the background canvas
				backgroundCanvas.adjustBounds(extentsBuff);
	
				CyNetworkView netView = netViewSnapshot.getMutableNetworkView();
				
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION))
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, (extentsBuff[0] + extentsBuff[2]) / 2.0d);
				
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION))
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, (extentsBuff[1] + extentsBuff[3]) / 2.0d);
	
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR)) {
					// Apply a factor 0.98 to zoom, so that it leaves a small border around the network and any annotations.
					final double zoom = Math.min(((double) networkCanvas.getWidth()) / 
					                             (extentsBuff[2] - extentsBuff[0]), 
					                              ((double) networkCanvas.getHeight()) / 
					                             (extentsBuff[3] - extentsBuff[1])) * 0.98;
					// Update view model.  Zoom Level should be modified.
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
				}
			}
			
			if (updateView)
				updateSnapshotAndView();
		});
	}
	
	@Override
	public void handleFitContent() {
		fitContent(/* updateView = */ true);
	}
	
	@Override
	public void handleUpdateView() {
		updateSnapshotAndView();
	}
	
	public void updateView() {
		updateView(false);
	}
	
	private void updateView(final boolean forceRedraw) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		if(eventHelper == null)
			return; // shutting down
		eventHelper.flushPayloadEvents();
		
		invokeOnEDTAndWait(() -> {
			if (forceRedraw)
				setContentChanged();
			
			networkCanvas.repaint();
			
			//Check if image size has changed if so, visual property needs to be changed as well
			if (networkCanvas.getWidth() != imageWidth || networkCanvas.getHeight() != imageHeight) {
				imageWidth = networkCanvas.getWidth();
				imageHeight = networkCanvas.getHeight();
				CyNetworkView netView = getViewModel();
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, (double) imageWidth);
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, (double) imageHeight);
			}
		});
		
		// Fire this event on another thread so that it doesn't block the renderer
		coalesceTimer.coalesce(() -> eventHelper.fireEvent(new UpdateNetworkPresentationEvent(getViewModel())));
	}
	
	public void pan(double deltaX, double deltaY) {
		synchronized (dingLock) {
			double x = networkCanvas.xCenter + deltaX;
			double y = networkCanvas.yCenter + deltaY;
			setCenter(x, y);
		}
		networkCanvas.setHideEdges();
		networkCanvas.repaint();
	}
	
	public void setCenter(double x, double y) {
		synchronized (dingLock) {
            networkCanvas.setCenter(x,y);
			setViewportChanged();
			
			// Update view model
			// TODO: don't do it from here?
			CyNetworkView netView = getViewModel();
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, networkCanvas.xCenter);
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, networkCanvas.yCenter);
		}
	}

	public Point2D getCenter() {
		synchronized (dingLock) {
			return new Point2D.Double(networkCanvas.xCenter, networkCanvas.yCenter);
		}
	}
	
	@Override
	public void handleFitSelected() {
		fitSelected();
	}

	public void fitSelected() {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();
		
//		synchronized (m_lock) {
			CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
			SpacialIndex2D<Long> spacial = netViewSnapshot.getSpacialIndex2D();
			Collection<View<CyNode>> selectedElms = netViewSnapshot.getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES);
			if(selectedElms.isEmpty())
				return;
			
			float[] extentsBuff = new float[4];

			float xMin = Float.POSITIVE_INFINITY;
			float yMin = Float.POSITIVE_INFINITY;
			float xMax = Float.NEGATIVE_INFINITY;
			float yMax = Float.NEGATIVE_INFINITY;

			View<CyNode> leftMost = null;
			View<CyNode> rightMost = null;

			for(View<CyNode> nodeView : selectedElms) {
				spacial.get(nodeView.getSUID(), extentsBuff);
				if (extentsBuff[0] < xMin) {
					xMin = extentsBuff[0];
					leftMost = nodeView;
				}

				if (extentsBuff[2] > xMax) {
					xMax = extentsBuff[2];
					rightMost = nodeView;
				}

				yMin = Math.min(yMin, extentsBuff[1]);
				yMax = Math.max(yMax, extentsBuff[3]);
			}

			xMin = xMin - (getLabelWidth(leftMost) / 2);
			xMax = xMax + (getLabelWidth(rightMost) / 2);

			CyNetworkView netView = netViewSnapshot.getMutableNetworkView();
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
				double zoom = Math.min(((double) networkCanvas.getWidth())
						/ (((double) xMax) - ((double) xMin)),
						((double) networkCanvas.getHeight())
								/ (((double) yMax) - ((double) yMin)));
				zoom = checkZoom(zoom, networkCanvas.scaleFactor);
				
				// Update view model.  Zoom Level should be modified.
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
			}
			
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)) {
				double xCenter = (((double) xMin) + ((double) xMax)) / 2.0d;
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, xCenter);
			}
			
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
				double yCenter = (((double) yMin) + ((double) yMax)) / 2.0d;
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, yCenter);
			}
//		}
			
		updateSnapshotAndView();
	}

	private int getLabelWidth(View<CyNode> nodeView) {
		if (nodeView == null)
			return 0;

		String s = nodeDetails.getLabelText(nodeView);
		if (s == null)
			return 0;

		char[] lab = s.toCharArray();
		if (lab == null)
			return 0;

		FontMetrics fontMetrics = networkCanvas.getFontMetrics();
		if (fontMetrics == null)
			return 0;
		return fontMetrics.charsWidth(lab, 0, lab.length);
	}

	
	public void setGraphLOD(GraphLOD lod) {
		synchronized (dingLock) {
			if(lod != networkCanvas.lod) {
				networkCanvas.lod = lod;
				setContentChanged();
			}
		}
	}

	public GraphLOD getGraphLOD() {
		return networkCanvas.lod;
	}

	public void setPrintingTextAsShape(boolean textAsShape) {
		synchronized (dingLock) {
			printLOD.setPrintingTextAsShape(textAsShape);
		}
	}

	/**
	 * Efficiently computes the set of nodes intersecting an axis-aligned query
	 * rectangle; the query rectangle is specified in the node coordinate
	 * system, not the component coordinate system.
	 * <p>
	 * NOTE: The order of elements placed on the stack follows the rendering
	 * order of nodes; the element waiting to be popped off the stack is the
	 * node that is rendered last, and thus is "on top of" other nodes
	 * potentially beneath it.
	 * <p>
	 * HINT: To perform a point query simply set xMin equal to xMax and yMin
	 * equal to yMax.
	 *
	 * @param xMinimum
	 *            a boundary of the query rectangle: the minimum X coordinate.
	 * @param yMinimum
	 *            a boundary of the query rectangle: the minimum Y coordinate.
	 * @param xMaximum
	 *            a boundary of the query rectangle: the maximum X coordinate.
	 * @param yMaximum
	 *            a boundary of the query rectangle: the maximum Y coordinate.
	 * @param treatNodeShapesAsRectangle
	 *            if true, nodes are treated as rectangles for purposes of the
	 *            query computation; if false, true node shapes are respected,
	 *            at the expense of slowing down the query by a constant factor.
	 * @param returnVal
	 *            RootGraph indices of nodes intersecting the query rectangle
	 *            will be placed onto this stack; the stack is not emptied by
	 *            this method initially.
	 */
	public List<Long> getNodesIntersectingRectangle(double xMinimum, double yMinimum, double xMaximum,
	                                          double yMaximum, boolean treatNodeShapesAsRectangle) {
	
		CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
		
		final float xMin = (float) xMinimum;
		final float yMin = (float) yMinimum;
		final float xMax = (float) xMaximum;
		final float yMax = (float) yMaximum;
		
		SpacialIndex2DEnumerator<Long> under = netViewSnapshot.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
		if(!under.hasNext())
			return Collections.emptyList();
		
		List<Long> returnVal = new ArrayList<>(under.size());
		
		if (treatNodeShapesAsRectangle) {
			while(under.hasNext()) {
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
				View<CyNode> cyNode = netViewSnapshot.getNodeView(suid);

				// The only way that the node can miss the intersection query is
				// if it intersects one of the four query rectangle's corners.
				if (((extentsBuff[0] < xMin) && (extentsBuff[1] < yMin))
				    || ((extentsBuff[0] < xMin) && (extentsBuff[3] > yMax))
				    || ((extentsBuff[2] > xMax) && (extentsBuff[3] > yMax))
				    || ((extentsBuff[2] > xMax) && (extentsBuff[1] < yMin))) {
					
					GeneralPath path = new GeneralPath();
					networkCanvas.grafx.getNodeShape(nodeDetails.getShape(cyNode),
							extentsBuff[0], extentsBuff[1],
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
	
	
	public List<Long> getNodesIntersectingPath(GeneralPath path, boolean treatNodeShapesAsRectangle) {
		Rectangle2D mbr = path.getBounds2D();
		CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
		SpacialIndex2DEnumerator<Long> under = netViewSnapshot.getSpacialIndex2D()
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
				View<CyNode> nodeView = netViewSnapshot.getNodeView(suid);
				GeneralPath nodeShape = new GeneralPath();
				networkCanvas.grafx.getNodeShape(nodeDetails.getShape(nodeView),
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
	
	
	public static Rectangle2D getMBRofPath(GeneralPath path) {
		return path.getBounds2D();
	}

	public List<Long> queryDrawnEdges(int xMin, int yMin, int xMax, int yMax) {
		synchronized (dingLock) {
			return networkCanvas.computeEdgesIntersecting(xMin, yMin, xMax, yMax);
		}
	}

	/**
	 * Extents of the nodes.
	 * Called by the birds-eye-view
	 */
	public boolean getExtents(double[] extentsBuff) {
//		synchronized (m_lock) {
			CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
			if(netViewSnapshot.getNodeCount() == 0) {
				Arrays.fill(extentsBuff, 0.0);
				return false;
			}
			netViewSnapshot.getSpacialIndex2D().getMBR(extentsBuff);
			return true;
//		}
	}

	public void xformComponentToNodeCoords(double[] coords) {
		synchronized (dingLock) {
			if (networkCanvas != null && networkCanvas.grafx != null)
				networkCanvas.grafx.xformImageToNodeCoords(coords);
		}
	}
	
	public void xformNodeToComponentCoords(double[] coords) {
		synchronized (dingLock) {
			if (networkCanvas != null && networkCanvas.grafx != null)
				networkCanvas.grafx.xformNodetoImageCoords(coords);
		}
	}

	/**
	 * This method is called by the BirdsEyeView to get a snapshot of the graphics.
	 */
	//TODO: Need to fix up scaling and sizing.  
	public void drawSnapshot(VolatileImage img, GraphLOD lod, Paint bgPaint, 
	                         double xMin, double yMin, double xCenter,
	                         double yCenter, double scaleFactor) {
		// First paint the background
		backgroundCanvas.drawCanvas(img, xMin, yMin, xCenter, yCenter, scaleFactor);
		
		// final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
		final Set<VisualPropertyDependency<?>> dependencies =
				serviceRegistrar.getService(VisualMappingManager.class)
				.getVisualStyle(getViewModel()).getAllVisualPropertyDependencies();

		// synchronized (m_lock) {
		try {
			GraphRenderer.renderGraph(getViewModelSnapshot(), lod, nodeDetails,
			                          edgeDetails, new GraphGraphics(img, false, false),
			                          bgPaint, xCenter, yCenter, scaleFactor, haveZOrder, dependencies);
		} catch (Exception e) { 
			// We probably had a node or edge view removed out from underneath us.  Just quietly return, but
			// set content changed so we redraw again
			setContentChanged();
		}
		// }

		// Finally, draw the foreground
		foregroundCanvas.drawCanvas(img, xMin, yMin, xCenter, yCenter, scaleFactor);
	}

	public void drawSnapshot(
		VolatileImage img,
		GraphLOD lod,
		Paint bgPaint, 
		double xMin,
		double yMin,
		double xCenter,
		double yCenter,
		double scaleFactor,
		List<View<CyNode>> nodes,
		List<View<CyEdge>> edges
	) {
		if (!largeModel) { // || (nodes.size() + edges.size()) >= (m_drawPersp.getNodeCount() + m_drawPersp.getEdgeCount()) / 4) {
			drawSnapshot(img, lod, bgPaint, xMin, yMin, xCenter, yCenter, scaleFactor);
			return;
		}

		try {
			synchronized (dingLock) {
				renderSubgraph(new GraphGraphics(img, false, false), lod, bgPaint, xCenter, yCenter, scaleFactor, nodes, edges);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * MKTODO
	 * This is commented out for now.
	 * Typically this is called when the selection changes, and only the selected nodes are re-rendered.
	 * This worsk by drawing just the selected nodes over top of the current canvas.
	 * 
	 * This is the following issues:
	 * - Complex and heavyweight. The old solution required creating a separate spacial index and network.
	 * - Doesn't respect z-order, the selected nodes are always rendered on top, causing them to pop to the top.
	 * - Only happens for one frame. Anything else that triggers a render will overwrite the entire canvas.
	 *   So the users selects some nodes, they pop up to the top, then they go back down again, it looks strange.
	 *
	 * I feel that we should look to alternate ways to optimize the renderer, and abandon the logic in this method.
	 */
	int renderSubgraph(GraphGraphics graphics, final GraphLOD lod, 
	                   Paint bgColor, double xCenter, double yCenter, double scale,
	                   List<View<CyNode>> nodeList, List<View<CyEdge>> edgeList) {

		// If we're updateing more then 1/4 of the nodes or edges, just redraw the entire network to avoid
		// the overhead of creating the SpacialIndex2D and CySubNetwork
//		if (!largeModel) // || ((nodeList.size() + edgeList.size()) >= (m_drawPersp.getNodeCount() + m_drawPersp.getEdgeCount())/4))
			return renderGraph(graphics, lod, bgColor, xCenter, yCenter, scale);
//
////		// Make a copy of the nodes and edges arrays to avoid a conflict with selection events
////		// The assumption here is that these arrays are relatively small
////		List<CyNode> nodes = new ArrayList<>(nodeList);
////		List<CyEdge> edges = new ArrayList<>(edgeList);
//
//		// Make sure the graphics is initialized
//		if (!graphics.isInitialized())
//			graphics.clear(bgColor, xCenter, yCenter, scale);
//
//		Color bg = (Color)bgColor;
//		
//		if (bg != null)
//			bg = new Color(bg.getRed(), bg.getBlue(), bg.getGreen(), 0);
//
//		// Create our private spacial index.
////		final SpacialIndex2DFactory spacialFactory = serviceRegistrar.getService(SpacialIndex2DFactory.class);
////		SpacialIndex2D sub_spacial = spacialFactory.createSpacialIndex2D();
//
//		// And our private subnetwork
////		CySubNetwork net = new MinimalNetwork(SUIDFactory.getNextSUID());
//
//		for (View<CyEdge> edge : edgeList) {
//			SnapshotEdgeInfo edgeInfo = getViewModelSnapshot().getEdgeInfo(edge);
//			nodeList.add(edgeInfo.getTargetNodeView());
//			nodeList.add(edgeInfo.getSourceNodeView());
//		}
//		
////		for (CyNode node: nodes) {
////			long idx = node.getSUID();
////			if (m_spacial.exists(idx, m_extentsBuff, 0)) {
////				if (!sub_spacial.exists(idx, new float[4], 0))
////					sub_spacial.insert(idx, m_extentsBuff[0], m_extentsBuff[1], m_extentsBuff[2], m_extentsBuff[3], 0.0);
//////				net.addNode(node);
////			}
////		}
//		
////		for (CyEdge edge: edges) {
////			net.addEdge(edge);
////		}
//
//		int lastRenderDetail = 0;
//		
//		try {
//			// final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
//			final Set<VisualPropertyDependency<?>> dependencies =
//					vmm.getVisualStyle(getViewModel()).getAllVisualPropertyDependencies();
//			
////			synchronized (m_lock) {
//				lastRenderDetail = GraphRenderer.renderGraph(this, sub_spacial, lod, m_nodeDetails, m_edgeDetails, hash,
//						graphics, null, xCenter, yCenter, scale, haveZOrder, dependencies);
////			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		setContentChanged(false);
//		setViewportChanged(false);
//		m_visualChanged = true;
//
//		return lastRenderDetail;
	}
	
	/**
	 *  @param setLastRenderDetail if true, "m_lastRenderDetail" will be updated, otherwise it will not be updated.
	 */
	int renderGraph(GraphGraphics graphics, final GraphLOD lod, Paint bgColor, double xCenter, double yCenter, double scale) {
		int lastRenderDetail = 0;
		
		try {
//			synchronized (m_lock) {
			Set<VisualPropertyDependency<?>> dependencies = serviceRegistrar.getService(VisualMappingManager.class)
					.getVisualStyle(getViewModel()).getAllVisualPropertyDependencies();
				
			lastRenderDetail = GraphRenderer.renderGraph(getViewModelSnapshot(),
			  						     lod,
			  						     nodeDetails,
			  						     edgeDetails,
			  						     graphics, bgColor, xCenter,
			  						     yCenter, scale, haveZOrder,
			  						     dependencies);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		setContentChanged(false);
		setViewportChanged(false);
		
		return lastRenderDetail;
	}

	
	public void addContentChangeListener(ContentChangeListener l) {
		contentChangeListeners.add(l);
	}

	public void removeContentChangeListener(ContentChangeListener l) {
		contentChangeListeners.remove(l);
	}

	public void fireContentChanged() {
		for(ContentChangeListener l : contentChangeListeners)
			l.contentChanged();
	}

	public void addViewportChangeListener(ViewportChangeListener l) {
		viewportChangeListeners.add(l);
	}

	public void removeViewportChangeListener(ViewportChangeListener l) {
		viewportChangeListeners.remove(l);
	}
	
	public void fireViewportChanged(int viewportWidth, int viewportHeight, double newXCenter,  double newYCenter, double newScaleFactor) {
		for(ViewportChangeListener l : viewportChangeListeners)
			l.viewportChanged(viewportWidth, viewportHeight, newXCenter, newYCenter, newScaleFactor);
	}

	
	@Override
	public int print(Graphics g, PageFormat pageFormat, int page) {
		if (page == 0) {
			((Graphics2D) g).translate(pageFormat.getImageableX(),
					pageFormat.getImageableY());

			// make sure the whole image on the screen will fit to the printable
			// area of the paper
			double image_scale = Math.min(pageFormat.getImageableWidth()  / networkCanvas.getWidth(),
										  pageFormat.getImageableHeight() / networkCanvas.getHeight());

			if (image_scale < 1.0d) {
				((Graphics2D) g).scale(image_scale, image_scale);
			}

			// old school
			// g.clipRect(0, 0, getComponent().getWidth(),
			// getComponent().getHeight());
			// getComponent().print(g);

			// from InternalFrameComponent
			g.clipRect(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
			backgroundCanvas.print(g);
			networkCanvas.print(g);
			foregroundCanvas.print(g);

			return PAGE_EXISTS;
		} else {
			return NO_SUCH_PAGE;
		}
	}

	/**
	 * Method to return a reference to the network canvas. This method existed
	 * before the addition of background and foreground canvases, and it remains
	 * for backward compatibility.
	 */
	public InnerCanvas getCanvas() {
		return networkCanvas;
	}

	/**
	 * Method to return a reference to a DingCanvas object, given a canvas id.
	 */
	public DingCanvas getCanvas(Canvas canvasId) {
		if (canvasId == Canvas.BACKGROUND_CANVAS) {
			return backgroundCanvas;
		} else if (canvasId == Canvas.NETWORK_CANVAS) {
			return networkCanvas;
		} else if (canvasId == Canvas.FOREGROUND_CANVAS) {
			return foregroundCanvas;
		}
		return null;
	}

	/**
	 * Method to return a reference to an Image object,
	 * which represents the current network view.
	 *
	 * @param width Width of desired image.
	 * @param height Height of desired image.
	 * @param shrink Percent to shrink the network shown in the image.
	 * @param skipBackground If true, we don't draw the background
	 * This doesn't shrink the image, just the network shown, as if the user zoomed out.
	 * Can be between 0 and 1, if not it will default to 1.  
	 * @return Image
	 * @throws IllegalArgumentException
	 */
	private Image createImage(int width, final int height, double shrink, final boolean skipBackground) {
		// Validate arguments
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("width and height arguments must be greater than zero");
		if (shrink < 0 || shrink > 1.0) {
			logger.debug("shrink is invalid: " + shrink + " using default of 1.0");
			shrink = 1.0;
		}

		final double scale = shrink;
		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		invokeOnEDTAndWait(() -> {
			// Save current sizes, zoom and viewport position
			Dimension originalBgSize  = backgroundCanvas.getSize();
			Dimension originalNetSize = networkCanvas.getSize();
			Dimension originalFgSize  = foregroundCanvas.getSize();
			double zoom = getZoom();
			
			CyNetworkViewSnapshot netVewSnapshot = getViewModelSnapshot();
			Double centerX = netVewSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
			Double centerY = netVewSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
			Double scaleFactor = netVewSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
			
			// Create image to return
			final Graphics g = image.getGraphics();
	
			if (!skipBackground) {
				// Paint background canvas into image
				backgroundCanvas.setSize(width, height);
				backgroundCanvas.paint(g);
			}
			
			// Paint inner canvas (network)
			networkCanvas.setSize(width, height);
			fitContent(true);
			setZoom(getZoom() * scale);
			networkCanvas.paint(g);
			
			// Paint foreground canvas
			foregroundCanvas.setSize(width, height);
			foregroundCanvas.paint(g);
			
			// Restore to to original size, zoom and viewport
			backgroundCanvas.setSize(originalBgSize);
			networkCanvas.setSize(originalNetSize);
			foregroundCanvas.setSize(originalFgSize);
			setZoom(zoom);
			
			CyNetworkView netView = getViewModel();
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, centerX);
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, centerY);
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, scaleFactor);
			
			updateSnapshotAndView();
		});
		
		return image;
	}


	/**
	 * utility that returns the nodeView that is located at input point
	 */
	public View<CyNode> getPickedNodeView(Point2D pt) {
		double[] locn = {pt.getX(), pt.getY()};
		xformComponentToNodeCoords(locn);
		float x = (float) locn[0];
		float y = (float) locn[1];

		boolean treatNodeShapesAsRectangle = (networkCanvas.getLastRenderDetail() & GraphRenderer.LOD_HIGH_DETAIL) == 0;
		List<Long> suids = getNodesIntersectingRectangle(x, y, x, y, treatNodeShapesAsRectangle);

		CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
		
		// return node with topmost Z
		View<CyNode> nv = null;
		for(Long suid : suids) {
			View<CyNode> dnv = netViewSnapshot.getNodeView(suid);
			if (nv == null || nodeDetails.getZPosition(dnv) > nodeDetails.getZPosition(nv)) {
				nv = dnv;
			}
		}
		return nv;
	}

	public View<CyEdge> getPickedEdgeView(Point2D pt) {
		View<CyEdge> ev = null;
		List<Long> edges = queryDrawnEdges((int) pt.getX(), (int) pt.getY(), (int) pt.getX(), (int) pt.getY());

		long chosenEdge = edges.isEmpty() ? -1 : edges.get(edges.size()-1);
		if (chosenEdge >= 0) {
			CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
			ev = netViewSnapshot.getEdgeView(chosenEdge);
		}
		return ev;
	}
	
	public HandleKey getPickedEdgeHandle(Point2D pt) {
		double[] ptBuff = {pt.getX(), pt.getY()};
		xformComponentToNodeCoords(ptBuff);
		HandleKey handleKey = getBendStore().pickHandle((float)ptBuff[0], (float)ptBuff[1]);
		return handleKey;
	}
	

	private double checkZoom(double zoom, double orig) {
		if (zoom > 0)
			return zoom;

		logger.debug("invalid zoom: " + zoom + "   using orig: " + orig);
		return orig;
	}

	/**
	 * This method is used by freehep lib to export network as graphics.
	 */
	public void print(Graphics g) {
		boolean opaque = backgroundCanvas.isOpaque();
		boolean transparentBackground = "true".equalsIgnoreCase(props.getProperty("exportTransparentBackground"));
		backgroundCanvas.setOpaque(!transparentBackground);
		backgroundCanvas.print(g);
		
		backgroundCanvas.setOpaque(opaque); // restore the previous opaque value
		networkCanvas.print(g);
		foregroundCanvas.print(g);
	}

	/**
	 * This method is used by BitmapExporter to export network as graphics (png, jpg, bmp)
	 */
	public void printNoImposter(Graphics g) {
		backgroundCanvas.print(g);
		networkCanvas.printNoImposter(g);
		foregroundCanvas.print(g);
	}

	/**
	 * Our implementation of Component setBounds(). If we don't do this, the
	 * individual canvas do not get rendered.
	 */
	public void setBounds(int x, int y, int width, int height) {
		// call reshape on each canvas
		backgroundCanvas.setBounds(x, y, width, height);
		networkCanvas.setBounds(x, y, width, height);
		foregroundCanvas.setBounds(x, y, width, height);
	
		// If this is the first call to setBounds, load any annotations
		if (!annotationsLoaded) {
			annotationsLoaded = true;
			cyAnnotator.loadAnnotations();
		}
	}

	public void setSize(Dimension d) {
		networkCanvas.setSize(d);
	}

	static <X> List<X> makeList(X nodeOrEdge) {
		List<X> nl = new ArrayList<>(1);
		nl.add(nodeOrEdge);
		return nl;
	}

	@Override
	public Printable createPrintable() {
		return this;
	}
	
	@Override
	public Properties getProperties() {
		return this.props;
	}
	
	@Override 
	public Image createImage(int width, int height) {
		return createImage(width, height, 1, false);
	}

	@Override
	public DVisualLexicon getVisualLexicon() {
		return lexicon;
	}


	// For now the viewModelSnapshot should only be re-assigned on the EDT.
	public CyNetworkViewSnapshot getViewModelSnapshot() {
		return viewModelSnapshot;
	}
	
	@Override
	public CyNetworkView getViewModel() {
		return viewModel;
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int w, int h) {
		return VisualPropertyIconFactory.createIcon(value, w, h);
	}

	/**
	 * Returns the current snapshot image of this view.
	 *
	 * <p>
	 * No unnecessary image object will be created if networks in the current
	 * session does not contain any nested network, i.e., should not have
	 * performance/memory issue.
	 *
	 * @return Image of this view.  It is always up-to-date.
	 */
	TexturePaint getSnapshot(final double width, final double height) {
		if (!latest) {
			// Need to update snapshot.
			snapshotImage = (BufferedImage) createImage(DEF_SNAPSHOT_SIZE, DEF_SNAPSHOT_SIZE, 1, true);
			latest = true;
		}

		// Handle non-square images
		// Get the height and width of the image
		int imageWidth = snapshotImage.getWidth();
		int imageHeight = snapshotImage.getHeight();
		double ratio = (double)imageHeight / (double) imageWidth;
		int adjustedWidth = (int)((double)width/ratio)+1;

		final Rectangle2D rect = new Rectangle2D.Double(-adjustedWidth / 2, -height / 2, adjustedWidth, height);
		final TexturePaint texturePaint = new TexturePaint(snapshotImage, rect);
		return texturePaint;
	}


	/**
	 * Listener for update flag of snapshot image.
	 */
	private final class DGraphViewContentChangeListener implements ContentChangeListener {
		public void contentChanged() {
			latest = false;
		}
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
		final boolean contentChanged = isContentChanged();
		final boolean viewportChanged = isViewportChanged();
		
		// Check properties related to printing:
		boolean exportAsShape = "true".equalsIgnoreCase(props.getProperty("exportTextAsShape"));
		
		setPrintingTextAsShape(exportAsShape);
		print(printCanvas);
		
		// Keep previous dirty flags, otherwise the actual view canvas may not be updated next time.
		// (this method is usually only used to export the View as image, create thumbnails, etc,
		// therefore it should not flag the Graph View as updated, because the actual view canvas
		// may still have to be redrawn after this).
		setContentChanged(contentChanged);
		setViewportChanged(viewportChanged);
	}

	public CyAnnotator getCyAnnotator() {
		return cyAnnotator;
	}
	
	@Override
	public void dispose() {
		synchronized (this) {
			checkDirtyTimer.stop();
			coalesceTimer.shutdown();
			
			// m_lis[0] = null;
			cyAnnotator.dispose();
			serviceRegistrar.unregisterAllServices(cyAnnotator);
			networkCanvas.dispose();
		}
	}

	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}
	
	/******************************************************
	 * Animation handling.  Currently, this only supports *
	 * edge marquee, but could be extended in the future  *
	 * to support other kinds of animations.              *
	 *****************************************************/
	@Override
	public void actionPerformed(ActionEvent e) {
		// If we're not even drawing dashed edges, no sense in trying to do marquee
		if ((networkCanvas.getLastRenderDetail() & GraphRenderer.LOD_DASHED_EDGES) == 0) {
			return;
		}
		
		// MKTODO make this work again
//
//		List<DEdgeView> removeMe = new ArrayList<>();
//		for (DEdgeView edgeView: animatedEdges) {
//			CyEdge edge = edgeView.getModel();
//			Stroke s = m_edgeDetails.getStroke(edge);
//			
//			if (s != null && s instanceof AnimatedStroke) { 
//				Stroke as = ((AnimatedStroke)s).newInstanceForNextOffset();
//				
//				synchronized (m_lock) {
//					m_edgeDetails.overrideStroke(edge, as);
//					setContentChanged();
//				}
//			} else if (s == null) {
//				removeMe.add(edgeView);
//			}
//		}
//
//		// We do this this way to avoid the overhead of concurrent maps since this should be relatively rare
//		if (removeMe.size() != 0) {
//			for (DEdgeView edgeView: removeMe)
//				animatedEdges.remove(edgeView);
//		}

		// Redraw?
		networkCanvas.repaint();
		//updateView();
	}
//
//	public void removeAnimatedEdge(DEdgeView edgeView) {
//		if (animatedEdges.contains(edgeView)) {
//			animatedEdges.remove(edgeView);
//
//			if (animatedEdges.size() == 0 && animationTimer.isRunning()) {
//				animationTimer.stop();
//			}
//		}
//	}
//
//	public void addAnimatedEdge(DEdgeView edgeView) {
//		if (!animatedEdges.contains(edgeView)) {
//			animatedEdges.add(edgeView);
//
//			if (!animationTimer.isRunning()) {
//				animationTimer.start();
//			}
//		}
//	}
	
	
	public <T extends CyIdentifiable> void select(Collection<View<T>> nodesOrEdgeViews, Class<T> type, boolean selected) {
		if (nodesOrEdgeViews == null || nodesOrEdgeViews.isEmpty())
			return;
		
		CyNetwork model = getViewModel().getModel();
		CyTable table = type.equals(CyNode.class) ? model.getDefaultNodeTable() : model.getDefaultEdgeTable();
		
		// MKTODO is this right? what if the row doesn't exist?
		CyNetworkViewSnapshot snapshot = getViewModelSnapshot();
		for (View<? extends CyIdentifiable> nodeOrEdgeView : nodesOrEdgeViews) {
			Long suid;
			if(type.equals(CyNode.class)) {
				suid = snapshot.getNodeInfo((View<CyNode>)nodeOrEdgeView).getModelSUID();
			} else {
				suid = snapshot.getEdgeInfo((View<CyEdge>)nodeOrEdgeView).getModelSUID();
			}
			
			CyRow row = table.getRow(suid);
			row.set(CyNetwork.SELECTED, selected);		
		}
	}
	
}