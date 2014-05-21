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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.charts.CyChartFactoryManager;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;


public class CyCustomGraphicsValueEditor extends JDialog implements ValueEditor<CyCustomGraphics> {

	private static final long serialVersionUID = 3276556808025021859L;

	private boolean editCancelled;
	
	private JTabbedPane cgTypeTpn;
	private JPanel bottomPnl;
	private GraphicsEditor graphicsEditor;
	private ChartEditor chartEditor;
	private JButton cancelBtn;
	private JButton applyBtn;
	
	private CyCustomGraphics<?> oldCustomGraphics;
	private CyCustomGraphics<?> newCustomGraphics;
	
	private boolean initialized;

	private final CustomGraphicsManager customGraphicsMgr;
	private final CyChartFactoryManager chartFactoryMgr;
	private final CyServiceRegistrar serviceRegistrar;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public CyCustomGraphicsValueEditor(final CustomGraphicsManager customGraphicsMgr,
									   final CyChartFactoryManager chartFactoryMgr,
									   final CyServiceRegistrar serviceRegistrar) {
		this.customGraphicsMgr = customGraphicsMgr;
		this.chartFactoryMgr = chartFactoryMgr;
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
		setMinimumSize(new Dimension(100, 40));
		
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
		getGraphicsEditor().updateList();
		getChartEditor().update();
		
		if (oldCustomGraphics instanceof CyChart) {
			getCgTypeTpn().setSelectedComponent(getChartEditor());
		} else {
			getCgTypeTpn().setSelectedComponent(getGraphicsEditor());
		}
		
		pack();
	}
	
	private void cancel() {
		editCancelled = true;
		dispose();
	}
	
	private void apply() {
		editCancelled = false;
		final Component c = getCgTypeTpn().getSelectedComponent();
		
		if (c instanceof GraphicsEditor)
			newCustomGraphics = ((GraphicsEditor)c).getCustomGraphics();
		else if (c instanceof ChartEditor)
			newCustomGraphics = ((ChartEditor)c).getChart();
		
		dispose();
	}
	
	private JTabbedPane getCgTypeTpn() {
		if (cgTypeTpn == null) {
			cgTypeTpn = new JTabbedPane();
			cgTypeTpn.addTab("Images", getGraphicsEditor());
			cgTypeTpn.addTab("Charts", getChartEditor());
		}
		
		return cgTypeTpn;
	}
	
	private JPanel getBottomPnl() {
		if (bottomPnl == null) {
			bottomPnl = new JPanel();
			bottomPnl.add(getCancelBtn());
			bottomPnl.add(getApplyBtn());
		}
		
		return bottomPnl;
	}
	
	private GraphicsEditor getGraphicsEditor() {
		if (graphicsEditor == null) {
			graphicsEditor = new GraphicsEditor();
		}
		
		return graphicsEditor;
	}
	
	private ChartEditor getChartEditor() {
		if (chartEditor == null) {
			chartEditor = new ChartEditor();
			chartEditor.setOpaque(false);
		}
		
		return chartEditor;
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
	private class GraphicsEditor extends JPanel {
		
		private static final long serialVersionUID = 1157288441507073705L;
		
		private DiscreteValueList<CyCustomGraphics> graphicsList;
		
		public GraphicsEditor() {
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
	
	private class ChartEditor extends JPanel {
		
		private static final long serialVersionUID = 6669567626195325838L;

		static final int CHART_ICON_SIZE = 18;
		
		private CyChart chart;
		private boolean updatingChartTypes;
		
		private JTabbedPane chartTypeTpn;
		
		public ChartEditor() {
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
}
