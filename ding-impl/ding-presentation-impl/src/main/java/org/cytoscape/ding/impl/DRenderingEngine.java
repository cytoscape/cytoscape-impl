package org.cytoscape.ding.impl;

import java.awt.BorderLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.PrintLOD;
import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.internal.util.CoalesceTimer;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphLOD;
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
import org.cytoscape.view.model.spacial.SpacialIndex2DFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.HandleFactory;
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


public class DRenderingEngine implements RenderingEngine<CyNetwork>, Printable, CyNetworkViewListener {

	private static final Logger logger = LoggerFactory.getLogger(DRenderingEngine.class);
	protected static int DEF_SNAPSHOT_SIZE = 400;
	
	private final CyServiceRegistrar serviceRegistrar;
	private final CyEventHelper eventHelper;
	
	private final DVisualLexicon lexicon;

	private final CyNetworkView viewModel;
	private CyNetworkViewSnapshot viewModelSnapshot;
	
	// Common object lock used for state synchronization
	final DingLock dingLock = new DingLock();

	private final NodeDetails nodeDetails;
	private final EdgeDetails edgeDetails;
	
	private boolean contentChanged = true;
	
	private PrintLOD printLOD;
	private final DingGraphLODAll dingGraphLODAll = new DingGraphLODAll();
	private final DingGraphLOD dingGraphLOD;

	private CompositeCanvas compositeCanvas;
	private RendererComponent renderComponent;
	private NetworkPicker picker;
	private FontMetrics fontMetrics;
	
	// Snapshot of current view.  Will be updated by CONTENT_CHANGED event.
	private BufferedImage snapshotImage;
	// Represents current snapshot is latest version or not.
	private boolean latest;

	private final Properties props;
	private final CyAnnotator cyAnnotator;
	private boolean largeModel = false;
	
	private final List<ContentChangeListener> contentChangeListeners  = new CopyOnWriteArrayList<>();
	
//	private final DingGraphLODAll dingGraphLODAll = new DingGraphLODAll();
//	private final DingGraphLOD dingGraphLOD;
	
//	private Timer animationTimer;
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
		this.eventHelper = registrar.getService(CyEventHelper.class);
//		this.vmm = registrar.getService(VisualMappingManager.class);
		
		this.props = new Properties();
		this.viewModel = view;
		this.lexicon = dingLexicon;
		this.dingGraphLOD = dingGraphLOD;
		
		SpacialIndex2DFactory spacialIndexFactory = registrar.getService(SpacialIndex2DFactory.class);
		this.bendStore = new BendStore(this, handleFactory, spacialIndexFactory);
		
		nodeDetails = new DNodeDetails(this, registrar);
		edgeDetails = new DEdgeDetails(this);
		printLOD = new PrintLOD();
		
		compositeCanvas = new CompositeCanvas(registrar, this, dingLock, dingGraphLOD);
		renderComponent = new RendererComponent();
		picker = new NetworkPicker(this);

//		setGraphLOD(dingGraphLOD);

		// Finally, intialize our annotations
		cyAnnotator = new CyAnnotator(this, annMgr, registrar);
		registrar.registerService(cyAnnotator, SessionAboutToBeSavedListener.class, new Properties());
		
		//Updating the snapshot for nested networks
//		this.addContentChangeListener(new DGraphViewContentChangeListener());

		CyNetworkViewSnapshot snapshot = view.createSnapshot();
		if (!dingGraphLOD.detail(snapshot.getNodeCount(), snapshot.getEdgeCount()))
			largeModel = true;

		viewModelSnapshot = viewModel.createSnapshot();
		
//		cyAnnotator.loadAnnotations();
		
		coalesceTimer = new CoalesceTimer();
		
