package org.cytoscape.ding.internal.charts.donut;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class DonutChartFactory implements CyChartFactory<DonutLayer> {

	private final CyColumnIdentifierFactory colIdFactory;
	
	public DonutChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public CyChart<DonutLayer> getInstance(final String input) {
		return new DonutChart(input, colIdFactory);
	}

	@Override
	public CyChart<DonutLayer> getInstance(final CyChart<DonutLayer> chart) {
		return new DonutChart((DonutChart)chart, colIdFactory);
	}
	
	@Override
	public CyChart<DonutLayer> getInstance(final Map<String, Object> properties) {
		return new DonutChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return DonutChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyChart<DonutLayer>> getSupportedClass() {
		return DonutChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Ring";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(DonutChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
