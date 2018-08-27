package org.cytoscape.ding.impl.cyannotator.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.util.swing.LookAndFeelUtil.createPanelBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
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
import org.cytoscape.util.swing.LookAndFeelUtil;
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
	
	private static final Color TREE_BG_COLOR = UIManager.getColor("Table.background");
	private static final Color TREE_FG_COLOR = UIManager.getColor("Table.foreground");
	private static final Color TREE_SEL_BG_COLOR = UIManager.getColor("Table.selectionBackground");
	private static final Color TREE_SEL_FG_COLOR = UIManager.getColor("Table.selectionForeground");
	
	private JPanel buttonPanel;
	private JLabel infoLabel;
	private JLabel selectionLabel;
	private JButton groupAnnotationsButton;
	private JButton ungroupAnnotationsButton;
	private JButton removeAnnotationsButton;
	private JButton pullToForegroundButton;
	private JButton pushToBackgroundButton;
	private LayerPanel foregroundLayerPanel;
	private LayerPanel backgroundLayerPanel;
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
		updateMoveToCanvasButtons();
		
		getBackgroundLayerPanel().setEnabled(enabled);
		getForegroundLayerPanel().setEnabled(enabled);
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
		buttonGroup.clearSelection();
	}
	
	void setSelected(Annotation a, boolean selected) {
		if (view == null || getAnnotationCount() == 0)
			return;
		
		final JTree tree = Annotation.FOREGROUND.equals(a.getCanvasName()) ?
				getForegroundTree() : getBackgroundTree();
		final AnnotationTreeModel model = (AnnotationTreeModel) tree.getModel();
		
		TreePath path = model.pathTo(a);
		
		if (path == null)
			return;

		if (selected) {
			tree.addSelectionPath(path);
			tree.scrollPathToVisible(path);
		} else {
			tree.removeSelectionPath(path);
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
		getBackgroundLayerPanel().update(map.get(Annotation.BACKGROUND));
		getForegroundLayerPanel().update(map.get(Annotation.FOREGROUND));
		
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
		updateMoveToCanvasButtons();
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
	
	void updateMoveToCanvasButtons() {
		getPushToBackgroundButton().setEnabled(
				getSelectedAnnotationCount(getForegroundTree(), DingAnnotation.class) > 0);
		getPullToForegroundButton().setEnabled(
				getSelectedAnnotationCount(getBackgroundTree(), DingAnnotation.class) > 0);
	}
	
	void updateAnnotationsOrder(ReorderType type) {
		Collection<Annotation> selectedAnnotations = getSelectedAnnotations();
		
		// Update all annotation trees, because an annotation may have been moved to another layer
		final List<Annotation> annotations = view != null ? view.getCyAnnotator().getAnnotations()
				: Collections.emptyList();
		{
			Map<String, Collection<Annotation>> map = separateByLayers(annotations);
			getBackgroundLayerPanel().update(map.get(Annotation.BACKGROUND));
			getForegroundLayerPanel().update(map.get(Annotation.FOREGROUND));
		}
		// Restore the row selection (the annotations that were selected before must be still selected)
		getBackgroundTree().clearSelection();
		getForegroundTree().clearSelection();
		Map<String, Collection<Annotation>> map = separateByLayers(selectedAnnotations);
		map.get(Annotation.BACKGROUND).forEach(a -> setSelected(a, true));
		map.get(Annotation.FOREGROUND).forEach(a -> setSelected(a, true));
		getBackgroundLayerPanel().updateButtons();
		getForegroundLayerPanel().updateButtons();
	}
	
	void stopTreeCellEditing() {
		stopCellEditing(getBackgroundTree());
		stopCellEditing(getForegroundTree());
	}
	
	void stopCellEditing(JTree tree) {
		if (tree.isEditing())
			tree.stopEditing();
	}
	
	private static Map<String, Collection<Annotation>> separateByLayers(Collection<Annotation> list) {
		Map<String, Collection<Annotation>> map = new HashMap<>();
		map.put(Annotation.BACKGROUND, new HashSet<>());
		map.put(Annotation.FOREGROUND, new HashSet<>());
		
		if (list != null) {
			list.forEach(a -> {
				if (a instanceof GroupAnnotation == false) {
					// Don't add GroupAnnotations now, because their actual canvas doesn't matter,
					// as a group can contain annotations from both layers
					Collection<Annotation> set = map.get(a.getCanvasName());
					
					if (set != null) // Should never be null, unless a new canvas name is created!
						set.add(a);
				}
			});
		}
		
		return map;
	}
	
	private void init() {
		if (isAquaLAF())
			setOpaque(false);
		
		equalizeSize(getGroupAnnotationsButton(), getUngroupAnnotationsButton(), getRemoveAnnotationsButton());
		equalizeSize(getSelectAllButton(), getSelectNoneButton());
		
		// I don't know of a better way to center the selection label perfectly
		// other than by using this "filler" panel hack...
		JPanel rightFiller = new JPanel();
		rightFiller.setPreferredSize(getRemoveAnnotationsButton().getPreferredSize());
		
		if (isAquaLAF())
			rightFiller.setOpaque(false);
		
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
				.addComponent(getForegroundLayerPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(getPushToBackgroundButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(getPullToForegroundButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(0, 0, Short.MAX_VALUE)
				)
				.addComponent(getBackgroundLayerPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
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
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getForegroundLayerPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(getPushToBackgroundButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getPullToForegroundButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getBackgroundLayerPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(CENTER, false)
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
			
			if (isAquaLAF())
				buttonPanel.setOpaque(false);
			
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
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(groupAnnotationsButton, iconManager.getIconFont(16f));
		}
		
		return groupAnnotationsButton;
	}
	
	JButton getUngroupAnnotationsButton() {
		if (ungroupAnnotationsButton == null) {
			ungroupAnnotationsButton = new JButton(IconManager.ICON_OBJECT_UNGROUP);
			ungroupAnnotationsButton.setToolTipText("Ungroup Selected Annotations");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(ungroupAnnotationsButton, iconManager.getIconFont(16f));
		}
		
		return ungroupAnnotationsButton;
	}
	
	JButton getRemoveAnnotationsButton() {
		if (removeAnnotationsButton == null) {
			removeAnnotationsButton = new JButton(IconManager.ICON_TRASH_O);
			removeAnnotationsButton.setToolTipText("Remove Selected Annotations");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(removeAnnotationsButton, iconManager.getIconFont(18f));
		}
		
		return removeAnnotationsButton;
	}
	
	JButton getPushToBackgroundButton() {
		if (pushToBackgroundButton == null) {
			pushToBackgroundButton = new JButton(IconManager.ICON_ARROW_DOWN);
			pushToBackgroundButton.setToolTipText("Push Annotations to Background Layer");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(pushToBackgroundButton, iconManager.getIconFont(12f));
		}
		
		return pushToBackgroundButton;
	}
	
	JButton getPullToForegroundButton() {
		if (pullToForegroundButton == null) {
			pullToForegroundButton = new JButton(IconManager.ICON_ARROW_UP);
			pullToForegroundButton.setToolTipText("Pull Annotations to Foreground Layer");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			styleToolBarButton(pullToForegroundButton, iconManager.getIconFont(12f));
		}
		
		return pullToForegroundButton;
	}
	
	LayerPanel getForegroundLayerPanel() {
		if (foregroundLayerPanel == null) {
			foregroundLayerPanel = new LayerPanel(Annotation.FOREGROUND);
		}
		
		return foregroundLayerPanel;
	}
	
	LayerPanel getBackgroundLayerPanel() {
		if (backgroundLayerPanel == null) {
			backgroundLayerPanel = new LayerPanel(Annotation.BACKGROUND);
		}
		
		return backgroundLayerPanel;
	}
	
	JTree getForegroundTree() {
		return getForegroundLayerPanel().getTree();
	}
	
	JTree getBackgroundTree() {
		return getBackgroundLayerPanel().getTree();
	}
	
	JTree getLayerTree(String canvasName) {
		return Annotation.BACKGROUND.equals(canvasName) ? getBackgroundTree() : getForegroundTree();
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
	
	//------[ CLASSES ]--------------------------------------------------------------------------------------
	
	class LayerPanel extends JPanel {
		
		private JLabel titleLabel;
		private JButton forwardButton;
		private JButton backwardButton;
		private JScrollPane scrollPane;
		private JTree tree;
		
		private final String canvasName;
		
		public LayerPanel(String canvasName) {
			this.canvasName = canvasName;
			init();
			setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
			
			getTree().getSelectionModel().addTreeSelectionListener(e -> {
				stopTreeCellEditing();
				updateSelectionLabel();
				updateGroupUngroupButton();
				updateRemoveAnnotationsButton();
				updateMoveToCanvasButtons();
				updateButtons();
			});
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			getTree().setEnabled(enabled);
			getTitleLabel().setEnabled(enabled);
			updateButtons();
		}

		JButton getForwardButton() {
			if (forwardButton == null) {
				forwardButton = new JButton(IconManager.ICON_CARET_UP);
				forwardButton.setToolTipText("Bring Annotations Forward");
				
				final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
				styleToolBarButton(forwardButton, iconManager.getIconFont(17f));
			}
			
			return forwardButton;
		}
		
		JButton getBackwardButton() {
			if (backwardButton == null) {
				backwardButton = new JButton(IconManager.ICON_CARET_DOWN);
				backwardButton.setToolTipText("Send Annotations Backward");
				
				final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
				styleToolBarButton(backwardButton, iconManager.getIconFont(17f));
			}
			
			return backwardButton;
		}
		
		JScrollPane getScrollPane() {
			if (scrollPane == null) {
				scrollPane = new JScrollPane(getTree());
				scrollPane.setViewportView(getTree());
				scrollPane.getViewport().setOpaque(false);
				scrollPane.getViewport().addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent evt) {
						stopCellEditing(getTree());
						getTree().clearSelection();
						scrollPane.requestFocusInWindow();
					}
				});
				scrollPane.setBackground(TREE_BG_COLOR);
				scrollPane.setBorder(
						BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			}
			
			return scrollPane;
		}
		
		JTree getTree() {
			if (tree == null) {
				final AnnotationTreeCellRenderer annotationCellRenderer = new AnnotationTreeCellRenderer();
				
				tree = new JTree(new AnnotationTreeModel(canvasName)) {
					@Override
					public TreeCellRenderer getCellRenderer() {
						return annotationCellRenderer;
					}
					@Override
					public Color getBackground() {
						// This guarantees the color will come from the correct Look-And-Feel,
						// even if this component is initialized before swing-application-impl
						return TREE_BG_COLOR;
					}
					@Override
					public void paintComponent(Graphics g) {
						// Highlight entire JTree row on selection...
						g.setColor(getBackground());
						g.fillRect(0, 0, getWidth(), getHeight());
						int[] rows = getSelectionRows();
						
						if (rows != null) {
							for (int i : rows) {
								Rectangle r = getRowBounds(i);
								g.setColor(TREE_SEL_BG_COLOR);
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
				tree.setName(canvasName);
				tree.setOpaque(false);
				tree.setRowHeight(24);
				makeSmall(tree);
				
				tree.setEditable(true);
				tree.setToggleClickCount(0);
				// Start editing with space key
				tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "startEditing");
				tree.setInvokesStopCellEditing(true); // this helps stop editing within focus of tree
				
				final JTextField textField = new JTextField();
				textField.setEditable(true);
				makeSmall(textField);
				
				TreeCellEditor txtEditor = new DefaultCellEditor(textField);
				TreeCellEditor editor = new AnnotationTreeCellEditor(tree, annotationCellRenderer, txtEditor);
				editor.addCellEditorListener(new CellEditorListener() {
					@Override
					public void editingStopped(ChangeEvent evt) {
						// Repaint both trees when a node's name has changed,
						// because a GroupAnnotation can have a corresponding node in each one
						getBackgroundTree().repaint();
						getForegroundTree().repaint();
					}
					@Override
					public void editingCanceled(ChangeEvent evt) {
						// Ignore...
					}
				});
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
			}
			
			return tree;
		}
		
		private void init() {
			if (isAquaLAF())
				setOpaque(false);
			
			final GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getTitleLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(10, 10, Short.MAX_VALUE)
							.addComponent(getForwardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getBackwardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(getTitleLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getForwardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getBackwardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		private JLabel getTitleLabel() {
			if (titleLabel == null) {
				String text = canvasName.toLowerCase() + " Layer";
				text = text.substring(0, 1).toUpperCase() + text.substring(1); // capitalize the first letter
				
				titleLabel = new JLabel(text);
				titleLabel.setVerticalAlignment(SwingConstants.BOTTOM);
				titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 2, 12));
				
				Border tb = LookAndFeelUtil.createTitledBorder(text);
				
				if (tb instanceof TitledBorder)
					titleLabel.setFont(((TitledBorder) tb).getTitleFont());
				else
					makeSmall(titleLabel);
			}
			
			return titleLabel;
		}
		
		private void update(Collection<Annotation> collection) {
			getTree().setModel(new AnnotationTreeModel(canvasName, collection));
			updateButtons();
		}
		
		private void updateButtons() {
			final int selectionCount = getTree().getSelectionCount();
			boolean b = isEnabled() && selectionCount > 0;
			
			if (!b) {
				getForwardButton().setEnabled(false);
				getBackwardButton().setEnabled(false);
				
				return;
			}
			
			final int[] selRows = getTree().getSelectionRows();
			int size = getTree().getRowCount();
			int firstIdx = -1;
			int lastIdx = size;
			
			if (selRows == null || selRows.length == 0)
				b = false;
			
			if (b) {
				final Set<TreeNode> parents = new HashSet<>();
				
				for (int i = 0; i < selRows.length; i++) {
					TreePath path = tree.getPathForRow(selRows[i]);
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					parents.add(node.getParent());
					
					// Cannot reorder when nodes from different parents are selected
					if (parents.size() > 1)
						break;
				}
				
				if (b) {
					DefaultMutableTreeNode pn = (DefaultMutableTreeNode) parents.iterator().next();
					size = pn.getChildCount();
					
					if (selectionCount == size) {
						// If, all nodes selected, it does not make sense to reorder all of them at once
						b = false;
					} else {
						TreeSet<Integer> selIdxSet = new TreeSet<>(); // So we can sort the selected indexes
						selIdxSet.addAll(Arrays.stream(selRows).boxed().collect(Collectors.toList()));
						
						TreePath path1 = tree.getPathForRow(selIdxSet.first());
						DefaultMutableTreeNode n1 = (DefaultMutableTreeNode) path1.getLastPathComponent();
						firstIdx = pn.getIndex(n1);
						
						TreePath path2 = tree.getPathForRow(selIdxSet.last());
						DefaultMutableTreeNode n2 = (DefaultMutableTreeNode) path2.getLastPathComponent();
						lastIdx = pn.getIndex(n2);
					}
				}
			}
			
			getForwardButton().setEnabled(b && firstIdx > 0);
			getBackwardButton().setEnabled(b && lastIdx < size - 1);
		}
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
		
		private final Map<Annotation, AnnotationNode> all = new LinkedHashMap<>();
		
		public AnnotationTreeModel(String name) {
			super(new DefaultMutableTreeNode(name.toUpperCase()));
		}
		
		public AnnotationTreeModel(String name, Collection<Annotation> data) {
			this(name);
			
			if (data != null) {
				TreeSet<Annotation> set = new TreeSet<>((a1, a2) -> {
					if (a1 instanceof DingAnnotation && a2 instanceof DingAnnotation) {
						JComponent canvas1 = ((DingAnnotation) a1).getCanvas();
						JComponent canvas2 = ((DingAnnotation) a2).getCanvas();
						int z1 = canvas1.getComponentZOrder(((DingAnnotation) a1).getComponent());
						int z2 = canvas2.getComponentZOrder(((DingAnnotation) a2).getComponent());
						
						return Integer.compare(z1, z2);
					}
					
					return 0;
				});
				set.addAll(data);
				set.forEach(a -> addNode(a));
			}
		}
		
		public List<Annotation> getData() {
			return new ArrayList<>(all.keySet());
		}
		
		public int rowOf(Annotation a) {
			AnnotationNode node = a != null ? all.get(a) : null;
			
			return node != null ? getRootNode().getIndex(node) : -1;
		}
		
		public TreePath pathTo(Annotation a) {
			AnnotationNode node = a != null ? all.get(a) : null;
			return node != null ? new TreePath(node.getPath()) : null;
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
		
		private void addNode(Annotation a) {
			AnnotationNode n = all.get(a);
			
			if (n == null)
				all.put(a, n = new AnnotationNode(a));
			
			if (a instanceof DingAnnotation && ((DingAnnotation) a).getGroupParent() != null) {
				GroupAnnotation ga = ((DingAnnotation) a).getGroupParent();
				AnnotationNode pn = all.get(ga);
				
				if (pn == null) {
					// Now we can create the Nodes for each GroupAnnotation we find,
					// because a group node can be added to both background and foreground trees,
					// since it may contain child annotations from different canvases
					all.put(ga, pn = new AnnotationNode(ga));
					addNode(ga);
				}
				
				if (pn.getIndex(n) < 0)
					pn.add(n);
			} else if (getRootNode().getIndex(n) < 0) {
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
				setForeground(TREE_SEL_FG_COLOR);
				setBackground(TREE_SEL_BG_COLOR);
			} else {
				setBackground(TREE_BG_COLOR);
				setForeground(TREE_FG_COLOR);
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
			return super.isCellEditable(e) && lastPath != null
					&& lastPath.getLastPathComponent() instanceof AnnotationNode;
		}

		@Override
		protected Container createContainer() {
	        return new EditorContainer() {
	        		@Override
	        		public void paint(Graphics g) {
	        			// There's a weird race condition that prevents the cell editor from rendering the
	        			// actual leaf icon for the editing row when double-clicking to edit the annotation name
	        			// (no problem when pressing SPACE), so let's force it to get the icon again
	        			// right before painting it.
	        			if (lastPath != null) {
	        				Object obj = lastPath.getLastPathComponent();
	        				
	        				if (obj instanceof AnnotationNode) {
	        					if (((AnnotationNode) obj).getUserObject() instanceof GroupAnnotation == false)
	        						editingIcon = getAnnotationIcon(((AnnotationNode) obj).getUserObject());
	        				}
	        			}
					
					super.paint(g);
	        		}
	        };
	    }
	}

	public static class AnnotationNode extends DefaultMutableTreeNode {

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
		
		@Override
		public boolean getAllowsChildren() {
			return getUserObject() instanceof GroupAnnotation;
		}
	}
}
