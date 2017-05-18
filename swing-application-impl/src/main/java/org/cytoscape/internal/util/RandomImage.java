package org.cytoscape.internal.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

public class RandomImage extends BufferedImage {

	private final int grain = 5;
	private final int colorRange = 5;
	
	public RandomImage(int width, int height) {
		super(width, height, BufferedImage.TYPE_INT_ARGB);
		draw();
	}
	
	private void draw() {
		int w = getWidth();
		int h = getHeight();
		
		Graphics2D g2 = (Graphics2D) getGraphics();
		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON));

		int max = 200, min = 100;
		
		Color color = randomColor();
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		
		g2.setColor(color);
		
		double blockout = Math.random();
		int x = 0, y = 0;
		
		for (int i = 0; i < grain; i++) {
			for (int j = 0; j < grain; j++) {
				if (blockout < 0.4) {
					g2.fillRect(x, y, w / grain, h / grain);
					g2.fillRect(w - x - w / grain, y, w / grain, h / grain);
					x += w / grain;
				} else {
					red -= colorRange;
					red = Math.min(max, Math.max(red, min));
					
					green += colorRange;
					green = Math.min(max, Math.max(green, min));
					
					blue += colorRange;
					blue = Math.min(max, Math.max(blue, min));
					
					g2.setColor(new Color(red, green, blue));
					x += w / grain;
				}
				
				blockout = Math.random();
			}
			
			y += h / grain;
			x = 0;
		}
	}
	
	private Color randomColor() {
		// Get rainbow, pastel colors
		Random random = new Random();
		final float hue = random.nextFloat();
		final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
		final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
		
		return Color.getHSBColor(hue, saturation, luminance);
	}
}
