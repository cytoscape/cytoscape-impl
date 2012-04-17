
package org.cytoscape.ding.impl;

import org.cytoscape.work.TaskFactory;

// This interface allows us to provide the actual TaskFactory name
// of TaskFactories without the proper service metadata (e.g. title).
public interface NamedTaskFactory extends TaskFactory {
	String getName();
}
