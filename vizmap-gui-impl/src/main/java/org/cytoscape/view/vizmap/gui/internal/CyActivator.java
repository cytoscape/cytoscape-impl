
package org.cytoscape.view.vizmap.gui.internal;

import java.awt.Color;
import java.awt.Paint;
import java.util.Properties;

import javax.swing.table.DefaultTableCellRenderer;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.action.EditSelectedCellAction;
import org.cytoscape.view.vizmap.gui.internal.bypass.BypassManager;
import org.cytoscape.view.vizmap.gui.internal.editor.BooleanVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.ColorVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.EditorManagerImpl;
import org.cytoscape.view.vizmap.gui.internal.editor.NumberVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.StringVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyColorPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.BooleanValueEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.CyColorChooser;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.FontEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.NumericValueEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.StringValueEditor;
import org.cytoscape.view.vizmap.gui.internal.event.VizMapEventHandlerManagerImpl;
import org.cytoscape.view.vizmap.gui.internal.task.ClearBendTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CopyVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CreateLegendTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CreateNewVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.DeleteMappingFunctionTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.DeleteVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.ImportDefaultVizmapTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.RenameVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.theme.ColorManager;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.FitLabelMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.NumberSeriesMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RainbowColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RainbowOscColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomNumberMappingGenerator;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import com.l2fprod.common.propertysheet.PropertySheetPanel;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
		DialogTaskManager dialogTaskManagerServiceRef = getService(bc,DialogTaskManager.class);
		VisualStyleFactory visualStyleFactoryServiceRef = getService(bc,VisualStyleFactory.class);
		VisualMappingManager vmmServiceRef = getService(bc,VisualMappingManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		RenderingEngineFactory presentationFactoryServiceRef = getService(bc,RenderingEngineFactory.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory graphViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		VizmapReaderManager vizmapReaderManagerServiceRef = getService(bc,VizmapReaderManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);
		
		AttributeSetManager attributeSetManager = new AttributeSetManager(cyNetworkTableManagerServiceRef);
		EditorManagerImpl editorManager = new EditorManagerImpl(cyApplicationManagerServiceRef,attributeSetManager,vmmServiceRef,cyNetworkTableManagerServiceRef, cyNetworkManagerServiceRef);
		MappingFunctionFactoryManagerImpl mappingFunctionFactoryManager = new MappingFunctionFactoryManagerImpl(editorManager);
		PropertySheetPanel propertySheetPanel = new PropertySheetPanel();
		
		CyColorChooser colorEditor = new CyColorChooser();
		CyColorPropertyEditor cyColorPropertyEditor = new CyColorPropertyEditor(colorEditor);
		
		FontEditor fontEditor = new FontEditor();
		NumericValueEditor<Double> doubleValueEditor = new NumericValueEditor<Double>(Double.class);
		NumericValueEditor<Integer> integerValueEditor = new NumericValueEditor<Integer>(Integer.class);
		NumericValueEditor<Float> floatValueEditor = new NumericValueEditor<Float>(Float.class);
		StringValueEditor stringValueEditor = new StringValueEditor();
		BooleanValueEditor booleanValueEditor = new BooleanValueEditor();
		
		ColorVisualPropertyEditor colorPropertyEditor = new ColorVisualPropertyEditor(Paint.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,editorManager,vmmServiceRef, cyColorPropertyEditor);
		NumberVisualPropertyEditor doublePropertyEditor = new NumberVisualPropertyEditor(Double.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,editorManager,vmmServiceRef);
		NumberVisualPropertyEditor integerPropertyEditor = new NumberVisualPropertyEditor(Integer.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,editorManager,vmmServiceRef);
		NumberVisualPropertyEditor floatPropertyEditor = new NumberVisualPropertyEditor(Float.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,editorManager,vmmServiceRef);
		
		StringVisualPropertyEditor stringPropertyEditor = new StringVisualPropertyEditor();
		final CyComboBoxPropertyEditor booleanEditor = new CyComboBoxPropertyEditor();
		booleanEditor.setAvailableValues(new Boolean[] {true, false});
		BooleanVisualPropertyEditor booleanVisualPropertyEditor = new BooleanVisualPropertyEditor(booleanEditor);
		
		ColorManager colorMgr = new ColorManager();
		IconManager iconManager = new IconManager();
		VizMapperMenuManager menuManager = new VizMapperMenuManager(dialogTaskManagerServiceRef,propertySheetPanel,vmmServiceRef, cyApplicationManagerServiceRef);
		DefaultViewPanelImpl defaultViewPanel = new DefaultViewPanelImpl(cyNetworkFactoryServiceRef,graphViewFactoryServiceRef,presentationFactoryServiceRef, vmmServiceRef);
		
		VizMapperUtil vizMapperUtil = new VizMapperUtil(vmmServiceRef);
		SetViewModeAction viewModeAction = new SetViewModeAction();
		
		DefaultViewEditorImpl defViewEditor = new DefaultViewEditorImpl(defaultViewPanel,editorManager,cyApplicationManagerServiceRef,vmmServiceRef,vizMapperUtil,cyEventHelperServiceRef,viewModeAction);
		CreateNewVisualStyleTaskFactory createNewVisualStyleTaskFactory = new CreateNewVisualStyleTaskFactory(visualStyleFactoryServiceRef,vmmServiceRef);
		DeleteVisualStyleTaskFactory removeVisualStyleTaskFactory = new DeleteVisualStyleTaskFactory(vmmServiceRef);
		ImportDefaultVizmapTaskFactory importDefaultVizmapTaskFactory = new ImportDefaultVizmapTaskFactory(vizmapReaderManagerServiceRef,vmmServiceRef,cyApplicationConfigurationServiceRef);
		VizMapPropertySheetBuilder vizMapPropertySheetBuilder = new VizMapPropertySheetBuilder(menuManager,cyNetworkManagerServiceRef,propertySheetPanel,editorManager,defaultViewPanel,cyTableManagerServiceRef,vizMapperUtil,vmmServiceRef);
		EditorWindowManager editorWindowManager = new EditorWindowManager(editorManager,propertySheetPanel);
		
		VizMapperMainPanel vizMapperMainPanel = new VizMapperMainPanel(visualStyleFactoryServiceRef,defViewEditor,iconManager,colorMgr,vmmServiceRef,menuManager,editorManager,propertySheetPanel,vizMapPropertySheetBuilder,editorWindowManager,cyApplicationManagerServiceRef,importDefaultVizmapTaskFactory,dialogTaskManagerServiceRef,viewModeAction);
		RenameVisualStyleTaskFactory renameVisualStyleTaskFactory = new RenameVisualStyleTaskFactory(vmmServiceRef);
		CopyVisualStyleTaskFactory copyVisualStyleTaskFactory = new CopyVisualStyleTaskFactory(vmmServiceRef,visualStyleFactoryServiceRef);
		CreateLegendTaskFactory createLegendTaskFactory = new CreateLegendTaskFactory(cyApplicationManagerServiceRef, vmmServiceRef);
		DeleteMappingFunctionTaskFactory deleteMappingFunctionTaskFactory = new DeleteMappingFunctionTaskFactory(propertySheetPanel,vmmServiceRef, cyApplicationManagerServiceRef);
		
		RainbowColorMappingGenerator rainbowGenerator = new RainbowColorMappingGenerator(Color.class);
		RainbowOscColorMappingGenerator rainbowOscGenerator = new RainbowOscColorMappingGenerator(Color.class);
		RandomColorMappingGenerator randomColorGenerator = new RandomColorMappingGenerator(Color.class);
		NumberSeriesMappingGenerator<Number> seriesGenerator = new NumberSeriesMappingGenerator<Number>(Number.class);
		RandomNumberMappingGenerator randomNumberGenerator = new RandomNumberMappingGenerator();
		FitLabelMappingGenerator<Number> fitLabelMappingGenerator = new FitLabelMappingGenerator<Number>(Number.class, cyApplicationManagerServiceRef, vmmServiceRef);
		
		DefaultTableCellRenderer emptyBoxRenderer = new DefaultTableCellRenderer();
		DefaultTableCellRenderer filledBoxRenderer = new DefaultTableCellRenderer();
		VizMapEventHandlerManagerImpl vizMapEventHandlerManager = new VizMapEventHandlerManagerImpl(vmmServiceRef, editorManager,vizMapPropertySheetBuilder,propertySheetPanel,vizMapperMainPanel,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,attributeSetManager,vizMapperUtil);
		BypassManager bypassManager = new BypassManager(cyServiceRegistrarServiceRef,editorManager, vmmServiceRef);
		
		// Context menu for edge bend
		BendFactory bf = getService(bc, BendFactory.class);
		
		final Properties clearBendProp = new Properties();
		clearBendProp.put(ServiceProperties.PREFERRED_MENU, "Clear Bend");
		final ClearBendTaskFactory clearBendTaskFactory = new ClearBendTaskFactory(vmmServiceRef, bf);
		registerService(bc, clearBendTaskFactory, EdgeViewTaskFactory.class, clearBendProp);
		
		registerAllServices(bc,viewModeAction, new Properties());
		
		registerAllServices(bc,attributeSetManager, new Properties());
		registerAllServices(bc,vizMapperMainPanel, new Properties());
		registerAllServices(bc,defViewEditor, new Properties());
		registerAllServices(bc,editorManager.getNodeEditor(), new Properties());
		registerAllServices(bc,editorManager.getEdgeEditor(), new Properties());
		registerAllServices(bc,editorManager.getNetworkEditor(), new Properties());
		registerAllServices(bc,colorEditor, new Properties());
		registerAllServices(bc,fontEditor, new Properties());
		registerAllServices(bc,doubleValueEditor, new Properties());
		registerAllServices(bc,integerValueEditor, new Properties());
		registerAllServices(bc,floatValueEditor, new Properties());
		registerAllServices(bc,stringValueEditor, new Properties());
		registerAllServices(bc,booleanValueEditor, new Properties());
		registerAllServices(bc,colorPropertyEditor, new Properties());
		registerAllServices(bc,doublePropertyEditor, new Properties());
		registerAllServices(bc,floatPropertyEditor, new Properties());
		registerAllServices(bc,integerPropertyEditor, new Properties());
		
		registerAllServices(bc,stringPropertyEditor, new Properties());
		registerAllServices(bc,booleanVisualPropertyEditor, new Properties());
		
		registerAllServices(bc,editorManager, new Properties());

		Properties createNewVisualStyleTaskFactoryProps = new Properties();
		createNewVisualStyleTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		createNewVisualStyleTaskFactoryProps.setProperty("title","Create New Visual Style");
		createNewVisualStyleTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,createNewVisualStyleTaskFactory, createNewVisualStyleTaskFactoryProps);

		Properties removeVisualStyleTaskFactoryProps = new Properties();
		removeVisualStyleTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		removeVisualStyleTaskFactoryProps.setProperty("title","Remove Visual Style");
		removeVisualStyleTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,removeVisualStyleTaskFactory, removeVisualStyleTaskFactoryProps);

		Properties renameVisualStyleTaskFactoryProps = new Properties();
		renameVisualStyleTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		renameVisualStyleTaskFactoryProps.setProperty("title","Rename Visual Style");
		renameVisualStyleTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,renameVisualStyleTaskFactory, renameVisualStyleTaskFactoryProps);

		Properties copyVisualStyleTaskFactoryProps = new Properties();
		copyVisualStyleTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		copyVisualStyleTaskFactoryProps.setProperty("title","Copy Visual Style");
		copyVisualStyleTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,copyVisualStyleTaskFactory, copyVisualStyleTaskFactoryProps);

		Properties createLegendTaskFactoryProps = new Properties();
		createLegendTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		createLegendTaskFactoryProps.setProperty("title","Create Legend");
		createLegendTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,createLegendTaskFactory, createLegendTaskFactoryProps);

		Properties deleteMappingFunctionTaskFactoryProps = new Properties();
		deleteMappingFunctionTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		deleteMappingFunctionTaskFactoryProps.setProperty("title","Delete Selected Mapping");
		deleteMappingFunctionTaskFactoryProps.setProperty("menu","context");
		registerAllServices(bc,deleteMappingFunctionTaskFactory, deleteMappingFunctionTaskFactoryProps);

		Properties rainbowGeneratorProps = new Properties();
		rainbowGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		rainbowGeneratorProps.setProperty("title","Rainbow");
		rainbowGeneratorProps.setProperty("menu","context");
		registerService(bc,rainbowGenerator,DiscreteMappingGenerator.class, rainbowGeneratorProps);

		Properties rainbowOscGeneratorProps = new Properties();
		rainbowOscGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		rainbowOscGeneratorProps.setProperty("title","Rainbow OSC");
		rainbowOscGeneratorProps.setProperty("menu","context");
		registerService(bc,rainbowOscGenerator,DiscreteMappingGenerator.class, rainbowOscGeneratorProps);

		Properties randomColorGeneratorProps = new Properties();
		randomColorGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		randomColorGeneratorProps.setProperty("title","Random Color");
		randomColorGeneratorProps.setProperty("menu","context");
		registerService(bc,randomColorGenerator,DiscreteMappingGenerator.class, randomColorGeneratorProps);
		
		Properties numberSeriesGeneratorProps = new Properties();
		numberSeriesGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		numberSeriesGeneratorProps.setProperty("title","Number Series");
		numberSeriesGeneratorProps.setProperty("menu","context");
		registerService(bc,seriesGenerator,DiscreteMappingGenerator.class, numberSeriesGeneratorProps);
		
		Properties randomNumberGeneratorProps = new Properties();
		randomNumberGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		randomNumberGeneratorProps.setProperty("title","Random Numbers");
		randomNumberGeneratorProps.setProperty("menu","context");
		registerService(bc, randomNumberGenerator, DiscreteMappingGenerator.class, randomNumberGeneratorProps);
		
		Properties fitLabelGeneratorProps = new Properties();
		fitLabelGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		fitLabelGeneratorProps.setProperty("title","Fit label width (SUID to Node Width)");
		fitLabelGeneratorProps.setProperty("menu","context");
		registerService(bc, fitLabelMappingGenerator, DiscreteMappingGenerator.class, fitLabelGeneratorProps);
				
		EditSelectedCellAction editAction = new EditSelectedCellAction(editorManager, cyApplicationManagerServiceRef, propertySheetPanel, vmmServiceRef);
		Properties editSelectedProps = new Properties();
		editSelectedProps.setProperty("service.type","vizmapUI.contextMenu");
		editSelectedProps.setProperty("title","Edit Selected");
		editSelectedProps.setProperty("menu","context");
		registerService(bc,editAction, CyAction.class, editSelectedProps);

		// Adding Vizmap-local context menus.
		registerServiceListener(bc,menuManager,"onBind","onUnbind",CyAction.class);
		
		registerServiceListener(bc,mappingFunctionFactoryManager,"addFactory","removeFactory",VisualMappingFunctionFactory.class);
		registerServiceListener(bc,editorManager,"addValueEditor","removeValueEditor",ValueEditor.class);
		registerServiceListener(bc,editorManager,"addVisualPropertyEditor","removeVisualPropertyEditor",VisualPropertyEditor.class);
		registerServiceListener(bc,menuManager,"addTaskFactory","removeTaskFactory",TaskFactory.class);
		registerServiceListener(bc,menuManager,"addMappingGenerator","removeMappingGenerator",DiscreteMappingGenerator.class);
		registerServiceListener(bc,editorManager,"addRenderingEngineFactory","removeRenderingEngineFactory",RenderingEngineFactory.class);
		registerServiceListener(bc,bypassManager,"addBypass","removeBypass",RenderingEngineFactory.class);
		
		registerServiceListener(bc,vizMapEventHandlerManager,"registerPCL","unregisterPCL", RenderingEngineFactory.class);
	}
}

