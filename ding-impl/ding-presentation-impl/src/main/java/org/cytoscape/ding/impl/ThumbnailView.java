package org.cytoscape.ding.impl;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.Objects;
import java.util.Properties;

import javax.swing.Icon;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

@SuppressWarnings("serial")
public class ThumbnailView extends Component implements RenderingEngine<CyNetwork>, ViewChangedListener, RowsSetListener {
	
	private final DRenderingEngine re;
	private final CyServiceRegistrar registrar;
	
	private ContentChangeListener contentListener;
	private ViewportChangeListener viewportListener;
	
	private boolean forceRender = false;
	
	
	public ThumbnailView(DRenderingEngine re, CyServiceRegistrar registrar) {
		this.re = Objects.requireNonNull(re);
		this.registrar = Objects.requireNonNull(registrar);
		
		contentListener  = () -> {
			forceRender = false;
			repaint();
		};
		viewportListener = (w, h, xCenter, yCenter, scale) -> {
			forceRender = false;
			repaint();
		};
		
		re.addContentChangeListener(contentListener);
		re.addViewportChangeListener(viewportListener);
	}
	
	@Override
	public void handleEvent(ViewChangedEvent<?> e) {
		// Ensure that the thumbnail updates when a visual property changes,
		// because the ContentChangeListener won't fire if the network view isn't visible.
		CyNetworkView source = e.getSource();
		if(source.equals(re.getViewModel())) {
			if(!re.getCanvas().isShowing() && hasNonResizeEvent(e)) {
				forceRender = true;
			}
			repaint();
		}
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {	
		if(!e.containsColumn(CyNetwork.SELECTED))
			return;
		
		CyTable source = e.getSource();
		CyNetwork model = re.getViewModel().getModel();
		if(!re.getCanvas().isShowing() && (source == model.getDefaultNodeTable() || source == model.getDefaultEdgeTable())) {
			forceRender = true;
			repaint();
		}
	}
	
	void registerServices() {
		registrar.registerAllServices(this, new Properties());
	}
	
	@Override
	public void dispose() {
		re.removeContentChangeListener(contentListener);
		re.removeViewportChangeListener(viewportListener);
		registrar.unregisterAllServices(this);
	}
		
	
	@Override
	public void paint(Graphics g) {
		update(g);
	}
	
	/**
	 * Render actual image on the panel.
	 */
	@Override 
	public void update(Graphics g) {
		if(isVisible()) {
			//long start = System.currentTimeMillis();
			
			re.getCanvas().ensureInitialized();
	
			int w = getWidth();
			int h = getHeight();
			
			DingCanvas bgCanvas  = re.getCanvas(DRenderingEngine.Canvas.BACKGROUND_CANVAS);
			DingCanvas netCanvas = re.getCanvas(DRenderingEngine.Canvas.NETWORK_CANVAS);
			DingCanvas fgCanvas  = re.getCanvas(DRenderingEngine.Canvas.FOREGROUND_CANVAS);
			
			Image bgImage  = bgCanvas.getImage();
			Image netImage = netCanvas.getImage();
			Image fgImage  = fgCanvas.getImage();
			
			if(forceRender || bgImage == null || netImage == null || fgImage == null) {
				// Slow path
				// If the view hasn't been rendered yet then the cached images won't be available (can happen on session load).
				// If a visual property has changed but the view hasn't rendered yet then the images are stale.
				// In these cases we need to force a render of the thumbnail.
				
				int vw = netCanvas.getWidth();  // assume the canvases are the same size
				int vh = netCanvas.getHeight();
				
				BufferedImage bufferedImage = new BufferedImage(vw, vh, BufferedImage.TYPE_INT_ARGB);
				Graphics g2 = bufferedImage.getGraphics();
				
				// Painting will cause the images to be cached, so next time the fast path should run.
				bgCanvas.paint(g2);
				netCanvas.paint(g2);
				fgCanvas.paint(g2);
				g2.dispose();
				
				drawThumbnailLayer(g, w, h, bufferedImage);
				forceRender = false;
			}
			else {
				// Fast path, take the pre-rendered cached images and scale them.
				drawThumbnailLayer(g, w, h, bgImage);
				drawThumbnailLayer(g, w, h, netImage);
				drawThumbnailLayer(g, w, h, fgImage);
			}
			
			//long time = System.currentTimeMillis() - start;
			//System.out.println("ThumbnailView.update() done: " + time);
		}
	}
	
	
	private static void drawThumbnailLayer(Graphics g, int w, int h, Image image) {
		if(image != null) {
			Image canvasThumb = scaleAndClip(image, w, h);
			g.drawImage(canvasThumb, 0, 0, null);
		} 
	}
	
	
	private static Image scaleAndClip(Image image, int w, int h) {
		final int vw = image.getWidth(null);
		final int vh = image.getHeight(null);
		
		final double rectRatio = (double)h / (double)w;
		final double viewRatio = (double)vh / (double)vw;
		final double scale = viewRatio > rectRatio ? (double) w / (double) vw : (double) h / (double) vh;

		final int svw = (int) Math.round(vw * scale);
		final int svh = (int) Math.round(vh * scale);

		// scale
		BufferedImage resized = new BufferedImage(svw, svh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, svw, svh, 0, 0, vw, vh, null);
		g.dispose();

		// clip
		Image thumbnail = resized.getSubimage((svw - w) / 2, (svh - h) / 2, w, h);
		
		return thumbnail;
	}
	
	
	private static boolean hasNonResizeEvent(final ViewChangedEvent<?> e) {
		for(ViewChangeRecord<?> record : e.getPayloadCollection()) {
			VisualProperty<?> vp = record.getVisualProperty();
			if(!BasicVisualLexicon.NETWORK_WIDTH.equals(vp) && !BasicVisualLexicon.NETWORK_HEIGHT.equals(vp)) {
				return true;
			}
		}
		return false;
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
		throw new UnsupportedOperationException("Printing is not supported for Thumbnail view.");
	}

	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}

}
