package org.cytoscape.ding.impl.cyannotator.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.LookAndFeelUtil.createPanelBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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
public class AnnotationMainPanel extends JPanel implements CytoPanelComponent2 {

	public static final float ICON_FONT_SIZE = 18.0f;
	
	private static final String TITLE = "Annotation";
	private static final String ID = "org.cytoscape.Annotation";
	
	private JPanel buttonPanel;
	private JLabel infoLabel;
	private JLabel selectionLabel;
	private JButton removeAnnotationsButton;
	private JPanel listPanel;
	private JTable listTable;
	private JScrollPane listScrollPane;
	private final Map<String, AnnotationToggleButton> buttonMap = new LinkedHashMap<>();
	private final Map<Class<? extends Annotation>, Icon> iconMap = new LinkedHashMap<>();
	private final ButtonGroup buttonGroup;
	
	private SequentialGroup btnHGroup;
	private ParallelGroup btnVGroup;
	
	/** Default icon for Annotations that provide no icon */
	private final Icon defIcon;
	
	private final CyServiceRegistrar serviceRegistrar;

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public AnnotationMainPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		Font font = serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 30f);
		defIcon = new TextIcon(
				IconUtil.ICON_ANNOTATION,
				font,
				AbstractDingAnnotationFactory.ICON_SIZE,
				AbstractDingAnnotationFactory.ICON_SIZE
		);
		
		// When a selected button is clicked again, we want it to be be deselected
		buttonGroup = new ButtonGroup() {
			private boolean isAdjusting;
			private ButtonModel prevModel;
			
			@Override
			public void setSelected(ButtonModel m, boolean b) {
				if (isAdjusting) return;
				if (m != null && m.equals(prevModel)) {
					isAdjusting = true;
					clearSelection();
					isAdjusting = false;
				} else {
					super.setSelected(m, b);
				}
				prevModel = getSelection();
				updateInfoLabel();
			}
		};
		
		init();
		
		getListTable().getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				updateSelectionLabel();
				updateRemoveAnnotationsButton();
			}
		});
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}
	
	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public Icon getIcon() {
		return null;
	}
	
	public JToggleButton addAnnotationButton(AnnotationFactory<? extends Annotation> f) {
		final AnnotationToggleButton btn = new AnnotationToggleButton(f);
		buttonGroup.add(btn);
		buttonMap.put(f.getId(), btn);
		iconMap.put(f.getType(), f.getIcon());
		
		btnHGroup.addComponent(btn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		btnVGroup.addComponent(btn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		if (isAquaLAF()) {
			btn.putClientProperty("JButton.buttonType", "segmentedTextured");
			updateSegmentedButtonStyles();
		}
		
		return btn;
	}
	
	public void removeAnnotationButton(AnnotationFactory<? extends Annotation> f) {
		JToggleButton btn = buttonMap.remove(f.getId());
		iconMap.remove(f.getType());
		
		if (btn != null) {
			getButtonPanel().remove(btn);
			
			if (isAquaLAF())
				updateSegmentedButtonStyles();
		}
	}
	
	public void addAnnotations(Collection<Annotation> list) {
		((AnnotationTableModel) getListTable().getModel()).addRows(list);
	}
	
	public void removeAnnotations(Collection<Annotation> list) {
		((AnnotationTableModel) getListTable().getModel()).removeRows(list);
	}
	
	public Collection<Annotation> getSelectedAnnotations() {
		final Set<Annotation> set = new HashSet<>();
		final int rowCount = getListTable().getRowCount();
		
		for (int i = 0; i < rowCount; i++) {
			if (getListTable().isRowSelected(i)) {
				Annotation a = ((AnnotationTableModel) getListTable().getModel()).getAnnotation(i);
				
				if (a != null)
					set.add(a);
			}
		}
		
		return set;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		if (!enabled)
			buttonGroup.clearSelection();
		
		buttonMap.values().forEach(btn -> btn.setEnabled(enabled));
		updateRemoveAnnotationsButton();
	}
	
	void update(CyNetworkView view, Collection<Annotation> annotations) {
		setEnabled(view instanceof DGraphView);
		((AnnotationTableModel) getListTable().getModel()).setData(annotations);
		updateInfoLabel();
		updateSelectionLabel();
	}
	
	private void updateInfoLabel() {
		if (buttonGroup.getSelection() == null) {
			getInfoLabel().setText("Select the Annotation you want to add...");
		} else {
			for (AnnotationToggleButton btn : buttonMap.values()) {
				if (btn.isSelected()) {
					if (btn.getFactory().getType() == ArrowAnnotation.class)
						getInfoLabel().setText("Click another Annotation in the view...");
					else
						getInfoLabel().setText("Click anywhere on the view...");
					
					break;
				}
			}
		}
	}
	
	void updateSelectionLabel() {
		final int total = getListTable().getRowCount();
		
		if (total == 0) {
			getSelectionLabel().setText(null);
		} else {
			final int selected = getListTable().getSelectedRowCount();
			getSelectionLabel().setText(
					selected + " of " + total + " Annotation" + (total == 1 ? "" : "s") + " selected");
		}
	}
	
	private void updateSegmentedButtonStyles() {
		final List<JToggleButton> buttons = new ArrayList<>(buttonMap.values());
		final int total = buttons.size();
		
		if (total == 1) {
			buttons.get(0).putClientProperty("JButton.segmentPosition", "only");
		} else {
			for (int i = 0; i < total; i++) {
				final JToggleButton btn = buttons.get(i);
				final String position;
				
				if (i == 0)
					position = "first";
				else if (i == total - 1)
					position = "last";
				else
					position = "middle";
				
				btn.putClientProperty("JButton.segmentPosition", position);
			}
		}
		
		getButtonPanel().repaint();
	}
	
	private void updateRemoveAnnotationsButton() {
		getRemoveAnnotationsButton().setEnabled(isEnabled() && getListTable().getSelectedRowCount() > 0);
	}
	
	private void init() {
		setOpaque(!isAquaLAF()); // Transparent if Aqua

		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getButtonPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(getSelectionLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(getRemoveAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addContainerGap()
				)
				.addComponent(getListPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getButtonPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(CENTER, true)
						.addComponent(getSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getRemoveAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getListPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		setEnabled(false);
	}
	
	JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setBorder(createPanelBorder());
			buttonPanel.setOpaque(!isAquaLAF()); // Transparent if Aqua
			
			final GroupLayout layout = new GroupLayout(buttonPanel);
			buttonPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addGap(0, 0, Short.MAX_VALUE)
							.addGroup(btnHGroup = layout.createSequentialGroup())
							.addGap(0, 0, Short.MAX_VALUE)
					)
					.addComponent(getInfoLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(btnVGroup = layout.createParallelGroup(CENTER, true))
					.addComponent(getInfoLabel())
			);
		}
		
		return buttonPanel;
	}
	
	private JLabel getInfoLabel() {
		if (infoLabel == null) {
			infoLabel = new JLabel(" ");
			infoLabel.setHorizontalAlignment(JLabel.CENTER);
			infoLabel.setEnabled(false);
			makeSmall(infoLabel);
		}
		
		return infoLabel;
	}
	
	private JLabel getSelectionLabel() {
		if (selectionLabel == null) {
			selectionLabel = new JLabel();
			selectionLabel.setHorizontalAlignment(JLabel.CENTER);
			makeSmall(selectionLabel);
		}
		
		return selectionLabel;
	}
	
	JButton getRemoveAnnotationsButton() {
		if (removeAnnotationsButton == null) {
			removeAnnotationsButton = new JButton(IconManager.ICON_TRASH_O);
			removeAnnotationsButton.setToolTipText("Remove Selected Annotations");
			removeAnnotationsButton.setBorderPainted(false);
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(removeAnnotationsButton, iconManager.getIconFont(18f));
			updateRemoveAnnotationsButton();
		}
		
		return removeAnnotationsButton;
	}
	
	private JPanel getListPanel() {
		if (listPanel == null) {
			listPanel = new JPanel(new BorderLayout());
			listPanel.setOpaque(!isAquaLAF()); // Transparent if Aqua
			
			listPanel.add(getListScrollPane(), BorderLayout.CENTER);
		}
		
		return listPanel;
	}
	
	JTable getListTable() {
		if (listTable == null) {
			listTable = new JTable(new AnnotationTableModel());
			listTable.setShowHorizontalLines(true);
			listTable.setShowVerticalLines(false);
			listTable.setGridColor(UIManager.getColor("Separator.foreground"));
			listTable.setIntercellSpacing(new Dimension(0, 0));
			listTable.setRowHeight(24);
			listTable.setTableHeader(null);
			
			listTable.getColumnModel().getColumn(0).setWidth(24);
			listTable.getColumnModel().getColumn(0).setPreferredWidth(24);
			listTable.getColumnModel().getColumn(0).setMaxWidth(24);
		}
		
		return listTable;
	}
	
	JScrollPane getListScrollPane() {
		if (listScrollPane == null) {
			listScrollPane = new JScrollPane(getListTable());
			listScrollPane.getViewport().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					getListTable().clearSelection();
				}
			});
		}
		
		return listScrollPane;
	}
	
	private void styleToolBarButton(AbstractButton btn, Font font) {
		if (font != null)
			btn.setFont(font);
		
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		btn.setContentAreaFilled(false);
		btn.setOpaque(false);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalTextPosition(SwingConstants.TOP);
	}
	
	class AnnotationToggleButton extends JToggleButton {
		
		private final AnnotationFactory<? extends Annotation> factory;

		public AnnotationToggleButton(AnnotationFactory<? extends Annotation> f) {
			this.factory = f;
			Icon icon = f.getIcon();
			
			setIcon(icon != null ? icon : defIcon);
			setToolTipText(f.getName());
			setHorizontalTextPosition(SwingConstants.CENTER);
		}
		
		public AnnotationFactory<? extends Annotation> getFactory() {
			return factory;
		}
	}
	
	class AnnotationTableModel extends AbstractTableModel {

		private final String[] COL_NAMES = new String[] { "Icon", "Annotation Name" };
		private final Class<?>[] COL_TYPES = new Class<?>[] { Icon.class, String.class };
		
		private final List<Annotation> data = new ArrayList<>();

		public void setData(Collection<Annotation> data) {
			this.data.clear();
			
			if (data != null) {
				this.data.addAll(data);
				sortData();
			}
			
			fireTableDataChanged();
		}

		public void addRows(Collection<Annotation> list) {
			final Set<Annotation> set = new HashSet<>(data); // Avoiding duplicates
			
			if (set.addAll(list)) {
				data.clear();
				data.addAll(set);
				sortData();
				fireTableDataChanged();
			}
		}
		
		public void removeRows(Collection<Annotation> list) {
			if (data.removeAll(list)) {
				sortData();
				fireTableDataChanged();
			}
		}
		
		public Annotation getAnnotation(int row) {
			return row >= 0 && data.size() > row ? data.get(row) : null;
		}
		
		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return COL_NAMES.length;
		}

		@Override
		public String getColumnName(int col) {
			return COL_NAMES[col];
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return COL_TYPES[col];
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (row >= data.size())
				return null;
			
			Annotation annotation = data.get(row);
			
			if (annotation == null)
				return null; // Should not happen!
			
			if (col == 0)
				return getIcon(annotation);
			if (col == 1)
				return annotation.getName();
			
			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			// Nothing to do here...
		}
		
		private void sortData() {
			if (data.size() <= 1)
				return;
			
			Collections.sort(data, (a1, a2) -> {
				if (a1.getCanvasName().equals(a2.getCanvasName())) {
					// TODO sort by Z-index
				} else if (a1.getCanvasName().equals(Annotation.BACKGROUND)) {
					return -1;
				} else {
					return 1;
				}
				
				return 0;
			});
		}
		
		public Object getIcon(Annotation a) {
			Icon icon = null;
			
			if (a instanceof DingAnnotation)
				icon = iconMap.get(((DingAnnotation) a).getType());

			return icon != null ? icon : defIcon;
		}
	}
}
