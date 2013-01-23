package org.cytoscape.cpath2.internal.view.model;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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
 * Encapsulates Organism Information.
 *
 * @author Ethan Cerami
 */
public class Organism {
    private String commonName;
    private String speciesName;
    private int ncbiTaxonomyId;

    /**
     * Constructor.
     * @param speciesName     Organism Species Name.
     * @param ncbiTaxonomyId  NCBI Taxonomy ID.
     */
    public Organism (String speciesName, int ncbiTaxonomyId) {
        this.speciesName = speciesName;
        this.ncbiTaxonomyId = ncbiTaxonomyId;
    }

    /**
     * Empty-Arg Constructor.
     */
    public Organism() {
    }

    /**
     * Gets Organism Common Name.
     * @return organism common name.
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * Sets Organism Common Name.
     * @param commonName organism common name.
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Gets the Species Name.
     * @return species name.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Sets the Species Name.
     * @param speciesName species name.
     */
    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    /**
     * Gets Organism NCBI Taxonomy ID.
     * @return NCBI Taxonomy ID.
     */
    public int getNcbiTaxonomyId() {
        return ncbiTaxonomyId;
    }

    /**
     * Sets Organism NCBI Taxonomy ID.
     * @param ncbiTaxonomyId NCBI Taxonomy ID.
     */
    public void setNcbiTaxonomyId(int ncbiTaxonomyId) {
        this.ncbiTaxonomyId = ncbiTaxonomyId;
    }

    /**
     * Over-rides toString() to return common name.
     * @return Organism common name.
     */
    public String toString() {
        return speciesName;
    }
}
