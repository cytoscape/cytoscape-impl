package org.cytoscape.tableimport.internal.tunable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.swing.GUITunableHandler;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class NetworkTableMappingParametersHandlerFactory implements GUITunableHandlerFactory {

	private final int dialogType;
    private final CyTableManager tableManager;
    
    
    public NetworkTableMappingParametersHandlerFactory(final int dialogType, final CyTableManager tableManager) {
		this.dialogType = dialogType;
		this.tableManager = tableManager;
		
	}
	@Override
	public GUITunableHandler createTunableHandler(Field field, Object instance,Tunable tunable) {
		if (!NetworkTableMappingParameters.class.isAssignableFrom(field.getType()))
			return null;

		return new NetworkTableMappingParametersHandler(field, instance, tunable, dialogType, tableManager);
	}

	@Override
	public TunableHandler createTunableHandler(Method getter, Method setter,
			Object instance, Tunable tunable) {
		if (!NetworkTableMappingParameters.class.isAssignableFrom(getter.getReturnType()))
			return null;
		return new NetworkTableMappingParametersHandler(getter, setter, instance, tunable, dialogType, tableManager);
	}

}
