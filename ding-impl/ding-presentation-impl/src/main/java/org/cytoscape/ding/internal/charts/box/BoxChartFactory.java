package org.cytoscape.ding.internal.charts.box;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;

public class BoxChartFactory implements CyChartFactory<BoxLayer> {
	
	@Override
	public CyChart<BoxLayer> getInstance(final String input) {
		return new BoxChart(input);
	}

	@Override
	public CyChart<BoxLayer> getInstance(final CyChart<BoxLayer> chart) {
		return new BoxChart((BoxChart)chart);
	}
	
	@Override
	public CyChart<BoxLayer> getInstance() {
		return new BoxChart();
	}

	@Override
	public String getId() {
		return BoxChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyChart<BoxLayer>> getSupportedClass() {
		return BoxChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Box";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(BoxChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
