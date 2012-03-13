
package org.cytoscape.ding.impl;

import org.cytoscape.ding.BendFactory;
import org.cytoscape.ding.Bend;

public class BendFactoryImpl implements BendFactory {
	public Bend createBend() {
		return new BendImpl();
	}
}
