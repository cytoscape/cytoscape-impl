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
import java.util.Set;

import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.presentation.property.values.MappableVisualPropertyValue;

public abstract class AbstractChartCustomGraphics<T extends CustomGraphicLayer> extends
		AbstractEnhancedCustomGraphics<T> implements CyChart<T>, MappableVisualPropertyValue {

	public static final String DATA_COLUMNS = "datacolumns";
	public static final String ITEM_LABELS_COLUMN = "itemlabelscolumn";
	public static final String DOMAIN_LABELS_COLUMN = "domainlabelscolumn";
	public static final String RANGE_LABELS_COLUMN = "rangelabelscolumn";
	public static final String GLOBAL_RANGE = "globalrange";
	public static final String AUTO_RANGE = "autorange";
	public static final String RANGE = "range";
	public static final String SHOW_ITEM_LABELS = "showitemlabels";
	public static final String SHOW_DOMAIN_AXIS = "showdomainaxis";
	public static final String SHOW_RANGE_AXIS = "showrangeaxis";
	public static final String VALUES = "valuelist";
	public static final String STACKED = "stacked";
//	/**
//	 * The vertical base of the chart as a proportion of the height.
//	 * By default, this is 0.5 (the center of the node), to allow for both positive and negative values.
//	 * If, however, you only have positive values, you might want to set this to 1.0 (the bottom of the node).
//	 * Note that this goes backwards from what might be expected, with 0.0 being the top of the node and 
//	 * 1.0 being the bottom of the node. The keyword bottom is also supported.
//	 */
//	public static final String YBASE = "ybase";
	
	private final CyColumnIdentifierFactory colIdFactory;
	
	protected AbstractChartCustomGraphics(final String displayName, final CyColumnIdentifierFactory colIdFactory) {
		super(displayName);
		
		if (colIdFactory == null)
			throw new IllegalArgumentException("'colIdFactory' must not be null.");
		
		this.colIdFactory = colIdFactory;
	}
	
	protected AbstractChartCustomGraphics(final String displayName, final String input,
			final CyColumnIdentifierFactory colIdFactory) {
		this(displayName, colIdFactory);
		addProperties(parseInput(input));
	}
	
	protected AbstractChartCustomGraphics(final AbstractChartCustomGraphics<T> chart,
			final CyColumnIdentifierFactory colIdFactory) {
		this(chart.getDisplayName(), colIdFactory);
		
		if (chart.getProperties() != null)
			this.properties.putAll(chart.getProperties());
	}
	
	protected AbstractChartCustomGraphics(final String displayName, final Map<String, Object> properties,
			final CyColumnIdentifierFactory colIdFactory) {
		this(displayName, colIdFactory);
		
		if (properties != null)
			this.properties.putAll(properties);
	}
	
	public List<Double> convertInputToDouble(String input) {
		return parseStringList((String) input);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<String, Object>(properties);
	}

	@Override
	public Set<CyColumnIdentifier> getMappedColumnNames() {
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
		final LinkedHashMap<String, List<Double>> data = new LinkedHashMap<String, List<Double>>();
		final CyRow row = network.getRow(model);
		
		if (row == null)
			return data;

		final CyTable table = row.getTable();

		for (final CyColumnIdentifier colId : columnNames) {
			final CyColumn column = table.getColumn(colId.getColumnName());
			
			if (column != null && column.getType() == List.class) {
				final List<Double> values = new ArrayList<Double>();
				final Class<?> type = column.getListElementType();
				
				if (type == Double.class) {
					List<Double> dlist = row.getList(colId.getColumnName(), Double.class);
					if (dlist != null)
						values.addAll(dlist);
				} else if (type == Integer.class) {
					List<Integer> iList = row.getList(colId.getColumnName(), Integer.class);
					for (Integer i : iList)
						values.add(i.doubleValue());
				} else if (type == Long.class) {
					List<Long> lList = row.getList(colId.getColumnName(), Long.class);
					for (Long l : lList)
						values.add(l.doubleValue());
				} else if (type == Float.class) {
					List<Float> fList = row.getList(colId.getColumnName(), Float.class);
					for (Float f : fList)
						values.add(f.doubleValue());
				} else if (type == String.class) {
					List<String> sList = row.getList(colId.getColumnName(), String.class);
					for (String s : sList)
						values.add(Double.valueOf(s));
				}
				
				data.put(colId.getColumnName(), values);
			}
		}

		return data;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLabelsFromColumn(final CyNetwork network, final CyIdentifiable model,
			final CyColumnIdentifier columnId) {
		final List<String> labels = new ArrayList<String>();
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
		List<Double> values = new ArrayList<Double>(input.size());
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
		List<Double> values = new ArrayList<Double>(input.size());
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

	/**
	 * Return the integer equivalent of the input
	 * 
	 * @param input
	 *            an input value that is supposed to be a integer
	 * @return the a integer value it represents
	 * @throws NumberFormatException
	 *             is the value is illegal
	 */
	public int getIntegerValue(Object input) throws NumberFormatException {
		if (input instanceof Integer)
			return ((Integer) input).intValue();
		else if (input instanceof Integer)
			return ((Integer) input).intValue();
		else if (input instanceof String)
			return Integer.parseInt((String) input);
		throw new NumberFormatException("input can not be converted to integer");
	}

	public List<Double> arrayMax(List<Double> maxValues, List<Double> values) {
		// Initialize, if necessary
		if (maxValues == null) {
			maxValues = new ArrayList<Double>(values.size());
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

	/**
	 * Takes a map of objects indexed by a string keyword and returns a map of
	 * strings indexed by that keyword. This involves figuring out if the object
	 * is a list, and if so converting it to a comma separated string
	 * 
	 * @param argMap
	 *            the map of objects indexed by strings
	 * @return the serialized map
	 */
	public Map<String, String> serializeArgMap(Map<String, Object> argMap) {
		Map<String, String> sMap = new HashMap<String, String>();
		for (String key : argMap.keySet()) {
			sMap.put(key, serializeObject(argMap.get(key)));
		}
		return sMap;
	}

	@Override
	protected Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return List.class;
		if (key.equalsIgnoreCase(ITEM_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(DOMAIN_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(RANGE_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(VALUES)) return List.class;
		if (key.equalsIgnoreCase(SHOW_ITEM_LABELS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_RANGE_AXIS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_DOMAIN_AXIS)) return Boolean.class;
		if (key.equalsIgnoreCase(GLOBAL_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(AUTO_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(RANGE)) return DoubleRange.class;
//		if (key.equalsIgnoreCase(YBASE)) return Double.class;
		if (key.equalsIgnoreCase(STACKED)) return Boolean.class;
			
		return super.getSettingType(key);
	}
	
	@Override
	protected Class<?> getSettingListType(final String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(VALUES)) return Double.class;
		
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
//				} else if (type == Double.class || type == Number.class) {
//					value = key.equalsIgnoreCase(YBASE) ? 
//							parseYBase(value.toString()) : Double.valueOf(value.toString());
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
	
//	private Double parseYBase(final String input) {
//		if (input != null) {
//			try {
//				return input.equalsIgnoreCase("bottom") ? 1.0 : Double.valueOf(input);
//			} catch (NumberFormatException e) {
//			}
//		}
//		
//		return DEFAULT_YBASE;
//	}
	
	/**
	 * Serialize an object that might be a list to a string
	 */
	private String serializeObject(Object obj) {
		String result = null;;
		
		if (obj instanceof List) {
			result = "";
			for (Object o : (List<?>) obj) {
				result += o.toString() + ",";
			}
			result = result.substring(0, result.length() - 1);
		} else
			result = obj.toString();

		return result;
	}
}
