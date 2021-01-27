package org.cytoscape.cg.internal.charts.heatmap;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.cg.internal.charts.AbstractChart;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.model.Orientation;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

public class HeatMapChart extends AbstractChart<HeatMapLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.HeatMapChart";
	public static final String DISPLAY_NAME = "Heat Map Chart";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					HeatMapChart.class.getClassLoader().getResource("images/charts/heatmap-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HeatMapChart(Map<String, Object> properties, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public HeatMapChart(HeatMapChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public HeatMapChart(String input, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, input, serviceRegistrar);
	}

	@Override 
	public List<HeatMapLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> view) {
		var network = networkView.getModel();
		var model = view.getModel();

		var itemLabels = getLabelsFromColumn(network, model, get(ITEM_LABELS_COLUMN, CyColumnIdentifier.class));
		var domainLabels = getLabelsFromColumn(network, model, get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
		var rangeLabels = getLabelsFromColumn(network, model, get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
		var global = get(GLOBAL_RANGE, Boolean.class, true);
		var range = global ? getList(RANGE, Double.class) : null;

		var data = getData(network, model);

		var colors = getColors(data);
		var size = 32.0;
		var bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);

		var showDomainAxis = get(SHOW_DOMAIN_AXIS, Boolean.class, false);
		var showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		var domainLabelPosition = get(DOMAIN_LABEL_POSITION, LabelPosition.class);
		var axisColor = get(AXIS_COLOR, Color.class, Color.DARK_GRAY);
		var axisFontSize = (float) convertFontSize(get(AXIS_LABEL_FONT_SIZE, Integer.class, 1));
		var orientation = get(ORIENTATION, Orientation.class);

		var layer = new HeatMapLayer(data, itemLabels, domainLabels, rangeLabels, showDomainAxis, showRangeAxis,
				domainLabelPosition, colors, axisColor, axisFontSize, range, orientation, bounds);

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
}
