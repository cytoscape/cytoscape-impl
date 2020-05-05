package org.cytoscape.ding.customgraphicsmgr.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.Taggable;
import org.cytoscape.ding.customgraphics.bitmap.URLBitmapCustomGraphics;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.jdesktop.swingx.JXImageView;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class CustomGraphicsDetailPanel extends JPanel implements ListSelectionListener {

	private static final String TAG_DELIMITER = ",";
	
	private JLabel heightLabel;
	private JTextField heightTextField;
	private JXImageView imageViewPanel;
	private JCheckBox lockCheckBox;
	private JLabel nameLabel;
	private JTextField nameTextField;
	private JButton resetButton;
	private JLabel tagLabel;
	private JTextField tagTextField;
	private JLabel widthLabel;
	private JTextField widthTextField;
    private JButton searchButton;
	
	private CyCustomGraphics<?> cg;
	
	private final CyApplicationManager appManager;

	public CustomGraphicsDetailPanel(CyApplicationManager appManager) {
		this.appManager = appManager;
		initComponents();
	}

	private void initComponents() {
        nameLabel = new JLabel();
        tagLabel = new JLabel();
        nameTextField = new JTextField();
        tagTextField = new JTextField();
        imageViewPanel = new JXImageView();
        widthLabel = new JLabel();
        widthTextField = new JTextField();
        heightLabel = new JLabel();
        lockCheckBox = new JCheckBox();
        heightTextField = new JTextField();
        resetButton = new JButton();
        searchButton = new JButton();

        setBorder(LookAndFeelUtil.createTitledBorder("Image"));
        imageViewPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
        
        nameLabel.setText("Name:");
        tagLabel.setText("Tags:");

        nameTextField.addActionListener(evt -> nameTextFieldActionPerformed(evt));
        tagTextField.addActionListener(evt -> tagsTextFieldActionPerformed(evt));

        // Just to make the border visible
        var imageViewPanelLayout = new GroupLayout(imageViewPanel);
        imageViewPanel.setLayout(imageViewPanelLayout);
        imageViewPanelLayout.setHorizontalGroup(imageViewPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );
        imageViewPanelLayout.setVerticalGroup(imageViewPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );

        widthLabel.setText("Width:");

        widthTextField.addActionListener(evt -> widthTextFieldActionPerformed(evt));

        heightLabel.setText("Height:");

        lockCheckBox.setSelected(true);
        lockCheckBox.setText("Aspect Ratio");
        //lockCheckBox.addActionListener(evt -> lockCheckBoxActionPerformed(evt));

        heightTextField.addActionListener(evt -> heightTextFieldActionPerformed(evt));

        resetButton.setText("Original");
        resetButton.addActionListener(evt -> resetButtonActionPerformed(evt));

        searchButton.setText("Search");
        searchButton.setToolTipText("This function is not implemented yet.");
        searchButton.setEnabled(false);
        searchButton.addActionListener(evt -> optionButtonActionPerformed(evt));

        var imgViewLbl = new JLabel("Actual Size View:");
        
        var layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
        		.addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(tagLabel)
                            .addComponent(nameLabel)
                        )
                        .addGroup(layout.createParallelGroup(Alignment.LEADING, true)
                            .addComponent(nameTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tagTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                        )
                )
                .addComponent(imgViewLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        		.addComponent(imageViewPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        		.addGroup(layout.createSequentialGroup()
        				.addComponent(widthLabel)
                        .addComponent(widthTextField, PREFERRED_SIZE, 60, PREFERRED_SIZE)
                        .addComponent(heightLabel)
                        .addComponent(heightTextField, PREFERRED_SIZE, 60, PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lockCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(resetButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(searchButton)
        		)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.CENTER, false)
                        .addComponent(nameLabel)
                        .addComponent(nameTextField)
                )
                .addGroup(layout.createParallelGroup(Alignment.CENTER, false)
                        .addComponent(tagLabel)
                        .addComponent(tagTextField)
                )
                .addComponent(imgViewLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addComponent(imageViewPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.CENTER, false)
                		.addComponent(widthLabel)
                        .addComponent(widthTextField)
                        .addComponent(heightLabel)
                        .addComponent(heightTextField)
                        .addComponent(lockCheckBox)
                        .addComponent(resetButton)
                        .addComponent(searchButton)
                )
        );
	}

	private void nameTextFieldActionPerformed(ActionEvent evt) {
		var newName = nameTextField.getText();
		
		if (newName != null && newName.trim().length() != 0 && cg != null)
			cg.setDisplayName(this.nameTextField.getText());
	}
	

	private void resetButtonActionPerformed(ActionEvent evt) {
		if (cg == null || cg.getRenderedImage() == null)
			return;

		if (cg instanceof URLBitmapCustomGraphics) {
			var image = ((URLBitmapCustomGraphics) cg).resetImage();
			imageViewPanel.setImage(image);
			int w = image.getWidth(null);
			int h = image.getHeight(null);
			widthTextField.setText(Integer.toString(w));
			heightTextField.setText(Integer.toString(h));
			cg.setWidth(w);
			cg.setHeight(h);
			
			var netView = appManager.getCurrentNetworkView();
			
			if (netView != null)
				netView.updateView();
		}
	}

	private void widthTextFieldActionPerformed(ActionEvent evt) {
		resizeImage(true);
	}

	private void heightTextFieldActionPerformed(ActionEvent evt) {
		resizeImage(false);
	}

	private void resizeImage(boolean isWidth) {
		var width = widthTextField.getText();
		var height = heightTextField.getText();
		var currentImage = cg.getRenderedImage();
		
		if (currentImage == null)
			return;

		boolean lock = lockCheckBox.isSelected();

		int currentW = currentImage.getWidth(null);
		int currentH = currentImage.getHeight(null);

		Integer w = null;
		Integer h = null;
		
		try {
			w = Integer.parseInt(width);
			h = Integer.parseInt(height);
		} catch (NumberFormatException e) {
			// back to current size
			widthTextField.setText(Integer.toString(currentW));
			heightTextField.setText(Integer.toString(currentH));
			
			return;
		}

		float ratio;
		int converted;
		
		if (!lock) {
			cg.setWidth(w);
			cg.setHeight(h);
			imageViewPanel.setImage(cg.getRenderedImage());
		} else if (isWidth) {
			ratio = ((float) currentH) / ((float) currentW);
			converted = (int) (w * ratio);
			cg.setWidth(w);
			cg.setHeight(converted);
			imageViewPanel.setImage(cg.getRenderedImage());
			heightTextField.setText(Integer.toString(converted));
		} else {
			ratio = ((float) currentW) / ((float) currentH);
			converted = (int) (h * ratio);
			cg.setWidth(converted);
			cg.setHeight(h);
			imageViewPanel.setImage(cg.getRenderedImage());
			widthTextField.setText(Integer.toString(converted));
		}
		
		var netView = appManager.getCurrentNetworkView();
		
		if (netView != null)
			netView.updateView();
	}

	private void tagsTextFieldActionPerformed(ActionEvent evt) {
		var tagStr = this.tagTextField.getText();
		
		if (tagStr != null && !tagStr.isBlank()) {
			if (cg instanceof Taggable) {
				var tags = tagStr.split(TAG_DELIMITER);
				
				for (var t : tags)
					((Taggable) cg).getTags().add(t.trim());
			}
		}
	}
	
	private void optionButtonActionPerformed(ActionEvent evt) {
    }
	
    @Override
	public void valueChanged(ListSelectionEvent e) {
		if (!(e.getSource() instanceof CustomGraphicsBrowser)
				|| e.getValueIsAdjusting())
			return;

		var browser = (CustomGraphicsBrowser) e.getSource();
		cg = (CyCustomGraphics<?>) browser.getSelectedValue();
		
		if (cg == null) {
			imageViewPanel.setImage((Image) null);
			heightTextField.setText(null);
			widthTextField.setText(null);
			nameTextField.setText(null);
			nameTextField.setToolTipText(null);
			tagTextField.setText(null);
			return;
		}

		var img = cg.getRenderedImage();

		// Set up detail panel
		imageViewPanel.setImage(img);
		heightTextField.setText(Integer.toString(img.getHeight(null)));
		widthTextField.setText(Integer.toString(img.getWidth(null)));
		nameTextField.setText(cg.getDisplayName());
		nameTextField.setToolTipText(cg.getDisplayName());
		
		if (cg instanceof Taggable) {
			var tags = ((Taggable) cg).getTags();
			int tagCount = tags.size();
			int counter = 0;
			var tagBuilder = new StringBuilder();
			
			for (var t : tags) {
				tagBuilder.append(t);
				counter++;
				
				if (tagCount != counter)
					tagBuilder.append(", ");
			}
			
			tagTextField.setText(tagBuilder.toString());
			tagTextField.setToolTipText(tagBuilder.toString());
		}
	}
}
