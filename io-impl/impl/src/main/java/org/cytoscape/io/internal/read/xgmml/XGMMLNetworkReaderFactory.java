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
package org.cytoscape.io.internal.read.xgmml;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractNetworkReaderFactory;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskIterator;

/**
 * XGMML file reader.<br>
 * This version is Metanode-compatible.
 * 
 * @since Cytoscape 2.3
 * @see cytoscape.data.writers.XGMMLWriter
 * @author kono
 */
public class XGMMLNetworkReaderFactory extends AbstractNetworkReaderFactory {

	private final RenderingEngineManager renderingEngineMgr;
	private final XGMMLParser parser;
	private final ReadDataManager readDataMgr;
	private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	public XGMMLNetworkReaderFactory(CyFileFilter filter,
									 CyNetworkViewFactory cyNetworkViewFactory,
									 CyNetworkFactory cyNetworkFactory,
									 RenderingEngineManager renderingEngineMgr,
									 ReadDataManager readDataMgr,
									 XGMMLParser parser,
									 UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
		this.renderingEngineMgr = renderingEngineMgr;
		this.readDataMgr = readDataMgr;
		this.parser = parser;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new XGMMLNetworkReader(inputStream, cyNetworkViewFactory, cyNetworkFactory,
													   renderingEngineMgr, readDataMgr, parser,
													   unrecognizedVisualPropertyMgr));
	}
}
