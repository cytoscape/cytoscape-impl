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

import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Null object for Custom Graphics. This is used to reset custom graphics on node views.
 */
public class NullCustomGraphics extends AbstractDCustomGraphics<CustomGraphicLayer> {
	
	public static Image DEF_IMAGE = IconUtil.emptyIcon(24, 24).getImage();
	
	static final CyCustomGraphics<CustomGraphicLayer> NULL = new NullCustomGraphics();

	public static CyCustomGraphics<CustomGraphicLayer> getNullObject() {
		return NULL;
	}

	// Human readable name of this null object.
	private static final String NAME = "[ Remove Graphics ]";

	private NullCustomGraphics() {
		super(0l, NAME);
	}

	@Override
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
