package org.cytoscape.ding.internal.charts.pie;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class PieChartFactory  implements CyCustomGraphics2Factory<PieLayer> {

	private final CyColumnIdentifierFactory colIdFactory;
	
	public PieChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public CyCustomGraphics2<PieLayer> getInstance(final String input) {
		return new PieChart(input, colIdFactory);
	}

	@Override
	public CyCustomGraphics2<PieLayer> getInstance(final CyCustomGraphics2<PieLayer> chart) {
		return new PieChart((PieChart)chart, colIdFactory);
	}
	
	@Override
	public CyCustomGraphics2<PieLayer> getInstance(final Map<String, Object> properties) {
		return new PieChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return PieChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<PieLayer>> getSupportedClass() {
		return PieChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Pie";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(PieChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
