package org.cytoscape.cpath2.internal.task;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.StaxHack;
import org.cytoscape.cpath2.internal.util.AttributeUtil;
import org.cytoscape.cpath2.internal.util.BioPaxUtil;
import org.cytoscape.cpath2.internal.util.SelectUtil;
import org.cytoscape.cpath2.internal.web_service.CPathException;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.cpath2.internal.web_service.CPathResponseFormat;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.cpath2.internal.web_service.EmptySetException;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.CyLayoutContext;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for Executing a Get Record(s) by CPath ID(s) command.
 * 
 * @author Ethan Cerami.
 */
public class ExecuteGetRecordByCPathId extends AbstractTask {
	private CPathWebService webApi;
	private long ids[];
	private String networkTitle;
	private boolean haltFlag = false;
	private CyNetwork mergedNetwork;
	private CPathResponseFormat format;
	private final static String CPATH_SERVER_NAME_ATTRIBUTE = "CPATH_SERVER_NAME";
	private final static String CPATH_SERVER_DETAILS_URL = "CPATH_SERVER_DETAILS_URL";
	private Logger logger = LoggerFactory.getLogger(ExecuteGetRecordByCPathId.class);
	private final CPath2Factory cPathFactory;
	private final VisualMappingManager mappingManager;


	/**
	 * Constructor.
	 * 
	 * @param webApi cPath Web API.
	 * @param ids Array of cPath IDs.
	 * @param format CPathResponseFormat Object.
	 * @param networkTitle Tentative Network Title.
	 * @param mergedNetwork Network to merge into.
	 * @param mapperFactory
	 * @param mapBioPaxToCytoscape
	 * @param viewManager
	 * @param application
	 */
	public ExecuteGetRecordByCPathId(
			CPathWebService webApi, 
			long ids[], 
			CPathResponseFormat format,
			String networkTitle, 
			CyNetwork mergedNetwork, 
			CPath2Factory cPathFactory,  
			VisualMappingManager mappingManager) 
	{
		this.webApi = webApi;
		this.ids = ids;
		this.format = format;
		this.networkTitle = networkTitle;
		this.mergedNetwork = mergedNetwork;
		this.cPathFactory = cPathFactory;
		this.mappingManager = mappingManager;
	}

	/**
	 * Our implementation of Task.abort()
	 */
	public void cancel() {
		webApi.abort();
		haltFlag = true;
	}

	/**
	 * Our implementation of Task.getTitle.
	 * 
	 * @return Task Title.
	 */
	public String getTitle() {
		return "Retrieving " + networkTitle + " from " + CPathProperties.getInstance().getCPathServerName() + "...";
	}

