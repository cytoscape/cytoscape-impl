package org.cytoscape.ding.impl.visualproperty;

import org.cytoscape.ding.customgraphics.CustomGraphicsRange;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.view.model.AbstractVisualProperty;

public class CustomGraphicsVisualProperty extends
		AbstractVisualProperty<CyCustomGraphics> {


	public CustomGraphicsVisualProperty(final CyCustomGraphics defaultValue, final CustomGraphicsRange customGraphicsRange,
			String id, String displayName, Class<?> targetObjectDataType) {
		super(defaultValue, customGraphicsRange, id, displayName,
				targetObjectDataType);
	}
	

	@Override
	public String toSerializableString(CyCustomGraphics value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CyCustomGraphics parseSerializableString(String value) {
		// TODO Auto-generated method stub
		return null;
	}

}
