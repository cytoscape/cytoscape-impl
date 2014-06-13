package org.cytoscape.ding.internal.charts.donut;

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
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class DonutChart extends AbstractChartCustomGraphics<DonutLayer> {

	public static final String FACTORY_ID = "org.cytoscape.chart.Donut";
	public static final String DISPLAY_NAME = "Donut Chart";
	
	public static final String START_ANGLE = "startangle";
	public static final String HOLE_SIZE = "holesize";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					DonutChart.class.getClassLoader().getResource("images/charts/donut-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DonutChart(final Map<String, Object> properties, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, properties, colIdFactory);
	}
	
	public DonutChart(final DonutChart chart, final CyColumnIdentifierFactory colIdFactory) {
		super(chart, colIdFactory);
	}
	
	public DonutChart(final String input, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, input, colIdFactory);
	}
	
	@Override
	public List<DonutLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<CyColumnIdentifier> dataColumns =
				new ArrayList<CyColumnIdentifier>(getList(DATA_COLUMNS, CyColumnIdentifier.class));
		final CyColumnIdentifier labelsColumn = get(ITEM_LABELS_COLUMN, CyColumnIdentifier.class);
		final List<Color> colors = getList(COLORS, Color.class);
		final double startAngle = get(START_ANGLE, Double.class, 90.0);
		final double hole = get(HOLE_SIZE, Double.class, 0.2);
		final List<String> labels = getLabelsFromColumn(network, model, labelsColumn);
		
		final Map<String, List<Double>> data = getDataFromColumns(network, model, dataColumns);

		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		final boolean showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		
		final DonutLayer layer = new DonutLayer(data, labels, showLabels, colors, startAngle, hole, bounds);
		
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
		if (key.equalsIgnoreCase(HOLE_SIZE)) return Double.class;
		
		return super.getSettingType(key);
	}
}
