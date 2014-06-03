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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.charts.CyChartFactoryManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientFactory;
import org.cytoscape.view.presentation.gradients.CyGradientFactoryManager;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

@SuppressWarnings("rawtypes")
public class CustomGraphicsTranslator implements ValueTranslator<String, CyCustomGraphics>{

	
	private final CustomGraphicsManager cgMgr;
	private final CyChartFactoryManager chartMgr;
	private final CyGradientFactoryManager gradMgr;
	
	public CustomGraphicsTranslator(final CustomGraphicsManager cgMgr, final CyChartFactoryManager chartMgr,
			final CyGradientFactoryManager gradMgr) {
		this.cgMgr = cgMgr;
		this.chartMgr = chartMgr;
		this.gradMgr = gradMgr;
	}
	
	@Override
	public CyCustomGraphics translate(String inputValue) {
		// First check if this is a URL
		CyCustomGraphics cg = translateURL(inputValue);
		if (cg != null) return cg;
		
		// Nope, so hand it to each factory that has a matching prefix
		// CyChart?
		for (CyChartFactory<?> factory: chartMgr.getAllCyChartFactories()) {
			if (factory.getId() != null && inputValue.startsWith(factory.getId() + ":")) {
				final CyChart<?> chart = factory.getInstance(inputValue.substring(factory.getId().length() + 1));
				
				if (chart != null)
					return chart;
			}
		}
		// CyGradient?
		for (CyGradientFactory<?> factory: gradMgr.getAllCyGradientFactories()) {
			if (factory.getId() != null && inputValue.startsWith(factory.getId() + ":")) {
				final CyGradient<?> grad = factory.getInstance(inputValue.substring(factory.getId().length() + 1));
				
				if (grad != null)
					return grad;
			}
		}
		// Regular CyCustomGraphics?
		for (CyCustomGraphicsFactory factory: cgMgr.getAllCustomGraphicsFactories()) {
			if (factory.getPrefix() != null && inputValue.startsWith(factory.getPrefix()+":")) {
				cg = factory.getInstance(inputValue.substring(factory.getPrefix().length()+1));
				if (cg != null) return cg;
			}
		}
		
		return null;
	}

	@Override
	public Class<CyCustomGraphics> getTranslatedValueType() {
		return CyCustomGraphics.class;
	}
	
	private CyCustomGraphics translateURL(String inputValue) {
		try {
			final URL url = new URL(inputValue);
			URLConnection conn = url.openConnection();
			if (conn == null) return null;
			String mimeType = conn.getContentType();
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
	
//	/**
//	 * Create a custom graphics from the given URL string.
//	 * This code try to access the data source and download the image.
//	 * 
//	 * @param value String representation of image source URL.
//	 * 
//	 * @return Image Custom Graphics created from the source image.
//	 */
//	private final CyCustomGraphics parse(String value) {
//		if(value == null)
//			return null;
//
//		// TODO: this needs to be made generic.  If we have a URL, then we can
//		// hand it to the appropriate factory
//		try {
//			final URL url = new URL(value);
//			CyCustomGraphics graphics = cgMgr.getCustomGraphicsBySourceURL(url);
//			if(graphics == null) {
//				// Currently not in the Manager.  Need to create new instance.
//				graphics = new URLImageCustomGraphics(cgMgr.getNextAvailableID(), url.toString());
//				// Use URL as display name
//				graphics.setDisplayName(value);
//				
//				// Register to manager.
//				cgMgr.addCustomGraphics(graphics, url);
//			}
//			return graphics;
//		} catch (IOException e) {
//			return null;			
//		}
//	}
}
