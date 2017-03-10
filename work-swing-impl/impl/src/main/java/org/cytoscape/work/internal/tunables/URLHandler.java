package org.cytoscape.work.internal.tunables;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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

import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ToolTipManager;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the type <i>URL</i> of <code>Tunable</code>
 */
@SuppressWarnings("serial")
public class URLHandler extends AbstractGUITunableHandler {

	private static final Logger logger = LoggerFactory.getLogger(URLHandler.class);

	private JComboBox<String> networkFileComboBox;

	private final Map<String, String> dataSourceMap;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>URL</code> type
	 * 
	 * It creates the GUI which displays a field to enter a URL, and a combobox
	 * which contains different registered URL with their description
	 * 
	 * @param f
	 *            field that has been annotated
	 * @param o
	 *            object contained in <code>f</code>
	 * @param t
	 *            tunable associated to <code>f</code>
	 */
	public URLHandler(Field f, Object o, Tunable t, final DataSourceManager manager) {
		super(f, o, t);
		dataSourceMap = new HashMap<>();
		init(manager);
	}

	public URLHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable,
			final DataSourceManager manager) {
		super(getter, setter, instance, tunable);
		dataSourceMap = new HashMap<>();
		init(manager);
	}

	private void init(final DataSourceManager dsManager) {
		// creation of the GUI and layout
		initGUI();
		
		// Get the DataSources of the appropriate DataCategory from the tunable parameters.
		final Collection<DataSource> dataSources = dsManager.getDataSources(
				DataCategory.valueOf(((String)getParams().get("fileCategory")).toUpperCase()));
		
		final SortedSet<String> labelSet = new TreeSet<String>();
		
		if (dataSources != null) {
			for (DataSource ds : dataSources) {
				String link = null;
				link = ds.getLocation().toString();
				final String sourceName = ds.getName();
				final String provider = ds.getProvider();
				final String sourceLabel = provider + ":" + sourceName;
				dataSourceMap.put(sourceLabel, link);
				labelSet.add(sourceLabel);
			}
		}

		for (final String label : labelSet)
			networkFileComboBox.addItem(label);
	}

	/**
	 * Set the url typed in the field, or chosen from the combobox to the
	 * object <code>URL</code> <code>o</code>
	 */
	@Override
	public void handle() {
		final Object selected = networkFileComboBox.getSelectedItem();
		
		if (selected == null)
			return;

		final String selectedString = selected.toString();
		final String urlString;
		
		if ((selectedString.startsWith("http:") == false) && (selectedString.startsWith("https:") == false))
			urlString = dataSourceMap.get(selectedString);
		else
			urlString = selectedString;

		if (urlString != null) {
			try {
				setValue(new URL(urlString));
			} catch (final Exception e) {
				logger.error("Could not create URL: " + urlString, e);
			}
		}

	}

	private void initGUI() {
		final ToolTipManager tipManager = ToolTipManager.sharedInstance();
		tipManager.setInitialDelay(1);
		tipManager.setDismissDelay(7500);

		networkFileComboBox = new JComboBox<>();
		networkFileComboBox.setEditable(true);
		networkFileComboBox.setName("networkFileComboBox");
		networkFileComboBox.setToolTipText(
				"<html><body>You can specify URL by the following:" +
				"<ul><li>Type URL</li><li>Select from pull down menu</li>" +
				"<li>Drag & Drop URL from Web Browser</li></ul></body><html>"
		);
		networkFileComboBox.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				this.setToolTipText(value != null ? value.toString() : null);
				return this;
			}
		});
		networkFileComboBox.setPreferredSize(new Dimension(660, networkFileComboBox.getPreferredSize().height));
		networkFileComboBox.setMaximumSize(networkFileComboBox.getPreferredSize());

		final JLabel label = new JLabel("Import data from URL:");
		
		updateFieldPanel(panel, label, networkFileComboBox, horizontal);
		setTooltip(getTooltip(), label, networkFileComboBox);
	}
}
