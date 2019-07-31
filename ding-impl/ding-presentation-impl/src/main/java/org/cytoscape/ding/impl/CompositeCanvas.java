package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Paint;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.ding.impl.work.ConsoleProgressMonitor;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.service.util.CyServiceRegistrar;

/**
 * Manages what used to be ContentChangedListener and ViewportChangedListener
 *
 */
public class CompositeCanvas {
	
	private GraphLOD lod;
	private final DRenderingEngine re;
	
	// Canvas layers from top to bottom
	private final AnnotationSelectionCanvas annotationSelectionCanvas;
	private final AnnotationCanvas foregroundAnnotationCanvas;
	private final NodeCanvas nodeCanvas;
	private final EdgeCanvas edgeCanvas;
	private final AnnotationCanvas backgroundAnnotationCanvas;
	private final ColorCanvas backgroundColorCanvas;
	
	private final NetworkImageBuffer image = new NetworkImageBuffer();
	
	private final List<DingCanvas> canvasList;
	private final double[] weights;
	private final ExecutorService executor;
	
	private RenderDetailFlags lastRenderFlags;
	
	public CompositeCanvas(CyServiceRegistrar registrar, DRenderingEngine re, GraphLOD lod, ExecutorService executor) {
		this.lod = lod;
		this.re = re;
		
		canvasList = Arrays.asList(
			annotationSelectionCanvas = new AnnotationSelectionCanvas(),
			foregroundAnnotationCanvas = new AnnotationCanvas(CanvasID.FOREGROUND, re),
			nodeCanvas = new NodeCanvas(this, re, registrar),
			edgeCanvas = new EdgeCanvas(this, re),
			backgroundAnnotationCanvas = new AnnotationCanvas(CanvasID.BACKGROUND, re),
			backgroundColorCanvas = new ColorCanvas()
		);
		
		// Must paint over top of each other in reverse order
		Collections.reverse(canvasList);
		// This is the proportion of total progress assigned to each canvas. Edge canvas gets the most.
		weights = new double[] {1, 1, 10, 3, 1, 1}; // MKTODO not very elegant
		
		// MKTODO what's the best thread pool for this?
		this.executor = executor;
	}
	
	
	public void dispose() {
		canvasList.forEach(DingCanvas::dispose);
	}
	
	public void setAnnotationSelection(AnnotationSelection selection) {
		annotationSelectionCanvas.setSelection(selection);
	}
	
	public void setLOD(GraphLOD lod) {
		this.lod = lod;
	}
	
	public GraphLOD getLOD() {
		return lod;
	}
	
	public NetworkTransform getTransform() {
		return image;
	}
	
	public Image getImage() {
		return image.getImage();
	}
	
	public RenderDetailFlags getLastRenderDetailFlags() {
		return lastRenderFlags;
	}
	
	public boolean treatNodeShapesAsRectangle() {
		return lastRenderFlags.not(RenderDetailFlags.LOD_HIGH_DETAIL);
	}
	
	public AnnotationCanvas getAnnotationCanvas(DingAnnotation.CanvasID canvasID) {
		if(canvasID == CanvasID.FOREGROUND)
			return foregroundAnnotationCanvas;
		if(canvasID == CanvasID.BACKGROUND)
			return backgroundAnnotationCanvas;
		throw new NullPointerException();
	}
	
	public void setBackgroundPaint(Paint paint) {
		if(paint instanceof Color)
			backgroundColorCanvas.setColor((Color)paint);
		else
			backgroundColorCanvas.setColor(ColorCanvas.DEFAULT_COLOR);
	}
	
	public boolean adjustBoundsToIncludeAnnotations(double[] extentsBuff) {
		// Returns true if either annotation canvas contains at least one annotation
		return foregroundAnnotationCanvas.adjustBoundsToIncludeAnnotations(extentsBuff)
			 | backgroundAnnotationCanvas.adjustBoundsToIncludeAnnotations(extentsBuff);
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
	

	private static Image overlayImage(Image composite, Image image) {
		if(image != null) {
			Graphics g = composite.getGraphics();
			g.drawImage(image, 0, 0, null);
		}
		return composite;
	}
	
	public void paintBlocking(Graphics g) {
		lastRenderFlags = RenderDetailFlags.create(re.getViewModelSnapshot(), image, lod, re.getEdgeDetails());
		paintParallelBlocking(g);
	}
	
	
	
	private void paintParallelBlocking(Graphics g) {
		ConsoleProgressMonitor pm = new ConsoleProgressMonitor("CompositeCanvas");
		pm.start();
		
		List<ProgressMonitor> subPms = pm.split(weights);
		
		CompletableFuture<Image> f = CompletableFuture.completedFuture(image.getImage());
		
		for(int i = 0; i < canvasList.size(); i++) {
			DingCanvas c = canvasList.get(i);
			ProgressMonitor subPm = subPms.get(i);
			CompletableFuture<Image> cf = CompletableFuture.supplyAsync(() -> c.paintImage(subPm, lastRenderFlags), executor);
			f = f.thenCombineAsync(cf, CompositeCanvas::overlayImage, executor);
		}
		
		try {
			f.get(); // block the current thread, wait for other threads to complete
			if(g != null) {
				g.drawImage(image.getImage(), 0, 0, null);
			}
			pm.done();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			pm.cancel();
		}
	}
	
	public void print(Graphics g) {
		
	}

}
