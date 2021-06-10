package org.cytoscape.ding.impl.canvas;

import static org.cytoscape.ding.impl.DRenderingEngine.UpdateType.*;
import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.BACKGROUND;
import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.FOREGROUND;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.cytoscape.ding.debug.DebugRootProgressMonitor;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

/**
 * Manages what used to be ContentChangedListener and ViewportChangedListener
 *
 */
public class CompositeImageCanvas {
	
	private final DRenderingEngine re;
	
	// Canvas layers from top to bottom
	private final SelectionCanvas<ImageGraphicsProvider> selectionCanvas;
	private final AnnotationCanvas<ImageGraphicsProvider> fgAnnotationCanvas;
	private final NodeCanvas<ImageGraphicsProvider> nodeCanvas;
	private final EdgeCanvas<ImageGraphicsProvider> edgeCanvas;
	private final AnnotationCanvas<ImageGraphicsProvider> bgAnnotationCanvas;
	
	private Color bgColor = Color.WHITE;
	
	private GraphLOD lod;
	private final ImageGraphicsProvider image;
	private final NetworkTransform transform;
	
	private final double[] weights;
	
	private final Executor executor;
	
	
	public CompositeImageCanvas(DRenderingEngine re, GraphLOD lod, int w, int h) {
		this.re = re;
		this.lod = lod;
		this.transform = new NetworkTransform(w, h);
		this.image = newBuffer(transform);
		
		selectionCanvas = new SelectionCanvas<>(NullGraphicsProvider.INSTANCE, re);
		fgAnnotationCanvas = new AnnotationCanvas<>(NullGraphicsProvider.INSTANCE, re, FOREGROUND);
		nodeCanvas = new NodeCanvas<>(newBuffer(transform), re);
		edgeCanvas = new EdgeCanvas<>(newBuffer(transform), re);
		bgAnnotationCanvas = new AnnotationCanvas<>(NullGraphicsProvider.INSTANCE, re, BACKGROUND);
	
		// This is the proportion of total progress assigned to each canvas. Edge canvas gets the most.
		// In reverse order because that's the order they are painted.
		weights = new double[] {1, 20, 3, 1, 0}; // MKTODO not very elegant
		
		re.getCyAnnotator().addPropertyChangeListener(e -> updateAnnotationAndSelectionCanvasBuffers());
		re.getLabelSelectionManager().addPropertyChangeListener(e -> updateAnnotationAndSelectionCanvasBuffers());
		updateAnnotationAndSelectionCanvasBuffers();
		
		this.executor = re.getSingleThreadExecutorService();
	}
	
	
	private static ImageGraphicsProvider newBuffer(NetworkTransform transform) {
		return new NetworkImageBuffer(transform);
	}
	
	public EdgeCanvas<ImageGraphicsProvider> getEdgeCanvas() {
		return edgeCanvas;
	}
	
	// Kind of hackey, we don't want the annotation selection to show up in the brids-eye-view
	public void showAnnotationSelection(boolean show) {
		selectionCanvas.show(show);
		fgAnnotationCanvas.setShowSelection(show);
		bgAnnotationCanvas.setShowSelection(show);
	}
	
