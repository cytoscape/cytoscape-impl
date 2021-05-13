package org.cytoscape.work.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.work.internal.tunables.utils.ViewUtil.invokeOnEDT;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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

@SuppressWarnings("serial")
public class TaskDialog extends JDialog {

	public static final String CANCEL_EVENT = "task-cancel-event";
	public static final String CLOSE_EVENT = "task-close-event";

	private static final int PREF_LABEL_WIDTH = 500;

	private static JLabel newLabelWithFont(final int style, final float sizeFactor) {
		Font font = UIManager.getFont("Label.font");
		font = font.deriveFont(font.getSize() * sizeFactor).deriveFont(style);
		final JLabel label = new JLabel();
		label.setFont(font);

		return label;
	}

	volatile boolean errorOccurred = false;

	private final JLabel titleLabel;
	private final JLabel subtitleLabel;
	private final RoundedProgressBar progressBar;
	private final JLabel msgIconLabel;
	private final JTextArea msgArea;
	private final JLabel cancelLabel;
	private final JButton cancelButton;
	private final JButton closeButton;

	public TaskDialog(final Window parent, final CyServiceRegistrar serviceRegistrar) {
		super(parent, "Cytoscape Task", DEFAULT_MODALITY_TYPE);

		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);

		titleLabel = newLabelWithFont(Font.PLAIN, 1.5f);
		subtitleLabel = newLabelWithFont(Font.PLAIN, 1.0f);
		subtitleLabel.setVisible(false);

		progressBar = new RoundedProgressBar();
		progressBar.setIndeterminate();

		msgIconLabel = new JLabel();
		msgIconLabel.setFont(iconManager.getIconFont(16.0f));

		msgArea = new JTextArea();
		msgArea.setEditable(false);
		msgArea.setHighlighter(null); // disables text selection
		msgArea.setBorder(null);
		msgArea.setLineWrap(true);
		msgArea.setWrapStyleWord(true);
		msgArea.setOpaque(false);
		
		if (LookAndFeelUtil.isNimbusLAF())
			msgArea.setBackground(new Color(0, 0, 0, 0)); // Workaround for Nimbus LAF!
		
		cancelLabel = newLabelWithFont(Font.ITALIC, 1.0f);
		cancelLabel.setText("Cancelling...");
		cancelLabel.setVisible(false);
		
		cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cancelButton.isVisible() && cancelButton.isEnabled())
					cancel();
			}
		});

		closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (closeButton.isVisible() && closeButton.isEnabled())
					close();
			}
		});
		closeButton.setVisible(false);

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(cancelButton, closeButton, cancelLabel);

		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap(20, 20)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addComponent(titleLabel, DEFAULT_SIZE, PREF_LABEL_WIDTH, Short.MAX_VALUE)
						.addComponent(subtitleLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(progressBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(msgIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(msgArea, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
						.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addContainerGap(20, 20)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(titleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(subtitleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(progressBar)
				.addGap(15)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addComponent(msgIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(msgArea, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addContainerGap()
		);

		getContentPane().add(contents);

		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), closeButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(closeButton);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (errorOccurred)
					close();
				else
					cancel();
			}
		});
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setModal(true);
		setResizable(false);
		
		pack();
		setLocationRelativeTo(parent);
	}

	public void setTaskTitle(final String taskTitle) {
		invokeOnEDT(() -> {
			final String currentTitle = titleLabel.getText();

			if (currentTitle == null || currentTitle.length() == 0) {
				titleLabel.setText(taskTitle);
				setTitle("Cytoscape: " + taskTitle);
			} else {
				subtitleLabel.setVisible(true);
				subtitleLabel.setText(taskTitle);
			}

			updateLabels();
			pack();
		});
	}

	public void setPercentCompleted(final float percent) {
		invokeOnEDT(() -> {
			if (percent < 0.0f)
				progressBar.setIndeterminate();
			else
				progressBar.setProgress(percent);
		});
	}

	public void setException(final Throwable t) {
		t.printStackTrace();
		errorOccurred = true;

		invokeOnEDT(() -> {
			setStatus(GUIDefaults.ICON_ERROR, LookAndFeelUtil.getErrorColor(), t.getMessage());
			progressBar.setVisible(false);
			closeButton.setVisible(true);
			cancelButton.setVisible(false);
			cancelLabel.setVisible(false);
			updateLabels();
			pack();
		});
	}

	public void setStatus(final String iconText, final Color iconForeground, final String message) {
		invokeOnEDT(() -> {
			msgIconLabel.setText(iconText);
			msgIconLabel.setForeground(iconForeground != null ? iconForeground : msgArea.getForeground());
			msgArea.setText(message);
			updateLabels();
			pack();
		});
	}

	public boolean errorOccurred() {
		return errorOccurred;
	}

	private void cancel() {
		cancelLabel.setVisible(true);
		cancelButton.setEnabled(false);
		progressBar.setIndeterminate();
		firePropertyChange(CANCEL_EVENT, null, null);
	}

	private void close() {
		firePropertyChange(CLOSE_EVENT, null, null);
	}
	
	private void updateLabels() {
		invokeOnEDT(() -> {
			titleLabel.setVisible(titleLabel.getText() != null && !titleLabel.getText().trim().isEmpty());
			subtitleLabel.setVisible(subtitleLabel.getText() != null && !subtitleLabel.getText().trim().isEmpty());
		});
	}
}
