package org.cytoscape.ding.impl.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class ColorCanvas<GP extends GraphicsProvider> extends DingCanvas<GP> {

	public static final Color DEFAULT_COLOR = Color.WHITE;
	public static final Color TRANSPARENT_COLOR = new Color(0,0,0,0);
	
	private Color color;
	private boolean dirty = true;
	
	public ColorCanvas(GP g, Color color) {
		super(g);
		setColor(color);
		g.getTransform().addTransformChangeListener(() -> {
			dirty = true;
		});
	}
	
	
	public void setColor(Color color) {
		if(Objects.equals(this.color, color))
			return;
		this.color = color;
		dirty = true;
	}
	
	public Color getColor() {
		return color;
	}

	@Override
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		if(pm.isCancelled())
			return;
		
		if(dirty) {
			if(color == null)
				clear();
			else
				fill();
			
			dirty = false;
		}
		
		pm.done();
	}

	
	private void clear() {
		NetworkTransform t = graphicsProvider.getTransform();
		Graphics2D g = graphicsProvider.getGraphics();
		g.setBackground(TRANSPARENT_COLOR);
		g.clearRect(0, 0, t.getWidth(), t.getHeight());
	}
	
	private void fill() {
		NetworkTransform t = graphicsProvider.getTransform();
		Graphics2D g = graphicsProvider.getGraphics();
		if(g != null) {
			g.setColor(color);
			g.fillRect(0, 0, t.getWidth(), t.getHeight());
		}
	}
}
