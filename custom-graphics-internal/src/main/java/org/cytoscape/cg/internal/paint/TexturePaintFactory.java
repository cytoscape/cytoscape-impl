package org.cytoscape.cg.internal.paint;

import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.cytoscape.cg.internal.util.PaintFactory;

public class TexturePaintFactory implements PaintFactory {
	
	private BufferedImage img;
	
	public TexturePaintFactory(BufferedImage img) {
		this.img = img;
	}

	@Override
	public TexturePaint getPaint(Rectangle2D bound) {
		return new TexturePaint(img, bound);
	}

	public BufferedImage getImage() {
		return img;
	}
}
