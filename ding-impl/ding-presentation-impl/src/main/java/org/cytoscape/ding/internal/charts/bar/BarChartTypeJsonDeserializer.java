package org.cytoscape.ding.internal.charts.bar;

import java.io.IOException;

import org.cytoscape.ding.internal.charts.bar.BarChart.BarChartType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class BarChartTypeJsonDeserializer extends JsonDeserializer<BarChartType> {

	@Override
	public BarChartType deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final ObjectCodec oc = jp.getCodec();
        final JsonNode node = oc.readTree(jp);
        
		return BarChartType.valueOf(node.asText().toUpperCase());
	}
}
