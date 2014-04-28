package org.cytoscape.ding.internal.charts.stripe;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class StripeChartEditorFactory implements CyChartEditorFactory<StripeLayer> {

	private final CyApplicationManager appMgr;
	
	public StripeChartEditorFactory(final CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}
	
	@Override
	public Class<? extends CyChart<StripeLayer>> getSupportedClass() {
		return StripeChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<StripeLayer> chart) {
		return new StripeChartEditor((StripeChart)chart, appMgr);
	}
}
