package org.cytoscape.network.merge.internal;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Dialog;
import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.network.merge.internal.ui.NetworkMergeDialog;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskManager;


public class NetworkMergeAction extends AbstractCyAction {

	private static final long serialVersionUID = -597481727043928800L;
	
	private static final String APP_MENU_TITLE ="Networks...";
	private static final String PARENT_MENU ="Tools.Merge[2.0]";
	
	private final CySwingApplication swingApp;
	private final CyNetworkManager cnm;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming cnn;
	private final TaskManager taskManager;
	private final CreateNetworkViewTaskFactory netViewCreator;

	public NetworkMergeAction(CySwingApplication swingApp, CyApplicationManager cam, CyNetworkManager cnm,
			CyNetworkViewManager cnvm, CyNetworkFactory cnf, CyNetworkNaming cnn, TaskManager taskManager,
			CreateNetworkViewTaskFactory netViewCreator) {
		super(APP_MENU_TITLE, cam, "network", cnvm);
		setPreferredMenu(PARENT_MENU);
		setMenuGravity((float)0.0);
		
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

		final NetworkMergeDialog dialog = new NetworkMergeDialog(cnm, cnf, cnn, taskManager, netViewCreator);
		dialog.setLocationRelativeTo(swingApp.getJFrame());
		dialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		dialog.setVisible(true);
	}
}
