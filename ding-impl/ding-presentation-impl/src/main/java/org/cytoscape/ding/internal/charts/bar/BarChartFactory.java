package org.cytoscape.ding.internal.charts.bar;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BarChartFactory implements CyCustomGraphics2Factory<BarLayer> {
	
	private final CyColumnIdentifierFactory colIdFactory;
	
	public BarChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}

	@Override
	public CyCustomGraphics2<BarLayer> getInstance(final String input) {
		return new BarChart(input, colIdFactory);
	}

	@Override
	public CyCustomGraphics2<BarLayer> getInstance(final CyCustomGraphics2<BarLayer> chart) {
		return new BarChart((BarChart)chart, colIdFactory);
	}
	
	@Override
	public CyCustomGraphics2<BarLayer> getInstance(final Map<String, Object> properties) {
		return new BarChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return BarChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<BarLayer>> getSupportedClass() {
		return BarChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Bar";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(BarChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
