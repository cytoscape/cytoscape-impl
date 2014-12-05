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


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.cytoscape.io.DataCategory;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handler for the type <i>File</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class FileHandler extends AbstractGUITunableHandler  implements DirectlyPresentableTunableHandler{
	
	private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);
	
	private static final Font FILE_NAME_FONT = new Font("SansSerif", Font.PLAIN, 10);
	private static final Dimension PANEL_SIZE_DIMENSION = new Dimension(500, 80);
	
	private final FileUtil fileUtil;

	private JButton chooseButton;
	private JTextField fileTextField;
	private ImageIcon image;
	private JLabel titleLabel;
	private GroupLayout layout;
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
		init(fileTypesManager);
	}


	public FileHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable,
			final SupportedFileTypesManager fileTypesManager, final FileUtil fileUtil) {
		super(getter, setter, instance, tunable);
		this.fileTypesManager = fileTypesManager;
		this.fileUtil = fileUtil;
		init(fileTypesManager);
	}

	private void init(final SupportedFileTypesManager fileTypesManager) {
		input = isInput();

		final String fileCategory = getFileCategory();
		final DataCategory dataCategory = DataCategory.valueOf(fileCategory.toUpperCase());
		filters = fileTypesManager.getSupportedFileTypes(dataCategory, input);
		String displayName = dataCategory.getDisplayName().toLowerCase();
		String a = isVowel( displayName.charAt(0) ) ? "an" : "a";
		defaultString = "Please select " + a + " " + displayName + " file...";

		setGui();
		setLayout();
		panel.setLayout(layout);
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
	public void handle() {
		try {
			if (fileTextField.getText().equals(defaultString) || fileTextField.getText().isEmpty() )
				setValue(null);
			else
			{
				String path = fileTextField.getText();
				File file = null;
				if( path.contains(System.getProperty("file.separator")) )
				{
					file = new File(path);
				}
				else
				{
					file = new File(System.getProperty("user.home"), path);
				}
				setValue(file);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void update(){
		
		final int load_or_save = input ? FileUtil.LOAD : FileUtil.SAVE;

		// Use the panel's parent if we have it, otherwise use the possible
		// parent specified in setFileTunableDirectly. 
		Component parentComponent = SwingUtilities.getWindowAncestor(panel);
		if ( parentComponent == null )
			parentComponent = possibleParent;
		final File file = fileUtil.getFile(parentComponent, titleLabel.getText(), load_or_save, filters);
		if (file != null) {
			fileTextField.setText(file.getAbsolutePath());
		}else{
			fileTextField.setText(defaultString);
		}
	}
	
	//construction of the GUI depending on the file type expected:
	//	-field to display the file's path
	//	-button to open the FileCHooser
	//add listener to the field and button
	private void setGui() {
		//titleSeparator = new JSeparator();
		titleLabel = new JLabel();
		titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 1, 5));
		image = new ImageIcon(getClass().getResource("/images/ximian/stock_open.png"));
		fileTextField = new JTextField();
		fileTextField.setEditable(false);
		fileTextField.setBackground(Color.LIGHT_GRAY);
		fileTextField.setFont(new Font(null, Font.ITALIC,12));
		chooseButton = new JButton(input ? "Open a File..." : "Browse...", image);
		chooseButton.setActionCommand(input ? "open" : "save");
		chooseButton.addActionListener(new myFileActionListener());

		//set title and textfield text for the file type
		final String fileCategory = getFileCategory();
		fileTextField.setText(defaultString);
		String description = this.getDescription();
		if(description == null || description.isEmpty())
			titleLabel.setText((input ? "Load " : "Save ") + initialCaps(fileCategory) + " File");
		else
			titleLabel.setText(description);
		
		panel.setPreferredSize(PANEL_SIZE_DIMENSION);

		// Set the tooltip.  Note that at this point, we're setting
		// the tooltip on the entire panel.  This may or may not be
		// the right thing to do.
		if (getTooltip() != null && getTooltip().length() > 0) {
			final ToolTipManager tipManager = ToolTipManager.sharedInstance();
			tipManager.setInitialDelay(1);
			tipManager.setDismissDelay(7500);
			panel.setToolTipText(getTooltip());
		}
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

	// displays the panel's component in a good view
	private void setLayout() {
		layout = new GroupLayout(panel);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					  .addGroup(layout.createSequentialGroup()
						    .addContainerGap()
						    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							      .addComponent(titleLabel,GroupLayout.PREFERRED_SIZE,350,GroupLayout.PREFERRED_SIZE)
//							      .addComponent(titleSeparator,GroupLayout.DEFAULT_SIZE,350,Short.MAX_VALUE)
							      .addGroup(layout.createSequentialGroup()
									.addComponent(fileTextField,GroupLayout.DEFAULT_SIZE,350,Short.MAX_VALUE)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(chooseButton))
							      )
						    .addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						  .addContainerGap()
						  .addComponent(titleLabel)
//						  .addGap(8, 8, 8)
//						  .addComponent(titleSeparator,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
						  .addGap(7, 7, 7)
						  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							    .addComponent(chooseButton)
							    .addComponent(fileTextField))
						  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,3, Short.MAX_VALUE)
						  .addContainerGap()));
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
		myFileActionListener action = new myFileActionListener();
		action.actionPerformed(null);
		handle();
		return !fileTextField.getText().equals(defaultString);
	}

	// Click on the "open" or "save" button action listener
	private final class myFileActionListener implements ActionListener{
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
			if ( parentComponent == null )
				parentComponent = possibleParent;
	
			final File file = fileUtil.getFile(parentComponent, titleLabel.getText(), load_or_save, filters);
			if (file != null) {
				fileTextField.setFont(FILE_NAME_FONT);
				fileTextField.setText(file.getAbsolutePath());
			}
		}
	}

	public String getState(){
		try{
			return fileTextField.getText();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
}
