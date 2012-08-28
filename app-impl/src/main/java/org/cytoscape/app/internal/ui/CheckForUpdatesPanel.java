package org.cytoscape.app.internal.ui;

import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.app.internal.event.UpdatesChangedEvent;
import org.cytoscape.app.internal.event.UpdatesChangedListener;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.Update;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

/**
 * This class represents the panel in the App Manager dialog's tab used for checking for app updates.
 * Its UI setup code is generated by the Netbeans 7 GUI builder.
 */
public class CheckForUpdatesPanel extends javax.swing.JPanel {

	/** Long serial version identifier required by the Serializable class */
	private static final long serialVersionUID = -7895560768591483673L;
	
	private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JButton installAllButton;
    private javax.swing.JButton installSelectedButton;
    private javax.swing.JLabel lastCheckForUpdatesLabel;
    private javax.swing.JButton manageUpdateSites;
    private javax.swing.JLabel updateCheckTimeLabel;
    private javax.swing.JLabel updatesAvailableLabel;
    private javax.swing.JScrollPane updatesScrollPane;
    private javax.swing.JTable updatesTable;
    
    Container parent;
    
    private UpdateManager updateManager;
    private AppManager appManager;
    private TaskManager taskManager;
    
    public CheckForUpdatesPanel(AppManager appManager, TaskManager taskManager, Container parent) {
    	this.updateManager = new UpdateManager();
        this.appManager = appManager;
        this.taskManager = taskManager;
    	
    	initComponents();
    	
    	this.parent = parent;
    }

