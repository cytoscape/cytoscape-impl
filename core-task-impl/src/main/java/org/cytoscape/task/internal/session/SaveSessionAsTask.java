/*
 File: SaveSessionAsTask.java

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

import java.io.File;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class SaveSessionAsTask extends AbstractTask {
	@ProvidesTitle
	public String getTitle() {
		return "Save Session";
	}

	@Tunable(description = "Save Session As:", params = "fileCategory=session;input=false")
	public File file;

	private final CySessionWriterManager writerMgr;
	private final CySessionManager sessionMgr;
	private final RecentlyOpenedTracker tracker;
	private final CyEventHelper cyEventHelper;
	
	/**
	 * setAcceleratorCombo(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
	 */
	public SaveSessionAsTask(CySessionWriterManager writerMgr, CySessionManager sessionMgr,
			final RecentlyOpenedTracker tracker, final CyEventHelper cyEventHelper) {
		super();
		this.writerMgr = writerMgr;
		this.sessionMgr = sessionMgr;
		this.tracker = tracker;
		this.cyEventHelper = cyEventHelper;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.05);

		final CySession session = sessionMgr.getCurrentSession();
		insertTasksAfterCurrentTask(new CySessionWriter(writerMgr, session, file));
		taskMonitor.setProgress(1.0);
		
		// Fire event to tell others session had been saved to a file.
		cyEventHelper.fireEvent(new SessionSavedEvent(this, session, file.getAbsolutePath()));
	
		// Add this session file URL as the most recent file.
		if ( !file.getName().endsWith(".cys"))
			file = new File(file.getPath() + ".cys");
	
		tracker.add(file.toURI().toURL());
	}
}
