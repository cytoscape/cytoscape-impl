package org.cytoscape.view.vizmap.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleAddedEvent;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleSetEvent;

public class TableVisualMappingManagerImpl extends AbstractVisualMappingManager<View<CyColumn>> implements TableVisualMappingManager {

	private final CyServiceRegistrar serviceRegistrar;
	
	public TableVisualMappingManagerImpl(VisualStyleFactory factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	protected VisualStyle buildGlobalDefaultStyle(VisualStyleFactory factory) {
		return null;
	}
	
	@Override
	public VisualStyle getDefaultVisualStyle() {
		return defaultStyle;
	}

	@Override
	protected View<CyColumn> getCurrentView() {
		return null;
	}

	@Override
	protected void fireChangeEvent(VisualStyle vs, View<CyColumn> view) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new ColumnVisualStyleSetEvent(this, vs, view));
	}

	@Override
	protected void fireAddEvent(VisualStyle vs) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new ColumnVisualStyleAddedEvent(this, vs));
	}

	@Override
	protected void fireRemoveEvent(VisualStyle vs) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new ColumnVisualStyleAboutToBeRemovedEvent(this, vs));
	}

	@Override
	protected void fireSetCurrentEvent(VisualStyle vs) {
	}

}
