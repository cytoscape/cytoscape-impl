package org.cytoscape.cg.internal.charts.line;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.cg.internal.charts.AbstractChart;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

public class LineChart extends AbstractChart<LineLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.LineChart";
	public static final String DISPLAY_NAME = "Line Chart";
	
	
	public static final String LINE_WIDTH ="cy_lineWidth";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					LineChart.class.getClassLoader().getResource("images/charts/line-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// ==[ CONSTRUCTORS ]===============================================================================================

	public LineChart(Map<String, Object> properties, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public LineChart(LineChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public LineChart(String input, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, input, serviceRegistrar);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	public Class<?> getSettingType(String key) {
		if (key.equalsIgnoreCase(LINE_WIDTH)) return Float.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	@Override
	protected LineLayer getLayer(CyRow row) {
		var itemLabels = getItemLabels(row);
		var domainLabels = getLabelsFromColumn(row, get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
		var rangeLabels = getLabelsFromColumn(row, get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
		var global = get(GLOBAL_RANGE, Boolean.class, true);
		var range = global ? getList(RANGE, Double.class) : null;

		var data = getData(row);

		if (domainLabels.isEmpty() && data.size() == 1)
			domainLabels = getSingleValueColumnNames(row);

		var colors = getColors(data);
		final double size = 32;
		var bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);

		var showItemLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		var showDomainAxis = get(SHOW_DOMAIN_AXIS, Boolean.class, false);
		var showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		var showRangeZeroBaseline = get(SHOW_RANGE_ZERO_BASELINE, Boolean.class, false);
		var domainLabelPosition = get(DOMAIN_LABEL_POSITION, LabelPosition.class);
		var axisWidth = get(AXIS_WIDTH, Float.class, 0.25f);
		var axisColor = get(AXIS_COLOR, Color.class, Color.DARK_GRAY);
		var axisFontSize = convertFontSize(get(AXIS_LABEL_FONT_SIZE, Integer.class, 1));
		var itemFontSize = convertFontSize(get(ITEM_LABEL_FONT_SIZE, Integer.class, 1));
		var lineWidth = get(LINE_WIDTH, Float.class, 1.0f);

		var layer = new LineLayer(data, itemLabels, domainLabels, rangeLabels, showItemLabels, showDomainAxis,
				showRangeAxis, showRangeZeroBaseline, itemFontSize, domainLabelPosition, colors, axisWidth, axisColor,
				axisFontSize, range, lineWidth, bounds);

		return layer;
	}
}
