package org.cytoscape.ding.internal.charts.line;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class LineChartEditorFactory implements CyChartEditorFactory<LineLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	
	public LineChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
	}
	
	@Override
	public Class<? extends CyChart<LineLayer>> getSupportedClass() {
		return (Class<? extends CyChart<LineLayer>>) LineChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<LineLayer> chart) {
		return new LineChartEditor((LineChart)chart, appMgr, iconMgr);
	}
}
