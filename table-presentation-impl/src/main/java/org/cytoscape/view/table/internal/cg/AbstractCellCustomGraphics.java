package org.cytoscape.view.table.internal.cg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.table.internal.cg.json.ColorJsonDeserializer;
import org.cytoscape.view.table.internal.cg.json.ColorJsonSerializer;
import org.cytoscape.view.table.internal.cg.json.ColorSchemeJsonDeserializer;
import org.cytoscape.view.table.internal.cg.json.ColorSchemeJsonSerializer;
import org.cytoscape.view.table.internal.cg.json.Point2DJsonDeserializer;
import org.cytoscape.view.table.internal.cg.json.Point2DJsonSerializer;
import org.cytoscape.view.table.internal.cg.json.PropertiesJsonDeserializer;
import org.cytoscape.view.table.internal.cg.json.PropertiesJsonSerializer;
import org.cytoscape.view.table.internal.cg.json.Rectangle2DJsonDeserializer;
import org.cytoscape.view.table.internal.cg.json.Rectangle2DJsonSerializer;
import org.jfree.util.Rotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class AbstractCellCustomGraphics implements CellCustomGraphics {

	/**
	 * The list of colors, one for each chart element
	 */
	public static final String COLORS = "cy_colors";
	public static final String COLOR_SCHEME = "cy_colorScheme";
	public static final String ORIENTATION = "cy_orientation";
	public static final String ROTATION = "cy_rotation";
	
	protected Long id;
	protected float fitRatio = 0.9f;
	protected String displayName;
	protected int width = 50;
	protected int height = 50;
	
	private final Map<String, Object> properties;
	
	private ObjectMapper mapper;
	
	protected final Logger logger;

	protected AbstractCellCustomGraphics(String displayName) {
		logger = LoggerFactory.getLogger(CyUserLog.NAME);
		this.displayName = displayName;
		this.properties = new HashMap<String, Object>();
	}
	
	protected AbstractCellCustomGraphics(String displayName, String input) {
		this(displayName);
		addProperties(parseInput(input));
	}
	
	protected AbstractCellCustomGraphics(String displayName, Map<String, ?> properties) {
		this(displayName);
		addProperties(properties);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		var map = new LinkedHashMap<String, Object>();
		
		// Make sure the returned map does not contain types not exposed in the API
		for (var entry : properties.entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			
			if (value instanceof ColorScheme)
				value = ((ColorScheme) value).getKey();
			else if (value instanceof Enum)
				value = value.toString();
			
			map.put(key, value);
		}
		
		return map;
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
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

//	@Override
//	public void setWidth(final int width) {
//		this.width = width;
//	}
//
//	@Override
//	public void setHeight(final int height) {
//		this.height = height;
//	}
//
//	@Override
//	public int getWidth() {
//		return width;
//	}
//
//	@Override
//	public int getHeight() {
//		return height;
//	}
//
//	@Override
//	public float getFitRatio() {
//		return fitRatio;
//	}
//
//	@Override
//	public void setFitRatio(float fitRatio) {
//		this.fitRatio = fitRatio;
//	}

	@Override
	public String toString() {
		return displayName;
	}

	@Override
	public String getSerializableString() {
		String output = "";
		
		try {
			var om = getObjectMapper();
			output = om.writeValueAsString(this.properties);
			output = getId() + ":" + output;
		} catch (JsonProcessingException e) {
			logger.error("Cannot create JSON from custom graphics", e);
		}
		
		return output;
	}
	
	public abstract String getId();
	
	public synchronized void set(String key, Object value) {
		if (key == null)
			throw new IllegalArgumentException("'key' must not be null.");
		
		var type = getSettingType(key);
		
		if (type != null) {
			if (value != null) {
				// It's OK; just take the value as it is.
				boolean correctType = 
						type == Array.class &&
						value.getClass().isArray() &&
						value.getClass().getComponentType() == getSettingElementType(key);
				correctType = correctType || type.isAssignableFrom(value.getClass());
				
				if (!correctType) {
					var om = getObjectMapper();
					String json = value.toString();
					
					if (type != List.class) {
						try {
							json = om.writeValueAsString(value);
						} catch (JsonProcessingException e) {
							logger.error("Cannot parse JSON field " + key, e);
						}
					}
					
					value = PropertiesJsonDeserializer.readValue(key, json, om, this);
				}
			}
			
			properties.put(key, value);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <S> S get(String key, Class<S> cls) {
		var obj = properties.get(key);
		
		return obj != null && cls.isAssignableFrom(obj.getClass()) ? (S) obj : null;
	}
	
	public synchronized <S> S get(String key, Class<S> cls, S defValue) {
		S value = get(key, cls);
		return value != null ? value : defValue;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized <S> List<S> getList(String key, Class<S> cls) {
		var obj = properties.get(key);
		
		return obj instanceof List ? (List)obj : Collections.emptyList();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <S> S[] getArray(String key, Class<S> cls) {
		var obj = properties.get(key);
		S[] arr = null;
		
		try {
			arr = (obj != null && obj.getClass().isArray()) ? (S[])obj : null;
		} catch (ClassCastException e) {
			logger.error("Cannot cast property '" + key + "' to array.", e);
		}
		
		return arr;
	}
	
	public synchronized float[] getFloatArray(String key) {
		var obj = properties.get(key);
		
		try {
			return (float[]) obj;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	public synchronized double[] getDoubleArray(String key) {
		var obj = properties.get(key);
		
		try {
			return (double[]) obj;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> parseInput(String input) {
		var props = new HashMap<String, Object>();
		
		if (input != null && !input.isEmpty()) {
			try {
				var om = getObjectMapper();
				Map<String, Object> map = om.readValue(input, Map.class);
				
				if (map != null) {
					for (var entry : map.entrySet()) {
						var key = entry.getKey();
						var value = entry.getValue();
						props.put(key, value);
					}
				}
			} catch (Exception e) {
				logger.error("Cannot parse JSON: " + input, e);
			}
		}
		
		return props;
	}
	
	public Class<?> getSettingType(String key) {
		if (key.equalsIgnoreCase(COLORS)) return List.class;
		if (key.equalsIgnoreCase(COLOR_SCHEME)) return ColorScheme.class;
		if (key.equalsIgnoreCase(ORIENTATION)) return Orientation.class;
		if (key.equalsIgnoreCase(ROTATION)) return Rotation.class;
			
		return null;
	}
	
	public Class<?> getSettingElementType(String key) {
		if (key.equalsIgnoreCase(COLORS)) return Color.class;
		
		return Object.class;
	}
	
	protected void addProperties(Map<String, ?> properties) {
		if (properties != null) {
			for (var entry : properties.entrySet()) {
				if (getSettingType(entry.getKey()) != null)
					set(entry.getKey(), entry.getValue());
			}
		}
	}

	protected void addJsonSerializers(SimpleModule module) {
		module.addSerializer(new PropertiesJsonSerializer());
		module.addSerializer(new ColorSchemeJsonSerializer());
		module.addSerializer(new ColorJsonSerializer());
		module.addSerializer(new Point2DJsonSerializer());
		module.addSerializer(new Rectangle2DJsonSerializer());
	}
	
	protected void addJsonDeserializers(SimpleModule module) {
		module.addDeserializer(Map.class, new PropertiesJsonDeserializer(this));
		module.addDeserializer(ColorScheme.class, new ColorSchemeJsonDeserializer());
		module.addDeserializer(Color.class, new ColorJsonDeserializer());
		module.addDeserializer(Point2D.class, new Point2DJsonDeserializer());
		module.addDeserializer(Rectangle2D.class, new Rectangle2DJsonDeserializer());
	}
	
	private ObjectMapper getObjectMapper() {
		// Lazy initialization of ObjectMapper, to make sure any other instance property is already initialized
		if (mapper == null) {
			var module = new SimpleModule();
			addJsonSerializers(module);
			addJsonDeserializers(module);
			
			mapper = new ObjectMapper();
			mapper.registerModule(module);
		}
		
		return mapper;
	}

	public void draw(Graphics g, Rectangle2D bounds, CyColumn column, CyRow row) {
		// TODO Auto-generated method stub
		
	}
}
