package org.cytoscape.work.internal.tunables;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class URLHandlerFactory implements GUITunableHandlerFactory<URLHandler> {
	
	private final DataSourceManager manager;

	public URLHandlerFactory(final DataSourceManager manager) {
		this.manager = manager;
	}

	@Override
	public URLHandler createTunableHandler(Field field, Object instance, Tunable tunable) {
		if (field.getType() != URL.class)
			return null;

		return new URLHandler(field, instance, tunable, manager);
	}

	@Override
	public URLHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if (getter.getReturnType() != URL.class)
			return null;

		return new URLHandler(getter, setter, instance, tunable, manager);
	}
}