	private void updateAnnotationAndSelectionCanvasBuffers() {
		// This is a memory optimization, don't allocate buffers for the annotation canvases if there are no annotations to render.
		var cyAnnotator = re.getCyAnnotator();
		boolean hasFG = cyAnnotator.hasAnnotations(CanvasID.FOREGROUND);
		boolean hasBG = cyAnnotator.hasAnnotations(CanvasID.BACKGROUND);
		boolean hasLabelSel = !re.getLabelSelectionManager().isEmpty();
		boolean hasSel = hasFG || hasBG || hasLabelSel;
		
		if(hasFG && fgAnnotationCanvas.getGraphicsProvier() instanceof NullGraphicsProvider) {
			fgAnnotationCanvas.setGraphicsProvider(newBuffer(transform));
		}
		if(!hasFG && !(fgAnnotationCanvas.getGraphicsProvier() instanceof NullGraphicsProvider)) {
			fgAnnotationCanvas.setGraphicsProvider(NullGraphicsProvider.INSTANCE);
		}
		
		if(hasBG && bgAnnotationCanvas.getGraphicsProvier() instanceof NullGraphicsProvider) {
			bgAnnotationCanvas.setGraphicsProvider(newBuffer(transform));
		}
		if(!hasBG && !(bgAnnotationCanvas.getGraphicsProvier() instanceof NullGraphicsProvider)) {
			bgAnnotationCanvas.setGraphicsProvider(NullGraphicsProvider.INSTANCE);
		}
		
		if(hasSel && selectionCanvas.getGraphicsProvier() instanceof NullGraphicsProvider) {
			selectionCanvas.setGraphicsProvider(newBuffer(transform));
		}
		if(!hasSel && !(selectionCanvas.getGraphicsProvier() instanceof NullGraphicsProvider)) {
			selectionCanvas.setGraphicsProvider(NullGraphicsProvider.INSTANCE);
		}
	}
	
	public void dispose() {
		selectionCanvas.dispose();
		fgAnnotationCanvas.dispose();
		nodeCanvas.dispose();
		edgeCanvas.dispose();
		bgAnnotationCanvas.dispose();
	}
	
	public void setLOD(GraphLOD lod) {
		this.lod = lod;
	}
	
	public NetworkTransform getTransform() {
		return transform;
	}
	
	public void setBackgroundPaint(Paint paint) {
		this.bgColor = (paint instanceof Color) ? (Color)paint : Color.WHITE;
	}
	
	public Color getBackgroundPaint() {
		return bgColor;
	}
	
	protected RenderDetailFlags getRenderDetailFlags(UpdateType updateType) {
		var snapshot = re.getViewModelSnapshot();
		return RenderDetailFlags.create(snapshot, transform, lod, updateType);
	}
	
	
	private Image overlayImage(Image composite, Image image) {
		return overlayImage(composite, image, 0, 0);
	}
	
	private Image overlayImage(Image composite, Image image, int dx, int dy) {
		if(image != null) {
			Graphics g = composite.getGraphics();
			g.drawImage(image, dx, dy, null);
		}
		return composite;
	}
	
	
	public static class PaintParameters {
		private final boolean isPan;
		private final UpdateType update;
		private final int panDx;
		private final int panDy;
		private CompositeImageCanvas slowCanvas;
		private final String panCanvasName;
		
		private PaintParameters(UpdateType updateType, boolean isPan, int panDx, int panDy, CompositeImageCanvas slowCanvas, String panCanvasName) {
			this.update = updateType;
			this.isPan = isPan;
			this.panDx = panDx;
			this.panDy = panDy;
			this.slowCanvas = slowCanvas;
			this.panCanvasName = panCanvasName;
		}
		
		public static PaintParameters updateType(UpdateType updateType) {
			return new PaintParameters(updateType, false, 0, 0, null, null);
		}
		
		public static PaintParameters pan(int panDx, int panDy, CompositeImageCanvas slowCanvas, String panCanvasName) {
			return new PaintParameters(UpdateType.ALL_FAST, true, panDx, panDy, slowCanvas, panCanvasName);
		}
		
		public boolean isPan() {
			return isPan;
		}
		public UpdateType getUpdate() {
			return update;
		}
		public int getPanDx() {
			return panDx;
		}
		public int getPanDy() {
			return panDy;
		}
		public String getPanCanvasName() {
			return panCanvasName;
		}
		public void done() {
			slowCanvas = null;
		}
	}
	
	
	/**
	 * Starts painting on a single separate thread. 
	 * Each layer of the canvas is painted sequentially in order. 
	 * Returns an ImageFuture that represents the result of the painting.
	 * To get the Image buffer from the ImageFuture call future.join().
	 */
	public ImageFuture paint(ProgressMonitor pm, PaintParameters params) {
		var pm2 = ProgressMonitor.notNull(pm);
		var flags = getRenderDetailFlags(params.update);
		var future = CompletableFuture.supplyAsync(() -> paintImpl(pm2, flags, params), executor);
		return new ImageFuture(future, flags, pm2);
	}
	
