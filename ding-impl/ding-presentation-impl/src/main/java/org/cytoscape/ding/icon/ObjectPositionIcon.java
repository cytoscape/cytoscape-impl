package org.cytoscape.ding.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.cytoscape.ding.ObjectPlacerGraphic;
import org.cytoscape.ding.ObjectPosition;

public class ObjectPositionIcon extends VisualPropertyIcon<ObjectPosition> {
	
	private static final long serialVersionUID = 6852491198236306710L;
	
	private Graphics2D g2d;
	private static final float ARC_RATIO = 0.35f;
	
	private static final Color BACK = new Color(0x1f, 0x1f, 0x1f, 150);

	public ObjectPositionIcon(final ObjectPosition value, int width, int height, String name) {
		super(value, width, height, name);
	}


	@Override public void paintIcon(Component c, Graphics g, int x, int y) {
		
		g2d = (Graphics2D) g;
		// AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Transform to fit to the component
		g2d.translate((x+leftPad), y);
		Float arc = width * ARC_RATIO;
		g2d.setColor(BACK);
		g2d.fillRoundRect(0, 0, width, height, arc.intValue(), arc.intValue());
		final ObjectPlacerGraphic lp = new ObjectPlacerGraphic(width, false, "Label");
		lp.setObjectPosition(value);
		lp.applyPosition();
		lp.paint(g2d);
		
		
		// Transform to fit to the component
		g2d.translate(-(x+leftPad), -y);
	}
}
