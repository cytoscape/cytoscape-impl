package org.cytoscape.filter.internal.prefuse;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



/**
 * Extension of the Prefuse JRangeSlider.
 *
 * @author Ethan Cerami.
 * @see org.cytoscape.filter.internal.prefuse.util.ui.JRangeSlider
 */
@SuppressWarnings("serial")
public class JRangeSliderExtended extends JRangeSlider implements ChangeListener {
	
	private Popup popup;
	private JLabel popupLow;
	private JLabel popupHigh;
	private PopupDaemon popupDaemon;

	/**
	 * Create a new range slider.
	 *
	 * @param model       - a BoundedRangeModel specifying the slider's range
	 * @param orientation - construct a horizontal or vertical slider?
	 */
	public JRangeSliderExtended(BoundedRangeModel model, int orientation) {
		super(model, orientation);
		addChangeListener(this);
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		stateChanged(null);
		super.mouseEntered(event);
	}
	
	@Override
	public void mouseExited(MouseEvent event) {
		if (event.getButton() == MouseEvent.NOBUTTON) {
			resetPopup();
		}
		super.mouseExited(event);
	}
	
	/**
	 * Resets / hides Popup window.
	 */
	public void resetPopup() {
		if (popup != null) {
			popup.hide();
		}

		this.popup = null;
	}

	/**
	 * Upon state change, pop-up a tiny window with low-high.
	 *
	 * @param e ChangeEvent Object.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		NumberRangeModel model = (NumberRangeModel) getModel();
		Number low = (Number) model.getLowValue();
		Number high = (Number) model.getHighValue();
		Number min = (Number) model.getMinValue();
		Number max = (Number) model.getMaxValue();

		DecimalFormat format;

        String lowStr = null;
        String highStr = null;
        if (min instanceof Integer || min instanceof Long) {
            lowStr = low.toString();
            highStr = high.toString();
        } else {
            if ((max.doubleValue() - min.doubleValue()) < .001) {
                format = new DecimalFormat("0.###E0");
            } else if ((max.doubleValue() - min.doubleValue()) > 100000) {
                format = new DecimalFormat("0.###E0");
            } else {
                format = new DecimalFormat("###,###.000");
            }
            lowStr = format.format(low);
            highStr = format.format(high);
        }
 
		if (isDisplayable()) {
			if (popup == null) {
				PopupFactory popupFactory = PopupFactory.getSharedInstance();
				JPanel panel = new JPanel();
				panel.setBorder(new LineBorder(UIManager.getColor("Separator.foreground"), 1));
				panel.setPreferredSize(getSize());
				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				popupLow = new JLabel(lowStr);
				popupLow.setBorder(new EmptyBorder(6, 2, 6, 2));
				popupHigh = new JLabel(highStr);
				popupHigh.setBorder(new EmptyBorder(6, 2, 6, 2));
				panel.add(popupLow);
				panel.add(Box.createHorizontalGlue());
				panel.add(popupHigh);
				popup = popupFactory.getPopup(this, panel, getLocationOnScreen().x,
				                              getLocationOnScreen().y + getPreferredSize().height
				                              + 2);
				popupDaemon = new PopupDaemon(this, 2000);
				
				popup.show();
				popupDaemon.restart();
			} else {
				popupLow.setText(lowStr);
				popupHigh.setText(highStr);
				popupDaemon.restart();
			}
		}
	}
}


/**
 * Daemon Thread to automatically hide Pop-up Window after xxx milliseconds.
 *
 * @author Ethan Cerami
 */
class PopupDaemon implements ActionListener {
	private Timer timer;
	private JRangeSliderExtended slider;

	/**
	 * Constructor.
	 *
	 * @param slider JRangeSliderExtended Object.
	 * @param delay  Delay until pop-up window is hidden.
	 */
	public PopupDaemon(JRangeSliderExtended slider, int delay) {
		timer = new Timer(delay, this);
		timer.setRepeats(false);
		this.slider = slider;
	}

	/**
	 * Restart timer.
	 */
	public void restart() {
		timer.restart();
	}

	/**
	 * Timer Event:  Hide popup now.
	 *
	 * @param e ActionEvent Object.
	 */
	public void actionPerformed(ActionEvent e) {
		slider.resetPopup();
	}
}
