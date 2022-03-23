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
	
	private double dpiScaleFactor = 1.0;
	
	
	public ThumbnailView(DRenderingEngine re) {
		this.re = Objects.requireNonNull(re);
		re.addThumbnailChangeListener(this);
	}
	
	@Override
	public void thumbnailChanged(Image image) {
		if(image == null) {
			thumbnail = null;
		} else {
			int thumbWidth  = (int)(getWidth()  * dpiScaleFactor);
			int thumbHeight = (int)(getHeight() * dpiScaleFactor);
			thumbnail = scaleAndClip(image, thumbWidth, thumbHeight);
		}
		repaint();
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if(width != getWidth() || height != getHeight()) {
			super.setBounds(x, y, width, height);
			thumbnail = null;
		}
	}
	
	@Override
	public void dispose() {
		re.removeThumbnailChangeListener(this);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		double scaleX;
		if(re.getGraphLOD().isHidpiEnabled()) {
			var config = ((Graphics2D)g).getDeviceConfiguration();
			var trans = config.getDefaultTransform();
			scaleX = trans.getScaleX();
		} else {
			scaleX = 1.0;
		}

		// This typically only happens if the user drags the cytoscape window from one monitor to another.
		if(scaleX != dpiScaleFactor) {
			this.dpiScaleFactor = scaleX;
			thumbnail = null;
		}

		if(thumbnail == null) {
			Image image = re.getImage();
			int thumbWidth  = (int)(getWidth()  * dpiScaleFactor);
			int thumbHeight = (int)(getHeight() * dpiScaleFactor);
			thumbnail = scaleAndClip(image, thumbWidth, thumbHeight);
		}
		
		int w = (int)(thumbnail.getWidth(null)  / dpiScaleFactor);
		int h = (int)(thumbnail.getHeight(null) / dpiScaleFactor);

		g.drawImage(thumbnail, 0, 0, w, h, null);
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
