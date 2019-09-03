package org.cytoscape.ding.impl.cyannotator.annotations;

import org.cytoscape.view.presentation.property.values.Position;

public class AnchorLocation {

	private final Position position;
	
	// Location of the top corner of the anchor in image coordinates
	private final int x;
	private final int y;
	
	// Distance from the top corner to the location the mouse was clicked (usually just a few pixels)
	private final int mouseOffsetX;
	private final int mouseOffsetY;
	
	
	public AnchorLocation(Position position, int x, int y, int mouseOffsetX, int mouseOffsetY) {
		this.position = position;
		this.x = x;
		this.y = y;
		this.mouseOffsetX = mouseOffsetX;
		this.mouseOffsetY = mouseOffsetY;
	}
	
	
	public Position getPosition() {
		return position;
	}
	
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public int getMouseOffsetX() {
		return mouseOffsetX;
	}

	public int getMouseOffsetY() {
		return mouseOffsetY;
	}
	
}
