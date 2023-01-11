package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;
import org.cytoscape.cg.model.BitmapCustomGraphics;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.cytoscape.cg.util.ImageCustomGraphicsSelector;
import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

/**
 * Provides a way to create ImageAnnotations
 */
@SuppressWarnings("serial")
public class LoadImageDialog extends AbstractAnnotationDialog<ImageAnnotationImpl> {

	private static final String NAME = "Image";
	private static final String UNDO_LABEL = "Create Image Annotation";
	
	private JTabbedPane tabbedPane;
	private JFileChooser fileChooser;
	private URLImportPanel urlImportPanel;
	private ImageCustomGraphicsSelector imageSelector;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private static File lastDirectory;

	private static final Logger logger = LoggerFactory.getLogger(LoadImageDialog.class);

	public LoadImageDialog(
			DRenderingEngine re,
			Point2D start,
			Window owner,
			CyServiceRegistrar serviceRegistrar
	) {
		super(NAME, re, start, owner);
		
		this.serviceRegistrar = serviceRegistrar;
		
		setTitle("Select an Image");
		setResizable(true);
		
		getTabbedPane().addTab("From File", getFileChooser());
		getTabbedPane().addTab("From URL", getUrlImportPanel());
		getTabbedPane().addTab("From Image Browser", getImageSelector());
		
		pack();
	}
	
	@Override
	protected JTabbedPane createControlPanel() {
		return getTabbedPane();
	}

	@Override
	protected ImageAnnotationImpl getPreviewAnnotation() {
		return null;
	}
	
	@Override
	protected int getPreviewWidth() {
		return 0;
	}

	@Override
	protected int getPreviewHeight() {
		return 0;
	}
	
	@Override
	protected void apply() {
		var selectedComp = getTabbedPane().getSelectedComponent();
		
		try {
			if (selectedComp == getFileChooser()) {
				var file = fileChooser.getSelectedFile();
				annotation = createAnnotation(file);
				
				// Save current directory
				if (file.getParentFile().isDirectory())
					lastDirectory = file.getParentFile();
			} else if (selectedComp == getUrlImportPanel()) {
				var cg = getUrlImportPanel().getImage();
				annotation = createAnnotation(cg);
			} else {
				var cg = getImageSelector().getSelectedImage();
				
				if (cg != null)
					annotation = createAnnotation(cg);
			}
			
			if (annotation != null) {
				var nodePoint = re.getTransform().getNodeCoordinates(startingLocation);
				var w = annotation.getWidth();
				var h = annotation.getHeight();
				
				annotation.setLocation(nodePoint.getX() - w / 2.0, nodePoint.getY() - h / 2.0);
				annotation.update();
				
				cyAnnotator.clearSelectedAnnotations();
				ViewUtils.selectAnnotation(re, annotation);
			}
		} catch (Exception ex) {
			logger.warn("Unable to load the selected image", ex);
		}
	}

