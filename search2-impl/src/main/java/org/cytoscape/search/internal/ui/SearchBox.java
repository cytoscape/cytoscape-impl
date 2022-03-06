package org.cytoscape.search.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.cytoscape.search.internal.search.SearchResults;
import org.cytoscape.search.internal.search.SearchTask;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

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
public abstract class SearchBox extends JPanel {

	private CyServiceRegistrar registrar;
	private JTextField searchTextField;

	public abstract SearchTask getSearchTask(String queryString);
	
	
	public SearchBox(CyServiceRegistrar registrar) {
		this.registrar = registrar;
		initComponents();
	}
	
	protected CyServiceRegistrar getServiceRegistrar() {
		return registrar;
	}
	
	private void initComponents() {
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(getSearchTextField(), 120, 240, 300)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(getSearchTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}
	
	private JTextField getSearchTextField() {
		if(searchTextField == null) {
			var defText = "Enter search term...";
			var defFont = UIManager.getFont("TextField.font") != null ?
					UIManager.getFont("TextField.font").deriveFont(getSmallFontSize()) : null;
			
			searchTextField = new JTextField();
			searchTextField.putClientProperty("JTextField.variant", "search");

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
		}
		return searchTextField;
	}
	
	
	private void doSearching() {
		var queryStr = searchTextField.getText().trim();
		if(queryStr.length() == 0)
			return;
		
		SearchTask searchTask = getSearchTask(queryStr);
		
		var taskMgr = registrar.getService(DialogTaskManager.class);
		taskMgr.execute(new TaskIterator(searchTask), new TaskObserver() {
			
			@Override
			public void allFinished(FinishStatus status) {
				if(status.getType() == FinishStatus.Type.SUCCEEDED) {
					var results = searchTask.getResults(SearchResults.class);
					if(results == null)
						return;
					String message = searchTask.getResults(String.class);
					showPopup(message, results.isError());
				} 
			}

			@Override
			public void taskFinished(ObservableTask task) { }
			
		});
	}
	
	
	@SuppressWarnings("deprecation")
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

	protected void showPopup(String message, boolean error) {
		if(message == null)
			return;
		
		var label = new JLabel();
		label.setText("   " + message + "   ");
		if (error)
			label.setForeground(Color.RED);
		
		LookAndFeelUtil.makeSmall(label);
		
		var popup = new JPopupMenu();
		popup.add(label);
		
		var timer = new Timer(3400, e -> popup.setVisible(false));
		timer.setRepeats(false);
		timer.start();
		
		popup.show(searchTextField, 0, searchTextField.getHeight());
	}

}
