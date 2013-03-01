package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

public abstract class AbstractContinuousMappingEditor<K extends Number, V> extends AbstractPropertyEditor implements
		ContinuousMappingEditor<K, V> {

	private static final Dimension DEF_SIZE = new Dimension(550, 400);
	private static final Dimension MIN_SIZE = new Dimension(300, 350);

	protected ContinuousMapping<K, V> mapping;
	protected ContinuousMappingEditorPanel<K, V> editorPanel;

	protected final CyNetworkTableManager manager;
	protected final CyApplicationManager appManager;
	protected final EditorManager editorManager;

	protected final VisualMappingManager vmm;
	protected final VisualMappingFunctionFactory continuousMappingFactory;

	private final JLabel iconLabel;

	private boolean isEditorDialogActive;
	private JDialog currentDialog;

	public AbstractContinuousMappingEditor(final CyNetworkTableManager manager, final CyApplicationManager appManager,
			final EditorManager editorManager, final VisualMappingManager vmm, VisualMappingFunctionFactory continuousMappingFactory) {

		this.isEditorDialogActive = false;
		this.iconLabel = new JLabel();
		this.vmm = vmm;
		this.manager = manager;
		this.appManager = appManager;
		this.editorManager = editorManager;
		this.continuousMappingFactory = continuousMappingFactory;

		editor = new JPanel();
		((JPanel) editor).setLayout(new BorderLayout());
		((JPanel) editor).add(iconLabel, BorderLayout.CENTER);

		this.editor.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent ev) {

				// Open only one editor at a time.
				if (isEditorDialogActive) {
					// Bring it to the front
					if (currentDialog != null)
						currentDialog.toFront();
					return;
				}

				final JDialog editorDialog = new JDialog();
				initComponents(editorDialog);

				editorDialog.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent evt) {
						final Dimension size = editor.getSize();
						drawIcon(size.width, size.height, false);
						isEditorDialogActive = false;
					}
				});

				editorDialog.setTitle("Continuous Mapping Editor for " + mapping.getVisualProperty().getDisplayName());
				editorDialog.setLocationRelativeTo(editor);
				editorDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
				editorDialog.setVisible(true);
				isEditorDialogActive = true;
				currentDialog = editorDialog;
			}

			private void initComponents(final JDialog dialog) {

				dialog.setLayout(new BorderLayout());
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.getContentPane().add(editorPanel, BorderLayout.CENTER);

				dialog.setPreferredSize(DEF_SIZE);
				dialog.setMinimumSize(MIN_SIZE);

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
