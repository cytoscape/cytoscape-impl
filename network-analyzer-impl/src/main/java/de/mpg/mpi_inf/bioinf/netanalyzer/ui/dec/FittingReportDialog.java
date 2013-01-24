package de.mpg.mpi_inf.bioinf.netanalyzer.ui.dec;

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
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog window which reports the results after fitting a function.
 * 
 * @author Yassen Assenov
 */
public class FittingReportDialog extends JDialog implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(FittingReportDialog.class);
	/**
	 * Initializes the common controls of <code>FittingReportDialog</code>.
	 * 
	 * @param aOwner The <code>Frame</code> from which this dialog is displayed.
	 * @param aTitle Dialog's title.
	 * @param aData Fit data to be displayed, encapsulated in a <code>FitData</code> instance.
	 */
	public FittingReportDialog(Frame aOwner, String aTitle, FitData aData) {
		super(aOwner, aTitle, true);
		initControls(aData);
		setLocationRelativeTo(aOwner);
	}

	/**
	 * Initializes the common controls of <code>FittingReportDialog</code>.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aTitle Dialog's title.
	 * @param aData Fit data to be displayed, encapsulated in a <code>FitData</code> instance.
	 */
	public FittingReportDialog(Dialog aOwner, String aTitle, FitData aData) {
		super(aOwner, aTitle, true);
		initControls(aData);
		setLocationRelativeTo(aOwner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == btnOK) {
			setVisible(false);
			dispose();
		} else if (src == btnHelp) {
			try {
				Desktop.getDesktop().browse(helpURL.toURI());
			} catch (Exception e1) {
				logger.warn("failed to open url: " + helpURL.toString(),e1);
			}
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1519314486190160307L;

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 * 
	 * @param aData Fit data to be displayed.
	 */
	private void initControls(FitData aData) {
		final int bs = Utils.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(0, bs));
		Utils.setStandardBorder(contentPane);
		final Box boxData = new Box(BoxLayout.PAGE_AXIS);
		contentPane.add(boxData, BorderLayout.CENTER);

		// Display informative message to the user
		contentPane.add(new JLabel(aData.getMessage(), SwingConstants.CENTER), BorderLayout.NORTH);

		// Display coefficients of the fitted function
		final Point2D.Double coefs = aData.getCoefs();
		final JTextField txfA = new JTextField(Utils.doubleToString(coefs.x, 6, 3));
		txfA.setEditable(false);
		txfA.setColumns(9);
		final JTextField txfB = new JTextField(Utils.doubleToString(coefs.y, 6, 3));
		txfB.setEditable(false);
		txfB.setColumns(9);
		final Box boxCoefs = new Box(BoxLayout.LINE_AXIS);
		boxCoefs.add(new JLabel("a = "));
		boxCoefs.add(txfA);
		boxCoefs.add(Box.createHorizontalStrut(bs * 2));
		boxCoefs.add(new JLabel("b = "));
		boxCoefs.add(txfB);
		boxCoefs.add(Box.createHorizontalGlue());
		boxCoefs.setAlignmentX(Component.CENTER_ALIGNMENT);
		boxData.add(boxCoefs);
		boxData.add(Box.createVerticalStrut(bs * 2));

		// Display goodness fit measures
		final JPanel panMeasures = new JPanel(new GridLayout(0, 2, bs, bs));
		final Double corr = aData.getCorrelation();
		if (corr != null) {
			createReport(panMeasures, Messages.DI_CORR, corr);
		}
		final Double rSquared = aData.getRSquared();
		if (rSquared != null) {
			createReport(panMeasures, Messages.DI_RSQUARED, rSquared);
		}
		if (corr != null || rSquared != null) {
			panMeasures.setAlignmentX(Component.CENTER_ALIGNMENT);
			boxData.add(panMeasures);
			boxData.add(Box.createVerticalStrut(bs * 2));
		}

		// Display a note
		final String note = aData.getNote();
		if (note != null && note.length() > 0) {
			final JLabel label = new JLabel("<html><b>Note:</b> " + note + "</html>");
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			boxData.add(label);
			boxData.add(Box.createVerticalStrut(bs * 2));
		}

		// Add "OK" and "Help" buttons
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, bs, 0));
		btnOK = Utils.createButton(Messages.DI_OK, null, this);
		btnHelp = Utils.createButton(Messages.DI_HELP, null, this);
		try {
			helpURL = new URL(aData.getHelpURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			logger.warn("bad url: " + aData.getHelpURL(), e);
		}
		Utils.equalizeSize(btnOK, btnHelp);
		bottomPanel.add(btnOK);
		bottomPanel.add(btnHelp);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);
		pack();
	}

	private void createReport(JPanel aPanel, String aMessage, Double aValue) {
		aPanel.add(new JLabel(aMessage, SwingConstants.TRAILING));
		final JTextField txf = new JTextField(Utils.doubleToString(aValue, 6, 3));
		txf.setEditable(false);
		txf.setColumns(9);
		aPanel.add(txf);
	}

	/**
	 * &quot;OK&quot; button.
	 */
	private JButton btnOK;

	/**
	 * &quot;Help&quot; button.
	 */
	private JButton btnHelp;

	/**
	 * URL to be visited when the user clicks on the &quot;Help&quot; button.
	 */
	private URL helpURL;
}