	/**
	 * Our implementation of Task.run().
	 * 
	 * @throws Exception
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {
		String title = "Retrieving " + networkTitle + " from " + CPathProperties.getInstance().getCPathServerName()
				+ "...";
		taskMonitor.setTitle(title);
		try {
			// read the network from cpath instance
			if (taskMonitor != null) {
				taskMonitor.setProgress(0);
				taskMonitor.setStatusMessage("Retrieving " + networkTitle + ".");
			}

			// Get and Store BioPAX or SIF data to a temp file
			String tmpDir = System.getProperty("java.io.tmpdir");
			// Branch based on download mode setting.
			File tmpFile;
			if (format == CPathResponseFormat.BIOPAX) {
				tmpFile = File.createTempFile("temp", ".xml", new File(tmpDir));
			} else {
				tmpFile = File.createTempFile("temp", ".sif", new File(tmpDir));
			}
			tmpFile.deleteOnExit();
			// Get Data, and write to temp file.
			String data = webApi.getRecordsByIds(ids, format, taskMonitor);
			FileWriter writer = new FileWriter(tmpFile);
			writer.write(data);
			writer.close();

			// read the network from the temp file (the reader is auto-detected by the file extension or content)
			CyNetworkReader reader = cPathFactory.getCyNetworkViewReaderManager()
					.getReader(tmpFile.toURI(), tmpFile.getName());
			if (taskMonitor != null) {
				taskMonitor.setStatusMessage("Creating Cytoscape Network...");
				taskMonitor.setProgress(0);
			}

			reader.run(taskMonitor);
			final CyNetwork cyNetwork = reader.getNetworks()[0];
			AttributeUtil.set(cyNetwork, cyNetwork, CyNetwork.NAME, networkTitle, String.class);
			
            final CyNetworkView view = reader.buildCyNetworkView(cyNetwork);

            cPathFactory.getCyNetworkManager().addNetwork(cyNetwork);
            cPathFactory.getCyNetworkViewManager().addNetworkView(view);

			cPathFactory.getCyNetworkManager().addNetwork(cyNetwork);
			cPathFactory.getCyNetworkViewManager().addNetworkView(view);

			// Branch, based on download mode.
			if (format == CPathResponseFormat.BINARY_SIF) {
				postProcessingBinarySif(view, taskMonitor);
			} else {
				postProcessingBioPAX(view, taskMonitor);
			}

			// Fire appropriate network event.
			// TODO: Port this?
			// if (mergedNetwork == null) {
			// // Fire a Network Loaded Event
			// Object[] ret_val = new Object[2];
			// ret_val[0] = cyNetwork;
			// ret_val[1] = networkTitle;
			// Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null,
			// ret_val);
			// } else {
			// // Fire a Network Modified Event; causes Quick Find to Re-Index.
			// Object[] ret_val = new Object[2];
			// ret_val[0] = mergedNetwork;
			// ret_val[1] = networkTitle;
			// Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null,
			// ret_val);
			// }

			// Add Links Back to cPath Instance
			addLinksToCPathInstance(cyNetwork);

			if (taskMonitor != null) {
				taskMonitor.setStatusMessage("Done");
				taskMonitor.setProgress(1.0);
			}

			CyLayoutAlgorithmManager layoutManager = cPathFactory.getCyLayoutAlgorithmManager();
			CyLayoutAlgorithm layout = layoutManager.getDefaultLayout();
			CyLayoutContext context = layout.createLayoutContext();
			Set<View<CyNode>> nodes = Collections.emptySet();
			insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, nodes));

		} catch (IOException e) {
			throw new Exception("Failed to retrieve records.", e);
		} catch (EmptySetException e) {
			throw new Exception("No matches found for your request.  ", e);
		} catch (CPathException e) {
			if (e.getErrorCode() != CPathException.ERROR_CANCELED_BY_USER) {
				throw e;
			}
		}
	}

	/**
	 * Add Node Links Back to cPath Instance.
	 * 
	 * @param cyNetwork
	 *            CyNetwork.
	 */
	private void addLinksToCPathInstance(CyNetwork cyNetwork) {
		CPathProperties props = CPathProperties.getInstance();
		String serverName = props.getCPathServerName();
		String serverURL = props.getCPathUrl();
		CyRow row = cyNetwork.getRow(cyNetwork);
		String cPathServerDetailsUrl = row.get(ExecuteGetRecordByCPathId.CPATH_SERVER_DETAILS_URL, String.class);
		if (cPathServerDetailsUrl == null) {
			AttributeUtil.set(cyNetwork, cyNetwork, ExecuteGetRecordByCPathId.CPATH_SERVER_NAME_ATTRIBUTE, serverName,
					String.class);
			String url = serverURL.replaceFirst("webservice.do", "record2.do?id=");
			AttributeUtil.set(cyNetwork, cyNetwork, ExecuteGetRecordByCPathId.CPATH_SERVER_DETAILS_URL, url, String.class);
		}
	}

