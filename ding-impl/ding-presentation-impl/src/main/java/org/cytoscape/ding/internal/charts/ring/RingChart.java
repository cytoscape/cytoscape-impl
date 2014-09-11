package org.cytoscape.ding.internal.charts.ring;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.ding.customgraphics.Rotation;
import org.cytoscape.ding.internal.charts.AbstractChart;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class RingChart extends AbstractChart<RingLayer> {

	public static final String FACTORY_ID = "org.cytoscape.RingChart";
	public static final String DISPLAY_NAME = "Ring Chart";
	
	public static final String START_ANGLE = "startAngle";
	public static final String HOLE_SIZE = "holeSize";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					RingChart.class.getClassLoader().getResource("images/charts/ring-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public RingChart(final Map<String, Object> properties, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, properties, colIdFactory);
	}
	
	public RingChart(final RingChart chart, final CyColumnIdentifierFactory colIdFactory) {
		super(chart, colIdFactory);
	}
	
	public RingChart(final String input, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, input, colIdFactory);
	}
	
	@Override
	public List<RingLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final double startAngle = get(START_ANGLE, Double.class, 0.0);
		final double hole = get(HOLE_SIZE, Double.class, 0.4);
		final Rotation rotation = get(ROTATION, Rotation.class, Rotation.ANTICLOCKWISE);
		final List<String> labels = getItemLabels(network, model);
		
		final Map<String, List<Double>> data = getData(network, model);

		final List<Color> colors = getColors(data);
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		final boolean showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		
		final float borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		final Color borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);
		
		final RingLayer layer = new RingLayer(data, labels, showLabels, colors, borderWidth, borderColor, startAngle,
				hole, rotation, bounds);
		
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
		if (key.equalsIgnoreCase(START_ANGLE)) return Double.class;
		if (key.equalsIgnoreCase(HOLE_SIZE)) return Double.class;
		
		return super.getSettingType(key);
	}
}
