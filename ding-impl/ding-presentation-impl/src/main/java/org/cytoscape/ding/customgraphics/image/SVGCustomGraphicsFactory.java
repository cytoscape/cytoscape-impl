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

public class SVGCustomGraphicsFactory extends AbstractURLImageCustomGraphicsFactory<SVGLayer> {

	public static final String SUPPORTED_CLASS_ID =
			SVGCustomGraphics.TYPE_NAMESPACE + "." + SVGCustomGraphics.TYPE_NAME;
	
	public SVGCustomGraphicsFactory(CustomGraphicsManager manager) {
		super(manager);
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return "image/svg+xml".equals(mimeType);
	}
	
	@Override
	public SVGCustomGraphics getInstance(String input) {
		try {
			var url = new URL(input);
			var cg = manager.getCustomGraphicsBySourceURL(url);
	
			if (cg instanceof SVGCustomGraphics == false) {
				var id = manager.getNextAvailableID();
				cg = new SVGCustomGraphics(id, input, url);
				manager.addCustomGraphics(cg, url);
			}
			
			return (SVGCustomGraphics) cg;
		} catch (IOException e) {
			return null;
		}
	}
	
	@Override
	protected SVGCustomGraphics createMissingImageCustomGraphics(String entryStr, long id, String sourceURL) {
		try {
			var cg = new MissingSVGCustomGraphics(entryStr, id, sourceURL, this);
			manager.addMissingImageCustomGraphics(cg);
			
			return cg;
		} catch (IOException e) {
			logger.error("Cannot create MissingSVGCustomGraphics object", e);
		}
		
		return null;
	}
	
	@Override
	public Class<? extends CyCustomGraphics<?>> getSupportedClass() {
		return SVGCustomGraphics.class;
	}
	
	@Override
	public String getSupportedClassId() {
		return SUPPORTED_CLASS_ID;
	}
}