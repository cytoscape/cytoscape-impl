package org.cytoscape.cg.internal.vector;

import java.net.URL;

import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

@SuppressWarnings("rawtypes")
public class GradientRoundRectangleFactory implements CyCustomGraphicsFactory {

	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = GradientRoundRectangleLayer.class;

	private final CustomGraphicsManager manager;

	public GradientRoundRectangleFactory(CustomGraphicsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getPrefix() {
		return "rectanglegradient";
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return false;
	}
	
	@Override
	public CyCustomGraphics<?> parseSerializableString(String entryStr) {
		String[] entry = entryStr.split(",");
		
		if (entry == null || entry.length < 2)
			return null;
		
		return new GradientRoundRectangleLayer(Long.parseLong(entry[0]));
	}

	@Override
	public CyCustomGraphics<?> getInstance(String input) {
		return new GradientRoundRectangleLayer(manager.getNextAvailableID());
	}

	@Override
	public CyCustomGraphics<?> getInstance(URL input) {
		return null;
	}

	@Override
	public Class<? extends CyCustomGraphics> getSupportedClass() {
		return TARGET_CLASS;
	}
}
