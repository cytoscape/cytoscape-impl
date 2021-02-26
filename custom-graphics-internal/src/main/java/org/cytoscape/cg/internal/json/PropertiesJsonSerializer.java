package org.cytoscape.cg.internal.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

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
				if (value.getClass().isArray()) {
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
