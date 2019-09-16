package org.cytoscape.ding.impl.canvas;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Paint;

import javax.swing.JComponent;

import org.cytoscape.ding.CyActivator;
import org.cytoscape.ding.debug.DebugFrameType;
import org.cytoscape.ding.debug.DebugProgressMonitor;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.TransformChangeListener;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

/**
 * This is the interface between the renderer and Swing.
 */
@SuppressWarnings("serial")
public abstract class RenderComponent extends JComponent {
	
	protected final DRenderingEngine re;
	
	private CompositeImageCanvas fastCanvas; // treat this as the 'main' canvas
	private CompositeImageCanvas slowCanvas;
	
	private ImageFuture slowFuture;
	private ImageFuture fastFuture;
	private UpdateType updateType = UpdateType.ALL_FULL;
	
	private boolean initialized = false;
	
	public RenderComponent(DRenderingEngine re, GraphLOD lod) {
		this.re = re;
		
		// MKTODO This is a hack, we don't know what the size of the buffer should be until setBounds() is called.
		// Unfortunately its possible for fitContent() to be called before setBounds() is called.
		fastCanvas = new CompositeImageCanvas(re, lod.faster(), 1, 1);
		slowCanvas = new CompositeImageCanvas(re, lod, 1, 1);
	}
	
	
	abstract ProgressMonitor getSlowProgressMonitor();
	abstract DebugFrameType getDebugFrameType(UpdateType type);
	
	
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		initialized = true;
		if(width == getWidth() && height == getHeight()) {
			return;
		}
		super.setBounds(x, y, width, height);
		fastCanvas.setViewport(width, height);
		slowCanvas.setViewport(width, height);
		
		updateView(UpdateType.ALL_FULL);
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public NetworkTransform getTransform() {
		return fastCanvas.getTransform();
	}
	
	public void addTransformChangeListener(TransformChangeListener l) {
		fastCanvas.getTransform().addTransformChangeListener(l);
	}
	
	public void removeTransformChangeListener(TransformChangeListener l) {
		fastCanvas.getTransform().removeTransformChangeListener(l);
	}
	
	public void setBackgroundPaint(Paint backgroundPaint) {
		fastCanvas.setBackgroundPaint(backgroundPaint);
		slowCanvas.setBackgroundPaint(backgroundPaint);
	}
	
	public Color getBackgroundPaint() {
		return fastCanvas.getBackgroundPaint();
	}
	
	public void setLOD(GraphLOD lod) {
		slowCanvas.setLOD(lod);
	}
	
	public void setCenter(double x, double y) {
		slowCanvas.setCenter(x, y);
		fastCanvas.setCenter(x, y);
	}
	
	public void setScaleFactor(double scaleFactor) {
		slowCanvas.setScaleFactor(scaleFactor);
		fastCanvas.setScaleFactor(scaleFactor);
	}
	
	public void updateView(UpdateType updateType) {
		// Run this on the EDT so there is no race condition with paint()
		// Fast painting and slow painting don't happen concurrently.
		invokeOnEDTAndWait(() -> {
			if(slowFuture != null) {
				slowFuture.cancel();
			}
			if(fastFuture != null) {
				fastFuture.cancel();
				fastFuture = null;
			}
			
			// don't render slow frames while panning, only render slow when user releases mouse button
			this.updateType = updateType;
			repaint();
		});
	}
	
	public Image getImage() {
		Image[] image = { null };
		invokeOnEDTAndWait(() -> {
			ImageFuture future;
			if(slowFuture != null && slowFuture.isReady()) {
				future = slowFuture;
			} else if(fastFuture != null && fastFuture.isReady()) {
				future = fastFuture;
			} else {
				future = fastCanvas.paintSync(null);
			}
			image[0] = future.join(); 
		});
		return image[0];
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		ImageFuture future;
		
		if(slowFuture != null && slowFuture.isReady()) {
			future = slowFuture;
		} else if(fastFuture != null) {
			future = fastFuture;
		} else {
			if(slowFuture != null) {
				slowFuture.cancel();
				slowFuture.join(); // make sure its cancelled
				slowFuture = null;
			}
			
			// RENDER: fast frame right now
			if(updateType == UpdateType.JUST_ANNOTATIONS) {
				var fastPm = debugPm(UpdateType.JUST_ANNOTATIONS, null);
				fastFuture = fastCanvas.paintSyncJustAnnotations(fastPm);
			} else {
				var fastPm = debugPm(UpdateType.ALL_FAST, null);
				fastFuture = fastCanvas.paintSync(fastPm);
			}
			future = fastFuture;
			updateThumbnail(fastFuture);

			// RENDER: start a slow frame if necessary
			if(updateType == UpdateType.ALL_FULL && !sameDetail()) { 
				var slowPm = debugPm(UpdateType.ALL_FULL, getSlowProgressMonitor());
				var executor = re.getSingleThreadExecutorService();
				slowFuture = slowCanvas.paintAsync(slowPm, executor);
				slowFuture.thenRun(this::repaint);
				slowFuture.thenAccept(this::updateThumbnail);
			}
			updateType = UpdateType.ALL_FAST;
		}
		
		Image image = future.join();
		setRenderDetailFlags(future.getLastRenderDetail());
		g.drawImage(image, 0, 0, null);
	}
	
	
	protected void updateThumbnail(ImageFuture slowFuture) {
	}
	
	protected void setRenderDetailFlags(RenderDetailFlags flags) {
	}
	
	private ProgressMonitor debugPm(UpdateType updateType, ProgressMonitor pm) {
		var debugType = getDebugFrameType(updateType);
		return CyActivator.DEBUG ? new DebugProgressMonitor(debugType, pm, re.getDebugCallback()) : pm;
	}
	
	private boolean sameDetail() {
		return fastFuture.getLastRenderDetail().equals(slowCanvas.getRenderDetailFlags());
	}
	
	public void dispose() {
		fastCanvas.dispose();
		slowCanvas.dispose();
	}

	public void showAnnotationSelection(boolean show) {
		fastCanvas.showAnnotationSelection(show);
		slowCanvas.showAnnotationSelection(show);
	}
	
}