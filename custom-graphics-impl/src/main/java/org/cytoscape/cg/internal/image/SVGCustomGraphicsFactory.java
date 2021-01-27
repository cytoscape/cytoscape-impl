package org.cytoscape.cg.internal.image;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import com.google.common.hash.Hashing;

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

/**
 * This factory accepts SVG images from URLs, Data URLs (e.g. "data:image/svg+xml;utf8,&lt;svg ...&gt;...&lt;/svg&gt;")
 * or just raw SVG text.
 */
public class SVGCustomGraphicsFactory extends AbstractURLImageCustomGraphicsFactory<SVGLayer> {

	public static final String SUPPORTED_CLASS_ID =
			SVGCustomGraphics.TYPE_NAMESPACE + "." + SVGCustomGraphics.TYPE_NAME;
	
	public SVGCustomGraphicsFactory(CustomGraphicsManager manager, CyServiceRegistrar serviceRegistrar) {
		super(manager, serviceRegistrar);
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return "image/svg+xml".equalsIgnoreCase(mimeType);
	}
	
	@Override
	public SVGCustomGraphics getInstance(String input) {
		try {
			URL url = null;
			String name = null;
			boolean isSVGText = false;
			
			if (isDataURL(input)) {
				var idx = input.indexOf(',');
				
				if (idx == -1 || idx >= input.length() - 1)
					return null;
				
				input = input.substring(idx + 1, input.length()).trim(); // This is now the SVG text only!
				isSVGText = true;
				
				// Create a name from this hash to try to reuse images that have already been parsed
				var sha = Hashing.sha256().hashString(input, StandardCharsets.UTF_8).toString();
				name = sha + ".svg";
				
				// Create a fake local URL so it can be stored in the manager
				var config = serviceRegistrar.getService(CyApplicationConfiguration.class);
				var dir = config.getConfigurationDirectoryLocation();
				var file = new File(dir, name);
				url = file.toURI().toURL();
			} else {
				name = input;
				url = new URL(input);
			}

			var cg = manager.getCustomGraphicsBySourceURL(url);

			if (cg instanceof SVGCustomGraphics == false) {
				var id = manager.getNextAvailableID();
				cg = isSVGText ? new SVGCustomGraphics(id, name, url, input) : new SVGCustomGraphics(id, input, url);

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
	
	private boolean isDataURL(String s) {
		return s.startsWith("data:image/svg+xml");
	}
}
