package org.cytoscape.ding.internal.charts.pie;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;

public class PieChartEditor extends AbstractChartEditor<PieChart> {

	private static final long serialVersionUID = -6185083260942898226L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public PieChartEditor(final PieChart chart, final CyApplicationManager appMgr) {
		super(chart, 1, false, false, false, false, false, false, appMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void init() {
		super.init();
		// TODO
	}
}
