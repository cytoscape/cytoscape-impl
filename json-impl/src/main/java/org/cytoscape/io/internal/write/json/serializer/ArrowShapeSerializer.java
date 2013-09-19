package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;

import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ArrowShapeSerializer extends JsonSerializer<ArrowShape> {

	@Override
	public void serialize(ArrowShape value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
	
		if(value == null) {
			// Use default
			jgen.writeString(ArrowShapeVisualProperty.NONE.getDisplayName().toLowerCase());
		} else {
			jgen.writeString(value.getDisplayName().toLowerCase());
		}
	}
	@Override
	public Class<ArrowShape> handledType() {
		return ArrowShape.class;
	}
}
