/**

 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.network.merge.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.network.merge.internal.ui.NetworkMergeFrame;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.TaskManager;
import org.cytoscape.task.creation.CreateNetworkViewTaskFactory;

import java.awt.event.ActionEvent;


public class NetworkMergeAction extends AbstractCyAction {

	private static final long serialVersionUID = -597481727043928800L;
	
	private static final String APP_MENU_TITLE ="Merge Networks";
	private static final String PARENT_MENU ="Tools";
	
	private final CySwingApplication swingApp;
	private final CyNetworkManager cnm;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming cnn;
	private final TaskManager taskManager;
	private final CreateNetworkViewTaskFactory netViewCreator; 

	public NetworkMergeAction(CySwingApplication swingApp, CyNetworkManager cnm,
			CyNetworkFactory cnf, CyNetworkNaming cnn, TaskManager taskManager,
			CreateNetworkViewTaskFactory netViewCreator) {
		super(APP_MENU_TITLE);
		setPreferredMenu(PARENT_MENU);
		
		this.swingApp = swingApp;
		this.cnm = cnm;
		this.cnf = cnf;
		this.cnn = cnn;
		this.taskManager = taskManager;
		this.netViewCreator = netViewCreator;
	}

	/**
	 * This method is called when the user selects the menu item.
	 */
	@Override
	public void actionPerformed(final ActionEvent ae) {

		final NetworkMergeFrame frame = new NetworkMergeFrame(cnm, cnf, cnn, taskManager, netViewCreator);
		frame.setLocationRelativeTo(swingApp.getJFrame());
		frame.setVisible(true);
		// TODO: make this value user-editable (always on top or not).
		frame.setAlwaysOnTop(true);
	}

}
