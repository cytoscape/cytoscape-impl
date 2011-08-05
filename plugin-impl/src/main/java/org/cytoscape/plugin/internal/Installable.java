/**
 * 
 */
package org.cytoscape.plugin.internal;




/**
 * @author skillcoy
 * 
 */
public interface Installable {

	public DownloadableInfo getInfoObj();
	
	public boolean install() throws java.io.IOException, org.cytoscape.plugin.internal.ManagerException;
	
	public boolean install(org.cytoscape.work.TaskMonitor taskMonitor) 
		throws java.io.IOException, org.cytoscape.plugin.internal.ManagerException;

	public boolean installToDir(java.io.File dir) throws java.io.IOException, org.cytoscape.plugin.internal.ManagerException;

	public boolean installToDir(java.io.File dir, org.cytoscape.work.TaskMonitor taskMonitor) 
		throws java.io.IOException, org.cytoscape.plugin.internal.ManagerException;
	
	public boolean uninstall() throws org.cytoscape.plugin.internal.ManagerException;
	
	public boolean update(org.cytoscape.plugin.internal.DownloadableInfo newObj) 
		throws java.io.IOException, org.cytoscape.plugin.internal.ManagerException;
	
	public boolean update(org.cytoscape.plugin.internal.DownloadableInfo newObj, org.cytoscape.work.TaskMonitor taskMonitor) 
		throws java.io.IOException, org.cytoscape.plugin.internal.ManagerException;

	public java.util.List<org.cytoscape.plugin.internal.DownloadableInfo> findUpdates()
		throws java.io.IOException, org.jdom.JDOMException;


}
