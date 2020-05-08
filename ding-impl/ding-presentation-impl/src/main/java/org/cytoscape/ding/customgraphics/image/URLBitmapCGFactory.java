package org.cytoscape.ding.customgraphics.image;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

public class URLBitmapCGFactory extends AbstractURLImageCGFactory {

	private final List<String> MIME_TYPES = List.of(
			"image/bmp",
			"image/x-windows-bmp",
			"image/gif",
			"image/jpeg",
			"image/png",
			"image/vnd.wap.wbmp"
	);
	
	public URLBitmapCGFactory(CustomGraphicsManager manager) {
		super(manager);
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return MIME_TYPES.contains(mimeType);
	}
	
	@Override
	public CyCustomGraphics<?> getInstance(String input) {
		try {
			var url = new URL(input);
			var cg = manager.getCustomGraphicsBySourceURL(url);
	
			if (cg == null) {
				var id = manager.getNextAvailableID();
				cg = new URLBitmapCustomGraphics(id, url);
				manager.addCustomGraphics(cg, url);
			}
	
			return cg;
		} catch (IOException e) {
			return null;
		}
	}
	
	public Class<? extends CyCustomGraphics> getSupportedClass() {
		return URLBitmapCustomGraphics.class;
	}
}
