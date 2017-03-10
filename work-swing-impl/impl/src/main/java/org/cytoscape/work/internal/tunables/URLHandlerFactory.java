package org.cytoscape.work.internal.tunables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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

public class URLHandlerFactory implements GUITunableHandlerFactory<URLHandler> {
	
	private final CyServiceRegistrar serviceRegistrar;

	public URLHandlerFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public URLHandler createTunableHandler(Field field, Object instance, Tunable tunable) {
		if (field.getType() != URL.class)
			return null;

		return new URLHandler(field, instance, tunable, serviceRegistrar.getService(DataSourceManager.class));
	}

	@Override
	public URLHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if (getter.getReturnType() != URL.class)
			return null;

		return new URLHandler(getter, setter, instance, tunable, serviceRegistrar.getService(DataSourceManager.class));
	}
}
