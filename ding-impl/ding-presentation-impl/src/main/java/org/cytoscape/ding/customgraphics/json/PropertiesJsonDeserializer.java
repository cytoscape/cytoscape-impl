package org.cytoscape.ding.customgraphics.json;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.ding.internal.gradients.AbstractGradient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class PropertiesJsonDeserializer extends JsonDeserializer<Map<String, Object>> {

	private final AbstractCustomGraphics2<?> cg2; // TODO remove circular dependency
	
	private static final Logger logger = LoggerFactory.getLogger(PropertiesJsonDeserializer.class);

	public PropertiesJsonDeserializer(final AbstractCustomGraphics2<?> cg2) {
		this.cg2 = cg2;
	}

	@Override
	public Map<String, Object> deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final Map<String, Object> props = new LinkedHashMap<>();
		
		final ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        final JsonNode rootNode = mapper.readTree(jp);
        
        if (rootNode.isObject()) {
	        final Iterator<String> fieldNames = rootNode.fieldNames();
	        
	        while (fieldNames.hasNext()) {
	        	final String key = fieldNames.next();
	        	final JsonNode jn = rootNode.get(key);
	        	final Object value = readValue(key, jn.toString(), mapper, cg2);
		        props.put(key, value);
	        }
        }
        
		return props;
	}
	
	public static Object readValue(final String key,
								   final String input,
								   final ObjectMapper mapper,
								   final AbstractCustomGraphics2<?> cg2) {
		Object value = null;
		final Class<?> type = cg2.getSettingType(key);
		
    	if (type != null) {
			final TypeFactory typeFactory = mapper.getTypeFactory();
			
			try {
				if (key.equals(AbstractGradient.STOP_LIST) && type.isAssignableFrom(Map.class)) {
					final JsonFactory factory = mapper.getFactory();
					final JsonParser jp = factory.createParser(input);
					
					try {
						jp.nextToken();
						
						if (jp.getCurrentToken() != JsonToken.START_ARRAY)
							return null;
						
						final Map<Float, Color> map = new TreeMap<>();
						
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							if (jp.getCurrentToken() != JsonToken.START_OBJECT)
								continue;
							
							float position = -1;
							Color color = null;
							
							while (jp.nextToken() != JsonToken.END_OBJECT) {
								if (jp.getCurrentToken() != JsonToken.FIELD_NAME)
									continue;
								
								final String fieldName = jp.getCurrentName();
								final JsonToken valueToken = jp.nextToken();
								
								if ("position".equals(fieldName)) {
									if (valueToken == JsonToken.VALUE_NUMBER_FLOAT)
										position = jp.getFloatValue();
									else if (valueToken == JsonToken.VALUE_NUMBER_INT)
										position = jp.getIntValue();
								} else if ("color".equals(fieldName) && valueToken == JsonToken.VALUE_STRING) {
									color = ColorUtil.parseColor(jp.getText());
								}
							}
							
							if (position >= 0.0f && color != null)
								map.put(position, color);
						}
					
						value = map;
					} finally {
						jp.close();
					}
				} else if (type == Array.class) {
					final Class<?> elementType = cg2.getSettingElementType(key);
					
					if (elementType != null) {
		        		final ArrayType arrType = typeFactory.constructArrayType(elementType);
		        		
		        		if (mapper.canDeserialize(arrType))
		        			value = mapper.readValue(input, arrType);
		    		}
				} else if (List.class.isAssignableFrom(type)) {
		    		final Class<?> elementType = cg2.getSettingElementType(key);
		    		
		    		if (elementType != null) {
		        		final CollectionType collType = typeFactory.constructCollectionType(List.class, elementType);
		        		
		        		if (mapper.canDeserialize(collType))
		        			value = mapper.readValue(input, collType);
		    		}
		    	} else {
		    		final JavaType simpleType = typeFactory.constructSimpleType(type, new JavaType[]{});
		    		
		    		if (mapper.canDeserialize(simpleType))
		    			value = mapper.readValue(input, simpleType);
		    	}
			} catch (Exception e) {
        		logger.error("Cannot parse JSON field " + key, e);
        	}
    	}
		
		return value;
	}
}
