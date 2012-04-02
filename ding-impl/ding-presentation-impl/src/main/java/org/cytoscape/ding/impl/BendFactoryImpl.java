package org.cytoscape.ding.impl;

import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.BendFactory;

public class BendFactoryImpl implements BendFactory {

	@Override
	public Bend createBend() {
		return new BendImpl();
	}

	@Override
	public Bend parseSerializableString(String serializedString) {
		return BendImpl.parseSerializableString(serializedString);
	}
}