    private void initComponents() {

    	updatesAvailableLabel = new javax.swing.JLabel();
        installSelectedButton = new javax.swing.JButton();
        installAllButton = new javax.swing.JButton();
        updatesScrollPane = new javax.swing.JScrollPane();
        updatesTable = new javax.swing.JTable();
        lastCheckForUpdatesLabel = new javax.swing.JLabel();
        updateCheckTimeLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        manageUpdateSites = new javax.swing.JButton();

        updatesAvailableLabel.setText("0 updates available.");

        installSelectedButton.setText("Update Selected");
        installSelectedButton.setEnabled(false);
        installSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installSelectedButtonActionPerformed(evt);
            }
        });

        installAllButton.setText("Update All");
        installAllButton.setEnabled(false);
        installAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installAllButtonActionPerformed(evt);
            }
        });

        updatesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "App Name", "Version"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        updatesScrollPane.setViewportView(updatesTable);

        lastCheckForUpdatesLabel.setText("Last check for updates:");

        updateCheckTimeLabel.setText("Today, at 6:00 pm");

        descriptionLabel.setText("Update Description:");

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setFocusable(false);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        manageUpdateSites.setText("Manage Sites");
        manageUpdateSites.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageUpdateSitesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(updatesAvailableLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lastCheckForUpdatesLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(updateCheckTimeLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(updatesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
                            .addComponent(descriptionScrollPane))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(installSelectedButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(installAllButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(manageUpdateSites))
                            .addComponent(descriptionLabel))
                        .addGap(0, 267, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updatesAvailableLabel)
                    .addComponent(lastCheckForUpdatesLabel)
                    .addComponent(updateCheckTimeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updatesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(installSelectedButton)
                    .addComponent(installAllButton)
                    .addComponent(manageUpdateSites))
                .addContainerGap())
        );
        
        updateManager.addUpdatesChangedListener(new UpdatesChangedListener() {
			
			@Override
			public void updatesChanged(UpdatesChangedEvent event) {

        		int updateCount = updateManager.getUpdates().size();
        		updatesAvailableLabel.setText(updateCount + " " 
        				+ (updateCount == 1 ? "update" : "updates") + " available.");
        		
        		Calendar lastUpdateCheckTime = updateManager.getLastUpdateCheckTime();
        		
        		int minute = lastUpdateCheckTime.get(Calendar.MINUTE);
        		
        		updateCheckTimeLabel.setText("Today, at " 
        			+ (lastUpdateCheckTime.get(Calendar.HOUR) == 0 ? "12" : lastUpdateCheckTime.get(Calendar.HOUR)) + ":"
        			+ (minute < 10 ? "0" : "") + minute + " "
        			+ (lastUpdateCheckTime.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm"));
        		
				repopulateUpdatesTable();

				// Enable/disable the update all button depending on update availability
				if (event.getSource().getUpdates().size() > 0) {
					if (!installAllButton.isEnabled()) {
						installAllButton.setEnabled(true);
					}
				} else {
					if (installAllButton.isEnabled()) {
						installAllButton.setEnabled(false);
					}
				}
			}
		});
        
        installAllButton.setEnabled(true);
        
        this.addComponentListener(new ComponentAdapter() {
		
        	@Override
        	public void componentShown(ComponentEvent e) {
        		
        		Set<App> appsToCheckUpdates = new HashSet<App>();
        		
        		for (App app : appManager.getApps()) {
        			if (app.getStatus() == AppStatus.INSTALLED
        					|| app.getStatus() == AppStatus.DISABLED) {
        				appsToCheckUpdates.add(app);
        			}
        		}
        		
        		updateManager.checkForUpdates(appManager.getWebQuerier(), 
        				appsToCheckUpdates, 
        				appManager);
        		
               	}
        });
        
        setupDescriptionListener();
    }

    private void installSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {
        final Set<Update> selectedUpdates = getSelectedUpdates();
        final int updateCount = selectedUpdates.size();
        
        taskManager.execute(new TaskIterator(new Task() {

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				taskMonitor.setTitle("Installing updates");
				
				double progress = 0;
				int count = 0;
				
				for (Update update : selectedUpdates) {
					count++;
					
					taskMonitor.setStatusMessage("Installing update " 
							+ update.getRelease().getReleaseVersion() 
							+ " for " + update.getApp().getAppName() 
							+ " (" + count + "/" + updateCount + ")");
					
		        	updateManager.installUpdate(update, appManager);
		        }
			}

			@Override
			public void cancel() {	
			}
        	
        }));
    }

    private void installAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        final Set<Update> updates = new HashSet<Update>(updateManager.getUpdates());
        final int updateCount = updates.size();

        taskManager.execute(new TaskIterator(new Task() {

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				taskMonitor.setTitle("Installing updates");
				
				double progress = 0;
				int count = 0;
				
				for (Update update : updates) {
					count++;
					
					taskMonitor.setStatusMessage("Installing update " 
							+ update.getRelease().getReleaseVersion() 
							+ " for " + update.getApp().getAppName() 
							+ " (" + count + "/" + updateCount + ")");
					
		        	updateManager.installUpdate(update, appManager);
		        }
			}

			@Override
			public void cancel() {
			}
        	
        }));
    }
    
    private void manageUpdateSitesActionPerformed(java.awt.event.ActionEvent evt) {
    	if (parent instanceof AppManagerDialog) {
    		((AppManagerDialog) parent).showManageDownloadSitesDialog();
    	}
    }
    
    private void repopulateUpdatesTable() {
   
    	SwingUtilities.invokeLater(new Runnable() {
    		
			@Override
			public void run() {
				
				updatesTable.setModel(new javax.swing.table.DefaultTableModel(
			            new Object [][] {

			            },
			            new String [] {
			                "App Name", "Current Version", "New Version", "Update URL"
			            }
			        ) {
						private static final long serialVersionUID = 5428723339522445073L;
						
						boolean[] canEdit = new boolean [] {
			                false, false, false, false
			            };

			            public boolean isCellEditable(int rowIndex, int columnIndex) {
			                return canEdit [columnIndex];
			            }
			        });
				
				
				DefaultTableModel tableModel = (DefaultTableModel) updatesTable.getModel();
				
				for (Update update : updateManager.getUpdates()) {	
		    			tableModel.addRow(new Object[]{
		    				update,
		    				update.getApp().getVersion(),
		    				update.getUpdateVersion(),
		    				(update.getRelease().getBaseUrl() 
		    						+ update.getRelease().getRelativeUrl()).replaceAll("//+", "/").replaceFirst(":/", "://")
					});
		    	}
			}
    		
    	});	
    }
    
    /**
     * Obtain the set of {@link Update} objects corresponding to currently selected entries in the table of apps
     * @return A set of {@link Update} objects corresponding to selected apps in the table
     */
    private Set<Update> getSelectedUpdates() {
        Set<Update> selectedUpdates = new HashSet<Update>();
    	 int[] selectedRows = updatesTable.getSelectedRows();
    	
        for (int index = 0; index < selectedRows.length; index++) {
        	
        	Update update = (Update) updatesTable.getModel().getValueAt(
        			updatesTable.convertRowIndexToModel(selectedRows[index]), 0);
        	
        	selectedUpdates.add(update);
        }
    	
    	return selectedUpdates;
    }
    
    /**
     * Setup and register a listener to the table to listen for selection changed events in order to update the
     * description box
     */
    private void setupDescriptionListener() {
    	updatesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					//System.out.println("selection change " + e.getFirstIndex());
					//System.out.println("selected: " + getSelectedUpdates().size());
					
					updateDescriptionBox();
				}
			}
		});
    }
    
    private void updateDescriptionBox() {
    	Set<Update> selectedUpdates = getSelectedUpdates();
    	
    	if (selectedUpdates.size() == 0) {
    		descriptionTextArea.setText("");
    		installSelectedButton.setEnabled(false);
    	} else if (selectedUpdates.size() == 1) {
    		Update update = selectedUpdates.iterator().next();
    		descriptionTextArea.setText("");
    		
    		if (!installSelectedButton.isEnabled()) {
    			installSelectedButton.setEnabled(true);
    		}
    	} else if (selectedUpdates.size() > 1) {
    		descriptionTextArea.setText("");
    		
    		if (!installSelectedButton.isEnabled()) {
    			installSelectedButton.setEnabled(true);
    		}
    	}
    	
//   	descriptionTextArea.setText("Update from ")
    }
}
