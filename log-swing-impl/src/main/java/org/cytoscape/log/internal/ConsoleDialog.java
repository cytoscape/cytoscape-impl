package org.cytoscape.log.internal;

/*
 * #%L
 * Cytoscape Log Swing Impl (log-swing-impl)
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


import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Level;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConsoleDialog {
	static class LogEvent {
		static final Level[] LEVELS = {
			Level.TRACE,
			Level.DEBUG,
			Level.INFO,
			Level.WARN,
			Level.ERROR,
			Level.FATAL
		};
        static final Level DEFAULT_LEVEL = Level.WARN;

		static final DateFormat timeFmt = DateFormat.getTimeInstance();

		final String message;
		final String log;
		final String thread;
		final Date timestamp;
		final int level;

		public LogEvent(PaxLoggingEvent event) {
			this.message = event.getMessage();
			this.log = event.getLoggerName();
			this.thread = event.getThreadName();
			this.timestamp = new Date(event.getTimeStamp());
			this.level = event.getLevel().toInt();
		}

		private String levelToString() {
			return Level.toLevel(this.level).toString();
		}

		public String getLog() {
			return log;
		}

		public void appendToLogViewer(final LogViewer logViewer)
		{
			final String submessage = String.format("%s %s [%s] %s", timeFmt.format(this.timestamp), this.levelToString(), this.thread, this.log);
			logViewer.append(levelToString(), this.message, submessage);
		}

		/**
		 * Determines if this log event matches the following criteria:
		 * a) Does the log event come from the currently selected log?
		 * b) Does the log event match the specified regular expression?
		 */
		public boolean logEventMatches(final String selectedLog, final Pattern regex, int logLevelThreshold)
		{
			if (this.level < logLevelThreshold)
				return false;
			if (selectedLog != null && !(this.log.startsWith(selectedLog)))
				return false;
			if (regex != null && !regex.matcher(this.message).find()) 
				return false;
			return true;
		}

		public String toString() {
			return String.format("%s %s [%s] %s: %s", levelToString(), timeFmt.format(this.timestamp), this.thread, this.log, this.message);
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ConsoleDialog.class);

	/**
	 * Contains all log events (except those deleted with the Clear button) that were added with the
	 * <code>addLogEvent</code> method.
	 */
	final List<LogEvent> allLogEvents = new ArrayList<>();

	/**
	 * Contains a subset of log events in <code>allLogEvents</code>. Only log events that are in the
	 * currently selected log and match the currently specified regular expression filter are stored
	 * in this array.
	 */
	final List<LogEvent> solicitedLogEvents = new ArrayList<>();

	/**
	 * A hierarchy tree of all the logs.
	 */
	final DefaultMutableTreeNode logs = new DefaultMutableTreeNode("All Logs");

	/**
	 * Used to execute instances of <code>SolicitedLogEventsUpdater</code>.
	 */
	final ExecutorService solicitedLogEventsUpdaterExecutor = Executors.newSingleThreadExecutor();

	/**
	 * Updates <code>solicitedLogEvents</code> by filtering out <code>allLogEvents</code>.
	 */
	final SolicitedLogEventsUpdater logEventsUpdater = new SolicitedLogEventsUpdater();

	/**
	 * The compiled regular expression specified by the user in <code>filterTextField</code>.
	 */
	Pattern currentPattern = null;

	final JDialog		dialog;
	final TaskManager	taskManager;
	final JTextField	filterTextField;
	final JComboBox		levelComboBox;
	final JTree		logsTree;
	final LogViewer		logViewer;
	final JCheckBox 	scrollCheckBox;

	public ConsoleDialog(final TaskManager taskManager, final CySwingApplication app, final Map<String,String> logViewerConfig) {
		this.taskManager = taskManager;

		dialog = new JDialog(app.getJFrame(), "Developer's Log Console", Dialog.ModalityType.MODELESS);
		dialog.setLayout(new GridBagLayout());

		filterTextField = new JTextField();
		makeFilterTextFieldValid();
		filterTextField.getDocument().addDocumentListener(new FilterUpdater());

		levelComboBox = new JComboBox(LogEvent.LEVELS);
		levelComboBox.setSelectedItem(LogEvent.DEFAULT_LEVEL);
		levelComboBox.addActionListener(new LevelThresholdListener());

		logViewer = new LogViewer(logViewerConfig);
		logViewer.clear();

		logsTree = new JTree(new DefaultTreeModel(logs));
		logsTree.setSelectionPath(new TreePath(logs.getPath()));
		logsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		logsTree.addTreeSelectionListener(new LogsTreeSelectionListener());
		final JScrollPane logsTreeScrollPane = new JScrollPane(logsTree);

		final JSplitPane logsPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logsTreeScrollPane, logViewer.getComponent());
		logsPane.setDividerLocation(200);

		scrollCheckBox = new JCheckBox("Scroll to new messages");
		scrollCheckBox.setSelected(true);

		final JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ClearAction());
		final JButton exportButton = new JButton("Save to file...");
		exportButton.addActionListener(new ExportAction());

		GridBagConstraints c = new GridBagConstraints();

		final JPanel filterPanel = new JPanel(new GridBagLayout());
		c.insets = new Insets(4, 4, 4, 0);
		c.gridx = 0;		c.gridy = 0;
		c.gridwidth = 1;	c.gridheight = 1;
		c.weightx = 0.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		filterPanel.add(new JLabel("Filter:"), c);

		c.gridx++;		    c.gridy = 0;
		c.weightx = 1.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		filterPanel.add(filterTextField, c);

		c.gridx++;		    c.gridy = 0;
		c.weightx = 0.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		filterPanel.add(new JLabel("Log Level Threshold:"), c);

		c.gridx++;		    c.gridy = 0;
		filterPanel.add(levelComboBox, c);

		c.insets = new Insets(4, 4, 0, 0);
		c.gridx = 0;		c.gridy = 0;
		c.gridwidth = 2;	c.gridheight = 1;
		c.weightx = 1.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		dialog.add(filterPanel, c);

		c.gridx = 0;		c.gridy++;
		c.gridwidth = 2;	c.gridheight = 1;
		c.weightx = 1.0;	c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		dialog.add(logsPane, c);

		c.gridx = 0;		c.gridy++;
		c.gridwidth = 1;	c.gridheight = 1;
		c.weightx = 0.5;	c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;

		dialog.add(scrollCheckBox, c);

		final JPanel btnsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnsPanel.add(exportButton);
		btnsPanel.add(clearButton);

		c.gridx++;
		dialog.add(btnsPanel, c);

		dialog.setPreferredSize(new Dimension(800, 450));
		dialog.pack();
	}

	public void open() {
        if (!dialog.isVisible()) {
            dialog.setVisible(true);
            refreshSolicitedLogEvents();
        }
	}


	/**
	 * this function is only for testing whether logs 
	 * in different levels are displayed and filtered
	 * correctly or not
	 */
	private void test() {
		// TODO Auto-generated method stub
		logger.debug("test for debug");
		logger.error("test fo error");
		logger.info("test for info");
		logger.trace("test for trace");
		logger.warn("test for warn");
	}

	/**
	 * Add a log message
	 */
	public void addLogEvent(final PaxLoggingEvent paxEvent)
	{
		final LogEvent event = new LogEvent(paxEvent);
		allLogEvents.add(event);
		updateLogs(event.getLog());
		if (dialog.isVisible() && event.logEventMatches(getSelectedLog(), currentPattern, getSelectedLogLevelThreshold()))
		{
			solicitedLogEvents.add(event);
			event.appendToLogViewer(logViewer);
			if (doScrollToBottom())
				logViewer.scrollToBottom();
		}
	}

	/**
	 * Gets the selected log
	 * @return The currently selected fully-qualified log name, or <code>null</code> if the root is selected.
	 */
	String getSelectedLog()
	{
		Object[] nodes = logsTree.getSelectionPath().getPath();
		if (nodes.length == 1)
			return null;
		StringBuffer path = new StringBuffer();
		for (int i = 0; i < nodes.length; i++)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[i];
			if (node.isRoot())
				continue;
			path.append(node.getUserObject());
			if (i != (nodes.length - 1))
				path.append('.');
		}
		return path.toString();
	}

	int getSelectedLogLevelThreshold() {
		return ((Level) levelComboBox.getSelectedItem()).toInt();
	}

	/**
	 * Adds a log to <code>logs</code>.
	 * @param newLog A fully-qualified log name
	 */
	void updateLogs(String newLog)
	{
		String[] path = newLog.split("\\.");
		DefaultMutableTreeNode node = logs;
		for (int pathIndex = 0; pathIndex < path.length; pathIndex++)
		{
			DefaultMutableTreeNode child = null;
			Enumeration e = node.children();
			while (e.hasMoreElements())
			{
				DefaultMutableTreeNode potential = (DefaultMutableTreeNode) e.nextElement();
				if (potential.getUserObject().equals(path[pathIndex]))
				{
					child = potential;
					break;
				}
			}

			if (child == null)
			{
				child = new DefaultMutableTreeNode(path[pathIndex]);
				addSortedNode(node, child);
			}

			node = child;
		}
	}

	/**
	 * A replacement for <code>DefaultMutableTreeNode.add</code> that ensures
	 * that the child is added to the parent such that all the children of the
	 * parent are sorted.
	 */
	static void addSortedNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild)
	{
		String newChildName = (String) newChild.getUserObject();

		int childIndex = 0;
		while(childIndex < parent.getChildCount())
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(childIndex);
			String childName = (String) child.getUserObject();
			if (newChildName.compareTo(childName) < 0)
				break;
			childIndex++;
		}

		parent.insert(newChild, childIndex);
	}

	boolean doScrollToBottom() {
		return scrollCheckBox.isSelected();
	}
	
	/**
	 * Updates <code>solicitedLogEvents</code> by filtering out <code>allLogEvents</code>.
	 * This class uses <code>logEventMatches</code> to determine if the elements in
	 * <code>allLogEvents</code> matches the criteria for being in <code>solicitedLogEvents</code>.
	 */
	class SolicitedLogEventsUpdater implements Runnable
	{
		boolean cancel = false;

		public void run()
		{
			logViewer.clear();
			solicitedLogEvents.clear();
			final String selectedLog = getSelectedLog();
            final int logLevelThreshold = getSelectedLogLevelThreshold();
			cancel = false;
			for (final LogEvent event : allLogEvents)
			{
				if (cancel) break;
				if (event.logEventMatches(selectedLog, currentPattern, logLevelThreshold))
				{
					solicitedLogEvents.add(event);
					event.appendToLogViewer(logViewer);
				}
			}
			logViewer.scrollToBottom();
		}

		public void cancel()
		{
			cancel = true;
		}
	}


	/**
	 * Creates a new instance of <code>SolicitedLogEventsUpdater</code> and
	 * executes it. If a <code>SolicitedLogEventsUpdater</code> is already
	 * running, this will cancel it.
	 */
	void refreshSolicitedLogEvents()
	{
		logEventsUpdater.cancel();
		solicitedLogEventsUpdaterExecutor.submit(logEventsUpdater);
	}

	static final String INVALID_FILTER_TEXT_FIELD_TOOLTIP = "<html>The regular expression specified is invalid, because:<br><font face=\"monospace\">%s</font></html>";
	static final String VALID_FILTER_TEXT_FIELD_TOOLTIP = "<html><p>Filter all log messages according to the regular expression entered here</p></html>";

	/**
	 * Changes the appearance of <code>filterTextField</code> when the user
	 * has entered an invalid regular expression.
	 */
	void makeFilterTextFieldInvalid(String exceptionMessage) {
		filterTextField.setForeground(Color.RED);
		exceptionMessage = exceptionMessage.replaceAll("\\n", "<br>");
		filterTextField.setToolTipText(String.format(INVALID_FILTER_TEXT_FIELD_TOOLTIP, exceptionMessage));
	}

	/**
	 * Changes the appearance of <code>filterTextField</code> when the user
	 * has entered a valid regular expression.
	 */
	void makeFilterTextFieldValid() {
		filterTextField.setForeground(Color.BLACK);
		filterTextField.setToolTipText(VALID_FILTER_TEXT_FIELD_TOOLTIP);
	}

	/**
	 * Triggers the internal <code>update</code> method whenever the
	 * user modifies <code>filterTextField</code>.
	 */
	class FilterUpdater implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			update();
		}

		public void insertUpdate(DocumentEvent e) {
			update();
		}

		public void removeUpdate(DocumentEvent e) {
			update();
		}

		/**
		 * Compiles the regular expression specified in <code>filterTextField</code>
		 * and stores it in <code>currentPattern</code> if the regular expression
		 * is syntactically correct.
		 */
		void update() {
			currentPattern = null;

			String regex = filterTextField.getText();

			if (regex.length() != 0) {
				try {
					currentPattern = Pattern.compile(regex);
					makeFilterTextFieldValid();
				} catch (PatternSyntaxException e) {
					makeFilterTextFieldInvalid(e.getMessage());
					return;
				}
			}
			refreshSolicitedLogEvents();
		}
	}

	class LevelThresholdListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			refreshSolicitedLogEvents();
		}
	}

	class LogsTreeSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			refreshSolicitedLogEvents();
		}
	}

	class ClearAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			logEventsUpdater.cancel();
			allLogEvents.removeAll(solicitedLogEvents);
			solicitedLogEvents.clear();
			logViewer.clear();
		}
	}
	
	class ExportAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return;

			final File file = fileChooser.getSelectedFile();
			ExportTaskFactory taskFactory = new ExportTaskFactory(file);
			taskManager.execute(taskFactory.createTaskIterator());
		}
	}

	class ExportTaskFactory extends AbstractTaskFactory {
		private final File file;

		ExportTaskFactory(final File file) {
			this.file = file;
		}

		public TaskIterator createTaskIterator() {
			return new TaskIterator(new ExportTask(file));
		}
	}

	class ExportTask extends AbstractTask {
		private final File file;
		private boolean cancelled = false;

		public ExportTask(final File file) {
			this.file = file;
		}

		@Override
		public void run(TaskMonitor monitor) throws Exception {
			monitor.setTitle("Developer's Log Console: Save to file");
			monitor.setStatusMessage("Saving to " + file.getName());
			final PrintWriter output = new PrintWriter(file);
			final int numEvents = solicitedLogEvents.size();
			cancelled = false;
			for (int i = 0; (i < numEvents) && !cancelled; i++) {
				monitor.setProgress(i / ((double) numEvents));
				for (final LogEvent event : solicitedLogEvents) {
					output.write(event.toString());
					output.println();
				}
			}
			output.close();
		}

		@Override
		public void cancel() {
			cancelled = true;
		}
	}
}
