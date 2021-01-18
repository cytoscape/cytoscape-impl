package org.cytoscape.view.table.internal.cg.json;

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
	public Rectangle2D deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        
        JsonNode xNode = node.get("x");
        JsonNode yNode = node.get("y");
        JsonNode wNode = node.get("width");
        JsonNode hNode = node.get("height");
        
        double x = xNode != null && xNode.isNumber() ? xNode.asDouble() : 0;
        double y = yNode != null && yNode.isNumber() ? yNode.asDouble() : 0;
        double w = wNode != null && wNode.isNumber() ? wNode.asDouble() : 0;
        double h = hNode != null && hNode.isNumber() ? hNode.asDouble() : 0;
        
		return new Rectangle2D.Double(x, y, w, h);
	}
}
