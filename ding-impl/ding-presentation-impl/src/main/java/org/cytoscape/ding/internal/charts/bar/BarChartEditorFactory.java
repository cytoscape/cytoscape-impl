package org.cytoscape.ding.internal.charts.bar;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class BarChartEditorFactory implements CyChartEditorFactory<BarLayer> {

	private final CyApplicationManager appMgr;
	
	public BarChartEditorFactory(final CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}
	
	@Override
	public Class<? extends CyChart<BarLayer>> getSupportedClass() {
		return BarChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<BarLayer> chart) {
		return new BarChartEditor((BarChart)chart, appMgr);
	}
}
