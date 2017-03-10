package org.cytoscape.ding.internal.charts.box;

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

public class BoxChartFactory implements CyCustomGraphics2Factory<BoxLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public BoxChartFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(final String input) {
		return new BoxChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(final CyCustomGraphics2<BoxLayer> chart) {
		return new BoxChart((BoxChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<BoxLayer> getInstance(final Map<String, Object> properties) {
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
	public JComponent createEditor(final CyCustomGraphics2<BoxLayer> chart) {
		return new BoxChartEditor((BoxChart)chart, serviceRegistrar);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
