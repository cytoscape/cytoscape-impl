package org.cytoscape.cg.internal.json;

import java.awt.Color;
import java.io.IOException;

import org.cytoscape.cg.internal.util.ColorUtil;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ColorJsonDeserializer extends JsonDeserializer<Color> {

	@Override
	public Color deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		var oc = jp.getCodec();
		JsonNode node = oc.readTree(jp);

		return node.isTextual() ? ColorUtil.parseColor(node.asText()) : null;
	}
}