		// Check if the view model has changed approximately 30 times per second
		checkDirtyTimer = new Timer(30, e -> checkModelIsDirty());
		checkDirtyTimer.setRepeats(true);
		checkDirtyTimer.start();
	}
	
	
	public void install(RootPaneContainer rootPane) {
		InputHandlerGlassPane glassPane = getInputHandlerGlassPane();
		rootPane.setGlassPane(glassPane);
		rootPane.setContentPane(renderComponent);
		glassPane.setVisible(true);
	}
	
	public void install(JComponent component) {
		component.setLayout(new BorderLayout());
		component.add(renderComponent, BorderLayout.CENTER);
	}
	
	
	/**
	 * This is the interface between the renderer and Swing.
	 */
	@SuppressWarnings("serial")
	private class RendererComponent extends JComponent {
		
		private boolean annotationsLoaded = false;
		
		@Override
		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			compositeCanvas.setViewport(width, height);
			
			// If this is the first call to setBounds, load any annotations
			if(!annotationsLoaded) {
				annotationsLoaded = true;
				// MKTODO make this asynchronous
				// MKTODO we should not load annotations here!
				cyAnnotator.loadAnnotations();
			}
			
			getViewModel().batch(netView -> {
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH,  (double) width);
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, (double) height);
			}, false); // don't set the dirty flag
		}
		
		@Override
		public void paint(Graphics g) {
			if(fontMetrics == null) {
				fontMetrics = g.getFontMetrics(); // needed to compute label widths
			}
			
			// block and render a quick low-quality frame
			compositeCanvas.paintBlocking(g);
			
			// start rendering a high-quality frame on a separate thread, repaint when done
			// MKTODO if painting sync is high quiality then avoid this?
			
			// MKTODO show the spinner
//			compositeCanvas.paintAsync(() -> {
//				this.repaint();
//				// stop the spinner
//			});
			
//			if(viewportChanged) {
//				double centerX = networkCanvas.getCenterX();
//				double centerY = networkCanvas.getCenterY();
//				double scaleFactor = networkCanvas.getScaleFactor();
//				fireViewportChanged(getWidth(), getHeight(), centerX, centerY, scaleFactor);
//			}
			
//			g.setColor(backgroundColor);
//			g.fillRect(0, 0, getWidth(), getHeight());
//			
//			backgroundCanvas.paint(g);
//			networkCanvas.paint(g);
//			foregroundCanvas.paint(g);
//			
//			if(contentChanged) {
//				fireContentChanged();
//			}
//			
//			setContentChanged(false);
//			setViewportChanged(false);
		}
	}
	
	public NetworkPicker getPicker() {
		return picker;
	}
	
	public Rectangle getBounds() {
		return renderComponent.getBounds();
	}
	
	public NetworkTransform getTransform() {
		return compositeCanvas.getNetworkTransform();
	}
	
	public GraphLOD getGraphLOD() {
		return compositeCanvas.getLOD();
	}
	
	public AnnotationCanvas getAnnotationCanvas(DingAnnotation.CanvasID canvasID) {
		return compositeCanvas.getAnnotationCanvas(canvasID);
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
		final boolean updateModel = viewModel.isDirty();
		final boolean updateView = contentChanged;
		
		if(updateModel) {
			updateModel();
		}
		if(updateModel || updateView) {
			updateView();
			fireContentChanged();
		}
		contentChanged = false;
	}
	
	private void updateModelAndView() {
		updateModel();
		updateView();
	}
	
	public void updateView() {
		renderComponent.repaint();
		// Fire this event on another thread (and debounce) so that it doesn't block the renderer
		coalesceTimer.coalesce(() -> eventHelper.fireEvent(new UpdateNetworkPresentationEvent(getViewModel())));
	}
	
	private void updateModel() {
		// create a new snapshot, this should be very fast
		viewModelSnapshot = viewModel.createSnapshot(); // sets viewModel.isDirty() to false
		
		// Check for important changes between snapshots
		Paint backgroundPaint = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		compositeCanvas.setBackgroundPaint(backgroundPaint);
		
		Collection<View<CyEdge>> selectedEdges = viewModelSnapshot.getTrackedEdges(CyNetworkViewConfig.SELECTED_EDGES);
		bendStore.updateSelectedEdges(selectedEdges);
		
//		Collection<View<CyEdge>> animatedEdges = viewModelSnapshot.getTrackedEdges(DingNetworkViewFactory.ANIMATED_EDGES);
//		edgeDetails.updateAnimatedEdges(animatedEdges);
//		if(animatedEdges.isEmpty() && animationTimer != null) {
//			animationTimer.stop();
//			animationTimer = null;
//		} else if(!animatedEdges.isEmpty() && animationTimer == null) {
//			animationTimer = new Timer(200, e -> advanceAnimatedEdges());
//			animationTimer.setRepeats(true);
//			animationTimer.start();
//		}
		
		// update LOD
		boolean hd = viewModelSnapshot.getVisualProperty(DVisualLexicon.NETWORK_FORCE_HIGH_DETAIL);
		compositeCanvas.setLOD(hd ? dingGraphLODAll : dingGraphLOD);
		
		// update view (for example if "fit selected" was run)
		double x = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
		double y = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
		compositeCanvas.setCenter(x, y);
		
		double scaleFactor = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		compositeCanvas.setScaleFactor(scaleFactor);
		
		setContentChanged(false);
//		updateView(true);
	}
	
	
