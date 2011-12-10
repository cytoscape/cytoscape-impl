/*
 File: AppManageDialog.java 
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.app.internal.ui;

import org.cytoscape.app.internal.Category;
import org.cytoscape.app.internal.DownloadableInfo;
import org.cytoscape.app.internal.Installable;
import org.cytoscape.app.internal.ManagerException;
import org.cytoscape.app.internal.ManagerUtil;
import org.cytoscape.app.internal.AppException;
import org.cytoscape.app.internal.AppInfo;
import org.cytoscape.app.internal.AppInquireAction;
import org.cytoscape.app.internal.AppManager;
import org.cytoscape.app.internal.AppManagerInquireTask;
import org.cytoscape.app.internal.AppManagerInquireTaskFactory;
import org.cytoscape.app.internal.AppStatus;
import org.cytoscape.app.internal.ThemeInfo;
import org.cytoscape.app.internal.action.AppManagerAction;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.bookmark.DataSource;

//import cytoscape.dialogs.preferences.BookmarkDialog;
//import cytoscape.dialogs.preferences.EditBookmarkDialog;

//import org.cytoscape.app.internal.AppTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.work.TaskMonitor;
//import cytoscape.util.OpenBrowser;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
//import org.cytoscape.work.TaskManager;
//import org.cytoscape.work.TaskMonitor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.TaskFactory;
import java.io.File;

public class AppManageDialog extends javax.swing.JDialog implements
		TreeSelectionListener, ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AppManageDialog.class);

	public static String CURRENTLY_INSTALLED = "Currently Installed";
	public static String AVAILABLE_FOR_INSTALL = "Available for Install";
	
	private String defaultAppSiteURL = AppManagerAction.DefaultAppUrl;
	private String currentAppSiteURL = defaultAppSiteURL;
	
	private String HOWTOSEARCH = "You can use wildcard * or ? in your search words";
	private JFrame desktop;
	
	private String bookmarkCategory = "apps";
	private Bookmarks theBookmarks;
	private BookmarksUtil bookmarksUtil;
	private TaskFactory appLoaderTaskFactory;
	
	//private final TaskManager taskManager;
	private DialogTaskManager guiTaskManagerServiceRef;
	
	public enum AppInstallStatus {
		INSTALLED(CURRENTLY_INSTALLED), AVAILABLE(AVAILABLE_FOR_INSTALL);
		private String typeText;

		private AppInstallStatus(String type) {
			typeText = type;
		}

		public String toString() {
			return typeText;
		}
	}

	public enum CommonError {
		NOXML("ERROR: Failed to read XML file "), BADXML(
				"ERROR: XML file may be incorrectly formatted, unable to read ");

		private String errorText;

		private CommonError(String error) {
			errorText = error;
		}

		public String toString() {
			return errorText;
		}
	}

	private String baseSiteLabel = "Apps available for download from: ";


	// This constructor is for test only
	private AppManageDialog() {
		this.setTitle("Manage Apps");
		initComponents();
		initTree();
		this.setSize(600, 500);
	}

	/*
	public AppManageDialog(javax.swing.JDialog owner) {
		super(owner, "Manage Apps");
		setLocationRelativeTo(owner);
		initComponents();
		initTree();
		this.setSize(600, 500);
	}
*/
	public AppManageDialog(JFrame owner, Bookmarks bookmarks, BookmarksUtil bookmarksUtil,
			DialogTaskManager guiTaskManagerServiceRef, TaskFactory appLoaderTaskFactory) {

		super(owner, "Manage Apps");
		this.desktop = owner;
		
		this.theBookmarks = bookmarks;
		this.bookmarksUtil = bookmarksUtil;
		this.guiTaskManagerServiceRef = guiTaskManagerServiceRef;
		this.appLoaderTaskFactory = 	appLoaderTaskFactory;

		setLocationRelativeTo(owner);
		initComponents();
		initTree();
		this.setSize(600, 500);
		this.btnSearch.setEnabled(false);
		this.availableAppsLabel.setVisible(false);
		this.downloadLocText.setVisible(false);
		//this.sitePanel2.setVisible(false);
		//this.changeSiteButton.setVisible(false);
		this.btnClear.setEnabled(false);
		
		bookmarksSetUp();
		
		this.lstDownloadSites.setCellRenderer(new BookmarkCellRenderer());
		
		this.lstDownloadSites.addListSelectionListener(new MyListSelectionListener());
		this.lstDownloadSites.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		
		loadBookmarkCMBox(false);
		//Select the Cytoscape download site by default
		//this.lstDownloadSites.setSelectedIndex(0);
		
		//this.lbSiteURL.setText(((DataSource)this.cmbDownloadSites.getSelectedItem()).getHref());
		this.jTabbedPane1.setSelectedIndex(0);
		
		this.jTabbedPane1.addChangeListener(new MyChangeListener());
		this.tfSearch.setToolTipText(HOWTOSEARCH);
		
		// by default, show all the apps
		this.versionCheck.setSelected(false);
		
		this.btnEditSite.setEnabled(false);
		this.btnDeleteSite.setEnabled(false);
	}
	
	
	class MyChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e){
			int width = AppManageDialog.this.jTabbedPane1.getSize().width;
			
			if (AppManageDialog.this.jTabbedPane1.getSelectedIndex() == 0){
				
				AppManageDialog.this.jTabbedPane1.setMinimumSize(new Dimension(width, 50));
				AppManageDialog.this.jTabbedPane1.setPreferredSize(new Dimension(width, 50));
				AppManageDialog.this.pnlSearch.setPreferredSize(new Dimension(width, 50));
				AppManageDialog.this.pnlSettings.setPreferredSize(new Dimension(width, 50));
			}
			else {
				AppManageDialog.this.jTabbedPane1.setMinimumSize(new Dimension(width, 150));
				AppManageDialog.this.jTabbedPane1.setPreferredSize(new Dimension(width, 150));
				AppManageDialog.this.pnlSearch.setPreferredSize(new Dimension(width, 150));
				AppManageDialog.this.pnlSettings.setPreferredSize(new Dimension(width, 150));
				
			}
			AppManageDialog.this.pack();
		}
	}
	
	// Refresh the app-tree after the change of App site URL
	private void refreshAppTree(){
		switchDownloadSites();
				
		Task task = new AppManagerInquireTask
		(this.currentAppSiteURL, new UrlAction(this, this.currentAppSiteURL));
		
		AppManagerInquireTaskFactory _taskFactory = new AppManagerInquireTaskFactory(task);

		this.guiTaskManagerServiceRef.execute(_taskFactory);
	}
	
	/*
	private class AppManagerInquireTaskFactory implements TaskFactory {
		Task task;
		public AppManagerInquireTaskFactory(Task task){
			this.task = task;
		}
		
		public TaskIterator createTaskIterator() {
			return new TaskIterator(task);
		}
	}
	*/
	private class UrlAction extends AppInquireAction {

		private AppManageDialog dialog;
		private String url;

		public UrlAction(AppManageDialog Dialog, String Url) {
			dialog = Dialog;
			url = Url;
		}

		public boolean displayProgressBar() {
			return true;
		}

		public String getProgressBarMessage() {
			return "Attempting to connect...";
		}

		public void inquireAction(List<DownloadableInfo> Results) {

			if (isExceptionThrown()) {
				if (getIOException() != null) {
					// failed to read the given url
					logger.warn(AppManageDialog.CommonError.NOXML + url, getIOException());
					dialog.setError(AppManageDialog.CommonError.NOXML + url);
				} else if (getJDOMException() != null) {
					// failed to parse the xml file at the url
					logger.warn(AppManageDialog.CommonError.BADXML + url, getJDOMException());
					dialog.setError(AppManageDialog.CommonError.BADXML + url);
				}
			} else {

				System.out.println("AppManagerDialog.inquireAction().....");
				
				AppManager Mgr = AppManager.getAppManager();
				List<DownloadableInfo> UniqueAvailable = ManagerUtil.getUnique(Mgr
						.getDownloadables(AppStatus.CURRENT), Results);

				Map<String, List<DownloadableInfo>> NewApps = ManagerUtil
						.sortByCategory(UniqueAvailable);

				if (NewApps.size() <= 0) {
					dialog.setError("No apps compatible with "
							+ AppManagerAction.cyVersion.getVersion()
							+ " available from this site.");
				} else {
					dialog.setMessage("");
				}

				for (String Category : NewApps.keySet()) {
					dialog.addCategory(Category, NewApps.get(Category),
							AppManageDialog.AppInstallStatus.AVAILABLE);
				}
			}

		}

	}

	
	// trying to listen to events in the Url dialog
	public void actionPerformed(ActionEvent evt) {
		logger.info("URL DIALOG: " + evt.getSource().toString());
	}

	/**
	 * Enables the delete/install buttons when the correct leaf node is selected
	 */
	public void valueChanged(TreeSelectionEvent e) {
		TreeNode Node = (TreeNode) appTree.getLastSelectedPathComponent();
		if (Node == null) {
			return;
		}

		if (Node.isLeaf()) {
			// display any object selected
			infoTextPane.setContentType("text/html");
			infoTextPane.setText(((DownloadableInfo) Node.getObject()).htmlOutput());
			infoTextPane.setCaretPosition(0);
			infoTextPane.setEditable(false);
			infoTextPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
						public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
							if (evt.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
								// call open browser on the link
								//OpenBrowser.openURL(evt.getURL().toString());
								}

							}
						});
			if (Node.getParent().getParent().getTitle().equalsIgnoreCase(CURRENTLY_INSTALLED)) {
				installDeleteButton.setText("Delete");
				if (AppManager.usingWebstartManager()) {
					installDeleteButton.setEnabled(false);
					setMessage("Delete is unavailable when using Web Start");
				} else {
					installDeleteButton.setEnabled(true);
				}
			}	else if (Node.getParent().getParent().getTitle().equalsIgnoreCase(AVAILABLE_FOR_INSTALL)) {

				installDeleteButton.setText("Install");
				installDeleteButton.setEnabled(true);
			}
		} else {
			installDeleteButton.setEnabled(false);
		}
	}

	/**
	 * Sets a message to be shown to the user regarding the app management
	 * actions.
	 * 
	 * @param Msg
	 */
	public void setMessage(String Msg) {
		msgPanel.setForeground(java.awt.Color.BLACK);
		msgPanel.setText(Msg);
	}

	public void setError(String Msg) {
		msgPanel.setForeground(new java.awt.Color(204, 0, 51));
		msgPanel.setText(Msg);
	}

	/**
	 * Set the name of the site the available apps are from.
	 * 
	 * @param SiteName
	 */
	public void setSiteName(String SiteName) {
		downloadLocText.setText(SiteName);
	}

	/**
	 * Call this when changing download sites to clear out the old available
	 * list in order to create a new one.
	 */
	public void switchDownloadSites() {
		hiddenNodes.clear();
		java.util.Vector<TreeNode> AvailableNodes = new java.util.Vector<TreeNode>(
				availableNode.getChildren());
		for (TreeNode child : AvailableNodes) {
			treeModel.removeNodeFromParent(child);
		}
	}

	/**
	 * Adds a category and it's list of apps to the appropriate tree (based
	 * on Status) in the dialog.
	 * 
	 * @param CategoryName
	 *            String category for this list of apps
	 * @param Apps
	 *            List of DownloadableInfo objects to be shown in the given
	 *            category
	 * @param Status
	 *            AppInstallStatus (currently installed or available for
	 *            install)
	 */
	public void addCategory(String CategoryName,
			List<DownloadableInfo> Apps, AppInstallStatus Status) {
		switch (Status) {
		case INSTALLED:
			addCategory(CategoryName, Apps, installedNode);
			break;

		case AVAILABLE:
			addCategory(CategoryName, Apps, availableNode);
			if (treeModel.getIndexOfChild(rootTreeNode, availableNode) < 0) {
				treeModel.addNodeToParent(rootTreeNode, availableNode);
			}
			break;
		}

		javax.swing.ToolTipManager.sharedInstance().registerComponent(
				appTree);

		//javax.swing.ImageIcon warningIcon = createImageIcon(
		//		"/resources/images/alert-red2.gif", "Warning");
		//javax.swing.ImageIcon okIcon = createImageIcon(
		//		"/resources/imagescheck-mark.gif", "Ok");
	
		
		javax.swing.ImageIcon warningIcon = new ImageIcon(getClass().getResource("/images/alert-red2.gif"));
		warningIcon.setDescription("Warning");
		javax.swing.ImageIcon okIcon = new ImageIcon(getClass().getResource("/images/check-mark.gif"));
		okIcon.setDescription("OK");

		if (warningIcon != null) {
			treeRenderer = new TreeCellRenderer(warningIcon, okIcon);
			appTree.setCellRenderer(treeRenderer);
		}
		
		appTree.expandPath( new TreePath(availableNode.getPath()) );
		appTree.expandPath( new TreePath(installedNode.getPath()) );
	}

	// add category to the set of apps under given node
	private void addCategory(String CategoryName,
			List<DownloadableInfo> Apps, TreeNode node) {
		TreeNode Category = new TreeNode(CategoryName, true);

		for (DownloadableInfo CurrentApp : Apps) {
			TreeNode AppNode = new TreeNode(CurrentApp);

			if (node.equals(availableNode)
					&& !CurrentApp.isAppCompatibleWithCurrent()) {
				java.util.List<TreeNode> hiddenCat = hiddenNodes.get(Category);
				if (hiddenCat == null)
					hiddenCat = new java.util.ArrayList<TreeNode>();

				hiddenCat.add(AppNode);
				hiddenNodes.put(Category, hiddenCat);
				if (!versionCheck.isSelected())
					treeModel.addNodeToParent(Category, AppNode);
			} else {
				treeModel.addNodeToParent(Category, AppNode);
			}
		}
		if (Category.getChildCount() > 0)
			treeModel.addNodeToParent(node, Category);
	}


	// allow for outdated versions
	private void versionCheckItemStateChanged(java.awt.event.ItemEvent evt) {
		TreePath[] SelectedPaths = appTree.getSelectionPaths();
		if (evt.getStateChange() == ItemEvent.DESELECTED) {
			appTree.collapsePath( new TreePath(availableNode.getPath()) );
			availableNode.removeChildren();
			for (TreeNode Category : hiddenNodes.keySet()) {
				for (TreeNode App : hiddenNodes.get(Category)) {
					treeModel.addNodeToParent(Category, App);
				}
				treeModel.addNodeToParent(availableNode, Category);
			}
		} else if (evt.getStateChange() == ItemEvent.SELECTED) {
			
			for (TreeNode Category : hiddenNodes.keySet()) {
				for (TreeNode App: hiddenNodes.get(Category)) {
					hiddenNodes.get(Category);
					treeModel.removeNodesFromParent(hiddenNodes.get(Category));
					if (Category.getChildCount() <= 0) {
						availableNode.removeChild(Category);
						treeModel.reload();
					}
				}
			}
		}
		if (SelectedPaths != null) {
			for (TreePath Path: SelectedPaths) { 
				appTree.expandPath(Path);
				appTree.setSelectionPath(Path);
			}
		} else {
			appTree.expandPath( new TreePath(availableNode.getPath()) );
			appTree.expandPath( new TreePath(installedNode.getPath()) );
		}
	}

	private void installDeleteButtonActionPerformed(ActionEvent evt) {
		if (installDeleteButton.getText().equals("Delete")) {
			deleteButtonActionPerformed(evt);
		} else if (installDeleteButton.getText().equals("Install")) {
			installButtonActionPerformed(evt);
		}
	}

	// delete event
	private void deleteButtonActionPerformed(ActionEvent evt) {
		
		TreeNode Node = (TreeNode) appTree.getLastSelectedPathComponent();

		if (Node == null) {
			return;
		}
		DownloadableInfo NodeInfo = Node.getObject();
		String ChangeMsg = "Changes will not take effect until you have restarted Cytoscape.";
		String VerifyMsg = "";
		if (NodeInfo.getCategory().equalsIgnoreCase("core")) {
			VerifyMsg = "This is a 'core' app and other apps may depend on it, "
					+ "are you sure you want to delete it?\n" + ChangeMsg;
		} else {
			VerifyMsg = "Are you sure you want to delete the app '"
					+ NodeInfo.getName() + "'?\n" + ChangeMsg;
		}
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
				VerifyMsg, "Verify Delete App", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE)) {
			try {
				AppManager.getAppManager().delete(NodeInfo);
				treeModel.removeNodeFromParent(Node);
				setMessage(NodeInfo.getName()
						+ " will be removed when you restart Cytoscape.");
			} catch (org.cytoscape.app.internal.WebstartException we) {
				logger.warn("Unable to remove '"+NodeInfo.getName()+"': "+we.getMessage(), we);
			}
		}
		
	}

	// install new downloadable obj
	private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {
		final TreeNode node = (TreeNode) appTree
				.getLastSelectedPathComponent();

		if (node == null) { // error
			return;
		}
		Object nodeInfo = node.getObject();
		if (node.isLeaf()) {
			boolean licenseRequired = false;
			final LicenseDialog License = new LicenseDialog(this);
			final DownloadableInfo infoObj = (DownloadableInfo) nodeInfo;

			switch (infoObj.getType()) {
			case APP:
				if (infoObj.getLicenseText() != null) {
					License.addApp(infoObj);
					licenseRequired = true;
				}
				break;
			case THEME:
				ThemeInfo themeInfo = (ThemeInfo) infoObj;
				for (AppInfo pInfo : themeInfo.getApps()) {
					if (pInfo.getLicenseText() != null) {
						License.addApp(pInfo);
						licenseRequired = true;
					}
				}
				break;
			case FILE: // currently nothing, there are no FileInfo objects
				// right now
				break;
			}

			if (licenseRequired) {
				License.addListenerToOk(new ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						License.dispose();
						createInstallTask(infoObj, node);
					}
				});
				License.selectDefault();
				License.setVisible(true);
			} else {
				createInstallTask(infoObj, node);
			}
		}
	}

	private void updateCurrent(DownloadableInfo info) {
		boolean categoryMatched = false;

		for (TreeNode Child : installedNode.getChildren()) {
			if (Child.getTitle().equals(info.getCategory())) {
				Child.addChild(new TreeNode(info));
				categoryMatched = true;
			}
		}

		if (!categoryMatched) {
			List<DownloadableInfo> NewApp = new java.util.ArrayList<DownloadableInfo>();
			NewApp.add(info);
			addCategory(info.getCategory(), NewApp,
					AppInstallStatus.INSTALLED);
		}
	}

	// close button
	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	// initialize the JTree and base nodes
	private void initTree() {
		appTree.setRootVisible(false);
		appTree.addTreeSelectionListener(this);

		appTree.getSelectionModel().setSelectionMode(
				javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);

		rootTreeNode = new TreeNode("Apps", true);
		installedNode = new TreeNode(AppInstallStatus.INSTALLED.toString(),
				true);
		availableNode = new TreeNode(AppInstallStatus.AVAILABLE.toString(),
				true);

		treeModel = new ManagerModel(rootTreeNode);
		treeModel.addNodeToParent(rootTreeNode, installedNode);
		treeModel.addNodeToParent(rootTreeNode, availableNode);

		appTree.setModel(treeModel);
		
		hiddenNodes = new java.util.HashMap<TreeNode, java.util.List<TreeNode>>();
	}

	
	private Vector getAllAppVector(){
		
		Vector<Vector> allAppVect = new Vector<Vector>();

		TreeNode root = (TreeNode) this.treeModel.getRoot();
		for (int i=0; i< root.getChildCount(); i++){
			TreeNode n = root.getChildAt(i);
			Vector<TreeNode> categories = n.getChildren();
			for (int j=0; j<categories.size(); j++){
				TreeNode category = categories.elementAt(j);
				for (int k=0; k<category.getChildCount(); k++){
					TreeNode leaf = category.getChildAt(k);
					Vector aAppVect = new Vector();
					aAppVect.add(n.getTitle());					
					aAppVect.add(category.getTitle());
					aAppVect.add(leaf.getObject());
					allAppVect.add(aAppVect);	
				}			
			}			
		}
		
		return allAppVect;
	}
	
	
    // Update tree model based on the search result 
    private void updateTreeModel(Vector filteredAppVector) {

    	TreeNode newRootTreeNode = new TreeNode("Apps", true);
    	ManagerModel newTreeModel = new ManagerModel(newRootTreeNode);

    	TreeNode newInstalledNode = new TreeNode(AppInstallStatus.INSTALLED.toString());
    	TreeNode newAvailableNode = new TreeNode(AppInstallStatus.AVAILABLE.toString());
		//
    	newTreeModel.addNodeToParent(newRootTreeNode, newInstalledNode);
    	newTreeModel.addNodeToParent(newRootTreeNode, newAvailableNode);
		
    	if (filteredAppVector != null){
    		// Add the filtered apps to the new Tree Model
    		for (int i=0; i < filteredAppVector.size(); i++ ){
    			
    			Vector aApp = (Vector) filteredAppVector.elementAt(i);

    			if (aApp.elementAt(0).toString().equalsIgnoreCase(CURRENTLY_INSTALLED)){
    				//add to branch of newInstalledNode
    				String category = aApp.elementAt(1).toString();
    				TreeNode leafNode = new TreeNode((DownloadableInfo) aApp.elementAt(2));
    				// get the category TreeNode
    				TreeNode categoryNode = null;
    				for (int j=0; j<newInstalledNode.getChildCount();j++ ){
    					TreeNode node = newInstalledNode.getChildAt(j);
    					if (node.getTitle().equalsIgnoreCase(category)){
    						categoryNode = node;						
    						newTreeModel.addNodeToParent(categoryNode, leafNode);
    						break;
    					}
    				}
    				if (categoryNode == null){
    					categoryNode = new TreeNode(category, true);
    					newTreeModel.addNodeToParent(categoryNode, leafNode);
    					newTreeModel.addNodeToParent(newInstalledNode,categoryNode);
    				}
    			}
    			else if (aApp.elementAt(0).toString().equalsIgnoreCase(AVAILABLE_FOR_INSTALL)){
    				//add to the branch of newAvailableNode
    				String category = aApp.elementAt(1).toString();
    				TreeNode leafNode = new TreeNode((DownloadableInfo) aApp.elementAt(2));
    				// get the category TreeNode
    				TreeNode categoryNode = null;
    				for (int j=0; j<newAvailableNode.getChildCount();j++ ){
    					TreeNode node = newAvailableNode.getChildAt(j);
    					if (node.getTitle().equalsIgnoreCase(category)){
    						categoryNode = node;						
    						newTreeModel.addNodeToParent(categoryNode, leafNode);
    						break;
    					}
    				}
    				if (categoryNode == null){
    					categoryNode = new TreeNode(category, true);
    					newTreeModel.addNodeToParent(categoryNode, leafNode);
    					newTreeModel.addNodeToParent(newAvailableNode,categoryNode);
    				}
    			}
    		}
    	}
	
		appTree.setModel(newTreeModel);
		
		appTree.expandPath( new TreePath(newAvailableNode.getPath()) );
		appTree.expandPath( new TreePath(newInstalledNode.getPath()) );
    }

    
    private void switchToMainDialog() {
    	// switch to the main treeModel
		appTree.setModel(treeModel);
		TreeNode root = (TreeNode) treeModel.getRoot();

		// Expand the tree to level 1
		Vector<TreeNode> treeNodeVect = root.getChildren();
		for (int i=0; i< treeNodeVect.size(); i++){
			TreeNode n = treeNodeVect.elementAt(i);
			appTree.expandPath(new TreePath(treeModel.getPathToRoot(n)));
		}
		
		// switch the sitePanel
    	this.sitePanel.setVisible(true);
    	//this.sitePanel2.setVisible(false);    	
    }

	
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">   
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane2 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        pnlSearch = new javax.swing.JPanel();
        topPane = new javax.swing.JPanel();
        sitePanel = new javax.swing.JPanel();
        tfSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        downloadLocText = new javax.swing.JLabel();
        lbSearchTitle = new javax.swing.JLabel();
        pnlSettings = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnAddSite = new javax.swing.JButton();
        btnEditSite = new javax.swing.JButton();
        btnDeleteSite = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDownloadSites = new javax.swing.JList();
        pnlImportLocalApp = new javax.swing.JPanel();
        lbSelectLocalFile = new javax.swing.JLabel();
        btnSelectLocalApp = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        availableAppsLabel = new javax.swing.JLabel();
        versionCheck = new javax.swing.JCheckBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        treeScrollPane = new javax.swing.JScrollPane();
        appTree = new javax.swing.JTree();
        infoScrollPane = new javax.swing.JScrollPane();
        infoTextPane = new javax.swing.JEditorPane();
        msgLabel = new javax.swing.JLabel();
        msgPanel = new javax.swing.JTextArea();
        bottomPane = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        installDeleteButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        pnlSearch.setLayout(new java.awt.GridBagLayout());

        topPane.setLayout(new java.awt.GridBagLayout());

        sitePanel.setLayout(new java.awt.GridBagLayout());

        tfSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSearchActionPerformed(evt);
            }
        });
        tfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfSearchKeyTyped(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        sitePanel.add(tfSearch, gridBagConstraints);

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        sitePanel.add(btnSearch, gridBagConstraints);

        btnClear.setText("Clear");
        btnClear.setPreferredSize(new java.awt.Dimension(67, 23));
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        sitePanel.add(btnClear, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        topPane.add(sitePanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        pnlSearch.add(topPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        pnlSearch.add(downloadLocText, gridBagConstraints);

        lbSearchTitle.setText("Enter key words to search");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        pnlSearch.add(lbSearchTitle, gridBagConstraints);

        jTabbedPane1.addTab("Search", pnlSearch);

        pnlSettings.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Download sites");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 0);
        pnlSettings.add(jLabel3, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        btnAddSite.setText("Add");
        btnAddSite.setPreferredSize(new java.awt.Dimension(65, 23));
        btnAddSite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSiteActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 20);
        jPanel2.add(btnAddSite, gridBagConstraints);

        btnEditSite.setText("Edit");
        btnEditSite.setPreferredSize(new java.awt.Dimension(65, 23));
        btnEditSite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditSiteActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 20);
        jPanel2.add(btnEditSite, gridBagConstraints);

        btnDeleteSite.setText("Delete");
        btnDeleteSite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteSiteActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 20);
        jPanel2.add(btnDeleteSite, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlSettings.add(jPanel2, gridBagConstraints);

        lstDownloadSites.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lstDownloadSites);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        pnlSettings.add(jScrollPane1, gridBagConstraints);

        jTabbedPane1.addTab("Settings", pnlSettings);

        pnlImportLocalApp.setLayout(new java.awt.GridBagLayout());

        lbSelectLocalFile.setText("Please selct a app file to load:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 0);
        pnlImportLocalApp.add(lbSelectLocalFile, gridBagConstraints);

        btnSelectLocalApp.setText("Select");
        btnSelectLocalApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectLocalAppActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 0);
        pnlImportLocalApp.add(btnSelectLocalApp, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pnlImportLocalApp.add(jLabel1, gridBagConstraints);

        jTabbedPane1.addTab("Import Local App", pnlImportLocalApp);

        jSplitPane2.setLeftComponent(jTabbedPane1);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        availableAppsLabel.setText("Apps available for download from:");
        availableAppsLabel.setEnabled(false);
        availableAppsLabel.setFocusable(false);
        availableAppsLabel.setInheritsPopupMenu(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 0);
        jPanel3.add(availableAppsLabel, gridBagConstraints);

        versionCheck.setText("Show only apps verified to work in this release");
        versionCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        versionCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
        versionCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                versionCheckItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 0);
        jPanel3.add(versionCheck, gridBagConstraints);

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(400, 326));
        treeScrollPane.setViewportView(appTree);

        jSplitPane1.setLeftComponent(treeScrollPane);

        infoScrollPane.setViewportView(infoTextPane);

        jSplitPane1.setRightComponent(infoScrollPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel3.add(jSplitPane1, gridBagConstraints);

        msgLabel.setText("Messages:");
        msgLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 0);
        jPanel3.add(msgLabel, gridBagConstraints);

        msgPanel.setBackground(new java.awt.Color(230, 230, 230));
        msgPanel.setColumns(20);
        msgPanel.setEditable(false);
        msgPanel.setLineWrap(true);
        msgPanel.setRows(5);
        msgPanel.setWrapStyleWord(true);
        msgPanel.setMinimumSize(new java.awt.Dimension(30, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 20);
        jPanel3.add(msgPanel, gridBagConstraints);

        bottomPane.setLayout(new java.awt.GridBagLayout());

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        installDeleteButton.setText("Install");
        installDeleteButton.setEnabled(false);
        installDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installDeleteButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 10);
        buttonPanel.add(installDeleteButton, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(closeButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        bottomPane.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        jPanel3.add(bottomPane, gridBagConstraints);

        jSplitPane2.setRightComponent(jPanel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSplitPane2, gridBagConstraints);

        pack();
    }// </editor-fold>                        

                                                      
    private void btnSelectLocalAppActionPerformed(java.awt.event.ActionEvent evt) {
        this.guiTaskManagerServiceRef.execute(this.appLoaderTaskFactory);
    }                                                    

    private void btnDeleteSiteActionPerformed(java.awt.event.ActionEvent evt) {                                              
    	/*
    	DataSource theDataSource = (DataSource)this.lstDownloadSites.getSelectedValue();
		
    	if (theDataSource.getName().equalsIgnoreCase("Cytoscape")){
    		JOptionPane.showMessageDialog(this, "Your can not delete default Cytoscape site", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
		int confirm = JOptionPane.showConfirmDialog(this,"Are you sure you want to " +
				"delete this site","Delete Confirm",JOptionPane.OK_CANCEL_OPTION);
			
		if (confirm == JOptionPane.OK_OPTION){
			BookmarksUtil.deleteBookmark(theBookmarks, bookmarkCategory, theDataSource);
			loadBookmarkCMBox(true); // reload is required to update the GUI			
		}
		*/
    }                                             

    private void btnEditSiteActionPerformed(java.awt.event.ActionEvent evt) {    
    	/*
    	DataSource theDataSource = (DataSource) this.lstDownloadSites.getSelectedValue();
    	EditBookmarkDialog theEditDialog = new EditBookmarkDialog(this, true, theBookmarks,
    			bookmarkCategory, "edit", theDataSource);
    	theEditDialog.setLocationRelativeTo(this);

    	theEditDialog.setVisible(true);
    	loadBookmarkCMBox(true);
    	*/
    }                                           

    private void btnAddSiteActionPerformed(java.awt.event.ActionEvent evt) {      
    	/*
    	EditBookmarkDialog theNewDialog = new EditBookmarkDialog(this, true, theBookmarks,
    			bookmarkCategory, "new", null);
    	theNewDialog.setLocationRelativeTo(this);
    	theNewDialog.setVisible(true);
    	theNewDialog.getDataSource();
    	loadBookmarkCMBox(true); // reload is required to update the GUI
    	*/
    }                                          


    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {                                         
    	this.switchToMainDialog();
    	this.tfSearch.setText("");
    	this.btnSearch.setEnabled(false);
    	this.versionCheck.setEnabled(true);
    }                                        

    private void btnBackMainActionPerformed(java.awt.event.ActionEvent evt) {                                            
    	// TODO add your handling code here:
    }                                           

    	    
    // Disable btnSearch if there is no text in the Search textfield
    private void tfSearchKeyTyped(java.awt.event.KeyEvent evt) {
    	if (this.tfSearch.getText().trim().equals("")){
    		this.btnSearch.setEnabled(false);
    		this.switchToMainDialog();
    		this.btnClear.setEnabled(false);
    		this.versionCheck.setEnabled(true);
    	}
    	else {
    		this.btnSearch.setEnabled(true);
    		this.btnClear.setEnabled(true);
    		this.versionCheck.setEnabled(false);
    	}
    }

    private void tfSearchActionPerformed(java.awt.event.ActionEvent evt) {
    	// Perform the same effect as the click of btnSearch 
    	//System.out.println("Entered is pressed in search textField");
    	btnSearchActionPerformed(null);
    }

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {
    
    	try {
     		Vector  filteredAppVector = AppIndex.getSearchResult(this.tfSearch.getText().trim(),!this.versionCheck.isSelected(), downloadLocText.getText());
    		
     		if (filteredAppVector == null){
    			// The index does not exist, build it now
    			
    			//This will create new index for the AllAppVector
    	    	AppIndex.setAllAppVector(this.getAllAppVector(), !this.versionCheck.isSelected(), downloadLocText.getText());
    	    	// After the index is created, we can do searching against the index
    			filteredAppVector = AppIndex.getSearchResult(this.tfSearch.getText().trim(),!this.versionCheck.isSelected(), downloadLocText.getText());
    		}
       	
        	updateTreeModel(filteredAppVector);
        	
        	//this.sitePanel.setVisible(false);
        	//this.sitePanel2.setVisible(true);    		
    	}
    	catch (Exception e){
    		e.printStackTrace();
    		JOptionPane.showMessageDialog(this, "Error in build index for AppManager!");
    	}
    }
	
    
	/*
	 * --- create the tasks and task monitors to show the user what's going on
	 * during download/install ---
	 */

	private void createInstallTask(DownloadableInfo obj, TreeNode node) {
		// Create Task
		InstallTask task = new InstallTask(obj, node);

		AppManagerInstallTaskFactory _taskFactory = new AppManagerInstallTaskFactory(task);
		this.guiTaskManagerServiceRef.execute(_taskFactory);

		DownloadableInfo info = task.getDownloadedApp();
		if (info != null) {
			updateCurrent(info);
			cleanTree(node);
		} else {
			// TODO somehow disable the node??
		}
	}

	
	private class AppManagerInstallTaskFactory implements TaskFactory {
		Task task;
		public AppManagerInstallTaskFactory(Task task){
			this.task = task;
		}
		
		public TaskIterator createTaskIterator() {
			return new TaskIterator(task);
		}
	}
	
	private void cleanTree(TreeNode node) {
		DownloadableInfo info = (DownloadableInfo) node.getObject();
		List<TreeNode> RemovableNodes = new java.util.ArrayList<TreeNode>();

		for (int i = 0; i < node.getParent().getChildCount(); i++) {
			TreeNode Child = (TreeNode) node.getParent().getChildAt(i);
			DownloadableInfo childInfo = (DownloadableInfo) Child.getObject();

			if (childInfo.getID().equals(info.getID())
					&& childInfo.getName().equals(info.getName())) {
				RemovableNodes.add(Child);
			}
		}

		for (TreeNode treeNode : RemovableNodes) {
			treeModel.removeNodeFromParent(treeNode);
		}
	}


	/** Returns an ImageIcon, or null if the path was invalid. */
	private javax.swing.ImageIcon createImageIcon(String path,
			String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new javax.swing.ImageIcon(imgURL, description);
		} else {
			logger.warn("Couldn't find file: " + path);
			return null;
		}
	}

	private class InstallTask implements Task {

		private DownloadableInfo infoObj;

		private TreeNode node;

		public InstallTask(DownloadableInfo Info, TreeNode Node)
				throws java.lang.IllegalArgumentException {
			String ErrorMsg = null;
			if (Info == null) {
				ErrorMsg = "DownloadableInfo object cannot be null\n";
				throw new java.lang.IllegalArgumentException(ErrorMsg);
			}
			infoObj = Info;
			node = Node;
		}

		public void run(TaskMonitor taskMonitor) {
			if (taskMonitor == null) {
				throw new IllegalStateException("Task Monitor is not set.");
			}
			taskMonitor.setStatusMessage("Installing " + infoObj.getName() + " v"
					+ infoObj.getObjectVersion());
			//taskMonitor.setProgress(-1);

			AppManager Mgr = AppManager.getAppManager();
			
			Installable ins = infoObj.getInstallable();
			try {
				infoObj = Mgr.download(infoObj, taskMonitor);
				taskMonitor.setStatusMessage(infoObj.getName() + " v"
						+ infoObj.getObjectVersion() + " download complete.");

				AppManageDialog.this.setMessage(infoObj.toString()
						+ " download complete.");

				taskMonitor.setStatusMessage(infoObj.toString() + " installing...");

				Mgr.install(infoObj);
				Mgr.loadApp(infoObj);

				if ( Mgr.getLoadingErrors().size() > 0 ) { 
					// since we're only loading one app, presumably there will only
					// be one throwable...
					Throwable t = Mgr.getLoadingErrors().get(0);
					Mgr.clearErrorList();
					throw new AppException("Failed to load app: " + infoObj.toString(),t); 
				}

				taskMonitor.setStatusMessage(infoObj.toString() + " install complete.");
					
			} catch (java.io.IOException ioe) {
				taskMonitor.setStatusMessage("Failed to download "
								+ infoObj.getName() + " from "
								+ infoObj.getObjectUrl());
				logger.warn("Failed to download "
								+ infoObj.getName() + " from "
								+ infoObj.getObjectUrl(), ioe);
				infoObj = null;
			} catch (ManagerException me) {
				AppManageDialog.this.setError("Failed to install " + infoObj.toString());
				logger.warn("Failed to install " + infoObj.toString(), me);
				taskMonitor.setStatusMessage(me.getMessage());
				infoObj = null;
			} catch (AppException pe) {
				AppManageDialog.this.setError("Failed to install " + infoObj.toString());
				logger.warn("Failed to install " + infoObj.toString(), pe);
				taskMonitor.setStatusMessage(pe.getMessage());
				infoObj = null;
			} catch (ClassNotFoundException cne) {
				AppManageDialog.this.setError("Failed to install " + infoObj.toString());
				logger.warn("Failed to install " + infoObj.toString(), cne);
				taskMonitor.setStatusMessage(cne.getMessage());
				infoObj = null;
			} finally {
				taskMonitor.setProgress(1.0);
			}

			try {
			if (infoObj == null)
				ins.uninstall();
			} catch (ManagerException me) {
				logger.warn("Failed to cleanup after installation failure", me);
			}
			
		}

		public void cancel(){	
		}
		
		public DownloadableInfo getDownloadedApp() {
			return infoObj;
		}

		public void halt() {
			// not haltable
		}

		//public void setTaskMonitor(TaskMonitor monitor)
		//		throws IllegalThreadStateException {
		//	this.taskMonitor = monitor;
		//}

		public String getTitle() {
			return "Installing Cytoscape " + infoObj.getType().name() + " '" + infoObj.getName() + "'";
		}

	}

    // Variables declaration - do not modify                     
    private javax.swing.JLabel availableAppsLabel;
    private javax.swing.JPanel bottomPane;
    private javax.swing.JButton btnAddSite;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDeleteSite;
    private javax.swing.JButton btnEditSite;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSelectLocalApp;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel downloadLocText;
    private javax.swing.JScrollPane infoScrollPane;
    private javax.swing.JEditorPane infoTextPane;
    private javax.swing.JButton installDeleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbSearchTitle;
    private javax.swing.JLabel lbSelectLocalFile;
    private javax.swing.JList lstDownloadSites;
    private javax.swing.JLabel msgLabel;
    private javax.swing.JTextArea msgPanel;
    private javax.swing.JTree appTree;
    private javax.swing.JPanel pnlImportLocalApp;
    private javax.swing.JPanel pnlSearch;
    private javax.swing.JPanel pnlSettings;
    private javax.swing.JPanel sitePanel;
    private javax.swing.JTextField tfSearch;
    private javax.swing.JPanel topPane;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JCheckBox versionCheck;
    // End of variables declaration                   
    
    private TreeNode rootTreeNode;
	private TreeNode installedNode;
	private TreeNode availableNode;
	private ManagerModel treeModel;
	private TreeCellRenderer treeRenderer;
	private java.util.HashMap<TreeNode, java.util.List<TreeNode>> hiddenNodes;
	
	
	// loads the combobox for bookmarks
	private void loadBookmarkCMBox(boolean selectLast) {
		DefaultComboBoxModel theModel = new DefaultComboBoxModel();

		// Extract the URL entries
		List<DataSource> theDataSourceList = this.bookmarksUtil.getDataSourceList(
				bookmarkCategory, theBookmarks.getCategory());

		if (theDataSourceList != null) {
			for (DataSource Current : theDataSourceList) {
				theModel.addElement(Current);
				if (selectLast)
					theModel.setSelectedItem(Current);
			}
		}

		this.lstDownloadSites.setModel(theModel);
		
	}
	

	/*
	 * Sets up the bookmarks for app download sites
	 */
	private void bookmarksSetUp() {


/*
		try {
			theBookmarks = this.bookmarksReader.getProperties(); //Cytoscape.getBookmarks();
		} catch (Exception E) {
			JOptionPane.showMessageDialog(this.desktop,
					"Failed to retrieve bookmarks for app download sites.",
					"Error", JOptionPane.ERROR_MESSAGE);
			logger.warn("Failed to retrieve bookmarks for app download sites.", E);

			return;
		}
*/

		// if theBookmarks does not exist, create an empty one
		if (this.theBookmarks == null) {
			theBookmarks = new Bookmarks();
			//this.theBookmarks.s.setBookmarks(theBookmarks);
		}

	}

	private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);

	// required to make the text of the data source show up correctly in the
	// combo box
	private class BookmarkCellRenderer extends JLabel implements
			ListCellRenderer {
		
		public BookmarkCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			DataSource dataSource = (DataSource) value;
			setText(dataSource.getName());

			setToolTipText(dataSource.getHref());
			
			if (isSelected) {
			      setBackground(HIGHLIGHT_COLOR);
			      setForeground(Color.white);
			    } else {
			      setBackground(Color.white);
			      setForeground(Color.black);
			    }

			return this;
		}
	}

	
	private class MyListSelectionListener implements ListSelectionListener {
		
		private int clickCount =0;
		
		public void valueChanged(ListSelectionEvent ev){
			clickCount++;

			JList list = (JList) ev.getSource();
			if (list.getSelectedIndices().length == 0){
				// If nothing is selected, disable Edit/Delete button
				AppManageDialog.this.btnEditSite.setEnabled(false);
				AppManageDialog.this.btnDeleteSite.setEnabled(false);
				return;
			}

			if (clickCount % 2 == 0){
				//This is a work-around to handle two events -- from mouse press and mouse release 
				// Reset the click counter
				clickCount = 0;
				return;
			}

			DataSource dataSource = (DataSource) list.getSelectedValue();
			
			if (dataSource.getName().equalsIgnoreCase("Cytoscape")){
				// Cytoscape is the default site, user should never edit/delete it
				AppManageDialog.this.btnEditSite.setEnabled(false);
				AppManageDialog.this.btnDeleteSite.setEnabled(false);			
			}
			else {
				AppManageDialog.this.btnEditSite.setEnabled(true);
				AppManageDialog.this.btnDeleteSite.setEnabled(true);
			}

			//If download site is changed, update the app tree 
			String urlStr = dataSource.getHref();
					
			if (!AppManageDialog.this.currentAppSiteURL.equalsIgnoreCase(urlStr)){
				AppManageDialog.this.currentAppSiteURL = urlStr;
				AppManageDialog.this.refreshAppTree();
			}
		}
	}
	

	public static void main(String[] args) {
		AppManageDialog pd = new AppManageDialog();
        pd.setSiteName("Testing");
        List<DownloadableInfo> Apps = new java.util.ArrayList<DownloadableInfo>();

		AppInfo infoC = new AppInfo("1", "A App");
		infoC.addCytoscapeVersion(AppManagerAction.cyVersion.getVersion());
		Apps.add(infoC);

		infoC = new AppInfo("2", "B App");
		infoC.addCytoscapeVersion(AppManagerAction.cyVersion.getVersion());
		Apps.add(infoC);

		infoC = new AppInfo("3", "C");
		infoC.addCytoscapeVersion(AppManagerAction.cyVersion.getVersion());
		Apps.add(infoC);

		pd.addCategory(Category.NONE.toString(), Apps,
				AppInstallStatus.AVAILABLE);

		List<DownloadableInfo> Outdated = new java.util.ArrayList<DownloadableInfo>();

		AppInfo infoOD = new AppInfo("11", "CyGoose");
		infoOD.addCytoscapeVersion("2.3");
		Outdated.add(infoOD);

		infoOD = new AppInfo("12", "Y");
		infoOD.addCytoscapeVersion("2.3");
		Outdated.add(infoOD);

		pd.addCategory("Outdated", Outdated, AppInstallStatus.AVAILABLE);

		pd.setMessage("Foo bar");
		
		pd.setVisible(true);
	}

}
