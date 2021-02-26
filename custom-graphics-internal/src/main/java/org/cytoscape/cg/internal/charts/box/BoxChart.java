package org.cytoscape.cg.internal.charts.box;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.cg.internal.charts.AbstractChart;
import org.cytoscape.cg.model.Orientation;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;

public class BoxChart extends AbstractChart<BoxLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.BoxChart";
	public static final String DISPLAY_NAME = "Box Chart";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					BoxChart.class.getClassLoader().getResource("images/charts/box-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BoxChart(Map<String, Object> properties, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public BoxChart(BoxChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public BoxChart(String input, CyServiceRegistrar serviceRegistrar) {
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
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected BoxLayer getLayer(CyRow row) {
		var global = get(GLOBAL_RANGE, Boolean.class, true);
		var range = global ? getList(RANGE, Double.class) : null;
		
		var data = getData(row);
		var colors = getColors(data);
		
		var orientation = get(ORIENTATION, Orientation.class);
		var showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		var showRangeZeroBaseline = get(SHOW_RANGE_ZERO_BASELINE, Boolean.class, false);
		var axisWidth = get(AXIS_WIDTH, Float.class, 0.25f);
		var axisColor = get(AXIS_COLOR, Color.class, Color.DARK_GRAY);
		var axisFontSize = convertFontSize(get(AXIS_LABEL_FONT_SIZE, Integer.class, 1));
		var borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		var borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);
		
		var layer = new BoxLayer(data, showRangeAxis, showRangeZeroBaseline, colors, axisWidth, axisColor, axisFontSize,
				borderWidth, borderColor, range, orientation);
		
		return layer;
	}
}
