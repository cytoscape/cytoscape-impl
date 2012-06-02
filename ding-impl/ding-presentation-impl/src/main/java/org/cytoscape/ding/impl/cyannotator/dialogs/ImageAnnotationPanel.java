package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.Component;

import org.cytoscape.ding.impl.cyannotator.api.ImageAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;

public class ImageAnnotationPanel extends javax.swing.JPanel {
	private int WIDTH = 500;
	private int HEIGHT = 200;
	private int TOP = 10;
	private int LEFT = 10;
	private int COLUMN1 = 175;
	private int COLUMN2 = 305;
	private int RIGHT = WIDTH-10;

	private javax.swing.JPanel jPanel1;
	private JSlider borderOValue;
	private JSlider opacityValue;
	private JSlider contrastValue;
	private JSlider brightnessValue;
	private JComboBox eThickness;
	private JCheckBox edgeColor;
	private JButton sECButton;

	private ImageAnnotation preview;
	private PreviewPanel previewPanel;
	
	private ImageAnnotation mAnnotation;

	public ImageAnnotationPanel(ImageAnnotation mAnnotation, PreviewPanel previewPanel, int width, int height) {
		this.mAnnotation=mAnnotation;
		this.previewPanel = previewPanel;
		this.preview=(ImageAnnotation)previewPanel.getPreviewAnnotation();
		this.WIDTH = width;
		this.HEIGHT = height;
		initComponents();
		setSize(width,height);
	}

