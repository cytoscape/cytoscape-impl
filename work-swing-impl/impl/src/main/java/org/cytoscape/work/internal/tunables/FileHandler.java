package org.cytoscape.work.internal.tunables;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

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
	private MouseClick mouseClick;
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
	 * @param tunable the tunable associated to <code>field</code>
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
		filters = fileTypesManager.getSupportedFileTypes(DataCategory.valueOf(fileCategory.toUpperCase()), input);
		defaultString = "Please select a " + fileCategory.toLowerCase() + " file...";

		setGui();
		setLayout();
		panel.setLayout(layout);
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
				setValue(new File(fileTextField.getText()));
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
		titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		image = new ImageIcon(getClass().getResource("/images/ximian/stock_open.png"));
		fileTextField = new JTextField();
		fileTextField.setName("fileTextField");
		fileTextField.setEditable(true);
		fileTextField.setFont(new Font(null, Font.ITALIC,12));
		mouseClick = new MouseClick(fileTextField);
		fileTextField.addMouseListener(mouseClick);
		chooseButton = new JButton(input ? "Open a File..." : "Save a File...", image);
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
		return true;
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
				fileTextField.removeMouseListener(mouseClick);
			}
		}
	}

	//click on the field : removes its initial text
	private class MouseClick extends MouseAdapter implements MouseListener{
		JComponent component;

		public MouseClick(JComponent component) {
			this.component = component;
		}

		public void mouseClicked(MouseEvent e) {
			((JTextField)component).setText("");
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
