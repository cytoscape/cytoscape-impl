package org.cytoscape.tableimport.internal.ui;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.DB_OBJECT_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationTag.TAXON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogColorTheme.ONTOLOGY_COLOR;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogFontTheme.ITEM_FONT;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogFontTheme.LABEL_FONT;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.LOCAL_SOURCE_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.REMOTE_SOURCE_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIconSets.REMOTE_SOURCE_ICON_LARGE;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.xml.bind.JAXBException;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Attribute;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.bookmark.DataSource;
import org.cytoscape.tableimport.internal.task.ImportOntologyAndAnnotationTaskFactory;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.TaskManager;
import org.jdesktop.layout.GroupLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyPanelBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(OntologyPanelBuilder.class);

	// Default server
	private static final Dimension MIN_SIZE = new Dimension(900, 600);
	
	private static final String GENE_ASSOCIATION = "gene_association";
	private static final String DEF_ANNOTATION_ITEM = "Please select an annotation data source...";
	
	
	private static final String annotationHtml = "<html><body bgcolor=\"white\"><p><strong><font size=\"+1\" face=\"serif\"><u>%DataSourceName%</u></font></strong></p><br>"
			+ "<p><em>Annotation File URL</em>: <br><font color=\"blue\">%SourceURL%</font></p><br>"
			+ "<p><em>Data Format</em>: <font color=\"green\">%Format%</font></p><br>"
			+ "<p><em>Other Information</em>:<br>"
			+ "<table width=\"300\" border=\"0\" cellspacing=\"3\" cellpadding=\"3\">"
			+ "%AttributeTable%</table></p></body></html>";

	private static final String ontologyHtml = "<html><body bgcolor=\"white\"><p><strong><font size=\"+1\" face=\"serif\"><u>%DataSourceName%</u></font></strong></p><br>"
			+ "<p><em>Data Source URL</em>: <br><font color=\"blue\">%SourceURL%</font></p><br><p><em>Description</em>:<br>"
			+ "<table width=\"300\" border=\"0\" cellspacing=\"3\" cellpadding=\"3\"><tr>"
			+ "<td rowspan=\"1\" colspan=\"1\">%Description%</td></tr></table></p></body></html>";

	private final ImportTablePanel panel;
	private final CyProperty<Bookmarks> bookmarksProp;
	private final BookmarksUtil bkUtil;
	private final InputStreamTaskFactory factory;
	private final TaskManager taskManager;

	private final CyNetworkManager manager;
	private final CyTableFactory tableFactory;
	private final CyTableManager tableManager;

	private final FileUtil fileUtil;

	OntologyPanelBuilder(final ImportTablePanel panel, final CyProperty<Bookmarks> bookmarksProp,
			final BookmarksUtil bkUtil, final TaskManager taskManager, final InputStreamTaskFactory factory,
			final CyNetworkManager manager, final CyTableFactory tableFactory, final CyTableManager tableManager,
			final FileUtil fileUtil) {
		this.panel = panel;
		this.bookmarksProp = bookmarksProp;
		this.bkUtil = bkUtil;
		this.taskManager = taskManager;
		this.factory = factory;
		this.manager = manager;
		this.tableFactory = tableFactory;
		this.tableManager = tableManager;
		this.fileUtil = fileUtil;
	}

	protected void buildPanel() {
		panel.titleIconLabel1.setIcon(REMOTE_SOURCE_ICON_LARGE.getIcon());

		panel.ontologyLabel.setFont(LABEL_FONT.getFont());
		panel.ontologyLabel.setForeground(ONTOLOGY_COLOR.getColor());
		panel.ontologyLabel.setText("Ontology");

		panel.ontologyComboBox.setFont(new java.awt.Font("SansSerif", 1, 14));
		panel.ontologyComboBox.setPreferredSize(new java.awt.Dimension(68, 25));

		final ListCellRenderer ontologyLcr = panel.ontologyComboBox.getRenderer();
		panel.ontologyComboBox.setFont(ITEM_FONT.getFont());
		panel.ontologyComboBox.setForeground(ONTOLOGY_COLOR.getColor());
		panel.ontologyComboBox.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel ontologyItem = (JLabel) ontologyLcr.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				String url = panel.ontologyUrlMap.get(value);

				if (isSelected) {
					ontologyItem.setBackground(list.getSelectionBackground());
					ontologyItem.setForeground(list.getSelectionForeground());
				} else {
					ontologyItem.setBackground(list.getBackground());
					ontologyItem.setForeground(list.getForeground());
				}

				if ((url != null) && url.startsWith("http://")) {
					ontologyItem.setIcon(REMOTE_SOURCE_ICON.getIcon());
				} else {
					ontologyItem.setIcon(LOCAL_SOURCE_ICON.getIcon());
				}

				return ontologyItem;
			}
		});

		panel.ontologyComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ontologyComboBoxActionPerformed(evt);
			}
		});

		panel.browseOntologyButton.setText("Browse");
		panel.browseOntologyButton.setToolTipText("Browse local ontology file");
		panel.browseOntologyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browseOntologyButtonActionPerformed(evt);
			}
		});

		panel.sourceLabel.setFont(LABEL_FONT.getFont());
		panel.sourceLabel.setText("Annotation");

		panel.annotationComboBox.setName("annotationComboBox");
		panel.annotationComboBox.setFont(ITEM_FONT.getFont());
		panel.annotationComboBox.setPreferredSize(new java.awt.Dimension(68, 25));

		final ListCellRenderer lcr = panel.annotationComboBox.getRenderer();
		panel.annotationComboBox.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel cmp = (JLabel) lcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				String url = panel.annotationUrlMap.get(value);

				if (isSelected) {
					cmp.setBackground(list.getSelectionBackground());
					cmp.setForeground(list.getSelectionForeground());
				} else {
					cmp.setBackground(list.getBackground());
					cmp.setForeground(list.getForeground());
				}

				if (value == null)
					cmp.setIcon(null);
				else if (value.toString().equals(DEF_ANNOTATION_ITEM)) {
					cmp.setIcon(null);
				} else if ((url != null) && url.startsWith("http://")) {
					cmp.setIcon(REMOTE_SOURCE_ICON.getIcon());
				} else {
					cmp.setIcon(LOCAL_SOURCE_ICON.getIcon());
				}

				return cmp;
			}
		});

		panel.annotationComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				annotationComboBoxActionPerformed(evt);
			}
		});

		panel.browseAnnotationButton.setText("Browse");
		panel.browseAnnotationButton.setToolTipText("Browse local annotation file...");
		panel.browseAnnotationButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browseAnnotationButtonActionPerformed(evt);
			}
		});

		GroupLayout annotationAndOntologyImportPanelLayout = new GroupLayout(panel.annotationAndOntologyImportPanel);
		panel.annotationAndOntologyImportPanel.setLayout(annotationAndOntologyImportPanelLayout);

		annotationAndOntologyImportPanelLayout.setHorizontalGroup(annotationAndOntologyImportPanelLayout
				.createParallelGroup(GroupLayout.LEADING).add(
						annotationAndOntologyImportPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.add(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.LEADING)
										.add(panel.sourceLabel).add(panel.ontologyLabel))
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
								.add(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.TRAILING)
										.add(panel.annotationComboBox, 0, 100, Short.MAX_VALUE)
										.add(panel.ontologyComboBox, 0, 100, Short.MAX_VALUE))
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
								.add(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.LEADING)
										.add(panel.browseAnnotationButton).add(panel.browseOntologyButton))
								.addContainerGap()));
		annotationAndOntologyImportPanelLayout.setVerticalGroup(annotationAndOntologyImportPanelLayout
				.createParallelGroup(GroupLayout.LEADING).add(
						annotationAndOntologyImportPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.add(annotationAndOntologyImportPanelLayout
										.createParallelGroup(GroupLayout.CENTER)
										.add(panel.sourceLabel)
										.add(panel.browseAnnotationButton)
										.add(panel.annotationComboBox, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
								.add(annotationAndOntologyImportPanelLayout
										.createParallelGroup(GroupLayout.CENTER)
										.add(panel.ontologyLabel)
										.add(panel.ontologyComboBox, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.add(panel.browseOntologyButton))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		panel.setSize(MIN_SIZE);
		panel.setMinimumSize(MIN_SIZE);
		panel.setPreferredSize(MIN_SIZE);
	}

	private void ontologyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
		panel.ontologyComboBox.setToolTipText(getOntologyTooltip());
		panel.ontologyTextField.setText(panel.ontologyComboBox.getSelectedItem().toString());
	}

	private String getOntologyTooltip() {
		final String key = panel.ontologyComboBox.getSelectedItem().toString();
		String tooltip = ontologyHtml.replace("%DataSourceName%", key);
		final String description = panel.ontologyDescriptionMap.get(key);

		if (description == null) {
			tooltip = tooltip.replace("%Description%", "N/A");
		} else {
			tooltip = tooltip.replace("%Description%", description);
		}

		if (panel.ontologyUrlMap.get(key) != null) {
			return tooltip.replace("%SourceURL%", panel.ontologyUrlMap.get(key));
		} else {
			return tooltip.replace("%SourceURL%", "N/A");
		}
	}

	private void annotationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
		if (panel.annotationComboBox.getSelectedItem().toString().equals(DEF_ANNOTATION_ITEM)) {
			panel.annotationComboBox.setToolTipText(null);
			return;
		}

		panel.annotationComboBox.setToolTipText(getAnnotationTooltip());

		try {
			final String selectedSourceName = panel.annotationComboBox.getSelectedItem().toString();
			final URL sourceURL = new URL(panel.annotationUrlMap.get(selectedSourceName));

			panel.readAnnotationForPreviewOntology(sourceURL, panel.checkDelimiter());
		} catch (IOException e) {
			logger.error("Could not create preview.", e);
		}
	}

	private String getAnnotationTooltip() {
		final String key = panel.annotationComboBox.getSelectedItem().toString();
		String tooltip = annotationHtml.replace("%DataSourceName%", key);

		if (panel.annotationUrlMap.get(key) == null) {
			return "";
		}

		tooltip = tooltip.replace("%SourceURL%", panel.annotationUrlMap.get(key));

		if (panel.annotationFormatMap.get(key) != null) {
			tooltip = tooltip.replace("%Format%", panel.annotationFormatMap.get(key));
		} else {
			String[] parts = panel.annotationUrlMap.get(key).split("/");

			if (parts[parts.length - 1].startsWith(GENE_ASSOCIATION)) {
				tooltip = tooltip.replace("%Format%", "Gene Association");
			}

			tooltip = tooltip.replace("%Format%", "General Annotation Text Table");
		}

		if (panel.annotationAttributesMap.get(key) != null) {
			StringBuffer table = new StringBuffer();
			final Map<String, String> annotations = panel.annotationAttributesMap.get(key);

			for (String anno : annotations.keySet()) {
				table.append("<tr>");
				table.append("<td><strong>" + anno + "</strong></td><td>" + annotations.get(anno) + "</td>");
				table.append("</tr>");
			}

			return tooltip.replace("%AttributeTable%", table.toString());
		}

		return tooltip.replace("%AttributeTable%", "");
	}

	private void browseAnnotationButtonActionPerformed(java.awt.event.ActionEvent evt) {
		DataSourceSelectDialog dssd = new DataSourceSelectDialog(DataSourceSelectDialog.ANNOTATION_TYPE,
				CytoscapeServices.cySwingApplication.getJFrame(), Dialog.ModalityType.APPLICATION_MODAL, fileUtil);
		dssd.setLocationRelativeTo(CytoscapeServices.cySwingApplication.getJFrame());
		dssd.setVisible(true);

		String key = dssd.getSourceName();

		if (key != null) {
			panel.annotationComboBox.addItem(key);
			panel.annotationUrlMap.put(key, dssd.getSourceUrlString());
			panel.annotationComboBox.setSelectedItem(key);
			panel.annotationComboBox.setToolTipText(getAnnotationTooltip());
		}
	}

	private void browseOntologyButtonActionPerformed(java.awt.event.ActionEvent evt) {
		DataSourceSelectDialog dssd = new DataSourceSelectDialog(DataSourceSelectDialog.ONTOLOGY_TYPE,
				CytoscapeServices.cySwingApplication.getJFrame(), Dialog.ModalityType.APPLICATION_MODAL, fileUtil);
		dssd.setLocationRelativeTo(CytoscapeServices.cySwingApplication.getJFrame());
		dssd.setVisible(true);

		String key = dssd.getSourceName();

		if (key != null) {
			panel.ontologyComboBox.insertItemAt(key, 0);
			panel.ontologyUrlMap.put(key, dssd.getSourceUrlString());
			panel.ontologyComboBox.setSelectedItem(key);
			panel.ontologyComboBox.setToolTipText(getOntologyTooltip());
		}
	}

	protected void setOntologyComboBox() {
		final Bookmarks bookmarks = bookmarksProp.getProperties();
		final List<DataSource> annotations = bkUtil.getDataSourceList("ontology", bookmarks.getCategory());
		String key = null;

		for (DataSource source : annotations) {
			key = source.getName();
			panel.ontologyComboBox.addItem(key);
			panel.ontologyUrlMap.put(key, source.getHref());
			panel.ontologyDescriptionMap.put(key, bkUtil.getAttribute(source, "description"));
			panel.ontologyTypeMap.put(key, bkUtil.getAttribute(source, "ontologyType"));
		}

		panel.ontologyComboBox.setToolTipText(getOntologyTooltip());
	}

	protected void setAnnotationComboBox() throws JAXBException, IOException {
		final Bookmarks bookmarks = bookmarksProp.getProperties();
		final List<DataSource> annotations = bkUtil.getDataSourceList("annotation", bookmarks.getCategory());
		String key = null;

		panel.annotationComboBox.addItem(DEF_ANNOTATION_ITEM);

		for (DataSource source : annotations) {
			key = source.getName();
			panel.annotationComboBox.addItem(key);
			panel.annotationUrlMap.put(key, source.getHref());
			panel.annotationFormatMap.put(key, source.getFormat());

			final Map<String, String> attrMap = new HashMap<String, String>();

			for (Attribute attr : source.getAttribute())
				attrMap.put(attr.getName(), attr.getContent());

			panel.annotationAttributesMap.put(key, attrMap);
		}

		panel.annotationComboBox.setToolTipText(getAnnotationTooltip());
	}

	protected void buildAnnotationPanel() {
		panel.ontology2annotationPanel.setBackground(new java.awt.Color(250, 250, 250));
		panel.ontology2annotationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
				new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED),
				"Annotation File to Ontology Mapping", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11)));
		panel.targetOntologyLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
		panel.targetOntologyLabel.setForeground(new java.awt.Color(73, 127, 235));
		panel.targetOntologyLabel.setText("Ontology");

		panel.ontologyTextField.setFont(new java.awt.Font("SansSerif", 1, 14));
		panel.ontologyTextField.setForeground(ONTOLOGY_COLOR.getColor());
		panel.ontologyTextField.setBackground(Color.WHITE);
		panel.ontologyTextField.setEditable(false);
		panel.ontologyTextField.setToolTipText("This ontology will be used for mapping.");

		panel.ontologyInAnnotationLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
		panel.ontologyInAnnotationLabel.setForeground(new java.awt.Color(0, 255, 255));
		panel.ontologyInAnnotationLabel.setText("Key Column in Annotation File");

		panel.ontologyInAnnotationComboBox.setForeground(ONTOLOGY_COLOR.getColor());
		panel.ontologyInAnnotationComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ontologyInAnnotationComboBoxActionPerformed(evt);
			}
		});

		panel.arrowButton2.setBackground(new java.awt.Color(250, 250, 250));
		panel.arrowButton2.setIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource(
				"images/ximian/stock_right-16.png")));
		panel.arrowButton2.setBorder(null);
		panel.arrowButton2.setBorderPainted(false);

		GroupLayout ontology2annotationPanelLayout = new GroupLayout(panel.ontology2annotationPanel);
		panel.ontology2annotationPanel.setLayout(ontology2annotationPanelLayout);
		ontology2annotationPanelLayout.setHorizontalGroup(ontology2annotationPanelLayout.createParallelGroup(
				GroupLayout.LEADING).add(
				ontology2annotationPanelLayout
						.createSequentialGroup()
						.addContainerGap()
						.add(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
								.add(panel.ontologyInAnnotationLabel)
								.add(panel.ontologyInAnnotationComboBox, 0, 100, Short.MAX_VALUE))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(panel.arrowButton2)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.TRAILING)
								.add(GroupLayout.LEADING, panel.targetOntologyLabel)
								.add(panel.ontologyTextField, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
						.addContainerGap()));
		ontology2annotationPanelLayout.setVerticalGroup(ontology2annotationPanelLayout.createParallelGroup(
				GroupLayout.LEADING).add(
				ontology2annotationPanelLayout
						.createSequentialGroup()
						.add(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.BASELINE)
								.add(panel.ontologyInAnnotationLabel).add(panel.targetOntologyLabel))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(ontology2annotationPanelLayout
								.createParallelGroup(GroupLayout.BASELINE)
								.add(panel.ontologyInAnnotationComboBox, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.add(panel.ontologyTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE).add(panel.arrowButton2))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		// Disable unnecessary components
		panel.advancedOptionCheckBox.setEnabled(false);
		panel.textImportCheckBox.setEnabled(false);
		panel.importAllCheckBox.setSelected(true);
		panel.importAllCheckBox.setEnabled(false);
	}

	private void ontologyInAnnotationComboBoxActionPerformed(ActionEvent evt) {

		final int ontologyCol = panel.ontologyInAnnotationComboBox.getSelectedIndex();
		final List<Integer> gaAlias = new ArrayList<Integer>();
		gaAlias.add(DB_OBJECT_SYNONYM.getPosition());
		panel.previewPanel.getPreviewTable().setDefaultRenderer(
				Object.class,
				new AttributePreviewTableCellRenderer(panel.keyInFile, gaAlias, ontologyCol, TAXON.getPosition(),
						panel.importFlag, panel.listDelimiter));
		panel.previewPanel.repaint();
	}

	/**
	 * Create task for ontology reader and run the task.<br>
	 * 
	 * @param dataSource
	 * @param ontologyName
	 * @throws IOException
	 */
	private void loadOntology(final String dataSource, final String ontologyName, final String annotationSource)
			throws IOException {

		final URL url = new URL(dataSource);
		final URL annotationSourceUrl = new URL(annotationSource);

		InputStream is = null;
		if(annotationSourceUrl.toString().endsWith("gz")) {
			is = new GZIPInputStream(annotationSourceUrl.openStream());
		} else {
			is = annotationSourceUrl.openStream();
		}
		
		ImportOntologyAndAnnotationTaskFactory taskFactory = new ImportOntologyAndAnnotationTaskFactory(manager,
				factory, url.openStream(), ontologyName, tableFactory, is, annotationSource, tableManager);
		taskManager.execute(taskFactory.createTaskIterator());
	}

	protected void importOntologyAndAnnotation() throws IOException {

		final String selectedOntologyName = panel.ontologyComboBox.getSelectedItem().toString();
		final String ontologySourceLocation = panel.ontologyUrlMap.get(selectedOntologyName);
		final String annotationSource = panel.annotationUrlMap.get(panel.annotationComboBox.getSelectedItem());

		loadOntology(ontologySourceLocation, selectedOntologyName, annotationSource);
	}
}
