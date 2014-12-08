package org.cytoscape.ding.customgraphicsmgr.internal.ui;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
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
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.jdesktop.swingx.JXImageView;

/**
 * 
 */
public class CustomGraphicsDetailPanel extends JPanel implements ListSelectionListener {

	private static final long serialVersionUID = -412539582192509545L;

	private static final String TAG_DELIMITER = ",";
	
	private JLabel heightLabel;
	private JTextField heightTextField;
	private JXImageView imageViewPanel;
	private JCheckBox lockCheckBox;
	private JPanel modifyPanel;
	private JLabel nameLabel;
	private JTextField nameTextField;
	private JButton resetButton;
	private JLabel tagLabel;
	private JTextField tagTextField;
	private JLabel widthLabel;
	private JTextField widthTextField;
    private JButton searchButton;
	
	private CyCustomGraphics cg;
	
	private final CyApplicationManager appManager;

	/** Creates new form CustomGraphicsDetailPanel */
	public CustomGraphicsDetailPanel(final CyApplicationManager appManager) {
		this.appManager = appManager;
		initComponents();
	}

	private void initComponents() {
        nameLabel = new JLabel();
        tagLabel = new JLabel();
        nameTextField = new JTextField();
        tagTextField = new JTextField();
        imageViewPanel = new JXImageView();
        modifyPanel = new JPanel();
        widthLabel = new JLabel();
        widthTextField = new JTextField();
        heightLabel = new JLabel();
        lockCheckBox = new JCheckBox();
        heightTextField = new JTextField();
        resetButton = new JButton();
        searchButton = new JButton();

        imageViewPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")));
        
        nameLabel.setText("Name:");
        tagLabel.setText("Tags:");

        nameTextField.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });

        tagTextField.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                tagsTextFieldActionPerformed(evt);
            }
        });

        GroupLayout imageViewPanelLayout = new GroupLayout(imageViewPanel);
        imageViewPanel.setLayout(imageViewPanelLayout);
        imageViewPanelLayout.setHorizontalGroup(
            imageViewPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 573, Short.MAX_VALUE)
        );
        imageViewPanelLayout.setVerticalGroup(
            imageViewPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 228, Short.MAX_VALUE)
        );

        widthLabel.setText("Width:");

        widthTextField.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                widthTextFieldActionPerformed(evt);
            }
        });

        heightLabel.setText("Height:");

        lockCheckBox.setSelected(true);
        lockCheckBox.setText("Aspect Ratio");
        lockCheckBox.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                //lockCheckBoxActionPerformed(evt);
            }
        });

        heightTextField.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                heightTextFieldActionPerformed(evt);
            }
        });

        resetButton.setText("Original");
        resetButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        searchButton.setText("Search");
        searchButton.setToolTipText("This function is not implemented yet.");
        searchButton.setEnabled(false);
        searchButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
                optionButtonActionPerformed(evt);
            }
        });

        GroupLayout modifyPanelLayout = new GroupLayout(modifyPanel);
        modifyPanel.setLayout(modifyPanelLayout);
        modifyPanelLayout.setHorizontalGroup(
            modifyPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(modifyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(widthLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(widthTextField, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(heightLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(heightTextField, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lockCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(searchButton)
                .addGap(8, 8, 8))
        );
        modifyPanelLayout.setVerticalGroup(
            modifyPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(modifyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modifyPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(widthLabel)
                    .addComponent(widthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightLabel)
                    .addComponent(heightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lockCheckBox)
                    .addComponent(resetButton)
                    .addComponent(searchButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        final JLabel imgViewLbl = new JLabel("Actual Size View:");
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(tagLabel)
                            .addComponent(nameLabel)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(nameTextField, GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                            .addComponent(tagTextField, GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                        )
                )
                .addComponent(imgViewLbl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        		.addComponent(imageViewPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        		.addComponent(modifyPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(nameLabel)
                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(tagLabel)
                        .addComponent(tagTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                )
                .addComponent(imgViewLbl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(imageViewPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(modifyPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
	}

	private void nameTextFieldActionPerformed(ActionEvent evt) {
		final String newName = this.nameTextField.getText();
		if (newName != null && newName.trim().length() != 0 && cg != null)
			cg.setDisplayName(this.nameTextField.getText());
	}
	

	private void resetButtonActionPerformed(ActionEvent evt) {
		if (cg == null || cg.getRenderedImage() == null)
			return;

		if (cg instanceof URLImageCustomGraphics) {
			final Image image = ((URLImageCustomGraphics) cg).resetImage();
			imageViewPanel.setImage(image);
			final int w = image.getWidth(null);
			final int h = image.getHeight(null);
			widthTextField.setText(Integer.toString(w));
			heightTextField.setText(Integer.toString(h));
			cg.setWidth(w);
			cg.setHeight(h);
			
			appManager.getCurrentNetworkView().updateView();
		}
	}

	private void widthTextFieldActionPerformed(ActionEvent evt) {
		resizeImage(true);
	}

	private void heightTextFieldActionPerformed(ActionEvent evt) {
		resizeImage(false);
	}

	private void resizeImage(boolean isWidth) {
		final String width = widthTextField.getText();
		final String height = heightTextField.getText();


		final Image currentImage = cg.getRenderedImage();
		if (currentImage == null)
			return;

		final boolean lock = this.lockCheckBox.isSelected();

		final int currentW = currentImage.getWidth(null);
		final int currentH = currentImage.getHeight(null);

		Integer w;
		Integer h;
		try {
			w = Integer.parseInt(width);
			h = Integer.parseInt(height);
		} catch (NumberFormatException e) {
			// back to current size
			this.widthTextField.setText(Integer.toString(currentW));
			this.heightTextField.setText(Integer.toString(currentH));
			return;
		}

		float ratio;
		int converted;
		if (lock == false) {
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
		appManager.getCurrentNetworkView().updateView();
	}

	private void tagsTextFieldActionPerformed(ActionEvent evt) {
		final String tagStr = this.tagTextField.getText();
		if(tagStr != null && tagStr.trim().length() != 0) {
			if(cg instanceof Taggable) {
				final String[] tags = tagStr.split(TAG_DELIMITER);
				for(String tag:tags)
					((Taggable) cg).getTags().add(tag.trim());
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

		final CustomGraphicsBrowser browser = (CustomGraphicsBrowser) e
				.getSource();

		cg = (CyCustomGraphics) browser.getSelectedValue();
		if(cg == null) {
			imageViewPanel.setImage((Image)null);
			heightTextField.setText(null);
			widthTextField.setText(null);
			nameTextField.setText(null);
			nameTextField.setToolTipText(null);
			tagTextField.setText(null);
			return;
		}
			
		final Image img = cg.getRenderedImage();

		// Set up detail panel
		imageViewPanel.setImage(img);
		heightTextField.setText(Integer.toString(img.getHeight(null)));
		widthTextField.setText(Integer.toString(img.getWidth(null)));
		nameTextField.setText(cg.getDisplayName());
		nameTextField.setToolTipText(cg.getDisplayName());
		if(cg instanceof Taggable) {
			final Collection<String> tags = ((Taggable) cg).getTags();
			final int tagCount = tags.size();
			int counter = 0;
			final StringBuilder tagBuilder = new StringBuilder();
			for(String tag: tags) {
				tagBuilder.append(tag);
				counter++;
				if(tagCount != counter)
					tagBuilder.append(", ");
			}
			tagTextField.setText(tagBuilder.toString());
			tagTextField.setToolTipText(tagBuilder.toString());
		}
	}
}
