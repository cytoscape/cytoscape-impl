package org.cytoscape.ding.internal.charts;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.ding.internal.charts.ViewUtils.Position;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public abstract class AbstractEnhancedCustomGraphics<T extends CustomGraphicLayer> implements CyCustomGraphics<T> {

	public static final String POSITION = "position";
	public static final String SCALE = "scale";
	public static final String SIZE = "size";
	
	/**
	 * The list of colors, one for each chart element
	 */
	public static final String COLORS = "colorlist";
	public static final String COLOR_SCHEME = "colorscheme";
	public static final String ORIENTATION = "orientation";
	
	
/*
attributelist
If the values are going to be specified by node attributes,
this list provides the attribute or attributes to be used as a comma-separated list.

colorlist
The list of colors, one for each bar. colorlist also supports the following keywords:
contrasting: Select colors base on a contrasting color scheme
modulated: Select colors base on a modulated rainbow color scheme
rainbow: Select colors base on the rainbow color scheme
random: Select colors randomly
up:color: Use color if the value is positive
down:color: Use color if the value is negative

labellist
This list provides the labels to be associated with each bar, point, or pie slice on the resulting graph. The values should contain a comma-separated list of labels, one for each value.

position
The position of the graphic relative to the center of the node. This can be expressed as either an "x,y" pair, or using the main compas points: north, northeast, east, southeast, south, southwest, west, northwest or center.

range
A comma-separated pair of values that indicates the range of the input values. This may be used to force all charts across a number of nodes to have the same value range. Otherwise each charts will utilize it's own set of values to determine the hight of the bars.

scale
A scale factor for the size of the chart. A scale of 1.0 is the same size as the node, whereas a scale of 0.5 is 50% of the node size.

separation
The separation between any two bars

showlabels
A true/false value to enable or disable the creation of labels.

size
This specifies the size of the chart as a widthXheight pair. If only one value is given, it is assumed that the chart is square, otherwise the string is assumed to contain a width followed by an "X" followed by a height.

valuelist
The list of values (as a comma-separated list) for each point, bar, or pie slice.

ybase
	 */
	
	protected static final double DEFAULT_YBASE = 0.5;
	
	protected Long id;
	protected float fitRatio = 0.9f;
	protected String displayName;
	protected int width = 50;
	protected int height = 50;
	
	protected final Map<String, Object> properties;
	
	protected static Logger logger;

	protected AbstractEnhancedCustomGraphics(final String displayName) {
		logger = LoggerFactory.getLogger(this.getClass());
		this.displayName = displayName;
		this.properties = new HashMap<String, Object>();
	}
	
	protected AbstractEnhancedCustomGraphics(final String displayName, final String input) {
		this(displayName);
		addProperties(parseInput(input));
	}
	
	protected AbstractEnhancedCustomGraphics(final String displayName, final Map<String, Object> properties) {
		this(displayName);
		addProperties(properties);
	}

	@Override
	public Long getIdentifier() {
		return id;
	}

	@Override
	public void setIdentifier(Long id) {
		this.id = id;
	}

	@Override
	public void setWidth(final int width) {
		this.width = width;
	}

	@Override
	public void setHeight(final int height) {
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	@Override
	public float getFitRatio() {
		return fitRatio;
	}

	@Override
	public void setFitRatio(float fitRatio) {
		this.fitRatio = fitRatio;
	}

	@Override
	public String toString() {
		return displayName;
	}

	@Override
	public String toSerializableString() {
		final StringBuilder sb = new StringBuilder(getId() + ":");
		
		for (final Entry<String, Object> entry : properties.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			
			if (key != null && value != null)
				sb.append(serialize(key, value));
		}
		
		return sb.toString();
	}
	
	public abstract String getId();
	
	public synchronized void set(final String key, Object value) {
		if (key == null)
			throw new IllegalArgumentException("'key' must not be null.");
		
		final Class<?> type = getSettingType(key);
		
		if (type != null) {
			if (List.class.isAssignableFrom(type))
				value = parseListValue(key, value, getSettingListType(key));
			else
				value = parseValue(key, value, type);
			
			properties.put(key, value);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <S> S get(final String key, final Class<S> cls) {
		return (S) properties.get(key);
	}
	
	public synchronized <S> S get(final String key, final Class<S> cls, final S defValue) {
		S value = get(key, cls);
		return value != null ? value : defValue;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized <S> List<S> getList(final String key, final Class<S> cls) {
		Object obj = properties.get(key);
		
		return obj instanceof List ? (List)obj : Collections.emptyList();
	}
	
	public String serialize(final String key, final Object value) {
		return " " + key + "=\"" + serializeValue(key, value) + "\"";
	}

	public String serializeValue(final String key, final Object value) {
		String s = "";
		
		if (value instanceof Collection)
			s = serializeList(key, (Collection<?>)value);
		else if (value instanceof Color)
			s = ColorUtil.toHexString((Color)value);
		else if (value != null)
			s = value.toString();
		
		return s;
	}
	
	public String serializeList(final String key, final Collection<?> list) {
		final StringBuilder sb = new StringBuilder();
		
		for (final Iterator<?> i = list.iterator(); i.hasNext();) {
			Object value = serializeValue(key, i.next());
	        sb.append(value).append(i.hasNext() ? getListSeparator(key) : "");
		}
		
		return sb.toString();
	}
	
//	public List<Color> convertInputToColor(final String input, final Map<String, ? extends List<?>> data,
//	final boolean normalize) {
//int nColors = 0;
//
//for (final List<?> values : data.values())
//	nColors = Math.max(nColors, values.size());
//
//if (input != null) {
//	// OK, we have three possibilities. The input could be a keyword, a
//	// comma-separated list of colors, or a list of Color objects. We need to figure this out first...
//	// See if we have a csv
//	String[] colorArray = input.split(",");
//	
//	// Look for up/down special case
//	if (colorArray.length == 2
//			&& (colorArray[0].toLowerCase().startsWith(UP) || colorArray[0].toLowerCase().startsWith(DOWN))) {
//		return parseUpDownColor(colorArray, values, normalize);
//	} else if (colorArray.length == 3
//			&& (colorArray[0].toLowerCase().startsWith(UP) || colorArray[0].toLowerCase().startsWith(DOWN) || colorArray[0]
//					.toLowerCase().startsWith(ZERO))) {
//		return parseUpDownColor(colorArray, values, normalize);
//	} else if (colorArray.length > 1) {
//		return parseColorList(colorArray);
//	}
//}
//
//// Return the default
//return ColorUtil.generateContrastingColors(nColors);
//}
	
	protected Map<String, Object> parseInput(final String input) {
		final Map<String, Object> props = new HashMap<String, Object>();
		
		if (input == null)
			return props;
		
		// Tokenize
		StringReader reader = new StringReader(input);
		StreamTokenizer st = new StreamTokenizer(reader);

		// We don't really want to parse numbers as numbers...
		st.ordinaryChar('/');
		st.ordinaryChar('_');
		st.ordinaryChar('-');
		st.ordinaryChar('.');
		st.ordinaryChars('0', '9');

		st.wordChars('/', '/');
		st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.wordChars('.', '.');
		st.wordChars('0', '9');

		List<String> tokenList = new ArrayList<String>();
		int tokenIndex = 0;
		int i;
		
		try {
			while ((i = st.nextToken()) != StreamTokenizer.TT_EOF) {
				switch (i) {
				case '=':
					// Get the next token
					i = st.nextToken();
					if (i == StreamTokenizer.TT_WORD || i == '"') {
						tokenIndex--;
						String key = tokenList.get(tokenIndex);
						props.put(key, st.sval);
						tokenList.remove(tokenIndex);
					}
					break;
				case '"':
				case StreamTokenizer.TT_WORD:
					tokenList.add(st.sval);
					tokenIndex++;
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
		}
		
		return props;
	}
	
	protected Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(COLORS)) return List.class;
		if (key.equalsIgnoreCase(COLOR_SCHEME)) return String.class;
		if (key.equalsIgnoreCase(SCALE)) return Double.class;
		if (key.equalsIgnoreCase(SIZE)) return Rectangle2D.class;
		if (key.equalsIgnoreCase(POSITION)) return Position.class;
		if (key.equalsIgnoreCase(ORIENTATION)) return Orientation.class;
			
		return null;
	}
	
	protected Class<?> getSettingListType(final String key) {
		if (key.equalsIgnoreCase(COLORS)) return Color.class;
		
		return Object.class;
	}
	
	@SuppressWarnings("unchecked")
	protected <S> S parseValue(final String key, Object value, final Class<S> type) {
		if (value == null) {
			if (type == Double.class && key.equalsIgnoreCase(SCALE))
				return (S) new Double(0.9);
			
			return null;
		}
		
		try {
			if (!type.isAssignableFrom(value.getClass())) {
				if (type == String.class) {
					value = value.toString();
				} else if (type == Boolean.class) {
					value = Boolean.parseBoolean(value.toString());
				} else if (type == Byte.class) {
					value = Double.valueOf(value.toString()).byteValue();
				} else if (type == Short.class) {
					value = Double.valueOf(value.toString()).shortValue();
				} else if (type == Integer.class) {
					value = Double.valueOf(value.toString()).intValue();
				} else if (type == Long.class) {
					value = Double.valueOf(value.toString()).longValue();
				} else if (type == Float.class) {
					value = Double.valueOf(value.toString()).floatValue();
				} else if (type == Double.class || type == Number.class) {
					value = Double.valueOf(value.toString());
				} else if (type == Color.class) {
					value = ColorUtil.parseColor(value.toString());
				} else if (type == Rectangle2D.class) {
					value = parseRectangle(value.toString());
				} else if (type == Position.class) {
					value = ViewUtils.getPosition(value.toString());
				} else if (type == Orientation.class) {
					value = Orientation.valueOf(value.toString());
				} else if (type == Point2D.class) {
					value = parsePoint(value.toString());
				} else if (type == ControlPoint.class) {
					value = ControlPoint.parse(value.toString());
				} else {
					value = null;
				}
			}
			
			return (S) value;
		} catch (Exception e) {
			return null;
		}
	}
	
	protected <S> List<S> parseListValue(final String key, Object value, final Class<S> type) {
		if (value == null)
			return Collections.emptyList();
		
		List<S> list = new ArrayList<S>();
		
		try {
			if (List.class.isAssignableFrom(value.getClass())) {
				for (final Object listValue : (List<?>)value) {
					final S parsedValue = parseValue(key, listValue, type);
					
					if (parsedValue != null)
						list.add(parsedValue);
				}
			} else if (value instanceof String) {
				final String separator = getListSeparator(key);
				final String[] split = value.toString().split(separator);
				
				for (String s : split) {
					final S parsedValue = parseValue(key, s.trim(), type);
					
					if (parsedValue != null)
						list.add(parsedValue);
				}
			}
		} catch (Exception e) {
		}
			
		return list;
	}
	
	protected String getListSeparator(final String key) {
		return ",";
	}

	/**
	 * Return the size specified by the user in the width and height fields of
	 * the Rectangle The size can be either "sss" where "sss" will be both the
	 * height and the width or "hhhxwww" where hhh is the height and www is the
	 * width.
	 * 
	 * @param input the input size
	 * @return a rectangle to get the width and height from
	 */
	protected Rectangle2D parseRectangle(final String input) {
		if (input != null) {
			String inputString = (String) input;
			String[] sizes = inputString.split("[xX]");
			
			if (sizes.length == 1) {
				double v = Double.parseDouble(sizes[0]);
				return new Rectangle2D.Double(0.0, 0.0, v, v);
			} else if (sizes.length == 2) {
				double h = Double.parseDouble(sizes[0]);
				double w = Double.parseDouble(sizes[1]);
				return new Rectangle2D.Double(0.0, 0.0, w, h);
			}
		}
		
		return null;
	}
	
	protected Point2D parsePoint(final String input) {
		if (input == null || input.length() == 0)
			return null;

		String tokens[] = input.split(",");
		float x = Float.parseFloat(tokens[0].trim());
		float y = Float.parseFloat(tokens[1].trim());
		
		return new Point2D.Float(x, y);
	}

	/**
 	 * Return the double equivalent of the input
 	 *
 	 * @param input an input value that is supposed to be a double
 	 * @return the a double value it represents
 	 * @throws NumberFormatException is the value is illegal
 	 */
	protected double getDoubleValue(Object input) throws NumberFormatException {
		if (input instanceof Double)
			return ((Double)input).doubleValue();
		else if (input instanceof Integer)
			return ((Integer)input).doubleValue();
		else if (input instanceof String)
			return Double.parseDouble((String)input);
		throw new NumberFormatException("input can not be converted to double");
	}
	
	protected void addProperties(final Map<String, Object> properties) {
		if (properties != null) {
			for (final Entry<String, Object> entry : properties.entrySet()) {
				if (getSettingType(entry.getKey()) != null)
					set(entry.getKey(), entry.getValue());
			}
		}
	}
}
