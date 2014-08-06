package org.cytoscape.ding.internal.charts.bar;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BarChartFactory implements CyChartFactory<BarLayer> {
	
	private final CyColumnIdentifierFactory colIdFactory;
	
	public BarChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}

	@Override
	public CyChart<BarLayer> getInstance(final String input) {
		return new BarChart(input, colIdFactory);
	}

	@Override
	public CyChart<BarLayer> getInstance(final CyChart<BarLayer> chart) {
		return new BarChart((BarChart)chart, colIdFactory);
	}
	
	@Override
	public CyChart<BarLayer> getInstance(final Map<String, Object> properties) {
		return new BarChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return BarChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyChart<BarLayer>> getSupportedClass() {
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
