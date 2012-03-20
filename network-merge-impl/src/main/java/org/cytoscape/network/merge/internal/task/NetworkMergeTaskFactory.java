
package org.cytoscape.network.merge.internal.task;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.network.merge.internal.NetworkMerge.Operation;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollector;
import org.cytoscape.network.merge.internal.model.AttributeMapping;
import org.cytoscape.network.merge.internal.model.MatchingAttribute;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 *
 * @author jj
 */
public class NetworkMergeTaskFactory extends AbstractTaskFactory {
    final CyNetwork network;
    final MatchingAttribute matchingAttribute;
    final AttributeMapping nodeAttributeMapping;
    final AttributeMapping edgeAttributeMapping;
    final List<CyNetwork> selectedNetworkList;
    final Operation operation;
    final AttributeConflictCollector conflictCollector;
    final Map<String,Map<String,Set<String>>> selectedNetworkAttributeIDType;
    final String tgtType;
    final boolean inNetworkMerge;

    public NetworkMergeTaskFactory(CyNetwork network, 
            MatchingAttribute matchingAttribute, 
            AttributeMapping nodeAttributeMapping, 
            AttributeMapping edgeAttributeMapping, 
            List<CyNetwork> selectedNetworkList, 
            Operation operation, 
            AttributeConflictCollector conflictCollector, 
            Map<String, Map<String, Set<String>>> selectedNetworkAttributeIDType, 
            String tgtType, 
            boolean inNetworkMerge) {
        this.network = network;
        this.matchingAttribute = matchingAttribute;
        this.nodeAttributeMapping = nodeAttributeMapping;
        this.edgeAttributeMapping = edgeAttributeMapping;
        this.selectedNetworkList = selectedNetworkList;
        this.operation = operation;
        this.conflictCollector = conflictCollector;
        this.selectedNetworkAttributeIDType = selectedNetworkAttributeIDType;
        this.tgtType = tgtType;
        this.inNetworkMerge = inNetworkMerge;
    }
    
    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new NetworkMergeTask(network, matchingAttribute, nodeAttributeMapping,
                edgeAttributeMapping, selectedNetworkList, operation, conflictCollector,
                selectedNetworkAttributeIDType, tgtType, inNetworkMerge));
    }
}
