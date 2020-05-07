package org.cytoscape.view.vizmap.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;

public class TableVisualMappingManagerImpl extends AbstractVisualMappingManager<CyTableView> implements TableVisualMappingManager {

	public TableVisualMappingManagerImpl(VisualStyleFactory factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}

	@Override
	protected VisualStyle buildGlobalDefaultStyle(VisualStyleFactory factory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CyTableView getCurrentView() {
		return null;
	}

	@Override
	protected void fireChangeEvent(VisualStyle vs, CyTableView view) {
	}

	@Override
	protected void fireAddEvent(VisualStyle vs) {
	}

	@Override
	protected void fireRemoveEvent(VisualStyle vs) {
	}

	@Override
	protected void fireSetCurrentEvent(VisualStyle vs) {
	}

}
