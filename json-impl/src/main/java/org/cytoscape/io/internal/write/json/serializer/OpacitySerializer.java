package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class OpacitySerializer extends JsonSerializer<Integer> {

	@Override
	public void serialize(Integer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		if (value != null) {
			// Convert range 0-255 to 0-1
			double doubleValue = value/255d;
			jgen.writeNumber(doubleValue);
		} else {
			// Fully opaque
			jgen.writeNumber(1);
		}
	}

	@Override
	public Class<Integer> handledType() {
		return Integer.class;
	}
}