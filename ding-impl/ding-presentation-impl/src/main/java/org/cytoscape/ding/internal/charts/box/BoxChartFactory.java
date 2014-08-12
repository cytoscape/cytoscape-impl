package org.cytoscape.ding.internal.charts.box;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BoxChartFactory implements CyCustomGraphics2Factory<BoxLayer> {
	
	private final CyColumnIdentifierFactory colIdFactory;
	
	public BoxChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(final String input) {
		return new BoxChart(input, colIdFactory);
	}

	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(final CyCustomGraphics2<BoxLayer> chart) {
		return new BoxChart((BoxChart)chart, colIdFactory);
	}
	
	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(final Map<String, Object> properties) {
		return new BoxChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return BoxChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<BoxLayer>> getSupportedClass() {
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
