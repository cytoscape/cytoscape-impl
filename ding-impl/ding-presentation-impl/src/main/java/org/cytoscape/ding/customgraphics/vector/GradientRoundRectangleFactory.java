package org.cytoscape.ding.customgraphics.vector;

import java.net.URL;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

@SuppressWarnings("rawtypes")
public class GradientRoundRectangleFactory implements CyCustomGraphicsFactory {

	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = GradientRoundRectangleLayer.class;

	private final CustomGraphicsManager manager;

	public GradientRoundRectangleFactory(final CustomGraphicsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getPrefix() {
		return "rectanglegradient";
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return false;
	}
	
	@Override
	public CyCustomGraphics<?> parseSerializableString(String entryStr) {
		String[] entry = entryStr.split(",");
		
		if (entry == null || entry.length < 2)
			return null;
		
		return new GradientRoundRectangleLayer(Long.parseLong(entry[0]));
	}

	@Override
	public CyCustomGraphics<?> getInstance(String input) {
		return new GradientRoundRectangleLayer(manager.getNextAvailableID());
	}

	@Override
	public CyCustomGraphics<?> getInstance(URL input) {
		return null;
	}

	@Override
	public Class<? extends CyCustomGraphics> getSupportedClass() {
		return TARGET_CLASS;
	}
}
