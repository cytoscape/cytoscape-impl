package org.cytoscape.ding.internal.charts.pie;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class PieChartEditorFactory implements CyChartEditorFactory<PieLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;

	public PieChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public Class<? extends CyChart<PieLayer>> getSupportedClass() {
		return (Class<? extends CyChart<PieLayer>>) PieChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<PieLayer> chart) {
		return new PieChartEditor((PieChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
