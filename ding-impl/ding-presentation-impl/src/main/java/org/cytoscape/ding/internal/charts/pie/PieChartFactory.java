package org.cytoscape.ding.internal.charts.pie;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;

public class PieChartFactory  implements CyChartFactory<PieLayer> {

	@Override
	public CyChart<PieLayer> getInstance(final String input) {
		return new PieChart(input);
	}

	@Override
	public CyChart<PieLayer> getInstance(final CyChart<PieLayer> chart) {
		return new PieChart((PieChart)chart);
	}
	
	@Override
	public CyChart<PieLayer> getInstance(final Map<String, Object> properties) {
		return new PieChart(properties);
	}

	@Override
	public String getId() {
		return PieChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyChart<PieLayer>> getSupportedClass() {
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
