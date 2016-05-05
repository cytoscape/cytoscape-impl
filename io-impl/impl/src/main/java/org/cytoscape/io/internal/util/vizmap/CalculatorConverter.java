package org.cytoscape.io.internal.util.vizmap;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.io.internal.util.vizmap.model.AttributeType;
import org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping;
import org.cytoscape.io.internal.util.vizmap.model.ContinuousMappingPoint;
import org.cytoscape.io.internal.util.vizmap.model.Dependency;
import org.cytoscape.io.internal.util.vizmap.model.DiscreteMapping;
import org.cytoscape.io.internal.util.vizmap.model.DiscreteMappingEntry;
import org.cytoscape.io.internal.util.vizmap.model.PassthroughMapping;
import org.cytoscape.io.internal.util.vizmap.model.VisualProperty;
import org.cytoscape.io.internal.util.vizmap.model.VisualStyle;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;

/**
 * Converter for Cytoscape 2.x calculators, which are serialized as a properties file
 * (session_vizmap.props file in the .cys file).
 * @author Christian
 * @param <T>
 */
public class CalculatorConverter {

	/** This type corresponds to java.lang.Boolean. */
	public static final byte TYPE_BOOLEAN = 1;
	/** This type corresponds to java.lang.Double. */
	public static final byte TYPE_FLOATING_POINT = 2;
	/** This type corresponds to java.lang.Integer. */
	public static final byte TYPE_INTEGER = 3;
	/** This type corresponds to java.lang.String. */
	public static final byte TYPE_STRING = 4;
	/** This type corresponds to an attribute which has not been defined. */
	public static final byte TYPE_UNDEFINED = -1;
	/**
	 * This type corresponds to a 'simple' list.
	 * <P>
	 * A 'simple' list is defined as follows:
	 * <UL>
	 * <LI>All items within the list are of the same type, and are chosen from
	 * one of the following: <CODE>Boolean</CODE>, <CODE>Integer</CODE>,
	 * <CODE>Double</CODE> or <CODE>String</CODE>.
	 * </UL>
	 */
	public static final byte TYPE_SIMPLE_LIST = -2;

	private String propsKey;
	private String legacyPropsKey;
	
	private String key;
	private String legacyKey;
	private String visualPropertyId;
	private Class<? extends CyIdentifiable> targetType;

	private static final Map<Class<? extends CyIdentifiable>, Map<String, CalculatorConverter>> converters;

	/** old_style -> new_style */
	private static final Map<String, String> oldLineStyles;
	/** old_color -> new_color */
	private static final Map<String, String> oldArrowColors;

	static {
		converters = new HashMap<>();
		converters.put(CyNode.class, new HashMap<>());
		converters.put(CyEdge.class, new HashMap<>());
		converters.put(CyNetwork.class, new HashMap<>());

		oldLineStyles = new HashMap<>();
		oldLineStyles.put("", "SOLID");
		oldLineStyles.put("LINE", "SOLID");
		oldLineStyles.put("DASHED", "EQUAL_DASH");

		oldArrowColors = new HashMap<>();
		oldArrowColors.put("WHITE", "255,255,255");
		oldArrowColors.put("BLACK", "0,0,0");
	}

	CalculatorConverter(final String propsKey, String legacyPropsKey) {
		this.propsKey = propsKey;
		
		// Make legacy key null if it's actually the same as the new key, so we know it was never an old key
		legacyPropsKey = propsKey.equalsIgnoreCase(legacyPropsKey) ? null : legacyPropsKey;
		this.legacyPropsKey = legacyPropsKey;
		
		this.key = propsKey.split("\\.")[2];
		this.legacyKey = legacyPropsKey != null ? legacyPropsKey.split("\\.")[2] : null;
		
		this.visualPropertyId = parseVisualPropertyId(key);
		this.targetType = parseTargetDataType(key);
	}

	public void convert(VisualStyle vs, String propsValue, Properties props) {
		if (isDependency(propsKey)) {
			setDependency(vs, propsValue);
		} else if (isDefaultProperty(propsKey)) {
			// e.g. "globalAppearanceCalculator.MyStyle.defaultBackgroundColor"
			setDefaultProperty(vs, propsValue);
		} else if (isMappingFunction(propsKey)) {
			// e.g. "edgeAppearanceCalculator.MyStyle.edgeColorCalculator"
			setMappingFunction(vs, propsValue, props);
		}
	}

