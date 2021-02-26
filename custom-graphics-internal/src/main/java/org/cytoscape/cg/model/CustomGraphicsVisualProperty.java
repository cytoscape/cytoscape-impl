
package org.cytoscape.cg.model;

import org.cytoscape.cg.internal.model.CustomGraphicsManagerImpl;
import org.cytoscape.cg.internal.model.CustomGraphics2ManagerImpl;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

@SuppressWarnings("rawtypes")
public class CustomGraphicsVisualProperty extends AbstractVisualProperty<CyCustomGraphics> {
	
	public CustomGraphicsVisualProperty(
			CyCustomGraphics<CustomGraphicLayer> defaultValue,
			CustomGraphicsRange customGraphicsRange,
			String id,
			String displayName, 
			Class<? extends CyIdentifiable> targetObjectDataType
	) {
		super(defaultValue, customGraphicsRange, id, displayName, targetObjectDataType);
	}

	@Override
	public String toSerializableString(CyCustomGraphics value) {
		return value.toSerializableString();
	}

	// Parse the string associated with our visual property.  Note that we depend on the first
	// part of the string being the class name that was registered with the CyCustomGraphicsManager
	@Override
	@SuppressWarnings("unchecked")
	public CyCustomGraphics<CustomGraphicLayer> parseSerializableString(String value) {
		CyCustomGraphics<CustomGraphicLayer> cg = null;
		
		if (value != null
				&& !NullCustomGraphics.getNullObject().toString().equals(value)
				&& !value.contains("NullCustomGraphics")) {
			var parts = value.split(":");
			int offset = value.indexOf(":"); // Skip over the chart/gradient factory id
			
			// First check if it's a CyCustomGraphics2
			// ---------------------------------------
			// This is hack, but we've got no other way to get our hands on the
			// CustomGraphics2Manager this way, because the DVisualLexicon is created statically
			var cg2Mgr = CustomGraphics2ManagerImpl.getInstance();
			var chartFactory = cg2Mgr.getCustomGraphics2Factory(parts[0]);
			
			if (chartFactory != null) {
				cg = (CyCustomGraphics<CustomGraphicLayer>) chartFactory.getInstance(value.substring(offset + 1));
			} else {
				var gradFactory = cg2Mgr.getCustomGraphics2Factory(parts[0]);
				
				if (gradFactory != null)
					cg = (CyCustomGraphics<CustomGraphicLayer>) gradFactory.getInstance(value.substring(offset + 1));
			}
			
			if (cg == null) {
				// Try to parse it as a regular custom graphics then
				// -------------------------------------------------
				// This is hack, but we've got no other way to get our hands on the
				// CyCustomGraphicsManager since the DVisualLexicon is created statically
				var cgMgr = CustomGraphicsManagerImpl.getInstance();
				
				parts = value.split(",");
				var factory = cgMgr.getCustomGraphicsFactory(parts[0]);
				
				if (factory != null) {
					// Skip over the class name
					offset = value.indexOf(",");
					cg = factory.parseSerializableString(value.substring(offset + 1));
				}
			}
		}
		
		return cg != null ? cg : NullCustomGraphics.getNullObject();
	}
}
