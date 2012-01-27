/*
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.psi_mi.internal.data_mapper;

import org.cytoscape.psi_mi.internal.model.ExternalReference;
import org.cytoscape.psi_mi.internal.schema.mi1.ProteinInteractorType;
import org.cytoscape.psi_mi.internal.schema.mi25.InteractorElementType;
import org.cytoscape.psi_mi.internal.schema.mi25.NamesType;


/**
 * Mapper Utility Class.
 *
 * @author Ethan Cerami.
 */
public class MapperUtil {
	/**
	 * If there is no short label, we need to determine an interactor
	 * name from an External Reference or the full name.
	 * <p/>
	 * Here are the rules:
	 * <OL>
	 * <LI>If an interactor has a short label, we use it.
	 * <LI>Otherwise, try to use a SWISS-PROT or UNIPROT ID.
	 * <LI>Otherwise, try to use the FullName.
	 * <LI>Otherwise, try to use XML ID.
	 * <LI>Otherwise, throw a MapperException.
	 * </OL>
	 *
	 * @param interactor ProteinInteractor
	 * @param refs       Array of External Reference Objects.
	 * @return Name Substitute.
	 * @throws MapperException Error in Mapping.
	 */
	public static String extractName(InteractorElementType interactor, ExternalReference[] refs)
	    throws MapperException {
		String shortLabel = null;
		String fullName = null;
		NamesType names = interactor.getNames();

		if (names != null) {
			shortLabel = names.getShortLabel();
			fullName = names.getFullName();
		}

		if ((shortLabel != null) && (shortLabel.trim().length() > 0)) {
			return shortLabel;
		} else {
			if (refs != null) {
				for (int i = 0; i < refs.length; i++) {
					String dbName = refs[i].getDatabase();

					if (dbName.equals("SWP") || dbName.equals("SWISS-PROT")
					    || dbName.equalsIgnoreCase("SwissProt")
					    || dbName.equalsIgnoreCase("UniProt")) {
						return refs[i].getId();
					}
				}
			}
		}

		if ((fullName != null) && (fullName.trim().length() > 0)) {
			return fullName;
		} else if ((("" + interactor.getId()) != null)
		           && (("" + interactor.getId()).trim().length() > 0)) {
			return "" + interactor.getId();
		} else {
			throw new MapperException("Unable to determine name" + "for interactor:  "
			                          + interactor.getId());
		}
	}

	/**
	 * Exracts Interactor Name.
	 * @param interactor Interactor Object.
	 * @param refs Array of External References.
	 * @return Interaction name.
	 * @throws MapperException Mapper Error.
	 */
	public static String extractName(ProteinInteractorType interactor, ExternalReference[] refs)
	    throws MapperException {
		String shortLabel = null;
		String fullName = null;
		org.cytoscape.psi_mi.internal.schema.mi1.NamesType names = interactor.getNames();

		if (names != null) {
			shortLabel = names.getShortLabel();
			fullName = names.getFullName();
		}

		if ((shortLabel != null) && (shortLabel.trim().length() > 0)) {
			return shortLabel;
		} else {
			if (refs != null) {
				for (int i = 0; i < refs.length; i++) {
					String dbName = refs[i].getDatabase();

					if (dbName.equals("SWP") || dbName.equals("SWISS-PROT")
					    || dbName.equalsIgnoreCase("SwissProt")
					    || dbName.equalsIgnoreCase("UniProt")) {
						return refs[i].getId();
					}
				}
			}
		}

		if ((fullName != null) && (fullName.trim().length() > 0)) {
			return fullName;
		} else if ((interactor.getId() != null) && (interactor.getId().trim().length() > 0)) {
			return interactor.getId();
		} else {
			throw new MapperException("Unable to determine name" + "for interactor:  "
			                          + interactor.getId());
		}
	}
	
	public static String normalizeText(String text) {
		StringBuilder builder = new StringBuilder();
		int lastNonSpaceIndex = 0;
		boolean processingLeadingSpace = true;
		
		for (int i = 0; i < text.length(); i++) {
			char character = text.charAt(i);
			boolean isWhitespace = Character.isWhitespace(character);
			if (processingLeadingSpace && isWhitespace) {
				continue;
			}
			if (processingLeadingSpace && !isWhitespace) {
				processingLeadingSpace = false;
				builder.append(character);
				lastNonSpaceIndex = i;
				continue;
			}
			
			if (!isWhitespace && i - lastNonSpaceIndex > 1) {
				builder.append(" ");
			}
			
			if (!isWhitespace) {
				lastNonSpaceIndex = i;
				builder.append(character);
			}
		}
		return builder.toString();
	}
	
	// TODO: Remove
	public static void main(String[] args) {
		for (String string : new String[] {
			"",
			" a",
			"   b",
			"c",
			"d ",
			"d   ",
			"   e   ",
			"f f",
			"g   g",
			"   h   h   ",
		}) {
			System.out.printf("[%s]\n", normalizeText(string));
		}
	}
}
