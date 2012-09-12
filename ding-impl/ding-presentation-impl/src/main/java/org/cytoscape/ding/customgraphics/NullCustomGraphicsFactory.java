package org.cytoscape.ding.customgraphics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

/**
 * Create instance of a NullCustomGraphics
 * 
 */
public class NullCustomGraphicsFactory implements CyCustomGraphicsFactory {

	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = NullCustomGraphics.class;
	private String entry[];

	private final CustomGraphicsManager manager;
	
	public NullCustomGraphicsFactory(final CustomGraphicsManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Generate Custom Graphics object from a string.
	 */
	public CyCustomGraphics parseSerializableString(String entryStr) {
		return new NullCustomGraphics();
	}

	public CyCustomGraphics getInstance(String input) {
		return new NullCustomGraphics();
	}

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

}
