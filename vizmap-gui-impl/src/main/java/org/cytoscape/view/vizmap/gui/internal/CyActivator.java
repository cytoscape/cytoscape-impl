
package org.cytoscape.view.vizmap.gui.internal;

import java.awt.Color;
import java.awt.Paint;
import java.util.Properties;

import javax.swing.table.DefaultTableCellRenderer;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.VisualPropertyDependency;
import org.cytoscape.view.vizmap.gui.action.VizMapUIAction;
import org.cytoscape.view.vizmap.gui.dependency.NodeSizeDependency;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.bypass.BypassManager;
import org.cytoscape.view.vizmap.gui.internal.editor.ColorVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.EditorManagerImpl;
import org.cytoscape.view.vizmap.gui.internal.editor.NumberVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.CyColorChooser;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.FontEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.NumericValueEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.StringValueEditor;
import org.cytoscape.view.vizmap.gui.internal.event.VizMapEventHandlerManagerImpl;
import org.cytoscape.view.vizmap.gui.internal.task.CopyVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CreateLegendTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CreateNewVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.DeleteMappingFunctionTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.DeleteVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.ImportDefaultVizmapTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.RenameVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.theme.ColorManager;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.gui.internal.util.DefaultVisualStyleBuilder;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RainbowColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RainbowOscColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITaskManager;
import org.osgi.framework.BundleContext;

