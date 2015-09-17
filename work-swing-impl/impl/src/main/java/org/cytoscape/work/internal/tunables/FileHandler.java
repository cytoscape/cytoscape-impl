package org.cytoscape.work.internal.tunables;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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


import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.io.DataCategory;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;


/**
 * Handler for the type <i>File</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class FileHandler extends AbstractGUITunableHandler implements DirectlyPresentableTunableHandler{
	
	private final FileUtil fileUtil;

	private JPanel controlPanel;
	private JButton browseButton;
	private JTextField textField;
	private ImageIcon image;
	private JLabel label;
	private SupportedFileTypesManager fileTypesManager;
	private boolean input;
	private List<FileChooserFilter> filters;

	private Window possibleParent;

	private String defaultString;


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
	 * @param fileTypesManager
	 */
	public FileHandler(final Field field, final Object obj, final Tunable t,
			final SupportedFileTypesManager fileTypesManager, final FileUtil fileUtil) {
		super(field, obj, t);
		this.fileTypesManager = fileTypesManager;
		this.fileUtil = fileUtil;
		init();
	}

	public FileHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable,
			final SupportedFileTypesManager fileTypesManager, final FileUtil fileUtil) {
		super(getter, setter, instance, tunable);
		this.fileTypesManager = fileTypesManager;
		this.fileUtil = fileUtil;
		init();
	}

	private void init() {
		input = isInput();

		final String fileCategory = getFileCategory();
		final DataCategory dataCategory = DataCategory.valueOf(fileCategory.toUpperCase());
		filters = fileTypesManager.getSupportedFileTypes(dataCategory, input);
		String displayName = dataCategory.getDisplayName().toLowerCase();
		String a = isVowel( displayName.charAt(0) ) ? "an" : "a";
		defaultString = "Please select " + a + " " + displayName + " file...";

		setGui();
		
		updateFieldPanel(panel, label, controlPanel, horizontal);
		setTooltip(getTooltip(), textField, browseButton);
	}

	private boolean isVowel(char ch){
		ch=Character.toLowerCase(ch);
		return ch=='a' ||ch=='e' ||ch=='i' ||ch=='o' ||ch=='u';
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
			if (textField.getText().equals(defaultString) || textField.getText().isEmpty()) {
				setValue(null);
			} else {
				String path = textField.getText();
				File file = null;
				
				if (path.contains(System.getProperty("file.separator"))) {
					file = new File(path);
				} else {
					file = new File(System.getProperty("user.home"), path);
				}
				setValue(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(){
		final int load_or_save = input ? FileUtil.LOAD : FileUtil.SAVE;

		// Use the panel's parent if we have it, otherwise use the possible
		// parent specified in setFileTunableDirectly. 
		Component parentComponent = SwingUtilities.getWindowAncestor(panel);
		
		if (parentComponent == null)
			parentComponent = possibleParent;
		
		final File file = fileUtil.getFile(parentComponent, label.getText(), load_or_save, filters);
		
		if (file != null) {
			textField.setText(file.getAbsolutePath());
		} else {
			textField.setText(defaultString);
		}
	}
	
	/** Construction of the GUI depending on the file type expected */
	private void setGui() {
		label = new JLabel();
		image = new ImageIcon(getClass().getResource("/images/open-file-24.png"));
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setForeground(UIManager.getColor("Label.disabledForeground"));
		textField.setFont(textField.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		
		browseButton = new JButton( (input ? "Open File..." : "Browse..."), (input ? image : null) );
		browseButton.setActionCommand(input ? "open" : "save");
		browseButton.addActionListener(new MyFileActionListener());

		//set title and textfield text for the file type
		final String fileCategory = getFileCategory();
		textField.setText(defaultString);
		final String description = getDescription();
		
		if (description == null || description.isEmpty())
			label.setText((input ? "Load " : "Save ") + initialCaps(fileCategory) + " File");
		else
			label.setText(description);
		
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
	public boolean setTunableDirectly (Window possibleParent) {
		this.possibleParent = possibleParent;
		setGui();
		MyFileActionListener action = new MyFileActionListener();
		action.actionPerformed(null);
		handle();
		
		return !textField.getText().equals(defaultString);
	}

	// Click on the "open" or "save" button action listener
	private final class MyFileActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent ae) {
			final int load_or_save = input ? FileUtil.LOAD : FileUtil.SAVE;
				
			if (load_or_save == FileUtil.SAVE){
				//In case of export, we can not detect the filter current used, so we we use filter "All image files" or 
				//"All network files" when export image or network
				FileChooserFilter filter = null;
				
				for (int i=0; i<filters.size(); i++){
					filter = filters.get(i);
					
					if (filter.getDescription().trim().equalsIgnoreCase("All image files") ||
					    filter.getDescription().trim().equalsIgnoreCase("All network files")){
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
	
			final File file = fileUtil.getFile(parentComponent, label.getText(), load_or_save, filters);
			
			if (file != null)
				textField.setText(file.getAbsolutePath());
		}
	}

	@Override
	public String getState(){
		try{
			return textField.getText();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
}
