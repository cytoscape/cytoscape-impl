package org.cytoscape.ding.internal.charts.stripe;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;

public class StripeChartEditor extends AbstractChartEditor<StripeChart> {

	private static final long serialVersionUID = -7480674403722656873L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public StripeChartEditor(final StripeChart chart, final CyApplicationManager appMgr) {
		super(chart, 0, false, true, true, false, false, false, appMgr);
		itemLabelsColumnLbl.setText("Data Column");
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
}
