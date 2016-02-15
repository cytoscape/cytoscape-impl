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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobHandler;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.CyJobStatus.Status;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

import org.apache.log4j.Logger;

/**
 * An implementation of CyJobManager.
 */
public class GUIJobDialog extends JDialog {
	final CyServiceRegistrar serviceRegistrar;
	final ConcurrentMap<CyJob, CyJobStatus> statusMap;
	final List<CyJob> jobList;
	static final long serialVersionUID = 1001L;

	public GUIJobDialog(CyServiceRegistrar registrar, 
	                    CySwingApplication swingApp,
	                    ConcurrentMap<CyJob, CyJobStatus> statusMap) {
		super(swingApp.getJFrame(),"Job Monitor");
		this.serviceRegistrar = registrar;
		this.statusMap = statusMap;
		this.jobList = new ArrayList<>();
		for (CyJob job: statusMap.keySet()) {
			jobList.add(job);
		}
		initUI();
	}

	public void initUI() {
		// Create table of jobs
		JTable table = new JTable(new JobTableModel(this));
		// Put in JScrollPane
		JScrollPane scrollPane = new JScrollPane(table);
		// add to dialog
		add(scrollPane);
		setPreferredSize(new Dimension(500,100));
	}

	public void mapChanged() {
		jobList.clear();
		for (CyJob job: statusMap.keySet()) {
			jobList.add(job);
		}
	}

	public String jobToString(CyJob job) {
		return job.getJobName()+"("+job.getJobId()+")";
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
						return new ErrorButton(job, status, jobDialog, JOptionPane.ERROR_MESSAGE); 
					case FAILED:
					case PURGED:
					case TERMINATED:
						return new ErrorButton(job, status, jobDialog, JOptionPane.WARNING_MESSAGE); 
					case QUEUED:
					case RUNNING:
					case SUBMITTED:
					case UNKNOWN:
						return new CancelButton(job);
					case FINISHED:
						return new LoadDataButton(job);
				}
			}
			return null;
		}
	}

	class CancelButton extends JButton {
		static final long serialVersionUID = 1004L;
		final CyJob job;
		public CancelButton(final CyJob job) {
			super("Cancel Job");
			this.job = job;
			setAction(new AbstractAction() {
				static final long serialVersionUID = 1005L;
				public void actionPerformed(ActionEvent e) {
					job.getJobExecutionService().cancelJob(job);
					jobList.remove(job);
					statusMap.remove(job);
				}
			});
		}
	}

	class ErrorButton extends JButton {
		final CyJob job;
		final CyJobStatus jobStatus;
		final int messageType;
		final JDialog parent;
		static final long serialVersionUID = 1006L;

		public ErrorButton(final CyJob job, final CyJobStatus jobStatus, 
		                   final JDialog dialog, final int messageType) {
			super("Show Error");
			this.job = job;
			this.jobStatus = jobStatus;
			this.parent = dialog;
			this.messageType = messageType;
			setAction(new AbstractAction() {
				static final long serialVersionUID = 1007L;
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(parent, jobStatus.toString(),
					                              "Job "+jobToString(job),
																				messageType);
					jobList.remove(job);
					statusMap.remove(job);
				}
			});
		}
	}

	class LoadDataButton extends JButton {
		final CyJob job;
		static final long serialVersionUID = 1008L;

		public LoadDataButton(final CyJob job) {
			super("Load data");
			this.job = job;
			setAction(new AbstractAction() {
				static final long serialVersionUID = 1009L;
				public void actionPerformed(ActionEvent e) {
					TaskManager<?,?> taskManager = serviceRegistrar.getService(TaskManager.class);
					taskManager.execute(new TaskIterator(new LoadDataTask(job)));
					jobList.remove(job);
					statusMap.remove(job);
				}
			});
		}
	}

	class LoadDataTask extends AbstractTask {
		final CyJob job;
		public LoadDataTask(final CyJob job) {
			this.job = job;
		}

		public void run(TaskMonitor monitor) {
			CyJobHandler jobHandler = job.getJobHandler();
			if (jobHandler != null)
				jobHandler.loadData(job, monitor);
		}
	}

}
