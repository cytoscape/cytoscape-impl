package org.cytoscape.view.vizmap.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.vizmap.AbstractTableVisualMappingManagerTest;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;

public class TableVisualMappingManagerTest extends AbstractTableVisualMappingManagerTest {
	
	protected TableVisualMappingManager createTableVisualMappingManager(VisualStyleFactory factory, CyServiceRegistrar serviceRegistrar) {
		return new TableVisualMappingManagerImpl(factory, serviceRegistrar);
	}

}
