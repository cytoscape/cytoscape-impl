package org.cytoscape.view.vizmap.gui.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.MappingFunctionFactoryManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyComboBoxPropertyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingFunctionFactoryManagerImpl implements
		MappingFunctionFactoryManager {
	
	private static final Logger logger = LoggerFactory.getLogger(MappingFunctionFactoryManagerImpl.class);
	
	private final Map<Class<?>, VisualMappingFunctionFactory> factories;
	
	private final EditorManager editorManager;
	
	public MappingFunctionFactoryManagerImpl(final EditorManager editorManager) {
		this.editorManager = editorManager;
		factories = new HashMap<Class<?>, VisualMappingFunctionFactory>();
	}

	@Override
	public Collection<VisualMappingFunctionFactory> getFactories() {
		return factories.values();
	}
	
	
	public void addFactory(VisualMappingFunctionFactory factory, @SuppressWarnings("rawtypes") Map properties) {
		logger.debug("Got Mapping Factory: " + factory.toString());
		factories.put(factory.getMappingFunctionType(), factory);
		
		updateSelectorGUI();
	}

	
	public void removeFactory(VisualMappingFunctionFactory factory, @SuppressWarnings("rawtypes") Map properties) {
		logger.debug("************* Removing VM Function Factory ****************");
		factories.remove(factory.getMappingFunctionType());
		
		updateSelectorGUI();
	}
	
	private void updateSelectorGUI() {
//		final SortedSet<String> mappingNames = new TreeSet<String>();
//		for(final VisualMappingFunctionFactory factory: factories)
//			mappingNames.add(factory.toString());
		
		((CyComboBoxPropertyEditor)editorManager.getMappingFunctionSelector()).setAvailableValues(factories.values().toArray());
	}

	@Override
	public VisualMappingFunctionFactory getFactory(Class<?> mappingType) {
		return factories.get(mappingType);
	}

}
