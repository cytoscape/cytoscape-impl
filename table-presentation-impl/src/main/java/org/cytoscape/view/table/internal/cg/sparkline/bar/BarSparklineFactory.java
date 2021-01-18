package org.cytoscape.view.table.internal.cg.sparkline.bar;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.presentation.property.table.CellCustomGraphicsFactory;
import org.cytoscape.view.table.internal.util.IconUtil;

public class BarSparklineFactory implements CellCustomGraphicsFactory {
	
	private final CyServiceRegistrar serviceRegistrar;

	public BarSparklineFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public CellCustomGraphics getInstance(String input) {
		return new BarSparkline(input, serviceRegistrar);
	}

	@Override
	public CellCustomGraphics getInstance(CellCustomGraphics chart) {
		return new BarSparkline((BarSparkline) chart, serviceRegistrar);
	}

	@Override
	public CellCustomGraphics getInstance(Map<String, Object> properties) {
		return new BarSparkline(properties, serviceRegistrar);
	}

	@Override
	public String getId() {
		return BarSparkline.FACTORY_ID;
	}

	@Override
	public Class<? extends CellCustomGraphics> getSupportedClass() {
		return BarSparkline.class;
	}

	@Override
	public String getDisplayName() {
		return "Bar";
	}

	@Override
	public Icon getIcon(int width, int height) {
		return IconUtil.resizeIcon(BarSparkline.ICON, width, height);
	}

	@Override
	public JComponent createEditor(CellCustomGraphics chart) {
		return new BarSparklineEditor((BarSparkline) chart, serviceRegistrar);
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}
