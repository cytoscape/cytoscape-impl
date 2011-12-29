package org.cytoscape.ding.impl.visualproperty;

import org.cytoscape.ding.Bend;
import org.cytoscape.ding.impl.BendImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.Range;

public class EdgeBendVisualProperty extends AbstractVisualProperty<Bend> {

	private static final Range<Bend> EDGE_BEND_RANGE;
	public static final Bend DEFAULT_EDGE_BEND = new BendImpl();

	static {
		EDGE_BEND_RANGE = new ContinuousRange<Bend>(Bend.class, DEFAULT_EDGE_BEND, DEFAULT_EDGE_BEND, true, true);
	}

	public EdgeBendVisualProperty(Bend defaultValue, String id, String displayName) {
		super(defaultValue, EDGE_BEND_RANGE, id, displayName, CyEdge.class);
	}

	@Override
	public String toSerializableString(final Bend value) {
		return value.toString();
	}

	@Override
	public Bend parseSerializableString(String value) {
		// TODO: Implement parser for String representation of Bend.
		return DEFAULT_EDGE_BEND;
	}
}
