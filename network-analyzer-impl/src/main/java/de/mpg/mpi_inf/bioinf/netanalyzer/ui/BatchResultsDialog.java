package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

import de.mpg.mpi_inf.bioinf.netanalyzer.LoadNetstatsAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.SampleTaskMonitor;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkAnalysisReport;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;

/**
 * Dialog displaying the results of network batch analysis.
 * 
 * @author Yassen Assenov
 */
public class BatchResultsDialog extends JDialog implements ActionListener, ListSelectionListener {

	private final CyNetworkReaderManager cyNetworkViewReaderMgr;
	private final CyNetworkManager cyNetworkMgr;
	private final CyNetworkViewManager netViewMgr;
	
	private final Frame aOwner;
	
	private final LoadNetstatsAction action;

	/**
	 * Initializes a new instance of <code>BatchResultsDialog</code>.
	 * 
	 * @param aOwner
	 *            <code>Frame</code> from which this dialog is displayed.
	 * @param aReports
	 *            List of analysis reports to be visualized.
	 */
	public BatchResultsDialog(Frame aOwner, List<NetworkAnalysisReport> aReports, CyNetworkReaderManager cyNetworkViewReaderMgr, CyNetworkManager cyNetworkMgr,
			CyNetworkViewManager netViewMgr, final LoadNetstatsAction action) {
		super(aOwner, Messages.DT_BATCHRESULTS, false);
		this.action = action;
		
		init(aReports);
		setLocationRelativeTo(aOwner);
		this.cyNetworkViewReaderMgr = cyNetworkViewReaderMgr;
		this.cyNetworkMgr = cyNetworkMgr;
		this.netViewMgr = netViewMgr;
		this.aOwner = aOwner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnClose) {
			setVisible(false);
			dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			try {
				final AnalysisTableModel model = (AnalysisTableModel) tabResults.getModel();
				final int r = tabResults.getSelectedRow();
				final int c = tabResults.getSelectedColumn();
				if (lastSelected.x == c && lastSelected.y == r) {
					return;
				}
				lastSelected.x = c;
				lastSelected.y = r;
				if (c == 2) {
					final File file = model.getNetstatsFile(r);
					if (file != null) {
						action.openNetstats(aOwner,file);
					}
				} else if (c == 0) {
					// Open network and view when the user clicks on it
					final File file = model.getNetwork(r);
					if (file != null && file.isFile()) {
						CyNetworkReader reader = cyNetworkViewReaderMgr.getReader(file.toURI(), file.getName());
						try {
							// TODO Use the Task's task monitor
							reader.run(new SampleTaskMonitor());
							CyNetwork network = reader.getNetworks()[0];
							network.getRow(network).set(CyNetwork.NAME, file.getName());
							cyNetworkMgr.addNetwork(network);
							CyNetworkView view = reader.buildCyNetworkView(network);
							netViewMgr.addNetworkView(view);
						} catch (Exception ex) {
							// ignore
						}
					}
				}
			} catch (ClassCastException ex) {
				// Ignore
			} catch (IndexOutOfBoundsException ex) {
				// Ignore
			}
		}
	}

	/**
	 * Gets the number of networks discussed in the reports.
	 * 
	 * @param aReports
	 *            List of reports to be traversed.
	 * @return Number of unique instances for network files in the given reports.
	 * 
	 * @throws NullPointerException
	 *             If <code>aReports</code> is <code>null</code>.
	 */
	private static int getNetworkCount(List<NetworkAnalysisReport> aReports) {
		final Set<File> files = new HashSet<File>();
		for (final NetworkAnalysisReport report : aReports) {
			files.add(report.getNetwork());
		}
		return files.size();
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -4513989513140591013L;

	/**
	 * Maximum height, in pixels, of the table control as initially displayed in this dialog.
	 */
	private static final int TAB_HEIGHT_MAX = 400;

	/**
	 * Initializes and lays out the controls in this dialog.
	 * 
	 * @param aReports
	 *            List of analysis reports to be listed in a table.
	 */
	private void init(List<NetworkAnalysisReport> aReports) {
		lastSelected = new Point(-1, -1);
		final int BS = Utils.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		Utils.setStandardBorder(contentPane);

		// Add title label
		final String netCount = String.valueOf(getNetworkCount(aReports));
		final JLabel label = new JLabel(netCount + Messages.DI_BATCHREPORT, SwingConstants.CENTER);
		contentPane.add(label, BorderLayout.PAGE_START);

		// Add results table
		final AnalysisTableModel model = new AnalysisTableModel(aReports);
		tabResults = new JTable(model);
		tabResults.setDefaultRenderer(File.class, new FileCellRenderer());
		tabResults.setDefaultRenderer(NetworkInterpretation.class, new InterpretationCellRenderer());
		tabResults.getColumnModel().getSelectionModel().addListSelectionListener(this);
		tabResults.getSelectionModel().addListSelectionListener(this);
		tabResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		model.adjustDimensions(tabResults);
		final Dimension size = tabResults.getPreferredSize();
		if (size.height > TAB_HEIGHT_MAX) {
			size.height = TAB_HEIGHT_MAX;
		}
		tabResults.setPreferredScrollableViewportSize(size);
		contentPane.add(new JScrollPane(tabResults), BorderLayout.CENTER);

		// Add Close button
		btnClose = Utils.createButton(Messages.DI_CLOSE, null, this);
		final JPanel panButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panButton.add(btnClose);
		contentPane.add(panButton, BorderLayout.PAGE_END);

		setContentPane(contentPane);
		pack();
		setResizable(true);
	}

	/**
	 * &quot;Close&quot; button for closing this dialog.
	 */
	private JButton btnClose;

	/**
	 * Last selected table cell.
	 */
	private Point lastSelected;

	/**
	 * Table listing the analysis reports.
	 */
	private JTable tabResults;
}
