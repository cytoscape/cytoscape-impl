package org.cytoscape.cg.internal.charts.bar;

import java.io.IOException;

import org.cytoscape.cg.internal.charts.bar.BarChart.BarChartType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class BarChartTypeJsonDeserializer extends JsonDeserializer<BarChartType> {

	@Override
	public BarChartType deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		var oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        
		return BarChartType.valueOf(node.asText().toUpperCase());
	}
}
