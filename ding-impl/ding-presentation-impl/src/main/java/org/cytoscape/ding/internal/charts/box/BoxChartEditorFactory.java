package org.cytoscape.ding.internal.charts.box;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;

public class BoxChartEditorFactory implements CyChartEditorFactory<BoxLayer> {

	private final CyApplicationManager appMgr;
	
	public BoxChartEditorFactory(final CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}
	
	@Override
	public Class<? extends CyChart<BoxLayer>> getSupportedClass() {
		return BoxChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<BoxLayer> chart) {
		return new BoxChartEditor((BoxChart)chart, appMgr);
	}
}
