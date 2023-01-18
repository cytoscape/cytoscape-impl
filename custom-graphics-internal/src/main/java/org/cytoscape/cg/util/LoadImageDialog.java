package org.cytoscape.cg.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.cytoscape.cg.internal.util.ImageUtil;
import org.cytoscape.cg.model.AbstractURLImageCustomGraphics;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
class LoadImageDialog extends JDialog {
	
	private static final String IMG_FILES_DESCRIPTION = "Image file (PNG, GIF, JPEG or SVG)";
	private static final String[] IMG_EXTENSIONS = { "jpg", "jpeg", "png", "gif", "svg" };
	
	private JTabbedPane tabbedPane;
	private JFileChooser fileChooser;
	private URLImportPanel urlImportPanel;
	private JButton okButton;
	private JButton cancelButton;

	@SuppressWarnings("rawtypes")
	private final List<AbstractURLImageCustomGraphics> images = new ArrayList<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	LoadImageDialog(Window owner, CyServiceRegistrar serviceRegistrar) {
		super(owner, "Add Images", ModalityType.APPLICATION_MODAL);
		
		this.serviceRegistrar = serviceRegistrar;
		
		init();
		pack();
	}
	
	@SuppressWarnings("rawtypes")
	List<AbstractURLImageCustomGraphics> getImages() {
		return images;
	}
	
	void init() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(true);
		
		var buttonPanel = LookAndFeelUtil.createOkCancelPanel(getOkButton(), getCancelButton());

		var contents = new JPanel();
		var layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		var hGroup = layout.createParallelGroup(LEADING, true);
		var vGroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		hGroup.addComponent(getTabbedPane());
		vGroup.addComponent(getTabbedPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);

		hGroup.addComponent(buttonPanel);
		vGroup.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);

		makeSmall(getOkButton(), getCancelButton());
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkButton().getAction(), getCancelButton().getAction());
		getRootPane().setDefaultButton(getOkButton());
		
		getContentPane().add(contents);
	}
	
	JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.addTab("From File", getFileChooser());
			tabbedPane.addTab("From URL", getUrlImportPanel());
		}
		
		return tabbedPane;
	}
	
	URLImportPanel getUrlImportPanel() {
		if (urlImportPanel == null) {
			urlImportPanel = new URLImportPanel(serviceRegistrar);	
		}
		
		return urlImportPanel;
	}
	
	JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setControlButtonsAreShown(false);
			fileChooser.setCurrentDirectory(null);
			fileChooser.setDialogTitle("");
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setMultiSelectionEnabled(true);
			
			var filter = new FileNameExtensionFilter(IMG_FILES_DESCRIPTION, IMG_EXTENSIONS);
			fileChooser.setFileFilter(filter);
			fileChooser.addChoosableFileFilter(filter);
		}

		return fileChooser;
	}
	
	JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(new AbstractAction("OK") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					loadImages();
					dispose();
				}
			});
		}
		
		return okButton;
	}
	
	JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					images.clear();
					dispose();
				}
			});
		}
		
		return cancelButton;
	}
	
	private void loadImages() {
		var selectedComp = getTabbedPane().getSelectedComponent();
		
		if (selectedComp == getFileChooser()) {
			var files = fileChooser.getSelectedFiles();
			
			if (files != null && files.length > 0) {
				var newImages = ImageUtil.loadImageCustomGraphics(files, serviceRegistrar.getService(CustomGraphicsManager.class));
			
				if (!newImages.isEmpty())
					images.addAll(newImages);
			}
		} else if (selectedComp == getUrlImportPanel()) {
			var cg = getUrlImportPanel().getImage();
			
			if (cg != null)
				images.add(cg);
		}
	}
	
//	/**
//	 * This class provides a FileFilter for the JFileChooser.
//	 */
//	private class ImageFilter extends FileFilter {
//
//		/**
//		 * Accept all directories and all gif, jpg, tiff, png and svg files.
//		 */
//		@Override
//		public boolean accept(File f) {
//			if (f.isDirectory())
//				return true;
//
//			var ext = FilenameUtils.getExtension(f.getName()).toLowerCase();
//			
//			if (!ext.isEmpty())
//				return ext.equals("tiff") || ext.equals("tif") || ext.equals("jpeg") || ext.equals("jpg")
//						|| ext.equals("png") || ext.equals("gif") || ext.equals("svg");
//
//			return false;
//		}
//
//		@Override
//		public String getDescription() {
//			return "Just Images";
//		}
//	}
}