	public ImageFuture paint(ProgressMonitor pm) {
		return paint(pm, PaintParameters.updateType(ALL_FULL));
	}
	
	
	// Methods to determine which canvases are rendered.
	private static boolean renderAllCanvases(UpdateType ut) {
		return ut == ALL_FAST || ut == ALL_FULL;
	}
	private static boolean renderNodeCanvas(UpdateType ut) {
		return renderAllCanvases(ut);
	}
	private static boolean renderEdgeCanvas(UpdateType ut) {
		return renderAllCanvases(ut) || ut == JUST_EDGES;
	}
	private static  boolean renderAnnotationCanvas(UpdateType ut) {
		return renderAllCanvases(ut) || ut == JUST_ANNOTATIONS;
	}
	
	
	private Image paintImpl(ProgressMonitor pm, RenderDetailFlags flags, PaintParameters params) {
		var pms = pm.split(weights);
		pm.start("Frame"); // debug message
		
		// Render layers from bottom to top
		
		// Background color
		Image composite = image.getImage();
		fill(composite, bgColor);
		
		// Annotation background layer
		if(renderAnnotationCanvas(params.update)) {
			Image image = bgAnnotationCanvas.paintAndGet(pms[0], flags).getImage();
			overlayImage(composite, image);
		} else {
			Image image = bgAnnotationCanvas.getCurrent(pms[0]).getImage();
			overlayImage(composite, image);
		}
		
		// Edge layer
		if(renderEdgeCanvas(params.update)) {
			if(params.isPan) {
				// edge buffer pan optimization
				Image image = params.slowCanvas.getEdgeCanvas().getGraphicsProvier().getImage();
				overlayImage(composite, image, params.panDx, params.panDy);
				pms[1].addProgress(1.0);
			} else {
				Image image = edgeCanvas.paintAndGet(pms[1], flags).getImage();
				overlayImage(composite, image);
			}
		} else {
			Image image = edgeCanvas.getCurrent(pms[1]).getImage();
			overlayImage(composite, image);
		}
		
		// Node layer
		if(renderNodeCanvas(params.update)) {
			Image image = nodeCanvas.paintAndGet(pms[2], flags).getImage();
			overlayImage(composite, image);
		} else {
			Image image = nodeCanvas.getCurrent(pms[2]).getImage();
			overlayImage(composite, image);
		}
		
		// Annotation foreground layer
		if(renderAnnotationCanvas(params.update)) {
			Image image = fgAnnotationCanvas.paintAndGet(pms[3], flags).getImage();
			overlayImage(composite, image);
		} else {
			Image image = fgAnnotationCanvas.getCurrent(pms[3]).getImage();
			overlayImage(composite, image);
		}
		
		// Annotation selection layer
		if(renderAnnotationCanvas(params.update)) {
			Image image = selectionCanvas.paintAndGet(pms[4], flags).getImage();
			overlayImage(composite, image);
		} else {
			Image image = selectionCanvas.getCurrent(pms[4]).getImage();
			overlayImage(composite, image);
		}
		
		
		params.done();
		if(pm instanceof DebugRootProgressMonitor) // MKTODO hackey
			((DebugRootProgressMonitor)pm).done(flags, params);
		else
			pm.done();
		
		return composite;
	}
	
	
	private void fill(Image image, Color color) {
		NetworkTransform t = getTransform();
		Graphics2D g = (Graphics2D) image.getGraphics();
		if(g != null) {
			if(color == null) {
				color = new Color(0,0,0,0); // transparent
			}
			g.setColor(color);
			g.fillRect(0, 0, t.getPixelWidth(), t.getPixelHeight());
		}
	}

}
