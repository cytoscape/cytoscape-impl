package org.cytoscape.ding.customgraphics.image;

import java.io.IOException;
import java.net.URL;

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

@SuppressWarnings("rawtypes")
public class URLVectorCGFactory extends AbstractURLImageCGFactory {

	public static final String SUPPORTED_CLASS_ID =
			URLVectorCustomGraphics.TYPE_NAMESPACE + "." + URLVectorCustomGraphics.TYPE_NAME;
	
	public URLVectorCGFactory(CustomGraphicsManager manager) {
		super(manager);
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return "image/svg+xml".equals(mimeType);
	}
	
	@Override
	public CyCustomGraphics<?> getInstance(String input) {
		try {
			var url = new URL(input);
			var cg = manager.getCustomGraphicsBySourceURL(url);
	
			if (cg == null) {
				var id = manager.getNextAvailableID();
				cg = new URLVectorCustomGraphics(id, input, url);
				manager.addCustomGraphics(cg, url);
			}
			
			return cg;
		} catch (IOException e) {
			return null;
		}
	}
	
	@Deprecated
	@Override
	public Class<? extends CyCustomGraphics> getSupportedClass() {
		// TODO I think we shgould deprecate this class in the API as well and replace it with getSupportedClassId()
		return URLVectorCustomGraphics.class;
	}
	
	@Override
	public String getSupportedClassId() {
		return SUPPORTED_CLASS_ID;
	}
}
