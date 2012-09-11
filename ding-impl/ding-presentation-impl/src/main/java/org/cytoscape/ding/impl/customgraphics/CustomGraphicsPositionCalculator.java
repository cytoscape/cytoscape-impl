package org.cytoscape.ding.impl.customgraphics;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.Position;
import org.cytoscape.ding.impl.DNodeView;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.graph.render.stateful.PaintFactory;

public class CustomGraphicsPositionCalculator {
	
	/**
	 * Defines displacement.
	 */
	private static final Map<Position, Float[]> DISPLACEMENT_MAP;
	
	static {
		DISPLACEMENT_MAP = new HashMap<Position, Float[]>();
		
		DISPLACEMENT_MAP.put(Position.CENTER, new Float[]{0f, 0f} );
		
		DISPLACEMENT_MAP.put(Position.NORTH,  new Float[]{0f, -0.5f});
		DISPLACEMENT_MAP.put(Position.NORTH_WEST, new Float[]{-0.5f, -0.5f});
		DISPLACEMENT_MAP.put(Position.NORTH_EAST, new Float[]{0.5f, -0.5f});
		
		DISPLACEMENT_MAP.put(Position.SOUTH,  new Float[]{0f, 0.5f});
		DISPLACEMENT_MAP.put(Position.SOUTH_WEST,  new Float[]{-0.5f, 0.5f});
		DISPLACEMENT_MAP.put(Position.SOUTH_EAST,  new Float[]{0.5f, 0.5f});
		
		DISPLACEMENT_MAP.put(Position.WEST,  new Float[]{-0.5f, 0f});
		
		DISPLACEMENT_MAP.put(Position.EAST,  new Float[]{0.5f, 0f});
	}
	
	
	/**
	 * Creates new custom graphics in new location
	 * 
	 * @param p
	 * @param nv
	 * @param cg
	 * @return
	 */
	public static CustomGraphicLayer transform(final ObjectPosition p, final DNodeView nv, final CustomGraphicLayer cg) {		
		final Position anc = p.getAnchor();
		final Position ancN = p.getTargetAnchor();
		
		final double nodeW = nv.getWidth();
		final double nodeH = nv.getHeight();
		final double cgW = cg.getBounds().getWidth();
		final double cgH = cg.getBounds().getHeight();
		
		final Float[] disp1 = DISPLACEMENT_MAP.get(anc);
		final Float[] disp2 = DISPLACEMENT_MAP.get(ancN);
		
		// 1. Displacement for graphics
		final double dispX = -disp1[0] * nodeW;
		final double dispY = -disp1[1] * nodeH;
		
		final double dispNX = disp2[0] * cgW;
		final double dispNY = disp2[1] * cgH;
		
		// calc total and apply transform
		double totalDispX = dispX + dispNX + p.getOffsetX();
		double totalDispY = dispY + dispNY + p.getOffsetY();
		
		final AffineTransform tf = AffineTransform.getTranslateInstance(totalDispX, totalDispY);

		return cg.transform(tf);
	}
}
