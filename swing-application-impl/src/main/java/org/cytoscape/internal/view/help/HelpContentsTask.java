package org.cytoscape.internal.view.help;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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


import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import javax.help.CSH;
import java.awt.event.ActionEvent;


/**
 * Really just a wrapper for the CSH.DisplayHelpFromSource action.
 */
public class HelpContentsTask extends AbstractTask {
	private CSH.DisplayHelpFromSource csh; 
	private CySwingApplication app;

	public HelpContentsTask(CyHelpBroker help, CySwingApplication app) {
		csh = new CSH.DisplayHelpFromSource(help.getHelpBroker());
		this.app = app;
	}

	public void run(TaskMonitor tm) {
		csh.actionPerformed(new ActionEvent(app.getJFrame(),0,"help"));
	}

	@Override
	public void cancel() {
	}
}
