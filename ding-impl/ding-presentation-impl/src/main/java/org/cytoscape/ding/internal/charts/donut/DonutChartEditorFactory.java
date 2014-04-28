package org.cytoscape.ding.internal.charts.donut;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class DonutChartEditorFactory implements CyChartEditorFactory<DonutLayer> {

	private final CyApplicationManager appMgr;

	public DonutChartEditorFactory(final CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}
	
	@Override
	public Class<? extends CyChart<DonutLayer>> getSupportedClass() {
		return DonutChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<DonutLayer> chart) {
		return new DonutChartEditor((DonutChart)chart, appMgr);
	}
}
