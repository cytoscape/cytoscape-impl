package org.cytoscape.cg.internal.charts.heatmap;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class HeatMapChartFactory implements CyCustomGraphics2Factory<HeatMapLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public HeatMapChartFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(String input) {
		return new HeatMapChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(CyCustomGraphics2<HeatMapLayer> chart) {
		return new HeatMapChart((HeatMapChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(Map<String, Object> properties) {
		return new HeatMapChart(properties, serviceRegistrar);
	}

	@Override
	public String getId() {
		return HeatMapChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<HeatMapLayer>> getSupportedClass() {
		return HeatMapChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Heat Map";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtil.resizeIcon(HeatMapChart.ICON, width, height);
	}
	
	@Override
	public JComponent createEditor(CyCustomGraphics2<HeatMapLayer> chart) {
		return new HeatMapChartEditor((HeatMapChart)chart, serviceRegistrar);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
