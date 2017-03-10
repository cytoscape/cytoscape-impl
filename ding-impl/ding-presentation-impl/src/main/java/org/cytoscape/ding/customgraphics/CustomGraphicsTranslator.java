package org.cytoscape.ding.customgraphics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

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
public class CustomGraphicsTranslator implements ValueTranslator<String, CyCustomGraphics>{

	private final CustomGraphicsManager cgMgr;
	private final CyCustomGraphics2Manager cg2Mgr;
	
	private final Map<String,String> mimeTypes = new WeakHashMap<>();
	
	
	public CustomGraphicsTranslator(final CustomGraphicsManager cgMgr, final CyCustomGraphics2Manager cg2Mgr) {
		this.cgMgr = cgMgr;
		this.cg2Mgr = cg2Mgr;
	}
	
	@Override
	public CyCustomGraphics translate(String inputValue) {
		// First check if this is a URL
		CyCustomGraphics cg = translateURL(inputValue);
		
		// Nope, so hand it to each factory that has a matching prefix...
		
		// CyCustomGraphics2 serialization format?
		if (cg == null) {
			for (CyCustomGraphics2Factory<?> factory: cg2Mgr.getAllCyCustomGraphics2Factories()) {
				if (factory.getId() != null && inputValue.startsWith(factory.getId() + ":")) {
					cg = factory.getInstance(inputValue.substring(factory.getId().length() + 1));
					break;
				}
			}
		}
		// Old CyCustomGraphics?
		if (cg == null) {
			for (CyCustomGraphicsFactory factory: cgMgr.getAllCustomGraphicsFactories()) {
				if (factory.getPrefix() != null && inputValue.startsWith(factory.getPrefix() + ":")) {
					cg = factory.getInstance(inputValue.substring(factory.getPrefix().length() + 1));
					break;
				}
			}
		}
		
		return cg;
	}

	@Override
	public Class<CyCustomGraphics> getTranslatedValueType() {
		return CyCustomGraphics.class;
	}
	
	private CyCustomGraphics translateURL(String inputValue) {
		try {
			final URL url = new URL(inputValue);
			String mimeType = mimeTypes.get(inputValue);
			if(mimeType == null) {
				URLConnection conn = url.openConnection();
				if (conn == null) return null;
				mimeType = conn.getContentType();
				mimeTypes.put(inputValue, mimeType);
			}
			for (CyCustomGraphicsFactory factory: cgMgr.getAllCustomGraphicsFactories()) {
				if (factory.supportsMime(mimeType)) {
					CyCustomGraphics cg = factory.getInstance(url);
					if (cg != null) return cg;
				}
			}
		
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return null;
	}
}