//	private void advanceAnimatedEdges() {
//		edgeDetails.advanceAnimatedEdges();
//		// This is more lightweight than calling updateView(). And if the animation thread is faster 
//		// than the renderer the EDT will coalesce the extra paint events.
//		setContentChanged();
//		networkCanvas.repaint();
//	}
	
	public int getLastRenderDetail() {
		return compositeCanvas.getLastRenderDetail();
	}
	
	public boolean treatNodeShapesAsRectangle() {
		return compositeCanvas.treatNodeShapesAsRectangle();
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
	
	/**
	 * Mainly for using as a parent when showing dialogs and menus.
	 */
	public JComponent getComponent() {
		return getInputHandlerGlassPane();
	}

//	public boolean isNodeSelected(long suid) {
//		return nodeDetails.isSelected(getViewModelSnapshot().getNodeView(suid));
//	}
//	
//	public boolean isNodeSelected(View<CyNode> nodeView) {
//		return nodeDetails.isSelected(nodeView);
//	}
//	
//	public boolean isEdgeSelected(long suid) {
//		return edgeDetails.isSelected(getViewModelSnapshot().getEdgeView(suid));
//	}
//	
//	public boolean isEdgeSelected(View<CyEdge> edgeView) {
//		return edgeDetails.isSelected(edgeView);
//	}
	
	public NodeDetails getNodeDetails() {
		return nodeDetails;
	}
	
	public EdgeDetails getEdgeDetails() {
		return edgeDetails;
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
	
	public void fireContentChanged() {
		for(ContentChangeListener l : contentChangeListeners)
			l.contentChanged();
	}
	
	public void addContentChangeListener(ContentChangeListener l) {
		contentChangeListeners.add(l);
	}

	public void removeContentChangeListener(ContentChangeListener l) {
		contentChangeListeners.remove(l);
	}

	
//	public boolean isViewportChanged() {
//		return viewportChanged;
//	}
//	
//	public void setViewportChanged() {
//		setViewportChanged(true);
//	}
//	
//	private void setViewportChanged(final boolean b) {
//		viewportChanged = b;
//	}
//	
//
//	public double getZoom() {
//		return compositeCanvas.getScaleFactor();
//	}

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
			compositeCanvas.setScaleFactor(checkZoom(zoom, compositeCanvas.getScaleFactor()));
//			setViewportChanged();
		}
	}
	
	public double getZoom() {
		return compositeCanvas.getScaleFactor();
	}

	private void fitContent(final boolean updateView) {
		eventHelper.flushPayloadEvents();

		synchronized (dingLock) {
			// make sure we use the latest snapshot
			CyNetworkViewSnapshot netViewSnapshot = viewModel.createSnapshot();
			if(netViewSnapshot.getNodeCount() == 0)
				return;
			if (compositeCanvas.getWidth() == 0 || compositeCanvas.getHeight() == 0)
				return;
			
			double[] extentsBuff = new double[4];
			netViewSnapshot.getSpacialIndex2D().getMBR(extentsBuff); // extents of the network
			compositeCanvas.adjustBoundsToIncludeAnnotations(extentsBuff); // extents of the annotation canvases

			netViewSnapshot.getMutableNetworkView().batch(netView -> {
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION))
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, (extentsBuff[0] + extentsBuff[2]) / 2.0d);
				
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION))
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, (extentsBuff[1] + extentsBuff[3]) / 2.0d);
	
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR)) {
					// Apply a factor 0.98 to zoom, so that it leaves a small border around the network and any annotations.
					final double zoom = Math.min(((double) compositeCanvas.getWidth())  /  (extentsBuff[2] - extentsBuff[0]), 
					                             ((double) compositeCanvas.getHeight()) /  (extentsBuff[3] - extentsBuff[1])) * 0.98;
					// Update view model.  Zoom Level should be modified.
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
				}
			});
		}
		
		// MKTODO is this necessary, the timer will check the dirty flag
		if (updateView)
			updateModelAndView();
	}
	
	@Override
	public void handleFitContent() {
		fitContent(/* updateView = */ true);
	}
	
	@Override
	public void handleUpdateView() {
		updateModelAndView();
	}
	
