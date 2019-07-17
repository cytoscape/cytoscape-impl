package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Graphics;

public class ColorCanvas extends DingCanvas {

	public static final Color DEFAULT_COLOR = Color.WHITE;
	
	private Color color;
	private boolean dirty = true;
	
	public ColorCanvas(Color color) {
		this.color = color;
	}
	
	public ColorCanvas() {
		this(DEFAULT_COLOR);
	}
	
	public void setColor(Color color) {
		this.color = color;
		dirty = true;
	}

	@Override
	public void setViewport(int width, int height) {
		super.setViewport(width, height);
		dirty = true;
	}
	
	@Override
	public void paintImage() {
		if(color == null)
			return;
		
		if(dirty) {
			Graphics g = image.getGraphics();
			g.setColor(color);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			dirty = false;
		}
	}
	
}
