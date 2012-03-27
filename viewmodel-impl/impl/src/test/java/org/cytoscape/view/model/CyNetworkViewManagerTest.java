package org.cytoscape.view.model;

import static org.mockito.Mockito.*;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.internal.CyNetworkViewManagerImpl;
import org.junit.After;
import org.junit.Before;


public class CyNetworkViewManagerTest extends AbstractCyNetworkViewManagerTest {
	
	private NetworkTestSupport testSupport;
	
	public CyNetworkViewManagerTest() {
		testSupport = new NetworkTestSupport();
		this.network1 = testSupport.getNetwork();
		this.network2 = testSupport.getNetwork();
	}
	
	@Before
	public void setUp() throws Exception {
		
		final CyEventHelper cyEventHelper = mock(CyEventHelper.class);
		viewManager = new CyNetworkViewManagerImpl(cyEventHelper);
		this.networkViewA = mock(CyNetworkView.class);
		this.networkViewB = mock(CyNetworkView.class);
		this.networkViewC = mock(CyNetworkView.class);
		this.networkViewD = mock(CyNetworkView.class);
		this.networkViewE = mock(CyNetworkView.class);
		
		when(networkViewA.getModel()).thenReturn(network1);
		when(networkViewB.getModel()).thenReturn(network1);
		when(networkViewC.getModel()).thenReturn(network1);
		when(networkViewD.getModel()).thenReturn(network2);
		when(networkViewE.getModel()).thenReturn(network2);
		
		viewManager.addNetworkView(networkViewA);
		viewManager.addNetworkView(networkViewB);
		viewManager.addNetworkView(networkViewC);
		viewManager.addNetworkView(networkViewD);
		viewManager.addNetworkView(networkViewE);
	}

	@After
	public void tearDown() throws Exception {
	}

}
