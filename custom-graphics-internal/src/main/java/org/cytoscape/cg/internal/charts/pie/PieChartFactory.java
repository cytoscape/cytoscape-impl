package org.cytoscape.cg.internal.charts.pie;

import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.charts.AbstractChartFactory;
import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;

public class PieChartFactory extends AbstractChartFactory<PieLayer> {

	public PieChartFactory(
			Collection<Class<? extends CyIdentifiable>> targetTypes,
			CyServiceRegistrar serviceRegistrar
	) {
		super(targetTypes, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<PieLayer> getInstance(String input) {
		return new PieChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<PieLayer> getInstance(CyCustomGraphics2<PieLayer> chart) {
		return new PieChart((PieChart) chart, serviceRegistrar);
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
		return new PieChartEditor((PieChart) chart, serviceRegistrar);
	}

	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtil.resizeIcon(PieChart.ICON, width, height);
	}
}
