package org.cytoscape.ding.internal.charts.box;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.util.IconManager;

public class BoxChartEditor extends AbstractChartEditor<BoxChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BoxChartEditor(final BoxChart chart, final CyApplicationManager appMgr, final IconManager iconMgr) {
		super(chart, Number.class, 10, true, true, false, false, false, true, appMgr, iconMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
}
