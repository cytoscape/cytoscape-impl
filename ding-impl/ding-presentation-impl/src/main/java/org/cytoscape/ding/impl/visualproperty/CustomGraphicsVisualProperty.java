package org.cytoscape.ding.impl.visualproperty;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.ding.customgraphics.CustomGraphicsRange;
import org.cytoscape.ding.customgraphics.DummyCustomGraphics;
import org.cytoscape.ding.customgraphicsmgr.internal.CustomGraphicsManagerImpl;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.AbstractVisualProperty;

public class CustomGraphicsVisualProperty extends AbstractVisualProperty<CyCustomGraphics> {
	public CustomGraphicsVisualProperty(final CyCustomGraphics<CustomGraphicLayer> defaultValue,
			                                final CustomGraphicsRange customGraphicsRange, 
	                                    String id, String displayName, 
	                                    Class<? extends CyIdentifiable> targetObjectDataType) {
		super(defaultValue, customGraphicsRange, id, displayName, targetObjectDataType);
	}

	@Override
	public String toSerializableString(final CyCustomGraphics value) {
		return value.getClass().getCanonicalName()+","+value.toSerializableString();
	}

	// Parse the string associated with our visual property.  Note that we depend on the first
	// part of the string being the class name that was registered with the CustomGraphicsManager
	@Override
	public CyCustomGraphics<CustomGraphicLayer> parseSerializableString(String value) {
		// This is hokey, but we've got no other way to get our hands on the
		// CustomGraphicsManager since the DVisualLexicon is created statically
		CustomGraphicsManagerImpl cgMgr = CustomGraphicsManagerImpl.getInstance();
		// Return dummy if something is assigned.  This should be replaced after loading session.
		// System.out.println("CustomGraphicsVisualProperty: value = "+value);
		if(NullCustomGraphics.getNullObject().toString().equals(value) || value.contains("NullCustomGraphics")) {
			// System.out.println("CustomGraphicsVisualProperty: returning NullCustomGraphics");
			return NullCustomGraphics.getNullObject();
		} else {
			final String[] parts = value.split(",");
			// System.out.println("Getting factory for "+parts[0]);
			CyCustomGraphicsFactory factory = cgMgr.getCustomGraphicsFactory(parts[0]);
			if (factory == null) {
				// System.out.println("No factory for "+parts[0]);
				return NullCustomGraphics.getNullObject();
			}

			// System.out.println("Creating new "+parts[0]);
			// Skip over the class name
			int offset = value.indexOf(",");
			return factory.parseSerializableString(value.substring(offset+1));
		}
	}
}
