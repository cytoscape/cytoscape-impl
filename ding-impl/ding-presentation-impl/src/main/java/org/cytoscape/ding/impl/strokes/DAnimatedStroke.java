package org.cytoscape.ding.impl.strokes;

public interface DAnimatedStroke extends AnimatedStroke, WidthStroke {
	
	public static int STEPS_PER_SECOND = 10;
	
	public static float INITIAL_OFFSET = 100000.0f;
	
	
	float getWidth();
	
	@Override
	DAnimatedStroke newInstanceForNextOffset();
	
	
	public static float nextOffset(float currentOffset) {
		float stepSize = 1.0f / STEPS_PER_SECOND;
		float newOffset = currentOffset - stepSize;
		if (newOffset < 0.0) {
			newOffset = INITIAL_OFFSET - stepSize;
		}
		return newOffset;
	}

}
