package org.cytoscape.ding.impl.visualproperty;

import java.util.List;

import org.cytoscape.ding.customgraphics.CustomGraphicsRange;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.graph.render.stateful.CustomGraphic;
import org.cytoscape.view.model.AbstractVisualProperty;

public class CustomGraphicsVisualProperty extends AbstractVisualProperty<CyCustomGraphics> {

	public CustomGraphicsVisualProperty(final CyCustomGraphics<CustomGraphic> defaultValue,
			final CustomGraphicsRange customGraphicsRange, String id, String displayName, Class<?> targetObjectDataType) {
		super(defaultValue, customGraphicsRange, id, displayName, targetObjectDataType);
	}

	@Override
	public String toSerializableString(final CyCustomGraphics value) {
		List layers = value.getLayers();
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CyCustomGraphics<CustomGraphic> parseSerializableString(String value) {
		// TODO Auto-generated method stub
		return null;
	}

}
