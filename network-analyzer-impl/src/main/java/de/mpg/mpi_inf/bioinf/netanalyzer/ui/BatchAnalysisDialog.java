/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import de.mpg.mpi_inf.bioinf.netanalyzer.BatchNetworkAnalyzer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Dialog showing the progress of the batch analysis of networks.
 * 
 * @author Yassen Assenov
 * @author Nadezhda Doncheva
 */
public class BatchAnalysisDialog extends JDialog implements ActionListener {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == btnCancel) {
			synchronized (this) {
				if (timer == null) {
					setVisible(false);
					dispose();
				} else {
					btnCancel.setEnabled(false);
					btnCancel.setText(Messages.DI_CLOSE);
					timer.stop();
					timer = null;
					batchAnalyzer.cancel();
				}
			}
		} else if (src == btnResults) {
			resultsPressed = true;
			setVisible(false);
			dispose();
		} else if (src == timer && timer != null) {
			proAnalysis.setValue(batchAnalyzer.getCurrentProgress());
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Dialog#setVisible
	 */
	@Override
	public void setVisible(boolean b) {
		if (!analyzerStarted) {
			analyzerStarted = true;
			timer = new Timer(1000, this);
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
				btnResults.setEnabled(true);
			}
			btnCancel.setEnabled(true);
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
		JPanel panBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JPanel panButtons = new JPanel(new GridLayout(1, 2, BS, 0));
		panButtons.add(btnCancel = Utils.createButton(Messages.DI_CANCEL, null, this));
		panButtons.add(btnResults = Utils.createButton(Messages.DI_RESULTS, null, this));
		btnResults.setEnabled(false);
		panBottom.add(panButtons);
		contentPane.add(panBottom, BorderLayout.SOUTH);
		setContentPane(contentPane);
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
