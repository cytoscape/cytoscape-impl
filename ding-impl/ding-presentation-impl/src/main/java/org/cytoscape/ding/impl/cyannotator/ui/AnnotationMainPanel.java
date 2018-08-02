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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

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
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

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

	private static final String TITLE = "Annotation";
	private static final String ID = "org.cytoscape.Annotation";
	
	private JPanel buttonPanel;
	private JLabel infoLabel;
	private JLabel selectionLabel;
	private JButton groupAnnotationsButton;
	private JButton ungroupAnnotationsButton;
	private JButton removeAnnotationsButton;
	private JTree foregroundTree;
	private JTree backgroundTree;
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
	/** GroupAnnotation icon when collapsed */
	private Icon closedAnnotationIcon;
	/** GroupAnnotation icon when expanded */
	private Icon openAnnotationIcon;
	
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
		
		getBackgroundTree().getSelectionModel().addTreeSelectionListener(e -> {
			stopTreeCellEditing();
			updateSelectionLabel();
			updateGroupUngroupButton();
			updateRemoveAnnotationsButton();
		});
		getForegroundTree().getSelectionModel().addTreeSelectionListener(e -> {
			stopTreeCellEditing();
			updateSelectionLabel();
			updateGroupUngroupButton();
			updateRemoveAnnotationsButton();
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
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		if (!enabled)
			clearAnnotationButtonSelection();
		
		buttonMap.values().forEach(btn -> btn.setEnabled(enabled));
		updateGroupUngroupButton();
		updateRemoveAnnotationsButton();
		updateSelectionButtons();
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
		((AnnotationTreeModel) getBackgroundTree().getModel()).addRows(map.get(Annotation.BACKGROUND));
		((AnnotationTreeModel) getForegroundTree().getModel()).addRows(map.get(Annotation.FOREGROUND));
	}
	
	void removeAnnotations(Collection<Annotation> list) {
		Map<String, Collection<Annotation>> map = separateByLayers(list);
		((AnnotationTreeModel) getBackgroundTree().getModel()).removeRows(map.get(Annotation.BACKGROUND));
		((AnnotationTreeModel) getForegroundTree().getModel()).removeRows(map.get(Annotation.FOREGROUND));
	}
	
	Set<Annotation> getAllAnnotations() {
		final Set<Annotation> set = new HashSet<>();
		set.addAll(((AnnotationTreeModel) getBackgroundTree().getModel()).getData());
		set.addAll(((AnnotationTreeModel) getForegroundTree().getModel()).getData());
		
		return set;
	}
	
	Collection<Annotation> getSelectedAnnotations() {
		final Set<Annotation> set = new HashSet<>();
		set.addAll(getSelectedAnnotations(getBackgroundTree()));
		set.addAll(getSelectedAnnotations(getForegroundTree()));
		
		return set;
	}
	
	List<Annotation> getSelectedAnnotations(JTree tree) {
		return getSelectedAnnotations(tree, Annotation.class);
	}
	
	<T extends Annotation> Collection<T> getSelectedAnnotations(Class<T> type) {
		final Set<T> set = new LinkedHashSet<>();
		set.addAll(getSelectedAnnotations(getBackgroundTree(), type));
		set.addAll(getSelectedAnnotations(getForegroundTree(), type));
		
		return set;
	}
	
	@SuppressWarnings("unchecked")
	<T extends Annotation> List<T> getSelectedAnnotations(JTree tree, Class<T> type) {
		final List<T> set = new ArrayList<>();
		final TreePath[] treePaths = tree.getSelectionModel().getSelectionPaths();
		
		for (TreePath path : treePaths) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object obj = node.getUserObject();
			
			if (type.isAssignableFrom(obj.getClass()))
				set.add((T) obj);
		}
		
		return set;
	}
	
	DGraphView getDGraphView() {
		return view;
	}
	
	int getAnnotationCount() {
		return getBackgroundTree().getRowCount() + getForegroundTree().getRowCount();
	}
	
	int getSelectedAnnotationCount() {
		return getBackgroundTree().getSelectionCount() + getForegroundTree().getSelectionCount();
	}
	
	int getSelectedAnnotationCount(Class<? extends Annotation> type) {
		return getSelectedAnnotationCount(getBackgroundTree(), type)
				+ getSelectedAnnotationCount(getForegroundTree(), type);
	}
	
	int getSelectedAnnotationCount(JTree tree, Class<? extends Annotation> type) {
		int count = 0;
		final TreePath[] treePaths = tree.getSelectionModel().getSelectionPaths();
		
		for (TreePath path : treePaths) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object obj = node.getUserObject();
			
			if (type.isAssignableFrom(obj.getClass()))
				count++;
		}
		
		return count;
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
	
	void setSelected(Annotation a, boolean selected) {
		if (view == null || getAnnotationCount() == 0)
			return;
		
		final JTree tree = Annotation.FOREGROUND.equals(a.getCanvasName()) ?
				getForegroundTree() : getBackgroundTree();
		final AnnotationTreeModel model = (AnnotationTreeModel) tree.getModel();
		final int row = model.rowOf(a);
		
		if (row < 0 || row > tree.getRowCount() - 1)
			return;
		
		if (selected) {
			tree.addSelectionInterval(row, row);
			
			if (!isRowVisible(tree, row))
				tree.scrollRectToVisible(tree.getRowBounds(row));
		} else {
			tree.removeSelectionInterval(row, row);
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
		
		// Update annotation trees
		Map<String, Collection<Annotation>> map = separateByLayers(annotations);
		getBackgroundTree().setModel(
				new AnnotationTreeModel(Annotation.BACKGROUND, map.get(Annotation.BACKGROUND)));
		getForegroundTree().setModel(
				new AnnotationTreeModel(Annotation.FOREGROUND, map.get(Annotation.FOREGROUND)));
		
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
	
	private void updateGroupUngroupButton() {
		getGroupAnnotationsButton().setEnabled(isEnabled() && getSelectedAnnotationCount() > 1);
		getUngroupAnnotationsButton().setEnabled(isEnabled() && getSelectedAnnotationCount(GroupAnnotation.class) > 0);
	}
	
	void updateSelectionButtons() {
		final int total = getAnnotationCount();
		final int selected = getSelectedAnnotationCount();
		
		getSelectAllButton().setEnabled(isEnabled() && selected < total);
		getSelectNoneButton().setEnabled(isEnabled() && selected > 0);
	}
	
	void updateAnnotationsOrder(ReorderType type) {
		Collection<Annotation> selectedAnnotations = getSelectedAnnotations();
		
//		getBackgroundTree().getSelectionModel().setValueIsAdjusting(true);
//		getForegroundTree().getSelectionModel().setValueIsAdjusting(true);
		
		try {
			// Update all annotation trees, because an annotation may have been moved to another layer
			final List<Annotation> annotations = view != null ? view.getCyAnnotator().getAnnotations()
					: Collections.emptyList();
			{
				Map<String, Collection<Annotation>> map = separateByLayers(annotations);
				getBackgroundTree().setModel(
						new AnnotationTreeModel(Annotation.BACKGROUND, map.get(Annotation.BACKGROUND)));
				getForegroundTree().setModel(
						new AnnotationTreeModel(Annotation.FOREGROUND, map.get(Annotation.FOREGROUND)));
			}
			// Restore the row selection (the annotations that were selected before must be still selected)
			getBackgroundTree().clearSelection();
			getForegroundTree().clearSelection();
			Map<String, Collection<Annotation>> map = separateByLayers(selectedAnnotations);
			map.get(Annotation.BACKGROUND).forEach(a -> setSelected(a, true));
			map.get(Annotation.FOREGROUND).forEach(a -> setSelected(a, true));
		} finally {
//			getBackgroundTree().getSelectionModel().setValueIsAdjusting(false);
//			getForegroundTree().getSelectionModel().setValueIsAdjusting(false);
		}
	}
	
	void stopTreeCellEditing() {
		stopCellEditing(getBackgroundTree());
		stopCellEditing(getForegroundTree());
	}
	
	void stopCellEditing(JTree tree) {
		if (tree.isEditing())
			tree.stopEditing();
	}
	
	private boolean isRowVisible(JTree tree, int row) {
		if (tree.getParent() instanceof JViewport == false)
			return true;

		JViewport viewport = (JViewport) tree.getParent();
		// This rectangle is relative to the trees where the
		// northwest corner of cell (0,0) is always (0,0)
		Rectangle rect = tree.getRowBounds(row);
		// The location of the viewport relative to the tree
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
		equalizeSize(getGroupAnnotationsButton(), getUngroupAnnotationsButton(), getRemoveAnnotationsButton());
		equalizeSize(getSelectAllButton(), getSelectNoneButton());
		
		JLabel fgLabel = createLayerTitleLabel(Annotation.FOREGROUND);
		JLabel bgLabel = createLayerTitleLabel(Annotation.BACKGROUND);
		
		// I don't know of a better way to center the selection label perfectly
		// other than by using this "filler" panel hack...
		JPanel rightFiller = new JPanel();
		rightFiller.setPreferredSize(getRemoveAnnotationsButton().getPreferredSize());
		rightFiller.setOpaque(!isAquaLAF());
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getButtonPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(getGroupAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getUngroupAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(getSelectionLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(rightFiller, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
						.addComponent(getGroupAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getUngroupAnnotationsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(rightFiller, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
	
	JButton getGroupAnnotationsButton() {
		if (groupAnnotationsButton == null) {
			groupAnnotationsButton = new JButton(IconManager.ICON_OBJECT_GROUP);
			groupAnnotationsButton.setToolTipText("Group Selected Annotations");
			groupAnnotationsButton.setBorderPainted(false);
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(groupAnnotationsButton, iconManager.getIconFont(16f));
		}
		
		return groupAnnotationsButton;
	}
	
	JButton getUngroupAnnotationsButton() {
		if (ungroupAnnotationsButton == null) {
			ungroupAnnotationsButton = new JButton(IconManager.ICON_OBJECT_UNGROUP);
			ungroupAnnotationsButton.setToolTipText("Ungroup Selected Annotations");
			ungroupAnnotationsButton.setBorderPainted(false);
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(ungroupAnnotationsButton, iconManager.getIconFont(16f));
		}
		
		return ungroupAnnotationsButton;
	}
	
	JButton getRemoveAnnotationsButton() {
		if (removeAnnotationsButton == null) {
			removeAnnotationsButton = new JButton(IconManager.ICON_TRASH_O);
			removeAnnotationsButton.setToolTipText("Remove Selected Annotations");
			removeAnnotationsButton.setBorderPainted(false);
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(removeAnnotationsButton, iconManager.getIconFont(18f));
		}
		
		return removeAnnotationsButton;
	}
	
	JTree getForegroundTree() {
		if (foregroundTree == null) {
			foregroundTree = createLayerTree(Annotation.FOREGROUND);
		}
		
		return foregroundTree;
	}
	
	JTree getBackgroundTree() {
		if (backgroundTree == null) {
			backgroundTree = createLayerTree(Annotation.BACKGROUND);
		}
		
		return backgroundTree;
	}
	
	JTree getLayerTree(String canvasName) {
		return Annotation.BACKGROUND.equals(canvasName) ? getBackgroundTree() : getForegroundTree();
	}

	JScrollPane getFtScrollPane() {
		if (ftScrollPane == null) {
			ftScrollPane = createTreeScrollPane(getForegroundTree());
		}
		
		return ftScrollPane;
	}
	
	JScrollPane getBtScrollPane() {
		if (btScrollPane == null) {
			btScrollPane = createTreeScrollPane(getBackgroundTree());
		}
		
		return btScrollPane;
	}
	
	JButton getSelectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton("Select All");
			selectAllButton.addActionListener(evt -> {
				if (getBackgroundTree().getRowCount() > 0)
					getBackgroundTree().setSelectionInterval(0, getBackgroundTree().getRowCount() - 1);
				if (getForegroundTree().getRowCount() > 0)
					getForegroundTree().setSelectionInterval(0, getForegroundTree().getRowCount() - 1);
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
				getBackgroundTree().clearSelection();
				getForegroundTree().clearSelection();
			});
			
			makeSmall(selectNoneButton);
			
			if (isAquaLAF()) {
				selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
				selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return selectNoneButton;
	}
	
	private JTree createLayerTree(String name) {
		final AnnotationTreeCellRenderer annotationCellRenderer = new AnnotationTreeCellRenderer();
		
		final JTree tree = new JTree(new AnnotationTreeModel(name)) {
			@Override
			public TreeCellRenderer getCellRenderer() {
				return annotationCellRenderer;
			}
			@Override
			public Color getBackground() {
				// This guarantees the color will come from the correct Look-And-Feel,
				// even if this component is initialized before swing-application-impl
				return UIManager.getColor("Panel.background");
			}
			@Override
			public void paintComponent(Graphics g) {
				// Highlight entire JTree row on selection...
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				
				if (getSelectionCount() > 0) {
					for (int i : getSelectionRows()) {
						Rectangle r = getRowBounds(i);
						g.setColor(UIManager.getColor("Table.selectionBackground"));
						g.fillRect(0, r.y, getWidth(), r.height);
					}
				}
				
				super.paintComponent(g);
			}
			@Override
			public void setModel(TreeModel newModel) {
				super.setModel(newModel);
				// Or expand icons of first annotation groups won't be displayed
				setRootVisible(true);
				
				setRootVisible(false);
				expandRow(0);
			}
		};
		// Highlight entire JTree row on selection...
		tree.setUI(new BasicTreeUI() {
			@Override
			public Rectangle getPathBounds(JTree tree, TreePath path) {
				if (tree != null && treeState != null)
					return getPathBounds(path, tree.getInsets(), new Rectangle());
				
				return null;
			}
			
			private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
				bounds = treeState.getBounds(path, bounds);
				
				if (bounds != null) {
					bounds.width = tree.getWidth();
					bounds.y += insets.top;
				}
				
				return bounds;
			}
		});
		tree.setName(name);
		tree.setOpaque(false);
		tree.setRowHeight(24);
		makeSmall(tree);
		
		tree.setEditable(true);
		// Start editing with space key
		tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "startEditing");
		tree.setInvokesStopCellEditing(true); // this helps stop editing within focus of tree
		
		final JTextField textField = new JTextField();
		textField.setEditable(true);
		makeSmall(textField);
		
		TreeCellEditor txtEditor = new DefaultCellEditor(textField);
		TreeCellEditor editor = new AnnotationTreeCellEditor(tree, annotationCellRenderer, txtEditor);
		tree.setCellEditor(editor);
		
		tree.setShowsRootHandles(true);
		tree.expandRow(0);
		tree.setRootVisible(false);
		
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				int row = tree.getRowForLocation(evt.getPoint().x, evt.getPoint().y);
				
				if (row < 0) {
					stopCellEditing(tree);
					tree.clearSelection();
				}
			}
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					TreePath selectionPath = tree.getSelectionPath();

					if (selectionPath != null)
						tree.startEditingAtPath(selectionPath);
				}
			}
		});
		
		return tree;
	}
	
	private JScrollPane createTreeScrollPane(JTree tree) {
		JScrollPane sp = new JScrollPane() {
			@Override
			public Color getBackground() {
				// This guarantees the color will come from the correct Look-And-Feel,
				// even if this component is initialized before swing-application-impl
				return UIManager.getColor("Panel.background");
			}
		};
		sp.setViewportView(tree);
		sp.getViewport().setOpaque(false);
		sp.getViewport().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				stopCellEditing(tree);
				tree.clearSelection();
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
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 16f);
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
	
	private Icon getClosedAnnotationIcon() {
		if (closedAnnotationIcon == null) {
			// Lazily initialize the icon here, because the LAF might not have been set yet,
			// and we need to get the correct colors
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(16f);
			closedAnnotationIcon = new TextIcon(
					IconManager.ICON_FOLDER,
					font,
					UIManager.getColor("Label.foreground"),
					AbstractDingAnnotationFactory.ICON_SIZE,
					AbstractDingAnnotationFactory.ICON_SIZE
			);
		}
		
		return closedAnnotationIcon;
	}
	
	private Icon getOpenAnnotationIcon() {
		if (openAnnotationIcon == null) {
			// Lazily initialize the icon here, because the LAF might not have been set yet,
			// and we need to get the correct colors
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(16f);
			openAnnotationIcon = new TextIcon(
					IconManager.ICON_FOLDER_OPEN,
					font,
					UIManager.getColor("Label.foreground"),
					AbstractDingAnnotationFactory.ICON_SIZE,
					AbstractDingAnnotationFactory.ICON_SIZE
			);
		}
		
		return openAnnotationIcon;
	}
	
	private Icon getAnnotationIcon(Annotation a) {
		Icon icon = null;
		
		if (a instanceof DingAnnotation)
			icon = iconMap.get(((DingAnnotation) a).getType());

		return icon != null ? icon : getDefIcon();
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
	
	class AnnotationTreeModel extends DefaultTreeModel {
		
		private final Map<Annotation, AnnotationNode> all;
		
		public AnnotationTreeModel(String name) {
			super(new DefaultMutableTreeNode(name.toUpperCase()));
			all = new TreeMap<>((a1, a2) -> {
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
		
		public AnnotationTreeModel(String name, Collection<Annotation> data) {
			this(name);
			
			if (data != null)
				data.forEach(a -> all.put(a, new AnnotationNode(a)));
			
			updateRoot();
		}
		
		public List<Annotation> getData() {
			return new ArrayList<>(all.keySet());
		}
		
		public int rowOf(Annotation a) {
			AnnotationNode node = a != null ? all.get(a) : null;
			
			return node != null ? getRootNode().getIndex(node) : -1;
		}

		public void addRows(Collection<Annotation> list) {
			final Set<Annotation> set = new HashSet<>(all.keySet()); // Avoiding duplicates
			
			if (set.addAll(list)) {
				all.clear();
				set.forEach(a -> all.put(a, new AnnotationNode(a)));
				updateRoot();
				fireTreeNodesChanged(this, null, new int[0], new Object[0]);
			}
		}
		
		public void removeRows(Collection<Annotation> list) {
			if (list == null)
				return;
			
			boolean changed = false;
			
			for (Annotation a : list) {
				AnnotationNode n = all.remove(a);
				
				if (n != null)
					changed = true;
			}
			
			if (changed) {
				updateRoot();
				fireTreeNodesChanged(this, null, new int[0], new Object[0]);
			}
		}
		
		public int getNodeCount() {
			return getNodeCount(getRoot());  
		}

		public int getNodeCount(Object node) {
			int count = 1;
			int childCount = getChildCount(node);

			for (int i = 0; i < childCount; i++)
				count += getChildCount(getChild(node, i));

			return count;
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof AnnotationNode && !((AnnotationNode) parent).isLeaf() &&
					index >= 0 && index < ((AnnotationNode) parent).getChildCount())
				return ((AnnotationNode) parent).getChildAt(index);
			
			DefaultMutableTreeNode rootNode = getRootNode();
			
			return rootNode.equals(parent) && index >= 0 && index < rootNode.getChildCount() ?
					rootNode.getChildAt(index) : null;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent instanceof AnnotationNode)
				return ((AnnotationNode) parent).getChildCount();
			
			DefaultMutableTreeNode rootNode = getRootNode();
			
			return rootNode.equals(parent) ? rootNode.getChildCount() : 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent instanceof AnnotationNode) {
				Annotation annotation = ((AnnotationNode) parent).getUserObject();
				
				if (annotation instanceof GroupAnnotation)
					return ((GroupAnnotation) annotation).getMembers().indexOf(child);
			}
			
			DefaultMutableTreeNode rootNode = getRootNode();
			
			return rootNode.equals(parent) && child instanceof AnnotationNode ?
					rootNode.getIndex((AnnotationNode) child) : -1;
		}
		
		@Override
	    public boolean isLeaf(Object node) {
			return node instanceof TreeNode ? ((TreeNode) node).isLeaf() : true;
	    }
		
		private DefaultMutableTreeNode getRootNode() {
			return (DefaultMutableTreeNode) getRoot();
		}
		
		private void updateRoot() {
			getRootNode().removeAllChildren();
			all.forEach((a, n) -> addNode(a, n));
		}

		private void addNode(Annotation a, AnnotationNode n) {
			if (a instanceof DingAnnotation && ((DingAnnotation) a).getGroupParent() != null) {
				GroupAnnotation ga = ((DingAnnotation) a).getGroupParent();
				AnnotationNode pn = all.get(ga);
				
				if (pn != null && pn.getIndex(n) < 0)
					pn.add(n);
			} else {
				if (n != null && getRootNode().getIndex(n) < 0)
					getRootNode().add(n);
			}
		}
	}
	
	private final class AnnotationTreeCellRenderer extends DefaultTreeCellRenderer {
        
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder()); // Do not highlight the focused cell
			setHorizontalAlignment(LEFT);
			
			if (value instanceof AnnotationNode) {
				Annotation annotation = ((AnnotationNode) value).getUserObject();
				setText(annotation.getName());
				setToolTipText(annotation.getName());
				setIconTextGap(8);
				
				if (annotation instanceof GroupAnnotation) {
					setOpenIcon(getOpenAnnotationIcon());
					setClosedIcon(getClosedAnnotationIcon());
				} else {
					Icon icon = getAnnotationIcon(annotation);
					setLeafIcon(icon);
					setIcon(icon);
				}
			} else if (value instanceof DefaultMutableTreeNode) {
				setText(((DefaultMutableTreeNode) value).getUserObject() + " Layer");
			}
			
			if (selected) {
				setForeground(UIManager.getColor("Table.selectionForeground"));
				setBackground(UIManager.getColor("Table.selectionBackground"));
			} else {
				setBackground(UIManager.getColor("Panel.background"));
				setForeground(UIManager.getColor("Table.foreground"));
			}
			
			return this;
		}
	}
	
	class AnnotationTreeCellEditor extends DefaultTreeCellEditor {

		public AnnotationTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer, TreeCellEditor editor) {
			super(tree, renderer, editor);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row) {
			if (value instanceof AnnotationNode)
				value = ((AnnotationNode) value).getUserObject().getName();
			
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		}

		@Override
		public boolean isCellEditable(EventObject e) {
			return super.isCellEditable(e) && lastPath.getLastPathComponent() instanceof AnnotationNode;
		}
	}

	class AnnotationNode extends DefaultMutableTreeNode {

		AnnotationNode(Annotation annotation) {
	        super(annotation);
	    }

		@Override
		public Annotation getUserObject() {
			return (Annotation) super.getUserObject();
		}
		
		@Override
		public void setUserObject(Object obj) {
			if (obj instanceof String)
				getUserObject().setName((String) obj);
		}
	}
}
