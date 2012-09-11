package org.cytoscape.ding.customgraphics.bitmap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

/**
 * Create instance of URLImageCustomGraphics object from String.
 * 
 */
public class URLImageCustomGraphicsFactory implements CyCustomGraphicsFactory {

	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = URLImageCustomGraphics.class;
	private String entry[];

	private final CustomGraphicsManager manager;
	
	public URLImageCustomGraphicsFactory(final CustomGraphicsManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Generate Custom Graphics object from a string.
	 * 
	 * <p>
	 * There are two types of valid string:
	 * <ul>
	 * <li>Image URL only - This will be used in Passthrough mapper.
	 * <li>Output of toSerializableString method of URLImageCustomGraphics
	 * </ul>
	 * 
	 */
	public CyCustomGraphics parseSerializableString(String entryStr) {
		// Check this is URL or not
		if(entryStr == null) return null;
		
		if (!validate(entryStr)) {
			return null;
		}

		final String imageName = entry[1];
		CyCustomGraphics cg = manager.getCustomGraphicsByID(Long.parseLong(imageName));
		cg.setDisplayName(entry[2]);
		return cg;
	}

	public CyCustomGraphics getInstance(String input) {
		Long id = manager.getNextAvailableID();
		URL url = null;
		CyCustomGraphics ccg = null;

		try {
			ccg = new URLImageCustomGraphics(id, input);
			url = new URL(input);
		} catch (MalformedURLException e) {
			// Just fall through
		} catch (IOException e) {
			// Just fall through
		}
		if (input != null && url != null)
			manager.addCustomGraphics(ccg, url);
		return ccg;
	}

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	private boolean validate(final String entryStr) {
		entry = entryStr.split(",");
		if (entry == null || entry.length < 3) {
			return false;
		}

		// Check class name
		if (entry[0].trim().equals(
				URLImageCustomGraphics.class.getCanonicalName()) == false) {
			return false;
		}
		return true;
	}
}
