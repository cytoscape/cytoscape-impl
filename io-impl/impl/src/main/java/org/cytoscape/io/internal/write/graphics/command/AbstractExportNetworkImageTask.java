package org.cytoscape.io.internal.write.graphics.command;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

/**
 * The Tasks in this package are just for use as commands. 
 * 
 * This is because the old ExportNetworkImageTaskFactoryImpl does not provide
 * access to the tunables in the PDFWriter, PNGWriter, SVGWriter and PSWriter classes
 * for use as command arguments.
 * 
 * We also have a chicken and egg problem with the Writer classes. They all need
 * a RenderingEngine and OutputStream for their constructors. But for the commands 
 * we need the tunables to be available before we know the network or output file for the export.
 * 
 * The solution was to move the tunables into separate XXXTunable classes that can be shared
 * by the command tasks and the Writers.
 */
public abstract class AbstractExportNetworkImageTask extends AbstractTask implements ObservableTask {
	
	@ContainsTunables
	public ExportNetworkTunables tunables;
	
	
	public AbstractExportNetworkImageTask(CyServiceRegistrar registrar) {
		this.tunables = new ExportNetworkTunables(registrar);
	}
	
	
	abstract CyWriter createWriter(RenderingEngine<?> re, OutputStream outStream);
	

	@Override
	public void run(TaskMonitor tm) throws Exception {
		var outputStream = new FileOutputStream(tunables.getOutputFile());
		var renderingEngine = tunables.getRenderingEngine();
		
		CyWriter writeTask = createWriter(renderingEngine, outputStream);
		
		Task closeTask = new AbstractTask() {
			@Override public void run(TaskMonitor tm) throws IOException {
				outputStream.close();
			}
		};
		
		insertTasksAfterCurrentTask(writeTask, closeTask);
	}
	
	
	@Override
	public List<Class<?>> getResultClasses() {
		return List.of(String.class, JSONResult.class);
	}
	
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		var outPath = tunables.getOutPath();
		
		if(type == String.class) {
			return outPath == null ? null : type.cast("Output File: " + outPath);
		}
		if(type == JSONResult.class) {
			JSONResult res = () -> "{ \"file\": \"" + outPath + "\" }";
			return type.cast(res);
		}
		
		return null;
	}
}
