package org.cytoscape.ding.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.cytoscape.ding.ObjectPlacerGraphic;
import org.cytoscape.ding.ObjectPosition;

public class ObjectPositionIcon extends VisualPropertyIcon<ObjectPosition> {
	
	private static final long serialVersionUID = 6852491198236306710L;
	
	private Graphics2D g2d;

	public ObjectPositionIcon(final ObjectPosition value, int width, int height, String name) {
		super(value, width, height, name);
	}

	
	
	@Override public void paintIcon(Component c, Graphics g, int x, int y) {
		
		g2d = (Graphics2D) g;
		// AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		final ObjectPlacerGraphic lp = new ObjectPlacerGraphic(width, false, "Label");
		lp.setObjectPosition(value);
		lp.applyPosition();
		lp.paint(g2d);
		
	}
}
