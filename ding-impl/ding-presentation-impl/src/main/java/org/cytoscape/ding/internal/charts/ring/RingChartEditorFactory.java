package org.cytoscape.ding.internal.charts.ring;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class RingChartEditorFactory implements CyChartEditorFactory<RingLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;

	public RingChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public Class<? extends CyChart<RingLayer>> getSupportedClass() {
		return (Class<? extends CyChart<RingLayer>>) RingChart.class;
	}

	@Override
	public JComponent createEditor(final CyChart<RingLayer> chart) {
		return new RingChartEditor((RingChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
