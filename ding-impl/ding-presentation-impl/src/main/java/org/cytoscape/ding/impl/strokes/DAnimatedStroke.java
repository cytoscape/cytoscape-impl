package org.cytoscape.ding.impl.strokes;

public interface DAnimatedStroke extends AnimatedStroke, WidthStroke {
	
	public static float N_STEPS = 4.0f;
	
	float getWidth();
	
	@Override
	DAnimatedStroke newInstanceForNextOffset();

}
