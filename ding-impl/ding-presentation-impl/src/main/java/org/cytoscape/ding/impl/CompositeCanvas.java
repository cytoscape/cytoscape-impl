package org.cytoscape.ding.impl;

import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.BACKGROUND;
import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.FOREGROUND;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Paint;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cytoscape.ding.impl.work.NoOutputProgressMonitor;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

/**
 * Manages what used to be ContentChangedListener and ViewportChangedListener
 *
 */
@SuppressWarnings("unused")
public class CompositeCanvas {
	
	private final DRenderingEngine re;
	
	// Canvas layers from top to bottom
	private final AnnotationCanvas foregroundAnnotationCanvas;
	private final NodeCanvas nodeCanvas;
	private final EdgeCanvas edgeCanvas;
	private final AnnotationCanvas backgroundAnnotationCanvas;
	private final ColorCanvas backgroundColorCanvas;
	
	private GraphLOD lod;
	private final NetworkImageBuffer image;
	
	private final List<DingCanvas> canvasList;
	private final double[] weights;
	
	private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	
	public CompositeCanvas(DRenderingEngine re, GraphLOD lod, int w, int h) {
		this.re = re;
		this.lod = lod;
		this.image = new NetworkImageBuffer(w, h);
		
		canvasList = Arrays.asList(
			foregroundAnnotationCanvas = new AnnotationCanvas(FOREGROUND, re, w, h),
			nodeCanvas = new NodeCanvas(re, w, h),
			edgeCanvas = new EdgeCanvas(re, w, h),
			backgroundAnnotationCanvas = new AnnotationCanvas(BACKGROUND, re, w, h),
			backgroundColorCanvas = new ColorCanvas(w, h)
		);
		
		// Must paint over top of each other in reverse order
		Collections.reverse(canvasList);
		// This is the proportion of total progress assigned to each canvas. Edge canvas gets the most.
		weights = new double[] {1, 1, 10, 3, 1}; // MKTODO not very elegant
	}
	
	public CompositeCanvas(DRenderingEngine re, GraphLOD lod) {
		this(re, lod, 1, 1); // MKTODO does this make sense?
	}
	
	public CompositeCanvas(DRenderingEngine re, GraphLOD lod, NetworkTransform transform) {
		this(re, lod, transform.getWidth(), transform.getHeight());
		setCenter(transform.getCenterX(), transform.getCenterY());
		setScaleFactor(transform.getScaleFactor());
	}
	
	
	public void dispose() {
		canvasList.forEach(DingCanvas::dispose);
	}
	
	public void setLOD(GraphLOD lod) {
		this.lod = lod;
	}
	
	public NetworkTransform getTransform() {
		return image;
	}
	
	public Image getImage() {
		return image.getImage();
	}
	
	public void setBackgroundPaint(Paint paint) {
		Color color = (paint instanceof Color) ? (Color)paint : ColorCanvas.DEFAULT_COLOR;
		backgroundColorCanvas.setColor(color);
	}
	
	public Color getBackgroundPaint() {
		return backgroundColorCanvas.getColor();
	}
	
	public void setViewport(int width, int height) {
		image.setViewport(width, height);
		canvasList.forEach(c -> c.setViewport(width, height));
	}
	
	public void setCenter(double x, double y) {
		image.setCenter(x, y);
		canvasList.forEach(c -> c.setCenter(x, y));
	}
	
	public void setScaleFactor(double scaleFactor) {
		image.setScaleFactor(scaleFactor);
		canvasList.forEach(c -> c.setScaleFactor(scaleFactor));
	}
	

	public RenderDetailFlags getRenderDetailFlags() {
		var snapshot = re.getViewModelSnapshot();
		return RenderDetailFlags.create(snapshot, image, lod);
	}
	
	
	private Image overlayImage(Image composite, Image image) {
		if(image != null) {
			Graphics g = composite.getGraphics();
			g.drawImage(image, 0, 0, null);
		}
		return composite;
	}
	
	
	public ImageFuture paintOnCurrentThread() {
		return paintOnCurrentThread(null);
	}
	
	/**
	 * Paints on the current thread and blocks until painting is complete.
	 * Returns a future that is already complete (isDone() returns true immediatly).
	 */
	public ImageFuture paintOnCurrentThread(ProgressMonitor pm) {
		// MKTODO get rid of pm argument, not needed
		pm = ProgressMonitor.notNull(pm);
		var flags = getRenderDetailFlags();
		pm.start();
		
		for(DingCanvas c : canvasList) {
			Image canvasImage = c.paintImage(new NoOutputProgressMonitor(), flags);
			overlayImage(image.getImage(), canvasImage);
		}
		
		pm.done();
		var future = CompletableFuture.completedFuture(image.getImage());
		return new ImageFuture(future ,flags);
	}

	
	/**
	 * Starts painting on a single separate thread. 
	 * Each layer of the canvas is painted sequentially in order. 
	 * Returns an ImageFuture that represents the result of the painting.
	 * To get the Image buffer from the ImageFuture call future.join().
	 */
	public ImageFuture startPaintingSequential(ProgressMonitor pm) {
		pm = ProgressMonitor.notNull(pm);
		var flags = getRenderDetailFlags();
		var subPms = pm.split(weights);
		pm.start();
		
		var f = CompletableFuture.completedFuture(image.getImage());
		for(int i = 0; i < canvasList.size(); i++) {
			DingCanvas c = canvasList.get(i);
			ProgressMonitor subPm = subPms.get(i);
			f = f.thenApplyAsync(compositeImage -> {
				Image image = c.paintImage(subPm, flags);
				return overlayImage(compositeImage, image);
			}, singleThreadExecutor);
		}
		f.thenRun(pm::done);
		
		return new ImageFuture(f, flags, pm);
	}
	
	
	/**
	 * Starts painting using a thread pool provided by the given ExecutorService. 
	 * Each layer of the canvas is painted concurrently.
	 * Returns an ImageFuture that represents the result of the painting.
	 * To get the Image buffer from the ImageFuture call future.join().
	 */
	public ImageFuture startPaintingConcurrent(ProgressMonitor pm, ExecutorService executor) {
		pm = ProgressMonitor.notNull(pm);
		var flags = getRenderDetailFlags();
		var subPms = pm.split(weights);
		
		pm.start();
		
		var f = CompletableFuture.completedFuture(image.getImage());
		for(int i = 0; i < canvasList.size(); i++) {
			DingCanvas c = canvasList.get(i);
			ProgressMonitor subPm = subPms.get(i);
			var cf = CompletableFuture.supplyAsync(() -> c.paintImage(subPm, flags), executor);
			f = f.thenCombineAsync(cf, this::overlayImage, executor);
		}
		f.thenRun(pm::done);
		
		return new ImageFuture(f, flags, pm);
	}
	

}
