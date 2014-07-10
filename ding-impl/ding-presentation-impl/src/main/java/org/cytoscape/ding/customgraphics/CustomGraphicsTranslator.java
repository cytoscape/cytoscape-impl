package org.cytoscape.ding.customgraphics;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractChartCustomGraphics;
import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.charts.Rotation;
import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.ding.internal.charts.bar.BarChart;
import org.cytoscape.ding.internal.charts.donut.DonutChart;
import org.cytoscape.ding.internal.charts.line.LineChart;
import org.cytoscape.ding.internal.charts.pie.PieChart;
import org.cytoscape.ding.internal.charts.stripe.StripeChart;
import org.cytoscape.ding.internal.charts.util.ColorGradients;
import org.cytoscape.ding.internal.charts.util.ColorKeyword;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.ding.internal.gradients.linear.LinearGradient;
import org.cytoscape.ding.internal.gradients.radial.RadialGradient;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.charts.CyChartFactoryManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientFactory;
import org.cytoscape.view.presentation.gradients.CyGradientFactoryManager;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

@SuppressWarnings("rawtypes")
public class CustomGraphicsTranslator implements ValueTranslator<String, CyCustomGraphics>{

	private static final String ALL = "all";
	private static final String ATTRIBUTELIST = "attributelist";
	private static final String YBASE = "ybase";
	private static final String CLEAR = "clear";
	private static final String CURRENT = "current";
	private static final String LABELCOLOR = "labelcolor";
	private static final String LABELFONT = "labelfont";
	private static final String LABELOFFSET = "labeloffset"; // TODO
	private static final String LABELSTYLE = "labelstyle";
	private static final String LABELSIZE = "labelsize";
	private static final String LABELS = "labellist";
	private static final String LIST = "list";
	private static final String NETWORK = "network";
	private static final String POSITION = "position";
	private static final String RANGE = "range";
	private static final String SCALE = "scale";
	private static final String SIZE = "size";
	private static final String SHOWLABELS = "showlabels";
	private static final String VALUES = "valuelist";
	private static final String COLORS = "colorlist";
	private static final String SEPARATION = "separation";
	
	// Circos/Donut
	private static final String LABELCIRCLES = "labelcircles";
	private static final String SORTSLICES = "sortslices";
	private static final String MINIMUMSLICE = "minimumslice";
	private static final String ARCSTART = "arcstart";
	private static final String FIRSTARC = "firstarc";
	private static final String FIRSTARCWIDTH = "firstarcwidth";
	private static final String ARCWIDTH = "arcwidth";
	
	private static final String	DOWN = "down:";
	private static final String	UP = "up:";
	private static final String	ZERO = "zero:";
	private static final double EPSILON = 1E-8f;
	
	// Gradients
	private static final String	START = "start";
	private static final String	END = "end";
	private static final String STOPLIST = "stoplist";
	
	private final CustomGraphicsManager cgMgr;
	private final CyChartFactoryManager chartMgr;
	private final CyGradientFactoryManager gradMgr;
	
	public CustomGraphicsTranslator(final CustomGraphicsManager cgMgr, final CyChartFactoryManager chartMgr,
			final CyGradientFactoryManager gradMgr) {
		this.cgMgr = cgMgr;
		this.chartMgr = chartMgr;
		this.gradMgr = gradMgr;
	}
	
	@Override
	public CyCustomGraphics translate(String inputValue) {
		// First check if this is a URL
		CyCustomGraphics cg = translateURL(inputValue);
		
		// Bypass format?
//		if (cg == null)
//			cg = parseAsEnhancedGraphics(inputValue);
		
		// Nope, so hand it to each factory that has a matching prefix...
		
		// CyChart serialization format?
		if (cg == null) {
			for (CyChartFactory<?> factory: chartMgr.getAllCyChartFactories()) {
				if (factory.getId() != null && inputValue.startsWith(factory.getId() + ":")) {
					cg = factory.getInstance(inputValue.substring(factory.getId().length() + 1));
					break;
				}
			}
		}
		// CyGradient serialization format?
		if (cg == null) {
			for (CyGradientFactory<?> factory: gradMgr.getAllCyGradientFactories()) {
				if (factory.getId() != null && inputValue.startsWith(factory.getId() + ":")) {
					cg = factory.getInstance(inputValue.substring(factory.getId().length() + 1));
					break;
				}
			}
		}
		// Regular CyCustomGraphics?
		if (cg == null) {
			for (CyCustomGraphicsFactory factory: cgMgr.getAllCustomGraphicsFactories()) {
				if (factory.getPrefix() != null && inputValue.startsWith(factory.getPrefix() + ":")) {
					cg = factory.getInstance(inputValue.substring(factory.getPrefix().length() + 1));
					break;
				}
			}
		}
		
		return cg;
	}

