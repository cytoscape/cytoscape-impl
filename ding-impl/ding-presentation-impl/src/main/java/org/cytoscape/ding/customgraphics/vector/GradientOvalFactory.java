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

	public String getPrefix() { return "ovalgradient"; }
	public boolean supportsMime(String mimeType) { return false; }
	
	/**
	 * Generate Custom Graphics object from a string.
	 */
	public CyCustomGraphics parseSerializableString(String entryStr) {
		String[] entry = entryStr.split(",");
		if (entry == null || entry.length < 2) {
			return null;
		}
		return new GradientOvalLayer(Long.parseLong(entry[0]));
	}

	public CyCustomGraphics getInstance(String input) {
		return new GradientOvalLayer(manager.getNextAvailableID());
	}

	public CyCustomGraphics getInstance(URL input) { return null; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

}
