package org.cytoscape.ding.internal.charts.stripe;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class StripeChartEditorFactory implements CyChartEditorFactory<StripeLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;
	
	public StripeChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public Class<? extends CyChart<StripeLayer>> getSupportedClass() {
		return (Class<? extends CyChart<StripeLayer>>) StripeChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<StripeLayer> chart) {
		return new StripeChartEditor((StripeChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
