package org.cytoscape.cg.internal.charts.box;

import org.cytoscape.cg.internal.charts.AbstractChartEditor;
import org.cytoscape.service.util.CyServiceRegistrar;

@SuppressWarnings("serial")
public class BoxChartEditor extends AbstractChartEditor<BoxChart> {

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BoxChartEditor(BoxChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, true, true, true, false, false, false, true, true, serviceRegistrar);
		
		getDomainAxisVisibleCkb().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
}
