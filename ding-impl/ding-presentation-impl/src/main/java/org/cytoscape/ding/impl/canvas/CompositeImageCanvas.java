package org.cytoscape.ding.impl.canvas;

import static org.cytoscape.ding.impl.DRenderingEngine.UpdateType.ALL_FULL;
import static org.cytoscape.ding.impl.DRenderingEngine.UpdateType.JUST_ANNOTATIONS;
import static org.cytoscape.ding.impl.DRenderingEngine.UpdateType.JUST_EDGES;
import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.BACKGROUND;
import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.FOREGROUND;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
	private final AnnotationSelectionCanvas<ImageGraphicsProvider> annotationSelectionCanvas;
	private final AnnotationCanvas<ImageGraphicsProvider> fgAnnotationCanvas;
	private final NodeCanvas<ImageGraphicsProvider> nodeCanvas;
	private final EdgeCanvas<ImageGraphicsProvider> edgeCanvas;
	private final AnnotationCanvas<ImageGraphicsProvider> bgAnnotationCanvas;
	
	private Color bgColor = Color.WHITE;
	
	private GraphLOD lod;
	private final ImageGraphicsProvider image;
	private final NetworkTransform transform;
	
	private final List<DingCanvas<ImageGraphicsProvider>> canvasList;
	private final double[] weights;
	
	private final Executor executor;
	
	public CompositeImageCanvas(DRenderingEngine re, GraphLOD lod, int w, int h) {
		this.re = re;
		this.lod = lod;
		this.transform = new NetworkTransform(w, h);
		this.image = newBuffer(transform);
		
		canvasList = Arrays.asList(
			annotationSelectionCanvas = new AnnotationSelectionCanvas<>(NullGraphicsProvider.INSTANCE, re),
			fgAnnotationCanvas = new AnnotationCanvas<>(NullGraphicsProvider.INSTANCE, re, FOREGROUND),
			nodeCanvas = new NodeCanvas<>(newBuffer(transform), re),
			edgeCanvas = new EdgeCanvas<>(newBuffer(transform), re),
			bgAnnotationCanvas = new AnnotationCanvas<>(NullGraphicsProvider.INSTANCE, re, BACKGROUND)
		);
	
		// Must paint over top of each other in reverse order
		Collections.reverse(canvasList);
		// This is the proportion of total progress assigned to each canvas. Edge canvas gets the most.
		weights = new double[] {1, 20, 3, 1, 0}; // MKTODO not very elegant
		
		re.getCyAnnotator().addPropertyChangeListener(e -> updateAnnotationCanvasBuffers());
		updateAnnotationCanvasBuffers();
		
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
		annotationSelectionCanvas.show(show);
		fgAnnotationCanvas.setShowSelection(show);
		bgAnnotationCanvas.setShowSelection(show);
	}
	
	private void updateAnnotationCanvasBuffers() {
		// This is a memory optimization, don't allocate buffers for the annotation canvases if there are no annotations to render.
		var cyAnnotator = re.getCyAnnotator();
		boolean hasFG = cyAnnotator.hasAnnotations(CanvasID.FOREGROUND);
		boolean hasBG = cyAnnotator.hasAnnotations(CanvasID.BACKGROUND);
		boolean hasAnn = hasFG || hasBG;
		
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
		
		if(hasAnn && annotationSelectionCanvas.getGraphicsProvier() instanceof NullGraphicsProvider) {
			annotationSelectionCanvas.setGraphicsProvider(newBuffer(transform));
		}
		if(!hasAnn && !(annotationSelectionCanvas.getGraphicsProvier() instanceof NullGraphicsProvider)) {
			annotationSelectionCanvas.setGraphicsProvider(NullGraphicsProvider.INSTANCE);
		}
	}
	
	public void dispose() {
		canvasList.forEach(DingCanvas::dispose);
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
	
	public void setViewport(int width, int height) {
		transform.setViewport(width, height);
	}
	
	public void setCenter(double x, double y) {
		transform.setCenter(x, y);
	}
	
	public void setScaleFactor(double scaleFactor) {
		transform.setScaleFactor(scaleFactor);
	}

	public RenderDetailFlags getRenderDetailFlags() {
		var snapshot = re.getViewModelSnapshot();
		return RenderDetailFlags.create(snapshot, transform, lod);
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
		
		private PaintParameters(UpdateType updateType, boolean isPan, int panDx, int panDy, CompositeImageCanvas slowCanvas) {
			this.update = updateType;
			this.isPan = isPan;
			this.panDx = panDx;
			this.panDy = panDy;
			this.slowCanvas = slowCanvas;
		}
		
		public static PaintParameters updateType(UpdateType updateType) {
			return new PaintParameters(updateType, false, 0, 0, null);
		}
		
		public static PaintParameters pan(int panDx, int panDy, CompositeImageCanvas slowCanvas) {
			return new PaintParameters(UpdateType.ALL_FAST, true, panDx, panDy, slowCanvas);
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
		var flags = getRenderDetailFlags();
		var future = CompletableFuture.supplyAsync(() -> paintImpl(pm2, flags, params), executor);
		return new ImageFuture(future, flags, pm2);
	}
	
	public ImageFuture paint(ProgressMonitor pm) {
		return paint(pm, PaintParameters.updateType(ALL_FULL));
	}
	
	
	private Image paintImpl(ProgressMonitor pm, RenderDetailFlags flags, PaintParameters params) {
		var subPms = pm.split(weights);
		pm.start("Frame"); // debug message
		
		Image composite = image.getImage();
		fill(composite, bgColor);
		
		// Annotation background layer
		if(params.update == JUST_EDGES) {
			Image image = bgAnnotationCanvas.getCurrent(subPms[0]).getImage();
			overlayImage(composite, image);
		} else {
			Image image = bgAnnotationCanvas.paintAndGet(subPms[0], flags).getImage();
			overlayImage(composite, image);
		}
		
		// Edge layer
		if(params.update == JUST_ANNOTATIONS) {
			Image image = edgeCanvas.getCurrent(subPms[1]).getImage();
			overlayImage(composite, image);
		} else if(params.isPan) {
			Image image = params.slowCanvas.getEdgeCanvas().getGraphicsProvier().getImage();
			overlayImage(composite, image, params.panDx, params.panDy);
			subPms[1].addProgress(1.0);
		} else {
			Image image = edgeCanvas.paintAndGet(subPms[1], flags).getImage();
			overlayImage(composite, image);
		}
		
		// Node layer
		if(params.update == JUST_ANNOTATIONS) {
			Image image = nodeCanvas.getCurrent(subPms[2]).getImage();
			overlayImage(composite, image);
		} else {
			Image image = nodeCanvas.paintAndGet(subPms[2], flags).getImage();
			overlayImage(composite, image);
		}
		
		// Annotation foreground layer
		if(params.update == JUST_EDGES) {
			Image image = fgAnnotationCanvas.getCurrent(subPms[3]).getImage();
			overlayImage(composite, image);
		} else {
			Image image = fgAnnotationCanvas.paintAndGet(subPms[3], flags).getImage();
			overlayImage(composite, image);
		}
		
		// Annotation selection layer
		if(params.update == JUST_EDGES) {
			Image image = annotationSelectionCanvas.getCurrent(subPms[4]).getImage();
			overlayImage(composite, image);
		} else {
			Image image = annotationSelectionCanvas.paintAndGet(subPms[4], flags).getImage();
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
			g.fillRect(0, 0, t.getWidth(), t.getHeight());
		}
	}
	
//	/**
//	 * Starts painting using a thread pool provided by the given ExecutorService. 
//	 * Each layer of the canvas is painted concurrently.
//	 * Returns an ImageFuture that represents the result of the painting.
//	 * To get the Image buffer from the ImageFuture call future.join().
//	 * 
//	 * NOTE: Not currently being used. Need to revisit locks and other concurrency controls
//	 * if we want to render in parallel. It doesn't work properly at the moment and
//	 * results in flicker and strange behaviour.
//	 */
//	private ImageFuture startPaintingConcurrent(ProgressMonitor pm, ExecutorService executor) {
//		pm = ProgressMonitor.notNull(pm);
//		var flags = getRenderDetailFlags();
//		var subPms = pm.split(weights);
//		
//		pm.start();
//		
//		var f = CompletableFuture.completedFuture(image.getImage());
//		for(int i = 0; i < canvasList.size(); i++) {
//			var canvas = canvasList.get(i);
//			var subPm = subPms.get(i);
//			var cf = CompletableFuture.supplyAsync(() -> canvas.paintAndGet(subPm, flags).getImage(), executor);
//			f = f.thenCombineAsync(cf, this::overlayImage, executor);
//		}
//		f.thenRun(pm::done);
//		
//		return new ImageFuture(f, flags, pm);
//	}
	

}
