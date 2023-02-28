package org.cytoscape.ding.impl;

import static org.cytoscape.graph.render.stateful.RenderDetailFlags.LOD_HIGH_DETAIL;
import static org.cytoscape.graph.render.stateful.RenderDetailFlags.LOD_NODE_LABELS;
import static org.cytoscape.graph.render.stateful.RenderDetailFlags.OPT_LABEL_CACHE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;

import org.cytoscape.cg.event.CustomGraphicsLibraryUpdatedListener;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.PrintLOD;
import org.cytoscape.ding.debug.DebugProgressMonitorFactory;
import org.cytoscape.ding.debug.DingDebugMediator;
import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.ding.impl.canvas.CompositeGraphicsCanvas;
import org.cytoscape.ding.impl.canvas.MainRenderComponent;
import org.cytoscape.ding.impl.canvas.NetworkImageBuffer;
import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.strokes.DAnimatedStroke;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphLOD.RenderEdges;
import org.cytoscape.graph.render.stateful.LabelInfoCache;
import org.cytoscape.graph.render.stateful.LabelInfoProvider;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
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

/**
 * This class acts as a controller for rendering one CyNetworkView.
 * It initializes all the classes needed for rendering and acts as a bridge
 * between them.
 *
 */
public class DRenderingEngine implements RenderingEngine<CyNetwork>, Printable, CyNetworkViewListener {

	private static final Logger logger = LoggerFactory.getLogger(DRenderingEngine.class);
	protected static int DEF_SNAPSHOT_SIZE = 400;
	
	
	/**
	 * The UpdateType can effect rendering in many ways:
	 *  - which canvases are redrawn
	 *  - which RenderDetailFlags bits are set
	 *  - if a fast and/or slow frame is rendered
	 *  - which specific/nodes edges to redraw
	 *  - etc...
	 */
	public enum UpdateType {
		ALL_FAST,            // Render a fast frame, used internally for panning etc
		ALL_FULL,            // Render a fast frame, then if needed start rendering a full frame asynchronously
		JUST_ANNOTATIONS,    // Render a fast frame that redraws just annotations
		JUST_EDGES;          // Render a fast frame that redraws just edges, used for animated edges
	}
	
	
	private final CyServiceRegistrar serviceRegistrar;
	private final CyEventHelper eventHelper;
	
	private final DVisualLexicon lexicon;

	private final CyNetworkView viewModel;
	private CyNetworkViewSnapshot viewModelSnapshot;
	
	// Common object lock used for state synchronization
	final DingLock dingLock = new DingLock();

	private final NodeDetails nodeDetails;
	private final EdgeDetails edgeDetails;
	
	private final DingGraphLOD dingGraphLOD;

	private MainRenderComponent renderComponent;
	private NetworkPicker picker;
	
	// Snapshot of current view.  Will be updated by CONTENT_CHANGED event.
	private BufferedImage snapshotImage;
	// Represents current snapshot is latest version or not.
	private boolean latestSnapshot;

	private final CyAnnotator cyAnnotator;
	private final LabelSelectionManager labelSelectionManager;
	
	private final List<ContentChangeListener> contentChangeListeners = new CopyOnWriteArrayList<>();
	private final List<ThumbnailChangeListener> thumbnailChangeListeners = new CopyOnWriteArrayList<>();
	
	private final int timerDelay = 30;
	private boolean animateEdges = false;
	private int animationCounter = 0; // ok if this overflows
	private final Timer modelAndAnimationTimer;
	private final DebounceTimer eventFireTimer;
	
	private final LabelInfoCache labelInfoCache;
	
	private final BendStore bendStore;
	private InputHandlerGlassPane inputHandler = null;
	private DebugProgressMonitorFactory debugProgressMonitorFactory;
	
