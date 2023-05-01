package org.cytoscape.ding.impl;

import static org.cytoscape.ding.internal.util.ViewUtil.isSingleLeftClick;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.print.Printable;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.ding.impl.DRenderingEngine.Panner;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.canvas.BirdsEyeViewRenderComponent;
import org.cytoscape.ding.impl.canvas.RenderComponent;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;

public final class BirdsEyeView implements RenderingEngine<CyNetwork>, ContentChangeListener {

	private static final double SCALE_FACTOR = 0.97;
	
	private final double[] extents = new double[4];
	
	private DRenderingEngine re;
	private final CyServiceRegistrar registrar;
	private final RenderComponent renderComponent;
	
	private final DebounceTimer contentChangedTimer;
	
	public BirdsEyeView(DRenderingEngine re, CyServiceRegistrar registrar) {
		this.re = re;
		this.registrar = registrar;
		
		var lod = new BirdsEyeViewLOD(re.getGraphLOD());
		renderComponent = new BirdsEyeViewRenderComponent(re, lod);
		renderComponent.setBackgroundPaint(re.getBackgroundColor());
		renderComponent.showAnnotationSelection(false);
		
		var mouseListener = new InnerMouseListener();
		renderComponent.addMouseListener(mouseListener);
		renderComponent.addMouseMotionListener(mouseListener);
		renderComponent.addMouseWheelListener(mouseListener);
		
		re.addTransformChangeListener(renderComponent::repaint);
		re.addContentChangeListener(this);
		
		contentChangedTimer = new DebounceTimer(200);
		
		renderComponent.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// make sure the view is initialized properly, but we have 
				// to wait for setBounds() to be called to know the viewport size
				contentChanged(UpdateType.ALL_FAST);
				e.getComponent().removeComponentListener(this);
			}
		});
	}	
	

	@Override
	public void contentChanged(UpdateType updateType) {
		if(updateType == UpdateType.JUST_ANNOTATIONS) {
			renderComponent.updateView(UpdateType.JUST_ANNOTATIONS);
			return;
		}
			
		if(!contentChangedTimer.isShutdown()) {
			contentChangedTimer.debounce(() -> {
				fitCanvasToNetwork();
				renderComponent.setBackgroundPaint(re.getBackgroundColor());
				renderComponent.updateView(UpdateType.ALL_FULL);
			});
		}
	}	
	
	public JComponent getComponent() {
		return renderComponent;
	}
	
	@Override
	public Image createImage(int width, int height) {
		return null;
	}
	
	/**
	 * Returns the extents of the nodes, in node coordinates.
	 */
	private boolean getNetworkExtents(double[] extents) {
		CyNetworkViewSnapshot netViewSnapshot = re.getViewModelSnapshot();
		if(netViewSnapshot.getNodeCount() == 0) {
			Arrays.fill(extents, 0.0);
			return false;
		}
		netViewSnapshot.getSpacialIndex2D().getMBR(extents);
		return true;
	}
	
	
	private boolean fitCanvasToNetwork() {
		boolean hasComponents = getNetworkExtents(extents);
		hasComponents |= re.getCyAnnotator().adjustBoundsToIncludeAnnotations(extents);
		
		double myXCenter;
		double myYCenter;
		double myScaleFactor;
		
		if(hasComponents) {
			myXCenter = (extents[0] + extents[2]) / 2.0d;
			myYCenter = (extents[1] + extents[3]) / 2.0d;
			myScaleFactor = SCALE_FACTOR * 
			                  Math.min(((double) renderComponent.getWidth())  / (extents[2] - extents[0]), 
			                           ((double) renderComponent.getHeight()) / (extents[3] - extents[1]));
		} else {
			myXCenter = 0.0d;
			myYCenter = 0.0d;
			myScaleFactor = 1.0d;
		}
		
		
		var t = renderComponent.getTransform();
		if(t.getCenterX() == myXCenter && t.getCenterY() == myYCenter && t.getScaleFactor() == myScaleFactor) {
			// nothing to change
			return false;
		}
		
		renderComponent.setCenter(myXCenter, myYCenter);
		renderComponent.setScaleFactor(myScaleFactor);
		return true;
	}
	
	
	private final class InnerMouseListener extends MouseAdapter {
		
		private final double[] coords = new double[2];
		
		private Point mousePressedPoint;
		private Panner panner = null;
		
		@Override 
		public void mousePressed(MouseEvent e) {
			if(!isSingleLeftClick(e))
				return;
			
			mousePressedPoint = e.getPoint();
			
			// Center the network on the point that was clicked
			double myXCenter = renderComponent.getTransform().getCenterX();
			double myYCenter = renderComponent.getTransform().getCenterY();
			double myScaleFactor = renderComponent.getTransform().getScaleFactor();
			
			double halfWidth  = (double)renderComponent.getWidth()  / 2.0d;
			double halfHeight = (double)renderComponent.getHeight() / 2.0d;
			
			double centerX = ((e.getX() - halfWidth)  / myScaleFactor) + myXCenter;
			double centerY = ((e.getY() - halfHeight) / myScaleFactor) + myYCenter;
			
			re.setCenter(centerX, centerY);
			re.updateView(UpdateType.ALL_FULL);
			
			// Now start a pan operation
			panner = re.startPan();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(mousePressedPoint != null) {
				// MKTODO does holding SHIFT matter??
				coords[0] = mousePressedPoint.getX();
				coords[1] = mousePressedPoint.getY();
				renderComponent.getTransform().xformImageToNodeCoords(coords);
				double oldX = coords[0];
				double oldY = coords[1];
				
				coords[0] = e.getX();
				coords[1] = e.getY();
				renderComponent.getTransform().xformImageToNodeCoords(coords);
				double newX = coords[0];
				double newY = coords[1];
				
				mousePressedPoint = e.getPoint();
				
				double deltaX = oldX - newX;
				double deltaY = oldY - newY;
				
				panner.continuePan(-deltaX, -deltaY);
			}
		}
		
		@Override 
		public void mouseReleased(MouseEvent e) {
			mousePressedPoint = null;

			if(panner != null) { // why does this happen?
				panner.endPan();
				panner = null;
			}
		}
		
		@Override 
		public void mouseWheelMoved(MouseWheelEvent e) {
			re.zoomToCenter(e.getWheelRotation());
			re.updateView(UpdateType.ALL_FULL);
		}
	}
	
	
	@Override
	public View<CyNetwork> getViewModel() {
		return re.getViewModel();
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return re.getVisualLexicon();
	}

	@Override
	public Printable createPrintable() {
		return re.createPrintable();
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return re.createIcon(vp, value, width, height);
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
		throw new UnsupportedOperationException("Printing is not supported for Bird's eye view.");
	}

	@Override
	public void dispose() {
		renderComponent.dispose();
		contentChangedTimer.shutdown();
		re = null;
	}

	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}
}
