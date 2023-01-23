package org.cytoscape.cg.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.logging.log4j.util.Strings;
import org.cytoscape.cg.internal.util.VisualPropertyIconFactory;
import org.cytoscape.cg.model.AbstractURLImageCustomGraphics;
import org.cytoscape.cg.model.BitmapCustomGraphics;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
class URLImportPanel extends JPanel {

	private static final int PREVIEW_BORDER_WIDTH = 1;
	private static final int PREVIEW_PAD = 5;
	
	private JTextField urlTextField;
	private JLabel msgIconLabel;
	private JLabel msgLabel;
	private JLabel previewImgLabel;
	
	@SuppressWarnings("rawtypes")
	private AbstractURLImageCustomGraphics image;
	private boolean duplicateImage;
	
	private DebounceTimer urlDebouncer = new DebounceTimer(500);
	private DebounceTimer resizeDebouncer = new DebounceTimer(250);
	
	private boolean adjusting;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	URLImportPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
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
										.addComponent(getMsgIconLabel())
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(getMsgLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
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
						.addComponent(getMsgIconLabel())
						.addComponent(getMsgLabel(), getMsgLabel().getPreferredSize().height, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(previewLabel)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getPreviewImgLabel(), 320, 320, Short.MAX_VALUE)
		);
	}
	
	@SuppressWarnings("rawtypes")
	AbstractURLImageCustomGraphics getImage() {
		return duplicateImage ? null : image;
	}
	
	@SuppressWarnings("rawtypes")
	private void setImage(AbstractURLImageCustomGraphics image) {
		if (!Objects.equal(this.image, image)) {
			var oldValue = this.image;
			this.image = image;
			firePropertyChange("image", oldValue, image);
		}
	}
	
	boolean isDuplicateImage() {
		return duplicateImage;
	}
	
	private void setDuplicateImage(boolean duplicateImage) {
		if (this.duplicateImage != duplicateImage) {
			this.duplicateImage = duplicateImage;
			firePropertyChange("duplicateImage", !duplicateImage, duplicateImage);
		}
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
						updateErrorMessage(JOptionPane.PLAIN_MESSAGE, null, null);
					else
						urlDebouncer.debounce(() -> loadImage(text));
				}
			});
		}
		
		return urlTextField;
	}
	
	JLabel getMsgIconLabel() {
		if (msgIconLabel == null) {
			msgIconLabel = new JLabel(" ");
			msgIconLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			msgIconLabel.setVisible(false);
			LookAndFeelUtil.makeSmall(msgIconLabel);
		}
		
		return msgIconLabel;
	}
	
	JLabel getMsgLabel() {
		if (msgLabel == null) {
			// Start with a space char so the label is actually rendered and reserves some room for the error msg,
			// avoiding shifting the components bellow it when an error text is set
			msgLabel = new JLabel(" ");
			LookAndFeelUtil.makeSmall(msgLabel);
		}
		
		return msgLabel;
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
	@SuppressWarnings("rawtypes")
	private void loadImage(String urlStr) {
		urlStr = normalizeURL(urlStr);
		setImage(null);
		
		int msgType = JOptionPane.PLAIN_MESSAGE;
		String msg = null;
		String msgDesc = null;
		
		if (!Strings.isBlank(urlStr)) {
			// Load the image as Custom Graphics
			URL url = null;
			
			try {
				url = new URL(urlStr);
			} catch (Exception e) {
				msgType = JOptionPane.ERROR_MESSAGE;
				msg = "Invalid URL";
				msgDesc = e.getMessage();
			}
			
			if (url != null) {
				// Only load the image if the manager doesn't have any image with the same URL yet 
				var manager = serviceRegistrar.getService(CustomGraphicsManager.class);
				var cg = manager.getCustomGraphicsBySourceURL(url);
				
				if (cg == null) {
					setDuplicateImage(false);
					var id = manager.getNextAvailableID();
					
					try {
						setImage(
								CustomGraphicsUtil.isSVG(url)
								? new SVGCustomGraphics(id, url)
								: new BitmapCustomGraphics(id, url)
						);
					} catch (Exception e) {
						msgType = JOptionPane.ERROR_MESSAGE;
						msg = "Invalid Image";
						msgDesc = e.getMessage();
					}
				} else {
					setDuplicateImage(true);
					
					var img = cg instanceof AbstractURLImageCustomGraphics ? (AbstractURLImageCustomGraphics) cg : null;
					setImage(img);
					
					msgType = JOptionPane.INFORMATION_MESSAGE;
					msg = "Duplicate Image";
					msgDesc = "This image has been imported before";
				}
			}
		}
		
		// Update error and preview
		updatePreview();
		updateErrorMessage(msgType, msg, msgDesc);
	}

	private String normalizeURL(String urlStr) {
		urlStr = urlStr.trim();
		
		if (urlStr.startsWith("/"))
			urlStr = "file:" + urlStr; // Assume it's a local file
		else if (urlStr.startsWith("file://"))
			urlStr = urlStr.replaceFirst("//", "/"); // "file://" does NOT work, but "file:/" does
		else if (urlStr.startsWith("www."))
			urlStr = "https://" + urlStr; // Lets be nice and and the httpS protocol
		
		return urlStr;
	}

	private void updateErrorMessage(int msgType, String msg, String description) {
		var fg = LookAndFeelUtil.getInfoColor();
		Icon icon = null;
		
		if (msg != null) {
			var iconTxt = IconManager.ICON_INFO_CIRCLE;
			
			if (msgType == JOptionPane.WARNING_MESSAGE) {
				iconTxt = IconManager.ICON_WARNING;
				fg = LookAndFeelUtil.getWarnColor();
			} else if (msgType == JOptionPane.ERROR_MESSAGE) {
				iconTxt = IconManager.ICON_WARNING;
				fg = LookAndFeelUtil.getErrorColor();
			}
			
			var iconFont = serviceRegistrar.getService(IconManager.class).getIconFont(14.0f);
			icon = new TextIcon(iconTxt, iconFont, 14, 14);
		}
		
		getMsgIconLabel().setForeground(fg);
		getMsgIconLabel().setIcon(icon);
		
		getMsgLabel().setForeground(fg);
		getMsgLabel().setText(msg);
		getMsgLabel().setToolTipText(description);
		getMsgIconLabel().setVisible(msg != null);
		getMsgIconLabel().setToolTipText(description);
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
