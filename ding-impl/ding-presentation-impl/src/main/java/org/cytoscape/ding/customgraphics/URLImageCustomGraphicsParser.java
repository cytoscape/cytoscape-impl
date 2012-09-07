package org.cytoscape.ding.customgraphics;

import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Create instance of URLImageCustomGraphics object from String.
 * 
 */
public class URLImageCustomGraphicsParser implements CyCustomGraphicsParser {

	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = URLImageCustomGraphics.class;
	private String entry[];

	private final CustomGraphicsManager manager;
	
	public URLImageCustomGraphicsParser(final CustomGraphicsManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Generate Custom Graphics object from a string.
	 * 
	 * <p>
	 * There are two types of valid string:
	 * <ul>
	 * <li>Image URL only - This will be used in Passthrough mapper.
	 * <li>Output of toString method of URLImageCustomGraphics
	 * </ul>
	 * 
	 */

	public CyCustomGraphics getInstance(String entryStr) {
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

	
	public Class<? extends CyCustomGraphics> getTargetClass() {
		return TARGET_CLASS;
	}

}
