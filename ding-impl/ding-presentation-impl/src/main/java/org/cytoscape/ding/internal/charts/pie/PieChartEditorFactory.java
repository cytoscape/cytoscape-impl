package org.cytoscape.ding.internal.charts.pie;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2EditorFactory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class PieChartEditorFactory implements CyCustomGraphics2EditorFactory<PieLayer> {

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
	public Class<? extends CyCustomGraphics2<PieLayer>> getSupportedClass() {
		return (Class<? extends CyCustomGraphics2<PieLayer>>) PieChart.class;
	}

	@Override
	public JComponent createEditor(final CyCustomGraphics2<PieLayer> chart) {
		return new PieChartEditor((PieChart)chart, appMgr, iconMgr, colIdFactory);
	}
}
