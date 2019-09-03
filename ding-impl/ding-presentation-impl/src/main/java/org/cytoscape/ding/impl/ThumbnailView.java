package org.cytoscape.ding.impl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.Objects;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;

@SuppressWarnings("serial")
public class ThumbnailView extends JComponent implements RenderingEngine<CyNetwork>, ThumbnailChangeListener {
	
	private final DRenderingEngine re;
	private Image thumbnail;
	
	public ThumbnailView(DRenderingEngine re) {
		this.re = Objects.requireNonNull(re);
		re.addThumbnailChangeListener(this);
	}
	
	@Override
	public void thumbnailChanged(Image image) {
		thumbnail = scaleAndClip(image, getWidth(), getHeight());
		repaint();
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if(width != getWidth() || height != getHeight()) {
			super.setBounds(x, y, width, height);
			var image = re.getImage();
			thumbnail = scaleAndClip(image, width, height);
		}
	}
	
	@Override
	public void dispose() {
		re.removeThumbnailChangeListener(this);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if(thumbnail != null) {
			g.drawImage(thumbnail, 0, 0, null);
		}
	}
	
	private static Image scaleAndClip(Image image, int w, int h) {
		if(w <= 0 || h <= 0)
			return null;
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
