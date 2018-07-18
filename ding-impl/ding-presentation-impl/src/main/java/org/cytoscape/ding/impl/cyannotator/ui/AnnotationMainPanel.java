package org.cytoscape.ding.impl.cyannotator.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.LookAndFeelUtil.createPanelBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator.ReorderType;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;

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
	
	private static final int NAME_COL_IDX = 1;
	
	private JPanel buttonPanel;
	private JLabel infoLabel;
	private JLabel selectionLabel;
	private JButton removeAnnotationsButton;
	private JTable foregroundTable;
	private JTable backgroundTable;
	private JScrollPane ftScrollPane;
	private JScrollPane btScrollPane;
	private JButton selectAllButton;
	private JButton selectNoneButton;
	private final Map<String, AnnotationToggleButton> buttonMap = new LinkedHashMap<>();
	private final Map<Class<? extends Annotation>, Icon> iconMap = new LinkedHashMap<>();
	private final ButtonGroup buttonGroup;
	
	private SequentialGroup btnHGroup;
	private ParallelGroup btnVGroup;
	
	/** Default icon for Annotations that provide no icon */
	private Icon defIcon;
	
	private DGraphView view;
	
	private final CyServiceRegistrar serviceRegistrar;

	public AnnotationMainPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		// When a selected button is clicked again, we want it to be be unselected
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
		
		getBackgroundTable().getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				updateSelectionLabel();
				updateRemoveAnnotationsButton();
			}
		});
		getForegroundTable().getSelectionModel().addListSelectionListener(e -> {
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
	
	JToggleButton addAnnotationButton(AnnotationFactory<? extends Annotation> f) {
		final AnnotationToggleButton btn = new AnnotationToggleButton(f);
		btn.setFocusable(false);
		btn.setFocusPainted(false);
		
		buttonGroup.add(btn);
		buttonMap.put(f.getId(), btn);
		iconMap.put(f.getType(), f.getIcon());
		
		btnHGroup.addComponent(btn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		btnVGroup.addComponent(btn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		if (isAquaLAF())
			btn.putClientProperty("JButton.buttonType", "gradient");
		
		return btn;
	}
	
	void removeAnnotationButton(AnnotationFactory<? extends Annotation> f) {
		JToggleButton btn = buttonMap.remove(f.getId());
		iconMap.remove(f.getType());
		
		if (btn != null)
			getButtonPanel().remove(btn);
	}
	
	void addAnnotations(Collection<Annotation> list) {
		Map<String, Collection<Annotation>> map = separateByLayers(list);
		((AnnotationTableModel) getBackgroundTable().getModel()).addRows(map.get(Annotation.BACKGROUND));
		((AnnotationTableModel) getForegroundTable().getModel()).addRows(map.get(Annotation.FOREGROUND));
	}
	
	void removeAnnotations(Collection<Annotation> list) {
		Map<String, Collection<Annotation>> map = separateByLayers(list);
		((AnnotationTableModel) getBackgroundTable().getModel()).removeRows(map.get(Annotation.BACKGROUND));
		((AnnotationTableModel) getForegroundTable().getModel()).removeRows(map.get(Annotation.FOREGROUND));
	}
	
	Set<Annotation> getAllAnnotations() {
		final Set<Annotation> set = new HashSet<>();
		set.addAll(((AnnotationTableModel) getBackgroundTable().getModel()).getData());
		set.addAll(((AnnotationTableModel) getForegroundTable().getModel()).getData());
		
		return set;
	}
	
	Collection<Annotation> getSelectedAnnotations() {
		final Set<Annotation> set = new HashSet<>();
		set.addAll(getSelectedAnnotations(getBackgroundTable()));
		set.addAll(getSelectedAnnotations(getForegroundTable()));
		
		return set;
	}
	
	Collection<Annotation> getSelectedAnnotations(JTable table) {
		final Set<Annotation> set = new HashSet<>();
		final int rowCount = table.getRowCount();
		
		for (int i = 0; i < rowCount; i++) {
			if (table.isRowSelected(i)) {
				Annotation a = ((AnnotationTableModel) table.getModel()).getAnnotation(i);
				
				if (a != null)
					set.add(a);
			}
		}
		
		return set;
	}
	
	DGraphView getDGraphView() {
		return view;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		if (!enabled)
			clearAnnotationButtonSelection();
		
		buttonMap.values().forEach(btn -> btn.setEnabled(enabled));
		updateRemoveAnnotationsButton();
		updateSelectionButtons();
	}
	
	int getAnnotationCount() {
		return getBackgroundTable().getRowCount() + getForegroundTable().getRowCount();
	}
	
	int getSelectedAnnotationCount() {
		return getBackgroundTable().getSelectedRowCount() + getForegroundTable().getSelectedRowCount();
	}
	
	void clearAnnotationButtonSelection() {
		// Don't do buttonGroup.clearSelection(),
		// because we want the click event to be captured by the mediator
		for (AnnotationToggleButton btn : buttonMap.values()) {
			if (btn.isSelected()) {
				btn.doClick();
				break;
			}
		}
	}
	
	void setSelected(DingAnnotation a, boolean selected) {
		if (view == null || getAnnotationCount() == 0)
			return;
		
		final JTable table = Annotation.FOREGROUND.equals(a.getCanvasName()) ?
				getForegroundTable() : getBackgroundTable();
		final AnnotationTableModel model = (AnnotationTableModel) table.getModel();
		final int row = model.rowOf(a);
		
		if (row < 0 || row > table.getRowCount() - 1)
			return;
		
		if (selected) {
			table.addRowSelectionInterval(row, row);
			
			if (!isRowVisible(table, row))
				table.scrollRectToVisible(table.getCellRect(row, 0, true));
		} else {
			table.removeRowSelectionInterval(row, row);
		}
	}
	
	void update(DGraphView view) {
		this.view = view;
		
		final List<Annotation> annotations = view != null ? view.getCyAnnotator().getAnnotations()
				: Collections.emptyList();
		
		// Always clear the toggle button selection when annotations are added or removed
		clearAnnotationButtonSelection();
		// Enable/disable before next steps
		setEnabled(view != null);
		
		// Update annotation tables data
		Map<String, Collection<Annotation>> map = separateByLayers(annotations);
		((AnnotationTableModel) getBackgroundTable().getModel()).setData(map.get(Annotation.BACKGROUND));
		((AnnotationTableModel) getForegroundTable().getModel()).setData(map.get(Annotation.FOREGROUND));
		
		// Enable/disable annotation add buttons
		if (isEnabled()) {
			for (AnnotationToggleButton btn : buttonMap.values()) {
				if (ArrowAnnotation.class.equals(btn.getFactory().getType())) {
					// The ArrowAnnotation requires at least one other annotation before it can be added
					btn.setEnabled(getAnnotationCount() > 0);
					break;
				}
			}
		}
		
		// Labels and other components
		updateInfoLabel();
		updateSelectionLabel();
		updateSelectionButtons();
	}
	
	private void updateInfoLabel() {
		if (buttonGroup.getSelection() == null) {
			getInfoLabel().setText(isEnabled() ? "Select the Annotation you want to add..." : " ");
		} else {
			for (AnnotationToggleButton btn : buttonMap.values()) {
				if (btn.isSelected()) {
					if (ArrowAnnotation.class.equals(btn.getFactory().getType()))
						getInfoLabel().setText("Click another Annotation in the view...");
					else
						getInfoLabel().setText("Click anywhere on the view...");
					
					break;
				}
			}
		}
	}
	
	void updateSelectionLabel() {
		final int total = getAnnotationCount();
		
		if (total == 0) {
			getSelectionLabel().setText(null);
		} else {
			final int selected = getSelectedAnnotationCount();
			getSelectionLabel().setText(
					selected + " of " + total + " Annotation" + (total == 1 ? "" : "s") + " selected");
		}
	}
	
	private void updateRemoveAnnotationsButton() {
		getRemoveAnnotationsButton().setEnabled(isEnabled() && getSelectedAnnotationCount() > 0);
	}
	
	void updateSelectionButtons() {
		final int total = getAnnotationCount();
		final int selected = getSelectedAnnotationCount();
		
		getSelectAllButton().setEnabled(isEnabled() && selected < total);
		getSelectNoneButton().setEnabled(isEnabled() && selected > 0);
	}
	
	void updateAnnotationsOrder(ReorderType type) {
		if (type == ReorderType.CANVAS) {
			// Update all annotation tables data, because an annotation may have been moved to another layer
			final List<Annotation> annotations = view != null ? view.getCyAnnotator().getAnnotations()
					: Collections.emptyList();
			
			Map<String, Collection<Annotation>> map = separateByLayers(annotations);
			((AnnotationTableModel) getBackgroundTable().getModel()).setData(map.get(Annotation.BACKGROUND));
			((AnnotationTableModel) getForegroundTable().getModel()).setData(map.get(Annotation.FOREGROUND));
		} else {
			((AnnotationTableModel) getBackgroundTable().getModel()).sortData();
			((AnnotationTableModel) getForegroundTable().getModel()).sortData();
			((AnnotationTableModel) getBackgroundTable().getModel()).fireTableDataChanged();
			((AnnotationTableModel) getForegroundTable().getModel()).fireTableDataChanged();
		}
	}
	
	void stopTableCellEditing() {
		stopTableCellEditing(getBackgroundTable());
		stopTableCellEditing(getForegroundTable());
	}
	
	void stopTableCellEditing(JTable table) {
		TableCellEditor cellEditor = table.getCellEditor();
		
		if (cellEditor != null)
			cellEditor.stopCellEditing();
	}
	
	private boolean isRowVisible(JTable table, int row) {
		if (table.getParent() instanceof JViewport == false)
			return true;

		JViewport viewport = (JViewport) table.getParent();
		// This rectangle is relative to the table where the
		// northwest corner of cell (0,0) is always (0,0)
		Rectangle rect = table.getCellRect(row, 0, false);
		// The location of the viewport relative to the table
		Point pt = viewport.getViewPosition();
		// Translate the cell location so that it is relative
		// to the view, assuming the northwest corner of the view is (0,0)
		rect.setLocation(rect.x - pt.x, rect.y - pt.y);
		rect.width = 1;

		return viewport.contains(rect.getLocation());
	}
	
	private static Map<String, Collection<Annotation>> separateByLayers(Collection<Annotation> list) {
		Map<String, Collection<Annotation>> map = new HashMap<>();
		map.put(Annotation.BACKGROUND, new HashSet<>());
		map.put(Annotation.FOREGROUND, new HashSet<>());
		
		if (list != null) {
			list.forEach(a -> {
				Collection<Annotation> set = map.get(a.getCanvasName());
				
				if (set != null) // Should never be null, unless a new canvas name is created!
					set.add(a);
			});
		}
		
		return map;
	}
	
	private void init() {
		setOpaque(!isAquaLAF()); // Transparent if Aqua
		equalizeSize(getSelectAllButton(), getSelectNoneButton());
		
		JLabel fgLabel = createLayerTitleLabel(Annotation.FOREGROUND);
		JLabel bgLabel = createLayerTitleLabel(Annotation.BACKGROUND);
		
		// I don't know of a better way to center the selection label perfectly
		// other than by using this "filler" panel hack...
		JPanel leftFiller = new JPanel();
		leftFiller.setPreferredSize(getRemoveAnnotationsButton().getPreferredSize());
		leftFiller.setOpaque(!isAquaLAF());
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getButtonPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(leftFiller, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(getSelectionLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(getRemoveAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addContainerGap()
				)
				.addComponent(fgLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getFtScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bgLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBtScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addContainerGap()
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getButtonPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(CENTER, true)
						.addComponent(leftFiller, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getRemoveAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(fgLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getFtScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bgLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBtScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(CENTER, true)
						.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		
		setEnabled(false);
		
		// Stop editing when selection changes
		ListSelectionListener listSelectionListener = evt -> stopTableCellEditing();
		getBackgroundTable().getSelectionModel().addListSelectionListener(listSelectionListener);
		getForegroundTable().getSelectionModel().addListSelectionListener(listSelectionListener);
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
	
	JTable getForegroundTable() {
		if (foregroundTable == null) {
			foregroundTable = createAnnotationLayerTable();
		}
		
		return foregroundTable;
	}
	
	JTable getBackgroundTable() {
		if (backgroundTable == null) {
			backgroundTable = createAnnotationLayerTable();
		}
		
		return backgroundTable;
	}

	JScrollPane getFtScrollPane() {
		if (ftScrollPane == null) {
			ftScrollPane = createTableScrollPane(getForegroundTable());
		}
		
		return ftScrollPane;
	}
	
	JScrollPane getBtScrollPane() {
		if (btScrollPane == null) {
			btScrollPane = createTableScrollPane(getBackgroundTable());
		}
		
		return btScrollPane;
	}
	
	JButton getSelectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton("Select All");
			selectAllButton.addActionListener(evt -> {
				if (getBackgroundTable().getRowCount() > 0)
					getBackgroundTable().selectAll();
				if (getForegroundTable().getRowCount() > 0)
					getForegroundTable().selectAll();
			});
			
			makeSmall(selectAllButton);
			
			if (isAquaLAF()) {
				selectAllButton.putClientProperty("JButton.buttonType", "gradient");
				selectAllButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return selectAllButton;
	}
	
	JButton getSelectNoneButton() {
		if (selectNoneButton == null) {
			selectNoneButton = new JButton("Select None");
			selectNoneButton.addActionListener(evt -> {
				getBackgroundTable().clearSelection();
				getForegroundTable().clearSelection();
			});
			
			makeSmall(selectNoneButton);
			
			if (isAquaLAF()) {
				selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
				selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return selectNoneButton;
	}
	
	private JTable createAnnotationLayerTable() {
		final DefaultTableCellRenderer renderer = new AnnotationTableCellRenderer();
		
		final JTable table = new JTable(new AnnotationTableModel()) {
			@Override
			public void changeSelection(int row, int col, boolean toggle, boolean expand) {
				// This method is called when the user tries to move to a different cell.
				// If the cell they're trying to move to is not editable, just select the "name" cell.
				if (!getModel().isCellEditable(row, col))
					col = NAME_COL_IDX;

				super.changeSelection(row, col, toggle, expand);
			}
			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				return renderer;
			}
			@Override
			public Color getBackground() {
				// This guarantees the color will come from the correct Look-And-Feel,
				// even if this component is initialized before swing-application-impl
				return UIManager.getColor("Panel.background");
			}
			@Override
			public Color getGridColor() {
				return UIManager.getColor("Separator.foreground");
			}
		};
		
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(false);
		table.setIntercellSpacing(new Dimension(0, 2));
		table.setRowHeight(28);
		table.setTableHeader(null);
		table.setColumnSelectionAllowed(false);
		
		table.getColumnModel().getColumn(0).setWidth(32);
		table.getColumnModel().getColumn(0).setPreferredWidth(32);
		table.getColumnModel().getColumn(0).setMaxWidth(32);
		
		makeSmall(table);
		
		return table;
	}
	
	private JScrollPane createTableScrollPane(JTable table) {
		JScrollPane sp = new JScrollPane() {
			@Override
			public Color getBackground() {
				// This guarantees the color will come from the correct Look-And-Feel,
				// even if this component is initialized before swing-application-impl
				return UIManager.getColor("Panel.background");
			}
		};
		sp.setViewportView(table);
		sp.getViewport().setOpaque(false);
		sp.getViewport().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				stopTableCellEditing(table);
				table.clearSelection();
				sp.requestFocusInWindow();
			}
		});
		
		return sp;
	}
	
	private JLabel createLayerTitleLabel(String name) {
		JLabel label = new JLabel(name.toUpperCase() + " Layer");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		label.setBorder(BorderFactory.createEmptyBorder(6, 12, 2, 12	));
		
		makeSmall(label);
		
		if (isAquaLAF())
			label.putClientProperty("JComponent.sizeVariant", "mini");
		
		return label;
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
	
	private Icon getDefIcon() {
		if (defIcon == null) {
			// Lazily initialize the icon here, because the LAF might not have been set yet,
			// and we need to get the correct colors
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 30f);
			defIcon = new TextIcon(
					new String[] { IconUtil.ICON_ANNOTATION_1, IconUtil.ICON_ANNOTATION_2 },
					font,
					new Color[] { UIManager.getColor("Label.foreground"), Color.WHITE },
					AbstractDingAnnotationFactory.ICON_SIZE,
					AbstractDingAnnotationFactory.ICON_SIZE,
					1
			);
		}
		
		return defIcon;
	}
	
	class AnnotationToggleButton extends JToggleButton {
		
		private final AnnotationFactory<? extends Annotation> factory;

		public AnnotationToggleButton(AnnotationFactory<? extends Annotation> f) {
			this.factory = f;
			Icon icon = f.getIcon();
			
			setIcon(icon != null ? icon : getDefIcon());
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
		
		public List<Annotation> getData() {
			return new ArrayList<>(data);
		}
		
		public int rowOf(Annotation a) {
			return data.indexOf(a);
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
			return col == NAME_COL_IDX;
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
			if (col == 1 && row >= 0 && row < getRowCount() && value instanceof String)
				data.get(row).setName((String) value);
		}
		
		private void sortData() {
			if (data.size() <= 1)
				return;
			
			Collections.sort(data, (a1, a2) -> {
				if (a1 instanceof DingAnnotation && a2 instanceof DingAnnotation) {
					JComponent canvas1 = ((DingAnnotation) a1).getCanvas();
					JComponent canvas2 = ((DingAnnotation) a2).getCanvas();
					int z1 = canvas1.getComponentZOrder(((DingAnnotation) a1).getComponent());
					int z2 = canvas2.getComponentZOrder(((DingAnnotation) a2).getComponent());
					
					return Integer.compare(z1, z2);
				}
				
				return 0;
			});
		}
		
		public Object getIcon(Annotation a) {
			Icon icon = null;
			
			if (a instanceof DingAnnotation)
				icon = iconMap.get(((DingAnnotation) a).getType());

			return icon != null ? icon : getDefIcon();
		}
	}
	
	private final class AnnotationTableCellRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			this.setOpaque(true);
			this.setBorder(null); // Do not highlight the focused cell
			
			if (value instanceof String)
				this.setToolTipText((String) value);
			else
				this.setToolTipText(null);
			
			if (value instanceof Icon) {
				this.setIcon((Icon) value);
				this.setText(null);
				this.setHorizontalAlignment(CENTER);
			} else {
				this.setIcon(null);
				this.setHorizontalAlignment(LEFT);
			}
			
			if (isSelected) {
				this.setForeground(UIManager.getColor("Table.selectionForeground"));
				this.setBackground(UIManager.getColor("Table.selectionBackground"));
			} else {
				this.setBackground(UIManager.getColor("Panel.background"));
				this.setForeground(UIManager.getColor("Table.foreground"));
			}
				
			return this;
		}
	}
}
