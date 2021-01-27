package org.cytoscape.cg.internal.charts.box;

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
import org.cytoscape.cg.model.Orientation;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

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

	public BoxChart(Map<String, Object> properties, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public BoxChart(BoxChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public BoxChart(String input, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, input, serviceRegistrar);
	}

	@Override 
	public List<BoxLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final boolean global = get(GLOBAL_RANGE, Boolean.class, true);
		final List<Double> range = global ? getList(RANGE, Double.class) : null;
		
		final Map<String, List<Double>> data = getData(network, model);

		final List<Color> colors = getColors(data);
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		final Orientation orientation = get(ORIENTATION, Orientation.class);
		final boolean showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		final boolean showRangeZeroBaseline = get(SHOW_RANGE_ZERO_BASELINE, Boolean.class, false);
		final float axisWidth = get(AXIS_WIDTH, Float.class, 0.25f);
		final Color axisColor = get(AXIS_COLOR, Color.class, Color.DARK_GRAY);
		final float axisFontSize = convertFontSize(get(AXIS_LABEL_FONT_SIZE, Integer.class, 1));
		final float borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		final Color borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);
		
		final BoxLayer layer = new BoxLayer(data, showRangeAxis, showRangeZeroBaseline, colors, axisWidth, axisColor,
				axisFontSize, borderWidth, borderColor, range, orientation, bounds);
		
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
