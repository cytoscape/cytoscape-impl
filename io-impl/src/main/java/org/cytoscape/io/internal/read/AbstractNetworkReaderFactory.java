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
package org.cytoscape.io.internal.read;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;

public abstract class AbstractNetworkReaderFactory implements InputStreamTaskFactory {

	private final CyFileFilter filter;

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	protected InputStream inputStream;
	protected String inputName;

	public AbstractNetworkReaderFactory(CyFileFilter filter, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory) {
		this.filter = filter;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
	}

	public void setInputStream(InputStream is, String in) {
		if (is == null)
			throw new NullPointerException("Input stream is null");
		inputStream = is;
		inputName = in;
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return filter;
	}
}
