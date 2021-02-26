package org.cytoscape.cg.internal.charts.ring;

import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.cg.internal.charts.AbstractChartFactory;
import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;

public class RingChartFactory extends AbstractChartFactory<RingLayer> {

	public RingChartFactory(
			Collection<Class<? extends CyIdentifiable>> targetTypes,
			CyServiceRegistrar serviceRegistrar
	) {
		super(targetTypes, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<RingLayer> getInstance(String input) {
		return new RingChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<RingLayer> getInstance(CyCustomGraphics2<RingLayer> chart) {
		return new RingChart((RingChart) chart, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<RingLayer> getInstance(Map<String, Object> properties) {
		return new RingChart(properties, serviceRegistrar);
	}

	@Override
	public String getId() {
		return RingChart.FACTORY_ID;
	}

	@Override
	public Class<? extends CyCustomGraphics2<RingLayer>> getSupportedClass() {
		return RingChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Ring";
	}

	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtil.resizeIcon(RingChart.ICON, width, height);
	}

	@Override
	public JComponent createEditor(CyCustomGraphics2<RingLayer> chart) {
		return new RingChartEditor((RingChart) chart, serviceRegistrar);
	}
}
