/**
 * 
 */
package org.cytoscape.app.internal;




/**
 * @author skillcoy
 * 
 */
public interface Installable {

	public DownloadableInfo getInfoObj();
	
	public boolean install() throws java.io.IOException, org.cytoscape.app.internal.ManagerException;
	
	public boolean install(org.cytoscape.work.TaskMonitor taskMonitor) 
		throws java.io.IOException, org.cytoscape.app.internal.ManagerException;

	public boolean installToDir(java.io.File dir) throws java.io.IOException, org.cytoscape.app.internal.ManagerException;

	public boolean installToDir(java.io.File dir, org.cytoscape.work.TaskMonitor taskMonitor) 
		throws java.io.IOException, org.cytoscape.app.internal.ManagerException;
	
	public boolean uninstall() throws org.cytoscape.app.internal.ManagerException;
	
	public boolean update(org.cytoscape.app.internal.DownloadableInfo newObj) 
		throws java.io.IOException, org.cytoscape.app.internal.ManagerException;
	
	public boolean update(org.cytoscape.app.internal.DownloadableInfo newObj, org.cytoscape.work.TaskMonitor taskMonitor) 
		throws java.io.IOException, org.cytoscape.app.internal.ManagerException;

	public java.util.List<org.cytoscape.app.internal.DownloadableInfo> findUpdates()
		throws java.io.IOException, org.jdom.JDOMException;


}
