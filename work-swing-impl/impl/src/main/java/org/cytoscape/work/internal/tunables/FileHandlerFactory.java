package org.cytoscape.work.internal.tunables;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.swing.GUITunableHandler;
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

public final class FileHandlerFactory implements GUITunableHandlerFactory {
	
	private final SupportedFileTypesManager fileTypesManager;
	private final CyServiceRegistrar serviceRegistrar;

	public FileHandlerFactory(final SupportedFileTypesManager fileTypesManager,
			final CyServiceRegistrar serviceRegistrar) {
		this.fileTypesManager = fileTypesManager;
		this.serviceRegistrar = serviceRegistrar;
	}

	public GUITunableHandler createTunableHandler(Field field, Object instance, Tunable tunable) {
		if (!File.class.isAssignableFrom(field.getType()))
			return null;

		return new FileHandler(
				field,
				instance,
				tunable,
				fileTypesManager,
				serviceRegistrar.getService(FileUtil.class),
				serviceRegistrar.getService(CyApplicationManager.class)
		);
	}

	public GUITunableHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if (!File.class.isAssignableFrom(getter.getReturnType()))
			return null;

		return new FileHandler(
				getter,
				setter,
				instance,
				tunable,
				fileTypesManager,
				serviceRegistrar.getService(FileUtil.class),
				serviceRegistrar.getService(CyApplicationManager.class)
		);
	}
}

