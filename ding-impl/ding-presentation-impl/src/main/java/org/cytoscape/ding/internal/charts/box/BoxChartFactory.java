package org.cytoscape.ding.internal.charts.box;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BoxChartFactory implements CyChartFactory<BoxLayer> {
	
	private final CyColumnIdentifierFactory colIdFactory;
	
	public BoxChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public CyChart<BoxLayer> getInstance(final String input) {
		return new BoxChart(input, colIdFactory);
	}

	@Override
	public CyChart<BoxLayer> getInstance(final CyChart<BoxLayer> chart) {
		return new BoxChart((BoxChart)chart, colIdFactory);
	}
	
	@Override
	public CyChart<BoxLayer> getInstance(final Map<String, Object> properties) {
		return new BoxChart(properties, colIdFactory);
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
