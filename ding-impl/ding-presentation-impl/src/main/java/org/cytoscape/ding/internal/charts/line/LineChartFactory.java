package org.cytoscape.ding.internal.charts.line;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;

public class LineChartFactory implements CyChartFactory<LineLayer> {
	
	@Override
	public CyChart<LineLayer> getInstance(final String input) {
		return new LineChart(input);
	}

	@Override
	public CyChart<LineLayer> getInstance(final CyChart<LineLayer> chart) {
		return new LineChart((LineChart)chart);
	}
	
	@Override
	public CyChart<LineLayer> getInstance() {
		return new LineChart();
	}

	@Override
	public String getId() {
		return LineChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyChart<LineLayer>> getSupportedClass() {
		return LineChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Line";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(LineChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