//	public void updateView() {
//		updateView(false);
//	}
//	
//	private void updateView(final boolean forceRedraw) {
//		// MKTODO we don't need to do this anymore right?
////		eventHelper.flushPayloadEvents();
//		
//		if (forceRedraw) {
//			setContentChanged();
//		}
//		
//		renderComponent.repaint();
//		
////		//Check if image size has changed if so, visual property needs to be changed as well
////		if (networkCanvas.getWidth() != imageWidth || networkCanvas.getHeight() != imageHeight) {
////			imageWidth  = networkCanvas.getWidth();
////			imageHeight = networkCanvas.getHeight();
////			getViewModel().batch(netView -> {
////				netView.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH,  (double) imageWidth);
////				netView.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, (double) imageHeight);
////			}, false); // don't set the dirty flag
////		}
//		
//		// Fire this event on another thread so that it doesn't block the renderer
//		coalesceTimer.coalesce(() -> eventHelper.fireEvent(new UpdateNetworkPresentationEvent(getViewModel())));
//	}
	
	
	public void zoom(int ticks) {
		if(getViewModelSnapshot().isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR))
			return;
		
		double factor;
		if (ticks < 0)
			factor = 1.1; // scroll up, zoom in
		else if (ticks > 0)
			factor = 0.9; // scroll down, zoom out
		else
			return;
		
		double scaleFactor = compositeCanvas.getScaleFactor() * factor;
		
//			setHideEdges();
		setZoom(scaleFactor);
		
		getViewModel().batch(netView -> {
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, scaleFactor);
		}, false);
		
		renderComponent.repaint();
	}
	
	
	public void pan(double deltaX, double deltaY) {
		synchronized (dingLock) {
			double x = compositeCanvas.getCenterX() + deltaX;
			double y = compositeCanvas.getCenterY() + deltaY;
			setCenter(x, y);
		}
//		networkCanvas.setHideEdges();
		renderComponent.repaint();
	}
	
	public void setCenter(double x, double y) {
		synchronized (dingLock) {
			compositeCanvas.setCenter(x,y);
//			setViewportChanged();
			
			// Update view model
			// TODO: don't do it from here?
			getViewModel().batch(netView -> {
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, compositeCanvas.getCenterX());
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, compositeCanvas.getCenterY());
			}, false); // don't set the dirty flag
		}
	}

	public Point2D getCenter() {
		synchronized (dingLock) {
			return new Point2D.Double(compositeCanvas.getCenterX(), compositeCanvas.getCenterY());
		}
	}
	
	@Override
	public void handleFitSelected() {
		fitSelected();
	}

	public void fitSelected() {
		eventHelper.flushPayloadEvents();
		
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

		float xMinF = xMin - (getLabelWidth(leftMost) / 2);
		float xMaxF = xMax + (getLabelWidth(rightMost) / 2);
		float yMaxF = yMax;
		float yMinF = yMin;

		netViewSnapshot.getMutableNetworkView().batch(netView -> {
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
				double zoom = Math.min(((double) compositeCanvas.getWidth())
						/ (((double) xMaxF) - ((double) xMinF)),
						((double) compositeCanvas.getHeight())
								/ (((double) yMaxF) - ((double) yMinF)));
				zoom = checkZoom(zoom, compositeCanvas.getScaleFactor());
				
				// Update view model.  Zoom Level should be modified.
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
			}
			
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)) {
				double xCenter = (((double) xMinF) + ((double) xMaxF)) / 2.0d;
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, xCenter);
			}
			
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
				double yCenter = (((double) yMinF) + ((double) yMaxF)) / 2.0d;
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, yCenter);
			}
		});
			
		updateModelAndView();
	}

	
	private int getLabelWidth(View<CyNode> nodeView) {
		if (nodeView == null || fontMetrics == null)
			return 0;

		String s = nodeDetails.getLabelText(nodeView);
		if (s == null)
			return 0;

		char[] lab = s.toCharArray();
		return fontMetrics.charsWidth(lab, 0, lab.length);
	}

	
