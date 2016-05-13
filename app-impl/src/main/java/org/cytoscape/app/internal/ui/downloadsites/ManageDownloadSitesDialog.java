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
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager.DownloadSitesChangedEvent;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager.DownloadSitesChangedListener;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class ManageDownloadSitesDialog extends JDialog {

	private static final long serialVersionUID = -5333266960245441850L;
	
	private JButton newButton;
    private JButton closeButton;
    private JButton saveButton;
    private JButton removeButton;
    private JLabel listedSitesLabel;
    private JLabel siteNameLabel;
    private JTextField siteNameTextField;
    private JLabel siteUrlLabel;
    private JTextField siteUrlTextField;
    private JScrollPane sitesScrollPane;
    private JTable sitesTable;
	
    private DownloadSitesManager downloadSitesManager;
    private DownloadSitesChangedListener downloadSitesChangedListener;
    

    public ManageDownloadSitesDialog(Window parent, DownloadSitesManager downloadSitesManager) {
        super(parent, ModalityType.APPLICATION_MODAL);
        
        this.downloadSitesManager = downloadSitesManager;
        initComponents();
        
        this.downloadSitesChangedListener = new DownloadSitesChangedListener() {
			@Override
			public void downloadSitesChanged(DownloadSitesChangedEvent downloadSitesChangedEvent) {
				repopulateTable();
			}
		};
		
        if (downloadSitesManager.getDownloadSites().size() == 0) {
        	for (DownloadSite downloadSite : WebQuerier.DEFAULT_DOWNLOAD_SITES) {
        		downloadSitesManager.addDownloadSite(downloadSite);
        	}
        
        	downloadSitesManager.saveDownloadSites();
        }

        downloadSitesManager.addDownloadSitesChangedListener(downloadSitesChangedListener);
        
        repopulateTable();
        setupDescriptionListener();
        update();
        updateSaveButton();
    }

    @SuppressWarnings("serial")
	private void initComponents() {
    	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Manage Download Sites");
    	
        listedSitesLabel = new JLabel("Listed sites: 0");
        sitesTable = new JTable();
    	sitesScrollPane = new JScrollPane(sitesTable);
        newButton = new JButton("New");
        saveButton = new JButton("Save");
        removeButton = new JButton("Remove");
        siteNameLabel = new JLabel("Site Name:");
        siteNameTextField = new JTextField();
        siteUrlLabel = new JLabel("URL:");
        siteUrlTextField = new JTextField();

        final DocumentListener docListener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSaveButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSaveButton();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateSaveButton();
			}
		};
        
        siteNameTextField.getDocument().addDocumentListener(docListener);
        siteUrlTextField.getDocument().addDocumentListener(docListener);
        
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

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sitesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sitesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        sitesTable.getColumnModel().getColumn(1).setPreferredWidth(285);

        newButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        saveButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        removeButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        LookAndFeelUtil.equalizeSize(newButton, removeButton, saveButton);
        
        closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

        final JPanel detailPanel = new JPanel();
        detailPanel.setBorder(LookAndFeelUtil.createPanelBorder());
        {
        	final GroupLayout layout = new GroupLayout(detailPanel);
        	detailPanel.setLayout(layout);
            layout.setAutoCreateContainerGaps(true);
            layout.setAutoCreateGaps(true);
            
            layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
            		.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(TRAILING)
									.addComponent(siteNameLabel)
									.addComponent(siteUrlLabel)
							)
							.addGroup(layout.createParallelGroup(LEADING, true)
									.addComponent(siteNameTextField)
									.addComponent(siteUrlTextField)
							)
					)
            		.addGroup(layout.createSequentialGroup()
		            		.addComponent(newButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		            		.addPreferredGap(ComponentPlacement.UNRELATED, DEFAULT_SIZE, Short.MAX_VALUE)
		            		.addComponent(removeButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(saveButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
            );
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(Alignment.CENTER, false)
    	                    .addComponent(siteNameLabel)
    	                    .addComponent(siteNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
    	             )
                    .addGroup(layout.createParallelGroup(Alignment.CENTER, false)
    	                    .addComponent(siteUrlLabel)
    	                    .addComponent(siteUrlTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
    	             )
                    .addGroup(layout.createParallelGroup(Alignment.CENTER, false)
                    		.addComponent(newButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                    		.addComponent(removeButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                    		.addComponent(saveButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                    )
            );
        }
        
        final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton);

        final JPanel contents = new JPanel();
        final GroupLayout layout = new GroupLayout(contents);
        contents.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(listedSitesLabel)
		        .addComponent(sitesScrollPane)
		        .addComponent(detailPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		        .addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(listedSitesLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addComponent(sitesScrollPane, DEFAULT_SIZE, 120, Short.MAX_VALUE)
                .addComponent(detailPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
        );

        getContentPane().add(contents);
        pack();
        
        LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, closeButton.getAction());
    }

    private void newButtonActionPerformed(ActionEvent evt) {
    	sitesTable.getSelectionModel().clearSelection();
    	siteNameTextField.requestFocusInWindow();
    }
    
    private void saveButtonActionPerformed(ActionEvent evt) {
    	DownloadSite downloadSite = getSelectedSite();
    	
    	if (downloadSite == null) {
    		// Save new site...
        	String enteredSiteName = getEnteredSiteName();
        	String enteredSiteUrl = getEnteredUrl();
        	
        	if (enteredSiteName.trim().isEmpty() || enteredSiteUrl.trim().isEmpty())
        		return;
        	
        	downloadSite = new DownloadSite();
        	downloadSite.setSiteName(enteredSiteName);
        	downloadSite.setSiteUrl(enteredSiteUrl);
        	
        	downloadSitesManager.addDownloadSite(downloadSite);
    		downloadSitesManager.saveDownloadSites();
    		
    		update();
    	} else if (isLastCopyOfDefaultSite(downloadSite)) {
    		JOptionPane.showMessageDialog(this, "That is a default site, cannot edit");
    	} else {
    		// Save existing site...
    		downloadSite.setSiteName(getEnteredSiteName());
    		downloadSite.setSiteUrl(getEnteredUrl());
    		
    		// Tell the manager one of its sites changed
    		downloadSitesManager.notifyDownloadSitesChanged();
    		downloadSitesManager.saveDownloadSites();
    	}
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
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
							sitesTable.getSelectionModel().setSelectionInterval(selectionIndex - 1, selectionIndex - 1);
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
		
		// Don't allow editing if it's one of default sites, and it's the last copy of that default site
		for (DownloadSite defaultSite : WebQuerier.DEFAULT_DOWNLOAD_SITES) {
			if (downloadSite.equals(defaultSite))
				isDefaultSite = true;
		}
		
		if (isDefaultSite) {
    		for (DownloadSite registeredSite : downloadSitesManager.getDownloadSites()) {
    			if (registeredSite.equals(downloadSite))
    				copiesOfSite++;
    		}
		}
		
		return (isDefaultSite && copiesOfSite <= 1);
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
				if (!e.getValueIsAdjusting())
					update();
			}
		});
    }
    
    private void update() {
    	final DownloadSite selectedSite = getSelectedSite();
    	final boolean isNew = selectedSite == null;
    	final boolean isDefault = selectedSite != null && isLastCopyOfDefaultSite(selectedSite);
    	
    	if (selectedSite != null) {
    		siteNameTextField.setText(selectedSite.getSiteName());
    		siteUrlTextField.setText(selectedSite.getSiteUrl());
    		saveButton.setText("Save");
    	} else {
    		siteNameTextField.setText("");
    		siteUrlTextField.setText("");
    		saveButton.setText("Add");
    	}
    	
		siteNameTextField.setEnabled(!isDefault);
		siteUrlTextField.setEnabled(!isDefault);
    	newButton.setEnabled(!isNew);
    	removeButton.setEnabled(!isNew && !isDefault);
    	saveButton.setEnabled(false);
    }
    
    private void updateSaveButton() {
    	final DownloadSite selectedSite = getSelectedSite();
    	final boolean isDefault = selectedSite != null && isLastCopyOfDefaultSite(selectedSite);
    	
    	if (isDefault)
    		saveButton.setEnabled(false);
    	else
    		saveButton.setEnabled(!getEnteredSiteName().trim().isEmpty() && !getEnteredUrl().trim().isEmpty());
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
    	
        if (selectedSites.size() > 0)
        	return selectedSites.iterator().next();
        
        return null;
    }
}
