package org.cytoscape.welcome.internal.task;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.welcome.internal.style.IntActXGMMLVisualStyleBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by David Welker on 8/12/14
 * Copyright © 2014. All rights reserved.
 */
public class BuildNetworkBasedOnGenesTask extends AbstractTask
{
	private final CyNetworkReaderManager networkReaderManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyLayoutAlgorithmManager layoutAlgorithmManager;
	private final VisualMappingManager visualMappingManager;
	private final CyNetworkViewManager networkViewManager;
	private final IntActXGMMLVisualStyleBuilder intActVSBuilder;

	private final String species;
	private final List<String> geneNames;

	public BuildNetworkBasedOnGenesTask(CyNetworkReaderManager networkReaderManager, CyNetworkManager networkManager, CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layoutAlgorithmManager, VisualMappingManager visualMappingManager, CyNetworkViewManager networkViewManager, IntActXGMMLVisualStyleBuilder intActVSBuilder, String species, List<String> geneNames)
	{
		this.networkReaderManager = networkReaderManager;
		this.networkManager = networkManager;
		this.networkViewFactory = networkViewFactory;
		this.layoutAlgorithmManager = layoutAlgorithmManager;
		this.visualMappingManager = visualMappingManager;
		this.networkViewManager = networkViewManager;
		this.intActVSBuilder = intActVSBuilder;

		this.species = species;
		this.geneNames = geneNames;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception
	{
		taskMonitor.setTitle("Importing Interactions from IntAct Database");
		taskMonitor.setStatusMessage("Loading interactions from IntAct database...");
		taskMonitor.setProgress(0.01d);

		//TODO: Get query from GUI.
		String webserviceUrl = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/query/";
		String query = "species:" + species;
		if( geneNames != null && !geneNames.isEmpty() )
		{
			query += " AND ";
			if( geneNames.size() > 1 )
				query += "( ";
			for( int i = 0; i < geneNames.size() - 1; i++ )
			{
				String geneName = geneNames.get(i);
				query += "alias:" + geneName + " OR ";
			}
			query += "alias:" + geneNames.get(geneNames.size()-1);
			if( geneNames.size() > 1 )
				query += " )";
		}
		System.out.println("Query = " + query);
		query = URLEncoder.encode(query, "UTF-8").replace("+", "%20");
		System.out.println("Query = " + query);
		String format = "?firstResult=0&maxResults=500&format=xgmml";
		webserviceUrl += query + format;
		System.out.println("webserviceUrl = " + webserviceUrl);
		//String query = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/query/species:human?firstResult=0&maxResults=100&format=xgmml";
		URL url = new URL(webserviceUrl);
		//TODO: Need to think about whether writing to file is a good choice....
		File file = new File("/Users/David/Desktop/temp/temp.xgmml");
		URLConnection connection = url.openConnection();
		InputStream in = connection.getInputStream();
		FileOutputStream fos = new FileOutputStream(file);
		byte[] buf = new byte[512];
		while (true) {
			int len = in.read(buf);
			if (len == -1) {
				break;
			}
			fos.write(buf, 0, len);
		}
		in.close();
		fos.flush();
		fos.close();

		CyNetworkReader reader = networkReaderManager.getReader(file.toURI(), file.getName());

		if (cancelled)
			return;

		if (reader == null)
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);

		ShowBuiltNetworkTask showBuiltNetworkTask = new ShowBuiltNetworkTask(reader, networkManager, networkViewFactory, layoutAlgorithmManager, visualMappingManager, networkViewManager, intActVSBuilder);
		insertTasksAfterCurrentTask(reader, showBuiltNetworkTask);



	}

}
