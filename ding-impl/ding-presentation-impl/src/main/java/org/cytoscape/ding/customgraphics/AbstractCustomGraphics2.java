package org.cytoscape.ding.customgraphics;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.ding.customgraphics.json.ColorJsonDeserializer;
import org.cytoscape.ding.customgraphics.json.ColorJsonSerializer;
import org.cytoscape.ding.customgraphics.json.ColorSchemeJsonDeserializer;
import org.cytoscape.ding.customgraphics.json.ColorSchemeJsonSerializer;
import org.cytoscape.ding.customgraphics.json.Point2DJsonDeserializer;
import org.cytoscape.ding.customgraphics.json.Point2DJsonSerializer;
import org.cytoscape.ding.customgraphics.json.PropertiesJsonDeserializer;
import org.cytoscape.ding.customgraphics.json.PropertiesJsonSerializer;
import org.cytoscape.ding.customgraphics.json.Rectangle2DJsonDeserializer;
import org.cytoscape.ding.customgraphics.json.Rectangle2DJsonSerializer;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * 
 */
public abstract class AbstractCustomGraphics2<T extends CustomGraphicLayer> implements CyCustomGraphics2<T> {

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

	protected AbstractCustomGraphics2(final String displayName) {
		logger = LoggerFactory.getLogger(this.getClass());
		this.displayName = displayName;
		this.properties = new HashMap<String, Object>();
	}
	
	protected AbstractCustomGraphics2(final String displayName, final String input) {
		this(displayName);
		addProperties(parseInput(input));
	}
	
	protected AbstractCustomGraphics2(final String displayName, final Map<String, ?> properties) {
		this(displayName);
		addProperties(properties);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		final Map<String, Object> map = new LinkedHashMap<>();
		
		// Make sure the returned map does not contain types not exposed in the API
		for (final Entry<String, Object> entry : properties.entrySet()) {
			final String key = entry.getKey();
			Object value = entry.getValue();
			
			if (value instanceof ColorScheme)
				value = ((ColorScheme)value).getKey();
			else if (value instanceof Class && ((Class<?>)value).isEnum())
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
		String output = "";
		
		try {
			final ObjectMapper om = getObjectMapper();
			output = om.writeValueAsString(this.properties);
			output = getId() + ":" + output;
		} catch (JsonProcessingException e) {
			logger.error("Cannot create JSON from custom graphics", e);
		}
		
		return output;
	}
	
	public abstract String getId();
	
	public synchronized void set(final String key, Object value) {
		if (key == null)
			throw new IllegalArgumentException("'key' must not be null.");
		
		final Class<?> type = getSettingType(key);
		
		if (type != null) {
			if (value != null && !type.isAssignableFrom(value.getClass())) {
				final ObjectMapper om = getObjectMapper();
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
	
	@SuppressWarnings("unchecked")
	public synchronized <S> S[] getArray(final String key, final Class<S> cls) {
		final Object obj = properties.get(key);
		S[] arr = null;
		
		try {
			arr = (obj != null && obj.getClass().isArray()) ? (S[])obj : null;
		} catch (ClassCastException e) {
			logger.error("Cannot cast property '" + key + "' to array.", e);
		}
		
		return arr;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <K, V> Map<K, V> getMap(final String key) {
		final Object obj = properties.get(key);
		
		return obj instanceof Map ? (Map)obj : Collections.emptyMap();
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> parseInput(final String input) {
		final Map<String, Object> props = new HashMap<String, Object>();
		
		if (input != null && !input.isEmpty()) {
			try {
				final ObjectMapper om = getObjectMapper();
				final Map<String, Object> map = om.readValue(input, Map.class);
				
				if (map != null) {
					for (final Entry<String, Object> entry : map.entrySet()) {
						final String key = entry.getKey();
						final Object value = entry.getValue();
						props.put(key, value);
					}
				}
			} catch (Exception e) {
				logger.error("Cannot parse JSON: " + input, e);
			}
		}
		
		return props;
	}
	
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(COLORS)) return List.class;
		if (key.equalsIgnoreCase(COLOR_SCHEME)) return ColorScheme.class;
		if (key.equalsIgnoreCase(ORIENTATION)) return Orientation.class;
		if (key.equalsIgnoreCase(ROTATION)) return Rotation.class;
			
		return null;
	}
	
	public Class<?> getSettingElementType(final String key) {
		if (key.equalsIgnoreCase(COLORS)) return Color.class;
		
		return Object.class;
	}
	
	protected void addProperties(final Map<String, ?> properties) {
		if (properties != null) {
			for (final Entry<String, ?> entry : properties.entrySet()) {
				if (getSettingType(entry.getKey()) != null)
					set(entry.getKey(), entry.getValue());
			}
		}
	}

	protected void addJsonSerializers(final SimpleModule module) {
		module.addSerializer(new PropertiesJsonSerializer());
		module.addSerializer(new ColorSchemeJsonSerializer());
		module.addSerializer(new ColorJsonSerializer());
		module.addSerializer(new Point2DJsonSerializer());
		module.addSerializer(new Rectangle2DJsonSerializer());
	}
	
	protected void addJsonDeserializers(final SimpleModule module) {
		module.addDeserializer(Map.class, new PropertiesJsonDeserializer(this));
		module.addDeserializer(ColorScheme.class, new ColorSchemeJsonDeserializer());
		module.addDeserializer(Color.class, new ColorJsonDeserializer());
		module.addDeserializer(Point2D.class, new Point2DJsonDeserializer());
		module.addDeserializer(Rectangle2D.class, new Rectangle2DJsonDeserializer());
	}
	
	private ObjectMapper getObjectMapper() {
		// Lazy initialization of ObjectMapper, to make sure any other instance property is already initialized
		if (mapper == null) {
			final SimpleModule module = new SimpleModule();
			addJsonSerializers(module);
			addJsonDeserializers(module);
			
			mapper = new ObjectMapper();
			mapper.registerModule(module);
		}
		
		return mapper;
	}
}
