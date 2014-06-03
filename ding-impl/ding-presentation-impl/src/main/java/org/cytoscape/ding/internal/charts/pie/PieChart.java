package org.cytoscape.ding.internal.charts.pie;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.ding.internal.charts.AbstractChartCustomGraphics;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

public class PieChart extends AbstractChartCustomGraphics<PieLayer> {

	public static final String FACTORY_ID = "org.cytoscape.chart.Pie";
	public static final String DISPLAY_NAME = "Pie Chart";
	
	public static final String START_ANGLE = "startangle";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					PieChart.class.getClassLoader().getResource("images/charts/pie-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PieChart(final Map<String, Object> properties) {
		super(DISPLAY_NAME, properties);
	}
	
	public PieChart(final PieChart chart) {
		super(chart);
	}
	
	public PieChart(final String input) {
		super(DISPLAY_NAME, input);
	}
	
	@Override
	public List<PieLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<String> dataColumns = new ArrayList<String>(getList(DATA_COLUMNS, String.class));
		final String labelsColumn = get(ITEM_LABELS_COLUMN, String.class);
		final List<Color> colors = getList(COLORS, Color.class);
		final double startAngle = get(START_ANGLE, Double.class, 90.0);
		final List<String> labels = getLabelsFromColumn(network, model, labelsColumn);
		
		final Map<String, List<Double>> data = getDataFromColumns(network, model, dataColumns);
		
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		final boolean showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		
		final PieLayer layer = new PieLayer(data, labels, showLabels, colors, startAngle, bounds);
		
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
	protected Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(START_ANGLE)) return Double.class;
		
		return super.getSettingType(key);
	}
}
