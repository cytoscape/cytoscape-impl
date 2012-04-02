
package org.cytoscape.ding.impl;

import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;

public class HandleFactoryImpl implements HandleFactory {

	public Handle createHandle(double x, double y) {
		return new HandleImpl(x,y);
	}
}