//	public void setGraphLOD(GraphLOD lod) {
//		synchronized (dingLock) {
//			compositeCanvas.setLOD(lod);
//		}
//	}

//	public GraphLOD getGraphLOD() {
//		return networkCanvas.lod;
//	}

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
//	public List<Long> getNodesIntersectingRectangle(double xMinimum, double yMinimum, double xMaximum,
//	                                          double yMaximum, boolean treatNodeShapesAsRectangle) {
//	
//		CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
//		
//		final float xMin = (float) xMinimum;
//		final float yMin = (float) yMinimum;
//		final float xMax = (float) xMaximum;
//		final float yMax = (float) yMaximum;
//		
//		SpacialIndex2DEnumerator<Long> under = netViewSnapshot.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
//		if(!under.hasNext())
//			return Collections.emptyList();
//		
//		List<Long> returnVal = new ArrayList<>(under.size());
//		
//		if (treatNodeShapesAsRectangle) {
//			while(under.hasNext()) {
//				returnVal.add(under.next());
//			}
//		} else {
//			final double x = xMin;
//			final double y = yMin;
//			final double w = ((double) xMax) - xMin;
//			final double h = ((double) yMax) - yMin;
//
//			float[] extentsBuff = new float[4];
//			
//			while (under.hasNext()) {
//				final long suid = under.nextExtents(extentsBuff);
//				View<CyNode> cyNode = netViewSnapshot.getNodeView(suid);
//
//				// The only way that the node can miss the intersection query is
//				// if it intersects one of the four query rectangle's corners.
//				if (((extentsBuff[0] < xMin) && (extentsBuff[1] < yMin))
//				    || ((extentsBuff[0] < xMin) && (extentsBuff[3] > yMax))
//				    || ((extentsBuff[2] > xMax) && (extentsBuff[3] > yMax))
//				    || ((extentsBuff[2] > xMax) && (extentsBuff[1] < yMin))) {
//					
//					GeneralPath path = new GeneralPath();
//					networkCanvas.getNodeShape(nodeDetails.getShape(cyNode),
//							extentsBuff[0], extentsBuff[1],
//							extentsBuff[2], extentsBuff[3], path);
//
//					if ((w > 0) && (h > 0)) {
//						if (path.intersects(x, y, w, h))
//							returnVal.add(suid);
//					} else {
//						if (path.contains(x, y))
//							returnVal.add(suid);
//					}
//				} else {
//					returnVal.add(suid);
//				}
//			}
//		}
//		return returnVal;
//	}
	
//	
//	public List<Long> getNodesIntersectingPath(GeneralPath path, boolean treatNodeShapesAsRectangle) {
//		Rectangle2D mbr = path.getBounds2D();
//		CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
//		SpacialIndex2DEnumerator<Long> under = netViewSnapshot.getSpacialIndex2D()
//				.queryOverlap((float)mbr.getMinX(), (float)mbr.getMinY(), (float)mbr.getMaxX(), (float)mbr.getMaxY());
//		if(!under.hasNext())
//			return Collections.emptyList();
//		
//		List<Long> result = new ArrayList<>(under.size());
//		float[] extents = new float[4];
//		
//		if(treatNodeShapesAsRectangle) {
//			while(under.hasNext()) {
//				Long suid = under.nextExtents(extents);
//				float x = extents[0];
//				float y = extents[1];
//				float w = extents[2] - x;
//				float h = extents[3] - y;
//				if(path.intersects(x, y, w, h)) {
//					result.add(suid);
//				}
//			}
//		} else {
//			while(under.hasNext()) {
//				Long suid = under.nextExtents(extents);
//				View<CyNode> nodeView = netViewSnapshot.getNodeView(suid);
//				GeneralPath nodeShape = new GeneralPath();
//				GraphGraphics.getNodeShape(nodeDetails.getShape(nodeView),
//						extents[0], extents[1],
//						extents[2], extents[3], nodeShape);
//				Area pathArea = new Area(path);
//				Area nodeArea = new Area(nodeShape);
//				pathArea.intersect(nodeArea);
//				if(!pathArea.isEmpty()) {
//					result.add(suid);
//				}
//			}
//		}
//		
//		return result;
//	}
//	
	
