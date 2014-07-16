package org.cytoscape.ding.internal.gradients.radial;

import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.CENTER;
import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.RADIUS;

import java.awt.geom.Point2D;

import org.cytoscape.ding.internal.gradients.AbstractGradientEditor;

public class RadialGradientEditor extends AbstractGradientEditor<RadialGradient> {
	
	private static final long serialVersionUID = 5997072753907737888L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialGradientEditor(final RadialGradient gradient) {
		super(gradient);
		
		// TODO
		final float radius = gradient.get(RADIUS, Float.class, 1.0f);
		final Point2D center = gradient.get(CENTER, Point2D.class, new Point2D.Float(radius/2, radius/2));
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		// TODO Auto-generated method stub
	}
}
