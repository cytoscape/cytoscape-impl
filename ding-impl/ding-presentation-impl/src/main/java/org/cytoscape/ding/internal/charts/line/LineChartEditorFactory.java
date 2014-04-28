package org.cytoscape.ding.internal.charts.line;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class LineChartEditorFactory implements CyChartEditorFactory<LineLayer> {

	private final CyApplicationManager appMgr;
	
	public LineChartEditorFactory(final CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}
	
	@Override
	public Class<? extends CyChart<LineLayer>> getSupportedClass() {
		return LineChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<LineLayer> chart) {
		return new LineChartEditor((LineChart)chart, appMgr);
	}
}
