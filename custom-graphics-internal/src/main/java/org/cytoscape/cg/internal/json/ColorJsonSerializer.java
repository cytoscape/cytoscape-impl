package org.cytoscape.cg.internal.json;

import java.awt.Color;
import java.io.IOException;

import org.cytoscape.cg.internal.util.ColorUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ColorJsonSerializer extends JsonSerializer<Color> {

	@Override
	public void serialize(Color value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		if (value != null)
			jgen.writeString(ColorUtil.toHexString((Color) value));
		else
			jgen.writeNull();
	}

	@Override
	public Class<Color> handledType() {
		return Color.class;
	}
}
