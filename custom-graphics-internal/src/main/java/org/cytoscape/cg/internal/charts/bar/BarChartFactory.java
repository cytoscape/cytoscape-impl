package org.cytoscape.cg.internal.charts.bar;

import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.charts.AbstractChartFactory;
import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;

public class BarChartFactory extends AbstractChartFactory<BarLayer> {
	
	public BarChartFactory(
			Collection<Class<? extends CyIdentifiable>> targetTypes,
			CyServiceRegistrar serviceRegistrar
	) {
		super(targetTypes, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<BarLayer> getInstance(String input) {
		return new BarChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<BarLayer> getInstance(CyCustomGraphics2<BarLayer> chart) {
		return new BarChart((BarChart) chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<BarLayer> getInstance(Map<String, Object> properties) {
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
		return ViewUtil.resizeIcon(BarChart.ICON, width, height);
	}
	
	@Override
	public JComponent createEditor(CyCustomGraphics2<BarLayer> chart) {
		return new BarChartEditor((BarChart) chart, serviceRegistrar);
	}
}
