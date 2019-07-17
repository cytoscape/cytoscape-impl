package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Paint;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.service.util.CyServiceRegistrar;

/**
 * Manages what used to be ContentChangedListener and ViewportChangedListener
 *
 */
public class CompositeCanvas {
	
	private final NetworkImageBuffer image = new NetworkImageBuffer();
	
//	private AnnotationSelection selection;
	private AnnotationCanvas foregroundAnnotationCanvas;
	private InnerCanvas networkCanvas;
	private AnnotationCanvas backgroundAnnotationCanvas;
	private ColorCanvas backgroundColorCanvas;
	
	private GraphLOD lod;
	
	private final List<DingCanvas> canvasList;
	
	private final ExecutorService executor;
	
	
	public CompositeCanvas(CyServiceRegistrar registrar, DRenderingEngine re, DingLock dingLock, GraphLOD lod) {
		this.lod = lod;
		
		// Must be in reverse order
		// MKTODO add an annotation selection canvas?
		canvasList = Arrays.asList(
			backgroundColorCanvas = new ColorCanvas(),
			backgroundAnnotationCanvas = new AnnotationCanvas(this, DingAnnotation.CanvasID.BACKGROUND, re),
			networkCanvas = new InnerCanvas(dingLock, this, re, registrar),
			foregroundAnnotationCanvas = new AnnotationCanvas(this, DingAnnotation.CanvasID.FOREGROUND, re)
		);
		
		// MKTODO what's the best thread pool for this?
		executor = Executors.newCachedThreadPool();
	}
	
	public void dispose() {
		canvasList.forEach(DingCanvas::dispose);
	}
	
	public void setLOD(GraphLOD lod) {
		this.lod = lod;
	}
	
	public GraphLOD getLOD() {
		return lod;
	}
	
	public NetworkTransform getNetworkTransform() {
		return image;
	}
	
	public int getLastRenderDetail() {
		return networkCanvas.getLastRenderDetail();
	}
	
	public boolean treatNodeShapesAsRectangle() {
		return (getLastRenderDetail() & GraphRenderer.LOD_HIGH_DETAIL) == 0;
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
	
	public void adjustBoundsToIncludeAnnotations(double[] extentsBuff) {
		foregroundAnnotationCanvas.adjustBounds(extentsBuff);
		backgroundAnnotationCanvas.adjustBounds(extentsBuff);
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
	
	public double getCenterX() {
		return image.getX();
	}
	
	public double getCenterY() {
		return image.getY();
	}
	
	public double getScaleFactor() {
		return image.getScaleFactor();
	}
	
	public int getHeight() {
		return image.getHeight();
	}
	
	public int getWidth() {
		return image.getWidth();
	}
	
	private static Image overlayImage(Image composite, Image image) {
		Graphics g = composite.getGraphics();
		g.drawImage(image, 0, 0, null);
		return composite;
	}
	
	
	public void paintBlocking(Graphics g) {
		// this can still be parallelized, just blocking
		// What if a frame is currently being renderered
		for(DingCanvas c : canvasList) {
			c.paintImage();
			Image canvasImage = c.get();
			overlayImage(image.getImage(), canvasImage);
		}
		g.drawImage(image.getImage(), 0, 0, null);
	}
	
	
	private void paintParallel(Graphics g) {
		CompletableFuture<Image> f = CompletableFuture.completedFuture(image.getImage());
		for(DingCanvas c : canvasList) {
			CompletableFuture<Image> cf = CompletableFuture.supplyAsync(c, executor);
			f = f.thenCombineAsync(cf, CompositeCanvas::overlayImage, executor);
		}
		
		try {
			f.get(); // block
			g.drawImage(image.getImage(), 0, 0, null);
		} catch (InterruptedException | ExecutionException e) {
			// MKTODO what to do here?
			e.printStackTrace();
		}
	}
	
	
	public void print(Graphics g) {
		
	}
	
	public void paintAsync(Runnable callback) {
		
	}
	
}