	// Accessors --------------

	/**
	 * @return The key used in the Visual Lexicon lookup.
	 */
	public String getVisualPropertyId() {
		return visualPropertyId;
	}
	
	// Private methods --------------

	private String updateLegacyValue(String value) {
		if (value != null) {
			if (legacyPropsKey != null) {
				// The value is from older calculators and need to be updated!
				if (key.matches("(?i)(default)?(node|edge)LineStyle(Calculator)?")) {
					// Convert the former line type (e.g. LINE_1, LINE_2, DASHED_1) to
					// current line style
					String[] vv = value.split("_");
					if (vv.length == 2) return oldLineStyles.get(vv[0]);
				} else if (key.matches("(?i)(default)?(node|edge)LineWidth(Calculator)?")) {
					// Convert the former line type to current line width
					String[] vv = value.split("_");
					if (vv.length == 2) return vv[1];
				} else if (key.matches("(?i)(default)?Edge(Source|Target)ArrowColor(Calculator)?")) {
					// Convert the former arrow property value (e.g. NONE,
					// WHITE_DIAMOND, BLACK_DIAMOND) to color
					if (!value.equalsIgnoreCase("NONE")) {
						String[] vv = value.split("_");
						if (vv.length == 2) return oldArrowColors.get(vv[0]);
					}
				} else if (key.matches("(?i)(default)?Edge(Source|Target)ArrowShape(Calculator)?")) {
					// Convert the former arrow property value to shape
					if (value.equalsIgnoreCase("NONE")) {
						return "NONE";
					} else {
						String[] vv = value.split("_");
						if (vv.length == 2) return vv[1];
					}
				} else {
					return value;
				}
			} else {
				// No need to update the value
				return value;
			}
		}

		return null;
	}

	/**
	 * @param props
	 *            All the visual properties
	 * @param mapperName
	 *            Example: "MyStyle-Edge Color-Discrete Mapper"
	 * @return A {@link org.cytoscape.io.internal.util.vizmap.model.DiscreteMapping}, 
	 *           {@link org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping} or
	 *           {@link org.cytoscape.io.internal.util.vizmap.model.PassThroughMapping} object
	 */
	private Object getMappingFunction(Properties props, String mapperName, VisualProperty vp) {
		String baseKey = null;
		String functionType = null;
		
		while (functionType == null) {
			// e.g. "edgeColorCalculator.MyStyle-Edge Color-Discrete Mapper.mapping."
			baseKey = (legacyKey != null ? legacyKey : key) + "." + mapperName + ".mapping.";
			functionType = props.getProperty(baseKey + "type");
			
			if (functionType == null) {
				// Try with Cytoscape v2.3 calculator names...
				if (baseKey.startsWith("nodeFillColorCalculator.")) {
					legacyKey = "nodeColorCalculator";
				} else if (baseKey.startsWith("edgeSourceArrowCalculator.") || 
						   baseKey.startsWith("edgeTargetArrowCalculator.")) {
					legacyKey = "edgeArrowCalculator";
				} else {
					return null;
				}
			}
		}
		
		String attrName = props.getProperty(baseKey + "controller");
		
		if (attrName == null)
			return null;

		// "ID" is actually the "name" column!!!
		if ("ID".equalsIgnoreCase(attrName)) attrName = "name";

		if ("DiscreteMapping".equalsIgnoreCase(functionType)) {
			String controllerTypeProp = props.getProperty(baseKey + "controllerType");
			byte controllerType = controllerTypeProp != null ? Byte.parseByte(controllerTypeProp) : TYPE_STRING;
			AttributeType attrType = null;

			if (attrName.equals("has_nested_network")) {
				// Force to boolean (in Cy2, this attribute is a "yes\no" string type)
				attrType = AttributeType.BOOLEAN;
			} else {
				switch (controllerType) {
					case TYPE_BOOLEAN:
						attrType = AttributeType.BOOLEAN;
						break;
					case TYPE_FLOATING_POINT:
						attrType = AttributeType.FLOAT;
						break;
					case TYPE_INTEGER:
						attrType = AttributeType.INTEGER;
						break;
					default:
						attrType = AttributeType.STRING;
						break;
				}
			}

			DiscreteMapping dm = new DiscreteMapping();
			dm.setAttributeName(attrName);
			dm.setAttributeType(attrType);

			String entryKey = baseKey + "map.";

			for (String sk : props.stringPropertyNames()) {
				if (sk.contains(entryKey)) {
					String attrValue = sk.replace(entryKey, "");
					String sv = props.getProperty(sk);
					String value = getValue(sv);

					if (attrName.equals("has_nested_network"))
						attrValue = attrValue.matches("(?i)yes|true") ? "true" : "false";
					
					DiscreteMappingEntry entry = new DiscreteMappingEntry();
					entry.setAttributeValue(attrValue);
					entry.setValue(value);

					dm.getDiscreteMappingEntry().add(entry);
				}
			}

			return dm;
		} else if ("ContinuousMapping".equalsIgnoreCase(functionType)) {
			ContinuousMapping cm = new ContinuousMapping();
			cm.setAttributeName(attrName);
			cm.setAttributeType(AttributeType.FLOAT);

			int boundaryValues = 0;

			try {
				String s = props.getProperty(baseKey + "boundaryvalues");
				boundaryValues = Integer.parseInt(s);
			} catch (NumberFormatException nfe) {
				// TODO: warning
			}

			for (int i = 0; i < boundaryValues; i++) {
				try {
					BigDecimal value = new BigDecimal(props.getProperty(baseKey + "bv" + i + ".domainvalue"));
					String lesser = getValue(props.getProperty(baseKey + "bv" + i + ".lesser"));
					String equal = getValue(props.getProperty(baseKey + "bv" + i + ".equal"));
					String greater = getValue(props.getProperty(baseKey + "bv" + i + ".greater"));

					ContinuousMappingPoint point = new ContinuousMappingPoint();
					point.setAttrValue(value);
					point.setLesserValue(lesser);
					point.setEqualValue(equal);
					point.setGreaterValue(greater);

					cm.getContinuousMappingPoint().add(point);
				} catch (Exception e) {
					// TODO: warning
				}
			}

			return cm;
		} else if ("PassThroughMapping".equalsIgnoreCase(functionType)) {
			PassthroughMapping pm = new PassthroughMapping();
			pm.setAttributeName(attrName);
			// Cy2 doesn't write the "controllerType" property for PassThroughMappings, so just set STRING
			// (it should work, anyway).
			pm.setAttributeType(AttributeType.STRING);

			return pm;
		}

		return null;
	}

