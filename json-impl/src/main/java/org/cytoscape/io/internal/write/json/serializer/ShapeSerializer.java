package org.cytoscape.io.internal.write.json.serializer;

import java.awt.Color;
import java.io.IOException;

import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ShapeSerializer extends JsonSerializer<NodeShapeVisualProperty> {

	@Override
	public void serialize(NodeShapeVisualProperty value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		if (value != null) {
			jgen.writeString(decodeColor(value));
		} else {
			// Use default color
			jgen.writeString(decodeColor(DEF_COLOR));
		}
	}


	@Override
	public Class<NodeShapeVisualProperty> handledType() {
		return NodeShapeVisualProperty.class;
	}

}
