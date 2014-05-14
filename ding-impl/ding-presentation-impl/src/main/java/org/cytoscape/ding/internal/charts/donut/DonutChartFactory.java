package org.cytoscape.ding.internal.charts.donut;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;

public class DonutChartFactory  implements CyChartFactory<DonutLayer> {

	@Override
	public CyChart<DonutLayer> getInstance(final String input) {
		return new DonutChart(input);
	}

	@Override
	public CyChart<DonutLayer> getInstance(final CyChart<DonutLayer> chart) {
		return new DonutChart((DonutChart)chart);
	}
	
	@Override
	public CyChart<DonutLayer> getInstance(final Map<String, Object> properties) {
		return new DonutChart(properties);
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
		return "Donut";
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
