package org.cytoscape.ding.internal.charts.stripe;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2EditorFactory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class StripeChartEditorFactory implements CyCustomGraphics2EditorFactory<StripeLayer> {

	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;
	
	public StripeChartEditorFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<StripeLayer>> getSupportedClass() {
		return (Class<? extends CyCustomGraphics2<StripeLayer>>) StripeChart.class;
	}

	@Override
	public JComponent createEditor(final CyCustomGraphics2<StripeLayer> chart) {
		return new StripeChartEditor((StripeChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
