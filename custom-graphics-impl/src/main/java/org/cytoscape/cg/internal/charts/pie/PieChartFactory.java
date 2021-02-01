package org.cytoscape.cg.internal.charts.pie;

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

public class PieChartFactory implements CyCustomGraphics2Factory<PieLayer> {

	private final CyServiceRegistrar serviceRegistrar;

	public PieChartFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyCustomGraphics2<PieLayer> getInstance(String input) {
		return new PieChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<PieLayer> getInstance(CyCustomGraphics2<PieLayer> chart) {
		return new PieChart((PieChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<PieLayer> getInstance(Map<String, Object> properties) {
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
	public JComponent createEditor(CyCustomGraphics2<PieLayer> chart) {
		return new PieChartEditor((PieChart)chart, serviceRegistrar);
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtil.resizeIcon(PieChart.ICON, width, height);
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
