package org.cytoscape.tableimport.internal.reader.ontology;

/**
 * Tags used in OBO file:<br>
 * <a href="http://www.geneontology.org/GO.format.shtml#oboflat">OBO</a>
 */
public enum OBOTags {

	/*
	 * Descriptions for the tags.
	 */
	ID("The unique id of the current term. This can be any string. This tag must always be the first tag in any term description."),
	NAME("The term name. Any term may only have ONE name defined. If multiple term names are defined, it is a parse error."), 
	ALT_ID("Defines an alternate id for this term. A term may have any number of alternate ids."), 
	NAMESPACE("The namespace in which the term belongs. If this tag is not present, the term will be assigned to the "
	          + "'default-namespace' specified in the file header stanza."), 
	DEF("The definition of the current term. There must be zero or one instances of this tag per term description. "
	    + "More than one definition for a term generates a parse error. The value of this tag should be the quote "
	    + "enclosed definition text, followed by a dbxref list containing dbxrefs that describe the origin of this "
	    + "definition.  The origine is stored in a separate attribute called def_origin."), 
	COMMENT("A comment for this term. There must be zero or one instances of this tag per term description. More than "
	        + "one comment for a term generates a parse error."), 
	SUBSET("This tag indicates a term subset to which this term belongs. The value of this tag must be a subset name as "
	       + "defined in a subsetdef tag in the file header. If the value of this tag is not mentioned in a subsetdef tag, "
	       + "a parse error will be generated. A term may belong to any number of subsets."), 
	SYNONYM("This tag gives a synonym for the term; whether the synonym is exact, broad, narrow, or otherwise related "
	        + "to the term is not specified. The value of this tag should be the quote enclosed synonym text, followed by "
	        + "an optional dbxref list containing dbxrefs that describe the origin of this synonym (see Dbxref Formatting "
	        + "for information on how dbxref lists are encoded). A term may have any number of synonyms."), 
	RELATED_SYNONYM("This tag gives a synonym for the term of the specified type."), 
	EXACT_SYNONYM("This tag gives a synonym for the term of the specified type."), 
	BROAD_SYNONYM("This tag gives a synonym for the term of the specified type."), 
	NARROW_SYNONYM("This tag gives a synonym for the term of the specified type."), 
	XREF("A dbxref that describes an analogous object in another vocabulary (see Dbxref Formatting for information "
	     + "about how the value of this tag must be formatted). A term may have any number of analogous xrefs. "), 
	XREF_ANALOG("A dbxref that describes an analogous object in another vocabulary (see Dbxref Formatting for information "
	            + "about how the value of this tag must be formatted). A term may have any number of analogous xrefs. "), 
	XREF_UNKNOWN("A dbxref with an unknown type."), 
	IS_A("This tag describes a subclassing relationship between one term and another. A term may have any number of is_a "
	     + "relationships. Terms with no is_a relationships are roots. A term with no is_a relationships may not specify "
	     + "any relationship tags."), 
	RELATIONSHIP("This tag describes a typed relationship between this term and another term. The value of this tag should "
	             + "be the relationship type id, and then the id of the target term. The relationship type name must be a "
	             + "relationship type name as defined in a typedef tag stanza. The [typedef] must either occur in a document in the "
	             + "current parse batch, or in a file imported via a typeref header tag. If the relationship type name is undefined, "
	             + "a parse error will be generated. If the id of the target term cannot be resolved by the end of parsing the current "
	             + "batch of files, this tag describes a \"dangling reference\"."), 
	IS_OBSOLETE("This tag indicates whether or not the term is obsolete. Allowable values are true and false (false is assumed "
	            + "if this tag is not present). Obsolete terms must have NO relationships, and no defined is_a tags."), 
	USE_TERM("This tag indicates which term to use instead of an obsolete term. The value of this tag is the id of another term. "
	         + "If the tag value refers to a term that is not specified in the current load batch, it is a \"dangling reference\" "
	         + "(see Parser Requirements). If this tag is specified and the \"is_obsolete\" value for the current term is not true, "
	         + "a parse error will be generated. This tag is not required for terms that specify the is_obsolete tag, but it is "
	         + "recommended (some parsers may choose to issue warnings about obsolete terms that do not specify a replacement term). "
	         + "An obsolete term may have any number of use_term tags."), 
	DOMAIN("This tag determines the children that can be assigned to relationships with this type. If the domain is set, term "
	       + "relationships with this type may only have children that are the same as, or subclasses of, the domain term."), 
	RANGE("This tag specifies the parents that can be assigned to relationships with this type. If the range is set, term "
	      + "relationships with this type may only have parents that are the same as, or subclasses of, the range term."), 
	IS_CYCLIC("This tag indicates that it is legal to create cycles out of this relationship."), 
	IS_TRANSITIVE("This tag indicates that the relationship is marked as transitive. This information is very useful to "
	              + "reasoners and other automatic traversals of the graph."), 
	IS_SYMMETRIC("This tag indicates that the relationship is marked as symmetric (meaning that if the relationship holds from "
	             + "the child to parent, it also holds from parent to child). This information is very useful to reasoners and other "
	             + "automatic traversals of the graph."), 
	DISJOINT_FROM("This tag indicates that a term is disjoint from another, meaning that the two terms have no instances or " +
			"subclasses in common. The value is the id of the term from which the current term is disjoint. This tag may not be " +
			"applied to relationship types."),

	/*
	 * This is a local tag defined by Kei.  This is a reference extracted from "DEF" tag.
	 */
	DEF_ORIGIN("A dbxref list containing dbxrefs that describe the origin of this definition.");
	/*
	 * Description String
	 */
	private String description;

	private OBOTags(String description) {
		this.description = description;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getDescription() {
		return description;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String toString() {
		return name().toLowerCase();
	}

	
	public static String getPrefix() {
		return "ontology";
	}
}
