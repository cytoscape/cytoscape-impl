package org.cytoscape.ding.customgraphics.vector;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractDCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsPropertyImpl;
import org.cytoscape.graph.render.stateful.PaintFactory;

/**
 * Proof of concept code to generate Custom Graphics dynamically as vector graphics.
 * 
 */
public abstract class GradientLayerCustomGraphics extends AbstractDCustomGraphics implements VectorCustomGraphics {

	// Paint fot this graphics
	protected PaintFactory paintFactory;
	
	// Bound of this graphics
	protected Shape bound;
	
	private static final float FIT = 0.9f;
	
	protected static final String COLOR1 = "Color 1";
	protected static final String COLOR2 = "Color 2";
	
	protected final CustomGraphicsProperty<Color> c1;
	protected final CustomGraphicsProperty<Color> c2;
	
	// Pre-Rendered image for icon.
	protected BufferedImage rendered;
	
	private static final Color transparentWhite = new Color(255, 255, 255, 100);
	private static final Color transparentBlack = new Color(100, 100, 100, 100);
	
	private static final int DEF_W = 100;
	private static final int DEF_H = 100;
	
	protected final Map<String, CustomGraphicsProperty<?>> props;

	
	public GradientLayerCustomGraphics(final Long id, final String name) {
		super(id, name);
		width = DEF_W;
		height = DEF_H;
		props = new HashMap<String, CustomGraphicsProperty<?>>();

		c1 = new CustomGraphicsPropertyImpl<Color>(transparentWhite);
		c2 = new CustomGraphicsPropertyImpl<Color>(transparentBlack);
		
		this.props.put(COLOR1, c1);
		this.props.put(COLOR2, c2);
		this.tags.add("vector image, gradient");
		this.fitRatio = FIT;
		
		// Render it for static icons.
		getRenderedImage();
	}

	
	public Map<String, CustomGraphicsProperty<?>> getGraphicsProps() {
		return this.props;
	}
	
	protected void renderImage(Graphics graphics) {
		rendered.flush();
		final Graphics2D g2d = (Graphics2D) graphics;
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
				RenderingHints.VALUE_RENDER_QUALITY );
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON );
	}
	

	public Image getRenderedImage() {
		if(rendered == null || (rendered != null && (rendered.getWidth() != width || rendered.getHeight() != height))) {
			rendered = new BufferedImage(width, 
				height, BufferedImage.TYPE_INT_ARGB);
			renderImage(rendered.getGraphics());
		}
		
		return rendered;
	}
}
