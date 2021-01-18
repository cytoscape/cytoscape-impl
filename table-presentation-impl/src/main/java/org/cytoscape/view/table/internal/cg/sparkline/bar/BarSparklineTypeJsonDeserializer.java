package org.cytoscape.view.table.internal.cg.sparkline.bar;

import java.io.IOException;

import org.cytoscape.view.table.internal.cg.sparkline.bar.BarSparkline.BarSparklineType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class BarSparklineTypeJsonDeserializer extends JsonDeserializer<BarSparklineType> {

	@Override
	public BarSparklineType deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        
		return BarSparklineType.valueOf(node.asText().toUpperCase());
	}
}
