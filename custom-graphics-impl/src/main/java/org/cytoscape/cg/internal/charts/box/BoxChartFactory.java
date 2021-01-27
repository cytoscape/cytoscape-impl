package org.cytoscape.cg.internal.charts.box;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.charts.ViewUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class BoxChartFactory implements CyCustomGraphics2Factory<BoxLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public BoxChartFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(String input) {
		return new BoxChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(CyCustomGraphics2<BoxLayer> chart) {
		return new BoxChart((BoxChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(Map<String, Object> properties) {
		return new BoxChart(properties, serviceRegistrar);
	}

	@Override
	public String getId() {
		return BoxChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<BoxLayer>> getSupportedClass() {
		return BoxChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Box";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(BoxChart.ICON, width, height);
	}
	
	@Override
	public JComponent createEditor(CyCustomGraphics2<BoxLayer> chart) {
		return new BoxChartEditor((BoxChart)chart, serviceRegistrar);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
