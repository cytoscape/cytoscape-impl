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

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import org.cytoscape.util.swing.LookAndFeelUtil;
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
public class BatchResultsDialog extends JDialog implements ListSelectionListener {

	private static final long serialVersionUID = -4513989513140591013L;

	/**
	 * Maximum height, in pixels, of the table control as initially displayed in this dialog.
	 */
	private static final int TAB_HEIGHT_MAX = 400;
	
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
	private JTable tblResults;
	
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
	public BatchResultsDialog(
			final Frame aOwner,
			final List<NetworkAnalysisReport> aReports,
			final CyNetworkReaderManager cyNetworkViewReaderMgr,
			final CyNetworkManager cyNetworkMgr,
			final CyNetworkViewManager netViewMgr,
			final LoadNetstatsAction action
	) {
		super(aOwner, Messages.DT_BATCHRESULTS, ModalityType.MODELESS);
		this.action = action;
		this.cyNetworkViewReaderMgr = cyNetworkViewReaderMgr;
		this.cyNetworkMgr = cyNetworkMgr;
		this.netViewMgr = netViewMgr;
		this.aOwner = aOwner;
		
		init(aReports);
		setResizable(false);
		setLocationRelativeTo(aOwner);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			try {
				final AnalysisTableModel model = (AnalysisTableModel) tblResults.getModel();
				final int r = tblResults.getSelectedRow();
				final int c = tblResults.getSelectedColumn();
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
	 * Initializes and lays out the controls in this dialog.
	 * 
	 * @param aReports
	 *            List of analysis reports to be listed in a table.
	 */
	@SuppressWarnings("serial")
	private void init(List<NetworkAnalysisReport> aReports) {
		lastSelected = new Point(-1, -1);

		// Add title label
		final int netCount = getNetworkCount(aReports);
		final String msg = netCount + (netCount == 1 ? " network was" : " networks were") + " analyzed:";
		final JLabel label = new JLabel(msg, SwingConstants.CENTER);

		// Add results table
		final AnalysisTableModel model = new AnalysisTableModel(aReports);
		tblResults = new JTable(model);
		tblResults.setDefaultRenderer(File.class, new FileCellRenderer());
		tblResults.setDefaultRenderer(NetworkInterpretation.class, new InterpretationCellRenderer());
		tblResults.getColumnModel().getSelectionModel().addListSelectionListener(this);
		tblResults.getSelectionModel().addListSelectionListener(this);
		tblResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		model.adjustDimensions(tblResults);
		
		final Dimension size = tblResults.getPreferredSize();
		
		if (size.height > TAB_HEIGHT_MAX)
			size.height = TAB_HEIGHT_MAX;
		
		tblResults.setPreferredScrollableViewportSize(size);
		final JScrollPane scrPane = new JScrollPane(tblResults);
		
		// Add Close button
		btnClose = Utils.createButton(new AbstractAction(Messages.DI_CLOSE) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		}, null);
		
		final JPanel panButton = LookAndFeelUtil.createOkCancelPanel(null, btnClose);

		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(label)
				.addComponent(scrPane, DEFAULT_SIZE, DEFAULT_SIZE, 680)
				.addComponent(panButton)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(label)
				.addComponent(scrPane, DEFAULT_SIZE, DEFAULT_SIZE, 480)
				.addComponent(panButton)
		);
		
		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, btnClose.getAction());
		getRootPane().setDefaultButton(btnClose);
		btnClose.requestFocusInWindow();
		
		pack();
	}
}
