package org.cytoscape.task.internal.quickstart;

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

import java.io.File;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.task.internal.quickstart.QuickStartState.Job;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class FileImportTableTask extends AbstractTask {
			
	@Tunable(description="Atrtribute file", params="fileCategory=table;input=true")
	public File file;
	
	private QuickStartState state;
	private final ImportTaskUtil util;
	private CyTableReader reader;
	private String[] previewKey;
	private String[][] previewData;
	
	public FileImportTableTask(QuickStartState state, ImportTaskUtil util, String[] previewKey, String[][]previewData) {
		this.state = state;
		this.util = util;
		this.previewKey = previewKey;
		this.previewData = previewData;
	}

	public void run(TaskMonitor taskMonitor) {
		
		taskMonitor.setStatusMessage("Finding Attribute Data Reader...");
		taskMonitor.setProgress(-1.0);
		reader = util.getTableReaderManager().getReader(file.toURI(),file.getName());

		if (reader == null)
			throw new NullPointerException("Failed to find reader for specified file!");
		else {
			insertTasksAfterCurrentTask(new GetAttributePreviewDataTask(state, reader, previewKey, previewData));
			
			taskMonitor.setStatusMessage("Importing Data Table...");
			insertTasksAfterCurrentTask(new SetTableNameTask(state, reader, file.getName()));
			insertTasksAfterCurrentTask(reader);
		}

		state.finished(Job.LOAD_TABLE);
	}
}
