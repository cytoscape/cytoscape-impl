package org.cytoscape.ding.internal.charts.heatmap;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class HeatMapChartEditorFactory implements CyChartEditorFactory<HeatMapLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	
	public HeatMapChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
	}
	
	@Override
	public Class<? extends CyChart<HeatMapLayer>> getSupportedClass() {
		return (Class<? extends CyChart<HeatMapLayer>>) HeatMapChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<HeatMapLayer> chart) {
		return new HeatMapChartEditor((HeatMapChart)chart, appMgr, iconMgr);
	}
}
