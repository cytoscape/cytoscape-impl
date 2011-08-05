package org.cytoscape.tableimport.internal.reader;

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
