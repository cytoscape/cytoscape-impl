package org.cytoscape.jobs.internal;

/*
 * #%L
 * Cytoscape Jobs Impl (jobs-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.CyJobStatus.Status;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

/**
 * An implementation of CyJobManager.
 */
@SuppressWarnings("serial")
public class GUIJobDialog extends JDialog {
	
	final CyServiceRegistrar serviceRegistrar;
	final ConcurrentMap<CyJob, CyJobStatus> statusMap;
	final List<CyJob> jobList;
	final CyJobManagerImpl jobManager;
	final GUICyJobMonitor jobMonitor;
	JobTableModel jobTableModel;
	final JDialog jobDialog;

	public GUIJobDialog(CyServiceRegistrar registrar, 
	                    CySwingApplication swingApp,
	                    ConcurrentMap<CyJob, CyJobStatus> statusMap,
											CyJobManagerImpl jobManager,
											GUICyJobMonitor jobMonitor) {
		super();
		this.setTitle("Job Monitor");
		this.serviceRegistrar = registrar;
		this.statusMap = statusMap;
		this.jobManager = jobManager;
		this.jobMonitor = jobMonitor;
		this.jobList = new ArrayList<>();
		for (CyJob job: statusMap.keySet()) {
			jobList.add(job);
		}
		initUI();
		pack();
		// setLocationRelativeTo(swingApp.getJFrame());
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		jobDialog = this;
	}

	public void initUI() {
		// Create table of jobs
		jobTableModel = new JobTableModel(this);
		JTable table = new JTable(jobTableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// Put in JScrollPane
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(500, 100));
		// Set up our button renderer
		TableCellRenderer buttonRenderer = new JTableButtonRenderer();
		table.setDefaultRenderer(JButton.class, buttonRenderer);
		TableCellEditor buttonEditor = new ButtonEditor(new JCheckBox());
		table.setDefaultEditor(JButton.class, buttonEditor);
		table.setRowHeight(30);
		// Create bottom panel
		JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		JPanel bottomPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, closeButton.getAction());
		// add to dialog
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	public void mapChanged() {
		jobList.clear();
		for (CyJob job: statusMap.keySet()) {
			jobList.add(job);
		}
		jobTableModel.fireTableDataChanged();
	}

	public String jobToString(CyJob job) {
		return job.getJobName()+"("+job.getJobId()+")";
	}

	String getButtonText(String action) {
		if (action == null) return "";
		if (action.equals("cancel"))
			return "Cancel Job";
		if (action.equals("error"))
			return "Show Error";
		if (action.equals("warning"))
			return "Show Error";
		if (action.equals("load"))
			return "Load Data";
		return null;
	}


	class JobTableModel extends AbstractTableModel {
		static final long serialVersionUID = 1002L;
		final JDialog jobDialog;
		public JobTableModel(JDialog dialog) {
			this.jobDialog = dialog; 
		}

		public boolean isCellEditable(int row, int column) {
			if (column == 3) return true;
			return false;
		}

		public String getColumnName(int column) {
			switch(column) {
			case 0:
				return "Job Name";
			case 1:
				return "Job Id";
			case 2:
				return "Current Status";
			case 3:
				return "";
			}
			return "";
		}

		public Class<?> getColumnClass(int column) {
			switch(column) {
			case 0:
			case 1:
			case 2:
				return String.class;
			case 3:
				return JButton.class;
			}
			return String.class;
		}

		public int getRowCount() { return statusMap.size(); }

		public int getColumnCount() { return 4; }

		public Object getValueAt(int row, int column) {
			CyJob job = jobList.get(row);
			CyJobStatus status = statusMap.get(job);
			Status jobStatus = status.getStatus();
			switch (column) {
			case 0:
				return job.getJobName();
			case 1:
				return job.getJobId();
			case 2:
				return jobStatus.toString();
			case 3:
				switch (jobStatus) {
					case ERROR:
					case FAILED:
						return "error";
						// return new ErrorButton(job, status, jobDialog, JOptionPane.ERROR_MESSAGE); 
					case PURGED:
					case TERMINATED:
					case CANCELED:
					case UNKNOWN:
						return "warning";
						// return new ErrorButton(job, status, jobDialog, JOptionPane.WARNING_MESSAGE); 
					case QUEUED:
					case RUNNING:
					case SUBMITTED:
						return "cancel";
						// return new CancelButton(job);
					case FINISHED:
						return "load";
						// return new LoadDataButton(job);
				}
			}
			return null;
		}
	}

	class JTableButtonRenderer extends JButton implements TableCellRenderer {
		public JTableButtonRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, 
		                                               boolean isSelected, boolean hasFocus, 
																		               int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(UIManager.getColor("Button.background"));
			}
			
			setText(getButtonText((String)value));
			return this;
		}
	}

	class ButtonEditor extends DefaultCellEditor {
		protected JButton button;
		private String action;
		private boolean isPushed;
		private CyJob job;
		private CyJobStatus jobStatus;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
		                                             boolean isSelected, 
																		             int row, int column) {
			action = (String)value;
			button.setText(getButtonText(action));
			isPushed = true;
			job = jobList.get(row);
			jobStatus = statusMap.get(job);
			return button;
		}

		public Object getCellEditorValue() {
			if (isPushed) {
				isPushed = false;
				if (action.equals("cancel")) {
					// System.out.println("Cancelling job");
					CyJobStatus status = job.getJobExecutionService().cancelJob(job);
					if (status.getStatus().equals(Status.CANCELED)) {
						jobManager.removeJob(job);
						mapChanged();
						jobMonitor.jobStatusChanged(job, status);
					}
				} else if (action.equals("error")) {
					showMessage(jobDialog, JOptionPane.ERROR_MESSAGE);
				} else if (action.equals("warning")) {
					showMessage(jobDialog, JOptionPane.WARNING_MESSAGE);
				} else if (action.equals("load")) {
					// System.out.println("Loading data");
					TaskManager<?,?> taskManager = serviceRegistrar.getService(TaskManager.class);
					taskManager.execute(new TaskIterator(new LoadDataTask(job)));
				}
			}
			return action;
		}

		void showMessage(JDialog parent, int messageType) {
			JOptionPane.showMessageDialog(parent, jobStatus.toString(),
			                              "Job "+jobToString(job),
																		messageType);
			jobManager.removeJob(job);
			jobList.remove(job);
			statusMap.remove(job);
			mapChanged();
			jobMonitor.updateIcon();
		}
	}

	class LoadDataTask extends AbstractTask {
		final CyJob job;
		public LoadDataTask(final CyJob job) {
			this.job = job;
		}

		public void run(TaskMonitor monitor) {
			monitor.setTitle("Loading data for "+job.toString());
			CyJobMonitor jobMonitor = job.getJobMonitor();
			// System.out.println("Load data task");
			if (jobMonitor != null) {
				// System.out.println("Calling loadData");
				jobMonitor.loadData(job, monitor);
			}

			jobManager.removeJob(job);
			jobList.remove(job);
			statusMap.remove(job);
			mapChanged();
		}
	}

}
