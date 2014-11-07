package org.cytoscape.ding.internal.charts.pie;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

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
	
	public PieChart(final Map<String, Object> properties, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, properties, colIdFactory);
	}
	
	public PieChart(final PieChart chart, final CyColumnIdentifierFactory colIdFactory) {
		super(chart, colIdFactory);
	}
	
	public PieChart(final String input, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, input, colIdFactory);
	}
	
	@Override
	public List<PieLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final double startAngle = get(START_ANGLE, Double.class, 0.0);
		final Rotation rotation = get(ROTATION, Rotation.class, Rotation.ANTICLOCKWISE);
		final List<String> labels = getItemLabels(network, model);
		
		final Map<String, List<Double>> data = getData(network, model);
		
		final List<Color> colors = getColors(data);
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		final boolean showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		
		final float borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		final Color borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);
		
		final PieLayer layer = new PieLayer(data, labels, showLabels, colors, borderWidth, borderColor, startAngle,
				rotation, bounds);
		
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
	public Map<String, List<Double>> getDataFromColumns(final CyNetwork network, final CyIdentifiable model,
			final List<CyColumnIdentifier> columnNames) {
		final Map<String, List<Double>> data = new HashMap<String, List<Double>>();
		
		// Values from multiple series have to be merged into one single series
		final Map<String, List<Double>> rawData = super.getDataFromColumns(network, model, columnNames);
		final List<Double> allValues = new ArrayList<>();
		
		for (final List<Double> values : rawData.values())
			allValues.addAll(values);
		
		data.put("Values", allValues);
		
		return data;
	}
	
	@Override
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(START_ANGLE)) return Double.class;
		
		return super.getSettingType(key);
	}
}
