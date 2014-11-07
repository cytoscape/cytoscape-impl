package org.cytoscape.ding.customgraphics.json;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class Rectangle2DJsonDeserializer extends JsonDeserializer<Rectangle2D> {

	@Override
	public Rectangle2D deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final ObjectCodec oc = jp.getCodec();
        final JsonNode node = oc.readTree(jp);
        
        final JsonNode xNode = node.get("x");
        final JsonNode yNode = node.get("y");
        final JsonNode wNode = node.get("width");
        final JsonNode hNode = node.get("height");
        
        final double x = xNode != null && xNode.isNumber() ? xNode.asDouble() : 0;
        final double y = yNode != null && yNode.isNumber() ? yNode.asDouble() : 0;
        final double w = wNode != null && wNode.isNumber() ? wNode.asDouble() : 0;
        final double h = hNode != null && hNode.isNumber() ? hNode.asDouble() : 0;
        
		return new Rectangle2D.Double(x, y, w, h);
	}
}
