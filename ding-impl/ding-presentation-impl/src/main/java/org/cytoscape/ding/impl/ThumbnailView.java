package org.cytoscape.ding.impl;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.Properties;

import javax.swing.Icon;

import org.cytoscape.ding.impl.DGraphView.Canvas;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;

@SuppressWarnings("serial")
public class ThumbnailView extends Component implements RenderingEngine<CyNetwork> {

	
	private final DGraphView viewModel;
	
	private ContentChangeListener contentListener;
	private ViewportChangeListener viewportListener;
	
	
	public ThumbnailView(final DGraphView viewModel) {
		if (viewModel == null)
			throw new NullPointerException("DGraphView is null.");

		this.viewModel = viewModel;
		
		contentListener  = this::repaint;
		viewportListener = (w, h, xCenter, yCenter, scale) -> repaint();
		
		viewModel.addContentChangeListener(contentListener);
		viewModel.addViewportChangeListener(viewportListener);
	}
	
	
	@Override
	public void dispose() {
		viewModel.removeContentChangeListener(contentListener);
		viewModel.removeViewportChangeListener(viewportListener);
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
			long start = System.currentTimeMillis();
			
			viewModel.m_networkCanvas.ensureInitialized();
	
			int w = getWidth();
			int h = getHeight();
			
			DingCanvas bgCanvas  = viewModel.getCanvas(Canvas.BACKGROUND_CANVAS);
			DingCanvas netCanvas = viewModel.getCanvas(Canvas.NETWORK_CANVAS);
			DingCanvas fgCanvas  = viewModel.getCanvas(Canvas.FOREGROUND_CANVAS);
			
			Image bgImage  = bgCanvas.getImage();
			Image netImage = netCanvas.getImage();
			Image fgImage  = fgCanvas.getImage();
			
			if(bgImage != null && netImage != null && fgImage != null) {
				// Fast path
				System.out.println("fast path");
				// Take the pre-rendered image directly from the canvas and scale it for the thumbnail
				drawThumbnailLayer(g, w, h, bgImage);
				drawThumbnailLayer(g, w, h, netImage);
				drawThumbnailLayer(g, w, h, fgImage);
			}
			else {
				// Slow path
				System.out.println("slow path");
				// If the view hasn't been rendered yet then the cached images won't be available (can happen on session load).
				// In this case we need to force it to render the thumbnail.
				
				// assume the canvases are the same size
				int vw = netCanvas.getWidth();
				int vh = netCanvas.getHeight();
				
				BufferedImage bufferedImage = new BufferedImage(vw, vh, BufferedImage.TYPE_INT_ARGB);
				Graphics g2 = bufferedImage.getGraphics();
				
				// Painting will cause the images to be cached, so next time the fast path will run.
				bgCanvas.paint(g2);
				netCanvas.paint(g2);
				fgCanvas.paint(g2);
				g2.dispose();
				
				drawThumbnailLayer(g, w, h, bufferedImage);
			}
			
			long time = System.currentTimeMillis() - start;
			System.out.println("ThumbnailView.update() done: " + time);
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

		// Scale
		BufferedImage resized = new BufferedImage(svw, svh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, svw, svh, 0, 0, vw, vh, null);
		g.dispose();

		// Clip
		Image thumbnail = resized.getSubimage((svw - w) / 2, (svh - h) / 2, w, h);
		
		return thumbnail;
	}
	

	
	@Override
	public View<CyNetwork> getViewModel() {
		return viewModel.getViewModel();
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return viewModel.getVisualLexicon();
	}

	@Override
	public Properties getProperties() {
		return viewModel.getProperties();
	}

	@Override
	public Printable createPrintable() {
		return viewModel.createPrintable();
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return viewModel.createIcon(vp, value, width, height);
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
