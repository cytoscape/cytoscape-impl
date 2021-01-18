package org.cytoscape.view.table.internal.cg.json;

import java.awt.Color;
import java.io.IOException;

import org.cytoscape.view.table.internal.util.ColorUtil;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ColorJsonDeserializer extends JsonDeserializer<Color> {

	@Override
	public Color deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		ObjectCodec oc = jp.getCodec();
		JsonNode node = oc.readTree(jp);

		return node.isTextual() ? ColorUtil.parseColor(node.asText()) : null;
	}
}
