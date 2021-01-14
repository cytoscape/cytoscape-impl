package org.cytoscape.ding.impl.canvas;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Paint;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.debug.DebugFrameType;
import org.cytoscape.ding.debug.DebugProgressMonitorFactory;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.TransformChangeListener;
import org.cytoscape.ding.impl.canvas.CompositeImageCanvas.PaintParameters;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

/**
 * This is the interface between the renderer and Swing.
 */
@SuppressWarnings("serial")
public abstract class RenderComponent extends JComponent {
	
	protected DRenderingEngine re;
	
	protected CompositeImageCanvas fastCanvas; // treat this as the 'main' canvas
	protected CompositeImageCanvas slowCanvas;
	
	private ImageFuture slowFuture;
	private ImageFuture fastFuture;
	private UpdateType updateType = UpdateType.ALL_FULL;
	
	private RenderDetailFlags lastFastRenderFlags;
	private boolean initialized = false;
	
	private Runnable initializedCallback;
	
	private NetworkTransform.Snapshot slowCanvasLastPaintSnapshot;
	private NetworkTransform.Snapshot fastCanvasPanStartedSnapshot;
	
	
	public RenderComponent(DRenderingEngine re, GraphLOD lod) {
		this.re = re;
		
		// MKTODO Using a size of 1 is a hack. We don't know what the size of the buffer should be until setBounds() is called.
		// Unfortunately its possible for fitContent() to be called before setBounds() is called.
		slowCanvas = new CompositeImageCanvas(re, lod, 1, 1);
		fastCanvas = new CompositeImageCanvas(re, lod.faster(), 1, 1);
	}
	
	
	abstract ProgressMonitor getSlowProgressMonitor();
	abstract DebugFrameType getDebugFrameType(UpdateType type);
	
	
	public void startPan() {
		fastCanvasPanStartedSnapshot = getTransform().snapshot();
	}
	
	private void takeSlowPaintSnapshot() {
		slowCanvasLastPaintSnapshot = getTransform().snapshot();
	}
	
	public void endPan() {
		slowCanvasLastPaintSnapshot = null;
		fastCanvasPanStartedSnapshot = null;
	}
	
	
	
	public void setInitializedCallback(Runnable callback) {
		this.initializedCallback = callback;
	}
	
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if(width == getWidth() && height == getHeight()) {
			return;
		}
		super.setBounds(x, y, width, height);
		fastCanvas.setViewport(width, height);
		slowCanvas.setViewport(width, height);
		
		if(!initialized) {
			initialized = true;
			if(initializedCallback != null) {
				initializedCallback.run();
			}
		}
		
		// Avoid deadlock if an App calls pack() or something similar from a Task.
		if(SwingUtilities.isEventDispatchThread()) {
			updateView(UpdateType.ALL_FULL);
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public RenderDetailFlags getLastFastRenderFlags() {
		return lastFastRenderFlags;
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
				future = fastCanvas.paint(null);
			}
			image[0] = future.join(); 
		});
		return image[0];
	}
	
	private PaintParameters getFastCanvasPaintParams() {
		if((updateType == UpdateType.ALL_FAST || updateType == UpdateType.ALL_FULL) 
				&& fastCanvasPanStartedSnapshot != null && Objects.equals(slowCanvasLastPaintSnapshot, fastCanvasPanStartedSnapshot)) {
			double[] coords = new double[2];
			
			// compute the distance the canvas needs to be panned in image coords
			coords[0] = fastCanvasPanStartedSnapshot.x;
			coords[1] = fastCanvasPanStartedSnapshot.y;
			fastCanvas.getTransform().xformNodeToImageCoords(coords);
			double oldX = coords[0];
			double oldY = coords[1];
			
			coords[0] = fastCanvas.getTransform().getCenterX();
			coords[1] = fastCanvas.getTransform().getCenterY();
			fastCanvas.getTransform().xformNodeToImageCoords(coords);
			double newX = coords[0];
			double newY = coords[1];
			
			var dx = (int) (oldX - newX);
			var dy = (int) (oldY - newY);
			
			return PaintParameters.pan(dx, dy, slowCanvas);
		} else {
			return PaintParameters.updateType(updateType);
		}
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		ImageFuture future;
		
		if(slowFuture != null && slowFuture.isReady()) {
			future = slowFuture;
		} else if(fastFuture != null && fastFuture.isReady()) {
			future = fastFuture;
		} else {
			if(slowFuture != null) {
				slowFuture.cancel();
				slowFuture.join(); // make sure its cancelled
				slowFuture = null;
			}
			
			// Paint the fast canvas synchronously
			PaintParameters paintParams = getFastCanvasPaintParams();
			fastFuture = fastCanvas.paint(debugPm(UpdateType.ALL_FAST), paintParams);
			fastFuture.join();
			lastFastRenderFlags = fastFuture.getLastRenderDetail();
			future = fastFuture;

			// start a slow frame if necessary
			if(updateType == UpdateType.ALL_FULL && !sameDetail()) { 
				var slowPm = debugPm(UpdateType.ALL_FULL, getSlowProgressMonitor());
				slowFuture = slowCanvas.paint(slowPm);
				slowFuture.thenRun(() -> {
					takeSlowPaintSnapshot();
					repaint();
				});
			}
			updateType = UpdateType.ALL_FAST;
		}
		
		Image image = future.join();
		setRenderDetailFlags(future.getLastRenderDetail());
		g.drawImage(image, 0, 0, null);
	}
	
	
	protected void setRenderDetailFlags(RenderDetailFlags flags) {
	}
	
	private ProgressMonitor debugPm(UpdateType updateType) {
		return debugPm(updateType, null);
	}
	
	private ProgressMonitor debugPm(UpdateType updateType, ProgressMonitor pm) {
		DebugProgressMonitorFactory factory = re.getDebugProgressMonitorFactory();
		if(factory != null) {
			var debugType = getDebugFrameType(updateType);
			return factory.create(debugType, pm);
		}
		return pm;
	}
	
	private boolean sameDetail() {
		return fastFuture.getLastRenderDetail().equals(slowCanvas.getRenderDetailFlags());
	}
	
	public void dispose() {
		fastCanvas.dispose();
		slowCanvas.dispose();
		re = null;
	}

	public void showAnnotationSelection(boolean show) {
		fastCanvas.showAnnotationSelection(show);
		slowCanvas.showAnnotationSelection(show);
	}
	
}