package org.cytoscape.io.internal.write.json.serializer;

import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ColorSerializer extends JsonSerializer<Paint> {

	private static final Color DEF_COLOR = Color.WHITE;
	
	@Override
	public void serialize(Paint value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {

		// Make sure it is a color object
		if(value instanceof Color == false || value == null) {
			jgen.writeString(decodeColor(DEF_COLOR));
			return;
		}
		
		jgen.writeString(decodeColor((Color) value));
	}

	private final String decodeColor(final Color color) {
		final StringBuilder builder = new StringBuilder();

		builder.append("rgb(");
		builder.append(color.getRed() + ",");
		builder.append(color.getGreen() + ",");
		builder.append(color.getBlue() + ")");

		return builder.toString();
	}

	@Override
	public Class<Paint> handledType() {
		return Paint.class;
	}
}
