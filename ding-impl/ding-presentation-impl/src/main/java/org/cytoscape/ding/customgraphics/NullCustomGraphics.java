package org.cytoscape.ding.customgraphics;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.cytoscape.ding.impl.customgraphics.AbstractDCustomGraphics;
import org.cytoscape.ding.impl.customgraphics.bitmap.URLImageCustomGraphics;

/**
 * Null object for Custom Graphics. This is used to reset custom graphics on
 * node views.
 * 
 * @author kono
 * 
 */
public class NullCustomGraphics extends AbstractDCustomGraphics {
	
	private static final String DEF_IMAGE_FILE = "images/no_image.png";
	private static BufferedImage DEF_IMAGE;
	
	static  {
		try {
			DEF_IMAGE =ImageIO.read(URLImageCustomGraphics.class
					.getClassLoader().getResource(DEF_IMAGE_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static final CyCustomGraphics NULL = new NullCustomGraphics();

	public static CyCustomGraphics getNullObject() {
		return NULL;
	}

	private static final String NAME = "[ Remove Graphics ]";

	public NullCustomGraphics() {
		super(NAME);
	}

	public String toString() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public Image getRenderedImage() {
		return DEF_IMAGE;
	}
}
