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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import de.mpg.mpi_inf.bioinf.netanalyzer.OpenBrowser;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStats;
import de.mpg.mpi_inf.bioinf.netanalyzer.sconnect.HelpConnector;

/**
 * Panel (group box) displaying the simple network statistics.
 * 
 * @author Yassen Assenov
 */
public class SimpleStatsPanel extends JPanel
	implements ActionListener {

	/**
	 * Initializes a new instance of <code>SimpleStatsPanel</code>.
	 * 
	 * @param aStats Network statistics, whose simple parameters are to be visualized.
	 */
	public SimpleStatsPanel(NetworkStats aStats) {
		super(new BorderLayout(0, Utils.BORDER_SIZE));

		String[] statNames = aStats.getComputedSimple();
		int nameCount = statNames.length;

		JPanel simpleStatsPanel = new JPanel();
		Box descrCol = new Box(BoxLayout.Y_AXIS);
		simpleStatsPanel.add(descrCol);
		Box valueCol = new Box(BoxLayout.Y_AXIS);
		simpleStatsPanel.add(valueCol);

		if (nameCount > 3) {
			// Order simple parameters in two columns
			simpleStatsPanel.add(Box.createHorizontalStrut(12));
			Box descrCol2 = new Box(BoxLayout.Y_AXIS);
			simpleStatsPanel.add(descrCol2);
			Box valueCol2 = new Box(BoxLayout.Y_AXIS);
			simpleStatsPanel.add(valueCol2);
			int half = nameCount / 2 + nameCount % 2;
			addDescrValuePair(aStats, statNames, 0, half, descrCol, valueCol);
			addDescrValuePair(aStats, statNames, half, nameCount, descrCol2, valueCol2);
		} else {
			// Order simlple parameters in one column
			addDescrValuePair(aStats, statNames, 0, nameCount, descrCol, valueCol);
		}
		add(simpleStatsPanel, BorderLayout.CENTER);

		// Add a help button
		final int bs = Utils.BORDER_SIZE;
		JPanel panHelp = new JPanel(new FlowLayout(FlowLayout.TRAILING, bs, bs));
		panHelp.add(btnHelp = Utils.createButton(Messages.DI_HELP, Messages.TT_ONLHELP, this));
		add(panHelp, BorderLayout.SOUTH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnHelp) {
			OpenBrowser.openURL(HelpConnector.getParamURL("simple"));
		}
	}

	/**
	 * (Relative) maximum number of symbols used to display a real value.
	 */
	private static final int NUMBER_LENGTH_MAX = 8;

	/**
	 * Maximum precision of a real value to be displayed.
	 */
	private static final int PRECISION_MAX = 3;

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 6346913948150828482L;

	/**
	 * Adds pairs of labels to display the descriptions of simple network parameters along with
	 * their values.
	 * 
	 * @param aNames Array of parameter IDs.
	 * @param aFromIndex Starting index of IDs for the parameters to add.
	 * @param aToIndex Ending index of IDs for the parameters to add. All parameters in the range [<code>aFromIndex</code>,
	 *        <code>aToIndex - 1</code>] are added.
	 * @param aDescrCont Container component for the description labels.
	 * @param aValueCont Container component for the value labels.
	 */
	private void addDescrValuePair(NetworkStats aStats, String[] aNames, int aFromIndex,
		int aToIndex, JComponent aDescrCont, JComponent aValueCont) {
		for (int i = aFromIndex; i < aToIndex; ++i) {
			String stat = aNames[i];
			JLabel messageLabel = new JLabel(Messages.get(stat) + " :", SwingConstants.RIGHT);
			messageLabel.setAlignmentX(1.0f);
			aDescrCont.add(messageLabel);
			aValueCont.add(createValueLabel(aStats, stat));
		}
	}

	/**
	 * Creates a label for the value of the given simple parameter.
	 * 
	 * @param aName ID of simple parameter to get the value of.
	 * @return Label with HTML text showing the value of the simple parameter <code>aName</code>.
	 */
	private JLabel createValueLabel(NetworkStats aStats, String aName) {
		Object value = aStats.get(aName);
		String labelText = value.toString();
		String toolTipText = null;
		if ("connPairs".equals(aName)) {
			long connPairs = aStats.getLong("connPairs");
			long totalPairs = aStats.getInt("nodeCount");
			totalPairs = totalPairs * (totalPairs - 1);
			long percentage = (connPairs * 100) / totalPairs;
			labelText = labelText + " (" + percentage + "%)";
		} else if (value instanceof Double) {
			toolTipText = labelText;
			labelText = Utils.doubleToString((Double) value, NUMBER_LENGTH_MAX, PRECISION_MAX);
		}
		return Utils.createLabel("<html><b>" + labelText + "</b></html>", toolTipText);
	}

	/**
	 * &quot;Help&quot; button.
	 */
	private JButton btnHelp;
}
