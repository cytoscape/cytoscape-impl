package org.cytoscape.cg.internal.charts.ring;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.cg.internal.charts.AbstractChart;
import org.cytoscape.cg.model.Rotation;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;

public class RingChart extends AbstractChart<RingLayer> {

	public static final String FACTORY_ID = "org.cytoscape.RingChart";
	public static final String DISPLAY_NAME = "Ring Chart";
	
	public static final String START_ANGLE = "cy_startAngle";
	public static final String HOLE_SIZE = "cy_holeSize";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					RingChart.class.getClassLoader().getResource("images/charts/ring-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RingChart(Map<String, Object> properties, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public RingChart(RingChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public RingChart(String input, CyServiceRegistrar serviceRegistrar) {
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
		if (key.equalsIgnoreCase(START_ANGLE)) return Double.class;
		if (key.equalsIgnoreCase(HOLE_SIZE)) return Double.class;
		
		return super.getSettingType(key);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	@Override
	protected RingLayer getLayer(CyRow row) {
		var startAngle = get(START_ANGLE, Double.class, 0.0);
		var hole = get(HOLE_SIZE, Double.class, 0.4);
		var rotation = get(ROTATION, Rotation.class, Rotation.ANTICLOCKWISE);
		var labels = getItemLabels(row);

		var data = getData(row);
		var colors = getColors(data);
		
		var showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		var itemFontSize = convertFontSize(get(ITEM_LABEL_FONT_SIZE, Integer.class, 1));
		var borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		var borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);

		var layer = new RingLayer(data, labels, showLabels, itemFontSize, colors, borderWidth, borderColor, startAngle,
				hole, rotation);

		return layer;
	}
}
