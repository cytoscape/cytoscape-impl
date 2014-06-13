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
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

/**
 *
 */
public class HeatMapChart extends AbstractChartCustomGraphics<HeatMapLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.chart.HeatMap";
	public static final String DISPLAY_NAME = "Heat Map Chart";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					HeatMapChart.class.getClassLoader().getResource("images/charts/line-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HeatMapChart(final Map<String, Object> properties, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, properties, colIdFactory);
	}
	
	public HeatMapChart(final HeatMapChart chart, final CyColumnIdentifierFactory colIdFactory) {
		super(chart, colIdFactory);
	}
	
	public HeatMapChart(final String input, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, input, colIdFactory);
	}

	@Override 
	public List<HeatMapLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<CyColumnIdentifier> dataColumns =
				new ArrayList<CyColumnIdentifier>(getList(DATA_COLUMNS, CyColumnIdentifier.class));
		final List<String> itemLabels =
				getLabelsFromColumn(network, model, get(ITEM_LABELS_COLUMN, CyColumnIdentifier.class));
		final List<String> domainLabels =
				getLabelsFromColumn(network, model, get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
		final List<String> rangeLabels =
				getLabelsFromColumn(network, model, get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
		final List<Color> colors = getList(COLORS, Color.class);
		final boolean global = get(GLOBAL_RANGE, Boolean.class, true);
		final DoubleRange range = global ? get(RANGE, DoubleRange.class) : null;
		
		final Map<String, List<Double>> data = getDataFromColumns(network, model, dataColumns);

		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		final boolean showDomainAxis = get(SHOW_DOMAIN_AXIS, Boolean.class, false);
		final boolean showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		
		final HeatMapLayer layer = new HeatMapLayer(data, itemLabels, domainLabels, rangeLabels,
				showDomainAxis, showRangeAxis, colors, range, bounds);
		
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
