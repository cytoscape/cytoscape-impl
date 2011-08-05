package org.cytoscape.view.vizmap.gui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.cytoscape.view.vizmap.gui.VisualPropertyDependency;
import org.cytoscape.view.vizmap.gui.VisualPropertyDependencyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisualPropertyDependencyManagerImpl implements VisualPropertyDependencyManager {
	
	private static final Logger logger = LoggerFactory.getLogger(VisualPropertyDependencyManagerImpl.class);
	
	private final List<VisualPropertyDependency> depList;
	
	VisualPropertyDependencyManagerImpl() {
		this.depList = new ArrayList<VisualPropertyDependency>();
	}

	@Override
	public Collection<VisualPropertyDependency> getDependencies() {
		return depList;
	}
	
	public void addDependency(final VisualPropertyDependency dep, @SuppressWarnings("rawtypes") Map props) {
		logger.info("@@@@@@@@@@@@ New Dependency: " + dep.getDisplayName());
		depList.add(dep);
	}

	public void removeDependency(final VisualPropertyDependency dep, @SuppressWarnings("rawtypes") Map props) {
		logger.debug("Removing Dependency: " + dep.getDisplayName());
		depList.remove(dep);
	}

}