	// This is Ding's own rendering thread. All rendering is single-threaded, but off the EDT
	private final ExecutorService singleThreadExecutor;
	
	
	public DRenderingEngine(
			CyNetworkView view,
			DVisualLexicon dingLexicon,
			AnnotationFactoryManager annMgr,
			DingGraphLOD dingGraphLOD,
			HandleFactory handleFactory,
			CyServiceRegistrar registrar
	) {
		this.serviceRegistrar = registrar;
		this.eventHelper = registrar.getService(CyEventHelper.class);
		this.viewModel = view;
		this.lexicon = dingLexicon;
		this.dingGraphLOD = dingGraphLOD;
		
		this.singleThreadExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("ding-" + thread.getName());
			return thread;
		});
		
		this.bendStore = new BendStore(this, eventHelper, handleFactory);
		
		nodeDetails = new DNodeDetails(registrar);
		edgeDetails = new DEdgeDetails(this);
		
		// Finally, intialize our annotations
		cyAnnotator = new CyAnnotator(this, annMgr, registrar);
		registrar.registerService(cyAnnotator, SessionAboutToBeSavedListener.class);
		registrar.registerService(cyAnnotator, CustomGraphicsLibraryUpdatedListener.class);
		
		labelSelectionManager = new LabelSelectionManager(this);
		
		renderComponent = new MainRenderComponent(this, dingGraphLOD);
		picker = new NetworkPicker(this, null);
		
		// Updating the snapshot for nested networks
		addContentChangeListener(ut -> latestSnapshot = false);

		viewModelSnapshot = viewModel.createSnapshot();
		
		labelInfoCache = new LabelInfoCache(1000, DingDebugMediator.showDebugPanel(registrar)); // MKTODO should maxSize be hardcoded?
		
		eventFireTimer = new DebounceTimer(240);
		
		// Check if the view model has changed approximately 30 times per second.
		// Also use same timer for animated edges so that we don't have a race condition between two timers.
		modelAndAnimationTimer = new Timer(timerDelay, e -> timerCheckModelAndAnimate());
		modelAndAnimationTimer.setRepeats(true);
		modelAndAnimationTimer.start();
		
		renderComponent.addTransformChangeListener(() -> {
			fireThumbnailChanged(null);
		});
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
	
	public void setDebugProgressMonitorFactory(DebugProgressMonitorFactory factory) {
		this.debugProgressMonitorFactory = factory;
	}
	
	public DebugProgressMonitorFactory getDebugProgressMonitorFactory() {
		return debugProgressMonitorFactory;
	}
	
	public Image getImage() {
		return renderComponent.getImage();
	}
	
	public ExecutorService getSingleThreadExecutorService() {
		return singleThreadExecutor;
	}
	
	public LabelInfoProvider getLabelCache() {
		return labelInfoCache;
	}
	
	public Rectangle getComponentBounds() {
		return renderComponent.getBounds();
	}
	
	public Point getComponentCenter() {
		var bounds = renderComponent.getBounds();
		int centerX = bounds.x + bounds.width  / 2;
		int centerY = bounds.y + bounds.height / 2;
		return new Point(centerX, centerY);
	}
	
	public NetworkPicker getPicker() {
		return picker;
	}

	public NetworkTransform getTransform() {
		return renderComponent.getTransform();
	}
	
	public GraphLOD getGraphLOD() {
		return dingGraphLOD;
	}
	
	/**
	 * This is being called by a Swing Timer, so this method is being run on the EDT.
	 * Painting is also done on the EDT. This is how we make sure that viewModelSnapshot does not
	 * change while a frame is being rendered.
	 * 
	 * Also the EDT will coalesce paint events, so if the timer runs faster than the frame rate the
	 * EDT will take care of that.
	 */
	private void timerCheckModelAndAnimate() {
		boolean modelDirty = viewModel.dirty(false);
		if(modelDirty) {
			boolean updated = updateModel();
			if(!updated) {
				return; // If the snapshot couldn't be updated, then don't clear the dirty flag.
			}
			updateAnimationState();
			viewModel.dirty(true); // Clear the dirty flag
		}
		
		boolean paintEdges = advanceAnimatedEdges();
		
		if(modelDirty) {
			updateView(UpdateType.ALL_FULL, true);
		} else if(paintEdges) {
			var flags = renderComponent.getLastFastRenderFlags();
			if (flags != null && flags.renderEdges() != RenderEdges.NONE) {
				updateView(UpdateType.JUST_EDGES);
			}
		}
	}
	
	private void updateAnimationState() { // call only when model is dirty
		if(viewModelSnapshot == null)
			return;
		Collection<View<CyEdge>> animatedEdges = viewModelSnapshot.getTrackedEdges(DingNetworkViewFactory.ANIMATED_EDGES);
		edgeDetails.updateAnimatedEdges(animatedEdges);
		this.animateEdges = !animatedEdges.isEmpty();
	}
	
	private boolean advanceAnimatedEdges() { // call every time the timer thread fires
		if(animateEdges) {
			int trigger = timerDelay / DAnimatedStroke.STEPS_PER_SECOND;
			boolean advance = animationCounter++ % trigger == 0;
			if(advance) {
				edgeDetails.advanceAnimatedEdges();
				return true;
			}
		}
		return false;
	}
	
	
	public void updateView(UpdateType updateType) {
		updateView(updateType, false);
	}
	
	public void updateView(UpdateType updateType, boolean contentChanged) {
		renderComponent.updateView(updateType);
		
		if(contentChanged) {
			fireContentChanged(updateType); // update the BEV
			cyAnnotator.getAnnotationSelection().getBounds();
		}
		
		// Fire this event on another thread (and debounce) so that it doesn't block the renderer
		if(!eventFireTimer.isShutdown())
			eventFireTimer.debounce(() -> eventHelper.fireEvent(new UpdateNetworkPresentationEvent(getViewModel())));
	}
	
	private boolean updateModel() {
		var snapshot = viewModel.createSnapshot();
		if(snapshot == null)
			return false; // Should happen very infrequently, try again on the next frame.
		
		viewModelSnapshot = snapshot;
		
		// Check for important changes between snapshots
		Paint backgroundPaint = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		renderComponent.setBackgroundPaint(backgroundPaint);
		
		// update LOD
		boolean hd = viewModelSnapshot.getVisualProperty(DVisualLexicon.NETWORK_FORCE_HIGH_DETAIL);
		renderComponent.setLOD(hd ? DingGraphLODAll.instance() : dingGraphLOD);
		
		// update view (for example if "fit selected" was run)
		double x = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
		double y = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
		renderComponent.setCenter(x, y);
		
		double scaleFactor = viewModelSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		renderComponent.setScaleFactor(scaleFactor);
		
		return true;
	}
	
	public Color getBackgroundColor() {
		return renderComponent.getBackgroundPaint();
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

	
	public NodeDetails getNodeDetails() {
		return nodeDetails;
	}
	
	public EdgeDetails getEdgeDetails() {
		return edgeDetails;
	}
	
	
	private void fireContentChanged(UpdateType updateType) {
		for(var l : contentChangeListeners) {
			l.contentChanged(updateType);
		}
		fireThumbnailChanged(null);
	}
	
	public void addContentChangeListener(ContentChangeListener l) {
		contentChangeListeners.add(l);
	}

	public void removeContentChangeListener(ContentChangeListener l) {
		contentChangeListeners.remove(l);
	}
	
	public void addTransformChangeListener(TransformChangeListener l) {
		renderComponent.addTransformChangeListener(l);
	}
	
	public void removeTransformChangeListener(TransformChangeListener l) {
		renderComponent.removeTransformChangeListener(l);
	}
	
	public void addThumbnailChangeListener(ThumbnailChangeListener l) {
		thumbnailChangeListeners.add(l);
	}
	
	public void removeThumbnailChangeListener(ThumbnailChangeListener l) {
		thumbnailChangeListeners.remove(l);
	}
	
	void fireThumbnailChanged(Image image) {
		for(var l : thumbnailChangeListeners) {
			l.thumbnailChanged(image);
		}
	}

	
	/**
	 * Set the zoom level and redraw the view.
	 */
	public void setZoom(double zoom) {
		synchronized (dingLock) {
			renderComponent.setScaleFactor(checkZoom(zoom, renderComponent.getTransform().getScaleFactor()));
		}
	}
	
	public double getZoom() {
		return renderComponent.getTransform().getScaleFactor();
	}
	
	@Override
	public void handleFitContent() {
		if(!renderComponent.isInitialized()) {
			renderComponent.setInitializedCallback(() -> {
				renderComponent.setInitializedCallback(null);
				handleFitContent();
			});
			return;
		}
		
		handleFit(false);
	}
	
	@Override
	public void handleFitSelected() {
		if(!renderComponent.isInitialized()) {
			renderComponent.setInitializedCallback(() -> {
				renderComponent.setInitializedCallback(null);
				handleFitSelected();
			});
			return;
		}
		
		handleFit(true);
	}
	
	private void computeNodeBounds(CyNetworkViewSnapshot netViewSnapshot, Collection<View<CyNode>> nodes, double[] buff) {
		SpacialIndex2D<Long> spacial = netViewSnapshot.getSpacialIndex2D();
		
		float[] extents = new float[4];

		float xMin = Float.POSITIVE_INFINITY, yMin = Float.POSITIVE_INFINITY;
		float xMax = Float.NEGATIVE_INFINITY, yMax = Float.NEGATIVE_INFINITY;

		for(View<CyNode> nodeView : nodes) {
			spacial.get(nodeView.getSUID(), extents);
			if (extents[SpacialIndex2D.X_MIN] < xMin) {
				xMin = extents[SpacialIndex2D.X_MIN];
			}
			if (extents[SpacialIndex2D.X_MAX] > xMax) {
				xMax = extents[SpacialIndex2D.X_MAX];
			}
			yMin = Math.min(yMin, extents[SpacialIndex2D.Y_MIN]);
			yMax = Math.max(yMax, extents[SpacialIndex2D.Y_MAX]);
		}
		
		buff[0] = xMin;
		buff[1] = yMin;
		buff[2] = xMax;
		buff[3] = yMax;
	}
	
	
	private void handleFit(boolean justSelectedNodes) {
		eventHelper.flushPayloadEvents();

		synchronized (dingLock) {
			// make sure we use the latest snapshot, don't wait for timer to check dirty flag
			CyNetworkViewSnapshot netViewSnapshot = getViewModel().createSnapshot();
			if(netViewSnapshot == null || netViewSnapshot.getNodeCount() == 0)
				return;
			
			NetworkTransform transform = renderComponent.getTransform();
			if(transform.getWidth() == 0 || transform.getHeight() == 0)
				return;
			
			double[] extents = new double[4];
			
			SpacialIndex2D<Long> spacial = netViewSnapshot.getSpacialIndex2D();
			
			// Get the bounds of the nodes
			Collection<View<CyNode>> selectedNodes = null;
			if(justSelectedNodes) {
				selectedNodes = netViewSnapshot.getTrackedNodes(DingNetworkViewFactory.SELECTED_NODES);
				if(selectedNodes.isEmpty())
					return;
				computeNodeBounds(netViewSnapshot, selectedNodes, extents);
			} else {
				spacial.getMBR(extents); // Minimum Bounding Rectangle: extents of the entire network
			}
			
			// Expand the area to include annotations
			if(!justSelectedNodes) {
				cyAnnotator.adjustBoundsToIncludeAnnotations(extents);
			}
			
			// Expand the area to include node labels, but only if node labels are visible and label caching is enabled.
			int visibleNodes = justSelectedNodes ? selectedNodes.size() : netViewSnapshot.getNodeCount();
			RenderDetailFlags flags = RenderDetailFlags.create(netViewSnapshot, visibleNodes, getGraphLOD());
			
			if(flags.all(LOD_HIGH_DETAIL, LOD_NODE_LABELS, OPT_LABEL_CACHE)) {
				LabelInfoProvider labelCache = getLabelCache();
				NetworkPicker picker = getPicker();
				
				Iterable<View<CyNode>> nodeIterable = justSelectedNodes ? selectedNodes : netViewSnapshot.getNodeViewsIterable();
				
				for(View<CyNode> node : nodeIterable) {
					var label = picker.getNodeLabelShape(node, labelCache);
					if(label != null) {
						var bounds = label.getShape().getBounds2D();
						if(bounds.getMinX() < extents[0])
							extents[0] = bounds.getMinX();
						if(bounds.getMinY() < extents[1])
							extents[1] = bounds.getMinY();
						if(bounds.getMaxX() > extents[2])
							extents[2] = bounds.getMaxX();
						if(bounds.getMaxY() > extents[3])
							extents[3] = bounds.getMaxY();
					}
				}
			}
			
			netViewSnapshot.getMutableNetworkView().batch(netView -> {
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION))
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, (extents[0] + extents[2]) / 2.0d);
				
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION))
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, (extents[1] + extents[3]) / 2.0d);
	
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR)) {
					// Apply a factor 0.98 to zoom, so that it leaves a small border around the network and any annotations.
					final double zoom = Math.min(((double) transform.getWidth())  /  (extents[2] - extents[0]), 
					                             ((double) transform.getHeight()) /  (extents[3] - extents[1])) * 0.98;
					// Update view model.  Zoom Level should be modified.
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
				}
			});
		}
	}
	
	
	@Override
	public void handleUpdateView() {
		// This is a hack to set the dirty flag, because we don't have an API to do that without setting a VP.
		// Just setting the dirty flag allows this method to be non-blocking.
		Boolean val = viewModel.getVisualProperty(DVisualLexicon.DUMMY);
		val = val == null ? Boolean.FALSE : val;
		viewModel.setVisualProperty(DVisualLexicon.DUMMY, !val);
	}
	
	
	public void zoomToCenter(int ticks) {
		int centerX = getTransform().getWidth()  / 2;
		int centerY = getTransform().getHeight() / 2;
		zoomToPointer(ticks, centerX, centerY);
	}
	
	public void zoomToPointer(int ticks, int imageX, int imageY) {
		if(getViewModelSnapshot().isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR))
			return;
		
		double factor = mouseWheelTicksToZoomFactor(ticks);
		if (factor == 1.0)
			return;
		
		var currentTransform = getTransform();
		double newScaleFactor = currentTransform.getScaleFactor() * factor;
		
		// mouse point before zooming in node coords
		var zoomPoint1 = currentTransform.getNodeCoordinates(imageX, imageY);
		
		// mouse point after zooming in node coords (no side effects)
		var scaledTransform = new NetworkTransform(currentTransform);
		scaledTransform.setScaleFactor(newScaleFactor);
		var zoomPoint2 = scaledTransform.getNodeCoordinates(imageX, imageY); 
		
		// amount we have to pan the network to move the zoom point to where the mouse is
		var dx = zoomPoint1.getX() - zoomPoint2.getX(); 
		var dy = zoomPoint1.getY() - zoomPoint2.getY();
		
		// compute where the new center should be after the zoom
		var newCenterX = scaledTransform.getCenterX() + dx;
		var newCenterY = scaledTransform.getCenterY() + dy;
		
		// update the renderComponent (zoom and pan at the same time)
		renderComponent.setScaleFactorAndCenter(newScaleFactor, newCenterX, newCenterY);
		
		// update the view model
		getViewModel().batch(netView -> {
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, newScaleFactor);
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, newCenterX);
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, newCenterY);
		}, false);
	}
	
	private static double mouseWheelTicksToZoomFactor(int ticks) {
		final int maxSpeed = 3;
		ticks = Math.max(-maxSpeed, Math.min(maxSpeed, ticks)); // clamp
		return 1.0 - (ticks * 0.1); 
	}
	
	
	public Panner startPan() {
		return new Panner();
	}
	
	public class Panner {
		private Panner() {}
		
		private boolean panned = false;
		
		public void continuePan(double dx, double dy) {
			if(!panned)
				renderComponent.startPan();
			
			panned = true;
		
			NetworkTransform transform = renderComponent.getTransform();
			double x = transform.getCenterX() + dx;
			double y = transform.getCenterY() + dy;
			renderComponent.setCenter(x, y);
			
			updateView(UpdateType.ALL_FAST);
		}
		
		public void endPan() {
			if(panned) {
				renderComponent.endPan();
				updateCenterVPs();
				updateView(UpdateType.ALL_FULL);
			}
		} 
	}
	
	
	/**
	 * Don't use this method to perform continuous mouse pan motions. 
	 * Only use to set the center as a one-time operation.
	 */
	public void setCenter(double x, double y) {
		synchronized (dingLock) {
			renderComponent.setCenter(x,y);
			updateCenterVPs();
		}
	}

	private void updateCenterVPs() {
		synchronized (dingLock) {
			getViewModel().batch(netView -> {
				NetworkTransform transform = renderComponent.getTransform();
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, transform.getCenterX());
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, transform.getCenterY());
			}, false); // don't set the dirty flag
		}
	}

	
	// File > Print
	@Override
	public int print(Graphics g, PageFormat pageFormat, int page) {
		if(page != 0)
			return NO_SUCH_PAGE;
		
		((Graphics2D) g).translate(pageFormat.getImageableX(), pageFormat.getImageableY());

		// make sure the whole image on the screen will fit to the printable area of the paper
		var transform = new NetworkTransform(renderComponent.getTransform());
		transform.setDPIScaleFactor(1.0);
		
		double image_scale = Math.min(pageFormat.getImageableWidth()  / transform.getWidth(),
									  pageFormat.getImageableHeight() / transform.getHeight());

		if (image_scale < 1.0d) {
			((Graphics2D)g).scale(image_scale, image_scale);
		}

		// from InternalFrameComponent
		g.clipRect(0, 0, renderComponent.getWidth(), renderComponent.getHeight());
		
		PrintLOD printLOD = new PrintLOD();
		CompositeGraphicsCanvas.paint((Graphics2D)g, getBackgroundColor(), printLOD, transform, this, false);
		
		return PAGE_EXISTS;
	}
	

	// File > Export Network to Image... (JPEG, PNG, PDF, POSTSCRIPT, SVG)
	@Override
	public void printCanvas(Graphics g, Map<String,String> props) {
		if(props == null)
			props = Collections.emptyMap();
		
		// Check properties related to printing:
		boolean exportAsShape = Boolean.parseBoolean(props.get("exportTextAsShape"));
		boolean transparent   = Boolean.parseBoolean(props.get("exportTransparentBackground"));
		boolean hideLabels    = Boolean.parseBoolean(props.get("exportHideLabels"));
		boolean highDetail    = Boolean.parseBoolean(props.getOrDefault("highDetail", "true"));
		boolean pdf           = Boolean.parseBoolean(props.get("pdf"));
		
		GraphLOD baseLOD = highDetail ? DingGraphLODAll.instance() : new DingGraphLOD(serviceRegistrar);
		PrintLOD printLOD = new PrintLOD(baseLOD, exportAsShape, !hideLabels);
		
		Color bg = transparent ? null : getBackgroundColor();
		
		// Don't use HiDPI transform when rendering an image.
		var transform = new NetworkTransform(renderComponent.getTransform());
		transform.setDPIScaleFactor(1.0);
		
		CompositeGraphicsCanvas.paint((Graphics2D)g, bg, printLOD, transform, this, pdf);
	}
	
	// File > Export Network to Image... (JPEG, PNG, PDF, POSTSCRIPT, SVG)
	@Override
	public void printCanvas(Graphics g) {
		printCanvas(g, null);
	}
	
	/**
	 * Method to return a reference to an Image object, which represents the current network view.
	 * @param width Width of desired image.
	 * @param height Height of desired image.
	 */
	@Override 
	public Image createImage(int width, int height) {
		return createImage(width, height, false);
	}
	
	
	private Image createImage(int width, int height, boolean transparentBackground) {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("width and height arguments must be greater than zero");

		// Run on the same thread as renderComponent to make sure the canvas is not in the middle of painting
		// MKTODO could we reuse the birds-eye-view buffer instead of doing a full frame draw?
		Future<Image> future = getSingleThreadExecutorService().submit(() -> {
			// MKTODO copy-pasted from fitContent()
			double[] extents = new double[4];
			getViewModelSnapshot().getSpacialIndex2D().getMBR(extents); // extents of the network
			cyAnnotator.adjustBoundsToIncludeAnnotations(extents); // extents of the annotation canvases
			double xCenter = (extents[0] + extents[2]) / 2.0d;
			double yCenter = (extents[1] + extents[3]) / 2.0d;
			double zoom = Math.min(((double) width)  / (extents[2] - extents[0]), 
                                   ((double) height) / (extents[3] - extents[1])) * 0.98;
			
			NetworkTransform transform = new NetworkTransform(width, height, xCenter, yCenter, zoom);
			NetworkImageBuffer buffer = new NetworkImageBuffer(transform);
			Color bgColor = transparentBackground ? null : getBackgroundColor();
			
			CompositeGraphicsCanvas.paint(buffer.getGraphics(true), bgColor, dingGraphLOD, transform, this, false);
			
			return buffer.getImage();
		});
		
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private double checkZoom(double zoom, double orig) {
		if (zoom > 0)
			return zoom;

		logger.debug("invalid zoom: " + zoom + "   using orig: " + orig);
		return orig;
	}


	@Override
	public Printable createPrintable() {
		return this;
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
		return VisualPropertyIconFactory.createIcon(value, vp, w, h);
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
	protected TexturePaint getSnapshot(final double width, final double height) {
		if(!latestSnapshot) {
			// Need to update snapshot.
			snapshotImage = (BufferedImage) createImage(DEF_SNAPSHOT_SIZE, DEF_SNAPSHOT_SIZE, true);
			latestSnapshot = true;
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

	public CyAnnotator getCyAnnotator() {
		return cyAnnotator;
	}
	
	public LabelSelectionManager getLabelSelectionManager() {
		return labelSelectionManager;
	}
	
	public CyServiceRegistrar getServiceRegistrar() {
		return serviceRegistrar;
	}
	
	@Override
	public void handleDispose() {
		dispose();
	}
	
	@Override
	public void dispose() {
		synchronized(this) {
			if (inputHandler != null)
				inputHandler.dispose();
			
			modelAndAnimationTimer.stop();
			eventFireTimer.shutdown();
			cyAnnotator.dispose();
			serviceRegistrar.unregisterAllServices(cyAnnotator);
			renderComponent.dispose();
		}
	}

	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}
	
}
