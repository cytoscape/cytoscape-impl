/*
  File: UndoMonitor.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

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
package org.cytoscape.internal.util.undo;

import java.util.Properties;

import org.cytoscape.session.events.SetCurrentNetworkViewEvent;
import org.cytoscape.session.events.SetCurrentNetworkViewListener;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.property.CyProperty;

/**
 * This class monitors the undoable edit stack and implements whatever
 * discard policy we might have. Currently, we discard all edits if
 * the network view focus changes.
 */
public class UndoMonitor implements SetCurrentNetworkViewListener {

	private UndoSupport undo;
	private Properties props;

	public UndoMonitor(UndoSupport undo,CyProperty<Properties> cyProps) {

		this.undo = undo;
		this.props = cyProps.getProperties();

		undo.getUndoManager().setLimit( getLimit() );
	}

    private int getLimit() {
        int lim;
        try {
            lim = Integer.parseInt( props.getProperty("undo.limit") );
        } catch ( Exception e ) {
            e.printStackTrace();
            lim = 10;
        }

        if ( lim < 0 )
            lim = 10;

        return lim;
    }

	/**
 	 * This method listens for changes to the current network and discards all edits
   	 * when the network changes. 
	 *
	 * @param e The change event.
	 */
    public void handleEvent(SetCurrentNetworkViewEvent e) {
		if ( e.getNetworkView() != null )
			undo.getUndoManager().discardAllEdits();
    }
}

