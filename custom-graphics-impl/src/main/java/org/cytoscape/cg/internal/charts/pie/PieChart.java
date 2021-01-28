package org.cytoscape.cg.internal.charts.pie;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.cg.internal.charts.AbstractChart;
import org.cytoscape.cg.model.Rotation;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

public class PieChart extends AbstractChart<PieLayer> {

	public static final String FACTORY_ID = "org.cytoscape.PieChart";
	public static final String DISPLAY_NAME = "Pie Chart";
	
	public static final String START_ANGLE = "cy_startAngle";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					PieChart.class.getClassLoader().getResource("images/charts/pie-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public PieChart(Map<String, Object> properties, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public PieChart(PieChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public PieChart(String input, CyServiceRegistrar serviceRegistrar) {
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
	public Map<String, List<Double>> getDataFromColumns(CyRow row, List<CyColumnIdentifier> columnNames) {
		var data = new HashMap<String, List<Double>>();

		// Values from multiple series have to be merged into one single series
		var rawData = super.getDataFromColumns(row, columnNames);
		var allValues = new ArrayList<Double>();

		for (var values : rawData.values())
			allValues.addAll(values);

		data.put("Values", allValues);

		return data;
	}
	
	@Override
	public Class<?> getSettingType(String key) {
		if (key.equalsIgnoreCase(START_ANGLE)) return Double.class;
		
		return super.getSettingType(key);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	@Override
	protected PieLayer getLayer(CyRow row) {
		var startAngle = get(START_ANGLE, Double.class, 0.0);
		var rotation = get(ROTATION, Rotation.class, Rotation.ANTICLOCKWISE);
		var labels = getItemLabels(row);

		var data = getData(row);

		var colors = getColors(data);
		final double size = 32;
		var bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		var showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		var itemFontSize = convertFontSize(get(ITEM_LABEL_FONT_SIZE, Integer.class, 1));
		var borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		var borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);

		var layer = new PieLayer(data, labels, showLabels, itemFontSize, colors, borderWidth, borderColor, startAngle,
				rotation, bounds);

		return layer;
	}
}
