/*
 File: NewSessionTask.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.session; 


import javax.swing.JOptionPane;

import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class NewSessionTask extends AbstractTask {

	/*@ProvidesTitle
	public String getTitle() {
		return "New Session";
	}
	
	@Tunable(description="<html>Current session (all networks/attributes) will be lost.<br />Do you want to continue?</html>")
	//public boolean destroyCurrentSession = true;*/

	private CySessionManager mgr;
	private Boolean test;
	
	
	public NewSessionTask(CySessionManager mgr,Boolean test) {
		this.mgr = mgr;
		this.test = test;
	}

	public void run(TaskMonitor taskMonitor) {
		// Ask user whether to delete current session or not.
		final String msg = "<html>Current session (all networks/attributes) will be lost.<br />Do you want to continue?</html>";
		final String header ="New Session";
		final Object[] options = { "Ok","Cancel" };
		final int n ;
		
		if (test ) {
			mgr.setCurrentSession(null,null);
		}
		else
		{
			n = JOptionPane.showOptionDialog(null, msg, header,
				                                     JOptionPane.OK_CANCEL_OPTION,
				                                     JOptionPane.QUESTION_MESSAGE, 
													 null, options, options[0]);
			if (n == JOptionPane.YES_OPTION  ) {
				mgr.setCurrentSession(null,null);
			}
		}

	
	}
}
