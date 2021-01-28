package org.cytoscape.cg.internal.charts.bar;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.cg.internal.charts.AbstractChart;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.model.Orientation;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

import com.fasterxml.jackson.databind.module.SimpleModule;

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
	
	public BarChart(Map<String, Object> properties, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public BarChart(BarChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public BarChart(String input, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, input, serviceRegistrar);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
	
	@Override
	public Class<?> getSettingType(String key) {
		if (key.equalsIgnoreCase(TYPE)) return BarChartType.class;
		if (key.equalsIgnoreCase(SEPARATION)) return Double.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public void addJsonDeserializers(SimpleModule module) {
		super.addJsonDeserializers(module);
		module.addDeserializer(BarChartType.class, new BarChartTypeJsonDeserializer());
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected BarLayer getLayer(CyRow row) {
		var itemLabels = getItemLabels(row);
		var domainLabels = getLabelsFromColumn(row, get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
		var rangeLabels = getLabelsFromColumn(row, get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
		var global = get(GLOBAL_RANGE, Boolean.class, true);
		var range = global ? getList(RANGE, Double.class) : null;
		var type = get(TYPE, BarChartType.class, BarChartType.GROUPED);

		var data = getData(row);

		if (domainLabels.isEmpty() && data.size() == 1 && type != BarChartType.STACKED)
			domainLabels = getSingleValueColumnNames(row);

		var colors = getColors(data);
		final double size = 32;
		var bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);

		var orientation = get(ORIENTATION, Orientation.class);
		var showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		var showDomainAxis = get(SHOW_DOMAIN_AXIS, Boolean.class, false);
		var showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		var showRangeZeroBaseline = get(SHOW_RANGE_ZERO_BASELINE, Boolean.class, false);
		var domainLabelPosition = get(DOMAIN_LABEL_POSITION, LabelPosition.class);
		var axisWidth = get(AXIS_WIDTH, Float.class, 0.25f);
		var axisColor = get(AXIS_COLOR, Color.class, Color.DARK_GRAY);
		var axisFontSize = convertFontSize(get(AXIS_LABEL_FONT_SIZE, Integer.class, 1));
		var itemFontSize = convertFontSize(get(ITEM_LABEL_FONT_SIZE, Integer.class, 1));
		var borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		var borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);

		var separation = get(SEPARATION, Double.class, 0.0);
		separation = (separation > MAX_SEPARATION) ? MAX_SEPARATION : (separation < 0.0 ? 0.0 : separation);

		var layer = new BarLayer(data, type, itemLabels, domainLabels, rangeLabels, showLabels, showDomainAxis,
				showRangeAxis, showRangeZeroBaseline, itemFontSize, domainLabelPosition, colors, axisWidth, axisColor,
				axisFontSize, borderWidth, borderColor, separation, range, orientation, bounds);

		return layer;
	}
}
