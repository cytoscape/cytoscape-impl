
package org.cytoscape.ding.impl;

import org.cytoscape.ding.HandleFactory;
import org.cytoscape.ding.Handle;

public class HandleFactoryImpl implements HandleFactory {

	public Handle createHandle(double x, double y) {
		return new HandleImpl(x,y);
	}
}
