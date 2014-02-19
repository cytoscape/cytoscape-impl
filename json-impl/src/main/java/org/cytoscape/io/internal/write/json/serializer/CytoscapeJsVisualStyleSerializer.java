package org.cytoscape.io.internal.write.json.serializer;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.CSS;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.SELECTED;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.SELECTOR;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.STYLE;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.TITLE;

import java.awt.Paint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.application.CyVersion;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CytoscapeJsVisualStyleSerializer extends JsonSerializer<VisualStyle> {
	
	private static final Pattern REPLACE_INVALID_JS_CHAR_PATTERN = Pattern.compile("^[^a-zA-Z_]+|[^a-zA-Z_0-9]+");

	private static final Collection<VisualProperty<?>> NODE_SELECTED_PROPERTIES = new ArrayList<VisualProperty<?>>();
	private static final Collection<VisualProperty<?>> EDGE_SELECTED_PROPERTIES = new ArrayList<VisualProperty<?>>();

	static {
		EDGE_SELECTED_PROPERTIES.add(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT);
		EDGE_SELECTED_PROPERTIES.add(BasicVisualLexicon.EDGE_SELECTED_PAINT);

		NODE_SELECTED_PROPERTIES.add(BasicVisualLexicon.NODE_SELECTED_PAINT);
	}

	// Visual Mapping serializer
	private final VisualMappingSerializer<PassthroughMapping<?, ?>> passthrough;

	// Mapping between Visual Property and Cytoscape.js tags
	private final CytoscapeJsStyleConverter converter;

	// Target visual lexicon.
	private final VisualLexicon lexicon;

	private final ValueSerializerManager manager;
	
	private final String version;

	public CytoscapeJsVisualStyleSerializer(final ValueSerializerManager manager, final VisualLexicon lexicon, final CyVersion cyVersion) {
		this.passthrough = new PassthroughMappingSerializer();
		this.manager = manager;

		this.converter = new CytoscapeJsStyleConverter();
		this.lexicon = lexicon;
		this.version = cyVersion.getVersion();
	}

	/**
	 * Serialize a Visual Style to a Cytoscape.js JSON.
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
		
		// Version
		jg.writeStringField(CytoscapeJsNetworkModule.FORMAT_VERSION_TAG, CytoscapeJsNetworkModule.FORMAT_VERSION);
		jg.writeStringField(CytoscapeJsNetworkModule.GENERATED_BY_TAG, "cytoscape-" + version);
		jg.writeStringField(CytoscapeJsNetworkModule.TARGET_CYJS_VERSION_TAG, CytoscapeJsNetworkModule.CYTOSCAPEJS_VERSION);

		// Title of Visual Style
		jg.writeStringField(TITLE.getTag(), vs.getTitle());

		// Mappings and Defaults are stored as array.
		jg.writeArrayFieldStart(STYLE.getTag());
		// Node Mapping
		serializeVisualProperties(BasicVisualLexicon.NODE, vs, jg);
		serializeMappings(BasicVisualLexicon.NODE, vs, jg, CyNode.class);
		serializeSelectedProperties(BasicVisualLexicon.NODE, vs, jg);

		serializeVisualProperties(BasicVisualLexicon.EDGE, vs, jg);
		serializeMappings(BasicVisualLexicon.EDGE, vs, jg, CyEdge.class);
		serializeSelectedProperties(BasicVisualLexicon.EDGE, vs, jg);

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
		
		// Mappings - Passthrough ONLY, because others needs special selectors.
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

	private final void serializeMappings(final VisualProperty<?> vp, final VisualStyle vs, final JsonGenerator jg,
			Class<? extends CyIdentifiable> target) throws IOException {
		// Find discreteMappings
		final Collection<VisualMappingFunction<?, ?>> mappings = vs.getAllVisualMappingFunctions();
		for (VisualMappingFunction<?, ?> mapping : mappings) {
			if (mapping.getVisualProperty().getTargetDataType() != target) {
				continue;
			}
			
			final VisualProperty<?> mappingVp = mapping.getVisualProperty();
			final CytoscapeJsToken tag = converter.getTag(mappingVp);
			if (tag == null && mappingVp != BasicVisualLexicon.NODE_SIZE) {
				continue;
			} else {
				if (mapping instanceof DiscreteMapping) {
					generateDiscreteMappingSection(tag, (DiscreteMapping<?, ?>) mapping, vp, vs, jg);
				} else if (mapping instanceof ContinuousMapping) {
					if(mappingVp == BasicVisualLexicon.NODE_SIZE) {
						// Special case: Node Size should create two types of mappings.
						generateContinuousMappingSection(CytoscapeJsToken.WIDTH, (ContinuousMapping<?, ?>) mapping, vp, vs, jg);
						generateContinuousMappingSection(CytoscapeJsToken.HEIGHT, (ContinuousMapping<?, ?>) mapping, vp, vs, jg);
					} else {
						generateContinuousMappingSection(tag, (ContinuousMapping<?, ?>) mapping, vp, vs, jg);
					}
				}
			}
		}
	}

	private final void generateContinuousMappingSection(final CytoscapeJsToken jsTag,
			final ContinuousMapping<?, ?> mapping, final VisualProperty<?> vp, final VisualStyle vs,
			final JsonGenerator jg) throws IOException {

		final Class<?> type = mapping.getVisualProperty().getRange().getType();
		final VisualProperty<?> targetVP = mapping.getVisualProperty();

		String columnName = mapping.getMappingColumnName();
		final Matcher matcher = REPLACE_INVALID_JS_CHAR_PATTERN.matcher(columnName);
		columnName = matcher.replaceAll("_");
		final List<?> points = mapping.getAllPoints();
		final String objectType = vp.getIdString().toLowerCase();

		// Special case 1: Empty mapping
		if (points.size() == 0) {
			// No mapping points. Ignore.
			return;
		}

		// Special case 2: only one point split into 3 selectors.
		if (points.size() == 1) {
			final ContinuousMappingPoint<?, ?> point = (ContinuousMappingPoint<?, ?>) points.get(0);
			final Number bound = (Number) point.getValue();
			writeSelector(targetVP, jg, point.getRange().lesserValue, "<", objectType, columnName, jsTag.getTag(), bound);
			writeSelector(targetVP, jg, point.getRange().equalValue, "=", objectType, columnName, jsTag.getTag(), bound);
			writeSelector(targetVP, jg, point.getRange().greaterValue, ">", objectType, columnName, jsTag.getTag(), bound);
			return;
		}

		// Sort points by value. This is necessary to create correct mapping.
		final TreeMap<Number, ContinuousMappingPoint<?, ?>> pointMap = new TreeMap<Number, ContinuousMappingPoint<?, ?>>();
		for (final Object point : points) {
			final ContinuousMappingPoint<?, ?> p = (ContinuousMappingPoint<?, ?>) point;
			Number val = (Number) p.getValue();
			pointMap.put(val, p);
		}

		final ValueSerializer serializer = manager.getSerializer(type);
		ContinuousMappingPoint<?, ?> prevPoint = null;
		for (final Number key : pointMap.descendingKeySet()) {

			final ContinuousMappingPoint<?, ?> point = (ContinuousMappingPoint<?, ?>) pointMap.get(key);
			final Number bound = (Number) point.getValue();
			// Largest key
			if (key.equals(pointMap.lastKey())) {
				// Highest value. This should be executed first.
				writeSelector(targetVP, jg, point.getRange().greaterValue, ">", objectType, columnName, jsTag.getTag(), bound);
				writeSelector(targetVP, jg, point.getRange().equalValue, "=", objectType, columnName, jsTag.getTag(), bound);
				prevPoint = point;
			} else if (key.equals(pointMap.firstKey())) {
				// Lowest value. This should be executed LAST.
				generateMap(jg, columnName, objectType, jsTag.getTag(), point, prevPoint, serializer);
				writeSelector(targetVP, jg, point.getRange().equalValue, "=", objectType, columnName, jsTag.getTag(), bound);
				writeSelector(targetVP, jg, point.getRange().lesserValue, "<", objectType, columnName, jsTag.getTag(), bound);
			} else {
				// Create map
				generateMap(jg, columnName, objectType, jsTag.getTag(), point, prevPoint, serializer);
				prevPoint = point;
			}
		}
	}

	private final void generateMap(final JsonGenerator jg, final String columnName, String objectType, String tag,
			final ContinuousMappingPoint<?, ?> point, final ContinuousMappingPoint<?, ?> prevPoint,
			ValueSerializer serializer) throws IOException {
		// Create map
		Object lowerVal = point.getRange().greaterValue;
		Object upperVal = prevPoint.getRange().lesserValue;

		String lowerValString = lowerVal.toString();
		String upperValString = upperVal.toString();
		if (serializer != null) {
			lowerValString = serializer.serialize(lowerVal);
			upperValString = serializer.serialize(upperVal);
		}
		String map = "mapData(" + columnName + ",";
		map += point.getValue().toString() + "," + prevPoint.getValue().toString() + "," + lowerValString + ","
				+ upperValString + ")";
		writeMapSelector(jg, map, objectType, columnName, tag, (Number) point.getValue(), (Number) prevPoint.getValue());
	}

	private final void writeMapSelector(final JsonGenerator jg, Object value, String objectType, String colName,
			String jsTag, Number boundL, Number boundU) throws IOException {

		jg.writeStartObject();

		// Always define region, i.e., a < P <b
		String tag = objectType + "[" + colName + " > ";
		tag += boundL + "][" + colName + " < " + boundU + "]";

		jg.writeStringField(SELECTOR.getTag(), tag);
		jg.writeObjectFieldStart(CSS.getTag());

		jg.writeObjectField(jsTag, value);

		jg.writeEndObject();
		jg.writeEndObject();
	}

	private final void writeSelector(final VisualProperty<?> vp, final JsonGenerator jg, Object value, String operator, String objectType,
			String colName, String jsTag, Number bound) throws IOException {

		jg.writeStartObject();

		String tag = objectType + "[" + colName + " " + operator + " ";
		tag += bound + "]";

		jg.writeStringField(SELECTOR.getTag(), tag);
		jg.writeObjectFieldStart(CSS.getTag());

		jg.writeObjectField(jsTag, convert(vp, value));

		jg.writeEndObject();
		jg.writeEndObject();
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
	private final void generateDiscreteMappingSection(final CytoscapeJsToken jsTag,
			final DiscreteMapping<?, ?> mapping, final VisualProperty<?> vp, final VisualStyle vs,
			final JsonGenerator jg) throws IOException {

		final Map<?, ?> mappingPairs = mapping.getAll();
		Set<VisualPropertyDependency<?>> locks = vs.getAllVisualPropertyDependencies();
		
		boolean sizeLocked = false;
		for(VisualPropertyDependency<?> lock:locks) {
			if(lock.getIdString().equals("nodeSizeLocked") )
				sizeLocked = true;
		}

		String colName = mapping.getMappingColumnName();
		final Matcher matcher = REPLACE_INVALID_JS_CHAR_PATTERN.matcher(colName);
		colName = matcher.replaceAll("_");
		final Class<?> colType = mapping.getMappingColumnType();

		for (Object key : mappingPairs.keySet()) {
			final Object value = convert(mapping.getVisualProperty(), mappingPairs.get(key));
			jg.writeStartObject();

			String tag = vp.getIdString().toLowerCase() + "[" + colName + " = ";
			if (colType == Integer.class || colType == Double.class || colType == Float.class || colType == Long.class) {
				tag += key + "]";
			} else {
				// String
				tag += "\'" + key + "\']";
			}
			jg.writeStringField(SELECTOR.getTag(), tag);
			jg.writeObjectFieldStart(CSS.getTag());

			// TODO: refactor this special case handlers!
			// Write actual key-value pair
			if(sizeLocked && mapping.getVisualProperty() == BasicVisualLexicon.NODE_SIZE) {
				jg.writeObjectField(CytoscapeJsToken.WIDTH.getTag(), value);
				jg.writeObjectField(CytoscapeJsToken.HEIGHT.getTag(), value);
			} else {
				jg.writeObjectField(jsTag.getTag(), value);
			}
			
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

		// Handle locked values
		final Set<VisualPropertyDependency<?>> deps = vs.getAllVisualPropertyDependencies();

		// TODO: better way to handle this?
		boolean useSize = false;
		boolean useStroke = false;
		for(VisualPropertyDependency<?> dep: deps) {
			if(dep.getIdString().equals("nodeSizeLocked")) {
				if(dep.isDependencyEnabled()) {
					useSize = true;
					dep.setDependency(false);
				}
			}
			
			if(dep.getIdString().equals("arrowColorMatchesEdge")) {
				if(dep.isDependencyEnabled()) {
					useStroke = true;
				}
			}
		}

		for (final VisualProperty<?> vp : visualProperties) {
			// If mapping is available, use it instead.
			final VisualMappingFunction<?, ?> mapping = vs.getVisualMappingFunction(vp);
			if (mapping != null && mapping instanceof PassthroughMapping) {
				continue;
			}

			final CytoscapeJsToken tag = converter.getTag(vp);
			if (tag == null) {
				continue;
			}

			// tag can be null. In that case, use default,
			if (writeValue(vp, vs, jg, useSize, useStroke)) {
				final Object defaultValue = getDefaultVisualPropertyValue(vs, vp);
				jg.writeObjectField(tag.getTag(), defaultValue);
			}
		}
	}

	/**
	 * Special case handler which requires value conversion.
	 * 
	 * TODO: better way to do this?
	 * 
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	private final boolean writeValue(final VisualProperty<?> vp, final VisualStyle vs, final JsonGenerator jg, 
			final boolean sizeLock, final boolean arrowLock) throws JsonProcessingException, IOException {

		if (vp == BasicVisualLexicon.EDGE_TRANSPARENCY || vp == BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY
				|| vp == BasicVisualLexicon.NODE_LABEL_TRANSPARENCY || vp == BasicVisualLexicon.NODE_TRANSPARENCY
				|| vp == BasicVisualLexicon.NODE_BORDER_TRANSPARENCY) {
			final Integer defaultValue = (Integer) getDefaultVisualPropertyValue(vs, vp);
			double doubleValue = defaultValue/255d;
			jg.writeNumberField(converter.getTag(vp).getTag(), doubleValue);
			return false;
		} else if (vp == BasicVisualLexicon.NODE_WIDTH || vp == BasicVisualLexicon.NODE_HEIGHT ) {
			if(sizeLock) {
				final Double size = (Double) getDefaultVisualPropertyValue(vs, BasicVisualLexicon.NODE_SIZE);
				jg.writeNumberField(converter.getTag(vp).getTag(), size);
				return false;
			} else {
				return true;
			}
		} else if (vp == BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT || 
						vp.getIdString().equals("EDGE_TARGET_ARROW_UNSELECTED_PAINT") || 
						vp.getIdString().equals("EDGE_SOURCE_ARROW_UNSELECTED_PAINT")) {
			if(arrowLock) {
				final Paint color = (Paint) getDefaultVisualPropertyValue(vs, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
				jg.writeObjectField(converter.getTag(vp).getTag(), color);
				return false;
			} else {
				return true;
			}
		} else if (vp.getIdString().equals("NODE_LABEL_POSITION")) {
			// This is a hack...  We need to implement how to expose other renderer's VP
			final Object labelPosition = getDefaultVisualPropertyValue(vs, vp);
			String valText = labelPosition.toString();
			writeLabelPosition(jg, valText);
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 
	 * Temp function to write label position.
	 * 
	 *  TODO: replace this.
	 * 
	 * @throws IOException 
	 * @throws JsonGenerationException 
	 * 
	 */
	private final void writeLabelPosition(final JsonGenerator jg, final String valText) throws JsonGenerationException, IOException {
			String[] parts = valText.split(" ");
			
			final String position = parts[1];
			if(position.equals("North") || position.equals("Northeast") || position.equals("Northwest")) {
				jg.writeStringField(CytoscapeJsToken.TEXT_VALIGN.getTag(), "top");
			} else if(position.equals("Center") || position.equals("East") || position.equals("West")) {
				jg.writeStringField(CytoscapeJsToken.TEXT_VALIGN.getTag(), "center");
			} else {
				jg.writeStringField(CytoscapeJsToken.TEXT_VALIGN.getTag(), "bottom");
			}

			if(position.equals("West") || position.equals("Northwest") || position.equals("Southwest")) {
				jg.writeStringField(CytoscapeJsToken.TEXT_HALIGN.getTag(), "left");
			} else if(position.equals("East") || position.equals("Northeast") || position.equals("Southeast")) {
				jg.writeStringField(CytoscapeJsToken.TEXT_HALIGN.getTag(), "right");
			} else {
				jg.writeStringField(CytoscapeJsToken.TEXT_HALIGN.getTag(), "center");
			}
	}

	/**
	 * Special value handlers.
	 * 
	 * TODO: find better way to do this!
	 * 
	 * @param vp
	 * @param originalValue
	 * @return
	 */
	private Object convert(VisualProperty<?> vp, Object originalValue) {
		if (vp == BasicVisualLexicon.EDGE_TRANSPARENCY || vp == BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY
				|| vp == BasicVisualLexicon.NODE_LABEL_TRANSPARENCY || vp == BasicVisualLexicon.NODE_TRANSPARENCY
				|| vp == BasicVisualLexicon.NODE_BORDER_TRANSPARENCY) {
			return ((Integer)originalValue)/255d;
		} else if (vp.getIdString().equals("NODE_LABEL_POSITION")) {
			final String valText = originalValue.toString();
			String[] parts = valText.split(" ");
			final String position = parts[1];
			if(position.equals("North") || position.equals("Northeast") || position.equals("Northwest")) {
				return "top";
			} else if(position.equals("Center") || position.equals("East") || position.equals("West")) {
				return "center";
			} else {
				return "bottom";
			}
		} else {
			return originalValue;
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
			if (mapping == null || mapping instanceof DiscreteMapping) {
				continue;
			}

			// Skip unsupported Visual Properties
			final CytoscapeJsToken jsTag = converter.getTag(mapping.getVisualProperty());
			if (jsTag == null && mapping.getVisualProperty() != BasicVisualLexicon.NODE_SIZE) {
				continue;
			}

			if (mapping instanceof PassthroughMapping) {
				if(mapping.getVisualProperty() == BasicVisualLexicon.NODE_SIZE) {
					jg.writeStringField(CytoscapeJsToken.WIDTH.getTag(), passthrough.serialize((PassthroughMapping<?, ?>) mapping));
					jg.writeStringField(CytoscapeJsToken.HEIGHT.getTag(), passthrough.serialize((PassthroughMapping<?, ?>) mapping));
				} else {
					jg.writeStringField(jsTag.getTag(), passthrough.serialize((PassthroughMapping<?, ?>) mapping));
				}
			}
		}
	}

	@Override
	public Class<VisualStyle> handledType() {
		return VisualStyle.class;
	}
}