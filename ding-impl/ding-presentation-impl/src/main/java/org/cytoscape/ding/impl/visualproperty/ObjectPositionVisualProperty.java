package org.cytoscape.ding.impl.visualproperty;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.Range;
import org.cytoscape.view.presentation.property.values.ObjectPosition;

public class ObjectPositionVisualProperty extends AbstractVisualProperty<ObjectPosition> {
	
	private static final Range<ObjectPosition> OBJECT_POSITION_RANGE;
	
	private static final ObjectPosition MIN_OBJECT = ObjectPosition.DEFAULT_POSITION;

	static {
		OBJECT_POSITION_RANGE = new ContinuousRange<ObjectPosition>(ObjectPosition.class, MIN_OBJECT, MIN_OBJECT, true, true);
	}

	public ObjectPositionVisualProperty(ObjectPosition defaultValue,
			String id, String displayName,
			Class<? extends CyIdentifiable> targetObjectDataType) {
		super(defaultValue, OBJECT_POSITION_RANGE, id, displayName, targetObjectDataType);
	}

	@Override
	public String toSerializableString(final ObjectPosition value) {
		return value.toSerializableString();
	}

	@Override
	public ObjectPosition parseSerializableString(final String objectPositionString) {
		return ObjectPosition.parse(objectPositionString);
	}

}