	private void setDefaultProperty(VisualStyle vs, String sValue) {
		VisualProperty vp = getVisualProperty(vs);
		String value = getValue(sValue);
		vp.setDefault(value);
	}

	private void setMappingFunction(VisualStyle vs, String sValue, Properties props) {
		VisualProperty vp = getVisualProperty(vs);
		Object mapping = getMappingFunction(props, sValue, vp);

		if (mapping instanceof PassthroughMapping)
			vp.setPassthroughMapping((PassthroughMapping) mapping);
		else if (mapping instanceof ContinuousMapping)
			vp.setContinuousMapping((ContinuousMapping) mapping);
		else if (mapping instanceof DiscreteMapping)
			vp.setDiscreteMapping((DiscreteMapping) mapping);
	}

	private void setDependency(VisualStyle vs, String sValue) {
		Dependency d = new Dependency();
		d.setName(visualPropertyId);
		d.setValue(new Boolean(sValue.trim()));

		if (targetType == CyNetwork.class) {
			vs.getNetwork().getDependency().add(d);
		} else if (targetType == CyNode.class) {
			vs.getNode().getDependency().add(d);
		} else if (targetType == CyEdge.class) {
			vs.getEdge().getDependency().add(d);
		}
	}

	/**
	 * Get or create a new Visual Property for the passed Visual Style.
	 * It checks if the property already exists in Network, Nodes or Edges, according to the dataType. If it does not
	 * exist, the property is created and added to the style.
	 * @param vs
	 * @return
	 */
	private VisualProperty getVisualProperty(VisualStyle vs) {
		VisualProperty vp = null;
		List<VisualProperty> vpList = null;

		if (targetType == CyNetwork.class) {
			vpList = vs.getNetwork().getVisualProperty();
		} else if (targetType == CyNode.class) {
			vpList = vs.getNode().getVisualProperty();
		} else if (targetType == CyEdge.class) {
			vpList = vs.getEdge().getVisualProperty();
		}

		for (VisualProperty v : vpList) {
			if (v.getName().equalsIgnoreCase(visualPropertyId)) {
				// The Visual Property has already been created...
				vp = v;
				break;
			}
		}

		if (vp == null) {
			// The Visual Property has not been created yet...
			vp = new VisualProperty();
			vp.setName(visualPropertyId);
			vpList.add(vp);
		}

		return vp;
	}