//	public static Rectangle2D getMBRofPath(GeneralPath path) {
//		return path.getBounds2D();
//	}

//	public List<Long> queryDrawnEdges(int xMin, int yMin, int xMax, int yMax) {
//		synchronized (dingLock) {
//			return networkCanvas.computeEdgesIntersecting(xMin, yMin, xMax, yMax);
//		}
//	}

	/**
	 * Extents of the nodes. Called by the birds-eye-view
	 */
	public boolean getExtents(double[] extentsBuff) {
		CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
		if(netViewSnapshot.getNodeCount() == 0) {
			Arrays.fill(extentsBuff, 0.0);
			return false;
		}
		netViewSnapshot.getSpacialIndex2D().getMBR(extentsBuff);
		return true;
	}

//	public void xformComponentToNodeCoords(double[] coords) {
//		networkCanvas.xformImageToNodeCoords(coords);
//	}
//	
//	public void xformNodeToComponentCoords(double[] coords) {
//		networkCanvas.xformNodetoImageCoords(coords);
//	}
//
//	/**
//	 * This method is called by the BirdsEyeView to get a snapshot of the graphics.
//	 */
//	//TODO: Need to fix up scaling and sizing.  
//	public void drawSnapshot(VolatileImage img, GraphLOD lod, Color bgPaint, 
//	                         double xMin, double yMin, double xCenter,
//	                         double yCenter, double scaleFactor) {
//		
//		// background color
//		Graphics2D g = img.createGraphics(); 
//		g.setPaint(bgPaint);
//		g.fillRect(0, 0, img.getWidth(), img.getHeight());
//		
//		backgroundCanvas.drawCanvas(img, xMin, yMin, xCenter, yCenter, scaleFactor);
//		
//		final Set<VisualPropertyDependency<?>> dependencies =
//				vmm.getVisualStyle(getViewModel()).getAllVisualPropertyDependencies();
//
//		synchronized (dingLock) {
//			try {
//				GraphRenderer.renderGraph(getViewModelSnapshot(), lod, nodeDetails,
//				                          edgeDetails, new GraphGraphics(img, false, false),
//				                          xCenter, yCenter, scaleFactor, true, dependencies);
//			} catch (Exception e) { 
//				// We probably had a node or edge view removed out from underneath us.  Just quietly return, but
//				// set content changed so we redraw again
//				setContentChanged();
//			}
//		}
//
//		foregroundCanvas.drawCanvas(img, xMin, yMin, xCenter, yCenter, scaleFactor);
//	}
//
//	public void drawSnapshot(
//		VolatileImage img,
//		GraphLOD lod,
//		Color bgPaint, 
//		double xMin,
//		double yMin,
//		double xCenter,
//		double yCenter,
//		double scaleFactor,
//		List<View<CyNode>> nodes,
//		List<View<CyEdge>> edges
//	) {
//		if (!largeModel) { // || (nodes.size() + edges.size()) >= (m_drawPersp.getNodeCount() + m_drawPersp.getEdgeCount()) / 4) {
//			drawSnapshot(img, lod, bgPaint, xMin, yMin, xCenter, yCenter, scaleFactor);
//			return;
//		}
//
//		try {
//			synchronized (dingLock) {
//				renderGraph(new GraphGraphics(img, false, false), lod, xCenter, yCenter, scaleFactor);//, nodes, edges);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	
//	int renderGraph(GraphGraphics graphics, final GraphLOD lod, double xCenter, double yCenter, double scale) {
//		int lastRenderDetail = 0;
//		
//		try {
//			synchronized (dingLock) {
//				Set<VisualPropertyDependency<?>> dependencies =
//						vmm.getVisualStyle(getViewModel()).getAllVisualPropertyDependencies();
//					
//				lastRenderDetail = GraphRenderer.renderGraph(getViewModelSnapshot(),
//				  						     lod,
//				  						     nodeDetails,
//				  						     edgeDetails,
//				  						     graphics, xCenter,
//				  						     yCenter, scale, true,
//				  						     dependencies);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return lastRenderDetail;
//	}

	
//	public void addContentChangeListener(ContentChangeListener l) {
//		contentChangeListeners.add(l);
//	}
//
//	public void removeContentChangeListener(ContentChangeListener l) {
//		contentChangeListeners.remove(l);
//	}
//
//	public void fireContentChanged() {
//		for(ContentChangeListener l : contentChangeListeners)
//			l.contentChanged();
//	}
//
//	public void addViewportChangeListener(ViewportChangeListener l) {
//		viewportChangeListeners.add(l);
//	}
//
//	public void removeViewportChangeListener(ViewportChangeListener l) {
//		viewportChangeListeners.remove(l);
//	}
//	
//	public void fireViewportChanged(int viewportWidth, int viewportHeight, double newXCenter,  double newYCenter, double newScaleFactor) {
//		for(ViewportChangeListener l : viewportChangeListeners)
//			l.viewportChanged(viewportWidth, viewportHeight, newXCenter, newYCenter, newScaleFactor);
//	}

	
	@Override
	public int print(Graphics g, PageFormat pageFormat, int page) {
		if(page != 0)
			return NO_SUCH_PAGE;
		
		((Graphics2D) g).translate(pageFormat.getImageableX(), pageFormat.getImageableY());

		// make sure the whole image on the screen will fit to the printable
		// area of the paper
		double image_scale = Math.min(pageFormat.getImageableWidth()  / compositeCanvas.getWidth(),
									  pageFormat.getImageableHeight() / compositeCanvas.getHeight());

		if (image_scale < 1.0d) {
			((Graphics2D) g).scale(image_scale, image_scale);
		}

		// old school
		// g.clipRect(0, 0, getComponent().getWidth(),
		// getComponent().getHeight());
		// getComponent().print(g);

		// from InternalFrameComponent
		g.clipRect(0, 0, renderComponent.getWidth(), renderComponent.getHeight());
		compositeCanvas.print(g);

		return PAGE_EXISTS;
	}

