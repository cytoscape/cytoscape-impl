package org.cytoscape.group;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.internal.CyGroupFactoryImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;

import static org.mockito.Mockito.*;

public class GroupTestSupport {

	protected CyGroupFactory groupFactory;
	protected CyGroupManagerImpl groupManager;
	
	public GroupTestSupport() {
		final CyEventHelper help = mock(CyEventHelper.class);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		groupManager = new CyGroupManagerImpl(help);
		groupFactory = new CyGroupFactoryImpl(help, groupManager, serviceRegistrar);
	}

	public CyGroupFactory getGroupFactory() {
		return groupFactory;
	}

	public CyGroupManagerImpl getGroupManager() {
		return groupManager;
	}
}
