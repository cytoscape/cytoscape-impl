package org.cytoscape.ding.impl.visualproperty;

import org.cytoscape.ding.DummyCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsRange;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.AbstractVisualProperty;

public class CustomGraphicsVisualProperty extends AbstractVisualProperty<CyCustomGraphics> {

	public CustomGraphicsVisualProperty(final CyCustomGraphics<CustomGraphicLayer> defaultValue,
			final CustomGraphicsRange customGraphicsRange, String id, String displayName, Class<? extends CyIdentifiable> targetObjectDataType) {
		super(defaultValue, customGraphicsRange, id, displayName, targetObjectDataType);
	}

	@Override
	public String toSerializableString(final CyCustomGraphics value) {
		return value.toSerializableString();
	}

	@Override
	public CyCustomGraphics<CustomGraphicLayer> parseSerializableString(String value) {
		// Return dummy if something is assigned.  This should be replaced after loading session.
		if(NullCustomGraphics.getNullObject().toString().equals(value) || value.contains("NullCustomGraphics")) {
			return NullCustomGraphics.getNullObject();
		} else {
			final String[] parts = value.split(",");
			return new DummyCustomGraphics(Long.parseLong(parts[1]), parts[2]);
		}
	}
}
