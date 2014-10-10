package org.cytoscape.ding.customgraphics.json;

import java.awt.Color;
import java.io.IOException;

import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.ding.internal.gradients.ControlPoint;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ControlPointJsonDeserializer extends JsonDeserializer<ControlPoint> {

	@Override
	public ControlPoint deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final ObjectCodec oc = jp.getCodec();
        final JsonNode node = oc.readTree(jp);
        
        final JsonNode colorNode = node.get("color");
        final JsonNode positionNode = node.get("position");
        
        if (colorNode == null || positionNode == null)
        	return null;
        
        final Color color = colorNode.isTextual() ? ColorUtil.parseColor(colorNode.textValue()) : Color.WHITE;
        final float position = positionNode.isNumber() ? positionNode.floatValue() : 0.0f;
        
		return new ControlPoint(color, position);
	}
}
