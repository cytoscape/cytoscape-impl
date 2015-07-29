package org.cytoscape.ding.internal.charts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.customgraphics.json.CyColumnIdentifierJsonDeserializer;
import org.cytoscape.ding.customgraphics.json.CyColumnIdentifierJsonSerializer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.presentation.property.values.MappableVisualPropertyValue;

import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class AbstractChart<T extends CustomGraphicLayer> extends AbstractCustomGraphics2<T>
		implements MappableVisualPropertyValue {

	public static final String DATA_COLUMNS = "cy_dataColumns";
	public static final String ITEM_LABELS_COLUMN = "cy_itemLabelsColumn";
	public static final String ITEM_LABELS = "cy_itemLabels";
	public static final String ITEM_LABEL_FONT_SIZE = "cy_itemLabelFontSize";
	public static final String DOMAIN_LABELS_COLUMN = "cy_domainLabelsColumn";
	public static final String RANGE_LABELS_COLUMN = "cy_rangeLabelsColumn";
	public static final String DOMAIN_LABEL_POSITION = "cy_domainLabelPosition";
	public static final String AXIS_LABEL_FONT_SIZE = "cy_axisLabelFontSize";
	public static final String GLOBAL_RANGE = "cy_globalRange";
	public static final String AUTO_RANGE = "cy_autoRange";
	public static final String RANGE = "cy_range";
	public static final String SHOW_ITEM_LABELS = "cy_showItemLabels";
	public static final String SHOW_DOMAIN_AXIS = "cy_showDomainAxis";
	public static final String SHOW_RANGE_AXIS = "cy_showRangeAxis";
	public static final String SHOW_RANGE_ZERO_BASELINE = "cy_showRangeZeroBaseline";
	public static final String AXIS_WIDTH = "cy_axisWidth";
	public static final String AXIS_COLOR = "cy_axisColor";
	public static final String VALUES = "cy_values";
	public static final String BORDER_WIDTH = "cy_borderWidth";
	public static final String BORDER_COLOR = "cy_borderColor";
	
	private final CyColumnIdentifierFactory colIdFactory;
	
	protected AbstractChart(final String displayName, final CyColumnIdentifierFactory colIdFactory) {
		super(displayName);
		
		if (colIdFactory == null)
			throw new IllegalArgumentException("'colIdFactory' must not be null.");
		
		this.colIdFactory = colIdFactory;
	}
	
	protected AbstractChart(final String displayName, final String input,
			final CyColumnIdentifierFactory colIdFactory) {
		this(displayName, colIdFactory);
		addProperties(parseInput(input));
	}
	
	protected AbstractChart(final AbstractChart<T> chart, final CyColumnIdentifierFactory colIdFactory) {
		this(chart.getDisplayName(), colIdFactory);
		addProperties(chart.getProperties());
	}
	
	protected AbstractChart(final String displayName, final Map<String, Object> properties,
			final CyColumnIdentifierFactory colIdFactory) {
		this(displayName, colIdFactory);
		addProperties(properties);
	}
	
	@Override
	public Set<CyColumnIdentifier> getMappedColumns() {
		final Set<CyColumnIdentifier> set = new HashSet<CyColumnIdentifier>();
		set.addAll(getList(DATA_COLUMNS, CyColumnIdentifier.class));
		
		if (get(SHOW_ITEM_LABELS, Boolean.class, Boolean.FALSE))
			set.addAll(getList(ITEM_LABELS_COLUMN, CyColumnIdentifier.class));
		
		if (get(SHOW_DOMAIN_AXIS, Boolean.class, Boolean.FALSE) && get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class) != null)
			set.add(get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
		
		if (get(SHOW_RANGE_AXIS, Boolean.class, Boolean.FALSE) && get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class) != null)
			set.add(get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
		
		return set;
	}
	
	@Override
	public String getSerializableString() {
		return toSerializableString();
	}
	
	@Override
	public void update() {
		// Doesn't need to do anything here, because charts are updated when layers are recreated.
	}
	
	public Map<String, List<Double>> getDataFromColumns(final CyNetwork network, final CyIdentifiable model,
			final List<CyColumnIdentifier> columnNames) {
		LinkedHashMap<String, List<Double>> data = new LinkedHashMap<>();
		final CyRow row = network.getRow(model);
		
		if (row == null)
			return data;

		final CyTable table = row.getTable();
		final List<Double> singleSeriesValues = new ArrayList<Double>();
		final StringBuilder singleSeriesKey = new StringBuilder();
		int singleSeriesIndex = -1;
		int count = 0;

		for (final CyColumnIdentifier colId : columnNames) {
			final CyColumn column = table.getColumn(colId.getColumnName());
			
			if (column == null)
				continue;
			
			final String colName = column.getName();
			final List<Double> values = new ArrayList<Double>();
			
			if (column.getType() == List.class) {
				// List Column: One column = one data series
				final Class<?> type = column.getListElementType();
				
				if (type == Double.class) {
					List<Double> list = row.getList(colName, Double.class);
					
					if (list != null)
						values.addAll(list);
				} else if (type == Integer.class) {
					List<Integer> list = row.getList(colName, Integer.class);
					
					if (list != null) {
						for (Integer i : list)
							values.add(i.doubleValue());
					}
				} else if (type == Long.class) {
					List<Long> list = row.getList(colName, Long.class);
					
					if (list != null) {
						for (Long l : list)
							values.add(l.doubleValue());
					}
				} else if (type == Float.class) {
					List<Float> list = row.getList(colName, Float.class);
					
					if (list != null) {
						for (Float f : list)
							values.add(f.doubleValue());
					}
				}
				
				data.put(colName, values);
			} else {
				// Single Column: All single columns together make only one data series
				final Class<?> type = column.getType();
				
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
			final Set<Entry<String, List<Double>>> entrySet = data.entrySet();
			data = new LinkedHashMap<>();
			int i = 0;
			
			for (final Entry<String, List<Double>> entry : entrySet) {
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
	public List<String> getLabelsFromColumn(final CyNetwork network, final CyIdentifiable model,
			final CyColumnIdentifier columnId) {
		final List<String> labels = new ArrayList<>();
		final CyRow row = network.getRow(model);
		
		if (row != null && columnId != null) {
			final CyTable table = row.getTable();
			final CyColumn column = table.getColumn(columnId.getColumnName());
			
			if (column != null && column.getType() == List.class) {
				final Class<?> type = column.getListElementType();
				final List<?> values = row.getList(columnId.getColumnName(), type);
				
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
	protected List<String> getSingleValueColumnNames(final CyNetwork network, final CyIdentifiable model) {
		final List<String> names = new ArrayList<>();
		final CyRow row = network.getRow(model);
		
		if (row == null)
			return names;
		
		final List<CyColumnIdentifier> dataColumns = getList(DATA_COLUMNS, CyColumnIdentifier.class);
		final CyTable table = row.getTable();
		
		boolean invalid = false;
		
		for (final CyColumnIdentifier colId : dataColumns) {
			final CyColumn column = table.getColumn(colId.getColumnName());
			
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

	protected Map<String, List<Double>> getData(final CyNetwork network, final CyIdentifiable model) {
		final Map<String, List<Double>> data;
		final List<Double> values = getList(VALUES, Double.class);
		
		if (values == null || values.isEmpty()) {
			final List<CyColumnIdentifier> dataColumns = getList(DATA_COLUMNS, CyColumnIdentifier.class);
			data = getDataFromColumns(network, model, dataColumns);
		} else {
			data = new HashMap<String, List<Double>>();
			data.put("Values", values);
		}
		
		return data;
	}
	
	protected List<String> getItemLabels(final CyNetwork network, final CyIdentifiable model) {
		List<String> labels = getList(ITEM_LABELS, String.class);
		
		if (labels == null || labels.isEmpty()) {
			final CyColumnIdentifier labelsColumn = get(ITEM_LABELS_COLUMN, CyColumnIdentifier.class);
			labels = getLabelsFromColumn(network, model, labelsColumn);
		}
		
		return labels;
	}
	
	protected List<Color> getColors(final Map<String, List<Double>> data) {
		List<Color> colors = getList(COLORS, Color.class);
		
		if (colors == null || colors.isEmpty()) {
			final ColorScheme scheme = get(COLOR_SCHEME, ColorScheme.class);
			
			if (scheme != null && data != null && !data.isEmpty()) {
				int nColors = 0;
				
				for (final List<Double> values : data.values()) {
					if (values != null)
						nColors = Math.max(nColors, values.size());
				}
				
				colors = scheme.getColors(nColors);
			}
		}
		
		return colors;
	}

	@Override
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return List.class;
		if (key.equalsIgnoreCase(VALUES)) return List.class;
		if (key.equalsIgnoreCase(ITEM_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(ITEM_LABELS)) return List.class;
		if (key.equalsIgnoreCase(ITEM_LABEL_FONT_SIZE)) return Integer.class;
		if (key.equalsIgnoreCase(DOMAIN_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(RANGE_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(SHOW_ITEM_LABELS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_RANGE_AXIS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_DOMAIN_AXIS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_RANGE_ZERO_BASELINE)) return Boolean.class;
		if (key.equalsIgnoreCase(DOMAIN_LABEL_POSITION)) return LabelPosition.class;
		if (key.equalsIgnoreCase(AXIS_LABEL_FONT_SIZE)) return Integer.class;
		if (key.equalsIgnoreCase(AXIS_WIDTH)) return Float.class;
		if (key.equalsIgnoreCase(AXIS_COLOR)) return Color.class;
		if (key.equalsIgnoreCase(GLOBAL_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(AUTO_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(RANGE)) return List.class;
		if (key.equalsIgnoreCase(BORDER_WIDTH)) return Float.class;
		if (key.equalsIgnoreCase(BORDER_COLOR)) return Color.class;
			
		return super.getSettingType(key);
	}
	
	@Override
	public Class<?> getSettingElementType(final String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(VALUES)) return Double.class;
		if (key.equalsIgnoreCase(ITEM_LABELS)) return String.class;
		if (key.equalsIgnoreCase(RANGE)) return Double.class;
		
		return super.getSettingElementType(key);
	}
	
	@Override
	protected void addJsonSerializers(final SimpleModule module) {
		super.addJsonSerializers(module);
		module.addSerializer(new CyColumnIdentifierJsonSerializer());
	}
	
	@Override
	protected void addJsonDeserializers(final SimpleModule module) {
		super.addJsonDeserializers(module);
		module.addDeserializer(CyColumnIdentifier.class, new CyColumnIdentifierJsonDeserializer(colIdFactory));
	}
	
	protected static float convertFontSize(final int size) {
		return size * 2.0f;
	}
}
