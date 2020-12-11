package org.cytoscape.ding.impl.visualproperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.presentation.property.values.AbstractVisualPropertyValue;

public class EdgeStackingVisualProperty extends AbstractVisualProperty<EdgeStacking> {
	
	public static final EdgeStacking AUTO_BEND = new EdgeStackingImpl("Auto Bend", "AUTO_BEND");
	public static final EdgeStacking HAYSTACK  = new EdgeStackingImpl("Haystack", "HAYSTACK");
	public static final EdgeStacking PARALLEL_HAYSTACK  = new EdgeStackingImpl("Parallel", "PARALLEL_HAYSTACK");
	
	private static final DiscreteRange<EdgeStacking> RANGE;
	private static final Map<String,EdgeStacking> VALUES;
	
	static {
		VALUES = new HashMap<>();
		VALUES.put(AUTO_BEND.getSerializableString().toUpperCase(), AUTO_BEND);
		VALUES.put(HAYSTACK.getSerializableString().toUpperCase(), HAYSTACK);
		VALUES.put(PARALLEL_HAYSTACK.getSerializableString().toUpperCase(), PARALLEL_HAYSTACK);
		RANGE = new DiscreteRange<>(EdgeStacking.class, new HashSet<>(VALUES.values()));
	}

	public EdgeStackingVisualProperty(EdgeStacking defaultValue, String id, String displayName, Class<? extends CyIdentifiable> modelDataType) {
		super(defaultValue, RANGE, id, displayName, modelDataType);
	}
	
	@Override
	public String toSerializableString(EdgeStacking value) {
		return value.getSerializableString();
	}

	@Override
	public EdgeStacking parseSerializableString(String value) {
		return value == null ? null : VALUES.get(value.toUpperCase());
	}
	
	private static class EdgeStackingImpl extends AbstractVisualPropertyValue implements EdgeStacking {
		public EdgeStackingImpl(String displayName, String serializableString) {
			super(displayName, serializableString);
		}
	}

}
