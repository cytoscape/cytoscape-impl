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
package org.cytoscape.io.internal.read.session;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.SimpleInputStreamTaskFactory;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.TaskIterator;

public class Cy2SessionReaderFactoryImpl extends SimpleInputStreamTaskFactory {

	private final CyNetworkReaderManager networkReaderMgr;
	private final CyPropertyReaderManager propertyReaderMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private final ReadCache cache;
	private final CyRootNetworkManager rootNetworkManager;

	public Cy2SessionReaderFactoryImpl(final CyFileFilter filter,
									   final ReadCache cache,
									   final CyNetworkReaderManager networkReaderMgr,
									   final CyPropertyReaderManager propertyReaderMgr,
									   final VizmapReaderManager vizmapReaderMgr,
									   final CyRootNetworkManager rootNetworkManager) {
		super(filter);
		this.cache = cache;
		this.networkReaderMgr = networkReaderMgr;
		this.propertyReaderMgr = propertyReaderMgr;
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.rootNetworkManager = rootNetworkManager;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new Cy2SessionReaderImpl(inputStream, cache, networkReaderMgr, propertyReaderMgr,
				vizmapReaderMgr, rootNetworkManager));
	}
}
