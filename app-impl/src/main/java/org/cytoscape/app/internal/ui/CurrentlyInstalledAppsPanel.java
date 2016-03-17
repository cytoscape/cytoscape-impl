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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.app.internal.event.AppsChangedEvent;
import org.cytoscape.app.internal.event.AppsChangedListener;
import org.cytoscape.app.internal.exception.AppDisableException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.manager.BundleApp;
import org.cytoscape.app.internal.manager.SimpleApp;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.util.DebugHelper;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * This class represents the panel in the App Manager dialog's tab used for checking for currently installed apps.
 */
@SuppressWarnings("serial")
public class CurrentlyInstalledAppsPanel extends JPanel {

    private JScrollPane appsAvailableScrollPane;
    private JTable appsAvailableTable;
    private JLabel appsInstalledLabel;
    private JLabel descriptionLabel;
    private JScrollPane descriptionScrollPane;
    private JTextArea descriptionTextArea;
    private JButton enableSelectedButton;
    private JButton disableSelectedButton;
    private JButton uninstallSelectedButton;
	
    private AppManager appManager;
    private AppsChangedListener appListener;
    
    public CurrentlyInstalledAppsPanel(AppManager appManager) {
    	this.appManager = appManager;
    	
    	initComponents();
        
        setupAppListener();
        setupDescriptionListener();
        populateTable();
    }

