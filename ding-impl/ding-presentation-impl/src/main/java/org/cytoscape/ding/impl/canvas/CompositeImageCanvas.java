package org.cytoscape.ding.impl.canvas;

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

import org.cytoscape.ding.impl.DRenderingEngine;
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
	private final AnnotationSelectionCanvas annotationSelectionCanvas;
	private final AnnotationCanvas<NetworkImageBuffer> foregroundAnnotationCanvas;
	private final NodeCanvas<NetworkImageBuffer> nodeCanvas;
	private final EdgeCanvas<NetworkImageBuffer> edgeCanvas;
	private final AnnotationCanvas<NetworkImageBuffer> backgroundAnnotationCanvas;
	private final ColorCanvas<NetworkImageBuffer> backgroundColorCanvas;
	
	private GraphLOD lod;
	private final NetworkImageBuffer image;
	
	private final List<DingCanvas<NetworkImageBuffer>> canvasList;
	private final double[] weights;
	
	private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	
	public CompositeImageCanvas(DRenderingEngine re, GraphLOD lod, int w, int h) {
		this.re = re;
		this.lod = lod;
		this.image = new NetworkImageBuffer(w, h);
		
		canvasList = Arrays.asList(
			annotationSelectionCanvas = new AnnotationSelectionCanvas(new NetworkImageBuffer(w, h), re),
			foregroundAnnotationCanvas = new AnnotationCanvas<>(new NetworkImageBuffer(w, h), FOREGROUND, re),
			nodeCanvas = new NodeCanvas<>(new NetworkImageBuffer(w, h), re),
			edgeCanvas = new EdgeCanvas<>(new NetworkImageBuffer(w, h), re),
			backgroundAnnotationCanvas = new AnnotationCanvas<>(new NetworkImageBuffer(w, h), BACKGROUND, re),
			backgroundColorCanvas = new ColorCanvas<>(new NetworkImageBuffer(w, h), null)
		);
		
		// Must paint over top of each other in reverse order
		Collections.reverse(canvasList);
		// This is the proportion of total progress assigned to each canvas. Edge canvas gets the most.
		weights = new double[] {1, 1, 10, 3, 1, 1}; // MKTODO not very elegant
	}
	
	public CompositeImageCanvas(DRenderingEngine re, GraphLOD lod) {
		this(re, lod, 1, 1); // MKTODO does this make sense?
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
		
		for(var canvas : canvasList) {
			Image canvasImage = canvas.paintAndGet(null,flags).getImage();
			overlayImage(image.getImage(), canvasImage);
		}
		
		pm.done();
		return new ImageFuture(image.getImage() ,flags);
	}
	
	/**
	 * Paints on the current thread and blocks until painting is complete.
	 * Returns a future that is already complete (isDone() returns true immediatly).
	 */
	public ImageFuture paintJustAnnotationsOnCurrentThread(ProgressMonitor pm) {
		// MKTODO get rid of pm argument, not needed
		pm = ProgressMonitor.notNull(pm);
		var flags = getRenderDetailFlags();
		pm.start();
		
		overlayImage(image.getImage(), backgroundColorCanvas.getTransform().getImage());
		overlayImage(image.getImage(), backgroundAnnotationCanvas.paintAndGet(null,flags).getImage());
		overlayImage(image.getImage(), edgeCanvas.getTransform().getImage());
		overlayImage(image.getImage(), nodeCanvas.getTransform().getImage());
		overlayImage(image.getImage(), foregroundAnnotationCanvas.paintAndGet(null,flags).getImage());
		overlayImage(image.getImage(), annotationSelectionCanvas.paintAndGet(null,flags).getImage());
		
		pm.done();
		return new ImageFuture(image.getImage(), flags);
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
			var canvas = canvasList.get(i);
			ProgressMonitor subPm = subPms.get(i);
			f = f.thenApplyAsync(compositeImage -> {
				Image image = canvas.paintAndGet(subPm, flags).getImage();
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
			DingCanvas<NetworkImageBuffer> c = canvasList.get(i);
			ProgressMonitor subPm = subPms.get(i);
			var cf = CompletableFuture.supplyAsync(() -> c.paintAndGet(subPm, flags).getImage(), executor);
			f = f.thenCombineAsync(cf, this::overlayImage, executor);
		}
		f.thenRun(pm::done);
		
		return new ImageFuture(f, flags, pm);
	}
	

}
