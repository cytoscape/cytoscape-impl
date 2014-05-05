package org.cytoscape.ding.internal.charts.heatmap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;

public class HeatMapChartEditor extends AbstractChartEditor<HeatMapChart> {

	private static final long serialVersionUID = -8463795233540323840L;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapChartEditor(final HeatMapChart chart, final CyApplicationManager appMgr) {
		super(chart, 10, true, false, false, true, true, true, appMgr);
		// TODO Make it easier for user to set many columns at once
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================

}
