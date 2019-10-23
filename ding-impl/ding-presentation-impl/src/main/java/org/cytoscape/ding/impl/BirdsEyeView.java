package org.cytoscape.ding.impl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.print.Printable;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.canvas.BirdsEyeViewRenderComponent;
import org.cytoscape.ding.impl.canvas.RenderComponent;
import org.cytoscape.ding.internal.util.CoalesceTimer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public final class BirdsEyeView implements RenderingEngine<CyNetwork>, ContentChangeListener {

	private static final double SCALE_FACTOR = 0.97;
	
	private final double[] extents = new double[4];
	
	private DRenderingEngine re;
	private final RenderComponent renderComponent;
	
	private final CoalesceTimer contentChangedTimer;
	
	public BirdsEyeView(DRenderingEngine re, CyServiceRegistrar registrar) {
		this.re = re;
		
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
		
		contentChangedTimer = new CoalesceTimer(200);
		
		renderComponent.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// make sure the view is initialized properly, but we have 
				// to wait for setBounds() to be called to know the viewport size
				fitCanvasToNetwork();
				e.getComponent().removeComponentListener(this);
			}
		});
	}	
	

	@Override
	public void contentChanged() {
		renderComponent.updateView(UpdateType.ALL_FAST);
		contentChangedTimer.debounce(() -> {
			fitCanvasToNetwork();
			renderComponent.setBackgroundPaint(re.getBackgroundColor());
			renderComponent.updateView(UpdateType.ALL_FULL);
		});
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
	
	
	private void fitCanvasToNetwork() {
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
		
		renderComponent.setCenter(myXCenter, myYCenter);
		renderComponent.setScaleFactor(myScaleFactor);
	}
	
	
	private final class InnerMouseListener extends MouseAdapter {

		private int currMouseButton = 0;
		private int lastXMousePos = 0;
		private int lastYMousePos = 0;
		
		@Override public void mousePressed(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1) {
				currMouseButton = 1;
				lastXMousePos = e.getX(); // needed by drag listener
				lastYMousePos = e.getY();
				
				double myXCenter = renderComponent.getTransform().getCenterX();
				double myYCenter = renderComponent.getTransform().getCenterY();
				double myScaleFactor = renderComponent.getTransform().getScaleFactor();
				
				double halfWidth  = (double)renderComponent.getWidth()  / 2.0d;
				double halfHeight = (double)renderComponent.getHeight() / 2.0d;
				
				double centerX = ((lastXMousePos - halfWidth)  / myScaleFactor) + myXCenter;
				double centerY = ((lastYMousePos - halfHeight) / myScaleFactor) + myYCenter;
				
				re.setCenter(centerX, centerY);
				re.updateView(UpdateType.ALL_FULL);
			}
		}

		@Override 
		public void mouseReleased(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1) {
				if(currMouseButton == 1) {
					currMouseButton = 0;
				}
			}
			re.updateView(UpdateType.ALL_FULL);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(currMouseButton == 1) {
				int currX = e.getX();
				int currY = e.getY();
				double deltaX = 0;
				double deltaY = 0;
				
				double myScaleFactor = renderComponent.getTransform().getScaleFactor();
				
				if (!re.getViewModel().isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)) {
					deltaX = (currX - lastXMousePos) / myScaleFactor;
					lastXMousePos = currX;
				}
				if (!re.getViewModel().isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
					deltaY = (currY - lastYMousePos) / myScaleFactor;
					lastYMousePos = currY;
				}
				if (deltaX != 0 || deltaY != 0) {
					re.pan(deltaX, deltaY);
					re.updateView(UpdateType.ALL_FAST);
				}
			}
		}
		
		@Override 
		public void mouseWheelMoved(MouseWheelEvent e) {
			re.getInputHandlerGlassPane().processMouseWheelEvent(e);
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
	public Properties getProperties() {
		return re.getProperties();
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
		re = null;
	}

	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}
}
