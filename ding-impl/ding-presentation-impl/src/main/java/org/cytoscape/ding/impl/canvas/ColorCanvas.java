package org.cytoscape.ding.impl.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class ColorCanvas<T extends NetworkTransform> extends DingCanvas<T> {

	public static final Color DEFAULT_COLOR = Color.WHITE;
	
	private Color color;
	private boolean dirty = true;
	
	public ColorCanvas(T t, Color color) {
		super(t);
		setColor(color);
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
	public void setViewport(int width, int height) {
		super.setViewport(width, height);
		dirty = true;
	}
	
	@Override
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		if(pm.isCancelled())
			return;
		
		if(dirty) {
			if(color != null) {
				fill();
			}
			dirty = false;
		}
	}

	private void fill() {
		Graphics2D g = transform.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, transform.getWidth(), transform.getHeight());
	}
}
