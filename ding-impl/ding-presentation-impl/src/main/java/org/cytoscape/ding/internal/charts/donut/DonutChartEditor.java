package org.cytoscape.ding.internal.charts.donut;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;

public class DonutChartEditor extends AbstractChartEditor<DonutChart> {

	private static final long serialVersionUID = -6185083260942898226L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public DonutChartEditor(final DonutChart chart, final CyApplicationManager appMgr) {
		super(chart, 5, false, appMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void init() {
		super.init();
		// TODO
	}
}
