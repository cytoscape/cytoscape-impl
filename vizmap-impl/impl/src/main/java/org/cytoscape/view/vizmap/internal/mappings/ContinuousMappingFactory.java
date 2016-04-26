package org.cytoscape.view.vizmap.internal.mappings;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContinuousMappingFactory implements VisualMappingFunctionFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(ContinuousMappingFactory.class);
	
	private final CyEventHelper eventHelper;
	
	public ContinuousMappingFactory(final CyEventHelper eventHelper) {
		this.eventHelper = eventHelper;
	}

	@Override
	public <K, V> VisualMappingFunction<K, V> createVisualMappingFunction(final String attributeName, 
			Class<K> attrValueType, VisualProperty<V> vp) {
		
		logger.debug("Trying to create Continuous mapping.  Data Type is " + attrValueType);
		
		// Validate attribute type: Continuous Mapping is compatible with Numbers only.
		if(Number.class.isAssignableFrom(attrValueType) == false)
			throw new IllegalArgumentException("ContinuousMapping can be used for numerical column types only.");
		
		return new ContinuousMappingImpl<>(attributeName, attrValueType, vp, eventHelper);
	}
	
	@Override public String toString() {
		return ContinuousMapping.CONTINUOUS;
	}

	@Override
	public Class<?> getMappingFunctionType() {
		return ContinuousMapping.class;
	}

}
