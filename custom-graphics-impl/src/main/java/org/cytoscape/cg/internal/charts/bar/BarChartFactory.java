package org.cytoscape.cg.internal.charts.bar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class BarChartFactory implements CyCustomGraphics2Factory<BarLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public BarChartFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public CyCustomGraphics2<BarLayer> getInstance(String input) {
		return new BarChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<BarLayer> getInstance(CyCustomGraphics2<BarLayer> chart) {
		return new BarChart((BarChart)chart, serviceRegistrar);
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
		return new BarChartEditor((BarChart)chart, serviceRegistrar);
	}
	
	@Override
	public Set<Class<? extends CyIdentifiable>> getSupportedTargetTypes() {
		return new HashSet<>(Arrays.asList(CyNode.class, CyColumn.class));
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
