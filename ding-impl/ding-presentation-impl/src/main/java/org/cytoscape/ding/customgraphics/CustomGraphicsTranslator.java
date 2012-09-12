package org.cytoscape.ding.customgraphics;

import java.io.IOException;
import java.net.URL;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

public class CustomGraphicsTranslator implements ValueTranslator<String, CyCustomGraphics>{

	
	private final CustomGraphicsManager cgManager;
	
	public CustomGraphicsTranslator(final CustomGraphicsManager cgManager) {
		this.cgManager = cgManager;
	}
	
	@Override
	public CyCustomGraphics translate(String inputValue) {
		return parse(inputValue);
	}

	@Override
	public Class<CyCustomGraphics> getTranslatedValueType() {
		return CyCustomGraphics.class;
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
