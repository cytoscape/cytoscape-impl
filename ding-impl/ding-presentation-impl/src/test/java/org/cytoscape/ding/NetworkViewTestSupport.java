
package org.cytoscape.ding;

import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryProvider;
import org.cytoscape.view.model.network.NetworkViewFactoryTestSupport;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class NetworkViewTestSupport extends NetworkTestSupport {

	protected NetworkViewFactoryTestSupport networkViewFactoryTestSupport;
	
	public NetworkViewTestSupport() {
		networkViewFactoryTestSupport = new NetworkViewFactoryTestSupport();
	}
	
	public CyNetworkView getNetworkView() {
		return getNetworkViewFactory().createNetworkView(getNetwork());
	}

	public CyNetworkViewFactory getNetworkViewFactory() {
		var lexicon = new DVisualLexicon();
		return networkViewFactoryTestSupport.getNetworkViewFactory(lexicon);
	}
	
	public CyNetworkViewFactoryProvider getNetworkViewFactoryProvider() {
		return networkViewFactoryTestSupport.getNetworkViewFactoryFactory();
	}
}
