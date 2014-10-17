package org.cytoscape.ding.customgraphics.json;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.ding.internal.gradients.AbstractGradient;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


public class PropertiesJsonSerializer extends JsonSerializer<Map<String, Object>> {

	@Override
	public void serialize(final Map<String, Object> map, final JsonGenerator jgen, final SerializerProvider provider)
			throws IOException, JsonProcessingException {
		// Start
		jgen.writeStartObject();
		
		// Write properties
		for (final Entry<String, Object> entry : map.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			
			if (key != null && value != null) {
				// TODO create subclass that handles particular case of ControlPoints key in gradients package
				if (key.equals(AbstractGradient.STOP_LIST) && value instanceof Map) {
					jgen.writeArrayFieldStart(key);
					
					final Map<?, ?> cpMap = (Map<?, ?>) value;
					
					for (final Object k : cpMap.keySet()) {
						final Object v = cpMap.get(k);
						
						if (k instanceof Number && v instanceof Color) {
							jgen.writeStartObject();
							jgen.writeNumberField("position", ((Number)k).floatValue());
							jgen.writeStringField("color", ColorUtil.toHexString((Color)v));
							jgen.writeEndObject();
						}
					}
					
					jgen.writeEndArray();
				} else if (value.getClass().isArray()) {
					int length = Array.getLength(value);
				    
					jgen.writeArrayFieldStart(key);
					
					for (int i = 0; i < length; i++)
						jgen.writeObject(Array.get(value, i));
					
					jgen.writeEndArray();
				} else if (value instanceof Collection) {
					jgen.writeArrayFieldStart(key);
					
					for (final Object v : (Collection<?>)value)
						jgen.writeObject(v);
					
					jgen.writeEndArray();
				} else {
					jgen.writeFieldName(key);
					jgen.writeObject(value);
				}
			}
		}
		
		// End
		jgen.writeEndObject();
	}
	
	@Override
	public Class handledType() {
		return Map.class;
	}
}
