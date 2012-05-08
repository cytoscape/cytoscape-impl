<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="copy.xsl"/>
   
<xsl:output method="xml" version="1.0" encoding="UTF-8"/>

<xsl:template match="section">
	<xsl:element name="section" use-attribute-sets="create-id">
		<xsl:apply-templates/>
	</xsl:element>
</xsl:template>

<xsl:attribute-set name="create-id">
	<xsl:attribute name="id">
		<xsl:value-of select="title"/>
	</xsl:attribute>
</xsl:attribute-set>

</xsl:stylesheet>
