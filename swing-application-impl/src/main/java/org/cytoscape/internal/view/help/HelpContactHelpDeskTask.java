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


import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class HelpContactHelpDeskTask extends AbstractTask {
	// Google Group's help desk top
	private String helpDeskURL = "https://groups.google.com/forum/?fromgroups#!forum/cytoscape-helpdesk";
	private OpenBrowser openBrowser;

	public HelpContactHelpDeskTask(OpenBrowser openBrowser) {
		this.openBrowser = openBrowser;
	}

	public void run(TaskMonitor tm) {
		openBrowser.openURL(helpDeskURL);
	}

	@Override
	public void cancel() {
	}
}
