package org.cytoscape.ding.internal.charts.box;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;

public class BoxChartEditor extends AbstractChartEditor<BoxChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BoxChartEditor(final BoxChart chart, final CyApplicationManager appMgr) {
		super(chart, 10, true, true, false, false, false, true, appMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
}