	@Override
	protected JButton getApplyButton() {
		if (applyButton == null) {
			applyButton = new JButton(new AbstractAction("Insert") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					apply();
					dispose();
				}
			});
		}
		
		return applyButton;
	}
	
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		}
		
		return tabbedPane;
	}
	
	public URLImportPanel getUrlImportPanel() {
		if (urlImportPanel == null) {
			urlImportPanel = new URLImportPanel();	
		}
		
		return urlImportPanel;
	}
	
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser(lastDirectory);
			fileChooser.setControlButtonsAreShown(false);
			fileChooser.setCurrentDirectory(null);
			fileChooser.setDialogTitle("");
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(new ImageFilter());
		}

		return fileChooser;
	}
	
	private ImageCustomGraphicsSelector getImageSelector() {
		if (imageSelector == null) {
			imageSelector = new ImageCustomGraphicsSelector(serviceRegistrar);
			imageSelector.addActionListener(evt -> getApplyButton().doClick());	
			
			if (isAquaLAF())
				imageSelector.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
		}
		
		return imageSelector;
	}
	
	private ImageAnnotationImpl createAnnotation(File file) throws IOException {
		final ImageAnnotationImpl annotation;
		
		var cgManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		// Read the selected Image, create an Image Annotation, repaint the
		// whole network and then dispose off this Frame
		var ext = FilenameUtils.getExtension(file.getName());
		var url = file.toURI().toURL();
		
		if (ext.equalsIgnoreCase("svg")) {
			// SVG...
			var sb = new StringBuilder();
			
			try (var in = new BufferedReader(new InputStreamReader(url.openStream()))) {
				String line = null;
				
				while ((line = in.readLine()) != null)
		            sb.append(line + "\n");
			}
			
			var svg = sb.toString();
			
			if (svg.isBlank())
				return null;
			
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					0d, // rotation
					url,
					svg,
					re.getZoom(),
					cgManager
			);
		} else {
			// Bitmap (PNG, JPG, etc.)...
			var image = ImageIO.read(file);
			
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					0d, // rotation
					url,
					image,
					re.getZoom(),
					cgManager
			);
		}
		
		return annotation;
	}
	
	private ImageAnnotationImpl createAnnotation(CyCustomGraphics<?> cg) {
		ImageAnnotationImpl annotation = null;
		
		var cgManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		if (cg instanceof SVGCustomGraphics) {
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(SVGCustomGraphics) cg,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					0d, // rotation
					re.getZoom(),
					cgManager
			);
		} else if (cg instanceof BitmapCustomGraphics) {
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(BitmapCustomGraphics) cg,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					0d, // rotation
					re.getZoom(),
					cgManager
			);
		}
		
		return annotation;
	}

	/**
	 * This class provides a FileFilter for the JFileChooser.
	 */
	private class ImageFilter extends FileFilter {

		/**
		 * Accept all directories and all gif, jpg, tiff, png and svg files.
		 */
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;

			var ext = FilenameUtils.getExtension(f.getName()).toLowerCase();
			
			if (!ext.isEmpty())
				return ext.equals("tiff") || ext.equals("tif") || ext.equals("jpeg") || ext.equals("jpg")
						|| ext.equals("png") || ext.equals("gif") || ext.equals("svg");

			return false;
		}

		@Override
		public String getDescription() {
			return "Just Images";
		}
	}
	
	private class URLImportPanel extends JPanel {
		
		private static final int PREVIEW_BORDER_WIDTH = 1;
		private static final int PREVIEW_PAD = 5;
		
		private JTextField urlTextField;
		private JLabel errorIconLabel;
		private JLabel errorLabel;
		private JLabel previewImgLabel;
		
		@SuppressWarnings("rawtypes")
		private CyCustomGraphics image;
		
		private DebounceTimer urlDebouncer = new DebounceTimer(500);
		private DebounceTimer resizeDebouncer = new DebounceTimer(250);
		
		private boolean adjusting;
		
		URLImportPanel() {
			if (LookAndFeelUtil.isAquaLAF())
				setOpaque(false);
			
			var urlLabel = new JLabel("Image URL:");
			var previewLabel = new JLabel("Preview:");
			
			var layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(LEADING, false)
									.addComponent(urlLabel)
							)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(LEADING, true)
									.addComponent(getUrlTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addGroup(layout.createSequentialGroup()
											.addComponent(getErrorIconLabel())
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(getErrorLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									)
							)
					)
					.addComponent(previewLabel)
					.addComponent(getPreviewImgLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(urlLabel)
							.addComponent(getUrlTextField())
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(getErrorIconLabel())
							.addComponent(getErrorLabel(), getErrorLabel().getPreferredSize().height, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(previewLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getPreviewImgLabel(), 320, 320, Short.MAX_VALUE)
			);
		}
		
		@SuppressWarnings("rawtypes")
		CyCustomGraphics getImage() {
			return image;
		}
		
		JTextField getUrlTextField() {
			if (urlTextField == null) {
				urlTextField = new JTextField();
				urlTextField.setToolTipText("The address of the image (JPEG, PNG, GIF, TIFF, SVG) on the Internet");
				urlTextField.getDocument().addDocumentListener(new DocumentListener() {
					@Override
					public void removeUpdate(DocumentEvent e) {
						maybeLoadImage();
					}
					@Override
					public void insertUpdate(DocumentEvent e) {
						maybeLoadImage();
					}
					@Override
					public void changedUpdate(DocumentEvent e) {
						// Ignore...
					}
					private void maybeLoadImage() {
						var text = urlTextField.getText();
						resetPreview(!text.isBlank());
						
						if (text.isBlank())
							updateErrorMessage(null, null);
						else
							urlDebouncer.debounce(() -> loadImage(text));
					}
				});
			}
			
			return urlTextField;
		}
		
		JLabel getErrorIconLabel() {
			if (errorIconLabel == null) {
				var iconFont = serviceRegistrar.getService(IconManager.class).getIconFont(14.0f);
				
				errorIconLabel = new JLabel(" ");
				errorIconLabel.setIcon(new TextIcon(IconManager.ICON_WARNING, iconFont, 14, 14));
				errorIconLabel.setForeground(LookAndFeelUtil.getErrorColor());
				errorIconLabel.setHorizontalTextPosition(SwingConstants.CENTER);
				errorIconLabel.setVisible(false);
				LookAndFeelUtil.makeSmall(errorIconLabel);
			}
			
			return errorIconLabel;
		}
		
		JLabel getErrorLabel() {
			if (errorLabel == null) {
				// Start with a space char so the label is actually rendered and reserves some room for the error msg,
				// avoiding shifting the components bellow it when an error text is set
				errorLabel = new JLabel(" ");
				errorLabel.setForeground(LookAndFeelUtil.getErrorColor());
				LookAndFeelUtil.makeSmall(errorLabel);
			}
			
			return errorLabel;
		}
		
		JLabel getPreviewImgLabel() {
			if (previewImgLabel == null) {
				previewImgLabel = new JLabel();
				previewImgLabel.setOpaque(true);
				previewImgLabel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"),
						PREVIEW_BORDER_WIDTH));
				previewImgLabel.setHorizontalAlignment(JLabel.CENTER);
				previewImgLabel.setHorizontalTextPosition(JLabel.CENTER);
				previewImgLabel.setForeground(UIManager.getColor("Separator.foreground"));
				previewImgLabel.setFont(previewImgLabel.getFont().deriveFont(Font.BOLD, 16.0f));
				previewImgLabel.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						if (image != null && !adjusting) {
							resetPreview(false); // Remove the previous icon first to reset the size of the preview component
							resizeDebouncer.debounce(() -> updatePreview());
						}
					}
				});
			}
			
			return previewImgLabel;
		}
		
		/**
		 * Load the image (wrapped as Custom Graphics) and update the preview.
		 */
		private void loadImage(String urlStr) {
			if (urlStr != null)
				urlStr = urlStr.trim();
			
			if (urlStr.startsWith("/"))
				urlStr = "file:" + urlStr; // Assume it's a local file
			else if (urlStr.startsWith("file://"))
				urlStr = urlStr.replaceFirst("//", "/"); // "file://" does NOT work, but "file:/" does
			else if (urlStr.startsWith("www."))
				urlStr = "https://" + urlStr; // Lets be nice and and the httpS protocol
			
			image = null;
			
			String errorMsg = null;
			String errorDesc = null;
			
			if (!Strings.isBlank(urlStr)) {
				// Load the image as Custom Graphics
				URL url = null;
				
				try {
					url = new URL(urlStr);
				} catch (Exception e) {
					errorMsg = "Invalid URL";
					errorDesc = e.getMessage();
				}
				
				if (url != null) {
					var manager = serviceRegistrar.getService(CustomGraphicsManager.class);
					var id = manager.getNextAvailableID();
					var name = urlStr;
					
					try {
						image = isSVG(url)
								? new SVGCustomGraphics(id, name, url)
								: new BitmapCustomGraphics(id, name, url);
					} catch (Exception e) {
						errorMsg = "Invalid Image";
						errorDesc = e.getMessage();
					}
				}
			}
			
			// Update error and preview
			updatePreview();
			updateErrorMessage(errorMsg, errorDesc);
		}

		private void updateErrorMessage(String msg, String description) {
			getErrorLabel().setText(msg);
			getErrorLabel().setToolTipText(description);
			getErrorIconLabel().setVisible(msg != null);
			getErrorIconLabel().setToolTipText(description);
		}
		
		private void resetPreview(boolean loading) {
			getPreviewImgLabel().setIcon(null);
			getPreviewImgLabel().setText(loading ? "Loading..." : "");
		}

		private void updatePreview() {
			Icon icon = null;
			
			if (image != null) {
				// The icon must fit inside the labels dimension minus the border,
				// otherwise the resize event will cause an infinite loop
				var totalPad = (2 * PREVIEW_BORDER_WIDTH) + (2 * PREVIEW_PAD);
				var w = getPreviewImgLabel().getWidth() - totalPad;
				var h = getPreviewImgLabel().getHeight() - totalPad;
				
				if (w > 0 && h > 0)
					icon = VisualPropertyIconFactory.createIcon(image, w, h);
			}
			
			getPreviewImgLabel().setText("");
			getPreviewImgLabel().setIcon(icon);
		}
	}
	
	private boolean isSVG(URL url) throws IOException {
		var conn = url.openConnection();
		var type = conn.getHeaderField("Content-Type");
		
		if ("image/svg+xml".equalsIgnoreCase(type))
			return true;
		
		// If the Content-Type is null, check whether this is a local file
		// (it may happen that the OS does not return the file type, in which case we can still
		// try to check the file extension)
		if (type == null) {
			var protocol = url.getProtocol();
			
			if (protocol.equalsIgnoreCase("file"))
				return url.getFile().toLowerCase().endsWith(".svg");
		}
		
		return false;
	}
}