	/**
	 * Execute Post-Processing on BINARY SIF Network.
	 * 
	 * @param cyNetwork
	 *            Cytoscape Network Object.
	 */
	private void postProcessingBinarySif(final CyNetworkView view, TaskMonitor taskMonitor) {
		final CyNetwork cyNetwork = view.getModel();

		// Set the Quick Find Default Index
		AttributeUtil.set(cyNetwork, cyNetwork, "quickfind.default_index", CyNetwork.NAME, String.class);

		// Specify that this is a BINARY_NETWORK
		AttributeUtil.set(cyNetwork, cyNetwork, "BIOPAX_NETWORK", Boolean.TRUE, Boolean.class);

		// Get all node details.
		getNodeDetails(cyNetwork, taskMonitor);

		if (haltFlag == false) {
			if (mergedNetwork != null) {
				mergeNetworks(cyNetwork, taskMonitor);
			} else {
				// } else if (cyNetwork.getNodeCount() <
				// Integer.parseInt(CytoscapeInit.getProperties()
				// .getProperty("viewThreshold"))) {
				if (taskMonitor != null) {
					taskMonitor.setStatusMessage("Creating Network View...");
					taskMonitor.setProgress(0);
				}

				VisualStyle visualStyle = cPathFactory.getBinarySifVisualStyleUtil().getVisualStyle();
				mappingManager.setVisualStyle(visualStyle, view);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String networkTitleWithUnderscores = networkTitle.replaceAll(": ", "");
						networkTitleWithUnderscores = networkTitleWithUnderscores.replaceAll(" ", "_");
						CyNetworkNaming naming = cPathFactory.getCyNetworkNaming();
						networkTitleWithUnderscores = naming.getSuggestedNetworkTitle(networkTitleWithUnderscores);
						AttributeUtil.set(cyNetwork, cyNetwork, CyNetwork.NAME, networkTitleWithUnderscores, String.class);
					}
				});
			}
		} else {
			// If we have requested a halt, and we have a network, destroy it.
			// if (cyNetwork != null) {
			// Cytoscape.destroyNetwork(cyNetwork);
			// }
		}
	}

	/**
	 * Execute Post-Processing on BioPAX Network.
	 * 
	 * @param cyNetwork
	 *            Cytoscape Network Object.
	 */
	private void postProcessingBioPAX(final CyNetworkView view, TaskMonitor taskMonitor) {
		final CyNetwork cyNetwork = view.getModel();

		if (haltFlag == false) {
			if (mergedNetwork != null) {
				mergeNetworks(cyNetwork, taskMonitor);
			} else {
				// } else if (cyNetwork.getNodeCount() <
				// Integer.parseInt(CytoscapeInit.getProperties()
				// .getProperty("viewThreshold"))) {
				if (taskMonitor != null) {
					taskMonitor.setStatusMessage("Creating Network View...");
					taskMonitor.setProgress(0);
				}

				// Set up the right visual style
				// VisualStyle visualStyle =
				// BioPaxVisualStyleUtil.getBioPaxVisualStyle();

				// Set up the right layout algorithm.
				// LayoutUtil layoutAlgorithm = new LayoutUtil();

				// Now, create the view.
				// Use local create view option, so that we don't mess up the
				// visual style.
				// CyNetworkView view = createNetworkView(cyNetwork,
				// cyNetwork.getCyRow(cyNetwork).get(CyNetwork.NAME, String.class),
				// layoutAlgorithm, null);

				// Now apply the visual style;
				// Doing this as a separate step ensures that the visual style
				// appears
				// in the visual style drop-down menu.
				// view.applyVizmapper(visualStyle);
			}
		} else {
			// If we have requested a halt, and we have a network, destroy it.
			// TODO: Review: Network hasn't been added to manager at this point
			// so we don't need to do the following, right?
			// if (cyNetwork != null) {
			// Cytoscape.destroyNetwork(cyNetwork);
			// }
		}
	}

	private void mergeNetworks(CyNetwork cyNetwork, TaskMonitor taskMonitor) {
		// TODO: Do we need to clone nodes/edges when merging?
		taskMonitor.setStatusMessage("Merging Network...");
		Map<String, CyNode> nodes = new HashMap<String, CyNode>();
		for (CyNode node : cyNetwork.getNodeList()) {
			CyNode newNode = mergedNetwork.addNode();
			AttributeUtil.copyAttributes(mergedNetwork, node, newNode);
			String name = cyNetwork.getRow(node).get(CyNetwork.NAME, String.class);
			nodes.put(name, newNode);
		}
		Set<CyEdge> edges = new HashSet<CyEdge>();
		for (CyEdge edge : cyNetwork.getEdgeList()) {
			String sourceName = cyNetwork.getRow(edge.getSource()).get(CyNetwork.NAME, String.class);
			String targetName = cyNetwork.getRow(edge.getTarget()).get(CyNetwork.NAME, String.class);
			CyNode source = nodes.get(sourceName);
			CyNode target = nodes.get(targetName);
			CyEdge newEdge = mergedNetwork.addEdge(source, target, true);
			AttributeUtil.copyAttributes(mergedNetwork, edge, newEdge);
			edges.add(newEdge);
		}

		// // Select this view
		// final CyNetworkView networkView =
		// viewManager.getNetworkView((mergedNetwork.getSUID());
		// Cytoscape.setCurrentNetwork(mergedNetwork.getIdentifier());
		// Cytoscape.setCurrentNetworkView(mergedNetwork.getIdentifier());

		// Select only the new nodes
		SelectUtil.unselectAllNodes(mergedNetwork);
		SelectUtil.unselectAllEdges(mergedNetwork);
		SelectUtil.setSelectedNodeState(mergedNetwork,nodes.values(), true);
		SelectUtil.setSelectedEdgeState(mergedNetwork,edges, true);

		// Delete the temp network.
		// Cytoscape.destroyNetwork(cyNetwork);

		// Apply Layout
		Object[] options = { "Yes", "No" };
		int n = JOptionPane.showOptionDialog(cPathFactory.getCySwingApplication().getJFrame(),
				"Would you like to layout the modified network?", "Adjust Layout?", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (n == 0) {
			// SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			// LayoutUtil layoutAlgorithm = new LayoutUtil();
			// networkView.applyLayout(layoutAlgorithm);
			// networkView.fitContent();
			// }
			// });
		}
	}

	/**
	 * Gets Details for Each Node from Web Service API.
	 */
	private void getNodeDetails(CyNetwork cyNetwork, TaskMonitor taskMonitor) {
		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Retrieving node details...");
			taskMonitor.setProgress(0);
		}
		List<List<CyNode>> batchList = createBatchArray(cyNetwork);
		if (batchList.size() == 0) {
			logger.info("Skipping node details.  Already have all the details new need.");
		}
		
		for (int i = 0; i < batchList.size(); i++) {
			if (haltFlag == true) {
				break;
			}
			List<CyNode> currentList = batchList.get(i);
			logger.debug("Getting node details, batch:  " + i);
			long ids[] = new long[currentList.size()];
			Map<String, CyNode> nodes = new HashMap<String, CyNode>();
			for (int j = 0; j < currentList.size(); j++) {
				CyNode node = currentList.get(j);
				String name = cyNetwork.getRow(node).get(CyNetwork.NAME, String.class);
				// 'name' is actually a CPATH-ID (for SIF imports)
				nodes.put(name, node);
				ids[j] = Long.valueOf(name);
			}
			try {
				final String xml = webApi.getRecordsByIds(ids, CPathResponseFormat.BIOPAX, new NullTaskMonitor());
				Model model = convertFromOwl(new ByteArrayInputStream(xml.getBytes()));
				// convert L2 to L3 if required (L1 is converted to L2 always anyway - by the handler)
				if(BioPAXLevel.L2.equals(model.getLevel())) { // 
					model = (new OneTwoThree()).filter(model);
				}
				//normalize/infer properties: displayName, organism, dataSource
				fixDisplayName(model);
				ModelUtils mu = new ModelUtils(model);
				mu.inferPropertyFromParent("dataSource");
				mu.inferPropertyFromParent("organism");
				//map biopax properties to Cy attributes for SIF nodes
				for (BioPAXElement e : model.getObjects()) {
					if(e instanceof EntityReference 
							|| e instanceof Complex 
								|| e.getModelInterface().equals(PhysicalEntity.class)) {
						String cpathId = e.getRDFId().replaceFirst(model.getXmlBase(), "");
						if (cpathId != null) {
							cpathId = cpathId.replaceAll("CPATH-", "");
							CyNode node = nodes.get(cpathId);
							if(node != null)
								BioPaxUtil.createAttributesFromProperties(e, node, cyNetwork);
							// - this will also update the 'name' attribute (to a biol. label)
							else {
								logger.debug("Oops: no node for " + e.getRDFId());
							}
						}
					}
				}
				
				double percentComplete = i / (double) batchList.size();
				if (taskMonitor != null) {
					taskMonitor.setProgress(percentComplete);
				}
			} catch (EmptySetException e) {
				e.printStackTrace();
			} catch (CPathException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Model convertFromOwl(final InputStream stream) {
		final Model[] model = new Model[1];
		StaxHack.runWithHack(new Runnable() {
			@Override
			public void run() {
				model[0] = (new SimpleIOHandler()).convertFromOWL(stream);
			}
		});
		return model[0];
	}

	private List<List<CyNode>> createBatchArray(CyNetwork cyNetwork) {
		int max_ids_per_request = 50;
		List<List<CyNode>> masterList = new ArrayList<List<CyNode>>();
		List<CyNode> currentList = new ArrayList<CyNode>();
		int counter = 0;
		for (CyNode node : cyNetwork.getNodeList()) {
			CyRow row = cyNetwork.getRow(node);
			String label = row.get(CyNetwork.NAME, String.class);
			if (label != null) {
				currentList.add(node);
				counter++;
			}
			if (counter > max_ids_per_request) {
				masterList.add(currentList);
				currentList = new ArrayList<CyNode>();
				counter = 0;
			}
		}
		if (currentList.size() > 0) {
			masterList.add(currentList);
		}
		return masterList;
	}

	// private CyNetworkView createNetworkView (CyNetwork network, String title,
	// CyLayoutAlgorithm
	// layout, VisualStyle vs) {
	//
	// if (viewManager.viewExists((network.getSUID()))) {
	// return Cytoscape.getNetworkView(network.getIdentifier());
	// }
	//
	// final DingNetworkView view = new DingNetworkView(network, title);
	// view.setGraphLOD(new CyGraphLOD());
	// view.setIdentifier(network.getIdentifier());
	// view.setTitle(network.getTitle());
	// Cytoscape.getNetworkViewMap().put(network.getIdentifier(), view);
	// Cytoscape.setSelectionMode(Cytoscape.getSelectionMode(), view);
	//
	// VisualMappingManager VMM = Cytoscape.getVisualMappingManager();
	// if (vs != null) {
	// view.setVisualStyle(vs.getName());
	// VMM.setVisualStyle(vs);
	// VMM.setNetworkView(view);
	// }
	//
	// if (layout == null) {
	// layout = CyLayouts.getDefaultLayout();
	// }
	//
	// Cytoscape.firePropertyChange(cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED,
	// null, view);
	// layout.doLayout(view);
	// view.fitContent();
	// view.redrawGraph(false, true);
	// return view;
	// }
	

	class NullTaskMonitor implements TaskMonitor {
		@Override
		public void setProgress(double arg0) {
		}

		@Override
		public void setStatusMessage(String arg0) {
		}

		@Override
		public void setTitle(String arg0) {
		}
	}

	private void fixDisplayName(Model model) {
		if (logger.isInfoEnabled())
			logger.info("Trying to auto-fix 'null' displayName...");
		// where it's null, set to the shortest name if possible
		for (Named e : model.getObjects(Named.class)) {
			if (e.getDisplayName() == null) {
				if (e.getStandardName() != null) {
					e.setDisplayName(e.getStandardName());
				} else if (!e.getName().isEmpty()) {
					String dsp = e.getName().iterator().next();
					for (String name : e.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					e.setDisplayName(dsp);
				}
			}
		}
		// if required, set PE name to (already fixed) ER's name...
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
				if(spe.getDisplayName() == null || spe.getDisplayName().trim().length() == 0) {
					if(er.getDisplayName() != null && er.getDisplayName().trim().length() > 0) {
						spe.setDisplayName(er.getDisplayName());
					}
				}
			}
		}
	}
		
}
