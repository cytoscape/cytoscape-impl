package org.cytoscape.tableimport.internal.tunable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.tableimport.internal.task.TableImportContext;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.swing.GUITunableHandler;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class NetworkTableMappingParametersHandlerFactory implements GUITunableHandlerFactory {

	private final ImportType dialogType;
	private final TableImportContext tableImportContext;
    private final CyServiceRegistrar serviceRegistrar;
    
    public NetworkTableMappingParametersHandlerFactory(
    		ImportType dialogType,
    		TableImportContext tableImportContext,
    		CyServiceRegistrar serviceRegistrar
    ) {
		this.dialogType = dialogType;
		this.tableImportContext = tableImportContext;
		this.serviceRegistrar = serviceRegistrar;
	}
    
	@Override
	public GUITunableHandler createTunableHandler(Field field, Object instance,Tunable tunable) {
		if (!NetworkTableMappingParameters.class.isAssignableFrom(field.getType()))
			return null;

		return new NetworkTableMappingParametersHandler(field, instance, tunable, dialogType, tableImportContext,
				serviceRegistrar);
	}

	@Override
	public TunableHandler createTunableHandler(Method getter, Method setter,
			Object instance, Tunable tunable) {
		if (!NetworkTableMappingParameters.class.isAssignableFrom(getter.getReturnType()))
			return null;
		
		return new NetworkTableMappingParametersHandler(getter, setter, instance, tunable, dialogType,
				tableImportContext, serviceRegistrar);
	}
}
