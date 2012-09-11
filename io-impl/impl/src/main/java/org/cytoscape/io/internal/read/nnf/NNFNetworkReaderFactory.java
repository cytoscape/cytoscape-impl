/*
 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.read.nnf;

import java.io.InputStream;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractNetworkReaderFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class NNFNetworkReaderFactory extends AbstractNetworkReaderFactory {

	private final CyLayoutAlgorithmManager layouts;
	private final CyNetworkManager cyNetworkManagerServiceRef;
	private final CyRootNetworkManager cyRootNetworkFactory;


	public NNFNetworkReaderFactory(CyFileFilter filter, CyLayoutAlgorithmManager layouts,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			 CyNetworkManager cyNetworkManagerServiceRef,CyRootNetworkManager cyRootNetworkFactory) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
		this.layouts = layouts;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyRootNetworkFactory = cyRootNetworkFactory;
	}

	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new NNFNetworkReader(inputStream, layouts, cyNetworkViewFactory, cyNetworkFactory,
				 cyNetworkManagerServiceRef, cyRootNetworkFactory));
	}
}
