package org.cytoscape.ding.internal.charts.heatmap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.util.IconManager;

public class HeatMapChartEditor extends AbstractChartEditor<HeatMapChart> {

	private static final long serialVersionUID = -8463795233540323840L;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapChartEditor(final HeatMapChart chart, final CyApplicationManager appMgr, final IconManager iconMgr) {
		super(chart, Number.class, 10, true, false, false, true, true, true, appMgr, iconMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================

}
