package org.cytoscape.ding.internal.charts.stripe;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;

public class StripeChartFactory implements CyChartFactory<StripeLayer> {
	
	@Override
	public CyChart<StripeLayer> getInstance(final String input) {
		return new StripeChart(input);
	}

	@Override
	public CyChart<StripeLayer> getInstance(final CyChart<StripeLayer> chart) {
		return new StripeChart((StripeChart)chart);
	}
	
	@Override
	public CyChart<StripeLayer> getInstance(final Map<String, Object> properties) {
		return new StripeChart(properties);
	}

	@Override
	public String getId() {
		return StripeChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyChart<StripeLayer>> getSupportedClass() {
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
