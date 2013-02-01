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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
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
public class URLHandler extends AbstractGUITunableHandler {

	private static final Logger logger = LoggerFactory.getLogger(URLHandler.class);

	private JComboBox networkFileComboBox;
	private JLabel titleLabel;

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
		dataSourceMap = new HashMap<String, String>();
		init(manager);
	}

	public URLHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable,
			final DataSourceManager manager) {
		super(getter, setter, instance, tunable);
		dataSourceMap = new HashMap<String, String>();
		init(manager);
	}

	private void init(final DataSourceManager dsManager) {

		// creation of the GUI and layout
		initGUI();
		
		//Get the DataSources of the appropriate DataCategory from the tunable parameters.
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
	 * Set the url typed in the field, or choosen from the combobox to the
	 * object <code>URL</code> <code>o</code>
	 */
	@Override
	public void handle() {
		final Object selected = networkFileComboBox.getSelectedItem();
		if (selected == null)
			return;

		final String selectedString = selected.toString();

		final String urlString;
		if (selectedString.startsWith("http:") == false)
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

	// Tooltips to inform the user are also provided on the combobox
	private void initGUI() {
		// adding tooltips to panel components
		final ToolTipManager tipManager = ToolTipManager.sharedInstance();
		tipManager.setInitialDelay(1);
		tipManager.setDismissDelay(7500);

		titleLabel = new JLabel("Import data from URL: ");

		networkFileComboBox = new JComboBox();
		networkFileComboBox.setEditable(true);
		networkFileComboBox.setName("networkFileComboBox");
		networkFileComboBox
				.setToolTipText("<html><body>You can specify URL by the following:<ul><li>Type URL</li><li>Select from pull down menu</li><li>Drag & Drop URL from Web Browser</li></ul></body><html>");

		this.panel.setLayout(getLayout());
	}

	// diplays the panel's component in a good view
	private GroupLayout getLayout() {
		final GroupLayout layout = new GroupLayout(panel);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(networkFileComboBox, 0, 450, Short.MAX_VALUE)
										.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 350,
												GroupLayout.PREFERRED_SIZE)).addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(titleLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(networkFileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		return layout;
	}
}
