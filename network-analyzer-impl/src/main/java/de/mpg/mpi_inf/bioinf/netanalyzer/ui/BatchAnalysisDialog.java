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
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.cytoscape.util.swing.LookAndFeelUtil;

import de.mpg.mpi_inf.bioinf.netanalyzer.BatchNetworkAnalyzer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Dialog showing the progress of the batch analysis of networks.
 * 
 * @author Yassen Assenov
 * @author Nadezhda Doncheva
 */
public class BatchAnalysisDialog extends JDialog {

	/**
	 * Initializes a new instance of <code>BatchAnalysisDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aBatchAnalyzer
	 */
	public BatchAnalysisDialog(Frame aOwner, BatchNetworkAnalyzer aBatchAnalyzer) {
		super(aOwner, Messages.DT_BATCHANALYSIS, true);
		resultsPressed = false;
		batchAnalyzer = aBatchAnalyzer;
		analyzerStarted = false;
		aBatchAnalyzer.setDialog(this);

		initControls();
		pack();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(aOwner);
	}

	@Override
	public void setVisible(boolean b) {
		if (!analyzerStarted) {
			analyzerStarted = true;
			timer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					proAnalysis.setValue(batchAnalyzer.getCurrentProgress());
					repaint();
				}
			});
			timer.start();
			batchAnalyzer.start();
		}
		super.setVisible(b);
	}

	/**
	 * Indicates that the batch analysis has finished and enables the &quot;Show Results&quot; button.
	 */
	public void analysisFinished() {
		synchronized (this) {
			if (timer != null) {
				timer.stop();
				timer = null;
				proAnalysis.setValue(proAnalysis.getMaximum());
				btnCancel.setText(Messages.DI_CLOSE);
				btnResults.getAction().setEnabled(true);
			}
			btnCancel.getAction().setEnabled(true);
		}
	}

	/**
	 * Gets the status of the flag indicating if the &quot;Show Results&quot; button is pressed.
	 * 
	 * @return <code>true</code> if the &quot;Show Results&quot; button is pressed, and <code>false</code>
	 *         otherwise.
	 */
	public boolean resultsPressed() {
		return resultsPressed;
	}

	/**
	 * Appends a string to the other text in the dialog window.
	 * 
	 * @param aString
	 *            String to be appended.
	 */
	public void write(String aString) {
		texOutput.append(aString);
		texOutput.setCaretPosition(texOutput.getDocument().getLength());
		repaint();
	}

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	@SuppressWarnings("serial")
	private void initControls() {
		final int BS = Utils.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		Utils.setStandardBorder(contentPane);

		// Add network number info and progress bar.
		final JPanel panTop = new JPanel(new BorderLayout(BS, BS));
		final JLabel labTitle = new JLabel(Messages.DI_AUTOANALYSIS1 + batchAnalyzer.getInputFilesCount()
				+ Messages.DI_AUTOANALYSIS2, SwingConstants.CENTER);
		panTop.add(labTitle, BorderLayout.NORTH);
		proAnalysis = new JProgressBar(0, batchAnalyzer.getMaxProgress());
		proAnalysis.setValue(0);
		proAnalysis.setStringPainted(true);
		panTop.add(proAnalysis, BorderLayout.SOUTH);
		contentPane.add(panTop, BorderLayout.NORTH);

		// Add console
		texOutput = new JTextArea(10, 40);
		texOutput.setMargin(new Insets(5, 5, 5, 5));
		texOutput.setEditable(false);
		texOutput.setCursor(null);
		texOutput.setFont(labTitle.getFont());
		JPanel panConsole = new JPanel(new BorderLayout(0, 0));
		panConsole.setBorder(BorderFactory.createEmptyBorder(BS, BS, BS, BS));
		final int vsp = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		final int hsp = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
		panConsole.add(new JScrollPane(texOutput, vsp, hsp), BorderLayout.CENTER);
		contentPane.add(panConsole, BorderLayout.CENTER);

		// Add Cancel and Results buttons
		btnCancel = Utils.createButton(new AbstractAction(Messages.DI_CANCEL) {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (this) {
					if (timer == null) {
						setVisible(false);
						dispose();
					} else {
						btnCancel.getAction().setEnabled(false);
						btnCancel.setText(Messages.DI_CLOSE);
						timer.stop();
						timer = null;
						batchAnalyzer.cancel();
					}
				}
			}
		}, null);
		btnResults = Utils.createButton(new AbstractAction(Messages.DI_RESULTS) {
			@Override
			public void actionPerformed(ActionEvent e) {
				resultsPressed = true;
				setVisible(false);
				dispose();
			}
		}, null);
				
		Utils.equalizeSize(btnResults, btnCancel);
		btnResults.getAction().setEnabled(false);
		
		final JPanel panBottom = LookAndFeelUtil.createOkCancelPanel(btnResults, btnCancel);
		
		contentPane.add(panBottom, BorderLayout.SOUTH);
		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnResults.getAction(), btnCancel.getAction());
		getRootPane().setDefaultButton(btnResults);
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1074422259082449432L;

	/**
	 * Flag indicating if the analysis has started yet.
	 */
	private boolean analyzerStarted;

	/**
	 * Instance of <code>BatchNetworkAnalyzer</code>.
	 */
	private BatchNetworkAnalyzer batchAnalyzer;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * &quot;Show Results&quot; button.
	 */
	private JButton btnResults;

	/**
	 * A progress bar in the dialog showing how many networks have been processed.
	 */
	private JProgressBar proAnalysis;

	/**
	 * Flag indicating if the user had pressed the "Show Results" button.
	 */
	private boolean resultsPressed;

	/**
	 * A text console that logs the progress of the analysis
	 */
	private JTextArea texOutput;

	/**
	 * Timer responsible for regular updates of the progress monitor.
	 */
	private Timer timer;

}
