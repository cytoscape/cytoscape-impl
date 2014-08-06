package org.cytoscape.ding.internal.charts.box;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BoxChartEditorFactory implements CyChartEditorFactory<BoxLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;
	
	public BoxChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public Class<? extends CyChart<BoxLayer>> getSupportedClass() {
		return (Class<? extends CyChart<BoxLayer>>) BoxChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<BoxLayer> chart) {
		return new BoxChartEditor((BoxChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
