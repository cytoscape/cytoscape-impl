package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.Dimension;
import java.util.SortedSet;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

public class VisualStylePanel {
	
	private OptionsButton optionsBtn;
	private JPanel stylesPnl;
	
	protected VisualStyleDropDownButton stylesBtn;
	protected VisualStyleSelector styleSelector;
	
	private final ServicesUtil servicesUtil;
	
	public VisualStylePanel(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
		styleSelector = new VisualStyleSelector(2, 0, servicesUtil);
	}
	
	
	public JComponent getComponent() {
		return getStylesPnl();
	}
	
	public VisualStyle getSelectedVisualStyle() {
		return styleSelector.getSelectedStyle();
	}
	
	public void setSelectedVisualStyle(final VisualStyle style) {
		getStylesBtn().setSelectedItem(style);
	}
	
	
	
	private JPanel getStylesPnl() {
		if (stylesPnl == null) {
			stylesPnl = new JPanel();
			stylesPnl.setOpaque(!isAquaLAF());
			
			// TODO: For some reason, the Styles button is naturally taller than the Options one on Nimbus and Windows.
			//       Let's force it to have the same height.
			getStylesBtn().setPreferredSize(
					new Dimension(getStylesBtn().getPreferredSize().width, getOptionsBtn().getOptionsBtn().getPreferredSize().height));
			
			var layout = new GroupLayout(stylesPnl);
			stylesPnl.setLayout(layout);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(getStylesBtn(), 0, 146, Short.MAX_VALUE)
					.addComponent(getOptionsBtn().getOptionsBtn(), PREFERRED_SIZE, 64, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(getStylesBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getOptionsBtn().getOptionsBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return stylesPnl;
	}
	
	public void updateVisualStyles(SortedSet<VisualStyle> styles, VisualStyle selectedStyle) {
		getStylesBtn().update(styles, selectedStyle);
	}
	
	public Component getDefaultView(VisualStyle vs) {
		return styleSelector.getDefaultView(vs);
	}
	
	VisualStyleDropDownButton getStylesBtn() {
		if (stylesBtn == null) {
			stylesBtn = new VisualStyleDropDownButton();
			stylesBtn.setToolTipText("Current Style");
		}
		
		return stylesBtn;
	}
	
	OptionsButton getOptionsBtn() {
		if (optionsBtn == null) {
			optionsBtn = new OptionsButton(servicesUtil);
		}
		return optionsBtn;
	}
	

	@SuppressWarnings("serial")
	class VisualStyleDropDownButton extends DropDownMenuButton {

		private JPopupMenu popup;
		
		VisualStyleDropDownButton() {
			super(true);
			
			setHorizontalAlignment(LEFT);
			
			addActionListener(evt -> {
				if (!styleSelector.isEmpty())
					showDialog();
			});
			
			styleSelector.addPropertyChangeListener("selectedStyle", evt -> {
				update();
				disposePopup();
				firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
			});
		}
		
		public void update(SortedSet<VisualStyle> styles, VisualStyle selectedStyle) {
			styleSelector.update(styles, selectedStyle);
			setEnabled(!styleSelector.isEmpty());
		}
		
		public void setSelectedItem(VisualStyle vs) {
			styleSelector.setSelectedStyle(vs);
			
			if (styleSelector.isEditMode())
				update(vs);
		}
		
		public void update() {
			update(styleSelector.getSelectedStyle());
		}
		
		private void update(VisualStyle selectedStyle) {
			setText(selectedStyle != null ? selectedStyle.getTitle() : "");
			repaint();
		}
		
		private void showDialog() {
			setEnabled(false); // Disable the button to prevent accidental repeated clicks
			disposePopup(); // Just to make sure there will never be more than one popup
			
			popup = new JPopupMenu();
			popup.setBackground(styleSelector.getBackground());
			popup.setBorder(BorderFactory.createEmptyBorder());
			
			popup.addPropertyChangeListener("visible", evt -> {
				if (evt.getNewValue() == Boolean.FALSE)
					onPopupDisposed();
			});
			
			var layout = new GroupLayout(popup);
			popup.setLayout(layout);
			layout.setAutoCreateGaps(false);
			layout.setAutoCreateContainerGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(styleSelector, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(styleSelector, DEFAULT_SIZE, DEFAULT_SIZE, 660)
			);
			
			if (getSize() != null && getSize().width > 0)
				popup.setPreferredSize(new Dimension(getSize().width, popup.getPreferredSize().height));
			
			popup.pack();
			popup.show(VisualStyleDropDownButton.this, 0, 0);
			popup.requestFocus();
		}

		private void disposePopup() {
			if (popup != null)
				popup.setVisible(false);
			
			styleSelector.setEditMode(false);
		}

		private void onPopupDisposed() {
			if (popup != null) {
				popup.removeAll();
				popup = null;
			}
			
			setEnabled(!styleSelector.isEmpty()); // Re-enable the Styles button
			styleSelector.resetFilter();
		}
	}
}
