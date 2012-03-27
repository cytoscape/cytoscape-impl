/*
 File: SaveSessionAsTaskFactory.java

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
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.session.SaveSessionAsTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class SaveSessionAsTaskFactoryImpl extends AbstractTaskFactory implements SaveSessionAsTaskFactory {

	private CySessionManager sessionMgr;
	private CySessionWriterManager writerMgr;
	private final RecentlyOpenedTracker tracker;
	private final CyEventHelper cyEventHelper;
	
	private final TunableSetter tunableSetter;

	public SaveSessionAsTaskFactoryImpl(CySessionWriterManager writerMgr, CySessionManager sessionMgr,
			final RecentlyOpenedTracker tracker, final CyEventHelper cyEventHelper, TunableSetter tunableSetter) {
		this.sessionMgr = sessionMgr;
		this.writerMgr = writerMgr;
		this.tracker = tracker;
		this.cyEventHelper = cyEventHelper;
		this.tunableSetter = tunableSetter;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new SaveSessionAsTask(writerMgr, sessionMgr, tracker, cyEventHelper));
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("file", file);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}
}
