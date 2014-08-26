/* vim: set ts=2: */
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
package org.cytoscape.commandDialog.internal.ui;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.StreamTokenizer;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.KeyStroke;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.cytoscape.commandDialog.internal.handlers.CommandHandler;
import org.cytoscape.commandDialog.internal.handlers.MessageHandler;

public class CommandToolDialog extends JDialog 
                             implements ActionListener {

	private List<String> commandList;
	private int commandIndex = 0;

	// Dialog components
	private JResultsPane resultsText;
	private JTextField inputField;
	private CommandHandler commandHandler;

	public CommandToolDialog (Frame parent, CommandHandler commandHandler) {
		super(parent, false);
		commandList = new ArrayList();
		this.commandHandler = commandHandler;
		
		initComponents();
	}

	public void setVisible(boolean tf) {
		super.setVisible(tf);
		inputField.requestFocusInWindow();
	}

	/**
	 * Initialize all of the graphical components of the dialog
	 */
	private void initComponents() {
		this.setTitle("Command Line Dialog");

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Create a panel for the main content
		JPanel dataPanel = new JPanel();
		BoxLayout layout = new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS);
		dataPanel.setLayout(layout);

		Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

		resultsText = new JResultsPane(this, dataPanel);
		resultsText.setEditable(false);
		resultsText.setPreferredSize(new Dimension(900, 200));
		JScrollPane scrollPane = new JScrollPane(resultsText);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(resultsText);

		scrollPane.setBorder(BorderFactory.createTitledBorder(etchedBorder, "Reply Log"));
		dataPanel.add(scrollPane);

		inputField = new JTextField(80);
		inputField.setBorder(BorderFactory.createTitledBorder(etchedBorder, "Command"));
		// Set up our up-arrow/down-arrow actions
		Action previousAction = new LineAction("previous");
		inputField.getInputMap().put(KeyStroke.getKeyStroke("UP"), "previous");
		inputField.getActionMap().put("previous", previousAction);

		Action nextAction = new LineAction("next");
		inputField.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "next");
		inputField.getActionMap().put("next", nextAction);

		inputField.addActionListener(this);
		dataPanel.add(inputField);
		inputField.setMaximumSize(new Dimension(1000,45));

		// Create the button box
		JPanel buttonBox = new JPanel();
		JButton doneButton = new JButton("Done");
		doneButton.setActionCommand("done");
		doneButton.addActionListener(this);

		JButton clearButton = new JButton("Clear");
		clearButton.setActionCommand("clear");
		clearButton.addActionListener(this);

		buttonBox.add(clearButton);
		buttonBox.add(doneButton);
		buttonBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		buttonBox.setMaximumSize(new Dimension(1000,45));
		
		dataPanel.add(buttonBox);
		setContentPane(dataPanel);
		setMaximumSize(new Dimension(1000,1000));
	}

	/**
 	 * External interface for users to inject commands
 	 */
	public void executeCommand(String command) {
		resultsText.appendCommand(command);
		commandHandler.handleCommand((MessageHandler) resultsText, command);
	}

	public void actionPerformed(ActionEvent e) {
		if ("done".equals(e.getActionCommand())) {
			this.dispose();
		} else if ("clear".equals(e.getActionCommand())) {
			resultsText.clear();
		} else {
			String input = inputField.getText();
			resultsText.appendCommand(input);
			commandList.add(input);
			commandIndex = commandList.size();

			commandHandler.handleCommand((MessageHandler) resultsText, input);

			inputField.setText("");
		}
	}

	class LineAction extends AbstractAction {
		String action = null;
		public LineAction(String action) {
			super();
			this.action = action;
		}
			
		public void actionPerformed(ActionEvent e) {
			if (commandList.size() == 0) return;

			// System.out.println("in: size = "+commandList.size()+", index = "+commandIndex);

			if (action.equals("next")) {
				commandIndex++;
			} else if (action.equals("previous")) {
				commandIndex--;
			} else
				return;


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

			// System.out.println("out: size = "+commandList.size()+", index = "+commandIndex);
			inputField.setText(inputCommand);
			inputField.selectAll();
		}
	}
}
