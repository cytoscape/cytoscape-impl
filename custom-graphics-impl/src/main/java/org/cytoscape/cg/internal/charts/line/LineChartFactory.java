package org.cytoscape.cg.internal.charts.line;

import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.charts.AbstractChartFactory;
import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;

public class LineChartFactory extends AbstractChartFactory<LineLayer> {
	
	public LineChartFactory(
			Collection<Class<? extends CyIdentifiable>> targetTypes,
			CyServiceRegistrar serviceRegistrar
	) {
		super(targetTypes, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<LineLayer> getInstance(String input) {
		return new LineChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<LineLayer> getInstance(CyCustomGraphics2<LineLayer> chart) {
		return new LineChart((LineChart) chart, serviceRegistrar);
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
		return ViewUtil.resizeIcon(LineChart.ICON, width, height);
	}

	@Override
	public JComponent createEditor(CyCustomGraphics2<LineLayer> chart) {
		return new LineChartEditor((LineChart) chart, serviceRegistrar);
	}
}
