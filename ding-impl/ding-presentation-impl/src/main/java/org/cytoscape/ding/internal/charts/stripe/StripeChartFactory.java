package org.cytoscape.ding.internal.charts.stripe;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class StripeChartFactory implements CyCustomGraphics2Factory<StripeLayer> {
	
	private final CyColumnIdentifierFactory colIdFactory;
	
	public StripeChartFactory(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}
	
	@Override
	public CyCustomGraphics2<StripeLayer> getInstance(final String input) {
		return new StripeChart(input, colIdFactory);
	}

	@Override
	public CyCustomGraphics2<StripeLayer> getInstance(final CyCustomGraphics2<StripeLayer> chart) {
		return new StripeChart((StripeChart)chart, colIdFactory);
	}
	
	@Override
	public CyCustomGraphics2<StripeLayer> getInstance(final Map<String, Object> properties) {
		return new StripeChart(properties, colIdFactory);
	}

	@Override
	public String getId() {
		return StripeChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<StripeLayer>> getSupportedClass() {
		return StripeChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Stripe";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(StripeChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
