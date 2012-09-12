package org.cytoscape.ding.customgraphics.vector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

/**
 * Create instance of a GradientOvalLayer
 * 
 */
public class GradientOvalFactory implements CyCustomGraphicsFactory {

	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = GradientOvalLayer.class;
	private String entry[];

	private final CustomGraphicsManager manager;
	
	public GradientOvalFactory(final CustomGraphicsManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Generate Custom Graphics object from a string.
	 */
	public CyCustomGraphics parseSerializableString(String entryStr) {
		String[] entry = entryStr.split(",");
		if (entry == null || entry.length < 3) {
			return null;
		}

		if (entry[0].trim().equals(
				GradientOvalLayer.class.getCanonicalName()) == false) {
			return null;
		}
		return new GradientOvalLayer(Long.parseLong(entry[1]));
	}

	public CyCustomGraphics getInstance(String input) {
		return new GradientOvalLayer(manager.getNextAvailableID());
	}

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

}
