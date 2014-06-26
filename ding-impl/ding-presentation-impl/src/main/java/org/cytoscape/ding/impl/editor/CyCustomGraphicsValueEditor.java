package org.cytoscape.ding.impl.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.charts.CyChartFactoryManager;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientEditorFactory;
import org.cytoscape.view.presentation.gradients.CyGradientFactory;
import org.cytoscape.view.presentation.gradients.CyGradientFactoryManager;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;


public class CyCustomGraphicsValueEditor extends JDialog implements ValueEditor<CyCustomGraphics> {

	private static final long serialVersionUID = 3276556808025021859L;

	private boolean editCancelled;
	
	private JTabbedPane cgTypeTpn;
	private JPanel bottomPnl;
	private GraphicsPanel graphicsPnl;
	private ChartPanel chartPnl;
	private GradientPanel gradientPnl;
	private JButton removeBtn;
	private JButton cancelBtn;
	private JButton applyBtn;
	
	private CyCustomGraphics<?> oldCustomGraphics;
	private CyCustomGraphics<?> newCustomGraphics;
	
	private boolean initialized;

	private final CustomGraphicsManager customGraphicsMgr;
	private final CyChartFactoryManager chartFactoryMgr;
	private final CyGradientFactoryManager gradientFactoryMgr;
	private final CyServiceRegistrar serviceRegistrar;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public CyCustomGraphicsValueEditor(final CustomGraphicsManager customGraphicsMgr,
									   final CyChartFactoryManager chartFactoryMgr,
									   final CyGradientFactoryManager gradientFactoryMgr,
									   final CyServiceRegistrar serviceRegistrar) {
		this.customGraphicsMgr = customGraphicsMgr;
		this.chartFactoryMgr = chartFactoryMgr;
		this.gradientFactoryMgr = gradientFactoryMgr;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	@SuppressWarnings("rawtypes")
	public <S extends CyCustomGraphics> CyCustomGraphics<? extends CustomGraphicLayer> showEditor(final Component parent, final S initialValue) {
		oldCustomGraphics = initialValue;
		
		// Make sure it initializes only after the Cytoscape UI (specially DefaultViewPanel) is ready
		if (!initialized) {
			init();
			initialized = true;
		}
		
		updateUI();
		setLocationRelativeTo(parent);
		setVisible(true);
		
		return editCancelled ? oldCustomGraphics : newCustomGraphics;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class<CyCustomGraphics> getValueType() {
		return CyCustomGraphics.class;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		setTitle("Graphics");
		setResizable(false);
		setModal(true);
		setMinimumSize(new Dimension(400, 600));
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		
		getContentPane().add(getCgTypeTpn(), BorderLayout.CENTER);
		getContentPane().add(getBottomPnl(), BorderLayout.SOUTH);
	}
	
	private void updateUI() {
		getGraphicsPnl().updateList();
		getChartPnl().update();
		getGradientPnl().update();
		
		if (oldCustomGraphics instanceof CyChart) {
			getCgTypeTpn().setSelectedComponent(getChartPnl());
		} else if (oldCustomGraphics instanceof CyGradient) {
			getCgTypeTpn().setSelectedComponent(getGradientPnl());
		} else {
			getCgTypeTpn().setSelectedComponent(getGraphicsPnl());
		}
		
		pack();
	}
	
	private void remove() {
		editCancelled = false;
		newCustomGraphics = NullCustomGraphics.getNullObject();
		dispose();
	}
	
	private void cancel() {
		editCancelled = true;
		dispose();
	}
	
	private void apply() {
		editCancelled = false;
		final Component c = getCgTypeTpn().getSelectedComponent();
		
		if (c instanceof GraphicsPanel)
			newCustomGraphics = ((GraphicsPanel)c).getCustomGraphics();
		else if (c instanceof ChartPanel)
			newCustomGraphics = ((ChartPanel)c).getChart();
		else if (c instanceof GradientPanel)
			newCustomGraphics = ((GradientPanel)c).getGradient();
		
		dispose();
	}
	
	private JTabbedPane getCgTypeTpn() {
		if (cgTypeTpn == null) {
			cgTypeTpn = new JTabbedPane();
			cgTypeTpn.addTab("Images", getGraphicsPnl());
			cgTypeTpn.addTab("Charts", getChartPnl());
			cgTypeTpn.addTab("Gradients", getGradientPnl());
		}
		
		return cgTypeTpn;
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
	
	private ChartPanel getChartPnl() {
		if (chartPnl == null) {
			chartPnl = new ChartPanel();
			chartPnl.setOpaque(false);
		}
		
		return chartPnl;
	}
	
	public GradientPanel getGradientPnl() {
		if (gradientPnl == null) {
			gradientPnl = new GradientPanel();
			gradientPnl.setOpaque(false);
		}
		
		return gradientPnl;
	}
	
	public JButton getRemoveBtn() {
		if (removeBtn == null) {
			removeBtn = new JButton("Remove");
			removeBtn.setToolTipText("Remove Graphics");
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
	
	@SuppressWarnings("rawtypes")
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
	
	private class ChartPanel extends JPanel {
		
		private static final long serialVersionUID = 6669567626195325838L;

		static final int CHART_ICON_SIZE = 18;
		
		private CyChart chart;
		private boolean updatingChartTypes;
		
		private JTabbedPane chartTypeTpn;
		
		public ChartPanel() {
			this.setLayout(new BorderLayout());
			this.add(getChartTypeTpn(), BorderLayout.CENTER);
		}
		
		@SuppressWarnings("rawtypes")
		void update() {
			CyChartFactory factory = null;
			
			if (oldCustomGraphics instanceof CyChart) {
				factory = chartFactoryMgr.getCyChartFactory((Class<CyChart<?>>)oldCustomGraphics.getClass());

				if (factory != null)
					chart = factory.getInstance((CyChart)oldCustomGraphics);
			}
			
			updateChartTypes();
		}

		CyChart getChart() {
			return chart;
		}
		
		private void updateChartTypes() {
			updatingChartTypes = true;
			ChartEditorPane selectedEditorPn = null;
			int maxWidth = 100;
			
			try {
				getChartTypeTpn().removeAll();
				
				final Collection<CyChartFactory<?>> chartFactories = chartFactoryMgr.getAllCyChartFactories();
				
				for (final CyChartFactory<?> cf : chartFactories) {
					final CyChartEditorFactory<? extends CustomGraphicLayer> cef = 
							chartFactoryMgr.getCyChartEditorFactory(cf.getSupportedClass());
					final ChartEditorPane chartEditorPn = new ChartEditorPane(cf, cef);
					getChartTypeTpn().addTab(
							"", 
							cf.getIcon(CHART_ICON_SIZE, CHART_ICON_SIZE), 
							chartEditorPn,
							cf.getDisplayName());
					
					CyChart<?> initialChart = null;
					
					if (chart != null) {
						if (cf.getSupportedClass().isAssignableFrom(chart.getClass())) {
							selectedEditorPn = chartEditorPn;
							initialChart = chart;
						} else {
							initialChart = cf.getInstance(chart.getProperties());
						}
					} else {
						initialChart = cf.getInstance(new HashMap<String, Object>());
					}
					
					if (chart == null)
						chart = initialChart;
					
					if (initialChart != null) // Just so this panel's dimensions are set correctly
						chartEditorPn.update(initialChart);
					
					maxWidth = Math.max(maxWidth, chartEditorPn.getPreferredSize().width);
				}
			} finally {
				updatingChartTypes = false;
			}
			
			if (selectedEditorPn != null)
				getChartTypeTpn().setSelectedComponent(selectedEditorPn);
			
			getChartTypeTpn().setPreferredSize(new Dimension(maxWidth + 40, 400));
		}
		
		private JTabbedPane getChartTypeTpn() {
			if (chartTypeTpn == null) {
				chartTypeTpn = new JTabbedPane();
				chartTypeTpn.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (updatingChartTypes) return;
						
						final Component c = chartTypeTpn.getSelectedComponent();
						
						if (c instanceof ChartEditorPane) {
							final CyChartFactory<?> cf = ((ChartEditorPane)c).getChartFactory();
							
							if (chart == null || !cf.getSupportedClass().isAssignableFrom(chart.getClass()))
								chart = cf.getInstance(
										chart != null ? chart.getProperties() : new HashMap<String, Object>());
								
							((ChartEditorPane)c).update(chart);
						}
					}
				});
			}
			
			return chartTypeTpn;
		}
		
		private class ChartEditorPane extends JScrollPane {
			
			private static final long serialVersionUID = -5023596235150818148L;
			
			private final CyChartFactory<?> chartFactory;
			private final CyChartEditorFactory<? extends CustomGraphicLayer> editorFactory;
			private JComponent editor;

			ChartEditorPane(final CyChartFactory<?> chartFactory,
							final CyChartEditorFactory<? extends CustomGraphicLayer> editorFactory) {
				this.chartFactory = chartFactory;
				this.editorFactory = editorFactory;
				this.setBorder(BorderFactory.createEmptyBorder());
				this.setOpaque(false);
				this.getViewport().setOpaque(false);
			}
			
			void update(CyChart chart) {
				editor = editorFactory.createEditor(chart);
				this.setViewportView(editor);
				this.updateUI();
			}
			
			CyChartFactory<?> getChartFactory() {
				return chartFactory;
			}
			
			CyChartEditorFactory<? extends CustomGraphicLayer> getEditorFactory() {
				return editorFactory;
			}
			
			JComponent getEditor() {
				return editor;
			}
		}
	}
	
	private class GradientPanel extends JPanel {

		private static final long serialVersionUID = 740722017387521227L;
		
		private JComboBox gradientTypeCmb;
		private JComponent editor;
		
		private CyGradient<?> gradient;
		private boolean updatingGradientTypes;
		
		@SuppressWarnings("serial")
		GradientPanel() {
			this.setLayout(new BorderLayout());
			
			gradientTypeCmb = new JComboBox(new DefaultComboBoxModel());
			gradientTypeCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!updatingGradientTypes) {
						if (editor != null)
							GradientPanel.this.remove(editor);
						
						final CyGradientFactory factory = (CyGradientFactory) gradientTypeCmb.getSelectedItem();
						
						if (factory != null) {
							if (oldCustomGraphics != null &&
									factory.getSupportedClass().isAssignableFrom(oldCustomGraphics.getClass()))
								gradient = factory.getInstance((CyGradient)oldCustomGraphics);
							else if (oldCustomGraphics instanceof CyGradient)
								gradient = factory.getInstance(((CyGradient)oldCustomGraphics).getProperties());
							else
								gradient = factory.getInstance("");
							
							final CyGradientEditorFactory gef =
									gradientFactoryMgr.getCyGradientEditorFactory(factory.getSupportedClass());
							
							if (gef != null) {
								editor = gef.createEditor(gradient);
								GradientPanel.this.add(editor);
							}
						}
					}
				}
			});
			gradientTypeCmb.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					if (value == null)
						this.setText("-- none --");
					else if (value instanceof CyGradientFactory)
						this.setText(((CyGradientFactory<?>)value).getDisplayName());
					else
						this.setText("[ invalid gradient type ]"); // Should never happen
					
