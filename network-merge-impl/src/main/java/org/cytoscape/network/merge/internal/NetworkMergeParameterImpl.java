package org.cytoscape.network.merge.internal;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 *
 * @author gjj
 */
public class NetworkMergeParameterImpl implements NetworkMergeParameter {
        private boolean inNetworkMerge;

        public NetworkMergeParameterImpl() {
        }

        public NetworkMergeParameterImpl(boolean inNetworkMerge) {
                this.inNetworkMerge = inNetworkMerge;
        }

        public boolean inNetworkMergeEnabled() {
                return inNetworkMerge;
        }

        public void enableInNetworkMerge(boolean enabled) {
                inNetworkMerge = enabled;
        }
}
