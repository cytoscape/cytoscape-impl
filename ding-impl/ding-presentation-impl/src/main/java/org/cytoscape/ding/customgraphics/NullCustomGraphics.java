package org.cytoscape.ding.customgraphics;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

/**
 * Null object for Custom Graphics. This is used to reset custom graphics on
 * node views.
 * 
 */
public class NullCustomGraphics extends AbstractDCustomGraphics<CustomGraphicLayer> {
	
	private static final String DEF_IMAGE_FILE = "images/no_image.png";
	public static BufferedImage DEF_IMAGE;
	
	static  {
		try {
			DEF_IMAGE =ImageIO.read(URLImageCustomGraphics.class
					.getClassLoader().getResource(DEF_IMAGE_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static final CyCustomGraphics<CustomGraphicLayer> NULL = new NullCustomGraphics();

	public static CyCustomGraphics<CustomGraphicLayer> getNullObject() {
		return NULL;
	}

	// Human readable name of this null object.
	private static final String NAME = "[ Remove Graphics ]";

	public NullCustomGraphics() {
		super(0l, NAME);
	}

	public String toString() {
		return "None";
	}

	@Override
	public Image getRenderedImage() {
		return DEF_IMAGE;
	}
}
