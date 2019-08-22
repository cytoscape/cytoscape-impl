package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Image;
import java.util.Objects;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class ColorCanvas extends DingCanvas {

	public static final Color DEFAULT_COLOR = Color.WHITE;
	
	private Color color;
	private boolean dirty = true;
	
	public ColorCanvas(Color color, int width, int height) {
		super(width, height);
		setColor(color);
	}
	
	public ColorCanvas(int width, int height) {
		this(null, width, height);
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
	public Image paintImage(ProgressMonitor pm, RenderDetailFlags flags) {
		if(pm.isCancelled())
			return null;
		
		if(dirty) {
			if(color != null) {
				image.fill(color);
			} else {
				image.clear();
			}
			dirty = false;
		}
		
		return image.getImage();
	}
	
}
