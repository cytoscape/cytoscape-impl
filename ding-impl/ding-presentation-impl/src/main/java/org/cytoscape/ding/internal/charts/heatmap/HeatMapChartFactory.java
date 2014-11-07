package org.cytoscape.ding.internal.charts.heatmap;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class HeatMapChartFactory implements CyCustomGraphics2Factory<HeatMapLayer> {
	
	private final CyApplicationManager appMgr;
	private final IconManager iconMgr;
	private final CyColumnIdentifierFactory colIdFactory;
	
	public HeatMapChartFactory(final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		this.appMgr = appMgr;
		this.iconMgr = iconMgr;
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(final String input) {
		return new HeatMapChart(input, colIdFactory);
	}

	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(final CyCustomGraphics2<HeatMapLayer> chart) {
		return new HeatMapChart((HeatMapChart)chart, colIdFactory);
	}
	
	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(final Map<String, Object> properties) {
		return new HeatMapChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return HeatMapChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<HeatMapLayer>> getSupportedClass() {
		return HeatMapChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Heat Map";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(HeatMapChart.ICON, width, height);
	}
	
	@Override
	public JComponent createEditor(final CyCustomGraphics2<HeatMapLayer> chart) {
		return new HeatMapChartEditor((HeatMapChart)chart, appMgr, iconMgr, colIdFactory);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
