package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ColorCanvas implements DingCanvas {

	public static final Color DEFAULT_COLOR = Color.WHITE;
	
	private Image image;
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
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		dirty = true;
	}
	
	@Override
	public Image paintImage() {
		if(color == null || image == null)
			return null;
		
		if(dirty) {
			Graphics g = image.getGraphics();
			g.setColor(color);
			g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
			dirty = false;
		}
		return image;
	}
	
}
