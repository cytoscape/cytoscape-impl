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
import org.cytoscape.io.internal.read.datatable.CSVCyReaderFactory;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskIterator;

public class SessionReaderFactoryImpl implements InputStreamTaskFactory {

	private final CyFileFilter filter;
	private final CyNetworkReaderManager networkReaderMgr;
	private final CyPropertyReaderManager propertyReaderMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private final CSVCyReaderFactory csvCyReaderFactory;
	private final CyTableManager tableManager;

	private InputStream inputStream;
	private String inputName;

	public SessionReaderFactoryImpl(final CyFileFilter filter,
									final CyNetworkReaderManager networkReaderMgr,
									final CyPropertyReaderManager propertyReaderMgr,
									final VizmapReaderManager vizmapReaderMgr,
									final CSVCyReaderFactory csvCyReaderFactory,
									final CyTableManager tableManager) {
		this.filter = filter;
		this.networkReaderMgr = networkReaderMgr;
		this.propertyReaderMgr = propertyReaderMgr;
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.csvCyReaderFactory = csvCyReaderFactory;
		this.tableManager = tableManager;
	}

	public void setInputStream(InputStream is, String in) {
		if (is == null) throw new NullPointerException("Input stream is null");
		inputStream = is;
		inputName = in;
	}

	public CyFileFilter getCyFileFilter() {
		return filter;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new SessionReaderImpl(inputStream, networkReaderMgr, propertyReaderMgr,
													  vizmapReaderMgr, csvCyReaderFactory, tableManager));
	}
}
