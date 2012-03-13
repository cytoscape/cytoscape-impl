package org.cytoscape.group;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.internal.CyGroupFactoryImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;

import static org.mockito.Mockito.*;

public class GroupTestSupport {

	protected CyGroupFactory groupFactory;
	
	public GroupTestSupport() {
		final CyEventHelper help = mock(CyEventHelper.class);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		final CyGroupManagerImpl groupMgr = new CyGroupManagerImpl(help);
		
		this.groupFactory = new CyGroupFactoryImpl(help, groupMgr, serviceRegistrar);
	}

	public CyGroupFactory getGroupFactory() {
		return groupFactory;
	}
}
