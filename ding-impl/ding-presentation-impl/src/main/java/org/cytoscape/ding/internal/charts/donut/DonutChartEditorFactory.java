package org.cytoscape.ding.internal.charts.donut;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class DonutChartEditorFactory implements CyChartEditorFactory<DonutLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;

	public DonutChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
	}
	
	@Override
	public Class<? extends CyChart<DonutLayer>> getSupportedClass() {
		return DonutChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<DonutLayer> chart) {
		return new DonutChartEditor((DonutChart)chart, appMgr, iconMgr);
	}
}
