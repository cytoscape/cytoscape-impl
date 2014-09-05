package org.cytoscape.ding.impl.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.CyCustomGraphics2Manager;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CyCustomGraphicsValueEditor extends JPanel implements ValueEditor<CyCustomGraphics> {

	private static final long serialVersionUID = 3276556808025021859L;

	private boolean editCancelled;
	
	private JTabbedPane groupTpn;
	private JPanel bottomPnl;
	private GraphicsPanel graphicsPnl;
	private Map<String/*group*/, CustomGraphics2Panel> cg2PnlMap;
	private JButton removeBtn;
	private JButton cancelBtn;
	private JButton applyBtn;
	
	private CyCustomGraphics<?> oldCustomGraphics;
	private CyCustomGraphics<?> newCustomGraphics;
	
	private boolean initialized;

	private final CustomGraphicsManager customGraphicsMgr;
	private final CyCustomGraphics2Manager customGraphics2Mgr;
	private final CyServiceRegistrar serviceRegistrar;
	
	private JDialog dialog;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public CyCustomGraphicsValueEditor(final CustomGraphicsManager customGraphicsMgr,
									   final CyCustomGraphics2Manager customGraphics2Mgr,
									   final CyServiceRegistrar serviceRegistrar) {
		this.customGraphicsMgr = customGraphicsMgr;
		this.customGraphics2Mgr = customGraphics2Mgr;
		this.serviceRegistrar = serviceRegistrar;
		cg2PnlMap = new HashMap<>();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public <S extends CyCustomGraphics> CyCustomGraphics<? extends CustomGraphicLayer> showEditor(
			final Component parent, final S initialValue) {
		oldCustomGraphics = initialValue;
		
		// Make sure it initializes only after the Cytoscape UI (specially DefaultViewPanel) is ready
		if (!initialized) {
			init(parent);
			initialized = true;
		}
		
		refreshUI();
		
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
	
	private void init(final Component parent) {
		final Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
		dialog = new JDialog(owner, ModalityType.APPLICATION_MODAL);
		dialog.setMinimumSize(new Dimension(400, 600));
		dialog.add(this);
		dialog.setTitle("Graphics");
		dialog.setResizable(false);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		
		setLayout(new BorderLayout());
		
		add(getGroupTpn(), BorderLayout.CENTER);
		add(getBottomPnl(), BorderLayout.SOUTH);
	}
	
	private void refreshUI() {
		getGraphicsPnl().updateList();
		Component newSelected = getGraphicsPnl();
		final Iterator<Entry<String, CustomGraphics2Panel>> iter = cg2PnlMap.entrySet().iterator();
		
		while (iter.hasNext()) {
			final CustomGraphics2Panel cg2Pnl = iter.next().getValue();
			cg2Pnl.update(oldCustomGraphics instanceof CyCustomGraphics2 ? (CyCustomGraphics2)oldCustomGraphics : null);
			
			if (cg2Pnl.getEditorCount() == 0) {
				iter.remove();
				
				if (getGroupTpn().indexOfComponent(cg2Pnl) >= 0)
					getGroupTpn().remove(cg2Pnl);
				
				continue;
			}
			
			if (oldCustomGraphics instanceof CyCustomGraphics2 && cg2Pnl.canEdit((CyCustomGraphics2)oldCustomGraphics))
				newSelected = cg2Pnl;
		}
		
		getGroupTpn().setSelectedComponent(newSelected);
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
		final Component c = getGroupTpn().getSelectedComponent();
		
		if (c instanceof GraphicsPanel)
			newCustomGraphics = ((GraphicsPanel)c).getCustomGraphics();
		else if (c instanceof CustomGraphics2Panel)
			newCustomGraphics = ((CustomGraphics2Panel)c).getCustomGraphics2();
		
		dialog.dispose();
	}
	
	private JTabbedPane getGroupTpn() {
		if (groupTpn == null) {
			groupTpn = new JTabbedPane();
			groupTpn.addTab("Images", getGraphicsPnl());
			
			for (final String group : customGraphics2Mgr.getGroups()) {
				final CustomGraphics2Panel cg2Pnl = getCG2Pnl(group);
				
				if (cg2Pnl != null)
					groupTpn.addTab(group, cg2Pnl);
			}
		}
		
		return groupTpn;
	}
	
	private JPanel getBottomPnl() {
		if (bottomPnl == null) {
			bottomPnl = new JPanel();
			bottomPnl.setLayout(new BoxLayout(bottomPnl, BoxLayout.X_AXIS));
			bottomPnl.add(getRemoveBtn());
			bottomPnl.add(Box.createVerticalStrut(35));
			bottomPnl.add(Box.createHorizontalGlue());
			bottomPnl.add(getCancelBtn());
			bottomPnl.add(getApplyBtn());
		}
		
		return bottomPnl;
	}
	
	private GraphicsPanel getGraphicsPnl() {
		if (graphicsPnl == null) {
			graphicsPnl = new GraphicsPanel();
		}
		
		return graphicsPnl;
	}
	
	private CustomGraphics2Panel getCG2Pnl(final String group) {
		CustomGraphics2Panel cg2Pnl = cg2PnlMap.get(group);
		
		if (cg2Pnl == null) {
			cg2Pnl = new CustomGraphics2Panel(group);
			cg2Pnl.setOpaque(false);
			cg2PnlMap.put(group, cg2Pnl);
		}
		
		return cg2Pnl;
	}
	
	public JButton getRemoveBtn() {
		if (removeBtn == null) {
			removeBtn = new JButton("Remove Graphics");
			removeBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					remove();
				}
			});
		}
		
		return removeBtn;
	}
	
	private JButton getCancelBtn() {
		if (cancelBtn == null) {
			cancelBtn = new JButton("Cancel");
			cancelBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					cancel();
				}
			});
		}
		
		return cancelBtn;
	}
	
	private JButton getApplyBtn() {
		if (applyBtn == null) {
			applyBtn = new JButton("Apply");
			applyBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					apply();
				}
			});
		}
		
		return applyBtn;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class GraphicsPanel extends JPanel {
		
		private static final long serialVersionUID = 1157288441507073705L;
		
		private DiscreteValueList<CyCustomGraphics> graphicsList;
		
		public GraphicsPanel() {
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setViewportView(getGraphicsList());
			
			setLayout(new BorderLayout());
			add(scrollPane);
		}
		
		public void updateList() {
			final Collection<CyCustomGraphics> newValues = customGraphicsMgr.getAllCustomGraphics();
			getGraphicsList().setListItems(newValues, oldCustomGraphics);
		}
		
		public CyCustomGraphics getCustomGraphics() {
			return (CyCustomGraphics) getGraphicsList().getSelectedValue();
		}
		
		private DiscreteValueList<CyCustomGraphics> getGraphicsList() {
			if (graphicsList == null) {
				DefaultViewPanel defViewPanel = serviceRegistrar.getService(DefaultViewPanel.class);
				
				graphicsList = new DiscreteValueList<CyCustomGraphics>(CyCustomGraphics.class, null, defViewPanel);
				graphicsList.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(final MouseEvent evt) {
						if (evt.getClickCount() == 2) {
							getApplyBtn().doClick();
						}
					}
				});
			}
			
			return graphicsList;
		}
	}
	
	/**
	 * Panel that contains all CyCustomGraphics2 editors that belong to the same group
	 */
	private class CustomGraphics2Panel extends JPanel {
		
		private static final long serialVersionUID = 6669567626195325838L;

		static final int ICON_SIZE = 18;
		
		private final String group;
		private Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> supportedFactories = Collections.emptyList();
		private CyCustomGraphics2 cg2;
		private boolean updatingTypes;
		
		private JTabbedPane typeTpn;

		public CustomGraphics2Panel(final String group) {
			this.group = group;
			this.setLayout(new BorderLayout());
			this.add(getTypeTpn(), BorderLayout.CENTER);
		}
		
		String getGroup() {
			return group;
		}
		
		boolean canEdit(final CyCustomGraphics2 cg2) {
			for (final CyCustomGraphics2Factory<?> cf : supportedFactories) {
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
		
		private void update(final CyCustomGraphics2 cg2) {
			updatingTypes = true;
			
			supportedFactories = customGraphics2Mgr.getCyCustomGraphics2Factories(group);
			CustomGraphics2EditorPane selectedEditorPn = null;
			int maxWidth = 100;
			
			try {
				getTypeTpn().removeAll();
				CyCustomGraphics2 initialCg2 = null;
				
				for (final CyCustomGraphics2Factory<?> cf : supportedFactories) {
					final CustomGraphics2EditorPane cg2EditorPn = new CustomGraphics2EditorPane(cf);
					final Icon icon = cf.getIcon(ICON_SIZE, ICON_SIZE);
					
					if (cg2 != null) {
						initialCg2 = cf.getInstance(cg2.getProperties());
						
						if (cf.getSupportedClass().isAssignableFrom(cg2.getClass())) {
							selectedEditorPn = cg2EditorPn;
							this.cg2 = initialCg2;
						}
					} else {
						initialCg2 = cf.getInstance(new HashMap<String, Object>());
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
			} finally {
				updatingTypes = false;
			}
			
			if (this.cg2 == null) {
				selectedEditorPn = (CustomGraphics2EditorPane) getTypeTpn().getSelectedComponent();
				
				if (selectedEditorPn != null)
					this.cg2 = selectedEditorPn.getCg2();
			}
			
			getTypeTpn().setPreferredSize(new Dimension(maxWidth + 40, 400));
		}
		
		private JTabbedPane getTypeTpn() {
			if (typeTpn == null) {
				typeTpn = new JTabbedPane();
				typeTpn.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (updatingTypes) return;
						
						final Component c = typeTpn.getSelectedComponent();
						
						if (c instanceof CustomGraphics2EditorPane) {
							final CyCustomGraphics2Factory<?> cf = ((CustomGraphics2EditorPane)c).getFactory();
							
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
		
		private class CustomGraphics2EditorPane extends JScrollPane {
			
			private static final long serialVersionUID = -5023596235150818148L;
			
			private final CyCustomGraphics2Factory<?> factory;
			private JComponent editor;
			private CyCustomGraphics2 cg2;

			CustomGraphics2EditorPane(final CyCustomGraphics2Factory<?> factory) {
				this.factory = factory;
				this.setBorder(BorderFactory.createEmptyBorder());
				this.setOpaque(false);
				this.getViewport().setOpaque(false);
			}
			
			void update(final CyCustomGraphics2 initialCg2) {
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
		}
	}
}
