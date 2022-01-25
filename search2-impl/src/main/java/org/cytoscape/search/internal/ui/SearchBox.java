package org.cytoscape.search.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.search.internal.search.SearchResults;
import org.cytoscape.search.internal.search.SearchTask;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L 
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2022 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class SearchBox extends JPanel {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final SearchManager searchManager;
	private final CyServiceRegistrar registrar;
	
	private JTextField searchTextField;

	public SearchBox(SearchManager searchManager, CyServiceRegistrar registrar) {
		this.searchManager = searchManager;
		this.registrar = registrar;
		initComponents();
	}


	private void doSearching() {
		var queryStr = searchTextField.getText().trim();
		if (queryStr == null || queryStr.length() == 0)
			return;
		
		var appManager = registrar.getService(CyApplicationManager.class);
		var currentNetwork = appManager.getCurrentNetwork();
		
		if (currentNetwork != null) {
			var taskMgr = registrar.getService(DialogTaskManager.class);
			SearchTask task = new SearchTask(searchManager, currentNetwork, queryStr);
			
			taskMgr.execute(new TaskIterator(task), new TaskObserver() {
				@Override
				public void taskFinished(ObservableTask task) {
					if (task instanceof SearchTask searchTask) {
						var result = searchTask.getResults(SearchResults.class);
						showPopup(result);
					}
				}
				@Override
				public void allFinished(FinishStatus finishStatus) { }
			});
		} else {
			logger.error("Could not find network for search");
		}
		
	}

	private void showPopup(SearchResults results) {
//		if (results == null)
//			return;
//		
//		var label = new JLabel();
//		
//		if (results.isError())
//			label.setForeground(Color.RED);
//		
//		label.setText("   " + results.getMessage() + "   ");
//		
//		makeSmall(label);
//		
//		var popup = new JPopupMenu();
//		popup.add(label);
//		
//		var timer = new Timer(3400, e -> popup.setVisible(false));
//		timer.setRepeats(false);
//		timer.start();
//		
//		popup.show(tfSearchText, 0, tfSearchText.getHeight());
	}
	
	private void initComponents() {
		var defText = "Enter search term...";
		var defFont = UIManager.getFont("TextField.font") != null ?
				UIManager.getFont("TextField.font").deriveFont(getSmallFontSize()) : null;
		
		searchTextField = new JTextField();
		searchTextField.putClientProperty("JTextField.variant", "search");
		searchTextField.setText("search2");

		searchTextField.setToolTipText("<html>Example Search Queries:<br><br>YL* -- Search all columns<br>name:YL* -- Search 'name' column<br>GO\\:1232 -- Escape special characters and spaces with backslash</html>");
		searchTextField.setName("tfSearchText");
		
		if (!isAquaLAF()) {
			searchTextField.setText(defText);
			if (defFont != null)
				searchTextField.setFont(defFont);
		}
		
		searchTextField.addActionListener(evt -> doSearching());
		searchTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (searchTextField.getText().equals(defText)) {
					searchTextField.setText("");
					searchTextField.setFont(UIManager.getFont("TextField.font"));
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if (!isAquaLAF() && searchTextField.getText().trim().isEmpty()) {
					searchTextField.setText(defText);
					if (defFont != null)
						searchTextField.setFont(defFont);
				}
			}
		});
		setKeyBindings(searchTextField);
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(searchTextField, 120, 240, 300)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(searchTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}
	
	private void setKeyBindings(JComponent comp) {
		var actionMap = comp.getActionMap();
		var inputMap = comp.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), KeyAction.FOCUS);
		actionMap.put(KeyAction.FOCUS, new KeyAction(KeyAction.FOCUS));
	}
	
	private class KeyAction extends AbstractAction {

		final static String FOCUS = "FOCUS";
		
		KeyAction(String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			var cmd = e.getActionCommand();
			
			if (cmd.equals(FOCUS)) {
				if (searchTextField.isVisible()) {
					searchTextField.requestFocusInWindow();
					searchTextField.selectAll();
				}
			}
		}
	}
}
