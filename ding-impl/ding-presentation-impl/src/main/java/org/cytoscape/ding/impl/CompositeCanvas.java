package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Paint;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.ding.impl.work.NoOutputProgressMonitor;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewSnapshot;

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
	private final NetworkImageBuffer image = new NetworkImageBuffer();
	
	private final List<DingCanvas> canvasList;
	private final double[] weights;
	
	
	public CompositeCanvas(CyServiceRegistrar registrar, DRenderingEngine re, GraphLOD lod) {
		this.re = re;
		setLOD(lod);
		
		canvasList = Arrays.asList(
			foregroundAnnotationCanvas = new AnnotationCanvas(CanvasID.FOREGROUND, re),
			nodeCanvas = new NodeCanvas(this, re, registrar),
			edgeCanvas = new EdgeCanvas(this, re),
			backgroundAnnotationCanvas = new AnnotationCanvas(CanvasID.BACKGROUND, re),
			backgroundColorCanvas = new ColorCanvas()
		);
		
		// Must paint over top of each other in reverse order
		Collections.reverse(canvasList);
		// This is the proportion of total progress assigned to each canvas. Edge canvas gets the most.
		weights = new double[] {1, 1, 10, 3, 1}; // MKTODO not very elegant
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
	

	private Image overlayImage(Image composite, Image image) {
		if(image != null) {
			Graphics g = composite.getGraphics();
			g.drawImage(image, 0, 0, null);
		}
		return composite;
	}
	
	public ImageFuture startPainting(ExecutorService executor) {
		return startPainting(new NoOutputProgressMonitor(), executor);
	}
	
	public ImageFuture startPainting(ProgressMonitor pm, ExecutorService executor) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		RenderDetailFlags flags = RenderDetailFlags.create(snapshot, image, lod);
		CompletableFuture<Image> future = paintLayersMultiThreaded(pm, executor, flags);
		return new ImageFuture(pm, future, flags);
	}
	
	private CompletableFuture<Image> paintLayersMultiThreaded(ProgressMonitor pm, ExecutorService executor, RenderDetailFlags flags) {
		pm.start();
		List<ProgressMonitor> subPms = pm.split(weights);
		
		CompletableFuture<Image> f = CompletableFuture.completedFuture(image.getImage());
		for(int i = 0; i < canvasList.size(); i++) {
			DingCanvas c = canvasList.get(i);
			ProgressMonitor subPm = subPms.get(i);
			CompletableFuture<Image> cf = CompletableFuture.supplyAsync(() -> c.paintImage(subPm, flags), executor);
			f = f.thenCombineAsync(cf, this::overlayImage, executor);
		}
		f.thenRun(pm::done);
		return f;
	}
	
	
	public void print(Graphics g) {
		
	}

}
