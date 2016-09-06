package org.cytoscape.ding.internal.charts.bar;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.ding.customgraphics.Orientation;
import org.cytoscape.ding.internal.charts.AbstractChart;
import org.cytoscape.ding.internal.charts.LabelPosition;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

import com.fasterxml.jackson.databind.module.SimpleModule;

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

public class BarChart extends AbstractChart<BarLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.BarChart";
	public static final String DISPLAY_NAME = "Bar Chart";
	
	public static final String TYPE = "cy_type";
	public static final String SEPARATION = "cy_separation";
	
	public static final double MAX_SEPARATION = 0.5;
	
	public static enum BarChartType { GROUPED, STACKED, HEAT_STRIPS, UP_DOWN };
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					BarChart.class.getClassLoader().getResource("images/charts/bar-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarChart(final Map<String, Object> properties, final CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public BarChart(final BarChart chart, final CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public BarChart(final String input, final CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, input, serviceRegistrar);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override 
	public List<BarLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<String> itemLabels = getItemLabels(network, model);
		List<String> domainLabels =
				getLabelsFromColumn(network, model, get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
		final List<String> rangeLabels =
				getLabelsFromColumn(network, model, get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
		final boolean global = get(GLOBAL_RANGE, Boolean.class, true);
		final List<Double> range = global ? getList(RANGE, Double.class) : null;
		final BarChartType type = get(TYPE, BarChartType.class, BarChartType.GROUPED);
		
		final Map<String, List<Double>> data = getData(network, model);
		
		if (domainLabels.isEmpty() && data.size() == 1 && type != BarChartType.STACKED)
			domainLabels = getSingleValueColumnNames(network, model);
		
		final List<Color> colors = getColors(data);
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		final Orientation orientation = get(ORIENTATION, Orientation.class);
		final boolean showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		final boolean showDomainAxis = get(SHOW_DOMAIN_AXIS, Boolean.class, false);
		final boolean showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		final boolean showRangeZeroBaseline = get(SHOW_RANGE_ZERO_BASELINE, Boolean.class, false);
		final LabelPosition domainLabelPosition = get(DOMAIN_LABEL_POSITION, LabelPosition.class);
		final float axisWidth = get(AXIS_WIDTH, Float.class, 0.25f);
		final Color axisColor = get(AXIS_COLOR, Color.class, Color.DARK_GRAY);
		final float axisFontSize = convertFontSize(get(AXIS_LABEL_FONT_SIZE, Integer.class, 1));
		final float itemFontSize = convertFontSize(get(ITEM_LABEL_FONT_SIZE, Integer.class, 1));
		final float borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		final Color borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);
		
		double separation = get(SEPARATION, Double.class, 0.0);
		separation = (separation > MAX_SEPARATION) ? MAX_SEPARATION : (separation < 0.0 ? 0.0 : separation);
		
		final BarLayer layer = new BarLayer(data, type, itemLabels, domainLabels, rangeLabels, showLabels,
				showDomainAxis, showRangeAxis, showRangeZeroBaseline, itemFontSize, domainLabelPosition, colors,
				axisWidth, axisColor, axisFontSize, borderWidth, borderColor, separation, range, orientation, bounds);

		return Collections.singletonList(layer);
	}

	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
	
	@Override
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(TYPE)) return BarChartType.class;
		if (key.equalsIgnoreCase(SEPARATION)) return Double.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public void addJsonDeserializers(final SimpleModule module) {
		super.addJsonDeserializers(module);
		module.addDeserializer(BarChartType.class, new BarChartTypeJsonDeserializer());
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

}
