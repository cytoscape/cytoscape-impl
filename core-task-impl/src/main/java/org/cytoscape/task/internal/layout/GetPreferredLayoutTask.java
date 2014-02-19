package org.cytoscape.task.internal.layout;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.util.Collection;
import java.util.Properties;

import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class GetPreferredLayoutTask extends AbstractTask implements ObservableTask {

	private static final String DEF_LAYOUT = "force-directed";

	private Properties props;
	private final CyLayoutAlgorithmManager layouts;
	private CyLayoutAlgorithm preferredLayout;

	public GetPreferredLayoutTask(final CyLayoutAlgorithmManager layouts, final Properties props) {
		this.layouts = layouts;
		this.props = props;
	}

	@Override
	public void run(TaskMonitor tm) {
		
		String pref = CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME;
		if (props != null)
			pref = props.getProperty("preferredLayoutAlgorithm", DEF_LAYOUT);
		tm.showMessage(TaskMonitor.Level.INFO, "Preferred layout is "+pref);
		preferredLayout = layouts.getLayout(pref);
		if (preferredLayout == null) {
			tm.showMessage(TaskMonitor.Level.WARN, "...but it's not available!");
		}
	}

	@Override
	public Object getResults(Class type) {
		if (preferredLayout == null) return null;

		if (type.equals(String.class))
			return preferredLayout.getName();

		return preferredLayout;
	}
}
