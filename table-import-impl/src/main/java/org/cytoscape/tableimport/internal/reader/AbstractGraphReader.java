package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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


import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
//import cytoscape.task.TaskMonitor;
//import cytoscape.util.CyNetworkNaming;

/**
*
*/
public abstract class AbstractGraphReader implements GraphReader {
	protected String fileName;
	protected String title=null; // network title
	
	/**
	 * Creates a new AbstractGraphReader object.
	 *
	 * @param fileName  DOCUMENT ME!
	 */
	public AbstractGraphReader(String fileName) {
		this.fileName = fileName;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	public abstract void read() throws IOException;

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Long[] getNodeIndicesArray() {
		return null;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Long[] getEdgeIndicesArray() {
		return null;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getNetworkName() {

		String t = "";

		if (title != null){
			t = title;
		}
		else if (fileName != null) {
			File tempFile = new File(fileName);
			t = tempFile.getName();

			// Remove the file extension '.sif' or '.gml' or '.xgmml' as network title
			// 1. determine the extension of file
			String ext = "";			
			int dotIndex = t.lastIndexOf(".");			
			if (dotIndex != -1){
				ext = t.substring(dotIndex+1);
			}
			
			// 2. check if the file ext is one of the pre-defined exts
			//Set extSets = (Set) Cytoscape.getImportHandler().getAllExtensions();			
			//if (extSets.contains(ext)){
			//	// if the file ext is pre-defined, remove it from network title
			//	t = t.substring(0, dotIndex);
			//}
		}

		//return //CyNetworkNaming.getSuggestedNetworkTitle(t);
		return CytoscapeServices.cyNetworkNaming.getSuggestedNetworkTitle(t);
	}

	/**
	 * Executes post-processing:  no-op.
	*/
	//public void doPostProcessing(CyNetwork network) {
	//}

	/**
	 * Return the CyLayoutAlgorithm used to layout the graph
	 */
	//public CyLayoutAlgorithm getLayoutAlgorithm() {
	//	return null;//CyLayoutAlgorithmManager.getDefaultLayout();
	//}

	/**
	 * Set the task monitor to use for this reader
	 *
	 * @param monitor the TaskMonitor to use
	 */
	//public void setTaskMonitor(TaskMonitor monitor) {
	//}

}
