package org.cytoscape.ding.internal.util;

import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Position;

public class CustomGraphicsPositionCalculator {
	
	/** Defines displacement. */
	private static final Map<Position, Float[]> DISPLACEMENT_MAP;

	static {
		DISPLACEMENT_MAP = new HashMap<>();

		DISPLACEMENT_MAP.put(Position.CENTER, new Float[] { 0f, 0f });

		DISPLACEMENT_MAP.put(Position.NORTH, new Float[] { 0f, -0.5f });
		DISPLACEMENT_MAP.put(Position.NORTH_WEST, new Float[] { -0.5f, -0.5f });
		DISPLACEMENT_MAP.put(Position.NORTH_EAST, new Float[] { 0.5f, -0.5f });

		DISPLACEMENT_MAP.put(Position.SOUTH, new Float[] { 0f, 0.5f });
		DISPLACEMENT_MAP.put(Position.SOUTH_WEST, new Float[] { -0.5f, 0.5f });
		DISPLACEMENT_MAP.put(Position.SOUTH_EAST, new Float[] { 0.5f, 0.5f });

		DISPLACEMENT_MAP.put(Position.WEST, new Float[] { -0.5f, 0f });

		DISPLACEMENT_MAP.put(Position.EAST, new Float[] { 0.5f, 0f });
	}

	/**
	 * Creates new custom graphics in new location
	 */
	public static CustomGraphicLayer transform(
			ObjectPosition p,
			double width,
			double height,
			CustomGraphicLayer layer
	) {
		Position anc = p.getAnchor();
		Position ancN = p.getTargetAnchor();

		var bounds = layer.getBounds2D();
		double cgW = bounds.getWidth();
		double cgH = bounds.getHeight();

		var disp1 = DISPLACEMENT_MAP.get(anc);
		var disp2 = DISPLACEMENT_MAP.get(ancN);

		// 1. Displacement for graphics
		double dispX = -disp1[0] * width;
		double dispY = -disp1[1] * height;

		double dispNX = disp2[0] * cgW;
		double dispNY = disp2[1] * cgH;

		// calc total and apply transform
		double totalDispX = dispX + dispNX + p.getOffsetX();
		double totalDispY = dispY + dispNY + p.getOffsetY();

		var tf = AffineTransform.getTranslateInstance(totalDispX, totalDispY);

		return layer.transform(tf);
	}
}
