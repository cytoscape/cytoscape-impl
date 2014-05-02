package org.cytoscape.ding.internal.charts.heatmap;

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
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 *
 */
public class HeatMapChart extends AbstractChartCustomGraphics<HeatMapLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.chart.HeatMap";
	
	public static final String CATEGORY_AXIS_VISIBLE = "categoryaxisvisible";
	public static final String RANGE_AXIS_VISIBLE = "rangeaxisvisible";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					HeatMapChart.class.getClassLoader().getResource("images/charts/line-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HeatMapChart() {
		this("");
	}
	
	public HeatMapChart(final HeatMapChart chart) {
		super(chart);
	}
	
	public HeatMapChart(final String input) {
		super("Heat Map Chart", input);
	}

	@Override 
	public List<HeatMapLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<String> dataColumns = new ArrayList<String>(getList(DATA_COLUMNS, String.class));
		final String labelsColumn = get(LABELS_COLUMN, String.class);
		final String colorScheme = get(COLOR_SCHEME, String.class);
		final Map<String, List<Double>> data;
		final List<String> labels = getLabelsFromColumn(network, model, labelsColumn);
		final List<Color> colors;
		final boolean global = get(GLOBAL_RANGE, Boolean.class, true);
		final DoubleRange range = global ? get(RANGE, DoubleRange.class) : null;
		
		if (!dataColumns.isEmpty()) {
			data = getDataFromColumns(network, model, dataColumns, false);
			colors = convertInputToColor(colorScheme, data, false);
		} else {
			data = Collections.emptyMap();
			colors = Collections.emptyList();
		}

		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		final boolean showLabels = get(SHOW_LABELS, Boolean.class, false);
		final boolean showCategoryAxis = get(CATEGORY_AXIS_VISIBLE, Boolean.class, false);
		final boolean showRangeAxis = get(RANGE_AXIS_VISIBLE, Boolean.class, false);
		
		HeatMapLayer layer = new HeatMapLayer(data, labels, showLabels, colors, range, showCategoryAxis, showRangeAxis,
				bounds);
		
		return Collections.singletonList(layer);
	}

	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	protected Class<?> getSettingType(final String key) {
		if (key.equals(RANGE_AXIS_VISIBLE)) return Boolean.class;
		if (key.equals(CATEGORY_AXIS_VISIBLE)) return Boolean.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
}
