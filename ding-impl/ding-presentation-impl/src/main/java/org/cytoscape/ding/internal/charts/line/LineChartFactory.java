package org.cytoscape.ding.internal.charts.line;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class LineChartFactory implements CyCustomGraphics2Factory<LineLayer> {
	
	private final CyColumnIdentifierFactory colIdFactory;
	
	public LineChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public CyCustomGraphics2<LineLayer> getInstance(final String input) {
		return new LineChart(input, colIdFactory);
	}

	@Override
	public CyCustomGraphics2<LineLayer> getInstance(final CyCustomGraphics2<LineLayer> chart) {
		return new LineChart((LineChart)chart, colIdFactory);
	}
	
	@Override
	public CyCustomGraphics2<LineLayer> getInstance(final Map<String, Object> properties) {
		return new LineChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return LineChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<LineLayer>> getSupportedClass() {
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
