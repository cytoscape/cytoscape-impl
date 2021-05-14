package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.MappingFunctionFactoryManager;

public class MappingFunctionFactoryManagerImpl implements MappingFunctionFactoryManager {
	
	private final Map<Class<?>, VisualMappingFunctionFactory> factories;
	
	public MappingFunctionFactoryManagerImpl() {
		factories = new HashMap<Class<?>, VisualMappingFunctionFactory>();
	}

	@Override
	public Collection<VisualMappingFunctionFactory> getFactories() {
		return factories.values();
	}
	
	@Override
	public VisualMappingFunctionFactory getFactory(Class<?> mappingType) {
		return factories.get(mappingType);
	}
	
	@SuppressWarnings("rawtypes")
	public void addFactory(VisualMappingFunctionFactory factory, Map properties) {
		factories.put(factory.getMappingFunctionType(), factory);
	}
	
	@SuppressWarnings("rawtypes")
	public void removeFactory(VisualMappingFunctionFactory factory, Map properties) {
		factories.remove(factory.getMappingFunctionType());
	}
}
