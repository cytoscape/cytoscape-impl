package org.cytoscape.work.internal.tunables;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.DataCategory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

/**
 * Handler for the type <i>File</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class FileHandler extends AbstractGUITunableHandler implements DirectlyPresentableTunableHandler, FocusListener{
	
	private final CyServiceRegistrar serviceRegistrar;

	private JPanel controlPanel;
	private JButton browseButton;
	private JTextField textField;
	private JLabel label;
	private SupportedFileTypesManager fileTypesManager;
	private boolean input;
	private List<FileChooserFilter> filters;

	private Window possibleParent;


	/**
	 * Constructs the <code>GUIHandler</code> for the <code>File</code> type
	 *
	 * It creates the GUI which displays the path of the current file in a field, and provides
	 * access to a FileChooser with filtering parameters on
	 * <i>network</i>,<i>attributes</i>, or <i>session</i> (parameters are set in the
	 * <code>Tunable</code>'s annotations of the <code>File</code>)
	 *
	 * @param field the field that has been annotated
	 * @param obj object contained in <code>field</code>
	 * @param t the tunable associated to <code>field</code>
	 */
	public FileHandler(
			final Field field,
			final Object obj,
			final Tunable t,
			final SupportedFileTypesManager fileTypesManager,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(field, obj, t);
		this.fileTypesManager = fileTypesManager;
		this.serviceRegistrar = serviceRegistrar;
		init();
	}

	public FileHandler(
			final Method getter,
			final Method setter,
			final Object instance,
			final Tunable tunable,
			final SupportedFileTypesManager fileTypesManager,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(getter, setter, instance, tunable);
		this.fileTypesManager = fileTypesManager;
		this.serviceRegistrar = serviceRegistrar;
		init();
	}

	private void init() {
		input = isInput();

		final String fileCategory = getFileCategory();
		final DataCategory dataCategory = DataCategory.valueOf(fileCategory.toUpperCase());
		filters = fileTypesManager.getSupportedFileTypes(dataCategory, input);

		setGui();

		updateFieldPanel(panel, label, controlPanel, horizontal);
		setTooltip(getTooltip(), textField, browseButton);
	}

	/**
	 * To set a path to the object <code>File</code> <code>o</code>
	 *
	 * It creates a new <code>File</code> from the selected file in the FileChooser, or from the path to a file, entered by the user in the field
	 * The initial <code>File</code> object <code>o</code> is set with this new file
	 */
	@Override
	public void handle() {
		try {
			if (textField.getText().isEmpty()) {
				setValue(null);
			} else {
				String path = textField.getText();
				File file = null;

				if (path.contains(System.getProperty("file.separator"))) {
					file = new File(path);
				} else {
					CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
					file = new File(applicationManager.getCurrentDirectory(), path);
					textField.setText(file.getAbsolutePath());
				}

				setValue(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(){
		File file = null;
		
		try {
			file = (File) getValue();
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		textField.setText(file != null ? file.getAbsolutePath() : "");
	}
	
	/** Construction of the GUI depending on the file type expected */
	private void setGui() {
		label = new JLabel();
		
		textField = new JTextField();
		textField.addFocusListener(this);
		
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		final TextIcon inputIcon = new TextIcon(IconManager.ICON_FOLDER_OPEN, iconManager.getIconFont(16.0f), 16, 16);
		
		browseButton = new JButton((input ? "Open File..." : "Browse..."), (input ? inputIcon : null));
		browseButton.setActionCommand(input ? "open" : "save");
		browseButton.addActionListener(new MyFileActionListener());
		
		if (input) // To prevent the button from getting too tall because of the icon
			browseButton.setPreferredSize(
					new Dimension(browseButton.getPreferredSize().width, new JButton("X").getPreferredSize().height));

		//set title and textfield text for the file type
		final String fileCategory = getFileCategory();
		final String description = getDescription();
		
		if (description == null || description.isEmpty())
			label.setText((input ? "Load " : "Save ") + initialCaps(fileCategory) + " File");
		else
			label.setText(description);
		
		try {
			File file = (File) getValue();
			
			if (file != null)
				textField.setText(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		controlPanel = new JPanel();
		final GroupLayout layout = new GroupLayout(controlPanel);
		controlPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(textField, DEFAULT_SIZE, 400, Short.MAX_VALUE)
				.addComponent(browseButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
				.addComponent(textField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(browseButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		// Workaround to correctly v-align the label
		final int lh = label.getPreferredSize().height;
		final int ch = controlPanel.getPreferredSize().height;
		
		if (lh < ch)
			label.setBorder(BorderFactory.createEmptyBorder((ch-lh)/2, 0, (ch-lh)/2, 0));
	}

	private String getFileCategory() {
		return getParams().getProperty("fileCategory", "unspecified");
	}

	private boolean isInput() {
		return getParams().getProperty("input", "false").equalsIgnoreCase("true");
	}

	private String initialCaps(final String s) {
		if (s.isEmpty())
			return "";
		else
			return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
	}

	@Override
	public boolean isForcedToSetDirectly() {
		return getParams().getProperty("ForceSetDirectly", "true").equalsIgnoreCase("true");
	}
	
	/**
	 * This method allows us to bypass the normal tunable support when the only
	 * tunable in a Task is a File.  This allows us to pop up a file dialog
	 * without first presenting the tunable dialog.
	 */
	@Override
	public boolean setTunableDirectly(Window possibleParent) {
		this.possibleParent = possibleParent;
		setGui();
		MyFileActionListener action = new MyFileActionListener();
		action.actionPerformed(null);
		handle();

		return !textField.getText().equals("");
	}

	// Click on the "open" or "save" button action listener
	private final class MyFileActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ae) {
			final int loadOrSave = input ? FileUtil.LOAD : FileUtil.SAVE;
				
			if (loadOrSave == FileUtil.SAVE) {
				// In case of export, we can not detect the filter current used, so we we use filter "All image files" or 
				// "All network files" when export image or network
				FileChooserFilter filter = null;
				
				for (int i = 0; i < filters.size(); i++) {
					filter = filters.get(i);

					if (filter.getDescription().trim().equalsIgnoreCase("All image files")
							|| filter.getDescription().trim().equalsIgnoreCase("All network files")) {
						filters = new ArrayList<FileChooserFilter>();
						filters.add(filter);
						break;
					}
				}
			}

			// Use the panel's parent if we have it, otherwise use the possible
			// parent specified in setFileTunableDirectly. 
			Component parentComponent = SwingUtilities.getWindowAncestor(panel);
			
			if (parentComponent == null)
				parentComponent = possibleParent;
	
			final FileUtil fileUtil = serviceRegistrar.getService(FileUtil.class);
			final File file = fileUtil.getFile(parentComponent, label.getText(), loadOrSave, filters);
			
			if (file != null)
				textField.setText(file.getAbsolutePath());
			
			handle();
		}
	}

	@Override
	public String getState(){
		try{
			return textField.getText();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		handle();
	}
}
