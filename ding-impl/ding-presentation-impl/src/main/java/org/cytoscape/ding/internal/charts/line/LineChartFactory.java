package org.cytoscape.ding.internal.charts.line;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class LineChartFactory implements CyCustomGraphics2Factory<LineLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public LineChartFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyCustomGraphics2<LineLayer> getInstance(final String input) {
		return new LineChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<LineLayer> getInstance(final CyCustomGraphics2<LineLayer> chart) {
		return new LineChart((LineChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<LineLayer> getInstance(final Map<String, Object> properties) {
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
	public JComponent createEditor(final CyCustomGraphics2<LineLayer> chart) {
		return new LineChartEditor((LineChart)chart, serviceRegistrar);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
