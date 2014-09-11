package org.cytoscape.ding.customgraphics.json;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class Rectangle2DJsonSerializer extends JsonSerializer<Rectangle2D> {

	@Override
	public void serialize(final Rectangle2D value, final JsonGenerator jgen, final SerializerProvider provider)
			throws IOException, JsonProcessingException {
		if (value != null) {
			jgen.writeStartObject();
	        jgen.writeNumberField("x", value.getX());
	        jgen.writeNumberField("y", value.getY());
	        jgen.writeNumberField("width", value.getWidth());
	        jgen.writeNumberField("height", value.getHeight());
	        jgen.writeEndObject();
		} else {
			jgen.writeNull();
		}
	}
	
	@Override
	public Class<Rectangle2D> handledType() {
		return Rectangle2D.class;
	}
}
