package org.cytoscape.ding.customgraphics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

public class CustomGraphicsTranslator implements ValueTranslator<String, CyCustomGraphics>{

	
	private final CustomGraphicsManager cgManager;
	
	public CustomGraphicsTranslator(final CustomGraphicsManager cgManager) {
		this.cgManager = cgManager;
	}
	
	@Override
	public CyCustomGraphics translate(String inputValue) {
		// Start by assuming this is a URL
		CyCustomGraphics cg = translateURL(inputValue);
		if (cg != null) return cg;

		// Nope, so hand it to each factory that has a matching prefix
		for (CyCustomGraphicsFactory factory: cgManager.getAllCustomGraphicsFactories()) {
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
			for (CyCustomGraphicsFactory factory: cgManager.getAllCustomGraphicsFactories()) {
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
	
	/**
	 * Create a custom graphics from the given URL string.
	 * This code try to access the data source and download the image.
	 * 
	 * @param value String representation of image source URL.
	 * 
	 * @return Image Custom Graphics created from the source image.
	 */
	private final CyCustomGraphics parse(String value) {
		if(value == null)
			return null;

		// TODO: this needs to be made generic.  If we have a URL, then we can
		// hand it to the appropriate factory
		try {
			final URL url = new URL(value);
			CyCustomGraphics graphics = cgManager.getCustomGraphicsBySourceURL(url);
			if(graphics == null) {
				// Currently not in the Manager.  Need to create new instance.
				graphics = new URLImageCustomGraphics(cgManager.getNextAvailableID(), url.toString());
				// Use URL as display name
				graphics.setDisplayName(value);
				
				// Register to manager.
				cgManager.addCustomGraphics(graphics, url);
			}
			return graphics;
		} catch (IOException e) {
			return null;			
		}
	}
}
