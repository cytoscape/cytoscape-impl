package org.cytoscape.ding.internal.charts.heatmap;

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

public class HeatMapChartFactory implements CyCustomGraphics2Factory<HeatMapLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public HeatMapChartFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(final String input) {
		return new HeatMapChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(final CyCustomGraphics2<HeatMapLayer> chart) {
		return new HeatMapChart((HeatMapChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<HeatMapLayer> getInstance(final Map<String, Object> properties) {
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
		return ViewUtils.resizeIcon(HeatMapChart.ICON, width, height);
	}
	
	@Override
	public JComponent createEditor(final CyCustomGraphics2<HeatMapLayer> chart) {
		return new HeatMapChartEditor((HeatMapChart)chart, serviceRegistrar);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
