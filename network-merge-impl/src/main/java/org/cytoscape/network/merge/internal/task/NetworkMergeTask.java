
package org.cytoscape.network.merge.internal.task;

import org.cytoscape.network.merge.internal.AttributeBasedNetworkMerge;
import org.cytoscape.network.merge.internal.NetworkMerge.Operation;
import org.cytoscape.network.merge.internal.model.*;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollector;
import org.cytoscape.network.merge.internal.util.AttributeMerger;
import org.cytoscape.network.merge.internal.util.AttributeValueMatcher;
import org.cytoscape.network.merge.internal.util.DefaultAttributeMerger;
import org.cytoscape.network.merge.internal.util.DefaultAttributeValueMatcher;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author jj
 */

public class NetworkMergeTask extends AbstractTask {
        private CyNetwork network;
	//private MatchingAttribute matchingAttribute;
	//private AttributeMapping nodeAttributeMapping;
	//private AttributeMapping edgeAttributeMapping;
	private List<CyNetwork> selectedNetworkList;
	private Operation operation;
	//private AttributeConflictCollector conflictCollector;
	//private Map<String,Map<String,Set<String>>> selectedNetworkAttributeIDType;
	//private final String tgtType;
	final private AttributeBasedNetworkMerge networkMerge ;  
	//private boolean cancelled;

	/**
	 * Constructor.<br>
	 *
	 */
	public NetworkMergeTask(final CyNetwork network,
				 final MatchingAttribute matchingAttribute,
				 final AttributeMapping nodeAttributeMapping,
				 final AttributeMapping edgeAttributeMapping,
				 final List<CyNetwork> selectedNetworkList,
				 final Operation operation,
				 final AttributeConflictCollector conflictCollector,
				 final Map<String,Map<String,Set<String>>> selectedNetworkAttributeIDType,
				 final String tgtType,
                                 final boolean inNetworkMerge) {
                this.network = network;
		//this.matchingAttribute = matchingAttribute;
		//this.nodeAttributeMapping = nodeAttributeMapping;
		//this.edgeAttributeMapping = edgeAttributeMapping;
		this.selectedNetworkList = selectedNetworkList;
		this.operation = operation;
		//this.mergedNetworkName = mergedNetworkName;
		//this.conflictCollector = conflictCollector;
		//this.selectedNetworkAttributeIDType = selectedNetworkAttributeIDType;
		//this.tgtType = tgtType;
		//cancelled = true;        
        
		final AttributeValueMatcher attributeValueMatcher;
		final AttributeMerger attributeMerger;
		//if (idMapping==null) {
                attributeValueMatcher = new DefaultAttributeValueMatcher();
                attributeMerger = new DefaultAttributeMerger(conflictCollector);
		//        } else {
		//                attributeValueMatcher = new IDMappingAttributeValueMatcher(idMapping);
		//                attributeMerger = new IDMappingAttributeMerger(conflictCollector,idMapping,tgtType);
		//        }

		networkMerge = new AttributeBasedNetworkMerge(
							      matchingAttribute,
							      nodeAttributeMapping,
							      edgeAttributeMapping,
							      attributeMerger,
							      attributeValueMatcher);
                networkMerge.setWithinNetworkMerge(inNetworkMerge);
	}
    
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Executes Task
	 *
	 * @throws
	 * @throws Exception
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
            try {
			networkMerge.setTaskMonitor(taskMonitor);

//			if (selectedNetworkAttributeIDType!=null) {
//				taskMonitor.setStatusMessage("Mapping IDs...");
//				taskMonitor.setProgress(0.0);
//				CyThesaurusServiceClient client = new CyThesaurusServiceMessageBasedClient("AdvanceNetworkMerge");
//				if (!client.isServiceAvailable()) {
//					taskMonitor.setStatus("CyThesaurs service is not available.");
//					taskMonitor.setPercentCompleted(100);
//					return;
//				}
//
//				defineTgtAttributes();
//
//				String mergedAttr = nodeAttributeMapping.getMergedAttribute(0);
//				for (String net : selectedNetworkAttributeIDType.keySet()) {
//					final Set<String> nets = new HashSet<String>(1);
//					nets.add(net);
//					Map<String,Set<String>> mapAttrTypes = selectedNetworkAttributeIDType.get(net);
//					for (String attr : mapAttrTypes.keySet()) {
//						Set<String> types = mapAttrTypes.get(attr);
//						if (!client.mapID(nets, attr, mergedAttr, types, tgtType)) {
//							taskMonitor.setStatus("Failed to map IDs.");
//							taskMonitor.setPercentCompleted(100);
//							return;
//						}
//					}
//				}
//
//				matchingAttribute.clear();
//				for (String net : selectedNetworkAttributeIDType.keySet()) {
//					matchingAttribute.putAttributeForMatching(net, mergedAttr);
//					nodeAttributeMapping.setOriginalAttribute(net, mergedAttr, 0);
//				}
//
//			}
                        
                        networkMerge.mergeNetwork(network, selectedNetworkList, operation);            

			/*
			  cytoscape.view.CyNetworkView networkView = Cytoscape.getNetworkView(mergedNetworkName);

			  // get the VisualMappingManager and CalculatorCatalog
			  cytoscape.visual.VisualMappingManager manager = Cytoscape.getVisualMappingManager();
			  cytoscape.visual.CalculatorCatalog catalog = manager.getCalculatorCatalog();

			  cytoscape.visual.VisualStyle vs = catalog.getVisualStyle(mergedNetworkName+" Visual Style");
			  if (vs == null) {
			  // if not, create it and add it to the catalog
			  //vs = createVisualStyle(networkMerge);
			  cytoscape.visual.NodeAppearanceCalculator nodeAppCalc = new cytoscape.visual.NodeAppearanceCalculator();
			  cytoscape.visual.mappings.PassThroughMapping pm = new cytoscape.visual.mappings.PassThroughMapping(new String(), cytoscape.data.Semantics.CANONICAL_NAME);

			  cytoscape.visual.calculators.Calculator nlc = new cytoscape.visual.calculators.BasicCalculator(null,
			  pm, cytoscape.visual.VisualPropertyType.NODE_LABEL);
			  nodeAppCalc.setCalculator(nlc);

			  vs.setNodeAppearanceCalculator(nodeAppCalc);

			  catalog.addVisualStyle(vs);
			  }
			  // actually apply the visual style
			  manager.setVisualStyle(vs);
			  networkView.redrawGraph(true,true);
			*/

			//            taskMonitor.setPercentCompleted(100);
			//            taskMonitor.setStatus("The selected networks were successfully merged into network '"
			//                                  + mergedNetwork.getTitle()
			//                                  + "' with "
			//                                  + conflictCollector.getMapToIDAttr().size()
			//                                  + " attribute conflicts.");

		} catch(Exception e) {
			throw new Exception(e);
		}
		
        
	}
}
