package org.cytoscape.io.internal.write.json.serializer;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CytoscapeJsVisualStyleSerializer extends JsonSerializer<VisualStyle> {

	private static final Collection<VisualProperty<?>> NODE_SELECTED_PROPERTIES = new ArrayList<VisualProperty<?>>();
	private static final Collection<VisualProperty<?>> EDGE_SELECTED_PROPERTIES = new ArrayList<VisualProperty<?>>();
	static {
		EDGE_SELECTED_PROPERTIES.add(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT);
		NODE_SELECTED_PROPERTIES.add(BasicVisualLexicon.NODE_SELECTED_PAINT);
	}

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
		jg.writeStringField(TITLE.getTag(), vs.getTitle());

		// Mappings and Defaults are stored as array.
		jg.writeArrayFieldStart(STYLE.getTag());
		// Node Mapping
		serializeVisualProperties(BasicVisualLexicon.NODE, vs, jg);
		serializeSelectedProperties(BasicVisualLexicon.NODE, vs, jg);
		serializeDiscreteMapping(BasicVisualLexicon.NODE, vs, jg, CyNode.class);

		serializeVisualProperties(BasicVisualLexicon.EDGE, vs, jg);
		serializeSelectedProperties(BasicVisualLexicon.EDGE, vs, jg);
		serializeDiscreteMapping(BasicVisualLexicon.EDGE, vs, jg, CyEdge.class);

		jg.writeEndArray();

		jg.writeEndObject();
	}

	private final void serializeVisualProperties(final VisualProperty<?> vp, final VisualStyle vs,
			final JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		jg.writeStringField(SELECTOR.getTag(), vp.getIdString().toLowerCase());
		jg.writeObjectFieldStart(CSS.getTag());

		// Generate mappings
		final Collection<VisualProperty<?>> visualProperties = lexicon.getAllDescendants(vp);
		for (VisualProperty<?> removed : NODE_SELECTED_PROPERTIES) {
			visualProperties.remove(removed);
		}
		for (VisualProperty<?> removed : EDGE_SELECTED_PROPERTIES) {
			visualProperties.remove(removed);
		}
		createDefaults(visualProperties, vs, jg);
		// Mappings
		createMappings(visualProperties, vs, jg);

		jg.writeEndObject();
		jg.writeEndObject();
	}

	private final void serializeSelectedProperties(final VisualProperty<?> vp, final VisualStyle vs,
			final JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		jg.writeStringField(SELECTOR.getTag(), vp.getIdString().toLowerCase() + SELECTED.getTag());
		jg.writeObjectFieldStart(CSS.getTag());

		// Generate mappings
		if (vp == BasicVisualLexicon.NODE) {
			createDefaults(NODE_SELECTED_PROPERTIES, vs, jg);
			createMappings(NODE_SELECTED_PROPERTIES, vs, jg);
		} else {
			createDefaults(EDGE_SELECTED_PROPERTIES, vs, jg);
			createMappings(EDGE_SELECTED_PROPERTIES, vs, jg);
		}

		jg.writeEndObject();
		jg.writeEndObject();
	}

	private final void serializeDiscreteMapping(final VisualProperty<?> vp, final VisualStyle vs, final JsonGenerator jg, Class<? extends CyIdentifiable> target)
			throws IOException {
		// Find discreteMappings
		final Collection<VisualMappingFunction<?, ?>> mappings = vs.getAllVisualMappingFunctions();
		for (VisualMappingFunction<?, ?> mapping : mappings) {
			if(mapping.getVisualProperty().getTargetDataType() != target) {
				continue;
			}
			
			if (mapping instanceof DiscreteMapping) {
				final VisualProperty<?> mappingVp = mapping.getVisualProperty();
				final CytoscapeJsToken tag = converter.getTag(mappingVp);
				if (tag == null) {
					continue;
				} else {
					generateDiscreteMappingSection(tag, (DiscreteMapping<?, ?>) mapping, vp, vs, jg);
				}
			}
		}
	}

	/**
	 * Generate a section for a discrete mapping entry. One entry is equal to
	 * one selector.
	 * 
	 * @param mapping
	 * @param vp
	 * @param vs
	 * @param jg
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	private final void generateDiscreteMappingSection(final CytoscapeJsToken jsTag,  final DiscreteMapping<?, ?> mapping, final VisualProperty<?> vp,
			final VisualStyle vs, final JsonGenerator jg) throws IOException {

		final Map<?, ?> mappingPairs = mapping.getAll();

		final String colName = mapping.getMappingColumnName();
		final Class<?> colType = mapping.getMappingColumnType();

		for (Object key : mappingPairs.keySet()) {
			final Object value = mappingPairs.get(key);
			jg.writeStartObject();

			String tag = vp.getIdString().toLowerCase() + "[\'" + colName + "\' = ";
			if (colType == Integer.class || colType == Double.class || colType == Float.class || colType == Long.class) {
				tag += key + "]";
			} else {
				tag += "\'" + key + "\']";
			}
			jg.writeStringField(SELECTOR.getTag(), tag);
			jg.writeObjectFieldStart(CSS.getTag());

			jg.writeObjectField(jsTag.getTag(),value); 

			jg.writeEndObject();
			jg.writeEndObject();
		}
	}


	/**
	 * 
	 * @param vs
	 * @param jg
	 * @throws IOException
	 */
	private void createDefaults(final Collection<VisualProperty<?>> visualProperties, final VisualStyle vs,
			final JsonGenerator jg) throws IOException {

		for (final VisualProperty<?> vp : visualProperties) {
			// If mapping is available, use it instead.
			final VisualMappingFunction<?, ?> mapping = vs.getVisualMappingFunction(vp);
			if (mapping != null) {
				continue;
			}

			final CytoscapeJsToken tag = converter.getTag(vp);
			if (tag == null) {
				continue;
			}

			// tag can be null. In that case, use default,
			if (writeValue(vp)) {
				jg.writeObjectField(tag.getTag(), getDefaultVisualPropertyValue(vs, vp));
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private final boolean writeValue(final VisualProperty<?> vp) {

		if (vp == BasicVisualLexicon.EDGE_TRANSPARENCY || vp == BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY
				|| vp == BasicVisualLexicon.NODE_LABEL_TRANSPARENCY || vp == BasicVisualLexicon.NODE_TRANSPARENCY
				|| vp == BasicVisualLexicon.NODE_BORDER_TRANSPARENCY) {
			return false;
		} else {
			return true;
		}
	}

	private final <T> T getDefaultVisualPropertyValue(final VisualStyle vs, final VisualProperty<T> vp) {
		final T value = vs.getDefaultValue(vp);
		if (value == null) {
			return vp.getDefault();
		} else {
			return value;
		}
	}

	private void createMappings(final Collection<VisualProperty<?>> visualProperties, final VisualStyle vs,
			JsonGenerator jg) throws IOException {
		for (final VisualProperty<?> vp : visualProperties) {
			final VisualMappingFunction<?, ?> mapping = vs.getVisualMappingFunction(vp);
			if (mapping == null) {
				continue;
			}

			// Skip unsupported Visual Properties
			final CytoscapeJsToken jsTag = converter.getTag(mapping.getVisualProperty());
			if (jsTag == null) {
				continue;
			}

			final String tag = jsTag.getTag();
			if (mapping instanceof PassthroughMapping) {
				jg.writeStringField(tag, passthrough.serialize((PassthroughMapping<?, ?>) mapping));
			} else if (mapping instanceof DiscreteMapping) {
				jg.writeStringField(tag, discrete.serialize((DiscreteMapping<?, ?>) mapping));
			} else if (mapping instanceof ContinuousMapping) {
				jg.writeStringField(tag, continuous.serialize((ContinuousMapping<?, ?>) mapping));
			}
		}
	}

	@Override
	public Class<VisualStyle> handledType() {
		return VisualStyle.class;
	}
}