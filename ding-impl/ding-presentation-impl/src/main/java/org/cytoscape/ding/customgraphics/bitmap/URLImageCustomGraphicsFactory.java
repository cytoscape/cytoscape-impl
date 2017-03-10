package org.cytoscape.ding.customgraphics.bitmap;

import java.io.IOException;
import java.net.URL;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class URLImageCustomGraphicsFactory implements CyCustomGraphicsFactory {

	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = URLImageCustomGraphics.class;
	private String entry[];

	private final CustomGraphicsManager manager;
	
	private static final Logger logger = LoggerFactory.getLogger(URLImageCustomGraphicsFactory.class);
	
	public URLImageCustomGraphicsFactory(final CustomGraphicsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getPrefix() {
		return "image";
	}

	@Override
	public boolean supportsMime(String mimeType) {
		if (mimeType.equals("image/bmp"))
			return true;
		if (mimeType.equals("image/x-windows-bmp"))
			return true;
		if (mimeType.equals("image/gif"))
			return true;
		if (mimeType.equals("image/jpeg"))
			return true;
		if (mimeType.equals("image/png"))
			return true;
		if (mimeType.equals("image/vnd.wap.wbmp"))
			return true;
		return false;
	}
	
	/**
	 * Generate Custom Graphics object from a string.
	 * <p>
	 * There are two types of valid string:
	 * <ul>
	 * <li>Image URL only - This will be used in Passthrough mapper.
	 * <li>Output of toSerializableString method of URLImageCustomGraphics
	 * </ul>
	 */
	@Override
	public CyCustomGraphics<?> parseSerializableString(String entryStr) {
		// Check this is URL or not
		if (entryStr == null) return null;
		if (!validate(entryStr)) return null;

		final String imageName = entry[0];
		final String sourceURL = entry[1];
		
		// Try using the URL first
		if (sourceURL != null) {
			try {
				URL url = new URL(sourceURL);
				CyCustomGraphics<?> cg = manager.getCustomGraphicsBySourceURL(url);
				cg.setDisplayName(entry[1]);
				return cg;
			} catch (Exception e) {
				// This just means that "sourceURL" is malformed.  That may be OK.
			}
		}
		
		final Long imageId = Long.parseLong(imageName);
		CyCustomGraphics<?> cg = manager.getCustomGraphicsByID(imageId);
		
		if (cg == null) {
			// Can't find image, maybe because it has not been added to the manager yet,
			// so create a special "missing image" graphics that stores the original raw value.
			// Cytoscape can then try to reload this missing custom graphics later.
			try {
				cg = new MissingImageCustomGraphics(entryStr, imageId, sourceURL, this);
				manager.addMissingImageCustomGraphics((MissingImageCustomGraphics)cg);
			} catch (IOException e) {
				logger.error("Cannot create MissingImageCustomGraphics object", e);
			}
		}
		
		cg.setDisplayName(entry[1]);
		
		return cg;
	}

	@Override
	public CyCustomGraphics<?> getInstance(URL url) {
		return getInstance(url.toString());
	}

	@Override
	public CyCustomGraphics<?> getInstance(String input) {
		try {
			URL url = new URL(input);
			CyCustomGraphics<?> cg = manager.getCustomGraphicsBySourceURL(url);

			if (cg == null) {
				Long id = manager.getNextAvailableID();
				cg = new URLImageCustomGraphics<>(id, input);
				manager.addCustomGraphics(cg, url);
			}

			return cg;
		} catch (IOException e) {
			return null;
		}
	}

	public Class<? extends CyCustomGraphics> getSupportedClass() {
		return TARGET_CLASS;
	}

	private boolean validate(final String entryStr) {
		entry = entryStr.split(",");
		
		if (entry == null || entry.length < 2)
			return false;
		
		return true;
	}
}
