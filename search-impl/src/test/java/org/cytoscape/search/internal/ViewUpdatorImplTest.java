package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

public class ViewUpdatorImplTest {

	@Test
	public void testUpdateViewWithNullViewsNullNetworkView() {
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		CyNetworkViewManager mockManager = mock(CyNetworkViewManager.class);
		CyApplicationManager mockApplication = mock(CyApplicationManager.class);

		CyNetwork mockNetwork = mock(CyNetwork.class);
		
		when(mockManager.getNetworkViews(mockNetwork)).thenReturn(null);
		when(mockApplication.getCurrentNetworkView()).thenReturn(null);
		
		when(mockRegistrar.getService(CyNetworkViewManager.class)).thenReturn(mockManager);
		when(mockRegistrar.getService(CyApplicationManager.class)).thenReturn(mockApplication);
		ViewUpdatorImpl updator = new ViewUpdatorImpl(mockRegistrar);
		updator.updateView(mockNetwork);

		verify(mockApplication).getCurrentNetworkView();
	}

	@Test
	public void testUpdateViewNoViewsAndNonNullNetworkView() {
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		CyNetworkViewManager mockManager = mock(CyNetworkViewManager.class);
		CyApplicationManager mockApplication = mock(CyApplicationManager.class);

		CyNetwork mockNetwork = mock(CyNetwork.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		
		when(mockManager.getNetworkViews(mockNetwork)).thenReturn(new ArrayList<CyNetworkView>());
		when(mockApplication.getCurrentNetworkView()).thenReturn(mockView);
		
		when(mockRegistrar.getService(CyNetworkViewManager.class)).thenReturn(mockManager);
		when(mockRegistrar.getService(CyApplicationManager.class)).thenReturn(mockApplication);
		ViewUpdatorImpl updator = new ViewUpdatorImpl(mockRegistrar);
		updator.updateView(mockNetwork);

		verify(mockView).updateView();
		verify(mockApplication).getCurrentNetworkView();
	}
	
	@Test
	public void testUpdateViewWithViewsAndNullNetworkView() {
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		CyNetworkViewManager mockManager = mock(CyNetworkViewManager.class);
		CyApplicationManager mockApplication = mock(CyApplicationManager.class);

		CyNetwork mockNetwork = mock(CyNetwork.class);
		ArrayList<CyNetworkView> viewList = new ArrayList<>();
		CyNetworkView mockView = mock(CyNetworkView.class);
		viewList.add(mockView);
		
		when(mockManager.getNetworkViews(mockNetwork)).thenReturn(viewList);
		when(mockApplication.getCurrentNetworkView()).thenReturn(null);
		
		when(mockRegistrar.getService(CyNetworkViewManager.class)).thenReturn(mockManager);
		when(mockRegistrar.getService(CyApplicationManager.class)).thenReturn(mockApplication);
		ViewUpdatorImpl updator = new ViewUpdatorImpl(mockRegistrar);
		updator.updateView(mockNetwork);

		verify(mockView).updateView();
		verify(mockApplication).getCurrentNetworkView();
	}
	
	@Test
	public void testUpdateViewWithViewsAndNetworkView() {
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		CyNetworkViewManager mockManager = mock(CyNetworkViewManager.class);
		CyApplicationManager mockApplication = mock(CyApplicationManager.class);

		CyNetwork mockNetwork = mock(CyNetwork.class);
		ArrayList<CyNetworkView> viewList = new ArrayList<>();
		CyNetworkView mockView = mock(CyNetworkView.class);
		viewList.add(mockView);
		
		CyNetworkView mockNetView = mock(CyNetworkView.class);
		when(mockManager.getNetworkViews(mockNetwork)).thenReturn(viewList);
		when(mockApplication.getCurrentNetworkView()).thenReturn(mockNetView);
		
		when(mockRegistrar.getService(CyNetworkViewManager.class)).thenReturn(mockManager);
		when(mockRegistrar.getService(CyApplicationManager.class)).thenReturn(mockApplication);
		ViewUpdatorImpl updator = new ViewUpdatorImpl(mockRegistrar);
		updator.updateView(mockNetwork);

		verify(mockView).updateView();
		verify(mockNetView).updateView();
		verify(mockApplication).getCurrentNetworkView();
	}
}
