package org.cytoscape.work.internal.properties;

/*
 * #%L
 * org.cytoscape.work-impl
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2015 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.work.properties.TunablePropertyHandler;
import org.cytoscape.work.properties.TunablePropertyHandlerFactory;
import org.cytoscape.work.properties.TunablePropertySerializer;
import org.cytoscape.work.properties.TunablePropertySerializerFactory;

public class TunablePropertySerializerFactoryImpl implements TunablePropertySerializerFactory {

	private final List<TunablePropertyHandlerFactory<TunablePropertyHandler>> tunableHandlerFactories = new ArrayList<>(2);
	
	
	@Override
	public TunablePropertySerializer createSerializer() {
		TunablePropertySerializerImpl serializer = new TunablePropertySerializerImpl();
		for(TunablePropertyHandlerFactory<TunablePropertyHandler> thf : tunableHandlerFactories) {
			serializer.addTunableHandlerFactory(thf, new HashMap<>());
		}
		return serializer;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addTunableHandlerFactory(TunablePropertyHandlerFactory thf, Map properties) {
		tunableHandlerFactories.add(thf);
	}
	@SuppressWarnings({ "rawtypes" })
	public void removeTunableHandlerFactory(TunablePropertyHandlerFactory thf, Map properties) {
		tunableHandlerFactories.remove(thf);
	}
	

}
