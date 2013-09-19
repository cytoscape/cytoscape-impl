package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;

import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LineStyleSerializer extends JsonSerializer<LineType> {

	@Override
	public void serialize(LineType value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		if (value == null) {
			jgen.writeString(LineTypeVisualProperty.SOLID.getDisplayName().toLowerCase());
		} else {
			// Use default color
			jgen.writeString(value.getDisplayName().toLowerCase());
		}
	}

	@Override
	public Class<LineType> handledType() {
		return LineType.class;
	}

}