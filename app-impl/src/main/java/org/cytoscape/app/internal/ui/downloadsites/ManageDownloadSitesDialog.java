package org.cytoscape.app.internal.ui.downloadsites;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.net.Update;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager.DownloadSitesChangedEvent;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager.DownloadSitesChangedListener;

public class ManageDownloadSitesDialog extends javax.swing.JDialog {

	private static final long serialVersionUID = -5333266960245441850L;
	
	private javax.swing.JButton addSiteButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton editSiteButton;
    private javax.swing.JButton removeSiteButton;
    private javax.swing.JButton resetToDefaultButton;
    private javax.swing.JLabel siteNameLabel;
    private javax.swing.JTextField siteNameTextField;
    private javax.swing.JLabel siteUrlLabel;
    private javax.swing.JTextField siteUrlTextField;
    private javax.swing.JScrollPane sitesScrollPane;
    private javax.swing.JTable sitesTable;
	
    private DownloadSitesManager downloadSitesManager;
    private DownloadSitesChangedListener downloadSitesChangedListener;
    

    public ManageDownloadSitesDialog(java.awt.Frame parent, 
    		boolean modal, DownloadSitesManager downloadSitesManager) {
        super(parent, modal);
        
        this.downloadSitesManager = downloadSitesManager;
        
        initComponents();
        
        this.downloadSitesChangedListener = new DownloadSitesChangedListener() {
			
			@Override
			public void downloadSitesChanged(
					DownloadSitesChangedEvent downloadSitesChangedEvent) {
				
				repopulateTable();
			}
		};
		
        downloadSitesManager.addDownloadSitesChangedListener(
        		downloadSitesChangedListener);
    }

    private void initComponents() {

    	sitesScrollPane = new javax.swing.JScrollPane();
        sitesTable = new javax.swing.JTable();
        addSiteButton = new javax.swing.JButton();
        editSiteButton = new javax.swing.JButton();
        removeSiteButton = new javax.swing.JButton();
        siteNameLabel = new javax.swing.JLabel();
        siteNameTextField = new javax.swing.JTextField();
        siteUrlLabel = new javax.swing.JLabel();
        siteUrlTextField = new javax.swing.JTextField();
        resetToDefaultButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Manage Download Sites");

        sitesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "URL"
            }
        ) {
			private static final long serialVersionUID = -6222850661401678737L;
			
			boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sitesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sitesScrollPane.setViewportView(sitesTable);
        sitesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        sitesTable.getColumnModel().getColumn(1).setPreferredWidth(185);

        addSiteButton.setText("Add");
        addSiteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSiteButtonActionPerformed(evt);
            }
        });

        editSiteButton.setText("Edit");
        editSiteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSiteButtonActionPerformed(evt);
            }
        });

        removeSiteButton.setText("Remove");
        removeSiteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSiteButtonActionPerformed(evt);
            }
        });

        siteNameLabel.setText("Site Name");

        siteUrlLabel.setText("URL");

        siteUrlTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                siteUrlTextFieldActionPerformed(evt);
            }
        });

        resetToDefaultButton.setText("Reset to Default");

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sitesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(resetToDefaultButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addSiteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editSiteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeSiteButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(siteNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(siteNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(siteUrlLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(siteUrlTextField)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(siteNameLabel)
                    .addComponent(siteNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(siteUrlLabel)
                    .addComponent(siteUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addSiteButton)
                    .addComponent(editSiteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeSiteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sitesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(resetToDefaultButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>

    private void addSiteButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void editSiteButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void removeSiteButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void siteUrlTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
    }
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }
    
    private void repopulateTable() {
    	
    	final DefaultTableModel tableModel = new DefaultTableModel(
	            new Object [][] {
	            },
	            new String [] {
	                "Name", "URL"
	            }
	        ) {
				private static final long serialVersionUID = -1712121531730828785L;
				
				boolean[] canEdit = new boolean [] {
	                false, false
	            };
	
	            public boolean isCellEditable(int rowIndex, int columnIndex) {
	                return canEdit [columnIndex];
	            }
			};
		
		List<DownloadSite> downloadSites = new LinkedList<DownloadSite>(
    			downloadSitesManager.getDownloadSites());
    	
    	// Sort by name
    	Collections.sort(downloadSites, new Comparator<DownloadSite>() {

			@Override
			public int compare(DownloadSite o1, DownloadSite o2) {
				return o1.getSiteName().compareTo(o2.getSiteName());
			}
		});
    	
    	for (DownloadSite downloadSite : downloadSites) {
    		tableModel.addRow(new Object[]{
    				downloadSite,
    				downloadSite.getSiteUrl()
    		});
    	}
    	
    	SwingUtilities.invokeLater(new Runnable() {
    		
			@Override
			public void run() {
		    	sitesTable.setModel(tableModel);
			}
    	});
    }
    
    /**
     * Setup and register a listener to the table to listen for selection changed
     * events in order to update the text fields
     */
    private void setupDescriptionListener() {
    	sitesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					updateTextFields();
				}
			}
		});
    }
    
    private void updateTextFields() {
    	DownloadSite selectedSite = getSelectedSite();
    	
    	if (selectedSite != null) {
    		siteNameTextField.setText(selectedSite.getSiteName());
    		siteUrlTextField.setText(selectedSite.getSiteUrl());
    	}
    	
    }
    
    private String getEnteredUrl() {
    	return siteUrlTextField.getText();
    }
    
    private String getEnteredSiteName() {
    	return siteNameTextField.getText();
    }
    
    private DownloadSite getSelectedSite() {
        Set<DownloadSite> selectedSites = new HashSet<DownloadSite>();
    	int[] selectedRows = sitesTable.getSelectedRows();
    	
        for (int index = 0; index < selectedRows.length; index++) {
        	
        	DownloadSite downloadSite = (DownloadSite) sitesTable.getModel().getValueAt(
        			sitesTable.convertRowIndexToModel(selectedRows[index]), 0);
        	
        	selectedSites.add(downloadSite);
        }
    	
        if (selectedSites.size() > 0) {
        	return selectedSites.iterator().next();
        } else {
        	return null;
        }
    }
}