//	/**
//	 * Method to return a reference to the network canvas. This method existed
//	 * before the addition of background and foreground canvases, and it remains
//	 * for backward compatibility.
//	 */
//	public InnerCanvas getCanvas() {
//		return networkCanvas;
//	}
//
//	/**
//	 * Method to return a reference to a DingCanvas object, given a canvas id.
//	 */
//	public DingCanvas getCanvas(Canvas canvasId) {
//		if (canvasId == Canvas.BACKGROUND_CANVAS) {
//			return backgroundCanvas;
//		} else if (canvasId == Canvas.NETWORK_CANVAS) {
//			return networkCanvas;
//		} else if (canvasId == Canvas.FOREGROUND_CANVAS) {
//			return foregroundCanvas;
//		}
//		return null;
//	}

	
	/**
	 * Method to return a reference to an Image object, which represents the current network view.
	 *
	 * @param width Width of desired image.
	 * @param height Height of desired image.
	 * @param shrink Percent to shrink the network shown in the image.
	 * This doesn't shrink the image, just the network shown, as if the user zoomed out.
	 * Can be between 0 and 1, if not it will default to 1.  
	 */
	private Image createImage(int width, final int height, double scale) {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("width and height arguments must be greater than zero");
		if (scale < 0 || scale > 1.0)
			scale = 1.0;

		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		int origWidth = compositeCanvas.getWidth();
		int origHeight = compositeCanvas.getHeight();
		double origZoom = compositeCanvas.getScaleFactor();
		
		CyNetworkViewSnapshot netVewSnapshot = getViewModelSnapshot();
		Double centerX = netVewSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
		Double centerY = netVewSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
		Double scaleFactor = netVewSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		
		
		compositeCanvas.setViewport(width, height);
		fitContent(true);
		setZoom(compositeCanvas.getScaleFactor() * scale);
		
		Graphics g = image.getGraphics();
//		compositeCanvas.paintBlocking(g);
		
		compositeCanvas.setViewport(origWidth, origHeight);
		setZoom(origZoom);
		
		CyNetworkView netView = getViewModel();
		netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, centerX);
		netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, centerY);
		netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, scaleFactor);
		
		updateModelAndView();
		
		return image;
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
//		boolean opaque = backgroundCanvas.isOpaque();
//		boolean transparentBackground = "true".equalsIgnoreCase(props.getProperty("exportTransparentBackground"));
//		backgroundCanvas.setOpaque(!transparentBackground);
//		backgroundCanvas.print(g);
//		
//		backgroundCanvas.setOpaque(opaque); // restore the previous opaque value
//		networkCanvas.print(g);
//		foregroundCanvas.print(g);
	}
