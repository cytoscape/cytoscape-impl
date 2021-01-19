package org.cytoscape.view.table.internal.cg.sparkline;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.presentation.property.values.MappableVisualPropertyValue;
import org.cytoscape.view.table.internal.cg.AbstractCellCustomGraphics;
import org.cytoscape.view.table.internal.cg.ColorScheme;
import org.cytoscape.view.table.internal.cg.json.CyColumnIdentifierJsonDeserializer;
import org.cytoscape.view.table.internal.cg.json.CyColumnIdentifierJsonSerializer;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class AbstractSparkline<T extends Dataset> extends AbstractCellCustomGraphics
		implements CellSparkline, MappableVisualPropertyValue {

	public static final String DATA_COLUMNS = "cy_dataColumns";
	public static final String GLOBAL_RANGE = "cy_globalRange";
	public static final String AUTO_RANGE = "cy_autoRange";
	public static final String RANGE = "cy_range";
	public static final String VALUES = "cy_values";
	
	public static final int MAX_IMG_RESOLUTION = 3145728;
	public static final Color TRANSPARENT_COLOR = new Color(0x00, 0x00, 0x00, 0);
	
	/** Divisor which should be applied to chart lines so they have the same thickness as Cytoscape lines */
	public static final Color DEFAULT_ITEM_BG_COLOR = Color.LIGHT_GRAY;
	
	/** Category ID -> list of values */
	protected Map<String, List<Double>> data;
	protected List<Color> colors;
	protected List<Double> range;
	protected boolean global;
	
	protected Rectangle2D bounds;
	protected Rectangle2D scaledBounds;
	
	private boolean dirty = true;
	
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected AbstractSparkline(String displayName, CyServiceRegistrar serviceRegistrar) {
		super(displayName);
		
		if (serviceRegistrar == null)
			throw new IllegalArgumentException("'serviceRegistrar' must not be null.");
		
		this.serviceRegistrar = serviceRegistrar;
	}
	
	protected AbstractSparkline(String displayName, String input, CyServiceRegistrar serviceRegistrar) {
		this(displayName, serviceRegistrar);
		addProperties(parseInput(input));
	}
	
	protected AbstractSparkline(AbstractSparkline<?> chart, CyServiceRegistrar serviceRegistrar) {
		this(chart.getDisplayName(), serviceRegistrar);
		addProperties(chart.getProperties());
	}
	
	protected AbstractSparkline(String displayName, Map<String, Object> properties,
			CyServiceRegistrar serviceRegistrar) {
		this(displayName, serviceRegistrar);
		addProperties(properties);
	}
	
	@Override
	public Set<CyColumnIdentifier> getMappedColumns() {
		var set = new HashSet<CyColumnIdentifier>();
		set.addAll(getList(DATA_COLUMNS, CyColumnIdentifier.class));
		
		return set;
	}
	
	@Override
	public void update() {
		global = get(GLOBAL_RANGE, Boolean.class, true);
		range = global ? getList(RANGE, Double.class) : null;
	}
	
	protected boolean isDirty() {
		return dirty;
	}
	
	public Map<String, List<Double>> getDataFromColumns(CyRow row, List<CyColumnIdentifier> columnNames) {
		var data = new LinkedHashMap<String, List<Double>>();
		
		var table = row.getTable();
		var singleSeriesValues = new ArrayList<Double>();
		var singleSeriesKey = new StringBuilder();
		int singleSeriesIndex = -1;
		int count = 0;

		for (var colId : columnNames) {
			var column = table.getColumn(colId.getColumnName());
			
			if (column == null)
				continue;
			
			var colName = column.getName();
			var values = new ArrayList<Double>();
			
			if (column.getType() == List.class) {
				// List Column: One column = one data series
				var type = column.getListElementType();
				
				if (type == Double.class) {
					var list = row.getList(colName, Double.class);
					
					if (list != null)
						values.addAll(list);
				} else if (type == Integer.class) {
					var list = row.getList(colName, Integer.class);
					
					if (list != null) {
						for (Integer i : list)
							values.add(i.doubleValue());
					}
				} else if (type == Long.class) {
					var list = row.getList(colName, Long.class);
					
					if (list != null) {
						for (Long l : list)
							values.add(l.doubleValue());
					}
				} else if (type == Float.class) {
					var list = row.getList(colName, Float.class);
					
					if (list != null) {
						for (Float f : list)
							values.add(f.doubleValue());
					}
				}
				
				data.put(colName, values);
			} else {
				// Single Column: All single columns together make only one data series
				var type = column.getType();
				
				if (Number.class.isAssignableFrom(type)) {
					if (!row.isSet(colName)) {
						singleSeriesValues.add(Double.NaN);
					} else if (type == Double.class) {
						singleSeriesValues.add(row.get(colName, Double.class));
					} else if (type == Integer.class) {
						Integer i = row.get(colName, Integer.class);
						singleSeriesValues.add(i.doubleValue());
					} else if (type == Float.class) {
						Float f = row.get(colName, Float.class);
						singleSeriesValues.add(f.doubleValue());
					}
					
					singleSeriesKey.append(colName + ",");
					
					// The index of this data series is the index of the first single column
					if (singleSeriesIndex == -1)
						singleSeriesIndex = count;
				}
			}
			
			count++;
		}
		
		if (!singleSeriesValues.isEmpty()) {
			singleSeriesKey.deleteCharAt(singleSeriesKey.length() - 1);
			
			// To add the series of single columns into the correct position, we have to rebuild the data map
			var entrySet = data.entrySet();
			data = new LinkedHashMap<>();
			int i = 0;
			
			for (var entry : entrySet) {
				if (i == singleSeriesIndex)
					data.put(singleSeriesKey.toString(), singleSeriesValues);
				
				data.put(entry.getKey(), entry.getValue());
				i++;
			}
			
			if (!data.containsKey(singleSeriesKey.toString())) // (entrySet.isEmpty() || i >= entrySet.size())
				data.put(singleSeriesKey.toString(), singleSeriesValues);
		}

		return data;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLabelsFromColumn(CyNetwork network, CyIdentifiable model,
			CyColumnIdentifier columnId) {
		var labels = new ArrayList<String>();
		var row = network.getRow(model);
		
		if (row != null && columnId != null) {
			var table = row.getTable();
			var column = table.getColumn(columnId.getColumnName());
			
			if (column != null && column.getType() == List.class) {
				var type = column.getListElementType();
				var values = row.getList(columnId.getColumnName(), type);
				
				if (type == String.class) {
					labels.addAll((List<String>) values);
				} else {
					for (Object obj : values)
						labels.add(obj.toString());
				}
			}
		}
		
		return labels;
	}
	
	/**
	 * @return The names of the data columns or an empty list if any of the data columns is of type List.
	 */
	protected List<String> getSingleValueColumnNames(CyNetwork network, CyIdentifiable model) {
		var names = new ArrayList<String>();
		var row = network.getRow(model);
		
		if (row == null)
			return names;
		
		var dataColumns = getList(DATA_COLUMNS, CyColumnIdentifier.class);
		var table = row.getTable();
		
		boolean invalid = false;
		
		for (var colId : dataColumns) {
			var column = table.getColumn(colId.getColumnName());
			
			if (column == null || column.getType() == List.class) {
				// Not a single value column!
				invalid = true;
				break;
			}
			
			names.add(colId.getColumnName());
		}
		
		if (invalid)
			names.clear();
		
		return names;
	}

	protected Map<String, List<Double>> getData(CyRow row) {
		final Map<String, List<Double>> data;
		var values = getList(VALUES, Double.class);
		
		if (values == null || values.isEmpty()) {
			var dataColumns = getList(DATA_COLUMNS, CyColumnIdentifier.class);
			data = getDataFromColumns(row, dataColumns);
		} else {
			data = new HashMap<String, List<Double>>();
			data.put("Values", values);
		}
		
		return data;
	}
	
	protected List<Color> getColors(Map<String, List<Double>> data) {
		var colors = getList(COLORS, Color.class);
		
		if (colors == null || colors.isEmpty()) {
			var scheme = get(COLOR_SCHEME, ColorScheme.class);
			
			if (scheme != null && data != null && !data.isEmpty()) {
				int nColors = 0;
				
				for (var values : data.values()) {
					if (values != null)
						nColors = Math.max(nColors, values.size());
				}
				
				colors = scheme.getColors(nColors);
			}
		}
		
		return colors;
	}

	@Override
	public Class<?> getSettingType(String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return List.class;
		if (key.equalsIgnoreCase(VALUES)) return List.class;
		if (key.equalsIgnoreCase(GLOBAL_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(AUTO_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(RANGE)) return List.class;
			
		return super.getSettingType(key);
	}
	
	@Override
	public Class<?> getSettingElementType(String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(VALUES)) return Double.class;
		if (key.equalsIgnoreCase(RANGE)) return Double.class;
		
		return super.getSettingElementType(key);
	}
	
	@Override
	protected void addJsonSerializers(SimpleModule module) {
		super.addJsonSerializers(module);
		module.addSerializer(new CyColumnIdentifierJsonSerializer());
	}
	
	@Override
	protected void addJsonDeserializers(SimpleModule module) {
		super.addJsonDeserializers(module);
		CyColumnIdentifierFactory colIdFactory = serviceRegistrar.getService(CyColumnIdentifierFactory.class);
		module.addDeserializer(CyColumnIdentifier.class, new CyColumnIdentifierJsonDeserializer(colIdFactory));
	}
	
	protected static float convertFontSize(int size) {
		return size * 2.0f;
	}
	
	protected JFreeChart createChart(CyRow row) {
		if (isDirty()) {
			update();
			dirty = false;
		}
		
		var dataset = createDataset(row);
		var chart = createChart(dataset);
		
		return chart;
	}
	
	@Override
	public synchronized void set(String key, Object value) {
		super.set(key, value);
		dirty = true;
	}

	protected abstract T createDataset(CyRow row);
	
	protected abstract JFreeChart createChart(T dataset);
	
	@Override
	public void draw(Graphics g, Rectangle2D bounds, CyColumn column, CyRow row) {
		var g2 = (Graphics2D) g.create();
		
		// Give JFreeChart a larger area to draw into, so the proportions of the chart elements looks better
		double scale = 2.0;
		var newBounds = new Rectangle2D.Double(
				bounds.getX() * scale,
				bounds.getY() * scale,
				bounds.getWidth() * scale,
				bounds.getHeight() * scale
		);
		// Of course, we also have to ask Graphics2D to apply the inverse transformation
		double invScale = 1.0 / scale;
		g2.scale(invScale, invScale);

		// Check to see if we have a current alpha composite
		var comp = g2.getComposite();
		
		if (comp instanceof AlphaComposite) {
			float alpha = ((AlphaComposite) comp).getAlpha();
			var fc = createChart(row);
			var plot = fc.getPlot();
			plot.setForegroundAlpha(alpha);
			fc.draw(g2, newBounds);
		} else {
			createChart(row).draw(g2, newBounds);
		}
		
		g2.dispose();
	}
	
	public static CategoryDataset createCategoryDataset(Map<String, List<Double>> data) {
		var dataset = new DefaultCategoryDataset();
		
		int size = 0;
		
		for (var values : data.values())
			size = Math.max(size, values.size());
			
		
		for (var category : data.keySet()) {
			var values = data.get(category);
			
			for (int i = 0; i < values.size(); i++) {
				Double v = values.get(i);
				String k = "#" + (i+1); // row key
				
				dataset.addValue(v, category, k);
			}
		}
		
		return dataset;
	}
	
	// TODO minimumslice: The minimum size of a slice to be considered. All slices smaller than this are grouped together in a single "other" slice
//	public static PieDataset createPieDataset(List<Double> values) {
//		var dataset = new DefaultPieDataset();
//		
//		if (values != null) {
//			for (int i = 0; i < values.size(); i++) {
//				Double v = values.get(i);
//				String k = "#" + (i+1);
//				dataset.setValue(k, v);
//			}
//		}
//		
//		return dataset;
//	}
	
	public static List<Double> calculateRange(Collection<List<Double>> lists, boolean stacked) {
		var range = new ArrayList<Double>();
		
		if (lists != null && !lists.isEmpty()) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			
			for (var values : lists) {
				double sum = 0;
				
				if (values != null) {
					for (double v : values) {
						if (Double.isNaN(v))
							continue;
						
						if (stacked) {
							sum += v;
						} else {
							min = Math.min(min, v);
							max = Math.max(max, v);
						}
					}
					
					if (stacked) {
						min = Math.min(min, sum);
						max = Math.max(max, sum);
					}
				}
				
				if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY) {
					range.add(min);
					range.add(max);
				}
			}
		}
		
		return range;
	}
}
