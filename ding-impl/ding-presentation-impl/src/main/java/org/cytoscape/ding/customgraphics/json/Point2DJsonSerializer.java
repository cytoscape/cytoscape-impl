package org.cytoscape.ding.customgraphics.json;

import java.awt.geom.Point2D;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class Point2DJsonSerializer extends JsonSerializer<Point2D> {

	@Override
	public void serialize(final Point2D value, final JsonGenerator jgen, final SerializerProvider provider)
			throws IOException, JsonProcessingException {
		if (value != null) {
			jgen.writeStartObject();
	        jgen.writeNumberField("x", value.getX());
	        jgen.writeNumberField("y", value.getY());
	        jgen.writeEndObject();
		} else {
			jgen.writeNull();
		}
	}
	
	@Override
	public Class<Point2D> handledType() {
		return Point2D.class;
	}
}
