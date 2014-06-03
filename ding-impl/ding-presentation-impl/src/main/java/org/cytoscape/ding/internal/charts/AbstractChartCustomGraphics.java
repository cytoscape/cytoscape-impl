package org.cytoscape.ding.internal.charts;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractChartCustomGraphics<T extends CustomGraphicLayer> extends
		AbstractEnhancedCustomGraphics<T> implements CyChart<T> {

	protected AbstractChartCustomGraphics(final String displayName) {
		super(displayName);
	}
	
	protected AbstractChartCustomGraphics(final String displayName, final String input) {
		super(displayName, input);
	}
	
	protected AbstractChartCustomGraphics(final AbstractChartCustomGraphics<T> chart) {
		this(chart.getDisplayName());
		this.properties.putAll(chart.getProperties());
	}
	
	protected AbstractChartCustomGraphics(final String displayName, final Map<String, Object> properties) {
		super(displayName, properties);
	}
	
	public List<Double> convertInputToDouble(String input) {
		return parseStringList((String) input);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<String, Object>(properties);
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
			final List<String> columnNames) {
		final LinkedHashMap<String, List<Double>> data = new LinkedHashMap<String, List<Double>>();
		final CyRow row = network.getRow(model);
		
		if (row == null)
			return data;

		final CyTable table = row.getTable();

		for (final String name : columnNames) {
			final CyColumn column = table.getColumn(name);
			
			if (column != null && column.getType() == List.class) {
				final List<Double> values = new ArrayList<Double>();
				final Class<?> type = column.getListElementType();
				
				if (type == Double.class) {
					List<Double> dlist = row.getList(name, Double.class);
					if (dlist != null)
						values.addAll(dlist);
				} else if (type == Integer.class) {
					List<Integer> iList = row.getList(name, Integer.class);
					for (Integer i : iList)
						values.add(i.doubleValue());
				} else if (type == Long.class) {
					List<Long> lList = row.getList(name, Long.class);
					for (Long l : lList)
						values.add(l.doubleValue());
				} else if (type == Float.class) {
					List<Float> fList = row.getList(name, Float.class);
					for (Float f : fList)
						values.add(f.doubleValue());
				} else if (type == String.class) {
					List<String> sList = row.getList(name, String.class);
					for (String s : sList)
						values.add(Double.valueOf(s));
				}
				
				data.put(name, values);
			}
		}

		return data;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLabelsFromColumn(final CyNetwork network, final CyIdentifiable model,
			final String columnName) {
		final List<String> labels = new ArrayList<String>();
		final CyRow row = network.getRow(model);
		
		if (row != null && columnName != null) {
			final CyTable table = row.getTable();
			final CyColumn column = table.getColumn(columnName);
			
			if (column != null && column.getType() == List.class) {
				final Class<?> type = column.getListElementType();
				final List<?> values = row.getList(columnName, type);
				
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
