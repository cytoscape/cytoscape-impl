package org.cytoscape.filter.internal.topology;

import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.model.HolisticTransformer;
import org.cytoscape.filter.model.HolisticTransformerFactory;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class TopologyTransformerFactory implements HolisticTransformerFactory<CyNetwork, CyIdentifiable> {

	private FilterPanelController filterPanelController;

	public void setFilterPanelController(FilterPanelController filterPanelController) {
		this.filterPanelController = filterPanelController;
	}
	
	@Override
	public HolisticTransformer<CyNetwork, CyIdentifiable> createHolisticTransformer() {
		return new TopologyTransformer(filterPanelController);
	}
	
	@Override
	public String getId() {
		return TopologyTransformer.ID;
	}

}
