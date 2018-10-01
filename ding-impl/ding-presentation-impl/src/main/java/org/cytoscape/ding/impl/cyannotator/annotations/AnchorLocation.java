package org.cytoscape.ding.impl.cyannotator.annotations;

import org.cytoscape.view.presentation.property.values.Position;

public class AnchorLocation {

	private final Position position;
	private final double x;
	private final double y;
	
	public AnchorLocation(Position position, double x, double y) {
		this.position = position;
		this.x = x;
		this.y = y;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	
}
