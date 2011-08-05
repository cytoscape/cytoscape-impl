
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.filter.internal.quickfind.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.filter.internal.prefuse.data.query.NumberRangeModel;
import org.cytoscape.filter.internal.prefuse.util.ui.JRangeSlider;
import org.cytoscape.filter.internal.quickfind.plugin.QuickFindPlugIn;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.index.IndexFactory;
import org.cytoscape.filter.internal.widgets.autocomplete.index.NumberIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.index.TextIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.view.ComboBoxFactory;
import org.cytoscape.filter.internal.widgets.autocomplete.view.TextIndexComboBox;
import org.cytoscape.filter.internal.widgets.slider.JRangeSliderExtended;
import org.cytoscape.session.CyApplicationManager;



/**
 * Quick Find UI Panel.
 *
 * @author Ethan Cerami.
 */
public class QuickFindPanel extends JPanel {
	private TextIndexComboBox comboBox;
	private JButton configButton;
	private JRangeSliderExtended rangeSlider;
	private NumberRangeModel rangeModel;
	private JLabel label;
	private final CyApplicationManager applicationManager;
	private final CySwingApplication application;
	private static final String SEARCH_STRING = "Search:  ";
	private static final String SELECT_STRING = "Select:  ";

	/**
	 * Constructor.
	 */
	public QuickFindPanel(CyApplicationManager applicationManager, CySwingApplication application) {
		this.applicationManager = applicationManager;
		this.application = application;
		
		//  Must use BoxLayout, as we want to control width
		//  of all components.
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		comboBox = createTextIndexComboBox();
		configButton = createConfigButton();
		label = createSearchLabel();
		rangeModel = new NumberRangeModel(0.0, 0.0, 0.0, 0.0);
		rangeSlider = createSlider(rangeModel, comboBox);

		add(label);
		add(comboBox);
		add(rangeSlider);
		add(configButton);

		//  Add Right Buffer, to prevent config button from occassionally
		//  being partially obscured.
		add(Box.createHorizontalStrut(5));
	}

	/**
	 * Sets Current Index.
	 *
	 * @param index Generic Index Object.
	 */
	public void setIndex(GenericIndex index) {
		if (index instanceof TextIndex) {
			comboBox.setVisible(true);
			rangeSlider.setVisible(false);
			label.setText(SEARCH_STRING);
			comboBox.setTextIndex((TextIndex) index);
		} else if (index instanceof NumberIndex) {
			NumberIndex numberIndex = (NumberIndex) index;
            //  by creating a new NumberRangeModel, the model retains the
            //  number setting, e.g. double or integer;  otherwise, you get
            //  the bug described in #1315.			
            rangeModel = new NumberRangeModel(numberIndex.getMinimumValue(),
                    numberIndex.getMinimumValue(), numberIndex.getMinimumValue(),
                    numberIndex.getMaximumValue());
            rangeSlider.setModel(rangeModel);
            comboBox.setVisible(false);
			rangeSlider.setVisible(true);
			label.setText(SELECT_STRING);
		}
		else {
			// index = null, because there is no enough time to create the index, before switch to other network
			// just ignore it.
			return;
		}

		enableAllQuickFindButtons();
	}

	/**
	 * No Network Current Available.
	 */
	public void noNetworkLoaded() {
		disableAllQuickFindButtons();
		comboBox.setToolTipText("Please select or load a network");
		rangeSlider.setToolTipText("Please select or load a network");
	}

	/**
	 * Indexing Operating in Progress.
	 */
	public void indexingInProgress() {
		disableAllQuickFindButtons();
		comboBox.setToolTipText("Indexing network.  Please wait...");
		rangeSlider.setToolTipText("Indexing network.  Please wait...");
	}

	/**
	 * Gets the TextIndexComboBox Widget.
	 *
	 * @return TextIndexComboBox Widget.
	 */
	public TextIndexComboBox getTextIndexComboBox() {
		return comboBox;
	}

