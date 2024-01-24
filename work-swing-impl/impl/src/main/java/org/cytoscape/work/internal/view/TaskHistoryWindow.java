package org.cytoscape.work.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.work.internal.tunables.utils.ViewUtil.invokeOnEDT;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.cytoscape.event.DebounceTimer;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.task.TaskHistory;
import org.cytoscape.work.internal.task.TaskHistory.History;
import org.cytoscape.work.internal.tunables.utils.ColorUtil;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults.TaskIcon;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
public class TaskHistoryWindow {

	private final TaskHistory taskHistory;
	private final JDialog dialog;
	private final JEditorPane pane;
	
	private DebounceTimer debounceTimer;
	

	public TaskHistoryWindow(TaskHistory taskHistory) {
		this.taskHistory = taskHistory;

		dialog = new JDialog(null, "Cytoscape Task History", JDialog.ModalityType.MODELESS);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		pane = new JEditorPane();
		pane.setEditable(false);
		pane.setContentType("text/html");
		
		int fontSize = (int) LookAndFeelUtil.getSmallFontSize();
		String fontFamily = "\"Courier New\", monospace";
		
		HTMLEditorKit htmlEditorKit = (HTMLEditorKit) pane.getEditorKit();
		StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
		styleSheet.addRule("h1, h2, h3, p { font-family: " + fontFamily + "; font-size:" + fontSize  + "px; }");
		styleSheet.addRule("ul { list-style-type: none; font-family: " + fontFamily + "; font-size:" + fontSize  + "px; }");

		JButton clearButton = new JButton("Clear Display");
		clearButton.addActionListener(evt -> {
			taskHistory.clear();
			update();
		});
		
		JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});

		JPanel buttonsPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton, clearButton);
		buttonsPanel.add(clearButton);

		JScrollPane scrollPane = new JScrollPane(pane);

		GroupLayout layout = new GroupLayout(dialog.getContentPane());
		dialog.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(scrollPane, DEFAULT_SIZE, 500, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(buttonsPanel)
						.addContainerGap()
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(scrollPane, DEFAULT_SIZE, 400, Short.MAX_VALUE)
				.addComponent(buttonsPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addContainerGap()
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), null, closeButton.getAction());
		dialog.getRootPane().setDefaultButton(closeButton);

		taskHistory.setFinishListener(history -> {
			update();
		});

		dialog.pack();
		open();
	}
	
	private void close() {
		dialog.dispose();
		debounceTimer.shutdown();
		debounceTimer = null;
	}
	
	public void open() {
		if(debounceTimer == null) {
			debounceTimer = new DebounceTimer();
		}
		update();
		dialog.setVisible(true);
	}

	
	private void generateMessage(TaskHistory.Message message, StringBuilder buffer) {
		TaskMonitor.Level level = message.level();
		String iconText = GUIDefaults.getIconText(level);
		
		if (iconText != null) {
			Color color = GUIDefaults.getForeground(level);
			String fg = ColorUtil.toHexString(color);
			buffer.append("<li style='margin-top: 5px;'>");
			buffer.append("<span style='font-family: FontAwesome; color: " + fg + ";'>" + iconText + "</span>&nbsp;");
		} else {
			buffer.append("<li style='margin-top: 10px;'>");
			buffer.append("<b>");
		}
		
		buffer.append(message.message());
		
		if (level == null)
			buffer.append("</b>");
		
		buffer.append("</li>");
	}

	private void generateHistory(TaskHistory.History history, StringBuilder buffer) {
		if (history.getFirstTaskClass() == null) {
			// skip task iterators that never called history.setFirstTaskClass()
			// -- these
			// iterators were never started because they were cancelled by its
			// first tunable dialog
			return;
		}

		buffer.append("<p>");
		buffer.append("<h1 style='margin-top: 0px; margin-bottom: 0px;'>&nbsp;");

		FinishStatus.Type finishType = history.getFinishType();
		TaskIcon icon = GUIDefaults.getIcon(finishType);
		String iconText = icon != null ? icon.getText() : null;
		
		if (iconText != null) {
			Color color = GUIDefaults.getForeground(iconText);
			String fg = ColorUtil.toHexString(color);
			
			buffer.append("<span style='font-family: FontAwesome; color: " + fg + ";'>" + iconText + "</span>&nbsp;");
		}

		String title = history.getTitle();
		
		if (title == null || title.length() == 0) {
			buffer.append("<i>Untitled</i>");
			Class<?> klass = history.getFirstTaskClass();
			
			if (klass != null) {
				buffer.append(" <font size='-1'>(");
				buffer.append(klass.getName());
				buffer.append(")</font>");
			}
		} else {
			buffer.append(title);
		}
		
		buffer.append("</h1>");
		buffer.append("<ul style='margin-top: 0px; margin-bottom: 0px;'>");
		
		for (var message : history)
			generateMessage(message, buffer);
		
		buffer.append("</ul>");
		buffer.append("</p><br>");
	}

	private String generateHistoryHTML() {
		var buffer = new StringBuilder("<html>");
		
		for(History history : taskHistory) {
			if(history.isUnnested()) {
				buffer.append("<ul style='margin-top: 0px; margin-bottom: 0px; margin-left: 0px; padding-left: 0px;'>");
				generateMessage(history.getFirstMessage(), buffer);
			} else {
				generateHistory(history, buffer);
			}
		}
		
		return buffer.append("</html>").toString();
	}

	public void update() {
		debounceTimer.debounce(() -> {
			invokeOnEDT(() -> {
				String content = generateHistoryHTML();
				pane.setText(content);
			});
		});
	}
}
