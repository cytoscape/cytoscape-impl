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

import org.cytoscape.application.CyVersion;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class HelpReportABugTask extends AbstractTask{

	private String bugReportURL = "http://chianti.ucsd.edu/cyto_web/bugreport/bugreport.php";
	private OpenBrowser openBrowser;
	private final CyVersion cyVersion;
	
	public HelpReportABugTask(OpenBrowser openBrowser,  CyVersion cyVersion) {
		this.openBrowser = openBrowser;
		this.cyVersion = cyVersion;
	}

	public void run(TaskMonitor tm) {
		// get OS string
		String os_str = System.getProperty("os.name")+ "_"+ System.getProperty("os.version");
		os_str = os_str.replace(" ", "_");
		
		bugReportURL = "http://chianti.ucsd.edu/cyto_web/bugreport/bugreport.php?cyversion="+cyVersion.getVersion()+"&os="+os_str;
		openBrowser.openURL(bugReportURL);
	}

}
