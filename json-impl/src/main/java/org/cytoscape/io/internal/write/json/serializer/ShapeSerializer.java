package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;

import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ShapeSerializer extends JsonSerializer<NodeShape> {

	@Override
	public void serialize(NodeShape value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		if (value == null) {
			jgen.writeString(NodeShapeVisualProperty.ELLIPSE.getDisplayName().toLowerCase());
		} else {
			// Use default color
			jgen.writeString(value.getDisplayName().replaceAll(" ", "").toLowerCase());
		}
	}

	@Override
	public Class<NodeShape> handledType() {
		return NodeShape.class;
	}

}
