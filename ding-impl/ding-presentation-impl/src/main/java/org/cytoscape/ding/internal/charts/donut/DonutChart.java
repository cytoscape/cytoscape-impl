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

public class DonutChart extends AbstractChartCustomGraphics<DonutLayer> {

	public static final String FACTORY_ID = "org.cytoscape.chart.Donut";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					DonutChart.class.getClassLoader().getResource("images/charts/donut-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DonutChart() {
		this("");
	}
	
	public DonutChart(final DonutChart chart) {
		super(chart);
	}
	
	public DonutChart(final String input) {
		super("Donut Chart", input);
	}
	
	@Override
	public List<DonutLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<String> dataColumns = new ArrayList<String>(getList(DATA_COLUMNS, String.class));
		final String labelsColumn = get(ITEM_LABELS_COLUMN, String.class);
		final String colorScheme = get(COLOR_SCHEME, String.class);
		final Map<String, List<Double>> data;
		final List<String> labels = getLabelsFromColumn(network, model, labelsColumn);
		final List<Color> colors;
		
		if (!dataColumns.isEmpty()) {
			data = getDataFromColumns(network, model, dataColumns, false);
			colors = convertInputToColor(colorScheme, data, false);
		} else {
			data = Collections.emptyMap();
			colors = Collections.emptyList();
		}

		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		final boolean showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		
		final DonutLayer layer = new DonutLayer(data, labels, showLabels, colors, bounds);
		
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
