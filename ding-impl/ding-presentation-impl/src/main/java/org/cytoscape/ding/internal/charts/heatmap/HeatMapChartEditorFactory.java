package org.cytoscape.ding.internal.charts.heatmap;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class HeatMapChartEditorFactory implements CyChartEditorFactory<HeatMapLayer> {

	private final CyApplicationManager appMgr;
	
	public HeatMapChartEditorFactory(final CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}
	
	@Override
	public Class<? extends CyChart<HeatMapLayer>> getSupportedClass() {
		return HeatMapChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<HeatMapLayer> chart) {
		return new HeatMapChartEditor((HeatMapChart)chart, appMgr);
	}
}
