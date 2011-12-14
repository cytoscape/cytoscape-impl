/*
 File: PreferenceAction.java

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

//-------------------------------------------------------------------------
// $Revision: 7760 $
// $Date: 2006-06-26 09:28:49 -0700 (Mon, 26 Jun 2006) $
// $Author: mes $
//-------------------------------------------------------------------------
package org.cytoscape.app.internal.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.app.internal.DownloadableInfo;
import org.cytoscape.app.internal.ManagerException;
import org.cytoscape.app.internal.ManagerUtil;
import org.cytoscape.app.internal.AppInquireAction;
import org.cytoscape.app.internal.AppManager;
import org.cytoscape.app.internal.AppManagerInquireTask;
import org.cytoscape.app.internal.AppManagerInquireTaskFactory;
import org.cytoscape.app.internal.AppStatus;
import org.cytoscape.app.internal.ui.AppManageDialog;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;

/**
 *
 */
public class AppManagerAction extends AbstractCyAction {
	private final static long serialVersionUID = 12022346993206L;
	private CySwingApplication desktop;
	private BookmarksUtil bookmarksUtil;
	private Bookmarks theBookmarks;
	private DialogTaskManager guiTaskManagerServiceRef;
	private CyProperty cytoscapePropertiesServiceRef;
	private TaskFactory appLoaderTaskFactory;

	public static String cyConfigVerDir;
	public static String DefaultAppUrl = null;
	public static CyVersion cyVersion;
	
	/**
	 * Creates a new BookmarkAction object.
	 */
	public AppManagerAction(CySwingApplication desktop, CyVersion version,
			CyProperty<Bookmarks> bookmarksProp, BookmarksUtil bookmarksUtil, DialogTaskManager guiTaskManagerServiceRef
			, CyProperty<Properties> cytoscapePropertiesServiceRef, CyAppAdapter adapter, TaskFactory appLoaderTaskFactory,
			final CyApplicationConfiguration config) {
				
		super("App Manager");

		this.desktop = desktop;

		this.theBookmarks = bookmarksProp.getProperties();	
		this.bookmarksUtil = bookmarksUtil;
		this.guiTaskManagerServiceRef = guiTaskManagerServiceRef;

		this.appLoaderTaskFactory = 	appLoaderTaskFactory;

		// Note: We need pass cyConfigDir = ".cytoscape" and cyConfigVerDir to AppManager.java
		this.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
				
		DefaultAppUrl = cytoscapePropertiesServiceRef.getProperties().getProperty("defaultPluginDownloadUrl");
		
		// initialize version
		cyVersion = version;
		
		cyConfigVerDir = new File(config.getConfigurationDirectoryLocation(), File.separator + version.getMajorVersion()+ "." + version.getMinorVersion()).getAbsolutePath();
						
		setPreferredMenu("Apps");
		setMenuGravity(1.0f);
	
		//Initialize the AppManager
		AppManager Mgr = AppManager.getAppManager();
		Mgr.setTaskManager(this.guiTaskManagerServiceRef);
		Mgr.setCyAppAdapter(adapter);
		
		// Delete apps which are marked 'delete' in the track file
		try {
			Mgr.delete();			
		}
		catch (ManagerException me){
			me.printStackTrace();
		}
		
		//Load apps installed through pluiginManager
		List<DownloadableInfo> Current = AppManager.getAppManager().getDownloadables(AppStatus.CURRENT);
		for (int i=0; i< Current.size(); i++){
			try {
				Mgr.loadApp(Current.get(i));				
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		
		AppManageDialog dlg = new AppManageDialog(desktop.getJFrame(), theBookmarks, this.bookmarksUtil, 
				this.guiTaskManagerServiceRef, this.appLoaderTaskFactory);
		
		List<DownloadableInfo> Current = AppManager.getAppManager().getDownloadables(AppStatus.CURRENT);
		Map<String, List<DownloadableInfo>> InstalledInfo = ManagerUtil.sortByCategory(Current);				
		
		for (String Category : InstalledInfo.keySet()) {
			dlg.addCategory(Category, InstalledInfo.get(Category),AppManageDialog.AppInstallStatus.INSTALLED);
		}

		String DefaultTitle = "Cytoscape";
		
		Task task = new AppManagerInquireTask(this.DefaultAppUrl, new ManagerAction(dlg, DefaultTitle, this.DefaultAppUrl));

		AppManagerInquireTaskFactory _taskFactory = new AppManagerInquireTaskFactory(task);

		this.guiTaskManagerServiceRef.execute(_taskFactory);
	}

	private class ManagerAction extends AppInquireAction {
		  //private CyLogger logger = CyLogger.getLogger(ManagerAction.class);
		   private AppManageDialog dialog;
				private String title;
				private String url;

				public ManagerAction(AppManageDialog Dialog, String Title, String Url) {
					dialog = Dialog;
					title = Title;
					url = Url;
				}

				public String getProgressBarMessage() {
					return "Attempting to connect to " + url;
				}

				public void inquireAction(List<DownloadableInfo> Results) {
					AppManager Mgr = AppManager.getAppManager();
					if (isExceptionThrown()) {
						if (getIOException() != null) {
							// failed to read the given url
							//logger.error(getIOException().getMessage(), getIOException());
							dialog.setError(AppManageDialog.CommonError.NOXML.toString());
						} else if (getJDOMException() != null) {
							// failed to parse the xml file at the url
							//logger.error(getJDOMException().getMessage(), getJDOMException());
							dialog.setError(AppManageDialog.CommonError.BADXML.toString());
						} else {
							//logger.error(getException().getMessage(), getException());
							dialog.setError(getException().getMessage());
						}
					} else {
						List<DownloadableInfo> Unique = ManagerUtil.getUnique(Mgr.getDownloadables(AppStatus.CURRENT), Results);
						Map<String, List<DownloadableInfo>> AvailableInfo = ManagerUtil.sortByCategory(Unique);
						
						for (String Category : AvailableInfo.keySet()) {
							// get only the unique ones
							dialog.addCategory(Category, AvailableInfo.get(Category),
									AppManageDialog.AppInstallStatus.AVAILABLE);
						}
					}
					dialog.setSiteName(title);
					dialog.setVisible(true);
				}
			}
}
