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
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.cytoscape.app.internal.event.AppsChangedEvent;
import org.cytoscape.app.internal.event.AppsChangedListener;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.ResultsFilterer;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.net.WebQuerier.AppTag;
import org.cytoscape.app.internal.task.InstallAppsFromFileTask;
import org.cytoscape.app.internal.task.InstallAppsFromWebAppTask;
import org.cytoscape.app.internal.task.ShowInstalledAppsTask;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSite;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager.DownloadSitesChangedEvent;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager.DownloadSitesChangedListener;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the panel in the App Manager dialog's tab used for installing new apps.
 */
@SuppressWarnings("serial")
public class InstallAppsPanel extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
    private JPanel descriptionPanel;
    private JScrollPane descriptionScrollPane;
    private JSplitPane descriptionSplitPane;
    private JTextPane descriptionTextPane;
    private JComboBox downloadSiteComboBox;
    private JLabel downloadSiteLabel;
    private JTextField filterTextField;
    private JButton installButton;
    private JButton installFromFileButton;
    private JButton manageSitesButton;
    private JScrollPane resultsScrollPane;
    private JTree resultsTree;
    private JLabel searchAppsLabel;
    private JScrollPane tagsScrollPane;
    private JSplitPane tagsSplitPane;
    private JTree tagsTree;
    private JButton viewOnAppStoreButton;
	
	private JFileChooser fileChooser;
	
	private AppManager appManager;
	private DownloadSitesManager downloadSitesManager;
	private FileUtil fileUtil;
	private TaskManager taskManager;
	private Container parent;
	
	private WebApp selectedApp;
	private WebQuerier.AppTag currentSelectedAppTag;
	
	private Set<WebApp> resultsTreeApps;
	
    public InstallAppsPanel(
    		final AppManager appManager, 
    		final DownloadSitesManager downloadSitesManager, 
    		final FileUtil fileUtil,
    		final TaskManager taskManager,
    		final Container parent
    ) {
        this.appManager = appManager;
        this.downloadSitesManager = downloadSitesManager;
        this.fileUtil = fileUtil;
        this.taskManager = taskManager;
        this.parent = parent;
    	initComponents();
        
    	tagsTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				updateResultsTree();
				updateDescriptionBox();
			}
		});
    	
		resultsTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				updateDescriptionBox();
			}
		});
		
		setupTextFieldListener();
    	setupDownloadSitesChangedListener();
    	
		//queryForApps();
		
		appManager.addAppListener(new AppsChangedListener() {
			@Override
			public void appsChanged(AppsChangedEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						TreePath[] selectionPaths = resultsTree.getSelectionPaths();
						updateDescriptionBox();
						fillResultsTree(resultsTreeApps);
						resultsTree.setSelectionPaths(selectionPaths);
					}
				});
			}
		});

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                queryForApps();
            }
        });
    }
    
    private void setupDownloadSitesChangedListener() {
    	downloadSitesManager.addDownloadSitesChangedListener(new DownloadSitesChangedListener() {
			
			@Override
			public void downloadSitesChanged(DownloadSitesChangedEvent downloadSitesChangedEvent) {
				final DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel(
						new Vector<DownloadSite>(downloadSitesManager.getDownloadSites()));
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						downloadSiteComboBox.setModel(defaultComboBoxModel);
					}
				});
			}
		});
    }

    private boolean hasTagTreeBeenPopulated = false;
    
    // Queries the currently set app store url for available apps.
    private void queryForApps() {
        if (hasTagTreeBeenPopulated)
            return;

        final WebQuerier webQuerier = appManager.getWebQuerier();

    	taskManager.execute(new TaskIterator(new Task() {
			
			// Obtain information for all available apps, then append tag information
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
		    	
				taskMonitor.setTitle("Getting available apps");
				taskMonitor.setStatusMessage("Obtaining apps from: " 
						+ webQuerier.getCurrentAppStoreUrl());
				
				Set<WebApp> availableApps = webQuerier.getAllApps();
                if (availableApps == null)
                    return;
			
				// Once the information is obtained, update the tree
				
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						// populateTree(appManager.getWebQuerier().getAllApps());
						buildTagsTree();
						
						fillResultsTree(appManager.getWebQuerier().getAllApps());
					}
					
				});
			}

			@Override
			public void cancel() {
			}
		}));
    }

    private void initComponents() {
    	searchAppsLabel = new JLabel("Search:");
        installFromFileButton = new JButton("Install from File...");
        filterTextField = new JTextField();
        descriptionSplitPane = new JSplitPane();
        tagsSplitPane = new JSplitPane();
        tagsScrollPane = new JScrollPane();
        tagsTree = new JTree();
        resultsScrollPane = new JScrollPane();
        resultsTree = new JTree();
        descriptionPanel = new JPanel();
        descriptionScrollPane = new JScrollPane();
        descriptionTextPane = new JTextPane();
        viewOnAppStoreButton = new JButton("View on App Store");
        installButton = new JButton("Install");
        downloadSiteLabel = new JLabel("Download Site:");
        downloadSiteComboBox = new JComboBox();
        manageSitesButton = new JButton("Manage Sites...");

        searchAppsLabel.setVisible(!LookAndFeelUtil.isAquaLAF());
        filterTextField.putClientProperty("JTextField.variant", "search"); // Aqua LAF only
        filterTextField.setToolTipText("To search, start typing");
        
        installFromFileButton.addActionListener(evt -> installFromFileButtonActionPerformed(evt));

        descriptionSplitPane.setBorder(null);
        descriptionSplitPane.setDividerLocation(390);

        tagsSplitPane.setDividerLocation(175);
        tagsSplitPane.setBorder(null);

        DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode treeNode2 = new DefaultMutableTreeNode("all apps (0)");
        DefaultMutableTreeNode treeNode3 = new DefaultMutableTreeNode("collections (0)");
        DefaultMutableTreeNode treeNode4 = new DefaultMutableTreeNode("apps by tag");
        treeNode1.add(treeNode2);
        treeNode1.add(treeNode3);
        treeNode1.add(treeNode4);
        tagsTree.setModel(new DefaultTreeModel(treeNode1));
        tagsTree.setFocusable(false);
        tagsTree.setRootVisible(false);
        tagsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tagsScrollPane.setViewportView(tagsTree);

        tagsSplitPane.setLeftComponent(tagsScrollPane);

        treeNode1 = new DefaultMutableTreeNode("root");
        resultsTree.setModel(new DefaultTreeModel(treeNode1));
        resultsTree.setFocusable(false);
        resultsTree.setRootVisible(false);
        resultsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        resultsScrollPane.setViewportView(resultsTree);

        tagsSplitPane.setRightComponent(resultsScrollPane);

        descriptionSplitPane.setLeftComponent(tagsSplitPane);

        descriptionTextPane.setContentType("text/html");
        descriptionTextPane.setEditable(false);
        //descriptionTextPane.setText("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\">\n      App description is displayed here.\n    </p>\n  </body>\n</html>\n");
        descriptionTextPane.setText("");
        descriptionScrollPane.setViewportView(descriptionTextPane);

        final GroupLayout descriptionPanelLayout = new GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        
        descriptionPanelLayout.setHorizontalGroup(descriptionPanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(descriptionScrollPane, GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
        );
        descriptionPanelLayout.setVerticalGroup(descriptionPanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(descriptionScrollPane, GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
        );

        descriptionSplitPane.setRightComponent(descriptionPanel);

        viewOnAppStoreButton.setEnabled(false);
        viewOnAppStoreButton.addActionListener(evt -> viewOnAppStoreButtonActionPerformed(evt));

        installButton.setEnabled(false);
        installButton.addActionListener(evt -> installButtonActionPerformed(evt));

        downloadSiteComboBox.setModel(new DefaultComboBoxModel(new String[] { WebQuerier.DEFAULT_APP_STORE_URL }));
        downloadSiteComboBox.addItemListener(evt -> downloadSiteComboBoxItemStateChanged(evt));
        downloadSiteComboBox.addActionListener(evt -> downloadSiteComboBoxActionPerformed(evt));
        downloadSiteComboBox.addKeyListener(new KeyAdapter() {
        	@Override
            public void keyPressed(KeyEvent evt) {
                downloadSiteComboBoxKeyPressed(evt);
            }
        });

        manageSitesButton.addActionListener(evt -> manageSitesButtonActionPerformed(evt));
        
        LookAndFeelUtil.equalizeSize(installFromFileButton, viewOnAppStoreButton, installButton);
        
        final JSeparator sep = new JSeparator();

        final GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(downloadSiteLabel)
                        .addComponent(downloadSiteComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(manageSitesButton)
                )
                .addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(searchAppsLabel)
                        .addComponent(filterTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                )
                .addComponent(descriptionSplitPane)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(installFromFileButton)
                        .addPreferredGap(ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                        .addComponent(viewOnAppStoreButton)
                        .addComponent(installButton)
                )
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
	                    .addComponent(downloadSiteLabel)
	                    .addComponent(downloadSiteComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
	                    .addComponent(manageSitesButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                )
                .addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
	                    .addComponent(searchAppsLabel)
	                    .addComponent(filterTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                )
                .addComponent(descriptionSplitPane, DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
	                    .addComponent(installFromFileButton)
	                    .addComponent(viewOnAppStoreButton)
	                    .addComponent(installButton)
                )
        );

        // Add a key listener to the download site combo box to listen for the enter key event
        final WebQuerier webQuerier = this.appManager.getWebQuerier();
        
        downloadSiteComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				final ComboBoxEditor editor = downloadSiteComboBox.getEditor();
		    	final Object selectedValue = editor.getItem();
				
				if (e.isActionKey() || e.getKeyCode() == KeyEvent.VK_ENTER) {
			    	if (downloadSiteComboBox.getModel() instanceof DefaultComboBoxModel
			    			&& selectedValue != null) {
			    		final DefaultComboBoxModel comboBoxModel = (DefaultComboBoxModel) downloadSiteComboBox.getModel();
			    	
			    		SwingUtilities.invokeLater(new Runnable() {
			    			
			    			@Override
			    			public void run() {
				    			boolean selectedAlreadyInList = false;
				    	    	
				        		for (int i = 0; i < comboBoxModel.getSize(); i++) {
				        			Object listElement = comboBoxModel.getElementAt(i);
				        			
				        			if (listElement.equals(selectedValue)) {
				        				selectedAlreadyInList = true;
				        				
				        				break;	
				        			}
				        		}
				        		
				        		if (!selectedAlreadyInList) {
				        			comboBoxModel.insertElementAt(selectedValue, 1);
				        			
				        			editor.setItem(selectedValue);
				        		}
			    			}
			    			
			    		});
			    	}
			    	
			    	if (webQuerier.getCurrentAppStoreUrl() != selectedValue.toString()) {
			    		webQuerier.setCurrentAppStoreUrl(selectedValue.toString());
			    		
			    		queryForApps();
			    	}
				}
			}
		});
        
        // Make the JTextPane render HTML using the default UI font
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument) descriptionTextPane.getDocument()).getStyleSheet().addRule(bodyRule);
        
        // Setup the TreeCellRenderer to make the app tags use the folder icon instead of the default leaf icon, 
        // and have it use the opened folder icon when selected
    	DefaultTreeCellRenderer tagsTreeCellRenderer = new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
					boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
				
				// Make leaves use the open folder icon when selected
				if (selected && leaf)
					this.setIcon(getOpenIcon());
				
				return this;
			}
    	};
    	
		tagsTreeCellRenderer.setLeafIcon(tagsTreeCellRenderer.getDefaultClosedIcon());
		tagsTree.setCellRenderer(tagsTreeCellRenderer);
    }
    
    @Override
    public void addNotify() {
    	super.addNotify();
    	
    	if (filterTextField != null)
    		filterTextField.requestFocusInWindow();
    }
    
	private void installFromFileButtonActionPerformed(ActionEvent evt) {
		// Setup a the file filter for the open file dialog
		FileChooserFilter fileChooserFilter = new FileChooserFilter(
				"Jar, Zip, and Karaf Kar Files (*.jar, *.zip, *.kar)", new String[] { "jar", "zip", "kar" });

		Collection<FileChooserFilter> fileChooserFilters = new LinkedList<FileChooserFilter>();
		fileChooserFilters.add(fileChooserFilter);

		// Show the dialog
		final File[] files = fileUtil.getFiles(parent, "Choose file(s)", FileUtil.LOAD, FileUtil.LAST_DIRECTORY,
				"Install", true, fileChooserFilters);

		if (files != null) {
			TaskIterator ti = new TaskIterator();
			ti.append(new InstallAppsFromFileTask(Arrays.asList(files), appManager, true));
			ti.append(new ShowInstalledAppsTask(parent));
			taskManager.setExecutionContext(parent);
			taskManager.execute(ti);
		}
	}
	
    /**
     * Attempts to insert newlines into a given string such that each line has no 
     * more than the specified number of characters.
     */
    private String splitIntoLines(String text, int charsPerLine) {
    	return null;
    }

    private void setupTextFieldListener() {
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
        	
        	ResultsFilterer resultsFilterer = new ResultsFilterer();
        	
        	private void showFilteredApps() {
        		SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						
						tagsTree.clearSelection();
						
						fillResultsTree(resultsFilterer.findMatches(filterTextField.getText(), 
								appManager.getWebQuerier().getAllApps()));
					}
					
				});
        	}
        	
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (filterTextField.getText().length() != 0) {
					showFilteredApps();
				}
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				showFilteredApps();
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
    }
    
    private void installButtonActionPerformed(ActionEvent evt) {
    	final WebQuerier webQuerier = appManager.getWebQuerier();
    	taskManager.setExecutionContext(parent);
		taskManager.execute(new TaskIterator(new InstallAppsFromWebAppTask(Collections.singletonList(selectedApp), appManager, true)));
    }
    
    private void buildTagsTree() {
    	WebQuerier webQuerier = appManager.getWebQuerier();
    	
    	// Get all available apps and tags
    	Set<WebApp> availableApps = webQuerier.getAllApps();
    	Set<WebQuerier.AppTag> availableTags = webQuerier.getAllTags();
    	if(availableApps == null || availableTags == null)
    		return;
    	
    	List<WebQuerier.AppTag> sortedTags = new LinkedList<WebQuerier.AppTag>(availableTags);
    	
    	Collections.sort(sortedTags, new Comparator<WebQuerier.AppTag>() {

			@Override
			public int compare(AppTag tag, AppTag other) {
				return other.getCount() - tag.getCount();
			}
    	});
    	
    	
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    	
    	DefaultMutableTreeNode allAppsTreeNode = new DefaultMutableTreeNode("all apps" 
    			+ " (" + availableApps.size() + ")");
    	root.add(allAppsTreeNode);
    	
    	DefaultMutableTreeNode collectionsTreeNode = new DefaultMutableTreeNode("collections (0)");
    	
    	DefaultMutableTreeNode appsByTagTreeNode = new DefaultMutableTreeNode("apps by tag");
    	
    	for (final WebQuerier.AppTag appTag : sortedTags) {
    		if(appTag.getName().equals("collections"))
    			collectionsTreeNode.setUserObject(appTag);
    		else
    			appsByTagTreeNode.add(new DefaultMutableTreeNode(appTag));
    	}

    	root.add(collectionsTreeNode);
    	root.add(appsByTagTreeNode);
    	
    	tagsTree.setModel(new DefaultTreeModel(root));
    	// tagsTree.expandRow(2);
    	
    	currentSelectedAppTag = null;
        hasTagTreeBeenPopulated = true;
    }
 
    private void updateResultsTree() {
    	// bild tags tree if it hasn't been populated
    	if(!hasTagTreeBeenPopulated)
    		buildTagsTree();
    	
    	TreePath selectionPath = tagsTree.getSelectionPath();
    	
//    	DebugHelper.print(String.valueOf(selectedNode.getUserObject()));
    	currentSelectedAppTag = null;
    	
    	if (selectionPath != null) {
    		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        	
	    	// Check if the "all apps" node is selected
	    	if (selectedNode.getLevel() == 1 
	    			&& String.valueOf(selectedNode.getUserObject()).startsWith("all apps")) {
	    		fillResultsTree(appManager.getWebQuerier().getAllApps());
	    		
	    	} else if (selectedNode.getUserObject() instanceof WebQuerier.AppTag) {
	    		WebQuerier.AppTag selectedTag = (WebQuerier.AppTag) selectedNode.getUserObject();
	    		
	    		fillResultsTree(appManager.getWebQuerier().getAppsByTag(selectedTag.getName()));
	    		currentSelectedAppTag = selectedTag;
	    	} else {
	    		// Clear tree
	    		resultsTree.setModel(new DefaultTreeModel(null));	    		
	    	}
    	} else {
    		fillResultsTree(appManager.getWebQuerier().getAllApps());
//    		System.out.println("selection path null, not updating results tree");
    	}
    }
    
    private void fillResultsTree(Set<WebApp> webApps) {
    	if(webApps == null) {
    		resultsTree.setModel(new DefaultTreeModel(null));
    		resultsTreeApps = new HashSet<WebApp>();
    		return;
    	}
    	
    	appManager.getWebQuerier().checkWebAppInstallStatus(webApps, appManager);
    	List<WebApp> sortedApps = new LinkedList<WebApp>(webApps);
    	
    	// Sort apps by alphabetical order
    	Collections.sort(sortedApps, new Comparator<WebApp>() {
			@Override
			public int compare(WebApp webApp, WebApp other) {
				
				return (webApp.getName().compareToIgnoreCase(other.getName()));
			}
    	});
    	
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    	
    	DefaultMutableTreeNode treeNode;
    	for (WebApp webApp : sortedApps) {
    		if (webApp.getCorrespondingApp() != null
    				&& webApp.getCorrespondingApp().getStatus() == AppStatus.INSTALLED) {
    			webApp.setAppListDisplayName(webApp.getFullName() + " (Installed)");
    		} else {
    			webApp.setAppListDisplayName(webApp.getFullName());
    		}

    		treeNode = new DefaultMutableTreeNode(webApp);
    		root.add(treeNode);
    	}
    	
    	resultsTree.setModel(new DefaultTreeModel(root));
    	
    	resultsTreeApps = new HashSet<WebApp>(webApps);
  
    }
    
    private void updateDescriptionBox() {
    	
    	TreePath selectedPath = resultsTree.getSelectionPath();
    	
    	if (selectedPath != null) {
    		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) resultsTree.getSelectionPath().getLastPathComponent();
    		WebApp selectedApp = (WebApp) selectedNode.getUserObject();
        	
    		boolean appAlreadyInstalled = (selectedApp.getCorrespondingApp() != null
    				&& selectedApp.getCorrespondingApp().getStatus() == AppStatus.INSTALLED);
    		
    		String text = "";
    		
    		// text += "<html> <head> </head> <body hspace=\"4\" vspace=\"4\">";
    		text += "<html> <body hspace=\"4\" vspace=\"2\">";
    		
    		// App hyperlink to web store page
    		// text += "<p style=\"margin-top: 0\"> <a href=\"" + selectedApp.getPageUrl() + "\">" + selectedApp.getPageUrl() + "</a> </p>";
    		
    		// App name, version
    		text += "<b>" + selectedApp.getFullName() + "</b>";
    		String latestReleaseVersion = selectedApp.getReleases().get(selectedApp.getReleases().size() - 1).getReleaseVersion();
    		text += "<br />" + latestReleaseVersion;
    		
    		if (appAlreadyInstalled) {
    			if (!selectedApp.getCorrespondingApp().getVersion().equalsIgnoreCase(latestReleaseVersion)) {
    				text += " (installed: " + selectedApp.getCorrespondingApp().getVersion() + ")";
    			}
    		}
    		
    		/*
    		text += "<p>";
    		text += "<b>" + selectedApp.getFullName() + "</b>";
    		text += "<br />" + selectedApp.getReleases().get(selectedApp.getReleases().size() - 1).getReleaseVersion();
    		text += "</p>";
    		*/
    		text += "<p>";
    		
    		// App image
    		text += "<img border=\"0\" ";
    		text += "src=\"" + appManager.getWebQuerier().getDefaultAppStoreUrl() 
    			+ selectedApp.getIconUrl() + "\" alt=\"" + selectedApp.getFullName() + "\"/>";
    		
    		text += "</p>";
    		
    		
    		// App description
    		text += "<p>";
    		text += (String.valueOf(selectedApp.getDescription()).equalsIgnoreCase("null") ? "App description not found." : selectedApp.getDescription());
    		text += "</p>";
    		text += "</body> </html>";
    		descriptionTextPane.setText(text);
    		
    		this.selectedApp = selectedApp;
    		
    		if (appAlreadyInstalled) {
    			installButton.setEnabled(false);
    		} else {
    			installButton.setEnabled(true);
    		}
    		
    		viewOnAppStoreButton.setEnabled(true);
		
    	} else {
    		
    		//descriptionTextPane.setText("App description is displayed here.");
    		descriptionTextPane.setText("");
    		
    		this.selectedApp = null;
    		
    		installButton.setEnabled(false);
    		viewOnAppStoreButton.setEnabled(false);
    	}
    }

    private void resetButtonActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void viewOnAppStoreButtonActionPerformed(ActionEvent evt) {
    	if (selectedApp == null) {
    		return;
    	}
    	
    	if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			
			try {
				desktop.browse((new URL(selectedApp.getPageUrl())).toURI());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    private void downloadSiteComboBoxItemStateChanged(ItemEvent evt) {
    }

    private void downloadSiteComboBoxActionPerformed(ActionEvent evt) {
    	
    	final Object selected = downloadSiteComboBox.getSelectedItem();
    	
    	if (downloadSiteComboBox.getModel() instanceof DefaultComboBoxModel
    			&& selected != null) {
    		final DefaultComboBoxModel comboBoxModel = (DefaultComboBoxModel) downloadSiteComboBox.getModel();
    	
    		SwingUtilities.invokeLater(new Runnable() {
    			
    			@Override
    			public void run() {
	    			boolean selectedAlreadyInList = false;
	    	    	
	        		for (int i = 0; i < comboBoxModel.getSize(); i++) {
	        			Object listElement = comboBoxModel.getElementAt(i);
	        			
	        			if (listElement.equals(selected)) {
	        				selectedAlreadyInList = true;
	        				
	        				if (i > 0) {
	        					// comboBoxModel.removeElementAt(i);
	        					// comboBoxModel.insertElementAt(listElement, 1);
	        				}
	        				
	        				break;
	        			}
	        		}
	        		
	        		if (!selectedAlreadyInList) {
	        			comboBoxModel.insertElementAt(selected, 1);
	        		}
    			}
    			
    		});
    		
    		if (appManager.getWebQuerier().getCurrentAppStoreUrl() != selected.toString()) {
    			appManager.getWebQuerier().setCurrentAppStoreUrl(selected.toString());
	    		
	    		queryForApps();
	    	}
    	}
   
    }
    
    private void downloadSiteComboBoxKeyPressed(KeyEvent evt) {
    }
    
    private void manageSitesButtonActionPerformed(ActionEvent evt) {
        if (parent instanceof AppManagerDialog) {
        	((AppManagerDialog) parent).showManageDownloadSitesDialog();
        }
    }
}
