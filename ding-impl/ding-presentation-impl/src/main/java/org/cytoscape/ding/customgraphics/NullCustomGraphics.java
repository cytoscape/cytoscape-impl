package org.cytoscape.ding.customgraphics;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;

import org.cytoscape.model.CyNode;
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

	@Override
	public String toSerializableString() {
		return makeSerializableString(displayName);
	}
}
