
package org.cytoscape.ding.impl.visualproperty;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.CustomGraphicsRange;
import org.cytoscape.ding.customgraphics.CyCustomGraphics2ManagerImpl;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.customgraphicsmgr.internal.CustomGraphicsManagerImpl;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("rawtypes")
public class CustomGraphicsVisualProperty extends AbstractVisualProperty<CyCustomGraphics> {
	
	public CustomGraphicsVisualProperty(final CyCustomGraphics<CustomGraphicLayer> defaultValue,
										final CustomGraphicsRange customGraphicsRange, 
	                                    final String id,
	                                    final String displayName, 
	                                    final Class<? extends CyIdentifiable> targetObjectDataType) {
		super(defaultValue, customGraphicsRange, id, displayName, targetObjectDataType);
	}

	@Override
	public String toSerializableString(final CyCustomGraphics value) {
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
			String[] parts = value.split(":");
			int offset = value.indexOf(":"); // Skip over the chart/gradient factory id
			
			// First check if it's a CyCustomGraphics2
			// ------------------------------
			// This is hack, but we've got no other way to get our hands on the
			// CyCustomGraphics2Manager this way, because the DVisualLexicon is created statically
			final CyCustomGraphics2ManagerImpl cfMgr = CyCustomGraphics2ManagerImpl.getInstance();
			final CyCustomGraphics2Factory<? extends CustomGraphicLayer> chartFactory = cfMgr.getCyCustomGraphics2Factory(parts[0]);
			
			if (chartFactory != null) {
				cg = (CyCustomGraphics<CustomGraphicLayer>) chartFactory.getInstance(value.substring(offset + 1));
			} else {
				// Then check if it's a CyCustomGraphics2
				// -------------------------------
				final CyCustomGraphics2ManagerImpl cgMgr = CyCustomGraphics2ManagerImpl.getInstance();
				final CyCustomGraphics2Factory<? extends CustomGraphicLayer> gradFactory = cgMgr.getCyCustomGraphics2Factory(parts[0]);
				
				if (gradFactory != null)
					cg = (CyCustomGraphics<CustomGraphicLayer>) gradFactory.getInstance(value.substring(offset + 1));
				
			}
			
			if (cg == null) {
				// Try to parse it as a regular custom graphics then
				// -------------------------------------------------
				// This is hack, but we've got no other way to get our hands on the
				// CyCustomGraphicsManager since the DVisualLexicon is created statically
				final CustomGraphicsManager cgMgr = CustomGraphicsManagerImpl.getInstance();
				
				parts = value.split(",");
				final CyCustomGraphicsFactory factory = cgMgr.getCustomGraphicsFactory(parts[0]);
				
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
