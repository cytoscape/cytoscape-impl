package org.cytoscape.io.internal.write.json.serializer;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsVisualStyleSerializer extends JsonSerializer<VisualStyle> {

	@Override
	public void serialize(final VisualStyle vs, JsonGenerator jg, SerializerProvider provider) throws IOException,
			JsonProcessingException {

		jg.useDefaultPrettyPrinter();

		jg.writeStartObject();

		// Title of Visual Style
		jg.writeStringField(TITLE.toString(), vs.getTitle());

		// Mappings and Defaults are stored as array.
		jg.writeArrayFieldStart(STYLE.toString());

		// Node Mapping
		serializeNodeStyle(vs, jg);

		// Edge Mapping
		serializeEdgeStyle(vs, jg);

		// Selected
		serializeSelectedStyle(vs, jg);

		// TODO What else?
		jg.writeEndArray();

		jg.writeEndObject();
	}

	private void serializeSelectedStyle(final VisualStyle vs, JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		jg.writeStringField(SELECTOR.toString(), SELECTED.toString());

		jg.writeObjectFieldStart(CSS.toString());
		jg.writeStringField(BACKGROUND_COLOR.toString(),
				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT)));

		jg.writeStringField(LINE_COLOR.toString(),
				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)));
		jg.writeStringField(SOURCE_ARROW_COLOR.toString().toString(),
				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)));
		jg.writeStringField(TARGET_ARROW_COLOR.toString().toString(),
				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)));

		jg.writeEndObject();

		jg.writeEndObject();
	}

	private void serializeNodeStyle(final VisualStyle vs, JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		jg.writeStringField(SELECTOR.toString(), NODE.toString());

		jg.writeObjectFieldStart(CSS.toString());
		jg.writeStringField(BACKGROUND_COLOR.toString(),
				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR)));
		jg.writeStringField(COLOR.toString(),
				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR)));
		
		jg.writeNumberField(FONT_WEIGHT.toString(), 100);
		
		// TODO: implement dynamic generator for these values
		jg.writeStringField(TEXT_VALIGN.toString(), "center");
		jg.writeStringField(TEXT_HALIGN.toString(), "center");
		jg.writeNumberField(TEXT_OUTLINE_WIDTH.toString(), 0);
		jg.writeNumberField(FONT_SIZE.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE));

		jg.writeStringField(BORDER_COLOR.toString(),
				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT)));
		jg.writeNumberField(BORDER_WIDTH.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH));
		jg.writeNumberField(WIDTH.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_WIDTH));
		jg.writeNumberField(HEIGHT.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_HEIGHT));
		jg.writeNumberField(OPACITY.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY) / 255f);
		jg.writeStringField(SHAPE.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_SHAPE).getDisplayName()
				.replaceAll(" ", "").toLowerCase());

		// Mappings
		createNodeMapping(vs, jg);

		jg.writeEndObject();

		jg.writeEndObject();
	}

	private void createNodeMapping(final VisualStyle vs, JsonGenerator jg) throws IOException {
		// TODO implememt this
		// Passthrough
		Collection<VisualMappingFunction<?, ?>> mappings = vs.getAllVisualMappingFunctions();

		for (VisualMappingFunction<?, ?> mapping : mappings) {
			if (mapping.getVisualProperty().getTargetDataType() != CyNode.class)
				continue;

			if (mapping instanceof PassthroughMapping && mapping.getVisualProperty() == BasicVisualLexicon.NODE_LABEL) {
				jg.writeStringField(CONTENT.toString(), "data(" + mapping.getMappingColumnName() + ")");
			}
		}

	}

	private final void generateDiscreteMapping() {

	}

	private void createEdgeMapping(final VisualStyle vs, JsonGenerator jg) throws IOException {
		// TODO implememt this
		// Passthrough
		Collection<VisualMappingFunction<?, ?>> mappings = vs.getAllVisualMappingFunctions();

		for (VisualMappingFunction<?, ?> mapping : mappings) {
			if (mapping.getVisualProperty().getTargetDataType() != CyEdge.class)
				continue;

			if (mapping instanceof PassthroughMapping && mapping.getVisualProperty() == BasicVisualLexicon.EDGE_LABEL) {
				jg.writeStringField(CONTENT.toString(), "data(" + mapping.getMappingColumnName() + ")");
			}
		}

	}

	private void serializeEdgeStyle(final VisualStyle vs, JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		jg.writeStringField(SELECTOR.toString(), EDGE.toString());

		jg.writeObjectFieldStart(CSS.toString());
		jg.writeNumberField(WIDTH.toString(), vs.getDefaultValue(BasicVisualLexicon.EDGE_WIDTH));
		jg.writeStringField(LINE_COLOR.toString(),
				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)));
		jg.writeNumberField(OPACITY.toString(), vs.getDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY) / 255f);

		jg.writeStringField(TARGET_ARROW_SHAPE.toString(), 
				vs.getDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE).getDisplayName().toLowerCase());
		jg.writeStringField(SOURCE_ARROW_SHAPE.toString(), 
				vs.getDefaultValue(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE).getDisplayName().toLowerCase());
		
		
		// Mappings
		createEdgeMapping(vs, jg);

		jg.writeEndObject();

		jg.writeEndObject();
	}

	private final String decodeColor(Color color) {
		final StringBuilder builder = new StringBuilder();

		builder.append("rgb(");
		builder.append(color.getRed() + ",");
		builder.append(color.getGreen() + ",");
		builder.append(color.getBlue() + ")");

		return builder.toString();
	}

	@Override
	public Class<VisualStyle> handledType() {
		return VisualStyle.class;
	}
}