package org.cytoscape.view.table.internal.cg;

import java.util.Collections;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.Range;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.table.internal.cg.sparkline.CellSparkline;

public class CellCGVisualProperty extends AbstractVisualProperty<CellCustomGraphics> {

	private static final Range<CellCustomGraphics> RANGE = new DiscreteRange<>(
			CellCustomGraphics.class,
			Collections.emptySet()
	) {
		@Override
		public boolean inRange(CellCustomGraphics value) {
			return value != null;
		}
	};

	public CellCGVisualProperty(
			CellCustomGraphics defaultValue,
			String id,
			String displayName,
			Class<? extends CyIdentifiable> targetObjectDataType
	) {
		super(defaultValue, RANGE, id, displayName, targetObjectDataType);
	}

	@Override
	public String toSerializableString(CellCustomGraphics value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellSparkline parseSerializableString(String value) {
		// TODO Auto-generated method stub
		return null;
	}
}
