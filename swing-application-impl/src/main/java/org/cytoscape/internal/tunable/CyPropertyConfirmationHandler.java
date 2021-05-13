package org.cytoscape.internal.tunable;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class CyPropertyConfirmationHandler extends AbstractGUITunableHandler
		implements DirectlyPresentableTunableHandler {

	public CyPropertyConfirmationHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
	}

	public CyPropertyConfirmationHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	@Override
	public void handle() {
		// ???
	}
	
	@Override
	public boolean setTunableDirectly(Window possibleParent) {
		CyPropertyConfirmation value = getCyPropertyConfirmation();
		
		if (value == null || value.getPropertyValue() == null)
			return true;
		
		ConfirmationDialog dialog = new ConfirmationDialog(possibleParent, value);
		dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dialog.pack();
		dialog.setLocationRelativeTo(possibleParent);
		dialog.setVisible(true);

		if (dialog.getHideStatus())
			value.setPropertyValue("false");
		
		return value.isConfirmed();
	}

	@Override
	public boolean isForcedToSetDirectly() {
		return true;
	}
	
	private CyPropertyConfirmation getCyPropertyConfirmation() {
		try {
			return (CyPropertyConfirmation) getValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("serial")
	private class ConfirmationDialog extends JDialog {
		
		private JPanel msgPanel;
		private JCheckBox checkBox;
		private JButton okButton;
		private JButton cancelButton;
		private final JLabel iconLabel = new JLabel();
		private final JLabel msgLabel = new JLabel();
		
		private CyPropertyConfirmation value;
		private final CyServiceRegistrar serviceRegistrar;
		
		ConfirmationDialog(Window owner, CyPropertyConfirmation value) {
			super(owner);
			this.value = value;
			this.serviceRegistrar = value.getServiceRegistrar();

			initComponents();
		}

		public boolean getHideStatus() {
			return getCheckBox().isSelected();
		}

		private void initComponents() {
			setTitle(getParams().getProperty("ForceSetTitle", ""));
			setResizable(false);
			setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
			
			iconLabel.setText(IconManager.ICON_QUESTION_CIRCLE);
			iconLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(48.0f));
			iconLabel.setForeground(LookAndFeelUtil.getInfoColor());
			
			String desc = getDescription();
			
			if (desc != null)
				desc = desc.replaceAll("\\$\\{" + value.getPropertyName() + "\\}", value.getPropertyValue());
			
			msgLabel.setText(desc);
			
			final JPanel bottomPanel = LookAndFeelUtil.createOkCancelPanel(getOkButton(), getCancelButton());

			final GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addComponent(getMsgPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(bottomPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getMsgPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(bottomPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkButton().getAction(),
					getCancelButton().getAction());
			getRootPane().setDefaultButton(getOkButton());
			getOkButton().requestFocusInWindow();
		}
		
		private JPanel getMsgPanel() {
			if (msgPanel == null) {
				msgPanel = new JPanel();
				
				
				
				final int hpad = 20;
				final int vpad = 40;
				
				final GroupLayout layout = new GroupLayout(msgPanel);
				msgPanel.setLayout(layout);
				layout.setAutoCreateContainerGaps(true);
				layout.setAutoCreateGaps(true);
				
				layout.setHorizontalGroup(layout.createSequentialGroup()
						.addGap(hpad)
						.addComponent(iconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(LEADING, true)
								.addComponent(msgLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(getCheckBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addGap(hpad,  hpad, Short.MAX_VALUE)
				);
				layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
						.addGap(vpad)
						.addComponent(iconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(msgLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(getCheckBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addGap(vpad)
				);
			}
			
			return msgPanel;
		}
		
		private JCheckBox getCheckBox() {
			if (checkBox == null) {
				String s = getParams().getProperty("doNotAskLabel", "Do not ask me again");
				checkBox = new JCheckBox(s);
				checkBox.setHorizontalAlignment(SwingConstants.LEFT);
			}
			
			return checkBox;
		}
		
		private JButton getCancelButton() {
			if (cancelButton == null) {
				String s = getParams().getProperty("cancelLabel", "Cancel");
				cancelButton = new JButton(new AbstractAction(s) {
					@Override
					public void actionPerformed(ActionEvent e) {
						value.setConfirmed(false);
						dispose();
					}
				});
			}
			
			return cancelButton;
		}
		
		private JButton getOkButton() {
			if (okButton == null) {
				String s = getParams().getProperty("okLabel", "OK");
				okButton = new JButton(new AbstractAction(s) {
					@Override
					public void actionPerformed(ActionEvent e) {
						value.setConfirmed(true);
						dispose();
					}
				});
			}
			
			return okButton;
		}
	}
}
