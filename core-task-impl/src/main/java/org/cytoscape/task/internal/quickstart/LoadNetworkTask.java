/*
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.task.internal.quickstart;

import org.cytoscape.task.internal.quickstart.QuickStartState.Job;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import java.util.List;

public class LoadNetworkTask extends AbstractTask {

	private static final String FROM_FILE = "File";
	private static final String FROM_URL = "URL";
	private static final String FROM_SERVICE = "Public Database";

	@Tunable(description = "Select Data Source Type")
	public ListSingleSelection<String> dataSource = new ListSingleSelection<String>(
			FROM_FILE, FROM_URL, FROM_SERVICE);

	private QuickStartState state;
	private final ImportTaskUtil util;

	private  String[] previewKeys = {"key"};
	private  String[][] previewData = new String[20][1]; //{{"AAA"},{"BBB"},{"CCC"}};

	
	public LoadNetworkTask(QuickStartState state, ImportTaskUtil util) {
		this.state = state;
		this.util = util;
	}

	public void run(TaskMonitor monitor) {
		if (state.isJobFinished(Job.SELECT_MAPPING_ID_TYPE) == false) {
			// This is for next step: specify ID type
			insertTasksAfterCurrentTask(new SelectMappingKeyTypeTask(state,util, previewKeys, previewData));
			insertTasksAfterCurrentTask(new GetNetworkPreviewDataTask(util, previewData));
		} else if(state.isJobFinished(Job.LOAD_TABLE)) {
			insertTasksAfterCurrentTask(new MergeDataTask(state, util));
		}

		final String selected = dataSource.getSelectedValue();
		if (selected == FROM_FILE) {
			// Load file task
			insertTasksAfterCurrentTask(util.getFileImportTask());
		} else if (selected == FROM_URL) {
			// Load URL task
			insertTasksAfterCurrentTask(util.getURLImportTask());
		} else if (selected == FROM_SERVICE) {
			monitor.setStatusMessage("Checking & updating local data set.  Please wait...");
			monitor.setProgress(0.0);
			insertTasksAfterCurrentTask(util.getWebServiceImportTask());
		}

		state.finished(Job.LOAD_NETWORK);
	}
}
