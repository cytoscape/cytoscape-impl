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

	@Override
	public String getPrefix() { return "image"; }

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

		final String imageName = entry[0];
		final String sourceURL = entry[1];
		// Try using the URL first
		if (sourceURL != null) {
			try {
				URL url = new URL(sourceURL);
				CyCustomGraphics cg = manager.getCustomGraphicsBySourceURL(url);
				cg.setDisplayName(entry[1]);
				return cg;
			} catch (Exception e) {
				// This just means that "sourceURL" is malformed.  That may be OK.
			}
		}
		CyCustomGraphics cg = manager.getCustomGraphicsByID(Long.parseLong(imageName));
		cg.setDisplayName(entry[1]);
		return cg;
	}

	public CyCustomGraphics getInstance(URL url) {
		return getInstance(url.toString());
	}

	public CyCustomGraphics getInstance(String input) {
		Long id = manager.getNextAvailableID();
		URL url = null;
		CyCustomGraphics ccg = null;
		// System.out.println("URLImageCustomGraphicsFactory: input = "+input);

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
		if (entry == null || entry.length < 2) {
			return false;
		}
		return true;
	}
}
