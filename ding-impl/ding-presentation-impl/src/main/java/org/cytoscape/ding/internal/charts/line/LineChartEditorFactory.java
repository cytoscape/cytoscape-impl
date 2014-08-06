package org.cytoscape.ding.internal.charts.line;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class LineChartEditorFactory implements CyChartEditorFactory<LineLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;
	
	public LineChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public Class<? extends CyChart<LineLayer>> getSupportedClass() {
		return (Class<? extends CyChart<LineLayer>>) LineChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<LineLayer> chart) {
		return new LineChartEditor((LineChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
