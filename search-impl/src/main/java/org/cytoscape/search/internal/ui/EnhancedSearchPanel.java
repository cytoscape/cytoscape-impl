package org.cytoscape.search.internal.ui;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.EnhancedSearch;
import org.cytoscape.search.internal.SearchTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedSearchPanel extends JPanel {

	private static final long serialVersionUID = 3748296514173533886L;
	
	private static final Logger logger = LoggerFactory.getLogger(EnhancedSearchPanel.class);
	
	private final EnhancedSearch searchMgr;
	private final CyServiceRegistrar serviceRegistrar;
	
	private JTextField tfSearchText;

	/** Creates new form NewJPanel */
	public EnhancedSearchPanel(final EnhancedSearch searchMgr, final CyServiceRegistrar serviceRegistrar) {
		this.searchMgr = searchMgr;
		this.serviceRegistrar = serviceRegistrar;
		initComponents();
	}

	private void tfSearchTextActionPerformed(ActionEvent evt) {
		doSearching();
	}

	// Do searching based on the query string from user on text-field
	private void doSearching() {
		final String queryStr = this.tfSearchText.getText().trim();
		
		// Ignore if the search term is empty
		if (queryStr == null || queryStr.length() == 0)
			return;
		
		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetwork currentNetwork = appManager.getCurrentNetwork();
		
		if (currentNetwork != null) {
			final SearchTaskFactory factory = new SearchTaskFactory(searchMgr, queryStr, serviceRegistrar);
			
			final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
			taskMgr.execute(factory.createTaskIterator(currentNetwork));
		} else {
			logger.error("Could not find network for search");
		}
	}

	private void initComponents() {
		final String defText = "Enter search term...";
		final Font defFont = UIManager.getFont("TextField.font") != null ?
				UIManager.getFont("TextField.font").deriveFont(LookAndFeelUtil.getSmallFontSize()) : null;
		
		tfSearchText = new JTextField();
		tfSearchText.putClientProperty("JTextField.variant", "search");

		tfSearchText.setToolTipText("<html>Example Search Queries:<br><br>YL* -- Search all columns<br>name:YL* -- Search 'name' column<br>GO\\:1232 -- Escape special characters with backslash</html>");
		tfSearchText.setName("tfSearchText");
		
		if (!isAquaLAF()) {
			tfSearchText.setText(defText);
			
			if (defFont != null)
				tfSearchText.setFont(defFont);
		}
		
		tfSearchText.addActionListener(evt -> tfSearchTextActionPerformed(evt));
		tfSearchText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (tfSearchText.getText().equals(defText)) {
					tfSearchText.setText("");
					tfSearchText.setFont(UIManager.getFont("TextField.font"));
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if (!isAquaLAF() && tfSearchText.getText().trim().isEmpty()) {
					tfSearchText.setText(defText);
					
					if (defFont != null)
						tfSearchText.setFont(defFont);
				}
			}
		});
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(10, 20, Short.MAX_VALUE)
				.addComponent(tfSearchText, 120, 240, 300)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(tfSearchText, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}
}
