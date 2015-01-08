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

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStatus;

import javax.swing.*;

import org.cytoscape.util.swing.LookAndFeelUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Generic class for interpretation dialogs.
 * <p>
 * These are modal dialogs that either ask the user for an interpretation of the network edges, or
 * inform the user of a non-trivial interpretation that will be performed.
 * </p>
 * 
 * @author Yassen Assenov
 */
public final class InterpretationDialog extends JDialog implements ActionListener {

	/**
	 * Initializes a new instance of <code>InterpretationDialog</code>.
	 * <p>
	 * The dialog created is modal and has a title &quot;NetworkAnalyzer - Network
	 * Interpretation&quot;. The constructor creates and lays out all the controls of the dialog. It
	 * also positions the window according to its parent, so no subsequent calls to
	 * <code>pack()</code> or <code>setLocation(...)</code> are necessary.
	 * </p>
	 * 
	 * @param aOwner
	 *            The <code>JFrame</code> from which this dialog is displayed.
	 * @param aStatus
	 *            Status of the network to be analyzed.
	 * @throws HeadlessException
	 *             if <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>.
	 */
	public InterpretationDialog(JFrame aOwner, NetworkStatus aStatus)
			throws HeadlessException {
		super(aOwner, Messages.DT_INTERPRETATION, true);

		pressedOK = false;
		userChoice = aStatus.getDefaultInterprIndex();
		initControls(aStatus);
		pack();
		setLocationRelativeTo(aOwner);
	}
	
	// TODO: this is a hack!  We need clear separation between model and view.
	public InterpretationDialog(NetworkStatus aStatus, boolean directed)
			throws HeadlessException {
		super((JFrame)null, Messages.DT_INTERPRETATION, true);

		pressedOK = false;
		userChoice = aStatus.getDefaultInterprIndex();
		initControls(aStatus);

		if(directed) {
			
		} else {
			
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == btnOK) {
			pressedOK = true;
			this.setVisible(false);
			this.dispose();
		} else if (source == btnCancel) {
			userChoice = -1;
			this.setVisible(false);
			this.dispose();
		} else if (radOptions != null) {
			for (int i = 0; i < radOptions.length; ++i) {
				if (source == radOptions[i]) {
					userChoice = i;
					break;
				}
			}
		}
	}

	/**
	 * Checks if the user has chosen that the network analysis should proceed.
	 * <p>
	 * If (and only if) this method returns <code>true</code>, the
	 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation} instance passed to the
	 * constructor may be updated.
	 * </p>
	 * 
	 * @return <code>true</code> if the user has pressed the OK button of this dialog;
	 *         <code>false</code> otherwise.
	 */
	public boolean pressedOK() {
		return pressedOK;
	}

	/**
	 * Gets the user choice as a number.
	 * 
	 * @return Natural number representing the option the user has chosen; <code>-1</code> if the
	 *         user has pressed the &quot;Cancel&quot; button.
	 */
	public int getUserChoice() {
		return userChoice;
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -6915331490559980802L;

	/**
	 * Image of a horizontal arrow pointing to the right.
	 */
	private static ImageIcon arrowRight = de.mpg.mpi_inf.bioinf.netanalyzer.data.Utils.getImage(
			"arrow-right.png", "->");

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 * 
	 * @param aStatus
	 *            Status of the network to be analyzed.
	 */
	private void initControls(NetworkStatus aStatus) {
		JPanel contentPane = new JPanel(new BorderLayout(0, Utils.BORDER_SIZE));
		Utils.setStandardBorder(contentPane);

		JPanel interprPanel = new JPanel(new BorderLayout(0, Utils.BORDER_SIZE));
		// Add the title (information) label
		JLabel descrLabel = new JLabel(aStatus.getDescription(), SwingConstants.CENTER);
		JPanel panTitle = new JPanel();
		panTitle.add(descrLabel);
		interprPanel.add(panTitle, BorderLayout.NORTH);
		// Add the state of the network resulting from the interpretation
		interprPanel.add(initInterprPanel(aStatus), BorderLayout.CENTER);
		contentPane.add(interprPanel, BorderLayout.CENTER);

		// Add OK, Cancel and Help buttons
		btnOK = Utils.createButton(Messages.DI_OK, null, this);
		btnCancel = Utils.createButton(Messages.DI_CANCEL, null, this);
		Utils.equalizeSize(btnOK, btnCancel);
		JPanel panButtons = LookAndFeelUtil.createOkCancelPanel(btnOK, btnCancel);
		
		contentPane.add(panButtons, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().add(contentPane);
		getRootPane().setDefaultButton(btnOK);
		btnOK.requestFocusInWindow();
	}

	/**
	 * Creates and lays out the controls that visualize the possible network interpretations.
	 * <p>
	 * In case multiple interpretations are possible, this method creates and lays out radio boxes,
	 * stored in {@link #radOptions}.
	 * </p>
	 * 
	 * @param aStatus
	 *            Network status.
	 * @return Newly created panel containing the images visualizing network interpretations and the
	 *         controls for choosing one (if multiple are possible).
	 */
	private JPanel initInterprPanel(NetworkStatus aStatus) {
		final NetworkInterpretation[] trs = aStatus.getInterpretations();
		final JPanel panBody = new JPanel(new GridBagLayout());
		panBody.setBorder(LookAndFeelUtil.createTitledBorder(Messages.DI_INTERPR));
		
		ButtonGroup group = null;
		
		if (trs.length > 1) {
			group = new ButtonGroup();
			radOptions = new JRadioButton[trs.length];
		}
		
		final GridBagConstraints c = new GridBagConstraints();
		c.insets.top = Utils.BORDER_SIZE;
		c.insets.left = Utils.BORDER_SIZE / 2;
		c.insets.right = Utils.BORDER_SIZE / 2;
		c.gridy = 0;
		c.gridheight = trs.length;
		c.anchor = GridBagConstraints.CENTER;

		final JLabel labImage = new JLabel(aStatus.getIcon());
		labImage.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		panBody.add(labImage, c);
		
		c.gridheight = 1;

		for (int i = 0; i < trs.length; ++i) {
			c.gridx = 1;
			c.anchor = GridBagConstraints.WEST;
			
			if (trs[i].getIcon() != null) {
				panBody.add(new JLabel(arrowRight), c);
				final JLabel labIcon = new JLabel(trs[i].getIcon());
				labIcon.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
				
				c.gridx++;
				panBody.add(labIcon, c);
				c.gridx++;
			}
			
			if (trs.length > 1) {
				radOptions[i] = new JRadioButton(trs[i].getMessage());
				radOptions[i].addActionListener(this);
				
				if (aStatus.getDefaultInterprIndex() == i)
					radOptions[i].setSelected(true);
				
				group.add(radOptions[i]);
				panBody.add(radOptions[i], c);
			} else {
				panBody.add(new JLabel(trs[i].getMessage()), c);
			}
			
			c.gridy++;
		}

		return panBody;
	}

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * &quot;OK&quot; button.
	 */
	private JButton btnOK;

	/**
	 * Flag indicating if the user has pressed the OK button.
	 */
	private boolean pressedOK;

	/**
	 * Array of the radio buttons giving the interpretation options.
	 * <p>
	 * The number of options depends on the
	 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStatus} instance passed to the
	 * constructor.
	 * </p>
	 */
	private JRadioButton[] radOptions;

	/**
	 * User's choice as a positive natural number.
	 * <p>
	 * This field is initialed to <code>0</code> and is updated only when the user presses the
	 * &quot;OK&quot; button.
	 * </p>
	 */
	private int userChoice;
}
