package org.cytoscape.cg.internal.charts.bar;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.charts.ViewUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class BarChartFactory implements CyCustomGraphics2Factory<BarLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public BarChartFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public CyCustomGraphics2<BarLayer> getInstance(final String input) {
		return new BarChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<BarLayer> getInstance(final CyCustomGraphics2<BarLayer> chart) {
		return new BarChart((BarChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<BarLayer> getInstance(final Map<String, Object> properties) {
		return new BarChart(properties, serviceRegistrar);
	}

	@Override
	public String getId() {
		return BarChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<BarLayer>> getSupportedClass() {
		return BarChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Bar";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(BarChart.ICON, width, height);
	}
	
	@Override
	public JComponent createEditor(final CyCustomGraphics2<BarLayer> chart) {
		return new BarChartEditor((BarChart)chart, serviceRegistrar);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
