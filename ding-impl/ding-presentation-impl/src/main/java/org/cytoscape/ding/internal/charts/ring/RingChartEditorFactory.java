package org.cytoscape.ding.internal.charts.ring;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2EditorFactory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class RingChartEditorFactory implements CyCustomGraphics2EditorFactory<RingLayer> {

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
	public Class<? extends CyCustomGraphics2<RingLayer>> getSupportedClass() {
		return (Class<? extends CyCustomGraphics2<RingLayer>>) RingChart.class;
	}

	@Override
	public JComponent createEditor(final CyCustomGraphics2<RingLayer> chart) {
		return new RingChartEditor((RingChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
