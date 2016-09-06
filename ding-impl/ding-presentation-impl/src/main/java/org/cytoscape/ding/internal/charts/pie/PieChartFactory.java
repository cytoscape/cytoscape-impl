package org.cytoscape.ding.internal.charts.pie;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class PieChartFactory  implements CyCustomGraphics2Factory<PieLayer> {

	private final CyServiceRegistrar serviceRegistrar;

	public PieChartFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyCustomGraphics2<PieLayer> getInstance(final String input) {
		return new PieChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<PieLayer> getInstance(final CyCustomGraphics2<PieLayer> chart) {
		return new PieChart((PieChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<PieLayer> getInstance(final Map<String, Object> properties) {
		return new PieChart(properties, serviceRegistrar);
	}

	@Override
	public String getId() {
		return PieChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<PieLayer>> getSupportedClass() {
		return PieChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Pie";
	}
	
	@Override
	public JComponent createEditor(final CyCustomGraphics2<PieLayer> chart) {
		return new PieChartEditor((PieChart)chart, serviceRegistrar);
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
