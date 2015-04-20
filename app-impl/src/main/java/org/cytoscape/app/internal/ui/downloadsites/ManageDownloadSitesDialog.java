package org.cytoscape.app.internal.ui.downloadsites;

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

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager.DownloadSitesChangedEvent;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager.DownloadSitesChangedListener;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class ManageDownloadSitesDialog extends JDialog {

	private static final long serialVersionUID = -5333266960245441850L;
	
	private JButton addSiteButton;
    private JButton closeButton;
    private JButton editSiteButton;
    private JLabel listedSitesLabel;
    private JButton removeSiteButton;
    private JButton resetToDefaultButton;
    private JLabel siteNameLabel;
    private JTextField siteNameTextField;
    private JLabel siteUrlLabel;
    private JTextField siteUrlTextField;
    private JScrollPane sitesScrollPane;
    private JTable sitesTable;
	
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
				
//				System.out.println("Sites changed event");
				
				repopulateTable();
			}
		};
		
        DownloadSite site1 = new DownloadSite();
        site1.setSiteName("site1");
        site1.setSiteUrl("url1");
        
        DownloadSite site2 = new DownloadSite();
        site2.setSiteName("site2");
        site2.setSiteUrl("url2");
        
        DownloadSite site3 = new DownloadSite();
        site3.setSiteName("site3");
        site3.setSiteUrl("url3");
        
        downloadSitesManager.loadDownloadSites();
        
        // System.out.println("Sites loaded, count: " + downloadSitesManager.getDownloadSites().size());
        if (downloadSitesManager.getDownloadSites().size() == 0) {
        	for (DownloadSite downloadSite : WebQuerier.DEFAULT_DOWNLOAD_SITES) {
        		downloadSitesManager.addDownloadSite(downloadSite);
        	}
        
        	// System.out.println("Sites added, count: " + downloadSitesManager.getDownloadSites().size());
        	downloadSitesManager.saveDownloadSites();
        }

        downloadSitesManager.addDownloadSitesChangedListener(
        		downloadSitesChangedListener);
        
        repopulateTable();
        
        /*
        downloadSitesManager.addDownloadSite(site1);
        downloadSitesManager.addDownloadSite(site2);
        downloadSitesManager.addDownloadSite(site3);
        */
        
        setupDescriptionListener();
    }

    @SuppressWarnings("serial")
	private void initComponents() {
    	sitesScrollPane = new JScrollPane();
        sitesTable = new JTable();
        addSiteButton = new JButton();
        editSiteButton = new JButton();
        removeSiteButton = new JButton();
        siteNameLabel = new JLabel();
        siteNameTextField = new JTextField();
        siteUrlLabel = new JLabel();
        siteUrlTextField = new JTextField();
        resetToDefaultButton = new JButton();
        listedSitesLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Manage Download Sites");

        sitesTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "URL"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sitesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sitesScrollPane.setViewportView(sitesTable);
        sitesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        sitesTable.getColumnModel().getColumn(1).setPreferredWidth(285);

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

        siteNameLabel.setText("Site Name:");

        siteUrlLabel.setText("URL:");

        siteUrlTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                siteUrlTextFieldActionPerformed(evt);
            }
        });

        resetToDefaultButton.setText("Reset Sites");

        closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

        listedSitesLabel.setText("Listed sites: 0");

        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(sitesScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(siteNameLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(siteNameTextField, PREFERRED_SIZE, 119, PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(siteUrlLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(siteUrlTextField)
                    )
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addSiteButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(editSiteButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(removeSiteButton)
                        .addPreferredGap(ComponentPlacement.RELATED, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton, PREFERRED_SIZE, 79, PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(resetToDefaultButton)
                    )
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listedSitesLabel)
                        .addGap(0, 0, Short.MAX_VALUE)
                    )
                )
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(listedSitesLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(sitesScrollPane, DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(siteNameLabel)
                    .addComponent(siteNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                    .addComponent(siteUrlLabel)
                    .addComponent(siteUrlTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(editSiteButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeSiteButton)
                        .addComponent(closeButton)
                        .addComponent(resetToDefaultButton))
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(addSiteButton)
                    )
               )
        );

        pack();
        
        LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, closeButton.getAction());
        closeButton.setVisible(false);
        
    }// </editor-fold>

    private void addSiteButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	final DownloadSite downloadSite = new DownloadSite();
    	
    	String enteredSiteName = getEnteredSiteName();
    	String enteredSiteUrl = getEnteredUrl();
    	
    	downloadSite.setSiteName(enteredSiteName);
    	downloadSite.setSiteUrl(enteredSiteUrl);
    	
    	if (enteredSiteName.trim().length() == 0
    			|| enteredSiteUrl.trim().length() == 0) {
    		return;
    	}
    	
    	downloadSitesManager.addDownloadSite(downloadSite);

		downloadSitesManager.saveDownloadSites();
		
    	if (sitesTable.getModel() instanceof DefaultTableModel) {
    		
    		// Make the added site selected
    		SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					
					DefaultTableModel tableModel = (DefaultTableModel) sitesTable.getModel();
		    		
		    		Integer rowIndex = null;
		    		DownloadSite siteInTable = null;
		    		
//		    		System.out.println("Finding row index");
					
					for (int i = 0; i < tableModel.getRowCount(); i++) {
		    			Object o = tableModel.getValueAt(i, 0);
		    			
//		    			System.out.println(o);
		    			
		    			if (o instanceof DownloadSite) {
		    				siteInTable = (DownloadSite) o;
		    				
		    				if (siteInTable == downloadSite) {
		    					rowIndex = i;
//		    					System.out.println("Row index found: " + i);
		    				}
		    			}
		    		}
		    		
		    		if (rowIndex != null) {
		    		
		        		final int selectionRowIndex = rowIndex;
		    			
//						System.out.println("Setting row index: " + selectionRowIndex);
						
						sitesTable.getSelectionModel().setSelectionInterval(
								selectionRowIndex, selectionRowIndex);
					
		    		}
				}
			});
    	}
    }

    private void editSiteButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	DownloadSite downloadSite = getSelectedSite();
    	
    	if (downloadSite == null) {
    		JOptionPane.showMessageDialog(this, "Select a site to edit it");
    	} else if (isLastCopyOfDefaultSite(downloadSite)) {
    		JOptionPane.showMessageDialog(this, "That is a default site, cannot edit");
    		
    	} else {    		
    		downloadSite.setSiteName(getEnteredSiteName());
    		downloadSite.setSiteUrl(getEnteredUrl());
    		
    		// Tell the manager one of its sites changed
    		downloadSitesManager.notifyDownloadSitesChanged();

    		downloadSitesManager.saveDownloadSites();
    	}
    }

    private void removeSiteButtonActionPerformed(java.awt.event.ActionEvent evt) {
        DownloadSite downloadSite = getSelectedSite();
        
        if (downloadSite == null) {
        	JOptionPane.showMessageDialog(this, "Select a site to remove it");
        
        // If it is the last copy of a default download site, don't allow removing it
        } else if (isLastCopyOfDefaultSite(downloadSite)) {
        	JOptionPane.showMessageDialog(this, "That is a default site, cannot remove");
        	
        } else {
        	final int selectionIndex = sitesTable.getSelectedRow();
        	
        	DownloadSite siteToRemove = null;
        	
        	for (DownloadSite availableSite : downloadSitesManager.getDownloadSites()) {
        		if (availableSite.getSiteName().equals(downloadSite.getSiteName())
        			&& availableSite.getSiteUrl().equals(downloadSite.getSiteUrl())) {
        			
        			siteToRemove = availableSite;
        		}
        	}
        	
        	if (siteToRemove != null) {
        		downloadSitesManager.removeDownloadSite(siteToRemove);

        		downloadSitesManager.saveDownloadSites();
        		
        		SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						int rowCount = sitesTable.getRowCount();
						
						if (rowCount == 0) {
							return;
						} else if (selectionIndex > 0) {
							sitesTable.getSelectionModel().setSelectionInterval(
									selectionIndex - 1, selectionIndex - 1);
						} else if (selectionIndex == 0) {
							sitesTable.getSelectionModel().setSelectionInterval(0, 0);
						}
					}
				});
        		
        	}
        }
    }

    /**
     * Checks if a download site corresponds to the last copy of a default site
     * in the {@link DownloadSitesManager}.
     * @param downloadSite The site used to perform the check
     * @return Whether the site corresponds to the last copy of a default site
     * in the {@link DownloadSitesManager}
     */
    private boolean isLastCopyOfDefaultSite(DownloadSite downloadSite) {
    	boolean isDefaultSite = false;
		int copiesOfSite = 0;
		
		// Don't allow editing if it's one of default sites, and it's the last
		// copy of that default site
		for (DownloadSite defaultSite : WebQuerier.DEFAULT_DOWNLOAD_SITES) {
			if (downloadSite.sameSiteAs(defaultSite)) {
				isDefaultSite = true;
			}
		}
		
		if (isDefaultSite) {
    		for (DownloadSite registeredSite : downloadSitesManager.getDownloadSites()) {
    			if (registeredSite.sameSiteAs(downloadSite)) {
    				copiesOfSite++;
    			}
    		}
		}
		
		return (isDefaultSite && copiesOfSite <= 1);
    }
    
    private void siteUrlTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
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
		
		final List<DownloadSite> downloadSites = new LinkedList<DownloadSite>(
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
		    	
		    	sitesTable.getColumnModel().getColumn(0).setPreferredWidth(140);
		        sitesTable.getColumnModel().getColumn(1).setPreferredWidth(285);
		        
		        listedSitesLabel.setText("Listed sites: " + downloadSites.size());
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
    	} else {
    		siteNameTextField.setText("");
    		siteUrlTextField.setText("");
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