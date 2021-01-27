package org.cytoscape.cg.internal.editor;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.cg.model.CyCustomGraphics2Manager;
import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.cg.util.CustomGraphicsBrowser;
import org.cytoscape.cg.util.ImageCustomGraphicsSelector;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CyCustomGraphicsValueEditor implements VisualPropertyValueEditor<CyCustomGraphics> {

	private boolean editCancelled;
	
	private JTabbedPane groupTpn;
	private JPanel bottomPnl;
	private ImageCustomGraphicsSelector imageSelector;
	private Map<String/*group*/, CustomGraphics2Panel> cg2PnlMap;
	private JButton removeBtn;
	private JButton cancelBtn;
	private JButton applyBtn;
	
	private CyCustomGraphics<?> oldCustomGraphics;
	private CyCustomGraphics<?> newCustomGraphics;
	
	private boolean initialized;

	private final CustomGraphicsBrowser browser;
	private final CyServiceRegistrar serviceRegistrar;
	
	private JDialog dialog;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public CyCustomGraphicsValueEditor(CustomGraphicsBrowser browser, CyServiceRegistrar serviceRegistrar) {
		this.browser = browser;
		this.serviceRegistrar = serviceRegistrar;
		cg2PnlMap = new HashMap<>();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public <S extends CyCustomGraphics> CyCustomGraphics<? extends CustomGraphicLayer> showEditor(
			Component parent, S initialValue, VisualProperty<S> vp) {
		oldCustomGraphics = initialValue;
		
		// Make sure it initializes only after the Cytoscape UI (specially DefaultViewPanel) is ready
		if (!initialized) {
			init(parent);
			initialized = true;
		}
		
		update();
		
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		
		return editCancelled ? oldCustomGraphics : newCustomGraphics;
	}

	@Override
	public Class<CyCustomGraphics> getValueType() {
		return CyCustomGraphics.class;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init(Component parent) {
		var owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
		dialog = new JDialog(owner, ModalityType.APPLICATION_MODAL);
		dialog.setMinimumSize(new Dimension(400, 600));
		dialog.setTitle("Graphics");
		dialog.setResizable(false);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		
		var layout = new GroupLayout(dialog.getContentPane());
		dialog.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getGroupTpn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBottomPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getGroupTpn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBottomPnl())
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), getApplyBtn().getAction(),
				getCancelBtn().getAction());
		dialog.getRootPane().setDefaultButton(getApplyBtn());
	}
	
	private void update() {
		getImageSelector().update(oldCustomGraphics);
		Component newSelectedComp = getImageSelector();
		var iter = cg2PnlMap.entrySet().iterator();
		
		while (iter.hasNext()) {
			CustomGraphics2Panel cg2Pnl = iter.next().getValue();
			cg2Pnl.update(oldCustomGraphics instanceof CyCustomGraphics2 ?
					(CyCustomGraphics2) oldCustomGraphics : null);
			
			if (cg2Pnl.getEditorCount() == 0) {
				iter.remove();
				
				if (getGroupTpn().indexOfComponent(cg2Pnl) >= 0)
					getGroupTpn().remove(cg2Pnl);
				
				continue;
			}
			
			if (oldCustomGraphics instanceof CyCustomGraphics2 && cg2Pnl.canEdit((CyCustomGraphics2) oldCustomGraphics))
				newSelectedComp = cg2Pnl;
		}
		
		getGroupTpn().setSelectedComponent(newSelectedComp);
	}
	
	private void remove() {
		editCancelled = false;
		newCustomGraphics = NullCustomGraphics.getNullObject();
		dialog.dispose();
	}
	
	private void cancel() {
		editCancelled = true;
		dialog.dispose();
	}
	
	private void apply() {
		editCancelled = false;
		var c = getGroupTpn().getSelectedComponent();
		
		if (c instanceof ImageCustomGraphicsSelector)
			newCustomGraphics = ((ImageCustomGraphicsSelector) c).getSelectedValue();
		else if (c instanceof CustomGraphics2Panel)
			newCustomGraphics = ((CustomGraphics2Panel) c).getCustomGraphics2();

		dialog.dispose();
	}
	
	private JTabbedPane getGroupTpn() {
		if (groupTpn == null) {
			groupTpn = new JTabbedPane();
			groupTpn.addTab("Images", getImageSelector());
			
			var customGraphics2Mgr = serviceRegistrar.getService(CyCustomGraphics2Manager.class);
			
			for (var group : customGraphics2Mgr.getGroups()) {
				var cg2Pnl = getCG2Pnl(group);
				
				if (cg2Pnl != null)
					groupTpn.addTab(group, cg2Pnl);
			}
		}
		
		return groupTpn;
	}
	
	private JPanel getBottomPnl() {
		if (bottomPnl == null) {
			bottomPnl = LookAndFeelUtil.createOkCancelPanel(getApplyBtn(), getCancelBtn(), getRemoveBtn());
		}
		
		return bottomPnl;
	}
	
	private ImageCustomGraphicsSelector getImageSelector() {
		if (imageSelector == null) {
			imageSelector = new ImageCustomGraphicsSelector(browser, serviceRegistrar);
			imageSelector.addActionListener(evt -> apply());
		}
		
		return imageSelector;
	}
	
	private CustomGraphics2Panel getCG2Pnl(String group) {
		var cg2Pnl = cg2PnlMap.get(group);
		
		if (cg2Pnl == null) {
			cg2Pnl = new CustomGraphics2Panel(group);
			cg2Pnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			cg2PnlMap.put(group, cg2Pnl);
		}
		
		return cg2Pnl;
	}
	
	public JButton getRemoveBtn() {
		if (removeBtn == null) {
			removeBtn = new JButton("Remove Graphics");
			removeBtn.addActionListener(evt -> remove());
		}
		
		return removeBtn;
	}
	
	@SuppressWarnings("serial")
	private JButton getCancelBtn() {
		if (cancelBtn == null) {
			cancelBtn = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancel();
				}
			});
		}
		
		return cancelBtn;
	}
	
	@SuppressWarnings("serial")
	private JButton getApplyBtn() {
		if (applyBtn == null) {
			applyBtn = new JButton(new AbstractAction("Apply") {
				@Override
				public void actionPerformed(ActionEvent e) {
					apply();
				}
			});
		}
		
		return applyBtn;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	/**
	 * Panel that contains all CyCustomGraphics2 editors that belong to the same group
	 */
	@SuppressWarnings("serial")
	private class CustomGraphics2Panel extends JPanel {
		
		static final int ICON_SIZE = 18;
		
		private final String group;
		private Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> supportedFactories = Collections.emptyList();
		private CyCustomGraphics2 cg2;
		private boolean updatingTypes;
		
		private JTabbedPane typeTpn;

		public CustomGraphics2Panel(String group) {
			this.group = group;
			this.setLayout(new BorderLayout());
			this.add(getTypeTpn(), BorderLayout.CENTER);
		}
		
		String getGroup() {
			return group;
		}
		
		boolean canEdit(CyCustomGraphics2 cg2) {
			for (var cf : supportedFactories) {
				if (cf.getSupportedClass().isAssignableFrom(cg2.getClass()))
					return true;
			}
			
			return false;
		}
		
		CyCustomGraphics2 getCustomGraphics2() {
			return cg2;
		}
		
		int getEditorCount() {
			return getTypeTpn().getTabCount();
		}
		
		private void update(CyCustomGraphics2 cg2) {
			updatingTypes = true;
			
			var customGraphics2Mgr = serviceRegistrar.getService(CyCustomGraphics2Manager.class);
			supportedFactories = customGraphics2Mgr.getCyCustomGraphics2Factories(group);
			
			CustomGraphics2EditorPane selectedEditorPn = null;
			int maxWidth = 100;
			
			try {
				getTypeTpn().removeAll();
				CyCustomGraphics2 initialCg2 = null;
				
				for (var cf : supportedFactories) {
					var cg2EditorPn = new CustomGraphics2EditorPane(cf);
					var icon = cf.getIcon(ICON_SIZE, ICON_SIZE);
					
					if (cg2 != null) {
						initialCg2 = cf.getInstance(cg2.getProperties());
						
						if (cf.getSupportedClass().isAssignableFrom(cg2.getClass())) {
							selectedEditorPn = cg2EditorPn;
							this.cg2 = initialCg2;
						}
					} else {
						initialCg2 = cf.getInstance(new HashMap<String, Object>());
						this.cg2 = null;
					}
					
					cg2EditorPn.update(initialCg2);
					
					if (cg2EditorPn.getEditor() != null) {
						getTypeTpn().addTab(
								icon == null ? cf.getDisplayName() : "", 
								icon, 
								cg2EditorPn,
								cf.getDisplayName());
						
						maxWidth = Math.max(maxWidth, cg2EditorPn.getPreferredSize().width);
					}
				}
				
				if (selectedEditorPn != null)
					getTypeTpn().setSelectedComponent(selectedEditorPn);
				else
					getTypeTpn().setSelectedIndex(0);
			} finally {
				updatingTypes = false;
			}
			
			if (this.cg2 == null) {
				selectedEditorPn = (CustomGraphics2EditorPane) getTypeTpn().getSelectedComponent();
				
				if (selectedEditorPn != null)
					this.cg2 = selectedEditorPn.getCg2();
			}
			
			getTypeTpn().setPreferredSize(new Dimension(maxWidth + 40, 520));
		}
		
		private JTabbedPane getTypeTpn() {
			if (typeTpn == null) {
				typeTpn = new JTabbedPane();
				typeTpn.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (updatingTypes) return;
						
						var c = typeTpn.getSelectedComponent();
						
						if (c instanceof CustomGraphics2EditorPane) {
							var cf = ((CustomGraphics2EditorPane)c).getFactory();
							
							if (cg2 == null || !cf.getSupportedClass().isAssignableFrom(cg2.getClass()))
								cg2 = cf.getInstance(
										cg2 != null ? cg2.getProperties() : new HashMap<String, Object>());
								
							((CustomGraphics2EditorPane)c).update(cg2);
						}
					}
				});
			}
			
			return typeTpn;
		}
		
		@Override
		public String toString() {
			return "CG2 Panel for " + group;
		}
		
		private class CustomGraphics2EditorPane extends JScrollPane {
			
			private final CyCustomGraphics2Factory<?> factory;
			private JComponent editor;
			private CyCustomGraphics2 cg2;

			CustomGraphics2EditorPane(CyCustomGraphics2Factory<?> factory) {
				this.factory = factory;
				this.setBorder(BorderFactory.createEmptyBorder());
				this.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
				this.getViewport().setOpaque(!LookAndFeelUtil.isAquaLAF());
			}
			
			void update(CyCustomGraphics2 initialCg2) {
				this.cg2 = initialCg2;
				editor = factory.createEditor(initialCg2);
				this.setViewportView(editor);
				this.updateUI();
			}
			
			CyCustomGraphics2 getCg2() {
				return cg2;
			}
			
			CyCustomGraphics2Factory<?> getFactory() {
				return factory;
			}
			
			JComponent getEditor() {
				return editor;
			}
			
			@Override
			public String toString() {
				return "CG2EditorPane for " + factory.getDisplayName();
			}
		}
	}
}
