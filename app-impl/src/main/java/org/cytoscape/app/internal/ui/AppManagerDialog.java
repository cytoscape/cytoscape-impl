package org.cytoscape.app.internal.ui;

import org.cytoscape.app.internal.event.AppsChangedEvent;
import org.cytoscape.app.internal.event.AppsChangedListener;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.app.internal.ui.downloadsites.ManageDownloadSitesDialog;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.TaskManager;

/**
 * This class represents the App Manager dialog window. Its UI setup code is generated by the Netbeans 7 GUI builder.
 */
public class AppManagerDialog extends javax.swing.JDialog {

	private CheckForUpdatesPanel checkForUpdatesPanel;
    private CurrentlyInstalledAppsPanel currentlyInstalledAppsPanel;
    private InstallAppsPanel installAppsPanel;
    private javax.swing.JTabbedPane mainTabbedPane;

    private ManageDownloadSitesDialog manageDownloadSitesDialog;
    private DownloadSitesManager downloadSitesManager;
    
    private AppManager appManager;
	private FileUtil fileUtil;
	private TaskManager taskManager;
    
    public AppManagerDialog(AppManager appManager, 
    		DownloadSitesManager downloadSitesManager,
    		FileUtil fileUtil, 
    		TaskManager taskManager, 
    		java.awt.Frame parent, 
    		boolean modal) {
        super(parent, modal);
        
        this.appManager = appManager;
        this.downloadSitesManager = downloadSitesManager;
        this.fileUtil = fileUtil;
        this.taskManager = taskManager;
        initComponents();
        
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
        
        // Create new manage download sites dialog
        manageDownloadSitesDialog = new ManageDownloadSitesDialog(
        		parent, false, downloadSitesManager);
        manageDownloadSitesDialog.setLocationRelativeTo(this);
    }
   
    private void initComponents() {
    	mainTabbedPane = new javax.swing.JTabbedPane();
        installAppsPanel = new InstallAppsPanel(appManager, downloadSitesManager, fileUtil, taskManager, this);
        currentlyInstalledAppsPanel = new CurrentlyInstalledAppsPanel(appManager);
        checkForUpdatesPanel = new CheckForUpdatesPanel(appManager, downloadSitesManager, taskManager, this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("App Manager");
        
        mainTabbedPane.addTab("Install Apps", installAppsPanel);
        mainTabbedPane.addTab("Currently Installed", currentlyInstalledAppsPanel);
        mainTabbedPane.addTab("Check for Updates", checkForUpdatesPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                .addContainerGap())
        );
        
        pack();
    }
    
    public void changeTab(int index) {
    	mainTabbedPane.setSelectedIndex(index);
    }
    
    public void showManageDownloadSitesDialog() {
    	if (manageDownloadSitesDialog != null) {
    		manageDownloadSitesDialog.setLocationRelativeTo(this);
    		manageDownloadSitesDialog.setVisible(true);
    	}
    }
}
