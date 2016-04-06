package org.cytoscape.internal.view;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;

public class ThumbnailCache {
	
	private final int maxImageSize;
	
	private final Map<CyNetworkView, CompletableFuture<Image>> cache = new HashMap<>();
	
	private final Executor executor;
	
	
	public ThumbnailCache(int maxImageSize) {
		this.maxImageSize = maxImageSize;
		// TODO maybe pass in the executor?
		this.executor = Executors.newCachedThreadPool();
	}
	
	
	public CompletableFuture<Image> getThumbnailAsync(RenderingEngine<CyNetwork> engine, CyNetworkView netView, double w, double h) {
		// try to get the big thumbnail from the cache
		CompletableFuture<Image> imageFuture;
		synchronized(this) {
			imageFuture = cache.get(netView);
			if(imageFuture == null) {
				// start drawing the big thumbnail on a separate thread
				imageFuture = CompletableFuture.supplyAsync(() -> createReferenceThumbnail(engine, netView), executor);
				cache.put(netView, imageFuture);
			}
		}
		
		// now scale and clip down to the requested size
		imageFuture = imageFuture.thenApplyAsync(image -> scaleAndClip(image, netView, w, h), executor);
		
		// Uncomment to add a random delay for testing
//		imageFuture = imageFuture.thenApplyAsync(image -> {
//			try {
//				int min = 500, max = 5000;
//				int delay = min + (int)(Math.random() * (max - min + 1));
//				Thread.sleep(delay);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return image;
//		}, executor);
		
		return imageFuture;
	}
	
	
	public synchronized void invalidate(CyNetworkView netView) {
		cache.remove(netView);
	}
	
	
	public Executor getExecutor() {
		return executor;
	}
	
	/**
	 * Create a big thumbnail that will be scaled down later.
	 * Potentially long running.
	 */
	private Image createReferenceThumbnail(RenderingEngine<CyNetwork> engine, CyNetworkView netView) {
        BufferedImage image = null;
        
		// Fit network view image to available rectangle area
		final double vw = netView.getVisualProperty(NETWORK_WIDTH);
		final double vh = netView.getVisualProperty(NETWORK_HEIGHT);
		
		if (vw > 0 && vh > 0) {
			final double viewRatio = vh / vw;
            final double scale = viewRatio > 1.0 ? maxImageSize / vw  :  maxImageSize / vh;
			
            // Create scaled view image that is big enough to be clipped later
            final int svw = (int) Math.round(vw * scale);
            final int svh = (int) Math.round(vh * scale);
            
            if (svw > 0 && svh > 0) {
	            image = new BufferedImage(svw, svh, BufferedImage.TYPE_INT_ARGB);
	            
				final Graphics2D g = (Graphics2D) image.getGraphics();
				g.scale(scale, scale);
				engine.printCanvas(g); // very slow!
				g.dispose();
            }
		}
		
		if (image == null) {
			image = new BufferedImage(maxImageSize, maxImageSize, BufferedImage.TYPE_INT_RGB);
			
			final Graphics2D g2 = image.createGraphics();
			final Paint bg = netView.getVisualProperty(NETWORK_BACKGROUND_PAINT);
			g2.setPaint(bg);
			g2.drawRect(1, 1, maxImageSize, maxImageSize);
			g2.dispose();
		}
        
		return image;
	}
	
	
	private Image scaleAndClip(Image image, CyNetworkView netView, double w, double h) {
		final int vw = image.getWidth(null);
		final int vh = image.getHeight(null);
		
		final double rectRatio = h / w;
		final double viewRatio = vh / vw;
        final double scale = viewRatio > rectRatio ? w / vw  :  h / vh;
		
        final int svw = (int) Math.round(vw * scale);
        final int svh = (int) Math.round(vh * scale);
        
        // Scale
        BufferedImage resized = new BufferedImage(svw, svh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, svw, svh, 0, 0, vw, vh, null);
        g.dispose();
        
        // Clip
        final int iw = (int) Math.round(w);
        final int ih = (int) Math.round(h);
		Image thumbnail = resized.getSubimage((svw - iw) / 2, (svh - ih) / 2, iw, ih);
		return thumbnail;
	}
	

}

