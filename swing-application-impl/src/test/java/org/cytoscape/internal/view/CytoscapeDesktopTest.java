package org.cytoscape.internal.view;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Component;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponentName;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.TaskStatusPanelFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CytoscapeDesktopTest {

	CytoscapeDesktop desktop;
	
	@Mock NetworkViewManager netViewMgr;
	@Mock CyServiceRegistrar registrar;
	@Mock CyShutdown shut;
	@Mock CyEventHelper eh;
	@Mock DialogTaskManager taskMgr;
	@Mock TaskStatusPanelFactory taskStatusPanelFactory;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		final JPanel panel = new JPanel();
		when(taskStatusPanelFactory.createTaskStatusPanel()).thenReturn(panel);
		
		IconManagerImpl icoMgr = new IconManagerImpl();
		CytoscapeMenus menus = new CytoscapeMenus(new CytoscapeMenuBar(), new CytoscapeToolBar());
		
		desktop = new CytoscapeDesktop(menus, netViewMgr, shut, eh, registrar, taskMgr, taskStatusPanelFactory, icoMgr);
	}
	
	@Test
	public void testAddCytoPanelComponent() {
		Properties props = new Properties();
		
		DummyCytoPanelComponent c1 = new DummyCytoPanelComponent();
		props.put("cytoPanelComponentName", CytoPanelComponentName.NETWORK.toString());
		desktop.addCytoPanelComponent(c1, props);
		
		DummyCytoPanelComponent c2 = new DummyCytoPanelComponent();
		props.put("cytoPanelComponentName", CytoPanelComponentName.STYLE.toString());
		desktop.addCytoPanelComponent(c2, props);
		
		DummyCytoPanelComponent c3 = new DummyCytoPanelComponent();
		props.put("cytoPanelComponentName", CytoPanelComponentName.FILTER.toString());
		desktop.addCytoPanelComponent(c3, props);
		
		// Test:
		CytoPanel cp = desktop.getCytoPanel(CytoPanelName.WEST);
		assertEquals(3, cp.getCytoPanelComponentCount());
		
		assertEquals(0, cp.indexOfComponent(c1.getComponent()));
		assertEquals(1, cp.indexOfComponent(c2.getComponent()));
		assertEquals(2, cp.indexOfComponent(c3.getComponent()));
		
		assertEquals(0, cp.indexOfComponent(CytoPanelComponentName.NETWORK));
		assertEquals(1, cp.indexOfComponent(CytoPanelComponentName.STYLE));
		assertEquals(2, cp.indexOfComponent(CytoPanelComponentName.FILTER));
		
		// The other CytoPanels should not be affected
		cp = desktop.getCytoPanel(CytoPanelName.EAST);
		assertEquals(0, cp.getCytoPanelComponentCount());
		assertEquals(-1, cp.indexOfComponent(CytoPanelComponentName.NETWORK));
		
		cp = desktop.getCytoPanel(CytoPanelName.SOUTH);
		assertEquals(0, cp.getCytoPanelComponentCount());
	}
	
	@SuppressWarnings("serial")
	static class DummyCytoPanelComponent extends JPanel implements CytoPanelComponent {

		@Override
		public Component getComponent() {
			return this;
		}

		@Override
		public CytoPanelName getCytoPanelName() {
			return CytoPanelName.WEST;
		}

		@Override
		public String getTitle() {
			return "Panel";
		}

		@Override
		public Icon getIcon() {
			return null;
		}
	}
}
