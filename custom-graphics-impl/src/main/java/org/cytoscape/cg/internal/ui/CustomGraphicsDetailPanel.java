package org.cytoscape.cg.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.cg.internal.image.BitmapCustomGraphics;
import org.cytoscape.cg.internal.image.SVGCustomGraphics;
import org.cytoscape.cg.internal.util.VisualPropertyIconFactory;
import org.cytoscape.cg.util.CustomGraphicsBrowser;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.jdesktop.swingx.JXImageView;

@SuppressWarnings("serial")
public class CustomGraphicsDetailPanel extends JPanel implements ListSelectionListener {

	private JTextField heightTextField;
	private CGImageView imageViewPanel;
	private JCheckBox lockCheckBox;
	private JTextField nameTextField;
	private JButton resetButton;
	private JTextField widthTextField;
	
	private CyCustomGraphics<?> cg;
	
	private final CyApplicationManager appManager;

	public CustomGraphicsDetailPanel(CyApplicationManager appManager) {
		this.appManager = appManager;
		initComponents();
	}

	private void initComponents() {
		setBorder(LookAndFeelUtil.createTitledBorder("Image"));
		
        var nameLabel = new JLabel("Name:");
        var imgViewLabel = new JLabel("Actual Size View:");
        var widthLabel = new JLabel("Width:");
        var heightLabel = new JLabel("Height:");
        
        var layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
        		.addGroup(layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addComponent(getNameTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                )
                .addComponent(imgViewLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        		.addComponent(getImageViewPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        		.addGroup(layout.createSequentialGroup()
        				.addComponent(widthLabel)
                        .addComponent(getWidthTextField(), PREFERRED_SIZE, 60, PREFERRED_SIZE)
                        .addComponent(heightLabel)
                        .addComponent(getHeightTextField(), PREFERRED_SIZE, 60, PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(getLockCheckBox())
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(getResetButton())
        		)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.CENTER, false)
                        .addComponent(nameLabel)
                        .addComponent(getNameTextField())
                )
                .addComponent(imgViewLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addComponent(getImageViewPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.CENTER, false)
                		.addComponent(widthLabel)
                        .addComponent(getWidthTextField())
                        .addComponent(heightLabel)
                        .addComponent(getHeightTextField())
                        .addComponent(getLockCheckBox())
                        .addComponent(getResetButton())
                )
        );
	}

	JTextField getNameTextField() {
		if (nameTextField == null) {
			nameTextField = new JTextField();
			nameTextField.addActionListener(evt -> {
				var newName = nameTextField.getText();

				if (newName != null && newName.trim().length() != 0 && cg != null)
					cg.setDisplayName(nameTextField.getText());
			});
		}

		return nameTextField;
	}

	JTextField getWidthTextField() {
		if (widthTextField == null) {
			widthTextField = new JTextField();
	        widthTextField.addActionListener(evt -> resizeImage(widthTextField));
	        widthTextField.addFocusListener(new FocusAdapter() {
	        	@Override
	        	public void focusLost(FocusEvent evt) {
	        		resizeImage(widthTextField);
	        	}
			});
		}
		
		return widthTextField;
	}
	
	JTextField getHeightTextField() {
		if (heightTextField == null) {
			heightTextField = new JTextField();
			heightTextField.addActionListener(evt -> resizeImage(heightTextField));
			heightTextField.addFocusListener(new FocusAdapter() {
	        	@Override
	        	public void focusLost(FocusEvent evt) {
	        		resizeImage(heightTextField);
	        	}
			});
		}

		return heightTextField;
	}
	
	JCheckBox getLockCheckBox() {
		if (lockCheckBox == null) {
			lockCheckBox = new JCheckBox("Aspect Ratio");
			lockCheckBox.setSelected(true);
			lockCheckBox.addActionListener(evt -> resizeImage(null));
		}

		return lockCheckBox;
	}
	
	CGImageView getImageViewPanel() {
		if (imageViewPanel == null) {
			imageViewPanel = new CGImageView();
			imageViewPanel.setLayout(new BorderLayout());
	        imageViewPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		}
		
		return imageViewPanel;
	}
	
	JButton getResetButton() {
		if (resetButton == null) {
			resetButton = new JButton("Original");
	        resetButton.addActionListener(evt -> {
	        	if (cg == null || cg.getRenderedImage() == null)
	    			return;

	    		if (cg instanceof BitmapCustomGraphics) {
	    			var image = ((BitmapCustomGraphics) cg).resetImage();
	    			getImageViewPanel().setImage(image);
	    			int w = image.getWidth(null);
	    			int h = image.getHeight(null);
	    			getWidthTextField().setText(Integer.toString(w));
	    			getHeightTextField().setText(Integer.toString(h));
	    			cg.setWidth(w);
	    			cg.setHeight(h);
	    			
	    			var netView = appManager.getCurrentNetworkView();
	    			
	    			if (netView != null)
	    				netView.updateView();
	    		}
	        });
		}
		
		return resetButton;
	}
	
	private void resizeImage(JComponent source) {
		var width = getWidthTextField().getText();
		var height = getHeightTextField().getText();
		var currentImage = cg.getRenderedImage();
		
		if (currentImage == null)
			return;

		boolean lock = getLockCheckBox().isSelected();

		int currentW = currentImage.getWidth(null);
		int currentH = currentImage.getHeight(null);

		Integer w = null;
		Integer h = null;
		
		try {
			w = Integer.parseInt(width);
			h = Integer.parseInt(height);
		} catch (NumberFormatException e) {
			// back to current size
			getWidthTextField().setText(Integer.toString(currentW));
			getHeightTextField().setText(Integer.toString(currentH));
			
			return;
		}

		boolean isBitmap = cg instanceof SVGCustomGraphics == false;
		
		if (isBitmap) {
			float ratio;
			int converted;
			
			if (!lock) {
				cg.setWidth(w);
				cg.setHeight(h);
				getImageViewPanel().setImage(cg.getRenderedImage());
			} else if (getWidthTextField().equals(source)) {
				ratio = ((float) currentH) / ((float) currentW);
				converted = (int) (w * ratio);
				cg.setWidth(w);
				cg.setHeight(converted);
				getImageViewPanel().setImage(cg.getRenderedImage());
				getHeightTextField().setText(Integer.toString(converted));
			} else if (getHeightTextField().equals(source)) {
				ratio = ((float) currentW) / ((float) currentH);
				converted = (int) (h * ratio);
				cg.setWidth(converted);
				cg.setHeight(h);
				getImageViewPanel().setImage(cg.getRenderedImage());
				getWidthTextField().setText(Integer.toString(converted));
			}
		}
		
		var netView = appManager.getCurrentNetworkView();
		
		if (netView != null)
			netView.updateView();
	}

    @Override
	public void valueChanged(ListSelectionEvent e) {
		if (!(e.getSource() instanceof CustomGraphicsBrowser) || e.getValueIsAdjusting())
			return;

		var browser = (CustomGraphicsBrowser) e.getSource();
		cg = (CyCustomGraphics<?>) browser.getSelectedValue();
		
		getNameTextField().setText(null);
		getNameTextField().setToolTipText(null);
		getHeightTextField().setText(null);
		getWidthTextField().setText(null);
		
		if (cg == null) {
			getImageViewPanel().setImage((Image) null);
			getImageViewPanel().setIcon(null);
			getImageViewPanel().setEditable(false);
			
			return;
		}
		
		// Update name
		getNameTextField().setText(cg.getDisplayName());
		getNameTextField().setToolTipText(cg.getDisplayName());
		
		// Disable resize components if it's a vector image
		boolean isBitmap = cg instanceof BitmapCustomGraphics;
		
		getWidthTextField().setEnabled(isBitmap);
		getHeightTextField().setEnabled(isBitmap);
		getLockCheckBox().setEnabled(isBitmap);
		getResetButton().setEnabled(isBitmap);
		
		getImageViewPanel().setEditable(isBitmap);
		
		// Set up detail panel
		if (cg instanceof SVGCustomGraphics) {
			int w = getImageViewPanel().getWidth();
			int h = getImageViewPanel().getHeight();
			var icon = VisualPropertyIconFactory.createIcon(cg, w, h);
			getImageViewPanel().setIcon(icon);
			getImageViewPanel().setImage((Image) null);
		} else {
			var img = cg.getRenderedImage();
			getImageViewPanel().setImage(img);
			getImageViewPanel().setIcon(null);
			
			getHeightTextField().setText(Integer.toString(img.getHeight(null)));
			getWidthTextField().setText(Integer.toString(img.getWidth(null)));
		}
	}
    
    private class CGImageView extends JXImageView {
    	
    	private Icon icon;
    	
    	public void setIcon(Icon icon) {
    		this.icon = icon;
    		repaint();
    	}
    	
    	@Override
    	public void paint(Graphics g) {
    		super.paint(g);
    		
    		if (icon != null) {
    			Point2D center = new Point2D.Double(getWidth() / 2.0, getHeight() / 2.0);
				
    			if (getImageLocation() != null)
					center = getImageLocation();
    			
				var loc = new Point2D.Double();
				var w = icon.getIconWidth();
				var h = icon.getIconHeight();
				loc.setLocation(center.getX() - w / 2.0, center.getY() - h / 2.0);
				
				icon.paintIcon(this, g, (int) loc.getX(), (int) loc.getY());
			}
    	}
    }
}
