package org.cytoscape.tableimport.internal.tunable;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandler;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class AttributeMappingParametersHandlerFactory implements GUITunableHandlerFactory {
	
	private final int dialogType;
    private final CyTableManager tableManager;
    
	public AttributeMappingParametersHandlerFactory( final int dialogType, final CyTableManager tableManager) {
		this.dialogType = dialogType;
		this.tableManager = tableManager;
		
	}

	public GUITunableHandler createTunableHandler(Field field, Object instance, Tunable t) {
		if (!AttributeMappingParameters.class.isAssignableFrom(field.getType()))
			return null;

		return new AttributeMappingParametersHandler(field, instance, t, dialogType, tableManager);
	}

	public GUITunableHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if (!AttributeMappingParameters.class.isAssignableFrom(getter.getReturnType()))
			return null;

		return new AttributeMappingParametersHandler(getter, setter, instance, tunable, dialogType, tableManager);
	}

}