//
//	/**
//	 * This method is used by BitmapExporter to export network as graphics (png, jpg, bmp)
//	 */
//	public void printNoImposter(Graphics g) {
//		backgroundCanvas.print(g);
//		networkCanvas.printNoImposter(g);
//		foregroundCanvas.print(g);
//	}


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
		return createImage(width, height, 1);
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
			snapshotImage = (BufferedImage) createImage(DEF_SNAPSHOT_SIZE, DEF_SNAPSHOT_SIZE, 1);
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
//		final boolean contentChanged = isContentChanged();
//		final boolean viewportChanged = isViewportChanged();
//		
//		// Check properties related to printing:
//		boolean exportAsShape = "true".equalsIgnoreCase(props.getProperty("exportTextAsShape"));
//		
//		setPrintingTextAsShape(exportAsShape);
//		print(printCanvas);
//		
//		// Keep previous dirty flags, otherwise the actual view canvas may not be updated next time.
//		// (this method is usually only used to export the View as image, create thumbnails, etc,
//		// therefore it should not flag the Graph View as updated, because the actual view canvas
//		// may still have to be redrawn after this).
//		setContentChanged(contentChanged);
//		setViewportChanged(viewportChanged);
	}

	public CyAnnotator getCyAnnotator() {
		return cyAnnotator;
	}
	
	
	@Override
	public void handleDispose() {
		dispose();
	}
	
	@Override
	public void dispose() {
		synchronized(this) {
			checkDirtyTimer.stop();
			coalesceTimer.shutdown();
			cyAnnotator.dispose();
			serviceRegistrar.unregisterAllServices(cyAnnotator);
			compositeCanvas.dispose();
		}
	}

	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}
	
	
	public <T extends CyIdentifiable> void select(Collection<View<T>> nodesOrEdgeViews, Class<T> type, boolean selected) {
		if (nodesOrEdgeViews == null || nodesOrEdgeViews.isEmpty())
			return;
		
		boolean isNodes = type.equals(CyNode.class);
		Boolean selectedBoxed = Boolean.valueOf(selected);
		
		CyNetwork model = getViewModel().getModel();
		CyTable table = isNodes ? model.getDefaultNodeTable() : model.getDefaultEdgeTable();
		
		// MKTODO is this right? what if the row doesn't exist?
		CyNetworkViewSnapshot snapshot = getViewModelSnapshot();
		for (View<? extends CyIdentifiable> nodeOrEdgeView : nodesOrEdgeViews) {
			if(isNodes) {
				View<CyNode> mutableNodeView = getViewModel().getNodeView(nodeOrEdgeView.getSUID());
				Long modelSuid = snapshot.getNodeInfo((View<CyNode>)nodeOrEdgeView).getModelSUID();
				CyRow row = table.getRow(modelSuid);
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_SELECTED, selectedBoxed);
				row.set(CyNetwork.SELECTED, selectedBoxed);	
			} else {
				View<CyEdge> mutableEdgeView = getViewModel().getEdgeView(nodeOrEdgeView.getSUID());
				Long modelSuid = snapshot.getEdgeInfo((View<CyEdge>)nodeOrEdgeView).getModelSUID();
				CyRow row = table.getRow(modelSuid);
				mutableEdgeView.setVisualProperty(BasicVisualLexicon.EDGE_SELECTED, selectedBoxed);
				row.set(CyNetwork.SELECTED, selectedBoxed);	
			}
		}
	}
	
}