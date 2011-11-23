
/*
 Copyright (c) 2008, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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


import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.Identifiable;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowSetRecord;

/**
 * Any time that the CyTableEntry.NAME column is set
 * in a local table, update the shared table with the
 * new name.
 */
class NameSetListener implements RowsSetListener {

	private final CyTable shared;
	private final CyTable local;

	NameSetListener(CyTable shared, CyTable local) {
		if ( shared == null )
			throw new NullPointerException("source table is null");
		if ( local == null )
			throw new NullPointerException("target table is null");
		if ( shared == local )
			throw new IllegalArgumentException("source and target tables cannot be the same!");
		this.shared = shared;
		this.local = local;
	}

	public void handleEvent(RowsSetEvent e) {
		if ( e.getSource() != local )
			return;
		for ( RowSetRecord record : e.getPayloadCollection() ) {
			// assume payload collection is for same column
			if ( !record.getColumn().equals(CyTableEntry.NAME) )
				return;

			CyRow r = shared.getRow( record.getRow().get( Identifiable.SUID, Long.class ) );
			if ( r != null ) 
				r.set(CyRootNetwork.SHARED_NAME, record.getValue());
		}
	}
}

