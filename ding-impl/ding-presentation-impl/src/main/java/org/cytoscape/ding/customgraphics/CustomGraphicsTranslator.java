package org.cytoscape.ding.customgraphics;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

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
public class CustomGraphicsTranslator implements ValueTranslator<String, CyCustomGraphics> {

	private static final String SVG_DATA_URL_PREFIX = "data:image/svg+xml;utf8,";
	
	/** Opening svg tag */
	private static final Pattern SVG_PATTERN_1 = Pattern.compile("<svg[^>]*>", Pattern.CASE_INSENSITIVE);
	/** Closing svg tag */
	private static final Pattern SVG_PATTERN_2 = Pattern.compile("</svg>", Pattern.CASE_INSENSITIVE);
	
	private final CustomGraphicsManager cgMgr;
	private final CyCustomGraphics2Manager cg2Mgr;

	private final Map<String, String> mimeTypes = new WeakHashMap<>();

	public CustomGraphicsTranslator(CustomGraphicsManager cgMgr, CyCustomGraphics2Manager cg2Mgr) {
		this.cgMgr = cgMgr;
		this.cg2Mgr = cg2Mgr;
	}

	@Override
	public CyCustomGraphics translate(String inputValue) {
		// First check if this is a URL
		var cg = translateURL(inputValue);

		// Nope, so hand it to each factory that has a matching prefix...

		// CyCustomGraphics2 serialization format?
		if (cg == null) {
			for (var factory : cg2Mgr.getAllCyCustomGraphics2Factories()) {
				if (factory.getId() != null && inputValue.startsWith(factory.getId() + ":")) {
					cg = factory.getInstance(inputValue.substring(factory.getId().length() + 1));
					break;
				}
			}
		}

		// Old CyCustomGraphics?
		if (cg == null) {
			for (var factory : cgMgr.getAllCustomGraphicsFactories()) {
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

	private CyCustomGraphics translateURL(String input) {
		if (input == null)
			return null;
		
		input = input.trim();
		
		if (input.isEmpty())
			return null;
		
		try {
			boolean isDataURL = isDataURL(input);
			
			if (!isDataURL) {
				// Let's be nice and accept pure SVG text...
				if (isSVGText(input)) {
					// But convert to data url, because the image CG factories usually handle URLs only
					input = SVG_DATA_URL_PREFIX + input;
					isDataURL = true;
				}
			}
			
			if (isDataURL) {
				// It's a base64 data URL (e.g. "data:image/png;base64,iVBOR...")...
				var type = input.substring(input.indexOf(":") + 1, input.indexOf(";"));
				
				for (var factory : cgMgr.getAllCustomGraphicsFactories()) {
					if (factory.supportsMime(type)) {
						var cg = factory.getInstance(input);

						if (cg != null)
							return cg;
					}
				}
			} else {
				// Try to handle it as a regular URL...
				var url = new URL(input);
				var type = mimeTypes.get(input);

				if (type == null) {
					var conn = url.openConnection();

					if (conn == null)
						return null;

					type = conn.getContentType();
					mimeTypes.put(input, type);
				}

				for (var factory : cgMgr.getAllCustomGraphicsFactories()) {
					if (factory.supportsMime(type)) {
						var cg = factory.getInstance(url);

						if (cg != null)
							return cg;
					}
				}
			}
		} catch (IOException e) {
			// Just ignore...
		}

		return null;
	}

	private boolean isDataURL(String s) {
		return s.startsWith("data:") && s.indexOf(";") > s.indexOf("data:") + 1;
	}
	
	private static boolean isSVGText(String s) {
		return SVG_PATTERN_1.matcher(s).find() && SVG_PATTERN_2.matcher(s).find();
	}
}