    private void initComponents() {
        appsAvailableScrollPane = new JScrollPane();
        appsAvailableTable = new JTable();
        appsInstalledLabel = new JLabel("0 Apps installed.");
        enableSelectedButton = new JButton("Enable");
        disableSelectedButton = new JButton("Disable");
        uninstallSelectedButton = new JButton("Uninstall");
        descriptionLabel = new JLabel("App Information:");
        descriptionScrollPane = new JScrollPane();
        descriptionTextArea = new JTextArea();

        // The table of apps has a hidden first column that contains a reference to the actual App object
        appsAvailableTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "App", "Name", "Version", "Status"
            }
        ) {

			private static final long serialVersionUID = 919039586559362963L;
			
			boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

			@Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        appsAvailableScrollPane.setViewportView(appsAvailableTable);
        appsAvailableTable.getColumnModel().getColumn(1).setPreferredWidth(195);
        appsAvailableTable.removeColumn(appsAvailableTable.getColumn("App"));
        appsAvailableTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if (table.getValueAt(row, 2).equals(AppStatus.FAILED_TO_LOAD.toString()) || 
						table.getValueAt(row, 2).equals(AppStatus.FAILED_TO_START.toString()))
					setForeground(LookAndFeelUtil.getErrorColor());
				else
					setForeground(null);
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
        });
        
        enableSelectedButton.setEnabled(false);
        enableSelectedButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                enableSelectedButtonActionPerformed(evt);
            }
        });

        disableSelectedButton.setEnabled(false);
        disableSelectedButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                disableSelectedButtonActionPerformed(evt);
            }
        });

        uninstallSelectedButton.setEnabled(false);
        uninstallSelectedButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                uninstallSelectedButtonActionPerformed(evt);
            }
        });

        descriptionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setFocusable(false);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        LookAndFeelUtil.equalizeSize(enableSelectedButton, disableSelectedButton, uninstallSelectedButton);
        
        final GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(appsInstalledLabel)
				.addComponent(appsAvailableScrollPane, DEFAULT_SIZE, 634, Short.MAX_VALUE)
				.addComponent(descriptionLabel)
				.addComponent(descriptionScrollPane)
				.addGroup(layout.createSequentialGroup()
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(enableSelectedButton)
						.addComponent(disableSelectedButton)
						.addComponent(uninstallSelectedButton)
						.addGap(0, 0, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(appsInstalledLabel)
				.addComponent(appsAvailableScrollPane, DEFAULT_SIZE, 311, Short.MAX_VALUE)
				.addComponent(descriptionLabel)
				.addComponent(descriptionScrollPane, PREFERRED_SIZE, 106, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(enableSelectedButton)
						.addComponent(disableSelectedButton)
						.addComponent(uninstallSelectedButton)
				)
		);
        
        // Add listener to obtain descriptions for available apps
        this.addComponentListener(new ComponentAdapter() {
        	@Override
        	public void componentShown(ComponentEvent e) {
        		appManager.getWebQuerier().findAppDescriptions(appManager.getApps());
        	}
        });
        
        updateLabels();
    }
        
    private void enableSelectedButtonActionPerformed(ActionEvent evt) {
    	// Obtain App objects corresponding to currently selected table entries
        Set<App> selectedApps = getSelectedApps();
        
        for (App app : selectedApps) {
        	// Only install apps that are not already installed
        	if (app.getStatus() != AppStatus.INSTALLED) {
        		try {
					appManager.installApp(app);
				} catch (AppInstallException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        enableSelectedButton.setEnabled(false);
        disableSelectedButton.setEnabled(true);
        uninstallSelectedButton.setEnabled(true);
    }

    private void disableSelectedButtonActionPerformed(ActionEvent evt) {
    	// Obtain App objects corresponding to currently selected table entries
        Set<App> selectedApps = getSelectedApps();
        Map<App, Collection<App>> otherAppsDependingOn = getOtherAppsDependingOn(selectedApps);
        if(otherAppsDependingOn != null && !continueWithConflicts(otherAppsDependingOn)) 
        	return;
        
        for (App app : selectedApps) {
        	if (app.getStatus().equals(AppStatus.DISABLED))
                continue;

        	try {
        		appManager.disableApp(app);
        	} catch (AppDisableException e) {
        		// TODO Auto-generated catch block
				e.printStackTrace();
        	}
        }
        
        disableSelectedButton.setEnabled(false);
        enableSelectedButton.setEnabled(true);
        uninstallSelectedButton.setEnabled(true);
    }

    private void uninstallSelectedButtonActionPerformed(ActionEvent evt) {
    	// Obtain App objects corresponding to currently selected table entries
    	Set<App> selectedApps = getSelectedApps();
    	Map<App, Collection<App>> otherAppsDependingOn = getOtherAppsDependingOn(selectedApps);
        if(otherAppsDependingOn != null && !continueWithConflicts(otherAppsDependingOn)) 
        	return;
        
        for (App app : selectedApps) {
        	// Only uninstall apps that are installed
        	if (app.getStatus() == AppStatus.INSTALLED
        			|| app.getStatus() == AppStatus.DISABLED
        			|| app.getStatus() == AppStatus.FAILED_TO_LOAD
        			|| app.getStatus() == AppStatus.FAILED_TO_START) {
        		try {
					appManager.uninstallApp(app);
				} catch (AppUninstallException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        uninstallSelectedButton.setEnabled(false);
        disableSelectedButton.setEnabled(true);
        enableSelectedButton.setEnabled(true);
        appsAvailableTable.clearSelection();
    }

    /**
     * Obtain the set of {@link App} objects corresponding to currently selected entries in the table of apps
     * @return A set of {@link App} objects corresponding to selected apps in the table
     */
    private Set<App> getSelectedApps() {
        Set<App> selectedApps = new HashSet<>();
    	int[] selectedRows = appsAvailableTable.getSelectedRows();
    	
        for (int index = 0; index < selectedRows.length; index++) {
        	
        	App app = (App) appsAvailableTable.getModel().getValueAt(
        			appsAvailableTable.convertRowIndexToModel(selectedRows[index]), 0);
        	
        	selectedApps.add(app);
        }
    	
    	return selectedApps;
    }
    
    /**
     * Registers a listener to the {@link AppManager} to listen for app change events in order to rebuild the table
     */
    private void setupAppListener() {
    	appListener = new AppsChangedListener() {

			@Override
			public void appsChanged(AppsChangedEvent event) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						Set<App> selectedApps = getSelectedApps();
						
						// Clear table
						DefaultTableModel tableModel = (DefaultTableModel) appsAvailableTable.getModel();
						for (int rowIndex = tableModel.getRowCount() - 1; rowIndex >= 0; rowIndex--) {
							tableModel.removeRow(rowIndex);
						}
						
						// Re-populate table
						populateTable();
						
						// Update labels
						updateLabels();

						// Re-select previously selected apps
						for (int rowIndex = 0; rowIndex < tableModel.getRowCount(); rowIndex++) {
							if (selectedApps.contains(tableModel.getValueAt(rowIndex, 0))) {
								appsAvailableTable.addRowSelectionInterval(rowIndex, rowIndex);
							}
						}
					}
				});
			}
    	};
    	
    	appManager.addAppListener(appListener);
    }
    
    /**
     * Populate the table of apps by obtaining the list of currently available apps from the AppManager object.
     */
    private void populateTable() {
    	DefaultTableModel tableModel = (DefaultTableModel) appsAvailableTable.getModel();
		
    	for (App app : appManager.getApps()) {
    		
    		// Hide apps with certain statuses from the table, such as uninstalled ones.
    		if (app.isHidden()) {
    			// Do nothing
    			//DebugHelper.print(this, "Detached app: " + app.getAppName() + ", status: " + app.getStatus());
    		} else {
	    		tableModel.addRow(new Object[]{
						app,
						app.getAppFile() != null ? app.getAppName() : app.getAppName() + " (File moved)",
						app.getVersion(),
						app.getReadableStatus()
				});
    		}
    	}
    	
    	updateLabels();
    }
    
    /**
     * Update the labels that display the number of currently installed and available apps.
     */
    private void updateLabels() {
    	int listedCount = 0;
    	
    	for (App app : appManager.getApps()) {
    		
    		// Count the number of displayed apps
    		if (!app.isHidden()) {
    			listedCount++;
    		} else {
    			DebugHelper.print(this, "Hidden app: " + app.getAppName() + ", status: " + app.getStatus());
    		}
    	}
    	
    	appsInstalledLabel.setText(listedCount + " App(s) listed.");
    }
    
    /**
     * Setup and register a listener to the table to listen for selection changed events in order to update the
     * app description box
     */
    private void setupDescriptionListener() {
    	appsAvailableTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateDescriptionBox();
			}
		});
    }
    
    private void updateDescriptionBox() {
    	Set<App> selectedApps = getSelectedApps();
    	int numSelected = selectedApps.size();
    	
    	// If no apps are selected, clear the description box
    	if (numSelected == 0) {
    		descriptionTextArea.setText("");
    		
    		// Disable buttons
    		enableSelectedButton.setEnabled(false);
    		disableSelectedButton.setEnabled(false);
    		uninstallSelectedButton.setEnabled(false);
    		
    	// If a single app is selected, show its app description
    	} else if (numSelected == 1) {
    		App selectedApp = selectedApps.iterator().next();
    		
    		String type;
    		String text = selectedApp.getDescription() == null ? 
    			"App description not found." : selectedApp.getDescription();
    		type = "Type of app: ";
    		if (selectedApp instanceof BundleApp) {
    			type += "OSGi Bundle-based app";
    		} else if (selectedApp instanceof SimpleApp) {
    			type += "Standard Java Jar-based app";
    		} else {
    			type += "Unknown";
    		}
    		type += "\n";
    		type += "\n";
    		
    		descriptionTextArea.setText(type + text);
    		
    		// Enable/disable the appropriate button
    		if (selectedApp.getStatus() == AppStatus.INSTALLED || 
    				selectedApp.getStatus() == AppStatus.FAILED_TO_LOAD || 
    				selectedApp.getStatus() == AppStatus.FAILED_TO_START ) {
    			enableSelectedButton.setEnabled(false);
    			disableSelectedButton.setEnabled(true);
    			uninstallSelectedButton.setEnabled(true);
    		} else if (selectedApp.getStatus() == AppStatus.DISABLED) {
    			enableSelectedButton.setEnabled(true);
    			disableSelectedButton.setEnabled(false);
    			uninstallSelectedButton.setEnabled(true);
    		} else if (selectedApp.getStatus() == AppStatus.UNINSTALLED) {
    			enableSelectedButton.setEnabled(true);
    			disableSelectedButton.setEnabled(true);
    			uninstallSelectedButton.setEnabled(false);
    		} else {
    			enableSelectedButton.setEnabled(true);
    			disableSelectedButton.setEnabled(true);
    			uninstallSelectedButton.setEnabled(true);
    		}
    	} else {
    		descriptionTextArea.setText(numSelected + " apps selected.");
    		
    		// Enable/disable the appropriate buttons
    		boolean allInstalled = true;
    		boolean allDisabled = true;
    		boolean allUninstalled = true;
    		
    		for (App selectedApp : selectedApps) {
    			if (selectedApp.getStatus() == AppStatus.INSTALLED) {
    				allDisabled = false;
    				allUninstalled = false;
    			}
    			
    			if (selectedApp.getStatus() == AppStatus.DISABLED) {
    				allInstalled = false;
    				allUninstalled = false;
    			}
    			
    			if (selectedApp.getStatus() == AppStatus.UNINSTALLED) {
    				allInstalled = false;
    				allDisabled = false;
    			}
    		}
    		
    		if (allInstalled) {
    			enableSelectedButton.setEnabled(false);
    			disableSelectedButton.setEnabled(true);
    			uninstallSelectedButton.setEnabled(true);
    		} else if (allDisabled) {
    			enableSelectedButton.setEnabled(true);
    			disableSelectedButton.setEnabled(false);
    			uninstallSelectedButton.setEnabled(true);
    		} else if (allUninstalled) {
    			enableSelectedButton.setEnabled(true);
    			disableSelectedButton.setEnabled(true);
    			uninstallSelectedButton.setEnabled(false);
    		} else {
    			enableSelectedButton.setEnabled(true);
    			disableSelectedButton.setEnabled(true);
    			uninstallSelectedButton.setEnabled(true);
    		}
    	}
    }
    
    public boolean continueWithConflicts(Map<App, Collection<App>> otherAppsDependingOn) {
    	JPanel panel = new JPanel();
    	panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    	JLabel title = new JLabel("The following are required by one or more installed apps:");
    	title.setAlignmentX(LEFT_ALIGNMENT);
    	panel.add(title);
    	panel.add(Box.createVerticalStrut(title.getPreferredSize().height));
    	String deps = "";
    	for(App app: otherAppsDependingOn.keySet()) {
    		deps +=  app.getAppName() + " (required by";
    		for(App otherAppDependingOn: otherAppsDependingOn.get(app)) {
    			deps += " " + otherAppDependingOn.getAppName() + ",";
    		}
    		deps = deps.substring(0, deps.length() - 1) + ")\n";
    	}
    	deps = deps.substring(0, deps.length() - 1);
    	JTextArea textArea = new JTextArea(deps);
    	textArea.setRows(Math.min(otherAppsDependingOn.size(), 10));
		textArea.setEditable(false);
		textArea.setHighlighter(null); // disables text selection
		textArea.setBorder(null);
		textArea.setOpaque(false);
		
    	JScrollPane scrollPane = new JScrollPane(textArea);
    	scrollPane.setAlignmentX(LEFT_ALIGNMENT);
    	scrollPane.setBorder(null);
    	scrollPane.getViewport().setOpaque(false);
    	scrollPane.setOpaque(false);
    	panel.add(scrollPane);
    	panel.add(Box.createVerticalStrut(title.getPreferredSize().height));
    	JLabel message = new JLabel("Continue?");
    	message.setAlignmentX(LEFT_ALIGNMENT);
    	panel.add(message);
    	Dimension size = panel.getPreferredSize();
    	if(size.width > 600) {
    		size.width = 600;
    		panel.setPreferredSize(size);
    	}
    	int confirm = JOptionPane.showConfirmDialog(this, panel, "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    	return (confirm == JOptionPane.OK_OPTION);
    }
    
    public Map<App, Collection<App>> getOtherAppsDependingOn(Collection<App> apps) {
    	Map<App, Collection<App>> otherAppsDependingOn = new HashMap<App, Collection<App>>();
    	for(App app: apps) {
    		List<App> dependencies = new ArrayList<App>();
    		for(App installedApp: appManager.getInstalledApps())  {
    			if(!installedApp.getAppName().equalsIgnoreCase("core apps") 
    					&& !apps.contains(installedApp) && installedApp.getDependencies() != null)
	    			for (App.Dependency dep: installedApp.getDependencies()) {
	    				if(app.getAppName().equalsIgnoreCase(dep.getName()) &&
	    						WebQuerier.compareVersions(dep.getVersion(), app.getVersion()) >= 0)
	    					dependencies.add(installedApp);
	    			}
    		}
    		if(!dependencies.isEmpty())
    			otherAppsDependingOn.put(app, dependencies);
    	}
    	if(!otherAppsDependingOn.isEmpty())
    		return otherAppsDependingOn;
    	
    	return null;
    }
}
