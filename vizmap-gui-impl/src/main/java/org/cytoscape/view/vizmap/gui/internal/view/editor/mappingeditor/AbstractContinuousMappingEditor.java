package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

public abstract class AbstractContinuousMappingEditor<K extends Number, V> extends AbstractPropertyEditor implements
		ContinuousMappingEditor<K, V> {

	protected ContinuousMapping<K, V> mapping;
	protected ContinuousMappingEditorPanel<K, V> editorPanel;

	protected final EditorManager editorManager;
	protected final ServicesUtil servicesUtil;

	private final JLabel iconLabel;

	private boolean isEditorDialogActive;
	private JDialog currentDialog;

	public AbstractContinuousMappingEditor(final EditorManager editorManager, final ServicesUtil servicesUtil) {
		this.isEditorDialogActive = false;
		this.iconLabel = new JLabel();
		this.servicesUtil = servicesUtil;
		this.editorManager = editorManager;

		editor = new JPanel();
		((JPanel) editor).setLayout(new BorderLayout());
		((JPanel) editor).add(iconLabel, BorderLayout.CENTER);

		this.editor.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// Open only one editor at a time.
				if (isEditorDialogActive) {
					// Bring it to the front
					if (currentDialog != null)
						currentDialog.toFront();
					
					return;
				}

				final JDialog dialog = new JDialog(servicesUtil.get(CySwingApplication.class).getJFrame(),
						ModalityType.APPLICATION_MODAL);
				initComponents(dialog);

				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent evt) {
						final Dimension size = editor.getSize();
						drawIcon(size.width, size.height, false);
						isEditorDialogActive = false;
					}
				});

				dialog.setLocationRelativeTo(editor);
				dialog.setVisible(true);
				isEditorDialogActive = true;
				currentDialog = dialog;
			}

			private void initComponents(final JDialog dialog) {
				dialog.setTitle("Continuous Mapping Editor for " + mapping.getVisualProperty().getDisplayName());
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				
				dialog.getContentPane().add(editorPanel, BorderLayout.CENTER);

				LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), editorPanel.getOkButton().getAction(),
						editorPanel.getCancelButton().getAction());
				dialog.getRootPane().setDefaultButton(editorPanel.getOkButton());
				
				dialog.pack();
			}
		});
	}
	
	@Override
	public ImageIcon drawIcon(int width, int height, boolean detail) {
		if (editorPanel == null)
			return null;

		final ImageIcon newIcon = this.editorPanel.drawIcon(width, height, detail);
		iconLabel.setIcon(newIcon);

		return newIcon;
	}

	@Override
	public Object getValue() {
		return mapping;
	}
}
