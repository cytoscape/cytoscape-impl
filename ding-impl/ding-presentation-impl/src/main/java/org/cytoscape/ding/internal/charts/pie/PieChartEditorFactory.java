package org.cytoscape.ding.internal.charts.pie;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class PieChartEditorFactory implements CyChartEditorFactory<PieLayer> {

	private final CyApplicationManager appMgr;

	public PieChartEditorFactory(final CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}
	
	@Override
	public Class<? extends CyChart<PieLayer>> getSupportedClass() {
		return PieChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<PieLayer> chart) {
		return new PieChartEditor((PieChart)chart, appMgr);
	}
}