	/**
	 * Gets the Range Slider Widget.
	 *
	 * @return JRangeSliderExtended Object.
	 */
	public JRangeSliderExtended getSlider() {
		return this.rangeSlider;
	}

	/**
	 * Disables all Quick Find Buttons.
	 */
	private void disableAllQuickFindButtons() {
		comboBox.removeAllText();
		comboBox.setEnabled(false);
		rangeSlider.setEnabled(false);
		comboBox.setVisible(true);
		rangeSlider.setVisible(false);
		configButton.setEnabled(false);
		label.setForeground(Color.GRAY);
	}

	/**
	 * Enables all Quick Find Buttons.
	 */
	public void enableAllQuickFindButtons() {
		comboBox.setToolTipText("Enter search string");
		rangeSlider.setToolTipText("Select range");
		comboBox.setEnabled(true);
		rangeSlider.setEnabled(true);
		configButton.setEnabled(true);
		label.setForeground(Color.BLACK);
	}

	/**
	 * Creates Configure QuickFind Button.
	 *
	 * @return JButton Object.
	 */
	private JButton createConfigButton() {
		URL configIconUrl = getClass().getResource("/images/config.png");
		ImageIcon configIcon = new ImageIcon(configIconUrl, "Configure search options");
		JButton button = new JButton(configIcon);
		button.setToolTipText("Configure search options");
		button.setEnabled(false);
		button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new QuickFindConfigDialog(applicationManager, application);
				}
			});
		button.setBorderPainted(false);

		return button;
	}

	/**
	 * Creates TextIndex ComboBox.
	 *
	 * @return TextIndexComboBox Object.
	 */
	private TextIndexComboBox createTextIndexComboBox() {
		TextIndexComboBox box = null;

		try {
			TextIndex textIndex = IndexFactory.createDefaultTextIndex(QuickFind.INDEX_NODES);
			box = ComboBoxFactory.createTextIndexComboBox(textIndex, 2.0);
			box.setEnabled(false);

			//  Set Size of ComboBox Display, based on # of specific chars
			box.setPrototypeDisplayValue("01234567");
			box.setToolTipText("Please select or load a network to "
			                   + "activate search functionality.");

			//  Set Max Size of ComboBox to match preferred size
			box.setMaximumSize(box.getPreferredSize());

			return box;
		} catch (Exception e) {
		}

		return box;
	}

	/**
	 * Creates Search Label.
	 */
	private JLabel createSearchLabel() {
		JLabel label = new JLabel(SEARCH_STRING);
		label.setBorder(new EmptyBorder(0, 5, 0, 0));
		label.setForeground(Color.GRAY);

		//  Fix width of label
		label.setMaximumSize(label.getPreferredSize());

		return label;
	}

	/**
	 * Creates Slider Widget.
	 *
	 * @param box TextIndexComboBox (used to tweak size of slider).
	 * @return JRangeSliderExteneded Object
	 */
	private JRangeSliderExtended createSlider(BoundedRangeModel model, TextIndexComboBox box) {
		JRangeSliderExtended slider = new JRangeSliderExtended(model, JRangeSlider.HORIZONTAL,
		                                                       JRangeSlider.LEFTRIGHT_TOPBOTTOM);

		//  Hide slider range for now.
		slider.setVisible(false);

		//  Create Border
		slider.setBorder(new LineBorder(Color.GRAY, 1));

		//  Box Layout will respect the components' max size.
		//  Therefore set max size to match preferred size.
		Dimension dComboBox = box.getPreferredSize();
		Dimension dSlider = slider.getPreferredSize();

		//  Set RangeSlider to match width of combo box.
		dSlider.width = dComboBox.width;
		dSlider.height = dSlider.height + 3;
		slider.setMaximumSize(dSlider);
		slider.setPreferredSize(dSlider);

		return slider;
	}
}
