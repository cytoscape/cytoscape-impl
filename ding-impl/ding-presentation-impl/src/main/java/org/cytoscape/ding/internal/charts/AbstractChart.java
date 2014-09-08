package org.cytoscape.ding.internal.charts;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.presentation.property.values.MappableVisualPropertyValue;

public abstract class AbstractChart<T extends CustomGraphicLayer> extends AbstractCustomGraphics2<T>
		implements MappableVisualPropertyValue {

	public static final String DATA_COLUMNS = "dataColumns";
	public static final String ITEM_LABELS_COLUMN = "itemLabelsColumn";
	public static final String ITEM_LABELS = "itemLabels";
	public static final String DOMAIN_LABELS_COLUMN = "domainLabelsColumn";
	public static final String RANGE_LABELS_COLUMN = "rangeLabelsColumn";
	public static final String GLOBAL_RANGE = "globalRange";
	public static final String AUTO_RANGE = "autoRange";
	public static final String RANGE = "range";
	public static final String SHOW_ITEM_LABELS = "showItemLabels";
	public static final String SHOW_DOMAIN_AXIS = "showDomainAxis";
	public static final String SHOW_RANGE_AXIS = "showRangeAxis";
	public static final String AXIS_WIDTH = "axisWidth";
	public static final String AXIS_COLOR = "axisColor";
	public static final String VALUES = "values";
	public static final String BORDER_WIDTH = "borderWidth";
	public static final String BORDER_COLOR = "borderColor";
	
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
	
	protected AbstractChart(final AbstractChart<T> chart,
			final CyColumnIdentifierFactory colIdFactory) {
		this(chart.getDisplayName(), colIdFactory);
		
		if (chart.getProperties() != null)
			this.properties.putAll(chart.getProperties());
	}
	
	protected AbstractChart(final String displayName, final Map<String, Object> properties,
			final CyColumnIdentifierFactory colIdFactory) {
		this(displayName, colIdFactory);
		
		if (properties != null)
			addProperties(properties);
	}
	
	public List<Double> convertInputToDouble(String input) {
		return parseStringList((String) input);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<String, Object>(properties);
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
	
	/**
	 * Get values from a list of attributes. The attributesList can either be a
	 * single list attribute with numeric values or a list of integer or
	 * floating point attributes. At some point, it might be interesting to
	 * think about other combinations, but this is a good starting point.
	 * 
	 * @param view the node we're getting the custom graphics from
	 * @param attributelist the list of column names
	 * @return the lists of values @ if the attributes aren't numeric
	 */
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

	public List<Double> convertStringList(List<String> input) {
		List<Double> values = new ArrayList<>(input.size());
		for (String s : input) {
			try {
				Double d = Double.valueOf(s);
				values.add(d);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return values;
	}

	public List<Double> convertIntegerList(List<Integer> input) {
		List<Double> values = new ArrayList<>(input.size());
		for (Integer s : input) {
			double d = s.doubleValue();
			values.add(d);
		}
		return values;
	}

	public List<Double> parseStringList(String input) {
		if (input == null)
			return null;
		String[] inputArray = ((String) input).split(",");
		return convertStringList(Arrays.asList(inputArray));
	}

	public List<String> getStringList(String input) {
		if (input == null || input.length() == 0)
			return new ArrayList<String>();

		String[] inputArray = ((String) input).split(",");
		return Arrays.asList(inputArray);
	}

	/**
	 * Return the boolean equivalent of the input
	 * 
	 * @param input
	 *            an input value that is supposed to be Boolean
	 * @return the boolean value it represents
	 */
	public boolean getBooleanValue(Object input) {
		if (input instanceof Boolean)
			return ((Boolean) input).booleanValue();
		return Boolean.parseBoolean(input.toString());
	}

	public int getFontStyle(String input) {
		if (input.equalsIgnoreCase("italics"))
			return Font.ITALIC;
		if (input.equalsIgnoreCase("bold"))
			return Font.BOLD;
		if (input.equalsIgnoreCase("bolditalic"))
			return Font.ITALIC | Font.BOLD;
		return Font.PLAIN;
	}

	public Color getColorValue(String input) {
		String[] colorArray = new String[1];
		colorArray[0] = input;
		List<Color> colors = ColorUtil.parseColorList(colorArray);
		return colors.get(0);
	}

	/**
	 * Return the double equivalent of the input
	 * 
	 * @param input
	 *            an input value that is supposed to be a double
	 * @return the a double value it represents
	 * @throws NumberFormatException
	 *             is the value is illegal
	 */
	@Override
	public double getDoubleValue(Object input) throws NumberFormatException {
		if (input instanceof Double)
			return ((Double) input).doubleValue();
		else if (input instanceof Integer)
			return ((Integer) input).doubleValue();
		else if (input instanceof String)
			return Double.parseDouble((String) input);
		throw new NumberFormatException("input can not be converted to double");
	}

	public List<Double> arrayMax(List<Double> maxValues, List<Double> values) {
		// Initialize, if necessary
		if (maxValues == null) {
			maxValues = new ArrayList<>(values.size());
			for (Double d : values)
				maxValues.add(Math.abs(d));
			return maxValues;
		}

		// OK, now we need to actually do the work...
		for (int index = 0; index < values.size(); index++) {
			maxValues.set(index, Math.max(maxValues.get(index), Math.abs(values.get(index))));
		}
		return maxValues;
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
	protected Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return List.class;
		if (key.equalsIgnoreCase(VALUES)) return List.class;
		if (key.equalsIgnoreCase(ITEM_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(ITEM_LABELS)) return List.class;
		if (key.equalsIgnoreCase(DOMAIN_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(RANGE_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(SHOW_ITEM_LABELS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_RANGE_AXIS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_DOMAIN_AXIS)) return Boolean.class;
		if (key.equalsIgnoreCase(AXIS_WIDTH)) return Float.class;
		if (key.equalsIgnoreCase(AXIS_COLOR)) return Color.class;
		if (key.equalsIgnoreCase(GLOBAL_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(AUTO_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(RANGE)) return DoubleRange.class;
		if (key.equalsIgnoreCase(BORDER_WIDTH)) return Float.class;
		if (key.equalsIgnoreCase(BORDER_COLOR)) return Color.class;
			
		return super.getSettingType(key);
	}
	
	@Override
	protected Class<?> getSettingListType(final String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(VALUES)) return Double.class;
		if (key.equalsIgnoreCase(ITEM_LABELS)) return String.class;
		
		return super.getSettingListType(key);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected <S> S parseValue(final String key, Object value, final Class<S> type) {
		try {
			if (!type.isAssignableFrom(value.getClass())) {
				if (type == DoubleRange.class) {
					value = parseRange(value.toString());
				} else if (type == CyColumnIdentifier.class) {
					value = parseColumnIdentifier(value.toString());
				} else {
					value = super.parseValue(key, value, type);
				}
			}
			
			return (S) value;
		} catch (Exception e) {
			return null;
		}
	}
	
	private Object parseColumnIdentifier(final String input) {
		return colIdFactory.createColumnIdentifier(input);
	}
	
	private DoubleRange parseRange(final String input) {
		if (input != null) {
			String[] split = input.split(",");
			
			try {
				return new DoubleRange(getDoubleValue(split[0]), getDoubleValue(split[1]));
			} catch (NumberFormatException e) {
			}
		}
		
		return null;
	}
}
