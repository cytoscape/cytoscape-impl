
/*
 File: SessionShutdownHandler.java

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

package org.cytoscape.internal;


//import org.cytoscape.application.swing.session.CySessionManager;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.events.CytoscapeShutdownEvent;
import org.cytoscape.application.swing.events.CytoscapeShutdownListener;

import javax.swing.JOptionPane;

public class SessionShutdownHandler implements CytoscapeShutdownListener {

//	private CySessionManager session;
	private CySwingApplication desktop;
	private CyNetworkManager netmgr;

	public SessionShutdownHandler(/*CySessionManager session,*/ CySwingApplication desktop, CyNetworkManager netmgr) {
		//this.session = session;
		this.desktop = desktop;
		this.netmgr = netmgr;
	}

	public void handleEvent(CytoscapeShutdownEvent e) {

		// If there are no networks, just quit.
		if (netmgr.getNetworkSet().size() == 0) 
			return;

		// Ask user whether to save current session or not.
		final String msg = "Do you want to save your session?";
		final String header = "Save Networks Before Quitting?";
		final Object[] options = { "Yes, save and quit", "No, just quit", "Cancel" };
		final int n = JOptionPane.showOptionDialog(desktop.getJFrame(), msg, header,
		                                     JOptionPane.YES_NO_OPTION,
		                                     JOptionPane.QUESTION_MESSAGE, 
											 null, options, options[0]);

		if (n == JOptionPane.NO_OPTION) {
			return;
		} else if (n == JOptionPane.YES_OPTION) {
			// TODO 
			System.out.println("SESSION SAVING NOT IMPLEMENTED !!!!");
			return;
		} else {
			e.abortShutdown("User canceled the shutdown request.");
			return; 
		}
	}
}
