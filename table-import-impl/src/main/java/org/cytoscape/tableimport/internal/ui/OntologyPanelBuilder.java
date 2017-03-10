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

import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIcons.LOCAL_SOURCE_ICON;
import static org.cytoscape.tableimport.internal.ui.theme.ImportDialogIcons.REMOTE_SOURCE_ICON;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.xml.bind.JAXBException;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Attribute;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.bookmark.DataSource;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.task.ImportOntologyAndAnnotationTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyPanelBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(OntologyPanelBuilder.class);

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
	private final InputStreamTaskFactory isTaskFactory;
	private final CyServiceRegistrar serviceRegistrar;
	private Map<String, String> annotationUrlMap;
	private Map<String, String> annotationFormatMap;
	private Map<String, Map<String, String>> annotationAttributesMap;
	private Map<String, String> ontologyUrlMap;
	private Map<String, String> ontologyTypeMap;
	private Map<String, String> ontologyDescriptionMap;

	OntologyPanelBuilder(
			final ImportTablePanel panel,
			final InputStreamTaskFactory isTaskFactory,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.panel = panel;
		this.isTaskFactory = isTaskFactory;
		this.serviceRegistrar = serviceRegistrar;
		annotationUrlMap = new HashMap<>();
		annotationFormatMap = new HashMap<>();
		annotationAttributesMap = new HashMap<>();

		ontologyUrlMap = new HashMap<>();
		ontologyDescriptionMap = new HashMap<>();
		ontologyTypeMap = new HashMap<>();
	}

	protected void buildPanel() {
		panel.getAdvancedButton().setEnabled(false);

		final ListCellRenderer ontologyLcr = panel.ontologyComboBox.getRenderer();
		panel.ontologyComboBox.setRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel ontologyItem = (JLabel) ontologyLcr.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				String url = ontologyUrlMap.get(value);

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

		panel.ontologyComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ontologyComboBoxActionPerformed(evt);
			}
		});

		panel.browseOntologyButton.setToolTipText("Browse local ontology file...");
		panel.browseOntologyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				browseOntologyButtonActionPerformed(evt);
			}
		});

		panel.annotationComboBox.setName("annotationComboBox");

		final ListCellRenderer lcr = panel.annotationComboBox.getRenderer();
		panel.annotationComboBox.setRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel cmp = (JLabel) lcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				String url = annotationUrlMap.get(value);

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

		panel.annotationComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				annotationComboBoxActionPerformed(evt);
			}
		});

		panel.browseAnnotationButton.setToolTipText("Browse local annotation file...");
		panel.browseAnnotationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				browseAnnotationButtonActionPerformed(evt);
			}
		});
	}

	private void ontologyComboBoxActionPerformed(ActionEvent evt) {
		panel.ontologyComboBox.setToolTipText(getOntologyTooltip());
	}

	private String getOntologyTooltip() {
		final String key = panel.ontologyComboBox.getSelectedItem().toString();
		String tooltip = ontologyHtml.replace("%DataSourceName%", key);
		final String description = ontologyDescriptionMap.get(key);

		if (description == null) {
			tooltip = tooltip.replace("%Description%", "N/A");
		} else {
			tooltip = tooltip.replace("%Description%", description);
		}

		if (ontologyUrlMap.get(key) != null) {
			return tooltip.replace("%SourceURL%", ontologyUrlMap.get(key));
		} else {
			return tooltip.replace("%SourceURL%", "N/A");
		}
	}

	private void annotationComboBoxActionPerformed(ActionEvent evt) {
		if (panel.annotationComboBox.getSelectedItem().toString().equals(DEF_ANNOTATION_ITEM)) {
			panel.annotationComboBox.setToolTipText(null);
			panel.getAdvancedButton().setEnabled(false);
			
			return;
		}

		panel.annotationComboBox.setToolTipText(getAnnotationTooltip());
		panel.getAdvancedButton().setEnabled(true);

		try {
			final String selectedSourceName = panel.annotationComboBox.getSelectedItem().toString();
			final URL sourceURL = new URL(annotationUrlMap.get(selectedSourceName));

			panel.readAnnotationForPreviewOntology(sourceURL, panel.checkDelimiter());
		} catch (IOException e) {
			logger.error("Could not create preview.", e);
		}
	}

	private String getAnnotationTooltip() {
		final String key = panel.annotationComboBox.getSelectedItem().toString();
		String tooltip = annotationHtml.replace("%DataSourceName%", key);

		if (annotationUrlMap.get(key) == null) {
			return "";
		}

		tooltip = tooltip.replace("%SourceURL%", annotationUrlMap.get(key));

		if (annotationFormatMap.get(key) != null) {
			tooltip = tooltip.replace("%Format%", annotationFormatMap.get(key));
		} else {
			String[] parts = annotationUrlMap.get(key).split("/");

			if (parts[parts.length - 1].startsWith(GENE_ASSOCIATION)) {
				tooltip = tooltip.replace("%Format%", "Gene Association");
			}

			tooltip = tooltip.replace("%Format%", "General Annotation Text Table");
		}

		if (annotationAttributesMap.get(key) != null) {
			StringBuffer table = new StringBuffer();
			final Map<String, String> annotations = annotationAttributesMap.get(key);

			for (String anno : annotations.keySet()) {
				table.append("<tr>");
				table.append("<td><strong>" + anno + "</strong></td><td>" + annotations.get(anno) + "</td>");
				table.append("</tr>");
			}

			return tooltip.replace("%AttributeTable%", table.toString());
		}

		return tooltip.replace("%AttributeTable%", "");
	}

	private void browseAnnotationButtonActionPerformed(ActionEvent evt) {
		final JFrame parentFrame = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		
		DataSourceSelectDialog dssd = new DataSourceSelectDialog(DataSourceSelectDialog.ANNOTATION_TYPE,
				parentFrame, Dialog.ModalityType.APPLICATION_MODAL, serviceRegistrar);
		dssd.setLocationRelativeTo(parentFrame);
		dssd.setVisible(true);

		String key = dssd.getSourceName();

		if (key != null) {
			panel.annotationComboBox.addItem(key);
			annotationUrlMap.put(key, dssd.getSourceUrlString());
			panel.annotationComboBox.setSelectedItem(key);
			panel.annotationComboBox.setToolTipText(getAnnotationTooltip());
		}
	}

	private void browseOntologyButtonActionPerformed(ActionEvent evt) {
		final JFrame parentFrame = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		
		DataSourceSelectDialog dssd = new DataSourceSelectDialog(DataSourceSelectDialog.ONTOLOGY_TYPE,
				parentFrame, Dialog.ModalityType.APPLICATION_MODAL, serviceRegistrar);
		dssd.setLocationRelativeTo(parentFrame);
		dssd.setVisible(true);

		String key = dssd.getSourceName();

		if (key != null) {
			panel.ontologyComboBox.insertItemAt(key, 0);
			ontologyUrlMap.put(key, dssd.getSourceUrlString());
			panel.ontologyComboBox.setSelectedItem(key);
			panel.ontologyComboBox.setToolTipText(getOntologyTooltip());
		}
	}

	@SuppressWarnings("unchecked")
	protected void setOntologyComboBox() {
		final CyProperty<Bookmarks> bookmarksProp =
				serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=bookmarks)");
		final BookmarksUtil bkUtil = serviceRegistrar.getService(BookmarksUtil.class);
		
		final Bookmarks bookmarks = bookmarksProp.getProperties();
		final List<DataSource> annotations = bkUtil.getDataSourceList("ontology", bookmarks.getCategory());
		String key = null;

		for (DataSource source : annotations) {
			key = source.getName();
			panel.ontologyComboBox.addItem(key);
			ontologyUrlMap.put(key, source.getHref());
			ontologyDescriptionMap.put(key, bkUtil.getAttribute(source, "description"));
			ontologyTypeMap.put(key, bkUtil.getAttribute(source, "ontologyType"));
		}

		panel.ontologyComboBox.setToolTipText(getOntologyTooltip());
	}

	@SuppressWarnings("unchecked")
	protected void setAnnotationComboBox() throws JAXBException, IOException {
		final CyProperty<Bookmarks> bookmarksProp =
				serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=bookmarks)");
		final BookmarksUtil bkUtil = serviceRegistrar.getService(BookmarksUtil.class);
		
		final Bookmarks bookmarks = bookmarksProp.getProperties();
		final List<DataSource> annotations = bkUtil.getDataSourceList("annotation", bookmarks.getCategory());
		String key = null;

		panel.annotationComboBox.addItem(DEF_ANNOTATION_ITEM);

		for (DataSource source : annotations) {
			key = source.getName();
			panel.annotationComboBox.addItem(key);
			annotationUrlMap.put(key, source.getHref());
			annotationFormatMap.put(key, source.getFormat());

			final Map<String, String> attrMap = new HashMap<String, String>();

			for (Attribute attr : source.getAttribute())
				attrMap.put(attr.getName(), attr.getContent());

			annotationAttributesMap.put(key, attrMap);
		}

		panel.annotationComboBox.setToolTipText(getAnnotationTooltip());
	}

	/**
	 * Create task for ontology reader and run the task.<br>
	 */
	private void loadOntology(final String dataSource, final String ontologyName, final String annotationSource)
			throws IOException {
		final URL url = new URL(dataSource);
		final URL annotationSourceUrl = new URL(annotationSource);
		InputStream is = null;
		
		if (annotationSourceUrl.toString().endsWith("gz")) {
			is = new GZIPInputStream(annotationSourceUrl.openStream());
		} else {
			is = annotationSourceUrl.openStream();
		}
		
		ImportOntologyAndAnnotationTaskFactory taskFactory =
				new ImportOntologyAndAnnotationTaskFactory(isTaskFactory, url.openStream(), ontologyName, is,
						annotationSource, serviceRegistrar);
		
		serviceRegistrar.getService(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
	}

	protected void importOntologyAndAnnotation() throws IOException {
		final String selectedOntologyName = panel.ontologyComboBox.getSelectedItem().toString();
		final String ontologySourceLocation = ontologyUrlMap.get(selectedOntologyName);
		final String annotationSource = annotationUrlMap.get(panel.annotationComboBox.getSelectedItem());

		loadOntology(ontologySourceLocation, selectedOntologyName, annotationSource);
	}
}
