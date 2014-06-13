package org.cytoscape.ding.internal.charts.bar;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BarChartEditorFactory implements CyChartEditorFactory<BarLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;
	
	public BarChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public Class<? extends CyChart<BarLayer>> getSupportedClass() {
		return (Class<? extends CyChart<BarLayer>>) BarChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<BarLayer> chart) {
		return new BarChartEditor((BarChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
