package org.cytoscape.io.internal.write.json.serializer;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CytoscapeJsVisualStyleSerializer extends JsonSerializer<VisualStyle> {

	// Visual Mapping serializer
	private final VisualMappingSerializer<PassthroughMapping<?, ?>> passthrough;
	private final VisualMappingSerializer<DiscreteMapping<?, ?>> discrete;
	private final VisualMappingSerializer<ContinuousMapping<?, ?>> continuous;

	// Mapping between Visual Property and Cytoscape.js tags
	private final CytoscapeJsStyleConverter converter;

	// Target visual lexicon.
	private final VisualLexicon lexicon;


	public CytoscapeJsVisualStyleSerializer(final VisualLexicon lexicon) {
		this.passthrough = new PassthroughMappingSerializer();
		this.discrete = new DiscreteMappingSerializer();
		this.continuous = new ContinuousMappingSerializer();
	
		this.converter = new CytoscapeJsStyleConverter();
		this.lexicon = lexicon;
	}


	/**
	 * Serialize a Visual Style to a CYtoscape.js JSON.
	 * 
	 * @param vs
	 * @param jg
	 * @param provider
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	@Override
	public void serialize(final VisualStyle vs, final JsonGenerator jg, final SerializerProvider provider)
			throws IOException, JsonProcessingException {

		// Print in human readable format
		jg.useDefaultPrettyPrinter();

		// Write actual contents
		jg.writeStartObject();

		// Title of Visual Style
		jg.writeStringField(TITLE.toString(), vs.getTitle());

		// Mappings and Defaults are stored as array.
		jg.writeArrayFieldStart(STYLE.toString());
		// Node Mapping
		serializeVisualProperties(BasicVisualLexicon.NODE, vs, jg);
		serializeVisualProperties(BasicVisualLexicon.EDGE, vs, jg);
		// Selected
//		serializeSelectedStyle(vs, jg);
		jg.writeEndArray();

		jg.writeEndObject();
	}


	private final void serializeVisualProperties(final VisualProperty<?> vp, final VisualStyle vs, final JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		jg.writeStringField(SELECTOR.toString(), vp.getIdString().toLowerCase());
		jg.writeObjectFieldStart(CSS.toString());

		// Generate mappings
		createDefaults(vp, vs, jg);
		// Mappings
		createMappings(vs, jg);

		jg.writeEndObject();
		jg.writeEndObject();
	}


	/**
	 * 
	 * @param vs
	 * @param jg
	 * @throws IOException
	 */
	private void createDefaults(final VisualProperty<?> parent, final VisualStyle vs, final JsonGenerator jg) throws IOException {
		final Collection<VisualProperty<?>> visualProperties = lexicon.getAllDescendants(parent);
		
		for(final VisualProperty<?> vp: visualProperties) {
			CytoscapeJsToken tag = converter.getTag(vp);
			if(tag == null) {
				continue;
			}
			// tag can be null.  In that case, use default,
			jg.writeObjectField(tag.getTag(), getDefaultVisualPropertyValue(vs, vp));
		}
		
//		jg.writeStringField(BACKGROUND_COLOR.toString(),
//				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR)));
//		jg.writeStringField(COLOR.toString(),
//				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR)));
//
//		jg.writeNumberField(FONT_WEIGHT.toString(), 100);
//
//		jg.writeStringField(TEXT_VALIGN.toString(), "center");
//		jg.writeStringField(TEXT_HALIGN.toString(), "center");
//		jg.writeNumberField(TEXT_OUTLINE_WIDTH.toString(), 0);
//		jg.writeNumberField(FONT_SIZE.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE));
//
//		jg.writeStringField(BORDER_COLOR.toString(),
//				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT)));
//		jg.writeNumberField(BORDER_WIDTH.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH));
//		jg.writeNumberField(WIDTH.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_WIDTH));
//		jg.writeNumberField(HEIGHT.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_HEIGHT));
//		jg.writeNumberField(OPACITY.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY) / 255f);
//		jg.writeStringField(SHAPE.toString(), vs.getDefaultValue(BasicVisualLexicon.NODE_SHAPE).getDisplayName()
//				.replaceAll(" ", "").toLowerCase());
	}
	
	
	private final <T> T getDefaultVisualPropertyValue(final VisualStyle vs, final VisualProperty<T> vp) {
		final T value = vs.getDefaultValue(vp);
		if(value == null) {
			return vp.getDefault();
		} else {
			return value;
		}
	}

	private void createMappings(final VisualStyle vs, JsonGenerator jg) throws IOException {
		final Collection<VisualMappingFunction<?, ?>> mappings = vs.getAllVisualMappingFunctions();

		for (final VisualMappingFunction<?, ?> mapping : mappings) {
			if (mapping.getVisualProperty().getTargetDataType() != CyNode.class)
				continue;

			if (mapping instanceof PassthroughMapping) {
				final String tag = passthrough.getTag((PassthroughMapping<?, ?>) mapping);
				jg.writeStringField(tag, passthrough.serialize((PassthroughMapping<?, ?>) mapping));
			} else if (mapping instanceof DiscreteMapping) {
				final String tag = discrete.getTag((DiscreteMapping<?, ?>) mapping);
				jg.writeStringField(tag, discrete.serialize((DiscreteMapping<?, ?>) mapping));
			} else if (mapping instanceof ContinuousMapping) {
				final String tag = continuous.getTag((ContinuousMapping<?, ?>) mapping);
				jg.writeStringField(tag, continuous.serialize((ContinuousMapping<?, ?>) mapping));
			}
		}
	}

	private void createEdgeMapping(final VisualStyle vs, JsonGenerator jg) throws IOException {
		// Passthrough
		final Collection<VisualMappingFunction<?, ?>> mappings = vs.getAllVisualMappingFunctions();

		for (final VisualMappingFunction<?, ?> mapping : mappings) {
			if (mapping.getVisualProperty().getTargetDataType() != CyEdge.class)
				continue;

			if (mapping instanceof PassthroughMapping && mapping.getVisualProperty() == BasicVisualLexicon.EDGE_LABEL) {
				jg.writeStringField(CONTENT.toString(), "data(" + mapping.getMappingColumnName() + ")");
			}
		}

	}

	private void serializeEdgeStyle(final VisualStyle vs, JsonGenerator jg) throws IOException {
		jg.writeStartObject();

		//Default values
		createEdgeDefaults(vs, jg);
		
		// Mappings
		createEdgeMapping(vs, jg);

		jg.writeEndObject();

		jg.writeEndObject();
	}

	private void createEdgeDefaults(final VisualStyle vs, final JsonGenerator jg) throws IOException {
		
		final Collection<VisualProperty<?>> edgeVisualProperties = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		
		for(final VisualProperty<?> vp:edgeVisualProperties) {
			CytoscapeJsToken tag = converter.getTag(vp);
			if(tag == null) {
				continue;
			}
			// tag can be null.  In that case, use default,
			jg.writeStringField(tag.getTag(), getDefaultVisualPropertyValue(vs, vp).toString());
		}
//		jg.writeStringField(SELECTOR.toString(), EDGE.toString());
//
//		jg.writeObjectFieldStart(CSS.toString());
//		jg.writeNumberField(WIDTH.toString(), vs.getDefaultValue(BasicVisualLexicon.EDGE_WIDTH));
//		jg.writeStringField(LINE_COLOR.toString(),
//				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)));
//		jg.writeNumberField(OPACITY.toString(), vs.getDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY) / 255f);
//
//		jg.writeStringField(TARGET_ARROW_SHAPE.toString(),
//				vs.getDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE).getDisplayName().toLowerCase());
//		jg.writeStringField(SOURCE_ARROW_SHAPE.toString(),
//				vs.getDefaultValue(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE).getDisplayName().toLowerCase());
	}

	
	private void serializeSelectedStyle(final VisualStyle vs, JsonGenerator jg) throws IOException {
//		jg.writeStartObject();
//		jg.writeStringField(SELECTOR.toString(), SELECTED.toString());
//
//		jg.writeObjectFieldStart(CSS.toString());
//		jg.writeStringField(BACKGROUND_COLOR.toString(),
//				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT)));
//
//		jg.writeStringField(LINE_COLOR.toString(),
//				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)));
//		jg.writeStringField(SOURCE_ARROW_COLOR.toString().toString(),
//				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)));
//		jg.writeStringField(TARGET_ARROW_COLOR.toString().toString(),
//				decodeColor((Color) vs.getDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)));
//
//		jg.writeEndObject();
//
//		jg.writeEndObject();
	}


	@Override
	public Class<VisualStyle> handledType() {
		return VisualStyle.class;
	}
}