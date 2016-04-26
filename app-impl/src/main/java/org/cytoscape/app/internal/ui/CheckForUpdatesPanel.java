package org.cytoscape.app.internal.ui;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.app.internal.event.UpdatesChangedEvent;
import org.cytoscape.app.internal.event.UpdatesChangedListener;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.Update;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.task.InstallAppsFromWebAppTask;
import org.cytoscape.app.internal.task.InstallUpdatesTask;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSite;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

/**
 * This class represents the panel in the App Manager dialog's tab used for checking for app updates.
 */
@SuppressWarnings("serial")
public class CheckForUpdatesPanel extends JPanel {

	private JLabel descriptionLabel;
    private JScrollPane descriptionScrollPane;
    private JTextArea descriptionTextArea;
    private JButton installAllButton;
    private JButton installSelectedButton;
    private JLabel lastCheckForUpdatesLabel;
    private JButton manageUpdateSites;
    private JLabel updateCheckTimeLabel;
    private JLabel updatesAvailableLabel;
    private JScrollPane updatesScrollPane;
    private JTable updatesTable;
    
    Container parent;
    
    private UpdateManager updateManager;
    private AppManager appManager;
    private DownloadSitesManager downloadSitesManager;
    private TaskManager taskManager;
    
	public CheckForUpdatesPanel(AppManager appManager, DownloadSitesManager downloadSitesManager,
			UpdateManager updateManager, TaskManager taskManager, Container parent) {
    	this.updateManager = updateManager;
        this.appManager = appManager;
        this.downloadSitesManager = downloadSitesManager;
        this.taskManager = taskManager;
    	
    	initComponents();
    	
    	this.parent = parent;
    }

    private void initComponents() {
    	updatesAvailableLabel = new JLabel("0 updates available.");
        installSelectedButton = new JButton("Update Selected");
        installAllButton = new JButton("Update All");
        updatesScrollPane = new JScrollPane();
        updatesTable = new JTable();
        lastCheckForUpdatesLabel = new JLabel("Last check for updates:");
        updateCheckTimeLabel = new JLabel("Today, at 6:00 pm");
        descriptionLabel = new JLabel("Update Description:");
        descriptionScrollPane = new JScrollPane();
        descriptionTextArea = new JTextArea();
        manageUpdateSites = new JButton("Manage Sites...");

        installSelectedButton.setEnabled(false);
        installSelectedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	installUpdates(getSelectedUpdates());
            }
        });

        installAllButton.setEnabled(false);
        installAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	installUpdates(updateManager.getUpdates());
            }
        });

        updatesTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "App Name", "Version"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        updatesScrollPane.setViewportView(updatesTable);

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setFocusable(false);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        manageUpdateSites.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manageUpdateSitesActionPerformed(evt);
            }
        });

        LookAndFeelUtil.equalizeSize(installSelectedButton, installAllButton);
        
        final GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(updatesAvailableLabel)
						.addGap(10, 20, Short.MAX_VALUE)
						.addComponent(lastCheckForUpdatesLabel)
						.addComponent(updateCheckTimeLabel)
				)
				.addComponent(updatesScrollPane, DEFAULT_SIZE, 652, Short.MAX_VALUE)
				.addComponent(descriptionLabel)
				.addComponent(descriptionScrollPane)
				.addGroup(layout.createSequentialGroup()
						.addComponent(installSelectedButton)
						.addComponent(installAllButton)
						.addGap(10, 20, Short.MAX_VALUE)
						.addComponent(manageUpdateSites)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(updatesAvailableLabel)
						.addComponent(lastCheckForUpdatesLabel)
						.addComponent(updateCheckTimeLabel)
				)
				.addComponent(updatesScrollPane, DEFAULT_SIZE, 327, Short.MAX_VALUE)
				.addComponent(descriptionLabel, PREFERRED_SIZE, 16, PREFERRED_SIZE)
				.addComponent(descriptionScrollPane, PREFERRED_SIZE, 106, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(installSelectedButton)
						.addComponent(installAllButton)
						.addComponent(manageUpdateSites)
				)
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
				if (!event.getSource().getUpdates().isEmpty()) {
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
        		checkUpdates();
        	}
        });
        
        setupDescriptionListener();
    }
    
    private void checkUpdates() {
    	final Set<App> appsToCheckUpdates = appManager.getInstalledApps();

		taskManager.execute(new TaskIterator(new Task() {
			
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				taskMonitor.setTitle("Checking for updates");
				
				WebQuerier webQuerier = appManager.getWebQuerier();
				String siteName, siteUrl;
				double progress = 0.0;
				
				// Obtain apps listing from each site if not done so
				for (DownloadSite downloadSite : downloadSitesManager.getDownloadSites()) {
					
					siteName = downloadSite.getSiteName();
					siteUrl = downloadSite.getSiteUrl();
					
					taskMonitor.setStatusMessage("Obtaining apps listing from " 
							+ siteName + "(" + siteUrl + ") ...");
					taskMonitor.setProgress(progress);
					
					progress += 1.0 / (downloadSitesManager.getDownloadSites().size() + 1);
					
					webQuerier.setCurrentAppStoreUrl(siteUrl);
					Set<WebApp> webApps = webQuerier.getAllApps();
				}
				
				taskMonitor.setStatusMessage("Reading listings for new versions");
				taskMonitor.setProgress(0.98); // We're 98% done.
				
				updateManager.checkForUpdates(appsToCheckUpdates);
			}
			
			@Override
			public void cancel() {
				// TODO Auto-generated method stub
				
			}
		}));
    }
    
    private void installUpdates(final Set<Update> updates) {
        TaskIterator ti = new TaskIterator();
        ti.append(new InstallUpdatesTask(updates, appManager));
        taskManager.execute(ti, new TaskObserver(){

			@Override
			public void taskFinished(ObservableTask task) {}

			@Override
			public void allFinished(FinishStatus finishStatus) {
				checkUpdates();
			}});
    }
    
    private void manageUpdateSitesActionPerformed(ActionEvent evt) {
    	if (parent instanceof AppManagerDialog) {
    		((AppManagerDialog) parent).showManageDownloadSitesDialog();
    	}
    }
    
    private void repopulateUpdatesTable() {
    	SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updatesTable.setModel(new DefaultTableModel(
			            new Object [][] {

			            },
			            new String [] {
			                "App Name", "Current Version", "New Version", "Update URL"
			            }
			        ) {
						
						boolean[] canEdit = new boolean [] {
			                false, false, false, false
			            };

						@Override
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
    	
    	if (selectedUpdates.isEmpty()) {
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
