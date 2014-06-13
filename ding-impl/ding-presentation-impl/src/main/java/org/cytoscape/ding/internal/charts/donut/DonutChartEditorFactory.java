package org.cytoscape.ding.internal.charts.donut;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class DonutChartEditorFactory implements CyChartEditorFactory<DonutLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;

	public DonutChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public Class<? extends CyChart<DonutLayer>> getSupportedClass() {
		return (Class<? extends CyChart<DonutLayer>>) DonutChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<DonutLayer> chart) {
		return new DonutChartEditor((DonutChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
