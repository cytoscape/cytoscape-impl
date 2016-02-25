package org.cytoscape.internal.view;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.StatusBarPanelFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CytoscapeDesktopTest {

	CytoscapeDesktop desktop;
	
	@Mock NetworkViewMediator netViewMediator;
	@Mock CyServiceRegistrar registrar;
	@Mock CyShutdown shut;
	@Mock CyEventHelper eh;
	@Mock DialogTaskManager taskMgr;
	@Mock StatusBarPanelFactory taskStatusPanelFactory;
	@Mock IconManager icoMgr;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		final JPanel panel = new JPanel();
		when(taskStatusPanelFactory.createTaskStatusPanel()).thenReturn(panel);
		
		CytoscapeMenus menus = new CytoscapeMenus(new CytoscapeMenuBar(), new CytoscapeToolBar());
		
		desktop = new CytoscapeDesktop(menus, netViewMediator, shut, eh, registrar, taskMgr, icoMgr);
	}
	
	@Test
	public void testAddCytoPanelComponent() {
		DummyCytoPanelComponent2 c1 = new DummyCytoPanelComponent2("org.cytoscape.Comp1");
		desktop.addCytoPanelComponent(c1, new Properties());
		
		DummyCytoPanelComponent c2 = new DummyCytoPanelComponent();
		desktop.addCytoPanelComponent(c2, new Properties());
		
		DummyCytoPanelComponent2 c3 = new DummyCytoPanelComponent2("org.cytoscape.Comp2");
		desktop.addCytoPanelComponent(c3, new Properties());
		
		// Test:
		CytoPanel cp = desktop.getCytoPanel(CytoPanelName.WEST);
		assertEquals(3, cp.getCytoPanelComponentCount());
		
		assertEquals(0, cp.indexOfComponent(c1.getComponent()));
		assertEquals(1, cp.indexOfComponent(c2.getComponent()));
		assertEquals(2, cp.indexOfComponent(c3.getComponent()));
		
		assertEquals(0, cp.indexOfComponent(c1.getIdentifier()));
		assertEquals(1, cp.indexOfComponent(c2.getComponent()));
		assertEquals(2, cp.indexOfComponent(c3.getIdentifier()));
		
		// The other CytoPanels should not be affected
		cp = desktop.getCytoPanel(CytoPanelName.EAST);
		assertEquals(0, cp.getCytoPanelComponentCount());
		assertEquals(-1, cp.indexOfComponent(c1.getIdentifier()));
		
		cp = desktop.getCytoPanel(CytoPanelName.SOUTH);
		assertEquals(0, cp.getCytoPanelComponentCount());
	}
	
	@Test(expected=NullPointerException.class)
	public void testAddCytoPanelComponent2_NullIdentifier() {
		DummyCytoPanelComponent2 c = new DummyCytoPanelComponent2(null);
		desktop.addCytoPanelComponent(c, new Properties());
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
	
	@SuppressWarnings("serial")
	static class DummyCytoPanelComponent2 extends DummyCytoPanelComponent implements CytoPanelComponent2 {
		
		private final String identifier;
		
		public DummyCytoPanelComponent2(String identifier) {
			this.identifier = identifier;
		}
		
		@Override
		public String getIdentifier() {
			return identifier;
		}
	}
}
