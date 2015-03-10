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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.mpg.mpi_inf.bioinf.netanalyzer.OpenBrowser;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Utility class containing helping methods for laying out and getting data from UI controls.
 * 
 * @author Yassen Assenov
 */
public abstract class Utils {

	/**
	 * Default border size (padding) of dialog windows.
	 */
	public static final int BORDER_SIZE = 6;

	/**
	 * Unique string used as separator for attribute names.
	 */
	public static final String SEPARATOR = "SEPARATOR";

	/**
	 * Adjust the size of a component such that it has the specified preferred width.
	 * 
	 * @param aComponent
	 *            Component to be adjusted.
	 * @param aWidth
	 *            Value for the preferred width of the component.
	 */
	public static void adjustWidth(JComponent aComponent, int aWidth) {
		Dimension preferedSize = aComponent.getPreferredSize();
		preferedSize.width = aWidth;
		aComponent.setPreferredSize(preferedSize);
	}

	/**
	 * Verifies the given file does not exist and can therefore be created.
	 * <p>
	 * If the file exists, it will be overwritten. In such a case a confirmation dialog is displayed to the
	 * user.
	 * </p>
	 * 
	 * @param aFile
	 *            File to be checked.
	 * @param aParent
	 *            Parent component of the confirmation dialog. This parameter is used only if a confirmation
	 *            dialog is displayed.
	 * @return <code>true</code> if the specified file does not exist, or if it exists and the user has
	 *         confirmed it can be overwritten; <code>false</code> otherwise.
	 */
	public static boolean canSave(File aFile, Component aParent) {
		if (aFile.exists()) {
			return JOptionPane.showConfirmDialog(aParent, Messages.SM_FILEEXISTS, Messages.DT_FILEEXISTS,
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		}
		return true;
	}

	/**
	 * Creates a new button with manageable width.
	 * 
	 * @param aText
	 *            Text of the button.
	 * @param aTooltip
	 *            Tooltip text for the button. Set this to <code>null</code> if no tooltip is to be displayed.
	 * @param aListener
	 *            Button click's listener.
	 * @return Newly created instance of <code>JButton</code>.
	 */
	public static JButton createButton(String aText, String aTooltip, ActionListener aListener) {
		JButton button = new JButton(aText);
		button.setToolTipText(aTooltip);
		button.addActionListener(aListener);
		return button;
	}
	
	public static JButton createButton(Action action, String aTooltip) {
		JButton button = new JButton(action);
		button.setToolTipText(aTooltip);
		return button;
	}

	/**
	 * Creates a new checkbox with manageable width.
	 * 
	 * @param aText
	 *            Text of the checkbox.
	 * @param aToolTip
	 *            Tooltip text for the checkbox. Set this to <code>null</code> if no tooltip is to be
	 *            displayed.
	 * @param aListener
	 *            Checkbox click's listener.
	 * @return Newly created instance of <code>JCheckBox</code>.
	 */
	public static JCheckBox createCheckBox(String aText, String aToolTip, ActionListener aListener) {
		JCheckBox button = new JCheckBox(aText);
		button.setToolTipText(aToolTip);
		button.addActionListener(aListener);
		button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getHeight()));
		return button;
	}

	/**
	 * Creates a new text label.
	 * 
	 * @param aText
	 *            Text of the label.
	 * @param aToolTip
	 *            Tooltip text of the label. Set this to <code>null</code> if no tooltip is to be displayed.
	 * @return Newly created instance of <code>JLabel</code>.
	 */
	public static JLabel createLabel(String aText, String aToolTip) {
		JLabel l = new JLabel(aText);
		l.setToolTipText(aToolTip);
		return l;
	}

	/**
	 * Creates a <code>String</code> representation of a double based on the given maximum length and
	 * precision.
	 * 
	 * @param aValue
	 *            Value to find the <code>String</code> representation of.
	 * @param aMaxLength
	 *            Maximum length, in number of characters, of the output string. Note that the length of the
	 *            returned <code>String</code> may be greater than the number specified by this parameter.
	 * @param aMaxPrecision
	 *            Maximum precision to use for the value.
	 * @return <code>String</code> representation of <code>aValue</code>.
	 */
	public static String doubleToString(double aValue, int aMaxLength, int aMaxPrecision) {
		return doubleToString(new Double(aValue), aMaxLength, aMaxPrecision);
	}

	/**
	 * Creates a <code>String</code> representation of a double based on the given maximum length and
	 * precision.
	 * 
	 * @param aValue
	 *            Value to find the <code>String</code> representation of.
	 * @param aMaxLength
	 *            Maximum length, in number of characters, of the output string. Note that the length of the
	 *            returned <code>String</code> may be greater than the number specified by this parameter.
	 * @param aMaxPrecision
	 *            Maximum precision to use for the value.
	 * @return <code>String</code> representation of <code>aValue</code>.
	 */
	public static String doubleToString(Double aValue, int aMaxLength, int aMaxPrecision) {
		if (aValue.isNaN() || aValue.isInfinite()) {
			return Messages.DI_UNDEF;
		}
		String text = aValue.toString();
		int pointIndex = text.indexOf('.');
		if (pointIndex >= 0 && text.length() > aMaxLength) {
			if (pointIndex + 1 >= aMaxLength) {
				text = text.substring(0, pointIndex);
			} else {
				int dap = Math.min(aMaxPrecision, aMaxLength - 1 - pointIndex);
				double v = aValue.doubleValue();
				long rounded = Math.round(v * Math.pow(10, dap));
				text = String.valueOf(rounded);
				String sign = rounded < 0 ? "-" : "";
				text = text.substring(sign.length());
				while (text.length() <= aMaxPrecision) {
					text = "0" + text;
				}
				text = sign + text;
				if (text.length() < aMaxLength) {
					text = text.substring(0, pointIndex) + "." + text.substring(pointIndex);
				} else {
					text = text.substring(0, pointIndex + 1) + "."
							+ text.substring(pointIndex + 1, aMaxLength - 1);
				}
			}
		}
		return text;
	}

	/**
	 * Enlarges, if necessary, the given current size to cover the given other size.
	 * <p>
	 * If both the width and height of <code>aCurrentSize</code> are larger than the width and height of
	 * <code>aSize</code>, respectively, calling this method has no effect.
	 * </p>
	 * 
	 * @param aCurrentSize
	 *            Size to be enlarged if necessary.
	 * @param aSize
	 *            Minimal required size of <code>aCurrentSize</code>.
	 * 
	 * @throws NullPointerException
	 *             If any of the given parameters is <code>null</code>.
	 */
	public static void ensureSize(Dimension aCurrentSize, Dimension aSize) {
		if (aCurrentSize.height < aSize.height) {
			aCurrentSize.height = aSize.height;
		}
		if (aCurrentSize.width < aSize.width) {
			aCurrentSize.width = aSize.width;
		}
	}

	/**
	 * Resizes the given buttons making them equal in size.
	 * <p>
	 * The sizes of the buttons are equalized only by enlarging their widths and heights (when necessary).
	 * </p>
	 * 
	 * @param aButtons
	 *            Array of buttons to be made equal size.
	 * 
	 * @throws NullPointerException
	 *             If <code>aButtons</code> is <code>null</code>.
	 */
	public static void equalizeSize(JButton[] aButtons) {
		final Dimension preferredSize = aButtons[0].getPreferredSize();
		final Dimension maximumSize = aButtons[0].getMaximumSize();
		for (int i = 1; i < aButtons.length; ++i) {
			ensureSize(preferredSize, aButtons[i].getPreferredSize());
			ensureSize(maximumSize, aButtons[i].getMaximumSize());
		}
		setSizes(aButtons, preferredSize, maximumSize);
	}

	/**
	 * Resizes the given buttons making them equal in size.
	 * <p>
	 * This a convenience method only. Calling this method is equivalent to calling:<br/> <code>
	 * equalizeSize(new JButton[] { aButton1, aButton2 });
	 * </code>
	 * </p>
	 * 
	 * @param aButton1
	 *            First of the buttons to be made equal in size.
	 * @param aButton2
	 *            Second of the buttons to be made equal in size.
	 * @see #equalizeSize(JButton[])
	 */
	public static void equalizeSize(JButton aButton1, JButton aButton2) {
		equalizeSize(new JButton[] { aButton1, aButton2 });
	}

	/**
	 * Resizes the given buttons making them equal in size.
	 * <p>
	 * This a convenience method only. Calling this method is equivalent to calling:<br/> <code>
	 * equalizeSize(new JButton[] { aButton1, aButton2, aButton3 });
	 * </code>
	 * </p>
	 * 
	 * @param aButton1
	 *            First of the buttons to be made equal in size.
	 * @param aButton2
	 *            Second of the buttons to be made equal in size.
	 * @param aButton3
	 *            Third of the buttons to be made equal in size.
	 * @see #equalizeSize(JButton[])
	 */
	public static void equalizeSize(JButton aButton1, JButton aButton2, JButton aButton3) {
		equalizeSize(new JButton[] { aButton1, aButton2, aButton3 });
	}

	/**
	 * Gets the currently selected integer value in a spinner control.
	 * 
	 * @param aSpinner
	 *            Spinner, whose chosen value is to be extracted.
	 * @return The selected value of the spinner rounded to integer.
	 * @throws ClassCastException
	 *             If the spinner's model is not a {@link javax.swing.SpinnerNumberModel}.
	 */
	public static int getSpinnerInt(JSpinner aSpinner) {
		return ((SpinnerNumberModel) aSpinner.getModel()).getNumber().intValue();
	}

	/**
	 * Gets the invert of the given color.
	 * 
	 * @param aColor
	 *            Color to get the invert of.
	 * @return New <code>Color</code> instance which stores the invert of <code>aColor</code>.
	 */
	public static Color invertOf(Color aColor) {
		return new Color(255 - aColor.getRed(), 255 - aColor.getGreen(), 255 - aColor.getBlue());
	}

	/**
	 * Removes the selection from a file chooser in single selection mode.
	 * 
	 * @param aDialog
	 *            File selection dialog to be modified.
	 */
	public static void removeSelectedFile(JFileChooser aDialog) {
		File file = aDialog.getSelectedFile();
		if (file != null) {
			aDialog.setSelectedFile(null);
			try {
				((javax.swing.plaf.basic.BasicFileChooserUI) aDialog.getUI()).setFileName(null);
			} catch (Exception ex) {
				// Could not remove file name from the text field, ignore
			}
		}
	}

	/**
	 * Sets the preferred and maximum sizes of the given buttons.
	 * 
	 * @param aButtons
	 *            Buttons to be modified.
	 * @param aPreferred
	 *            Preferred size to be set to each of the buttons. If this is <code>null</code>, every
	 *            button's preferred size is set to its default value.
	 * @param aMax
	 *            Maximum size to be set to each of the buttons. If this is <code>null</code>, every button's
	 *            maximum size is set to its default value.
	 * 
	 * @throws NullPointerException
	 *             If <code>aButtons</code> is <code>null</code>.
	 */
	public static void setSizes(JButton[] aButtons, Dimension aPreferred, Dimension aMax) {
		for (final JButton button : aButtons) {
			button.setPreferredSize(aPreferred);
			button.setMaximumSize(aMax);
		}
	}

	/**
	 * Sets a standard border of the given component.
	 * <p>
	 * The standard border is an empty border of with {@link #BORDER_SIZE}.
	 * </p>
	 * 
	 * @param aComponent
	 *            Component to get a standard border. This component will lose its previous border, if any.
	 */
	public static void setStandardBorder(JComponent aComponent) {
		final int bs = BORDER_SIZE;
		aComponent.setBorder(BorderFactory.createEmptyBorder(bs, bs, bs, bs));
	}


	/**
	 * Displays an error message dialog.
	 * 
	 * @param aParent
	 *            Owner of the dialog. Please note that the displayed dialog is modal.
	 * @param aTitle
	 *            Title of the dialog.
	 * @param aMessage
	 *            Message to be displayed in the dialog.
	 */
	public static void showErrorBox(Component aParent, String aTitle, String aMessage) {
		JOptionPane.showMessageDialog(aParent, aMessage, aTitle, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays an information dialog.
	 * 
	 * @param aParent
	 *            Owner of the dialog. Please note that the displayed dialog is modal.
	 * @param aTitle
	 *            Title of the dialog.
	 * @param aMessage
	 *            Message to be displayed in the dialog.
	 */
	public static void showInfoBox(Component aParent, String aTitle, String aMessage) {
		JOptionPane.showMessageDialog(aParent, aMessage, aTitle, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Hyperlink listener for instances of <code>JEditorPane</code> with HTML contents.
	 * <p>
	 * Whenever a link is clicked, this listener opens the referenced URL with the default system's browser.
	 * </p>
	 * 
	 * @author Mario Albrecht
	 * @author Yassen Assenov
	 * @author Carola Huthmacher
	 */
	public static class MenuPaneHyperlinkListener implements HyperlinkListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
		 */
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				OpenBrowser.openURL(e.getURL().toString());
			}
		}
	}
}
