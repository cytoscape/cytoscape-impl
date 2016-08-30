package org.cytoscape.view.vizmap.internal.mappings;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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

public class PassthroughMappingFactory implements VisualMappingFunctionFactory {
	
	private static final Map<Class<?>, ValueTranslator<?, ?>> TRANSLATORS = new HashMap<>();
	private static final ValueTranslator<Object, String> DEFAULT_TRANSLATOR = new StringTranslator();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public PassthroughMappingFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	public void addValueTranslator(ValueTranslator<?, ?> translator, Map<?, ?> props) {
		if (translator != null)
			TRANSLATORS.put(translator.getTranslatedValueType(), translator);
	}

	public void removeValueTranslator(ValueTranslator<?, ?> translator, Map<?, ?> props) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K, V> VisualMappingFunction<K, V> createVisualMappingFunction(final String attributeName,
			final Class<K> attrValueType, final VisualProperty<V> vp) {

		final ValueTranslator<?, ?> translator = TRANSLATORS.get(vp.getRange().getType());
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);

		if (translator != null)
			return new PassthroughMappingImpl<>(attributeName, attrValueType, vp, (ValueTranslator<K, V>) translator,
					eventHelper);
		else
			return new PassthroughMappingImpl<>(attributeName, attrValueType, vp,
					(ValueTranslator<K, V>) DEFAULT_TRANSLATOR, eventHelper);
	}

	@Override
	public String toString() {
		return PassthroughMapping.PASSTHROUGH;
	}

	@Override
	public Class<?> getMappingFunctionType() {
		return PassthroughMapping.class;
	}
}