	private void initComponents() {
		setMaximumSize(new java.awt.Dimension(WIDTH, HEIGHT));
		setMinimumSize(new java.awt.Dimension(WIDTH, HEIGHT));
		setLayout(null);
		setBorder(BorderFactory.createLoweredBevelBorder());

		// Upper left components
		//

		int y = TOP;
		// Border color
		{
			// Border color
			edgeColor = new javax.swing.JCheckBox();
			edgeColor.setText("Border Color");
			if (mAnnotation.getBorderColor() != null) edgeColor.setSelected(true);
			edgeColor.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
				    edgeColorActionPerformed(evt);
				}
			});		
			add(edgeColor);
			edgeColor.setBounds(LEFT, y, edgeColor.getPreferredSize().width, 20);
	
			sECButton = new javax.swing.JButton();
			sECButton.setText("Select Edge Color");
			if(edgeColor.isSelected())
				sECButton.setEnabled(true);
			else
				sECButton.setEnabled(false);
	
			sECButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
				    sECButtonActionPerformed(evt);
				}
			});	   
			add(sECButton);
			sECButton.setBounds(COLUMN1, y, sECButton.getPreferredSize().width, 20);
		}
	
		// Border opacity
		{
			y = y+25;
			JLabel borderOLabel = new JLabel("Border Opacity");
			borderOLabel.setBounds(LEFT, y, borderOLabel.getPreferredSize().width, 20);
			add(borderOLabel);
	
			borderOValue = new JSlider(0, 100);
			borderOValue.setMajorTickSpacing(100);
			borderOValue.setPaintTicks(true);
			borderOValue.setPaintLabels(true);
			borderOValue.setValue(100);
			borderOValue.setBounds(COLUMN1, y, RIGHT-COLUMN1, borderOValue.getPreferredSize().height);
			borderOValue.setEnabled(false);
			borderOValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					updateBorderOpacity(borderOValue.getValue());
				}
			});
			add(borderOValue);
		}

		// Border thickness
		{
			y = y+50;
			JLabel jLabel6 = new JLabel();
			jLabel6.setText("Border Thickness");
			jLabel6.setBounds(LEFT, y, jLabel6.getPreferredSize().width, 14);
			add(jLabel6);

			eThickness = new javax.swing.JComboBox();
			eThickness.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
			eThickness.setSelectedIndex(1);
			for(int i=0;i<eThickness.getModel().getSize();i++){
				if( ((int)mAnnotation.getBorderWidth())==Integer.parseInt((String)eThickness.getModel().getElementAt(i)) ){
					eThickness.setSelectedIndex(i);
				break;
				}
			}
			eThickness.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
				    eThicknessActionPerformed(evt);
				}
			});	 
			eThickness.setBounds(COLUMN1, y, 42, 20);
			add(eThickness);
		}

		y = y+35;
		JLabel adjustmentsLabel = new JLabel("Image Adjustments");
		adjustmentsLabel.setBounds(LEFT, y, adjustmentsLabel.getPreferredSize().width, 
		                           adjustmentsLabel.getPreferredSize().height);
		add(adjustmentsLabel);
		// Image opacity
		{
			y = y+25;
			JLabel opacityLabel = new JLabel("Opacity");
			opacityLabel.setBounds(LEFT+5, y, opacityLabel.getPreferredSize().width, 20);
			add(opacityLabel);

			opacityValue = new JSlider(0, 100);
			opacityValue.setMajorTickSpacing(100);
			opacityValue.setPaintTicks(true);
			opacityValue.setPaintLabels(true);
			opacityValue.setValue(100);
			opacityValue.setBounds(COLUMN1, y, RIGHT-COLUMN1, opacityValue.getPreferredSize().height);
			opacityValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					updateOpacity(opacityValue.getValue());
				}
			});
			add(opacityValue);
		}

		// Brightness
		{
			y = y+50;
			JLabel lightLabel = new JLabel("Brightness");
			lightLabel.setBounds(LEFT+5, y, lightLabel.getPreferredSize().width, 20);
			add(lightLabel);

			brightnessValue = new JSlider(-100, 100);
			brightnessValue.setMajorTickSpacing(100);
			brightnessValue.setPaintTicks(true);
			brightnessValue.setPaintLabels(true);
			brightnessValue.setValue(0);
			brightnessValue.setBounds(COLUMN1, y, RIGHT-COLUMN1, brightnessValue.getPreferredSize().height);
			brightnessValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					updateBrightness(brightnessValue.getValue());
				}
			});
			add(brightnessValue);
		}

		// Contrast
		{
			y = y+50;
			JLabel contrastLabel = new JLabel("Contrast");
			contrastLabel.setBounds(LEFT+5, y, contrastLabel.getPreferredSize().width, 20);
			add(contrastLabel);

			contrastValue = new JSlider(-100, 100);
			contrastValue.setMajorTickSpacing(100);
			contrastValue.setPaintTicks(true);
			contrastValue.setPaintLabels(true);
			contrastValue.setValue(0);
			contrastValue.setBounds(COLUMN1, y, RIGHT-COLUMN1, contrastValue.getPreferredSize().height);
			contrastValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					updateContrast(contrastValue.getValue());
				}
			});
			add(contrastValue);
		}

		iModifySAPreview();	
	}
	
	public ImageAnnotation getPreview(){
		return preview;
	}
	
	public void iModifySAPreview(){
		preview.setBorderColor(mAnnotation.getBorderColor());
		preview.setBorderWidth( Integer.parseInt( (String)(eThickness.getModel().getSelectedItem()) ) );
		preview.setImageOpacity((float)opacityValue.getValue()/100.0f);
		preview.setImageBrightness(brightnessValue.getValue());
		preview.setImageContrast(contrastValue.getValue());
		previewPanel.repaint();
	}	
	
	public void modifySAPreview(){
		preview.setBorderWidth( Integer.parseInt( (String)(eThickness.getModel().getSelectedItem()) ) );
	
		previewPanel.repaint();
	}	    

	private void edgeColorActionPerformed(java.awt.event.ActionEvent evt) {
		//Edge Color
		if(edgeColor.isSelected()) {
			sECButton.setEnabled(true);
			borderOValue.setEnabled(true);
		} else {
			sECButton.setEnabled(false);
			preview.setBorderColor(null);
			borderOValue.setEnabled(false);
		}
	}

	private void eThicknessActionPerformed(java.awt.event.ActionEvent evt) {
		//Edge Thickness
		modifySAPreview();
	}

	private void sECButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//sECButton
		final SelectColor sASelectColor=new SelectColor(mAnnotation.getBorderColor());
		sASelectColor.setOKListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Color clr = sASelectColor.getColor();
				preview.setBorderColor(mixColor(clr,borderOValue.getValue()));
				previewPanel.repaint();
			}
		});
	
		sASelectColor.setVisible(true);
		sASelectColor.setSize(435, 420);		
		//2 -> EdgeColor
	}

	private void updateBorderOpacity(int opacity) {
		preview.setBorderColor(mixColor(preview.getBorderColor(),opacity));
		previewPanel.repaint();
	}

	private void updateOpacity(int opacity) {
		preview.setImageOpacity((float)opacity/100.0f); 
		previewPanel.repaint();
	}

	private void updateBrightness(int brightness) {
		preview.setImageBrightness(brightness); 
		previewPanel.repaint();
	}

	private void updateContrast(int contrast) {
		preview.setImageContrast(contrast); 
		previewPanel.repaint();
	}

	private Paint mixColor(Paint p, int value) {
		if (p == null || !(p instanceof Color)) return p;
		Color c = (Color)p;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), value*255/100);
	}
}

