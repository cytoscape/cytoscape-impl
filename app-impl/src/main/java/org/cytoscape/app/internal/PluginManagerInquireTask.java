/*
 File: PluginManagerInquireTask.java 
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
package org.cytoscape.app.internal;

import java.util.List;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class PluginManagerInquireTask implements Task {


	private String url;

	private PluginInquireAction actionObj;

	private TaskMonitor taskMonitor;

	public PluginManagerInquireTask(String Url, PluginInquireAction Obj) {
		url = Url;
		actionObj = Obj;
	}

	public void setTaskMonitor(TaskMonitor monitor)
			throws IllegalThreadStateException {
		taskMonitor = monitor;
	}

	public void halt() {
		// not implemented
	}

	public String getTitle() {
		return "Attempting to connect to " + url;
	}

	public void run(TaskMonitor taskMonitor) {
		List<DownloadableInfo> Results = null;

		taskMonitor.setStatusMessage(actionObj.getProgressBarMessage());

		try {
			Results = PluginManager.getPluginManager().inquire(url);
		} catch (org.jdom.JDOMException jde) {
			actionObj.setExceptionThrown(jde);
		} catch (java.io.IOException ioe) {
			actionObj.setExceptionThrown(ioe);
		} catch (Exception e) {

			if (e.getClass().equals(java.lang.NullPointerException.class)) {
				e = new org.jdom.JDOMException(
						"XML was incorrectly formed", e);
			}
			actionObj.setExceptionThrown(e);
		} finally {
			taskMonitor.setProgress(100);
			actionObj.inquireAction(Results);
		}
	}

	public void cancel(){
		
	}
}
