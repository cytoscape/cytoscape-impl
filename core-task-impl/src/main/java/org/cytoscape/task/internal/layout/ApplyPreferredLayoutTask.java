/*
 Copyright (c) 2006, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.layout;


import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.TaskMonitor;

import java.util.Properties;


public class ApplyPreferredLayoutTask extends AbstractNetworkViewTask {
	private static final String DEF_LAYOUT = "force-directed";

	private Properties props;
	private final CyLayoutAlgorithmManager layouts;

	public ApplyPreferredLayoutTask(final CyNetworkView v,
	                                final CyLayoutAlgorithmManager layouts,
	                                final Properties props)
	{
		super(v);
		this.layouts     = layouts;
		this.props       = props;
	}
	
	public ApplyPreferredLayoutTask(final CyNetworkView v,
	                                final CyLayoutAlgorithmManager layouts)
	{
		super(v);
		this.layouts = layouts;
	}

	
	@Override
	public void run(TaskMonitor tm) {
		tm.setProgress(0.0d);
		tm.setStatusMessage("Applying Default Layout...");
		tm.setProgress(0.1);
		String pref = CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME;
		if (props != null)
			pref = props.getProperty("preferredLayoutAlgorithm", DEF_LAYOUT);
		tm.setProgress(0.2d);
		final CyLayoutAlgorithm layout = layouts.getLayout(pref);
		if (layout != null) {
			tm.setProgress(0.6);
			insertTasksAfterCurrentTask(layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,""));
		} else {
			throw new IllegalArgumentException("Couldn't find layout algorithm: " + pref);
		}
		tm.setProgress(1.0);
	}
}
