package org.cytoscape.cg.internal.charts.line;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.charts.ViewUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class LineChartFactory implements CyCustomGraphics2Factory<LineLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public LineChartFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyCustomGraphics2<LineLayer> getInstance(String input) {
		return new LineChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<LineLayer> getInstance(CyCustomGraphics2<LineLayer> chart) {
		return new LineChart((LineChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<LineLayer> getInstance(Map<String, Object> properties) {
		return new LineChart(properties, serviceRegistrar);
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
	public JComponent createEditor(CyCustomGraphics2<LineLayer> chart) {
		return new LineChartEditor((LineChart)chart, serviceRegistrar);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
