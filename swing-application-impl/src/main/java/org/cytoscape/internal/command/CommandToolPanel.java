/**
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.cytoscape.internal.command;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class CommandToolPanel extends JPanel implements CytoPanelComponent2, ActionListener, CyShutdownListener {

	private static final String NEXT_ACTION = "next";
	private static final String PREV_ACTION = "previous";
	private static final String CLEAR_ACTION = "clear";
	
	public static final int MAX_SAVED_COMMANDS = 500;
	
	private static final Logger logger  = Logger.getLogger(CyUserLog.NAME);

	private final CyServiceRegistrar serviceRegistrar;
	private final CommandTaskRunner commandRunner;
	
	private List<String> commandList = new ArrayList<>();
	private int commandIndex = 0;

	private JResultsPane resultsText;
	private JTextField inputField;
	private File savedCommandsFile;

	public CommandToolPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.commandRunner = new CommandTaskRunner(serviceRegistrar);
		restoreCommandHistory();
		initComponents();
		setOpaque(false);
	}

	private void restoreCommandHistory() {
		CyApplicationConfiguration appConfig = serviceRegistrar.getService(CyApplicationConfiguration.class);
		File appConfigDir = appConfig.getConfigurationDirectoryLocation();
		savedCommandsFile = new File(appConfigDir.getAbsolutePath() + File.separator + "commandHistory.txt");
		if (savedCommandsFile.exists()) {
			readCommandHistory(savedCommandsFile);
		}
	}
	
	private void readCommandHistory(File commandsFile) {
		try(BufferedReader br = new BufferedReader(new FileReader(commandsFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				commandList.add(line);
			}
			commandIndex = commandList.size();
		} catch (IOException e) {
			logger.error("Error reading command history from '" + commandsFile.getAbsolutePath() + "': " + e);
		}
	}
	
	@Override
	public String getIdentifier() {
		return "CLI";
	}

	@Override
	public Component getComponent() {
		return this;
	}

	/**
	 * This method returns null because CytoPanelName.BOTTOM is not currently being exposed as API.
	 * The method contract for this method prohibits returning null, for this reason the CommandToolDialog 
	 * should not be accessible to Apps via any public API.
	 */
	@Override
	public CytoPanelName getCytoPanelName() {
		// return CytoPanelName.BOTTOM; 
		return null; 
	}

	@Override
	public Icon getIcon() { 
		return null; 
	}

	@Override
	public String getTitle() { 
		return "Command Line"; 
	}

	@Override
	public void setVisible(boolean tf) {
		super.setVisible(tf);
		getInputField().requestFocusInWindow();
	}

	/**
	 * Initialize all of the graphical components of the dialog
	 */
	private void initComponents() {
		JLabel inputLabel = new JLabel("Command:");
		resultsText = new JResultsPane(null, this);
		resultsText.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(resultsText);
		resultsText.setScrollPane(scrollPane); // So we can update the scroll position

		JButton clearButton = new JButton("Clear");
		clearButton.setToolTipText("Clear Log");
		clearButton.setActionCommand(CLEAR_ACTION);
		clearButton.addActionListener(this);
		clearButton.putClientProperty("JButton.buttonType", "gradient");

		LookAndFeelUtil.makeSmall(inputLabel, clearButton);
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);

		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
				.addComponent(clearButton)
				.addComponent(inputLabel)
			)
			.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(scrollPane, DEFAULT_SIZE, 880, Short.MAX_VALUE)
				.addComponent(getInputField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
				.addComponent(clearButton, 0, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(scrollPane, 0, 580, Short.MAX_VALUE)
			)
			.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(inputLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getInputField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			)
		);
	}

	/**
 	 * External interface for users to inject commands
 	 */
	public void executeCommand(String command) {
		resultsText.appendCommand(command);
		commandRunner.runCommand(command, resultsText);
	}
	
	/**
	 * Write out the last 500 commands
	 */
	public void handleEvent(CyShutdownEvent shutdown) {
		if (savedCommandsFile.exists()) {
			savedCommandsFile.delete();
		}
		int start = 0;
		if (commandList.size() > MAX_SAVED_COMMANDS)
			start = commandList.size() - MAX_SAVED_COMMANDS;

		try (FileWriter writer = new FileWriter(savedCommandsFile)) {
			for (int i = start; i < commandList.size(); i++) {
				writer.write(commandList.get(i) + "\n");
			}
		} catch (Exception ioe) {
			logger.error("Error writing command history to '"+savedCommandsFile.getAbsolutePath()+"': "+ioe);
			System.err.println("Error writing command history to '"+savedCommandsFile.getAbsolutePath()+"': "+ioe);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (CLEAR_ACTION.equals(e.getActionCommand())) {
			resultsText.clear();
		} else {
			String input = getInputField().getText();
			commandList.add(input);
			commandIndex = commandList.size();

			executeCommand(input);

			getInputField().setText("");
		}
	}

	private JTextField getInputField() {
		if (inputField == null) {
			inputField = new JTextField();
			inputField.setFont(inputField.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));

			// Set up our up-arrow/down-arrow actions
			Action previousAction = new LineAction(PREV_ACTION);
			inputField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), PREV_ACTION);
			inputField.getActionMap().put(PREV_ACTION, previousAction);

			Action nextAction = new LineAction(NEXT_ACTION);
			inputField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), NEXT_ACTION);
			inputField.getActionMap().put(NEXT_ACTION, nextAction);
			inputField.addActionListener(this);
		}

		return inputField;
	}

	private class LineAction extends AbstractAction {

		final String action;

		public LineAction(String action) {
			this.action = action;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (commandList.size() == 0)
				return;

			switch(action) {
				case NEXT_ACTION: commandIndex++; break;
				case PREV_ACTION: commandIndex--; break;
				default: return;
			}

			String inputCommand;
			if (commandIndex >= commandList.size()) {
				inputCommand = "";
				commandIndex = commandList.size();
			} else if (commandIndex < 0) {
				inputCommand = "";
				commandIndex = -1;
			} else {
				inputCommand = commandList.get(commandIndex);
			}

			getInputField().setText(inputCommand);
			getInputField().selectAll();
		}
	}
	
}
