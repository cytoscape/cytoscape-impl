package org.cytoscape.app.internal.ui;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.cytoscape.app.internal.CytoscapeApp;
import org.cytoscape.app.internal.DownloadableInfo;
import org.cytoscape.app.internal.AppInfo;
import org.cytoscape.app.internal.AppManager;
import org.cytoscape.app.internal.ThemeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
//import cytoscape.task.ui.JTaskConfig;
//import cytoscape.task.util.TaskManager;

// TODO clean out the tree for each updated app

public class AppUpdateDialog extends JDialog implements
		TreeSelectionListener {
	private static String title = "Update Apps";
	private static final Logger logger = LoggerFactory.getLogger(CytoscapeApp.class);


	public AppUpdateDialog(javax.swing.JDialog owner) {
		super(owner, title);
		setLocationRelativeTo(owner);
		initComponents();
		initTree();
	}

	public AppUpdateDialog(javax.swing.JFrame owner) {
		super(owner, title);
		setLocationRelativeTo(owner);
		initComponents();
		initTree();
	}

	/**
	 * Enables the delete/install buttons when the correct leaf node is selected
	 */
	public void valueChanged(TreeSelectionEvent e) {
		infoTextPane.setContentType("text/html");
		javax.swing.tree.TreePath[] Paths = appTree.getSelectionPaths();
		if (Paths == null) {
			updateSelectedButton.setEnabled(false);
			return;
		}

		if (Paths.length == 0) {
			updateSelectedButton.setEnabled(false);
		}

		for (int i = 0; i < Paths.length; i++) {
			TreeNode LastSelectedNode = (TreeNode) Paths[i]
					.getLastPathComponent();

			if (LastSelectedNode.isLeaf()) {
				AppInfo New = (AppInfo) LastSelectedNode.getObject();

				infoTextPane.setText(New.htmlOutput());
				updateSelectedButton.setEnabled(true);
			} else if (LastSelectedNode.getObject() != null
					&& LastSelectedNode.getObject().getClass().equals(
							AppInfo.class)) {
				AppInfo NodeInfo = (AppInfo) LastSelectedNode.getObject();
				infoTextPane.setText(NodeInfo.htmlOutput());
			}
		}
	}

	/**
	 * DOCUMENT ME
	 * 
	 * @param CategoryName
	 * @param Apps
	 * @param Status
	 */
	public void addCategory(String CategoryName, DownloadableInfo CurrentApp,
			List<DownloadableInfo> NewApps) {
		TreeNode Category = new TreeNode(CategoryName, true);
		treeModel.addNodeToParent(rootTreeNode, Category);

		TreeNode CurrentAppNode = new TreeNode(CurrentApp, true);
		treeModel.addNodeToParent(Category, CurrentAppNode);

		for (DownloadableInfo New : NewApps) {
			treeModel.addNodeToParent(CurrentAppNode, new TreeNode(New));
		}
	}

	/**
	 * 
	 * @param Msg
	 */
	public void setMessage(String Msg) {
		msgLabel.setText(Msg);
	}

	// update for single selections
	private void updateSelectedButtonActionPerformed(
			java.awt.event.ActionEvent evt) {
		Set<TreeNode> AllParents = new java.util.HashSet<TreeNode>();

		javax.swing.tree.TreePath[] Paths = appTree.getSelectionPaths();
		Map<DownloadableInfo, DownloadableInfo> UpdatableObjs = new java.util.HashMap<DownloadableInfo, DownloadableInfo>();

		// first make sure each node only has one option picked
		String Msg = "Please choose just one update option for the following apps:\n";
		boolean TooManyChildren = false;
		for (javax.swing.tree.TreePath Path : Paths) {
			TreeNode Node = (TreeNode) Path.getLastPathComponent();
			TreeNode Parent = Node.getParent();

			if (AllParents.contains(Parent)) {
				Msg += Parent.toString() + "\n";
				TooManyChildren = true;
			} else {
				AllParents.add(Parent);
				UpdatableObjs.put(Parent.getObject(), Node.getObject());
				treeModel.removeNodeFromParent(Node);
			}
		}
		if (TooManyChildren) {
			JOptionPane.showMessageDialog(this, Msg,
					"Warning: Too many updates selected",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// run the update
		List<DownloadableInfo[]> Updatable = getUpdateList(UpdatableObjs);
		createUpdateTask(Updatable);

		setMessage("Update will complete when Cytoscape is restarted.");
	}

	// close dialog
	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private void updateAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (JOptionPane
				.showConfirmDialog(
						this,
						"All apps will be updated to the newest available version.\n"
								+ "If you wish to choose a different version please press \"No\" then\n"
								+ "choose each version and \"Update Selected Apps\"",
						"Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
			return;
		}

		Map<DownloadableInfo, DownloadableInfo> UpdateableObjs = new java.util.HashMap<DownloadableInfo, DownloadableInfo>();
		Set<TreeNode> RemovableNodes = new java.util.HashSet<TreeNode>();

		List<TreeNode> Leaves = recursiveReadTree(rootTreeNode);
		for (TreeNode Node : Leaves) {
			TreeNode Parent = Node.getParent();

			if (Parent.getChildCount() > 1) { // multiple possible updates
				DownloadableInfo LastInfoObj = null;
				for (TreeNode Sib : Parent.getChildren()) {
					DownloadableInfo CurrentInfoObj = Sib.getObject();
					if (LastInfoObj == null
							|| LastInfoObj.isNewerObjectVersion(CurrentInfoObj))
						UpdateableObjs.put(Parent.getObject(), CurrentInfoObj);
					LastInfoObj = CurrentInfoObj;
				}
			} else {
				UpdateableObjs.put(Parent.getObject(), Node.getObject());
			}
			RemovableNodes.add(Parent);
		}
		List<DownloadableInfo[]> ObjToUpdate = getUpdateList(UpdateableObjs);
		createUpdateTask(ObjToUpdate);

		for (TreeNode Node : RemovableNodes) {
			treeModel.removeNodeFromParent(Node);
		}

		setMessage("Update will complete when Cytoscape is restarted.");
	}

	/**
	 * Show licenses if required, add all apps to be updated to list.
	 * 
	 * @param PotentialUpdates
	 *            Key: Current app, Value: New app
	 * @return List <AppInfo[]{Old, New}>
	 */
	private List<DownloadableInfo[]> getUpdateList(
			java.util.Map<DownloadableInfo, DownloadableInfo> PotentialUpdates) {
		final List<DownloadableInfo[]> Updates = new java.util.ArrayList<DownloadableInfo[]>();

		// display licenses, set up the list of objects to be updated
		for (DownloadableInfo Original : PotentialUpdates.keySet()) {
			final DownloadableInfo Old = (DownloadableInfo) Original;
			final DownloadableInfo New = (DownloadableInfo) PotentialUpdates.get(Old);
			final LicenseDialog ld = new LicenseDialog();
			boolean showLicense = false;
			// display only if always required at update
			switch (New.getType()) {
			case APP:
				if (New.isLicenseRequired() && New.getLicenseText() != null) {
					ld.addApp(New);
					showLicense = true;
				}
				break;
			case THEME:
				ThemeInfo themeInfo = (ThemeInfo) New;
				for (AppInfo pInfo: themeInfo.getApps()) {
					if (pInfo.isLicenseRequired() && pInfo.getLicenseText() != null) {
						ld.addApp(pInfo);
						showLicense = true;
					}
				}
				break;
			case FILE: // there is currently not a FileInfo object
				break;
			}
			
			if (showLicense) {
				ld.addListenerToOk(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						Updates.add(new DownloadableInfo[] { Old, New });
						ld.dispose();
					}
				});
				ld.selectDefault();
				ld.setVisible(true);
			} else {
				Updates.add(new DownloadableInfo[] { Old, New });
			}
		}
		return Updates;
	}

	private List<TreeNode> recursiveReadTree(TreeNode Node) {
		List<TreeNode> LeafNodes = new java.util.ArrayList<TreeNode>();
		for (TreeNode Child : Node.getChildren()) {
			if (!Child.isLeaf()) {
				List<TreeNode> DeeperNodes = recursiveReadTree(Child);
				LeafNodes.addAll(DeeperNodes);
			} else {
				LeafNodes.add(Child);
			}
		}
		return LeafNodes;
	}

	// sets up the swing stuff
	private void initTree() {
		rootTreeNode = new TreeNode("Updatable Apps", true);

		appTree.addTreeSelectionListener(this);
		appTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		treeModel = new ManagerModel(rootTreeNode);
		appTree.setModel(treeModel);
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">                          
    private void initComponents() {
        jSplitPane1 = new javax.swing.JSplitPane();
        infoScrollPane = new javax.swing.JScrollPane();
        infoTextPane = new javax.swing.JEditorPane();
        treeScrollPane = new javax.swing.JScrollPane();
        appTree = new javax.swing.JTree();
        updateLabel = new javax.swing.JLabel();
        updateAllButton = new javax.swing.JButton();
        updateSelectedButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        msgLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jSplitPane1.setDividerLocation(250);
        infoScrollPane.setViewportView(infoTextPane);

        jSplitPane1.setRightComponent(infoScrollPane);

        treeScrollPane.setViewportView(appTree);

        jSplitPane1.setLeftComponent(treeScrollPane);

        updateLabel.setLabelFor(jSplitPane1);
        updateLabel.setText("Listed are updates available for currently installed apps");
        updateLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        updateAllButton.setText("Update All");
        updateAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAllButtonActionPerformed(evt);
            }
        });

        updateSelectedButton.setText("Update Selected");
        updateSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateSelectedButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        msgLabel.setForeground(java.awt.Color.BLACK);
        //msgLabel.setForeground(new java.awt.Color(204, 0, 51));
        msgLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(43, 43, 43)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, updateLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, msgLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE))
                .add(41, 41, 41))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(272, Short.MAX_VALUE)
                .add(updateAllButton)
                .add(18, 18, 18)
                .add(updateSelectedButton)
                .add(22, 22, 22)
                .add(closeButton)
                .add(50, 50, 50))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(updateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(msgLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(updateAllButton)
                    .add(updateSelectedButton)
                    .add(closeButton))
                .addContainerGap())
        );
        pack();
    }// </editor-fold>                        

	private void createUpdateTask(List<DownloadableInfo[]> UpdateObjs) {
		// Create Task
		Task task = new AppUpdateTask(UpdateObjs);

		// Configure JTask Dialog Pop-Up Box
		//JTaskConfig jTaskConfig = new JTaskConfig();
		//jTaskConfig.setOwner(Cytoscape.getDesktop());
		//jTaskConfig.displayCloseButton(false);
		//jTaskConfig.displayStatus(true);
		//jTaskConfig.setAutoDispose(true);
		//jTaskConfig.displayCancelButton(true);
		// Execute Task in New Thread; pop open JTask Dialog Box.
		//TaskManager.executeTask(task, jTaskConfig);
	}

	private class AppUpdateTask implements Task {
		private TaskMonitor taskMonitor;

		private List<DownloadableInfo[]> toUpdate;

		public AppUpdateTask(List<DownloadableInfo[]> Updates) {
			toUpdate = Updates;
		}

		public void run(TaskMonitor taskMonitor) {
			if (taskMonitor == null) {
				throw new IllegalStateException("Task Monitor is not set.");
			}
			taskMonitor.setStatusMessage("Updating...");
			taskMonitor.setProgress(-1);

			AppManager Mgr = AppManager.getAppManager();

			for (DownloadableInfo[] UpdatePair : toUpdate) {
				taskMonitor.setStatusMessage("Updating " + UpdatePair[0].getName()
						+ " to version " + UpdatePair[1].getObjectVersion());
				try {
					Mgr.update(UpdatePair[0], UpdatePair[1], taskMonitor);
				} catch (java.io.IOException ioe) {
					taskMonitor.setStatusMessage("Failed to download "
							+ UpdatePair[1].getName());
				} catch (org.cytoscape.app.internal.ManagerException me) {
					JOptionPane.showMessageDialog(AppUpdateDialog.this, me
							.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch (org.cytoscape.app.internal.WebstartException we) {
					logger.warn("Unable to update '"+UpdatePair[1].getName()+"': "+we.getMessage(), we);
				}
			}
			taskMonitor.setProgress(100);
		}

		public void cancel(){	
		}
		
		public void halt() {
			// not haltable
		}

		public void setTaskMonitor(TaskMonitor monitor)
				throws IllegalThreadStateException {
			this.taskMonitor = monitor;
		}

		public String getTitle() {
			return "Updating Apps";
		}

	}

	// Variables declaration - do not modify
	private javax.swing.JButton closeButton;
	private javax.swing.JScrollPane infoScrollPane;
	private javax.swing.JEditorPane infoTextPane;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JLabel msgLabel;
	private javax.swing.JTree appTree;
	private javax.swing.JScrollPane treeScrollPane;
	private javax.swing.JButton updateAllButton;
	private javax.swing.JLabel updateLabel;
	private javax.swing.JButton updateSelectedButton;
	private TreeNode rootTreeNode;
	private ManagerModel treeModel;
	// End of variables declaration
}
