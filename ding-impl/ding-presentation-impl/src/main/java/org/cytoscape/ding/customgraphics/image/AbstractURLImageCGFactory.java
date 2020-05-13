package org.cytoscape.ding.customgraphics.image;

import java.io.IOException;
import java.net.URL;

import org.cytoscape.application.CyUserLog;
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

public abstract class AbstractURLImageCGFactory implements CyCustomGraphicsFactory {

	protected String entry[];
	protected final CustomGraphicsManager manager;
	
	static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public AbstractURLImageCGFactory(CustomGraphicsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getPrefix() {
		return "image";
	}

	/**
	 * Generate Custom Graphics object from a string.
	 * <p>
	 * There are two types of valid string:
	 * <ul>
	 * <li>Image URL only - This will be used in Passthrough mapper.
	 * <li>Output of toSerializableString method of URLBitmapCustomGraphics
	 * </ul>
	 */
	@Override
	public CyCustomGraphics<?> parseSerializableString(String entryStr) {
		// Check this is URL or not
		if (entryStr == null)
			return null;
		if (!validate(entryStr))
			return null;
	
		var imageName = entry[0];
		var sourceURL = entry[1];
		
		// Try using the URL first
		if (sourceURL != null) {
			try {
				var url = new URL(sourceURL);
				var cg = manager.getCustomGraphicsBySourceURL(url);
				cg.setDisplayName(entry[1]);
				
				return cg;
			} catch (Exception e) {
				// This just means that "sourceURL" is malformed.  That may be OK.
			}
		}
		
		var id = Long.parseLong(imageName);
		var cg = manager.getCustomGraphicsByID(id);
		
		if (cg == null) {
			// Can't find image, maybe because it has not been added to the manager yet,
			// so create a special "missing image" graphics that stores the original raw value.
			// Cytoscape can then try to reload this missing custom graphics later.
			try {
				cg = new MissingImageCustomGraphics(entryStr, id, sourceURL, this);
				manager.addMissingImageCustomGraphics((MissingImageCustomGraphics) cg);
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
	
	/**
	 * Use this method instead of {@link #getSupportedClass()} whenever possible, otherwise if the class name of the
	 * supported custom graphics is changed, this factory will no longer be located by the manager.
	 */
	public abstract String getSupportedClassId();

	protected boolean validate(String entryStr) {
		entry = entryStr.split(",");
		
		if (entry == null || entry.length < 2)
			return false;
		
		return true;
	}
}