	private String getValue(String sValue) {
		if (sValue != null) sValue = updateLegacyValue(sValue);

		return sValue;
	}
	
	// static parsing methods --------------
	
	/**
	 * @param key the Properties key
	 * @return The name of the visual style or null if the property key doesn't or shouldn't have it
	 */
	public static String parseStyleName(String key) {
		String styleName = null;

		if (key != null) {
			String[] tokens = key.split("\\.");

			if (tokens.length > 2 && tokens[0].matches("(node|edge|global)[a-zA-Z]+Calculator")) {
				// It seems to be a valid entry...
				if (tokens.length == 3) {
					String t3 = tokens[2];

					if (t3.matches("nodeSizeLocked|arrowColorMatchesEdge|nodeLabelColorFromNodeColor|"
								   + "defaultNodeShowNestedNetwork|nodeCustomGraphicsSizeSync|"
								   + "((node|edge)LabelColor)|" + "((node|edge)[a-zA-Z]+Calculator)|"
								   + "(default(Node|Edge|Background|SloppySelection)[a-zA-Z0-9]+)")) {
						// It looks like the second token is the style name!
						styleName = tokens[1];
					}
				}
			}
		}

		return styleName;
	}
	
	static boolean isConvertible(String propsKey) {
		return isDefaultProperty(propsKey) || isDependency(propsKey) || isMappingFunction(propsKey);
	}
	
	static boolean isDefaultProperty(String key) {
		boolean b = false;

		if (key != null) {
			// Globals
			b |= key.matches("globalAppearanceCalculator\\.[^\\.]+\\.default[a-zA-Z]+Color");
			// Nodes
			b |= key.matches("nodeAppearanceCalculator\\.[^\\.]+\\.default(Node|NODE)\\w+");
			// Edges
			b |= key.matches("edgeAppearanceCalculator\\.[^\\.]+\\.default(Edge|EDGE)[a-zA-Z_]+");
		}

		return b;
	}

	static boolean isMappingFunction(String key) {
		boolean b = false;

		if (key != null) {
			b |= key.matches("(?i)(node|edge)AppearanceCalculator\\.[^\\.]+\\."
							 + "\\1((CustomGraphics(Position)?\\d+)|LabelColor|([a-zA-Z_]+Calculator))");
		}

		return b;
	}

	static boolean isDependency(String key) {
		boolean b = false;

		if (key != null) {
			// We need to distinguish node dependencies from edge dependencies because in
			// some old versions of vizmap.props (e.g. 2.5 era) we would see a dependency
			// (e.g. nodeSizeLocked) listed under both nodeAppearanceCalculator and
			// edgeAppearanceCalculator, which means two Dependency objects get mapped
			// to the same "nodeSizeLocked" VisualPropertyDependency.
			b |= key.matches("nodeAppearanceCalculator\\.[^\\.]+\\." + 
			                 "(nodeSizeLocked|nodeLabelColorFromNodeColor|nodeCustomGraphicsSizeSync)");
			b |= key.matches("edgeAppearanceCalculator\\.[^\\.]+\\.arrowColorMatchesEdge");
		}

		return b;
	}
	
	static Class<? extends CyIdentifiable> parseTargetDataType(String calcKey) {
		calcKey = calcKey.toLowerCase();

		if (calcKey.contains("node")) return CyNode.class;
		if (calcKey.contains("edge")) return CyEdge.class;

		return CyNetwork.class;
	}
	
	static String parseVisualPropertyId(String calcKey) {
		if (calcKey != null) {
			return calcKey.replaceAll("(?i)default|calculator|uniform", "").toLowerCase().trim();
		}

		return calcKey;
	}

	@Override
	public String toString() {
		return "CalculatorConverter [propsKey=" + propsKey
				+ ", legacyPropsKey=" + legacyPropsKey + ", key=" + key
				+ ", legacyKey=" + legacyKey + ", visualPropertyId="
				+ visualPropertyId + ", targetType=" + targetType + "]";
	}
}
