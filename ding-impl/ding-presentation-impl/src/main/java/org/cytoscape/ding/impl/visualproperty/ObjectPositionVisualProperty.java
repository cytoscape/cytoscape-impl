package org.cytoscape.ding.impl.visualproperty;

import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.impl.ObjectPositionImpl;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.Range;

public class ObjectPositionVisualProperty extends AbstractVisualProperty<ObjectPosition> {
	
	private static final Range<ObjectPosition> OBJECT_POSITION_RANGE;
	
	private static final ObjectPosition MIN_OBJECT = ObjectPositionImpl.DEFAULT_POSITION;

	static {
		OBJECT_POSITION_RANGE = new ContinuousRange<ObjectPosition>(ObjectPosition.class, MIN_OBJECT, MIN_OBJECT, true, true);
	}

	public ObjectPositionVisualProperty(ObjectPosition defaultValue,
			String id, String displayName,
			Class<?> targetObjectDataType) {
		super(defaultValue, OBJECT_POSITION_RANGE, id, displayName, targetObjectDataType);
	}

	@Override
	public String toSerializableString(ObjectPosition value) {
		return value.shortString();
	}

	@Override
	public ObjectPosition parseSerializableString(String value) {
		// TODO Auto-generated method stub
		return null;
	}

}