					return this;
				}
			});
			
			this.add(gradientTypeCmb, BorderLayout.NORTH);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		void update() {
			if (oldCustomGraphics instanceof CyGradient) {
				final CyGradientFactory factory =
						gradientFactoryMgr.getCyGradientFactory((Class<CyGradient<?>>)oldCustomGraphics.getClass());

				if (factory != null)
					gradient = factory.getInstance((CyGradient<?>)oldCustomGraphics);
			}
			
			updateGradientTypes();
		}
		
		CyGradient<?> getGradient() {
			return gradient;
		}
		
		@SuppressWarnings("rawtypes")
		private void updateGradientTypes() {
			final Collection<CyGradientFactory<?>> factories = gradientFactoryMgr.getAllCyGradientFactories();
			CyGradientFactory selectedFactory = factories.isEmpty() ? null : factories.iterator().next();
			updatingGradientTypes = true;
			
			final DefaultComboBoxModel cmbModel = (DefaultComboBoxModel) gradientTypeCmb.getModel();
			cmbModel.removeAllElements();
			
			try {
				for (final CyGradientFactory<?> gf : factories) {
					cmbModel.addElement(gf);
					
					if (gradient != null && gf.getSupportedClass().isAssignableFrom(gradient.getClass()))
						selectedFactory = gf;
				}
			} finally {
				updatingGradientTypes = false;
			}
			
			gradientTypeCmb.setSelectedItem(selectedFactory);
		}
	}
}