	@Override
	public Class<CyCustomGraphics> getTranslatedValueType() {
		return CyCustomGraphics.class;
	}
	
	private CyCustomGraphics translateURL(String inputValue) {
		try {
			final URL url = new URL(inputValue);
			URLConnection conn = url.openConnection();
			if (conn == null) return null;
			String mimeType = conn.getContentType();
			for (CyCustomGraphicsFactory factory: cgMgr.getAllCustomGraphicsFactories()) {
				if (factory.supportsMime(mimeType)) {
					CyCustomGraphics cg = factory.getInstance(url);
					if (cg != null) return cg;
				}
			}
		
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	/**
	 * Parse CyCharts from the format originally defined in the Enhanced Graphics App
	 * @param input
	 * @return
	 */
	private CyCustomGraphics parseAsEnhancedGraphics(final String input) {
		CyCustomGraphics cg = null;
		Class type = null;
		
		if (input.startsWith("barchart:"))
			type = BarChart.class;
		else if (input.startsWith("circoschart:"))
			type = DonutChart.class;
		else if (input.startsWith("heatstripchart:"))
			type = BarChart.class;
		else if (input.startsWith("linechart:"))
			type = LineChart.class;
		else if (input.startsWith("lingrad:"))
			type = LinearGradient.class;
		else if (input.startsWith("piechart:"))
			type = PieChart.class;
		else if (input.startsWith("radgrad:"))
			type = RadialGradient.class;
		else if (input.startsWith("stripechart:"))
			type = StripeChart.class;
		
		if (type != null) {
			final Map<String, String> args = parseInput(input);
			final Map<String, Object> props = populateValues(args, type);
			
			// Heat Strip is handled as a special case of Bar Chart
			if (input.startsWith("heatstripchart:")) {
				props.put(BarChart.HEAT_STRIPS, true);
				
				if (props.get(BarChart.COLORS) == null)
					props.put(BarChart.COLORS, ColorGradients.YELLOWBLACKCYAN.getColors());
			}
			
			if (CyChart.class.isAssignableFrom(type)) {
				final CyChartFactory<?> factory = chartMgr.getCyChartFactory(type);
				cg = factory.getInstance(props);
			} else if (CyGradient.class.isAssignableFrom(type)) {
				final CyGradientFactory<?> factory = gradMgr.getCyGradientFactory(type);
				cg = factory.getInstance(props);
			}
		}
		
		return cg;
	}
	
	private static Map<String, String> parseInput(final String input) {
		final Map<String, String> props = new HashMap<String, String>();
		
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

	private static final Map<String, Object> populateValues(final Map<String, String> args, final Class type) {
		final Map<String, Object> props = new HashMap<String, Object>();
		
		List<Double> values = parseStringList(args.get(VALUES));
		List<String> labels = getStringList(args.get(LABELS));
		List<String> attributes = null;
		List<Color> colorList = null;
		String colorScheme = null;
		DoubleRange range = null;
		double ybase = 0.5;
		Color labelColor = Color.BLACK;
		int labelSize = ViewUtils.DEFAULT_SIZE;
		String labelFont = ViewUtils.DEFAULT_FONT;
		int labelStyle = ViewUtils.DEFAULT_STYLE;
		boolean normalized = false;
		boolean showLabels = true;
		
		if (args.containsKey(SHOWLABELS))
			showLabels = getBooleanValue(args.get(SHOWLABELS));

		// Font information
		if (args.containsKey(LABELSIZE))
			labelSize = getIntegerValue(args.get(LABELSIZE));
		if (args.containsKey(LABELFONT))
			labelFont = args.get(LABELFONT);
		if (args.containsKey(LABELSTYLE))
			labelStyle = getFontStyle(args.get(LABELSTYLE));
		if (args.containsKey(LABELCOLOR))
			labelColor = getColorValue(args.get(LABELCOLOR));

		double scale = 0.90;
		if (args.containsKey(SCALE)) {
			try {
				scale = getDoubleValue(args.get(SCALE));
			} catch (NumberFormatException e) {
			}
		}

		if (args.containsKey(RANGE)) {
			String split[] = args.get(RANGE).split(",");
			try {
				double min = getDoubleValue(split[0]);
				double max = getDoubleValue(split[1]);
				range = new DoubleRange(min, max);
			} catch (NumberFormatException e) {
			}
		}

		// Get our position
		Object pos = null;
		if (args.containsKey(POSITION)) {
			String position = (String) args.get(POSITION);
			pos = ViewUtils.getPosition(position);
		}

		// Get our size (if we have one)
		// *not used anywhere*
		Rectangle2D size = null;
		if (args.containsKey(SIZE)) {
			String sizeString = (String) args.get(SIZE);
			size = getSize(sizeString);
		} 

		// Get the base of the chart
		if (args.containsKey(YBASE)) {
			String yb = (String) args.get(YBASE);
			if (yb.equalsIgnoreCase("bottom"))
				ybase = 1.0;
			else if (yb.equalsIgnoreCase("top"))
				ybase = 0.0;
			else if (yb.equalsIgnoreCase("middle"))
				ybase = 0.5;
			else {
				try {
					ybase = getDoubleValue(yb);
				} catch (NumberFormatException e) {
					System.err.println("Unable to parse ybase value: "+yb);
					ybase = 0.5;
				}
			}
		}

		if (type == PieChart.class || type == DonutChart.class) {
			double arcStart = 0.0;
			boolean sortSlices = true;
			double minimumSlice = 2.0;
			
			if (args.containsKey(SORTSLICES))
				sortSlices = getBooleanValue(args.get(SORTSLICES));

			// Get our angular offset
			if (args.containsKey(ARCSTART))
				arcStart = getDoubleValue(args.get(ARCSTART));

			if (args.containsKey(LABELCIRCLES))
				showLabels = getBooleanValue(args.get(LABELCIRCLES));
			
			if (type == DonutChart.class) {
				double firstArc = 0.2; // 20% out for first inner arc
				double arcWidth = 0.1; // 10% of node width for arcs
				double firstArcWidth = 0.1; // 10% of node width for arcs
				
				if (args.containsKey(FIRSTARC))
					firstArc = getDoubleValue(args.get(FIRSTARC));
				if (args.containsKey(ARCWIDTH))
					arcWidth = getDoubleValue(args.get(ARCWIDTH));
				
				if (args.containsKey(FIRSTARCWIDTH))
					firstArcWidth = getDoubleValue(args.get(FIRSTARCWIDTH));
				else
					firstArcWidth = arcWidth;
				
				props.put(DonutChart.START_ANGLE, arcStart);
				props.put(DonutChart.HOLE_SIZE, firstArc);
				props.put(DonutChart.ROTATION, Rotation.ANTICLOCKWISE);
			} else {
				props.put(PieChart.START_ANGLE, arcStart);
				props.put(PieChart.ROTATION, Rotation.ANTICLOCKWISE);
			}
		} else if (type == LinearGradient.class || type == RadialGradient.class) {
			Point2D start = new Point2D.Float(0.0f, 0.0f);
			Point2D end = new Point2D.Float(1.0f, 0.0f);

			// Create our defaults
			final List<ControlPoint> stopList = new ArrayList<ControlPoint>();
			int nStops = 0;

			if (args.containsKey(START))
				start = parsePoint(args.get(START));
			if (args.containsKey(END))
				end = parsePoint(args.get(END));
			if (args.containsKey(STOPLIST))
				nStops = parseStopList(args.get(STOPLIST), stopList);
			
			if (nStops == 0) {
				stopList.add(new ControlPoint(new Color(255, 255, 255, 255), 0.0f));
				stopList.add(new ControlPoint(new Color(100, 100, 100, 100), 1.0f));
			}
			
//			props.put(LinearGradient.START, start);
//			props.put(LinearGradient.END, end);
			props.put(LinearGradient.STOP_LIST, stopList);
		} else if (type == BarChart.class) {
			double separation = 0;
			
			if (args.containsKey(SEPARATION))
				separation =  Integer.parseInt(args.get(SEPARATION));
			
			if (separation > 0.0) separation /= 100;
			if (separation > 0.5) separation = 0.5;
			if (separation < 0) separation = 0.0;
			
			props.put(BarChart.SEPARATION, separation);
		}
		
		if (args.containsKey(ATTRIBUTELIST))
			attributes = getStringList(args.get(ATTRIBUTELIST));
		
		if (args.containsKey(COLORS)) {
			colorList = convertInputToColor(args.get(COLORS), values, range);
			colorScheme = convertInputToColorScheme(args.get(COLORS));
		}
		
		if (!showLabels)
			labels = null;
		if (attributes != null && (labels == null || labels.isEmpty()))
			labels = attributes;
		
		// Set actual CyChart/Gradient properties
		if (attributes != null)
			props.put(AbstractChartCustomGraphics.DATA_COLUMNS, attributes);
		if (values != null)
			props.put(AbstractChartCustomGraphics.VALUES, values);
		if (range != null)
			props.put(AbstractChartCustomGraphics.RANGE, range);
		if (colorList != null && !colorList.isEmpty())
			props.put(AbstractChartCustomGraphics.COLORS, colorList);
		if (colorScheme != null)
			props.put(AbstractChartCustomGraphics.COLOR_SCHEME, colorScheme);
		if (labels != null)
			props.put(AbstractChartCustomGraphics.ITEM_LABELS, labels);
		
		props.put(AbstractChartCustomGraphics.SHOW_ITEM_LABELS, showLabels);
		props.put(AbstractChartCustomGraphics.SHOW_DOMAIN_AXIS, false);
		props.put(AbstractChartCustomGraphics.SHOW_RANGE_AXIS, false);
		
		return props;
	}
	
	private static List<Double> parseStringList(String input)  {
		if (input == null)
			return null;
		String[] inputArray = ((String)input).split(",");
		return convertStringList(Arrays.asList(inputArray));
	}
	
	private static List<Double> convertStringList(List<String> input)  {
		List<Double> values = new ArrayList<Double>(input.size());
		for (String s: input) {
			try {
				Double d = Double.valueOf(s);
				values.add(d);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return values;
	}
	
	private static List<Color> convertInputToColor(final String input, List<Double>values, DoubleRange range)  {
		int nColors = values != null ? values.size() : 0;
		
		// give the default: contrasting colors
		if (input == null && nColors > 0)
			return ColorUtil.generateContrastingColors(nColors);

		// OK, we have three possibilities.  The input could be a keyword, a comma-separated list of colors, or
		// a list of Color objects.  We need to figure this out first...
		// See if we have a CSV
		String [] colorArray = input.split(",");
		
		// Look for up/down special case
		if (colorArray.length == 2 &&
		    (colorArray[0].toLowerCase().startsWith(UP) || colorArray[0].toLowerCase().startsWith(DOWN))) {
			return parseUpDownColor(colorArray);
		} else if (colorArray.length == 3 &&
		    (colorArray[0].toLowerCase().startsWith(UP) || colorArray[0].toLowerCase().startsWith(DOWN) ||
		     colorArray[0].toLowerCase().startsWith(ZERO))) {
			return parseUpDownColor(colorArray);
		} else if (colorArray.length > 1) {
			return parseColorList(colorArray);
		} else {
			return ColorUtil.parseColorKeyword(input.trim(), nColors);
		}
	}
	
	private static String convertInputToColorScheme(final String input)  {
		if (input == null)
			return ColorUtil.CONTRASTING;

		// OK, we have three possibilities.  The input could be a keyword, a comma-separated list of colors, or
		// a list of Color objects.  We need to figure this out first...
		// See if we have a CSV
		String [] colorArray = input.split(",");
		
		// Look for up/down special case
		if (colorArray.length == 2 &&
		    (colorArray[0].toLowerCase().startsWith(UP) || colorArray[0].toLowerCase().startsWith(DOWN))) {
			return ColorUtil.UP_DOWN;
		} else if (colorArray.length == 3 &&
		    (colorArray[0].toLowerCase().startsWith(UP) || colorArray[0].toLowerCase().startsWith(DOWN) ||
		     colorArray[0].toLowerCase().startsWith(ZERO))) {
			return ColorUtil.UP_DOWN;
		} else if (colorArray.length > 1) {
			return ColorUtil.CUSTOM;
		} else {
			return input.trim();
		}
	}
	
	private static List<Color> parseUpDownColor(String[] colorArray)  {
		if (colorArray.length < 2)
			return null;

		String [] colors = new String[3];
		colors[2] = "black";
		
		for (int index = 0; index < colorArray.length; index++) {
			if (colorArray[index].toLowerCase().startsWith(UP))
				colors[0] = colorArray[index].substring(UP.length());
			else if (colorArray[index].toLowerCase().startsWith(DOWN))
				colors[1] = colorArray[index].substring(DOWN.length());
			else if (colorArray[index].toLowerCase().startsWith(ZERO))
				colors[2] = colorArray[index].substring(ZERO.length());
		}
		
		return parseColorList(colors);
	}
	
	private static Color scaleColor(double v, Color zero, Color c, DoubleRange range) {
		if (range == null || range.min == 0.0 || range.max == 0.0)
			return c;

		// We want to scale our color to be between "zero" and "c"
		// v = 1-v;
		int b = (int)(c.getBlue()*v + zero.getBlue()*(1-v));
		int r = (int)(c.getRed()*v + zero.getRed()*(1-v));
		int g = (int)(c.getGreen()*v + zero.getGreen()*(1-v));

		return new Color(r, g, b);
	}
	
	private static List<String> getStringList(String input) {
		if (input == null || input.length() == 0)
			return new ArrayList<String>();

		String[] inputArray = ((String)input).split(",",-1);
		return Arrays.asList(inputArray);
	}
	
	private static boolean getBooleanValue(Object input) {
		if (input instanceof Boolean)
			return ((Boolean)input).booleanValue();
		return Boolean.parseBoolean(input.toString());
	}

	private static int getFontStyle(String input) {
		if (input.equalsIgnoreCase("italics"))
			return Font.ITALIC;
		if (input.equalsIgnoreCase("bold"))
			return Font.BOLD;
		if (input.equalsIgnoreCase("bolditalic"))
			return Font.ITALIC|Font.BOLD;
		return Font.PLAIN;
	}

	private static Color getColorValue(String input) {
		String [] colorArray = new String[1];
		colorArray[0] = input;
		List<Color> colors = parseColorList(colorArray);
		return colors.get(0);
	}
	
	private static double getDoubleValue(Object input) throws NumberFormatException {
		if (input instanceof Double)
			return ((Double)input).doubleValue();
		else if (input instanceof Integer)
			return ((Integer)input).doubleValue();
		else if (input instanceof String)
			return Double.parseDouble((String)input);
		throw new NumberFormatException("input can not be converted to double");
	}
	
	private static int getIntegerValue(Object input) throws NumberFormatException {
		if (input instanceof Integer)
			return ((Integer)input).intValue();
		else if (input instanceof Integer)
			return ((Integer)input).intValue();
		else if (input instanceof String)
			return Integer.parseInt((String)input);
		throw new NumberFormatException("input can not be converted to integer");
	}
	
	private static Rectangle2D getSize(Object input)  {
		if (input instanceof Rectangle2D) {
			return (Rectangle2D) input;
		} else if (input instanceof Double) {
			double v = ((Double)input).doubleValue();
			return new Rectangle2D.Double(0.0,0.0,v,v);
		} else if (input instanceof Integer) {
			double v = ((Integer)input).doubleValue();
			return new Rectangle2D.Double(0.0,0.0,v,v);
		} else if (input instanceof String) {
			String inputString = (String)input;
			String[] sizes = inputString.split("[xX]");
			if (sizes.length == 1) {
				double v = Double.parseDouble(sizes[0]);
				return new Rectangle2D.Double(0.0,0.0,v,v);
			} else if (sizes.length == 2) {
				double h = Double.parseDouble(sizes[0]);
				double w = Double.parseDouble(sizes[1]);
				return new Rectangle2D.Double(0.0,0.0,w,h);
			} 
		}
		return null;
	}
	
	private static List<Color> parseColorList(String[] inputArray)  {
		List<Color> colors = new ArrayList<Color>();
		// A color in the array can either be a hex value or a text color
		for (String colorString: inputArray) {
			colorString = colorString.trim();
			if (colorString.matches("^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})$")) {
				// We have a hex value with either 6 (rgb) or 8 (rgba) digits
				int r = Integer.parseInt(colorString.substring(1,3), 16);
				int g = Integer.parseInt(colorString.substring(3,5), 16);
				int b = Integer.parseInt(colorString.substring(5,7), 16);
				if (colorString.length() > 7) {
					int a = Integer.parseInt(colorString.substring(7,9), 16);
					colors.add(new Color(r,g,b,a));
				} else {
					colors.add(new Color(r,g,b));
				}
			} else {
				// Check for color string
				Color c = ColorKeyword.getColor(colorString);
				if (c == null)
					return null;
				colors.add(c);
			}
		}
		return colors;
	}
	
	private static Point2D parsePoint(String point) {
		if (point == null || point.length() == 0)
			return null;

		String tokens[] = point.split(",");
		float x = Float.parseFloat(tokens[0].trim());
		float y = Float.parseFloat(tokens[1].trim());
		
		return new Point2D.Float(x,y);
	}
	
	// Parse out a stop list.  The stoplist is of the form:
	// 	r,g,b,a,stop|r,g,b,a,stop...
	private static int parseStopList(final String stoplist, final List<ControlPoint> stops) {
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
			
			stops.add(new ControlPoint(new Color(r, g, b, a), stop));
			nStops++;
		}
		
		return nStops;
	}
	
	private static double normalize(double v, DoubleRange range) {
		if (range == null || range.min == 0.0 || range.max == 0.0)
			return v;
		
		final double min = range.min;
		final double max = range.max;
		double delta = max-min;
		double val = 0.0;

		// Clamp v
		if (v < min) v = min;
		if (v > max) v = max;

		if ((min > 0.0 && max > 0.0) || (min < 0.0 && max < 0.0))
			val = (v - min) / delta;
		else if (v < 0.0 && min < 0.0)
			val = -(v / min);
		else if (v > 0.0 && max > 0.0)
			val = (v / max);

		return val;
	}
	
//	/**
//	 * Create a custom graphics from the given URL string.
//	 * This code try to access the data source and download the image.
//	 * 
//	 * @param value String representation of image source URL.
//	 * 
//	 * @return Image Custom Graphics created from the source image.
//	 */
//	private final CyCustomGraphics parse(String value) {
//		if(value == null)
//			return null;
//
//		// TODO: this needs to be made generic.  If we have a URL, then we can
//		// hand it to the appropriate factory
//		try {
//			final URL url = new URL(value);
//			CyCustomGraphics graphics = cgMgr.getCustomGraphicsBySourceURL(url);
//			if(graphics == null) {
//				// Currently not in the Manager.  Need to create new instance.
//				graphics = new URLImageCustomGraphics(cgMgr.getNextAvailableID(), url.toString());
//				// Use URL as display name
//				graphics.setDisplayName(value);
//				
//				// Register to manager.
//				cgMgr.addCustomGraphics(graphics, url);
//			}
//			return graphics;
//		} catch (IOException e) {
//			return null;			
//		}
//	}
}
