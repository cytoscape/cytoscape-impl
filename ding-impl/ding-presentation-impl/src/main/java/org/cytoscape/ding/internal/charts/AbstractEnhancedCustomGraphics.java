package org.cytoscape.ding.internal.charts;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.ding.internal.charts.ViewUtils.Position;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: remove this class?
public abstract class AbstractEnhancedCustomGraphics<T extends CustomGraphicLayer> implements CyChart<T> {

	public static final String DATA_COLUMNS = "datacolumns";
	public static final String YBASE = "ybase";
	public static final String ITEM_LABELS_COLUMN = "itemlabelscolumn";
	public static final String DOMAIN_LABELS_COLUMN = "domainlabelscolumn";
	public static final String RANGE_LABELS_COLUMN = "rangelabelscolumn";
	public static final String POSITION = "position";
	public static final String GLOBAL_RANGE = "globalrange";
	public static final String AUTO_RANGE = "autorange";
	public static final String RANGE = "range";
	public static final String SCALE = "scale";
	public static final String SIZE = "size";
	public static final String SHOW_ITEM_LABELS = "showitemlabels";
	public static final String SHOW_DOMAIN_AXIS = "showdomainaxis";
	public static final String SHOW_RANGE_AXIS = "showrangeaxis";
	public static final String VALUES = "valuelist";
	public static final String COLORS = "colorlist";
	public static final String COLOR_SCHEME = "colorscheme";
	public static final String ORIENTATION = "orientation";
	public static final String STACKED = "stacked";
	
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
	
	protected AbstractEnhancedCustomGraphics(final AbstractEnhancedCustomGraphics<T> chart) {
		this(chart.getDisplayName());
		this.properties.putAll(chart.getProperties());
	}
	
	protected AbstractEnhancedCustomGraphics(final String displayName, final Map<String, Object> properties) {
		this(displayName);
		addProperties(properties);
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<String, Object>(properties);
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
		if (key.equals(DATA_COLUMNS)) return List.class;
		if (key.equals(ITEM_LABELS_COLUMN)) return String.class;
		if (key.equals(DOMAIN_LABELS_COLUMN)) return String.class;
		if (key.equals(RANGE_LABELS_COLUMN)) return String.class;
		if (key.equals(VALUES)) return List.class;
		if (key.equals(COLORS)) return List.class;
		if (key.equals(COLOR_SCHEME)) return String.class;
		if (key.equals(SCALE)) return Double.class;
		if (key.equals(SHOW_ITEM_LABELS)) return Boolean.class;
		if (key.equals(SHOW_RANGE_AXIS)) return Boolean.class;
		if (key.equals(SHOW_DOMAIN_AXIS)) return Boolean.class;
		if (key.equals(GLOBAL_RANGE)) return Boolean.class;
		if (key.equals(AUTO_RANGE)) return Boolean.class;
		if (key.equals(RANGE)) return DoubleRange.class;
		if (key.equals(SIZE)) return Rectangle2D.class;
		if (key.equals(YBASE)) return Double.class;
		if (key.equals(POSITION)) return Position.class;
		if (key.equals(ORIENTATION)) return Orientation.class;
		if (key.equals(STACKED)) return Boolean.class;
			
		return null;
	}
	
	protected Class<?> getSettingListType(final String key) {
		if (key.equals(DATA_COLUMNS)) return String.class;
		if (key.equals(VALUES)) return Double.class;
		if (key.equals(COLORS)) return Color.class;
		
		return Object.class;
	}
	
	@SuppressWarnings("unchecked")
	protected <S> S parseValue(final String key, Object value, final Class<S> type) {
		if (value == null) {
			if (type == Boolean.class && key.equalsIgnoreCase(SHOW_ITEM_LABELS))
				return (S) Boolean.TRUE;
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
					value = key.equalsIgnoreCase(YBASE) ? 
							parseYBase(value.toString()) : Double.valueOf(value.toString());
				} else if (type == Color.class) {
					value = ColorUtil.parseColor(value.toString());
				} else if (type == Rectangle2D.class) {
					value = parseRectangle(value.toString());
				} else if (type == Position.class) {
					value = ViewUtils.getPosition(value.toString());
				} else if (type == DoubleRange.class) {
					value = parseRange(value.toString());
				} else if (type == Orientation.class) {
					value = Orientation.valueOf(value.toString());
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
					list.add(parsedValue);
				}
			} else if (value instanceof String) {
				String[] split = value.toString().split(",");
				
				for (String s : split) {
					final S parsedValue = parseValue(key, s.trim(), type);
					list.add(parsedValue);
				}
			}
		} catch (Exception e) {
		}
			
		return list;
	}
	
	protected DoubleRange parseRange(final String input) {
		if (input != null) {
			String[] split = input.split(",");
			
			try {
				return new DoubleRange(getDoubleValue(split[0]), getDoubleValue(split[1]));
			} catch (NumberFormatException e) {
			}
		}
		
		return null;
	}
	
	protected Double parseYBase(final String input) {
		if (input != null) {
			try {
				return input.equalsIgnoreCase("bottom") ? 1.0 : Double.valueOf(input);
			} catch (NumberFormatException e) {
			}
		}
		
		return DEFAULT_YBASE;
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

	// Parse out a stop list. The stoplist is of the form:
	// r,g,b,a,stop|r,g,b,a,stop...
	protected int parseStopList(String stoplist, List<Color> colors, List<Float> stops) {
		if (stoplist == null || stoplist.length() == 0)
			return 0;
		
		int nStops = 0;

		String[] tokens = stoplist.split("\\|");
		
		for (String token : tokens) {
			String[] components = token.split(",");
			if (components.length != 4 && components.length != 5)
				continue;

			int r = Integer.parseInt(components[0]);
			int g = Integer.parseInt(components[1]);
			int b = Integer.parseInt(components[2]);
			int a = 255;
			float stop;
			if (components.length == 5) {
				a = Integer.parseInt(components[3]);
				stop = Float.parseFloat(components[4]);
			} else {
				stop = Float.parseFloat(components[3]);
			}
			colors.add(new Color(r, g, b, a));
			stops.add(stop);
			nStops++;
		}
		
		return nStops;
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
