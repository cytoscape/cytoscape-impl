/*
 Copyright (c) 2008, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.model.internal;


import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.Identifiable;
import org.cytoscape.model.SUIDFactory;

import java.util.Map;


class CyTableEntryImpl implements CyTableEntry, Identifiable {
	private final long suid;
	private final Map<String, CyTable> attrMgr;

	CyTableEntryImpl(final Map<String, CyTable> attrMgr, long suid) {
		this.suid = suid;
		this.attrMgr = attrMgr;
		getCyRow().set(CyTableEntry.NAME, "");
		getCyRow().set(CyNetwork.SELECTED, Boolean.FALSE);
	}

	CyTableEntryImpl(final Map<String, CyTable> attrMgr) {
		this(attrMgr,SUIDFactory.getNextSUID());
	}

	final public long getSUID() {
		return suid;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param namespace DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	final public CyRow getCyRow(final String namespace) {
		if (namespace == null)
			throw new NullPointerException("namespace is null");

		final CyTable mgr = attrMgr.get(namespace);
		if (mgr == null)
			throw new NullPointerException("attribute manager is null for namespace: " + namespace);

		return mgr.getRow(suid);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	final public CyRow getCyRow() {
		final CyTable mgr = attrMgr.get(CyNetwork.DEFAULT_ATTRS);
		if (mgr == null)
			throw new NullPointerException("attribute manager is null for namespace: "
			                               + CyNetwork.DEFAULT_ATTRS);

		return mgr.getRow(suid);
	}
}