import com.l2fprod.common.propertysheet.PropertySheetPanel;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
		GUITaskManager taskManagerServiceRef = getService(bc,GUITaskManager.class);
		VisualStyleFactory visualStyleFactoryServiceRef = getService(bc,VisualStyleFactory.class);
		VisualLexicon dingVisualLexiconServiceRef = getService(bc,VisualLexicon.class,"(id=ding)");
		RenderingEngineFactory dingRenderingEngineFactoryServiceRef = getService(bc,RenderingEngineFactory.class,"(id=ding)");
		VisualMappingManager vmmServiceRef = getService(bc,VisualMappingManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		RenderingEngineFactory presentationFactoryServiceRef = getService(bc,RenderingEngineFactory.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory graphViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		VizmapReaderManager vizmapReaderManagerServiceRef = getService(bc,VizmapReaderManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);
		
		AttributeSetManager attributeSetManager = new AttributeSetManager(cyNetworkTableManagerServiceRef);
		SelectedVisualStyleManagerImpl selectedVisualStyleManager = new SelectedVisualStyleManagerImpl(vmmServiceRef);
		EditorManagerImpl editorManager = new EditorManagerImpl(cyApplicationManagerServiceRef,attributeSetManager,vmmServiceRef,cyNetworkTableManagerServiceRef,selectedVisualStyleManager);
		MappingFunctionFactoryManagerImpl mappingFunctionFactoryManager = new MappingFunctionFactoryManagerImpl(editorManager);
		DefaultVisualStyleBuilder defaultVisualStyleBuilder = new DefaultVisualStyleBuilder(visualStyleFactoryServiceRef,passthroughMappingFactoryRef);
		PropertySheetPanel propertySheetPanel = new PropertySheetPanel();
		CyColorChooser colorEditor = new CyColorChooser();
		FontEditor fontEditor = new FontEditor();
		NumericValueEditor doubleValueEditor = new NumericValueEditor(Double.class);
		NumericValueEditor integerValueEditor = new NumericValueEditor(Integer.class);
		NumericValueEditor floatValueEditor = new NumericValueEditor(Float.class);
		StringValueEditor stringValueEditor = new StringValueEditor(String.class);
		ColorVisualPropertyEditor colorPropertyEditor = new ColorVisualPropertyEditor(Paint.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,selectedVisualStyleManager,editorManager,vmmServiceRef);
		NumberVisualPropertyEditor doublePropertyEditor = new NumberVisualPropertyEditor(Double.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,selectedVisualStyleManager,editorManager,vmmServiceRef);
		NumberVisualPropertyEditor integerPropertyEditor = new NumberVisualPropertyEditor(Integer.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,selectedVisualStyleManager,editorManager,vmmServiceRef);
		NumberVisualPropertyEditor floatPropertyEditor = new NumberVisualPropertyEditor(Float.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,selectedVisualStyleManager,editorManager,vmmServiceRef);
		ColorManager colorMgr = new ColorManager();
		IconManager iconManager = new IconManager();
		VizMapperMenuManager menuManager = new VizMapperMenuManager(taskManagerServiceRef,propertySheetPanel,selectedVisualStyleManager,cyApplicationManagerServiceRef);
		DefaultViewPanelImpl defaultViewPanel = new DefaultViewPanelImpl(cyNetworkFactoryServiceRef,graphViewFactoryServiceRef,presentationFactoryServiceRef,selectedVisualStyleManager);
		NodeSizeDependency nodeSizeDep = new NodeSizeDependency();
		VizMapperUtil vizMapperUtil = new VizMapperUtil(vmmServiceRef);
		VisualPropertyDependencyManagerImpl vpDependencyManager = new VisualPropertyDependencyManagerImpl();
		DefaultViewEditorImpl defViewEditor = new DefaultViewEditorImpl(defaultViewPanel,editorManager,cyApplicationManagerServiceRef,vmmServiceRef,selectedVisualStyleManager,vizMapperUtil,vpDependencyManager,cyEventHelperServiceRef);
		CreateNewVisualStyleTaskFactory createNewVisualStyleTaskFactory = new CreateNewVisualStyleTaskFactory(vizMapperUtil, visualStyleFactoryServiceRef,vmmServiceRef);
		DeleteVisualStyleTaskFactory removeVisualStyleTaskFactory = new DeleteVisualStyleTaskFactory(vmmServiceRef,selectedVisualStyleManager);
		ImportDefaultVizmapTaskFactory importDefaultVizmapTaskFactory = new ImportDefaultVizmapTaskFactory(vizmapReaderManagerServiceRef,vmmServiceRef,cyApplicationConfigurationServiceRef);
		VizMapPropertySheetBuilder vizMapPropertySheetBuilder = new VizMapPropertySheetBuilder(menuManager,cyNetworkManagerServiceRef,propertySheetPanel,editorManager,defaultViewPanel,cyTableManagerServiceRef,vizMapperUtil,vmmServiceRef);
		EditorWindowManager editorWindowManager = new EditorWindowManager(editorManager,propertySheetPanel);
		VizMapperMainPanel vizMapperMainPanel = new VizMapperMainPanel(visualStyleFactoryServiceRef,defViewEditor,iconManager,colorMgr,vmmServiceRef,menuManager,editorManager,propertySheetPanel,vizMapPropertySheetBuilder,editorWindowManager,cyApplicationManagerServiceRef,cyEventHelperServiceRef,selectedVisualStyleManager,importDefaultVizmapTaskFactory,taskManagerServiceRef);
		RenameVisualStyleTaskFactory renameVisualStyleTaskFactory = new RenameVisualStyleTaskFactory(vmmServiceRef,selectedVisualStyleManager,vizMapperUtil,vizMapperMainPanel);
		CopyVisualStyleTaskFactory copyVisualStyleTaskFactory = new CopyVisualStyleTaskFactory(vmmServiceRef,visualStyleFactoryServiceRef,selectedVisualStyleManager,vizMapperUtil,vizMapperMainPanel);
		CreateLegendTaskFactory createLegendTaskFactory = new CreateLegendTaskFactory(selectedVisualStyleManager,vizMapperMainPanel);
		DeleteMappingFunctionTaskFactory deleteMappingFunctionTaskFactory = new DeleteMappingFunctionTaskFactory(propertySheetPanel,selectedVisualStyleManager,cyApplicationManagerServiceRef);
		RainbowColorMappingGenerator rainbowGenerator = new RainbowColorMappingGenerator(Color.class);
		RainbowOscColorMappingGenerator rainbowOscGenerator = new RainbowOscColorMappingGenerator(Color.class);
		RandomColorMappingGenerator randomColorGenerator = new RandomColorMappingGenerator(Color.class);
		DefaultTableCellRenderer emptyBoxRenderer = new DefaultTableCellRenderer();
		DefaultTableCellRenderer filledBoxRenderer = new DefaultTableCellRenderer();
		VizMapEventHandlerManagerImpl vizMapEventHandlerManager = new VizMapEventHandlerManagerImpl(selectedVisualStyleManager,editorManager,vizMapPropertySheetBuilder,propertySheetPanel,vizMapperMainPanel,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,attributeSetManager,vizMapperUtil);
		BypassManager bypassManager = new BypassManager(cyServiceRegistrarServiceRef,editorManager,selectedVisualStyleManager);
		
		registerAllServices(bc,selectedVisualStyleManager, new Properties());
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
		registerAllServices(bc,colorPropertyEditor, new Properties());
		registerAllServices(bc,doublePropertyEditor, new Properties());
		registerAllServices(bc,floatPropertyEditor, new Properties());
		registerAllServices(bc,integerPropertyEditor, new Properties());
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
		randomColorGeneratorProps.setProperty("title","Random");
		randomColorGeneratorProps.setProperty("menu","context");
		registerService(bc,randomColorGenerator,DiscreteMappingGenerator.class, randomColorGeneratorProps);
		registerAllServices(bc,nodeSizeDep, new Properties());

		registerServiceListener(bc,menuManager,"onBind","onUnbind",VizMapUIAction.class);
		registerServiceListener(bc,mappingFunctionFactoryManager,"addFactory","removeFactory",VisualMappingFunctionFactory.class);
		registerServiceListener(bc,editorManager,"addValueEditor","removeValueEditor",ValueEditor.class);
		registerServiceListener(bc,editorManager,"addVisualPropertyEditor","removeVisualPropertyEditor",VisualPropertyEditor.class);
		registerServiceListener(bc,menuManager,"addTaskFactory","removeTaskFactory",TaskFactory.class);
		registerServiceListener(bc,menuManager,"addMappingGenerator","removeMappingGenerator",DiscreteMappingGenerator.class);
		registerServiceListener(bc,vpDependencyManager,"addDependency","removeDependency",VisualPropertyDependency.class);
		registerServiceListener(bc,editorManager,"addRenderingEngineFactory","removeRenderingEngineFactory",RenderingEngineFactory.class);
		registerServiceListener(bc,bypassManager,"addBypass","removeBypass",RenderingEngineFactory.class);
		
		registerServiceListener(bc,vizMapEventHandlerManager,"registerPCL","unregisterPCL", RenderingEngineFactory.class);
	}
}

