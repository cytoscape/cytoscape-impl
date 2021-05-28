package org.cytoscape.ding.impl.cyannotator.annotations;

import static org.cytoscape.view.presentation.property.values.Position.*;

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
	
	// static utility methods
	public static boolean isNorth(Position position) {
		return position == NORTH || position == NORTH_EAST || position == NORTH_WEST;
	}
	
	public static boolean isSouth(Position position) {
		return position == SOUTH || position == SOUTH_EAST || position == SOUTH_WEST;
	}
	
	public static boolean isWest(Position position) {
		return position == WEST || position == NORTH_WEST || position == SOUTH_WEST;
	}
	
	public static boolean isEast(Position position) {
		return position == EAST || position == NORTH_EAST || position == SOUTH_EAST;
	}
	
	public static boolean isCorner(Position position) {
		return position == NORTH_WEST || position == NORTH_EAST || position == SOUTH_WEST || position == SOUTH_EAST;
	}
	
}
