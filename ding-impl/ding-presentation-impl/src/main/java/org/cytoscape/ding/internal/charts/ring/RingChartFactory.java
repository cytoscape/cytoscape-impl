package org.cytoscape.ding.internal.charts.ring;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class RingChartFactory implements CyCustomGraphics2Factory<RingLayer> {

	private final CyColumnIdentifierFactory colIdFactory;
	
	public RingChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public CyCustomGraphics2<RingLayer> getInstance(final String input) {
		return new RingChart(input, colIdFactory);
	}

	@Override
	public CyCustomGraphics2<RingLayer> getInstance(final CyCustomGraphics2<RingLayer> chart) {
		return new RingChart((RingChart)chart, colIdFactory);
	}
	
	@Override
	public CyCustomGraphics2<RingLayer> getInstance(final Map<String, Object> properties) {
		return new RingChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return RingChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<RingLayer>> getSupportedClass() {
		return RingChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Ring";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(RingChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
