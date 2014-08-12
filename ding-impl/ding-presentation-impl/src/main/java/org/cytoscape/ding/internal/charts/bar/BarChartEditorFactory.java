package org.cytoscape.ding.internal.charts.bar;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2EditorFactory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BarChartEditorFactory implements CyCustomGraphics2EditorFactory<BarLayer> {

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
	public Class<? extends CyCustomGraphics2<BarLayer>> getSupportedClass() {
		return (Class<? extends CyCustomGraphics2<BarLayer>>) BarChart.class;
	}

	@Override
	public JComponent createEditor(final CyCustomGraphics2<BarLayer> chart) {
		return new BarChartEditor((BarChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
