package org.cytoscape.log.internal;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;


/**
 * @author Pasteur
 */
class AdvancedLogViewer {
	static final String[] COLUMNS = { "Time", "Log", "Level", "Thread", "Message" };

	/**
	 * Contains all log events (except those deleted with the Clear button) that were added with the
	 * <code>addLogEvent</code> method.
	 */
	final java.util.List<String[]> allLogEvents = new java.util.ArrayList<String[]>();

	/**
	 * Contains a subset of log events in <code>allLogEvents</code>. Only log events that are in the
	 * currently selected log and match the currently specified regular expression filter are stored
	 * in this array.
	 */
	java.util.List<String[]> solicitedLogEvents = new java.util.ArrayList<String[]>();

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
	SolicitedLogEventsUpdater currentUpdater = null;

	/**
	 * The compiled regular expression specified by the user in <code>filterTextField</code>.
	 */
	Pattern currentPattern = null;

	final TaskManager	taskManager;
	final JTextField	filterTextField;
	final JTree		logsTree;
	final LogViewer		logViewer;
	final JComboBox		filterTargetComboBox;
	final JPanel		contents;

	public AdvancedLogViewer(TaskManager taskManager, LogViewer logViewer) {
		this.taskManager = taskManager;
		this.logViewer = logViewer;
		logViewer.clear();
		contents = new JPanel();

		filterTextField = new JTextField();
		makeFilterTextFieldValid();
		filterTextField.getDocument().addDocumentListener(new FilterUpdater());

		logsTree = new JTree(new DefaultTreeModel(logs));
		logsTree.setSelectionPath(new TreePath(logs.getPath()));
		logsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		logsTree.addTreeSelectionListener(new LogsTreeSelectionListener());

		filterTargetComboBox = new JComboBox(COLUMNS);
		filterTargetComboBox.addActionListener(new FilterTargetUpdater());

		JScrollPane logsTreeScrollPane = new JScrollPane(logsTree);
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ClearAction());
		JButton exportButton = new JButton("Export...");
		exportButton.addActionListener(new ExportAction());

		contents.setLayout(new GridBagLayout());
		JPanel panel1 = new JPanel(new GridBagLayout());
		JLabel element0 = new JLabel("Filter: ");
		panel1.add(element0, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 6, 4, 2), 0, 0));
		panel1.add(filterTextField, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 2, 4), 0, 0));
		JLabel element1 = new JLabel("from");
		panel1.add(element1, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 2), 0, 0));
		panel1.add(filterTargetComboBox, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 2), 0, 0));
		contents.add(panel1, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		JSplitPane splitpane0 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logsTreeScrollPane, logViewer.getComponent());
		splitpane0.setResizeWeight(0.2);
		contents.add(splitpane0, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel2.add(clearButton);
		panel2.add(exportButton);
		contents.add(panel2, new GridBagConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}

	public JComponent getComponent()
	{
		return contents;
	}

	/**
	 * Add a log event
	 * @param event A 5-element array where <ul>
	 * <li><code>event[0]</code> specifies the date when the log event occurred</li>
	 * <li><code>event[1]</code> specifies a fully-qualified log name</li>
	 * <li><code>event[2]</code> specifies the level of the log event</li>
	 * <li><code>event[3]</code> specifies the name of the thread from which the log event was issued</li>
	 * <li><code>event[4]</code> specifies the log message</li>
	 * </ul>
	 */
	public void addLogEvent(String[] event)
	{
		allLogEvents.add(event);
		updateLogs(event[1]);
		if (logEventMatches(event, filterTargetComboBox.getSelectedIndex(), getSelectedLog()))
		{
			solicitedLogEvents.add(event);
			logViewer.append(event[2].toUpperCase(), event[4], formatEvent(event));
		}
	}

	String formatEvent(String[] event)
	{
		//return String.format("%s, <b>Level:</b> %s, <b>Log:</b> %s, <b>Thread:</b> %s", event[0], event[2], event[1], event[3]);
		return String.format("%s, Level: %s, Log: %s, Thread: %s", event[0], event[2], event[1], event[3]);
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
			final int target = filterTargetComboBox.getSelectedIndex();
			final String selectedPath = getSelectedLog();
			for (int i = 0; (i < allLogEvents.size()) && (!cancel); i++)
			{
				final String[] event = allLogEvents.get(i);
				if (logEventMatches(event, target, selectedPath))
				{
					solicitedLogEvents.add(event);
					logViewer.append(event[2].toUpperCase(), event[4], formatEvent(event));
				}
			}
		}

		public void cancel()
		{
			cancel = true;
		}
	}

	/**
	 * Determines if a log event matches the following criteria:
	 * a) Does the log event come from the currently selected log?
	 * b) Does the log event match the specified regular expression?
	 */
	boolean logEventMatches(String[] event, int target, String log)
	{
		if (log != null && !(event[1].startsWith(log)))
			return false;

		if (currentPattern != null && !currentPattern.matcher(event[target]).matches())
			return false;
		
		return true;
	}

	/**
	 * Creates a new instance of <code>SolicitedLogEventsUpdater</code> and
	 * executes it. If a <code>SolicitedLogEventsUpdater</code> is already
	 * running, this will cancel it.
	 */
	void refreshSolicitedLogEvents()
	{
		if (currentUpdater != null)
			currentUpdater.cancel();

		currentUpdater = new SolicitedLogEventsUpdater();
		solicitedLogEventsUpdaterExecutor.submit(currentUpdater);
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
					refreshSolicitedLogEvents();
				} catch (PatternSyntaxException e) {
					makeFilterTextFieldInvalid(e.getMessage());
				}
			} else
				refreshSolicitedLogEvents();
		}
	}

	class FilterTargetUpdater implements ActionListener {
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
			if (currentUpdater != null)
				currentUpdater.cancel();

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
			taskManager.execute(new ExportTaskFactory(file));
		}
	}

	class ExportTaskFactory implements TaskFactory {
		private final File file;

		ExportTaskFactory(final File file) {
			this.file = file;
		}

		public TaskIterator getTaskIterator() {
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
			monitor.setTitle("Developer Log Export");
			monitor.setStatusMessage("Saving to " + file.getName());
			PrintWriter output = new PrintWriter(file);
			for (int i = 0; (i < solicitedLogEvents.size()) && !cancelled; i++) {
				monitor.setProgress(i / ((double) solicitedLogEvents.size()));
				String[] entry = solicitedLogEvents.get(i);
				for (int j = 0; j < entry.length; j++)
				{
					output.print(entry[j]);
					if (j != entry.length - 1)
						output.print('\t');
				}
				output.println();
			}
			output.close();
		}

		@Override
		public void cancel() {
			cancelled = true;
		}
	}
}
